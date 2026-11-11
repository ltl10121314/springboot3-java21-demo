package org.example.springboot3java21demo.orderpay.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderRecord(
        String orderNo,
        String requestId,
        String userId,
        String skuId,
        String skuName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        String paymentNo,
        long version,
        Instant createdAt,
        Instant updatedAt,
        Instant paidAt) {
}
