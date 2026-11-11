package org.example.springboot3java21demo.orderpay.rpc;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

public class PaymentRpcResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String requestId;
    private String orderNo;
    private String paymentNo;
    private String status;
    private String channel;
    private String channelTradeNo;
    private String message;
    private boolean duplicate;
    private Instant paidAt;

    public PaymentRpcResponse() {
    }

    public PaymentRpcResponse(
            String requestId,
            String orderNo,
            String paymentNo,
            String status,
            String channel,
            String channelTradeNo,
            String message,
            boolean duplicate,
            Instant paidAt) {
        this.requestId = requestId;
        this.orderNo = orderNo;
        this.paymentNo = paymentNo;
        this.status = status;
        this.channel = channel;
        this.channelTradeNo = channelTradeNo;
        this.message = message;
        this.duplicate = duplicate;
        this.paidAt = paidAt;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelTradeNo() {
        return channelTradeNo;
    }

    public void setChannelTradeNo(String channelTradeNo) {
        this.channelTradeNo = channelTradeNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PaymentRpcResponse that)) {
            return false;
        }
        return duplicate == that.duplicate
                && Objects.equals(requestId, that.requestId)
                && Objects.equals(orderNo, that.orderNo)
                && Objects.equals(paymentNo, that.paymentNo)
                && Objects.equals(status, that.status)
                && Objects.equals(channel, that.channel)
                && Objects.equals(channelTradeNo, that.channelTradeNo)
                && Objects.equals(message, that.message)
                && Objects.equals(paidAt, that.paidAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId, orderNo, paymentNo, status, channel,
                channelTradeNo, message, duplicate, paidAt);
    }
}
