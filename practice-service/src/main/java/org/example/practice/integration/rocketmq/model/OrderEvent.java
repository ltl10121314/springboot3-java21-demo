package org.example.practice.integration.rocketmq.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.example.springboot3java21demo.common.trace.TraceContext;

/**
 * 实际发送到 RocketMQ 的业务消息体。
 */
public record OrderEvent(
        String eventId,
        String orderId,
        OrderEventType eventType,
        BigDecimal amount,
        String remark,
        Instant occurredAt,
        String traceId) {

    public static OrderEvent from(SendOrderRequest request) {
        return new OrderEvent(
                UUID.randomUUID().toString(),
                request.orderId(),
                request.eventType(),
                request.amount(),
                request.remark(),
                Instant.now(),
                TraceContext.current());
    }
}
