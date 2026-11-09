package org.example.springboot3java21demo.rocketmq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;
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

    public OrderMessageProducer(
            ObjectProvider<Producer> producerProvider,
            ObjectProvider<ClientServiceProvider> clientServiceProvider,
            RocketMqProperties properties,
            ObjectMapper objectMapper) {
        this.producerProvider = producerProvider;
        this.clientServiceProvider = clientServiceProvider;
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 同步发送会等待服务端返回 SendReceipt，适合下单这类必须知道发送结果的场景。
     */
    public SendReceipt send(OrderEvent event) {
        Producer producer = requireProducer();
        Message message = buildMessage(event);
        try {
            return producer.send(message);
        } catch (ClientException e) {
            throw new RocketMqSendException("RocketMQ 同步发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步发送不会阻塞 HTTP 请求线程，发送结果通过 CompletableFuture 返回。
     */
    public CompletableFuture<SendReceipt> sendAsync(OrderEvent event) {
        Producer producer = requireProducer();
        Message message = buildMessage(event);
        CompletableFuture<SendReceipt> future = producer.sendAsync(message);

        future.whenComplete((result, error) -> {
            if (error == null) {
                log.info("RocketMQ 异步发送成功 eventId={}, messageId={}",
                        event.eventId(), result.getMessageId());
            } else {
                log.error("RocketMQ 异步发送失败 eventId={}", event.eventId(), error);
            }
        });
        return future;
    }

    private Message buildMessage(OrderEvent event) {
        try {
            byte[] body = objectMapper.writeValueAsBytes(event);
            return clientServiceProvider.getObject().newMessageBuilder()
                    .setTopic(properties.getTopic())
                    // Tag 是 Topic 内的二级分类，供 Consumer 的 FilterExpression 过滤。
                    .setTag(event.eventType().tag())
                    // Key 用于根据订单号查询消息，最多可设置多个。
                    .setKeys(event.orderId())
                    .setBody(body)
                    .addProperty("eventId", event.eventId())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RocketMqSendException("订单消息序列化失败", e);
        }
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
