package org.example.springboot3java21demo.orderpay.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.example.springboot3java21demo.orderpay.config.OrderPayProperties;
import org.example.springboot3java21demo.orderpay.domain.OrderTimeoutCommand;
import org.example.springboot3java21demo.orderpay.domain.OutboxRecord;
import org.example.springboot3java21demo.common.trace.TraceConstants;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "order-pay.rocketmq", name = "enabled", havingValue = "true")
public class OrderPayRocketMqClient {

    private final OrderPayProperties properties;
    private final OrderTimeoutMessageListener listener;
    private final ObjectMapper objectMapper;

    private volatile ClientServiceProvider provider;
    private volatile Producer producer;
    private volatile PushConsumer consumer;

    public OrderPayRocketMqClient(
            OrderPayProperties properties,
            OrderTimeoutMessageListener listener,
            ObjectMapper objectMapper) {
        this.properties = properties;
        this.listener = listener;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        OrderPayProperties.RocketMq rocket = properties.getRocketmq();
        try {
            provider = ClientServiceProvider.loadService();
            ClientConfiguration clientConfiguration = ClientConfiguration.newBuilder()
                    .setEndpoints(rocket.getEndpoint())
                    .setRequestTimeout(Duration.ofMillis(rocket.getRequestTimeoutMillis()))
                    .enableSsl(false)
                    .build();
            producer = provider.newProducerBuilder()
                    .setClientConfiguration(clientConfiguration)
                    .setTopics(rocket.getTopic())
                    .setMaxAttempts(3)
                    .build();
            consumer = provider.newPushConsumerBuilder()
                    .setClientConfiguration(clientConfiguration)
                    .setConsumerGroup(rocket.getConsumerGroup())
                    .setSubscriptionExpressions(Map.of(
                            rocket.getTopic(), FilterExpression.SUB_ALL))
                    .setConsumptionThreadCount(2)
                    .setMessageListener(listener)
                    .build();
            log.info("订单支付 RocketMQ 已启动 endpoint={}, timeoutTopic={}, consumerGroup={}",
                    rocket.getEndpoint(), rocket.getTopic(), rocket.getConsumerGroup());
        } catch (Exception ex) {
            log.error("订单支付 RocketMQ 启动失败，Outbox 将保留消息并持续重试 endpoint={}",
                    rocket.getEndpoint(), ex);
        }
    }

    public void sendTimeoutCommand(OutboxRecord event) throws Exception {
        if (producer == null || provider == null) {
            throw new IllegalStateException("RocketMQ Producer 尚未就绪");
        }
        OrderTimeoutCommand command = objectMapper.readValue(
                event.payload(), OrderTimeoutCommand.class);
        MessageBuilder builder = provider.newMessageBuilder()
                .setTopic(properties.getRocketmq().getTopic())
                .setTag("ORDER_TIMEOUT")
                .setKeys(command.orderNo())
                .setBody(event.payload().getBytes(StandardCharsets.UTF_8))
                .setDeliveryTimestamp(event.deliverAt().toEpochMilli())
                .addProperty("eventId", event.eventId());
        if (command.traceId() != null && !command.traceId().isBlank()) {
            builder.addProperty(TraceConstants.TRACE_ID_MDC_KEY, command.traceId());
        }
        Message message = builder.build();
        log.debug("准备发送 RocketMQ 延迟关单消息 eventId={}, orderNo={}, deliverAt={}",
                event.eventId(), command.orderNo(), event.deliverAt());
        producer.send(message);
        log.info("RocketMQ 延迟关单消息发送成功 eventId={}, orderNo={}, deliverAt={}",
                event.eventId(), command.orderNo(), event.deliverAt());
    }

    @PreDestroy
    public void stop() {
        if (consumer != null) {
            try {
                consumer.close();
            } catch (Exception ex) {
                log.warn("关闭 RocketMQ Consumer 失败", ex);
            }
        }
        if (producer != null) {
            try {
                producer.close();
            } catch (Exception ex) {
                log.warn("关闭 RocketMQ Producer 失败", ex);
            }
        }
    }
}
