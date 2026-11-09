package org.example.springboot3java21demo.rocketmq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.example.springboot3java21demo.rocketmq.common.RocketMqMessageSupport;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;
import org.example.springboot3java21demo.rocketmq.model.ConsumeStatus;
import org.example.springboot3java21demo.rocketmq.model.ConsumedMessage;
import org.example.springboot3java21demo.rocketmq.model.OrderEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * 两类 Consumer 共用的反序列化和消费记录逻辑。
 */
@Service
@Slf4j
public class OrderMessageConsumptionService {

    private final ObjectMapper objectMapper;
    private final RocketMqProperties properties;
    private final ConsumedMessageStore store;

    public OrderMessageConsumptionService(
            ObjectMapper objectMapper,
            RocketMqProperties properties,
            ConsumedMessageStore store) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.store = store;
    }

    public DecodedMessage decode(MessageView message) throws JsonProcessingException {
        String payload = RocketMqMessageSupport.bodyText(message);
        OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
        log.debug("RocketMQ 消息反序列化成功 messageId={}, topic={}, tag={}, key={}, eventId={}, orderId={}",
                message.getMessageId(),
                message.getTopic(),
                message.getTag().orElse(null),
                RocketMqMessageSupport.keys(message),
                event.eventId(),
                event.orderId());
        return new DecodedMessage(payload, event);
    }

    public void record(
            String consumerGroup,
            MessageView message,
            DecodedMessage decoded,
            ConsumeStatus status) {
        ConsumedMessage consumedMessage = new ConsumedMessage(
                store.nextSequence(),
                consumerGroup,
                message.getMessageId().toString(),
                message.getTopic(),
                message.getTag().orElse(null),
                RocketMqMessageSupport.keys(message),
                message.getDeliveryAttempt(),
                status,
                decoded.payload(),
                Instant.now());
        store.add(consumedMessage, properties.getRecentMessageLimit());
        log.debug("消费记录写入 consumerGroup={}, messageId={}, status={}, deliveryAttempt={}",
                consumerGroup, message.getMessageId(), status, message.getDeliveryAttempt());
    }

    public record DecodedMessage(String payload, OrderEvent event) {
    }
}
