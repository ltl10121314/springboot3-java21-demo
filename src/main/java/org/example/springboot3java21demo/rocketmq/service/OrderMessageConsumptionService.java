package org.example.springboot3java21demo.rocketmq.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.example.springboot3java21demo.rocketmq.config.RocketMqProperties;
import org.example.springboot3java21demo.rocketmq.model.ConsumedMessage;
import org.example.springboot3java21demo.rocketmq.model.OrderEvent;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * 两类 Consumer 共用的反序列化和消费记录逻辑。
 */
@Service
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

    public OrderEvent record(String consumerGroup, MessageView message) throws JsonProcessingException {
        ByteBuffer body = message.getBody().duplicate();
        byte[] bytes = new byte[body.remaining()];
        body.get(bytes);
        String payload = new String(bytes, StandardCharsets.UTF_8);
        OrderEvent event = objectMapper.readValue(payload, OrderEvent.class);
        ConsumedMessage consumedMessage = new ConsumedMessage(
                store.nextSequence(),
                consumerGroup,
                message.getMessageId().toString(),
                message.getTopic(),
                message.getTag().orElse(null),
                String.join(",", message.getKeys()),
                message.getDeliveryAttempt(),
                payload,
                Instant.now());
        store.add(consumedMessage, properties.getRecentMessageLimit());
        return event;
    }
}
