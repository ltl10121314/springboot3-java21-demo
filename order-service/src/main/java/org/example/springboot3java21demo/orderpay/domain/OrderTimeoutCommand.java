package org.example.springboot3java21demo.orderpay.domain;

import java.time.Instant;

public record OrderTimeoutCommand(
        String eventId,
        String orderNo,
        Instant expiresAt,
        String traceId) {
}
