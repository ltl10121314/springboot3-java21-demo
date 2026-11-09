package org.example.springboot3java21demo.rocketmq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.message.MessageBuilder;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.example.springboot3java21demo.rocketmq.common.RocketMqConstants;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;
import org.example.springboot3java21demo.rocketmq.model.DeadLetterMessage;
import org.example.springboot3java21demo.rocketmq.model.OrderEvent;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * RocketMQ 5.x Java Producer 的使用示例。
 */
@Slf4j
@Service
public class OrderMessageProducer {

    private final ObjectProvider<Producer> producerProvider;
    private final ObjectProvider<ClientServiceProvider> clientServiceProvider;
    private final RocketMqProperties properties;
    private final ObjectMapper objectMapper;
    private final RocketMqMetrics metrics;

    public OrderMessageProducer(
            ObjectProvider<Producer> producerProvider,
            ObjectProvider<ClientServiceProvider> clientServiceProvider,
            RocketMqProperties properties,
            ObjectMapper objectMapper,
            RocketMqMetrics metrics) {
        this.producerProvider = producerProvider;
        this.clientServiceProvider = clientServiceProvider;
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }

    /**
     * 同步发送会等待服务端返回 SendReceipt，适合下单这类必须知道发送结果的场景。
     */
    public SendReceipt send(OrderEvent event) {
        return send(event, null, null);
    }

