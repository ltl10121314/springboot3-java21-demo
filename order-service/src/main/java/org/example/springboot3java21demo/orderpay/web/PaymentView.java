package org.example.springboot3java21demo.orderpay.web;

import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcResponse;

import java.time.Instant;

public record PaymentView(
        String requestId,
        String orderNo,
        String paymentNo,
        String status,
        String channel,
        String channelTradeNo,
        String message,
        boolean duplicate,
        Instant paidAt) {

    public static PaymentView of(PaymentRpcResponse response) {
        return new PaymentView(
                response.getRequestId(),
                response.getOrderNo(),
                response.getPaymentNo(),
                response.getStatus(),
                response.getChannel(),
                response.getChannelTradeNo(),
                response.getMessage(),
                response.isDuplicate(),
                response.getPaidAt());
    }
}
