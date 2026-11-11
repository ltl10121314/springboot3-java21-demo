package org.example.springboot3java21demo.orderpay.rpc;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

public class PaymentRpcRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String requestId;
    private String orderNo;
    private String paymentNo;
    private String channel;
    private BigDecimal amount;
    private String traceId;

    public PaymentRpcRequest() {
    }

    public PaymentRpcRequest(
            String requestId,
            String orderNo,
            String paymentNo,
            String channel,
            BigDecimal amount,
            String traceId) {
        this.requestId = requestId;
        this.orderNo = orderNo;
        this.paymentNo = paymentNo;
        this.channel = channel;
        this.amount = amount;
        this.traceId = traceId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PaymentRpcRequest that)) {
            return false;
        }
        return Objects.equals(requestId, that.requestId)
                && Objects.equals(orderNo, that.orderNo)
                && Objects.equals(paymentNo, that.paymentNo)
                && Objects.equals(channel, that.channel)
                && Objects.equals(amount, that.amount)
                && Objects.equals(traceId, that.traceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, orderNo, paymentNo, channel, amount, traceId);
    }
}