    public SendReceipt  send(OrderEvent event, String failConsumerGroup, Integer delaySeconds) {
        Producer producer = requireProducer();
        validateFailConsumerGroup(failConsumerGroup);
        Message message = buildMessage(event, failConsumerGroup, delaySeconds);
        String topic = resolveTopic(delaySeconds);
        long startedAt = System.nanoTime();
        log.info("准备同步发送 RocketMQ 消息 eventId={}, orderId={}, topic={}, tag={}, delaySeconds={}, failConsumerGroup={}",
                event.eventId(), event.orderId(), topic, event.eventType().tag(),
                delaySeconds, failConsumerGroup);
        try {
            SendReceipt receipt = producer.send(message);
            metrics.increment(RocketMqMetrics.Metric.SEND_SYNC_SUCCESS);
            log.info("同步发送成功 eventId={}, orderId={}, messageId={}, elapsedMs={}",
                    event.eventId(), event.orderId(), receipt.getMessageId(), elapsedMillis(startedAt));
            return receipt;
        } catch (ClientException e) {
            metrics.increment(RocketMqMetrics.Metric.SEND_FAILURE);
            log.error("同步发送失败 eventId={}, orderId={}, elapsedMs={}",
                    event.eventId(), event.orderId(), elapsedMillis(startedAt), e);
            throw new RocketMqSendException("RocketMQ 同步发送失败: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            metrics.increment(RocketMqMetrics.Metric.SEND_FAILURE);
            log.error("同步发送参数或客户端异常 eventId={}, orderId={}, topic={}, elapsedMs={}",
                    event.eventId(), event.orderId(), topic, elapsedMillis(startedAt), e);
            throw new RocketMqSendException("RocketMQ 同步发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步发送不会阻塞 HTTP 请求线程，发送结果通过 CompletableFuture 返回。
     */
    public CompletableFuture<SendReceipt> sendAsync(OrderEvent event) {
        return sendAsync(event, null, null);
    }

    public CompletableFuture<SendReceipt> sendAsync(
            OrderEvent event,
            String failConsumerGroup,
            Integer delaySeconds) {
        Producer producer = requireProducer();
        validateFailConsumerGroup(failConsumerGroup);
        Message message = buildMessage(event, failConsumerGroup, delaySeconds);
        String topic = resolveTopic(delaySeconds);
        long startedAt = System.nanoTime();
        log.info("准备异步发送 RocketMQ 消息 eventId={}, orderId={}, topic={}, tag={}, delaySeconds={}, failConsumerGroup={}",
                event.eventId(), event.orderId(), topic, event.eventType().tag(),
                delaySeconds, failConsumerGroup);
        CompletableFuture<SendReceipt> future = producer.sendAsync(message);

        future.whenComplete((result, error) -> {
            if (error == null) {
                metrics.increment(RocketMqMetrics.Metric.SEND_ASYNC_SUCCESS);
                log.info("RocketMQ 异步发送成功 eventId={}, orderId={}, messageId={}, elapsedMs={}",
                        event.eventId(), event.orderId(), result.getMessageId(), elapsedMillis(startedAt));
            } else {
                metrics.increment(RocketMqMetrics.Metric.SEND_FAILURE);
                log.error("RocketMQ 异步发送失败 eventId={}, orderId={}, elapsedMs={}",
                        event.eventId(), event.orderId(), elapsedMillis(startedAt), error);
            }
        });
        return future;
    }

    /**
     * 将死信重新投递到原始业务 Topic，并移除失败注入属性。
     */
    public SendReceipt replay(DeadLetterMessage deadLetterMessage) {
        Producer producer = requireProducer();
        MessageBuilder builder = clientServiceProvider.getObject().newMessageBuilder()
                .setTopic(properties.getTopic())
                .setBody(deadLetterMessage.payload().getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .addProperty(RocketMqConstants.REPLAYED_FROM_DLQ_PROPERTY, deadLetterMessage.messageId());
        if (deadLetterMessage.tag() != null && !deadLetterMessage.tag().isBlank()) {
            builder.setTag(deadLetterMessage.tag());
        }
        if (deadLetterMessage.key() != null && !deadLetterMessage.key().isBlank()) {
            builder.setKeys(deadLetterMessage.key().split(","));
        }

        long startedAt = System.nanoTime();
        log.info("准备重放死信 dlqMessageId={}, sourceGroup={}, topic={}, tag={}, key={}",
                deadLetterMessage.messageId(), deadLetterMessage.consumerGroup(),
                properties.getTopic(), deadLetterMessage.tag(), deadLetterMessage.key());
        try {
            SendReceipt receipt = producer.send(builder.build());
            log.info("死信重放发送成功 dlqMessageId={}, newMessageId={}, elapsedMs={}",
                    deadLetterMessage.messageId(), receipt.getMessageId(), elapsedMillis(startedAt));
            return receipt;
        } catch (ClientException e) {
            metrics.increment(RocketMqMetrics.Metric.SEND_FAILURE);
            log.error("死信重放发送失败 dlqMessageId={}, elapsedMs={}",
                    deadLetterMessage.messageId(), elapsedMillis(startedAt), e);
            throw new RocketMqSendException("死信重放失败: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            metrics.increment(RocketMqMetrics.Metric.SEND_FAILURE);
            log.error("死信重放参数或客户端异常 dlqMessageId={}, elapsedMs={}",
                    deadLetterMessage.messageId(), elapsedMillis(startedAt), e);
            throw new RocketMqSendException("死信重放失败: " + e.getMessage(), e);
        }
    }

    private Message buildMessage(OrderEvent event, String failConsumerGroup, Integer delaySeconds) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(event);
            MessageBuilder builder = clientServiceProvider.getObject().newMessageBuilder()
                    .setTopic(resolveTopic(delaySeconds))
                    // Tag 是 Topic 内的二级分类，供 Consumer 的 FilterExpression 过滤。
                    .setTag(event.eventType().tag())
                    // Key 用于根据订单号查询消息，最多可设置多个。
                    .setKeys(event.orderId())
                    .setBody(body)
                    .addProperty("eventId", event.eventId());
            if (failConsumerGroup != null && !failConsumerGroup.isBlank()) {
                builder.addProperty(RocketMqConstants.FAIL_CONSUMER_GROUP_PROPERTY, failConsumerGroup);
            }
            if (delaySeconds != null && delaySeconds > 0) {
                builder.setDeliveryTimestamp(System.currentTimeMillis() + delaySeconds * 1000L);
            }
            return builder.build();
        } catch (JsonProcessingException e) {
            throw new RocketMqSendException("订单消息序列化失败", e);
        }
    }

    private void validateFailConsumerGroup(String failConsumerGroup) {
        if (failConsumerGroup == null || failConsumerGroup.isBlank()) {
            return;
        }
        boolean supported = RocketMqConstants.FAIL_ALL_CONSUMER_GROUPS.equalsIgnoreCase(failConsumerGroup)
                || properties.getBusinessConsumerGroup().equals(failConsumerGroup)
                || properties.getAuditConsumerGroup().equals(failConsumerGroup);
        if (!supported) {
            throw new RocketMqBadRequestException(
                    "failConsumerGroup 仅支持业务组、审计组或 BOTH");
        }
    }

    public String resolveTopic(Integer delaySeconds) {
        return delaySeconds != null && delaySeconds > 0
                ? properties.getDelayTopic()
                : properties.getTopic();
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }

    private Producer requireProducer() {
        if (!properties.isEnabled()) {
            throw new RocketMqUnavailableException(
                    "RocketMQ 示例未启用，请设置 APP_ROCKETMQ_ENABLED=true");
        }
        Producer producer = producerProvider.getIfAvailable();
        if (producer == null) {
            throw new RocketMqUnavailableException("RocketMQ Producer 尚未就绪");
        }
        return producer;
    }
}
