package org.example.springboot3java21demo.orderpay.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderEvent(
        String eventId,
        String eventType,
        String orderNo,
        String userId,
        String status,
        BigDecimal amount,
        Instant occurredAt,
        String traceId) {
}
