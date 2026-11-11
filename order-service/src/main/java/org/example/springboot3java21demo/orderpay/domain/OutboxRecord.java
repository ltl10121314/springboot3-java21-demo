package org.example.springboot3java21demo.orderpay.domain;

import java.time.Instant;

public record OutboxRecord(
        long id,
        String eventId,
        String aggregateId,
        String eventType,
        String destination,
        String payload,
        Instant availableAt,
        Instant deliverAt,
        String status,
        int retryCount) {
}
