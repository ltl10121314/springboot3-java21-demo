package org.example.springboot3java21demo.paymentservice.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentRecord(
        String paymentNo,
        String requestId,
        String orderNo,
        BigDecimal amount,
        String channel,
        PaymentStatus status,
        String channelTradeNo,
        String failReason,
        long version,
        Instant createdAt,
        Instant updatedAt) {
}
