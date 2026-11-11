package org.example.springboot3java21demo.orderpay.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.orderpay.domain.OrderEvent;
import org.example.springboot3java21demo.orderpay.domain.OrderTimeoutCommand;
import org.example.springboot3java21demo.orderpay.repository.OutboxJdbcRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class OrderEventOutbox {

    public static final String KAFKA = "KAFKA";
    public static final String ROCKETMQ = "ROCKETMQ";

    private final OutboxJdbcRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public OrderEventOutbox(OutboxJdbcRepository outboxRepository, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
    }

    public void appendKafka(OrderEvent event) {
        outboxRepository.insert(
                event.eventId(),
                event.orderNo(),
                event.eventType(),
                KAFKA,
                write(event),
                event.occurredAt(),
                event.occurredAt());
        log.debug("Kafka Outbox 事件入队 eventId={}, orderNo={}, eventType={}",
                event.eventId(), event.orderNo(), event.eventType());
    }

    /**
     * Outbox 立即投递给 RocketMQ，但通过 deliverAt 使用服务端延迟投递。
     */
    public void appendTimeoutCommand(OrderTimeoutCommand command) {
        Instant now = Instant.now();
        outboxRepository.insert(
                command.eventId(),
                command.orderNo(),
                "ORDER_TIMEOUT_CHECK",
                ROCKETMQ,
                write(command),
                now,
                command.expiresAt());
        log.debug("RocketMQ Outbox 延迟事件入队 eventId={}, orderNo={}, expiresAt={}",
                command.eventId(), command.orderNo(), command.expiresAt());
    }

    private String write(Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("订单事件序列化失败", ex);
        }
    }
}
