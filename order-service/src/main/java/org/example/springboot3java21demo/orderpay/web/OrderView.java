package org.example.springboot3java21demo.orderpay.web;

import org.example.springboot3java21demo.orderpay.domain.OrderRecord;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderView(
        String orderNo,
        String requestId,
        String userId,
        String skuId,
        String skuName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal totalAmount,
        String status,
        String paymentNo,
        Instant createdAt,
        Instant updatedAt,
        Instant paidAt,
        boolean duplicate) {

    public static OrderView of(OrderRecord order, boolean duplicate) {
        return new OrderView(
                order.orderNo(),
                order.requestId(),
                order.userId(),
                order.skuId(),
                order.skuName(),
                order.unitPrice(),
                order.quantity(),
                order.totalAmount(),
                order.status().name(),
                order.paymentNo(),
                order.createdAt(),
                order.updatedAt(),
                order.paidAt(),
                duplicate);
    }
}
