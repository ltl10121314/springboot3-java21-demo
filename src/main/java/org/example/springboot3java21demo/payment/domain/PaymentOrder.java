package org.example.springboot3java21demo.payment.domain;

import org.example.springboot3java21demo.payment.constant.PaymentErrorCode;
import org.example.springboot3java21demo.payment.exception.PaymentException;

import java.math.BigDecimal;

/**
 * 支付单聚合根（不可变）。
 *
 * <p>不可变 + {@code version} 字段 = 乐观锁。仓储层用
 * {@code UPDATE ... SET version = version + 1 WHERE payment_no = ? AND version = ?}
 * 来保证并发下只有一个线程能推进状态，这是「数据一致性」的基本盘。</p>
 */
public record PaymentOrder(
        String paymentNo,
        String requestId,
        String orderNo,
        String userId,
        BigDecimal amount,
        String channel,
        StockLine stockLine,
        PaymentStatus status,
        long version,
        String bizDate,
        long createdAt,
        long updatedAt,
        String failReason,
        String traceId) {

    public static PaymentOrder create(String paymentNo,
                                      String requestId,
                                      String orderNo,
                                      String userId,
                                      BigDecimal amount,
                                      String channel,
                                      StockLine stockLine,
                                      String bizDate,
                                      long now,
                                      String traceId) {
        return new PaymentOrder(paymentNo, requestId, orderNo, userId, amount, channel, stockLine,
                PaymentStatus.INIT, 0L, bizDate, now, now, null, traceId);
    }

    /**
     * 状态机流转，非法流转直接抛异常（fail-fast，避免脏数据）
     */
    public PaymentOrder transitionTo(PaymentStatus target, String reason, long now) {
        if (!status.canTransferTo(target)) {
            throw new PaymentException(PaymentErrorCode.ORDER_STATE_ILLEGAL,
                    "非法状态流转 " + status + " -> " + target + ", paymentNo=" + paymentNo);
        }
        return new PaymentOrder(paymentNo, requestId, orderNo, userId, amount, channel, stockLine,
                target, version + 1, bizDate, createdAt, now, reason, traceId);
    }

    /**
     * 是否处于「需要补偿任务继续跟进」的中间态
     */
    public boolean isUnsettled() {
        return status == PaymentStatus.PAYING || status == PaymentStatus.UNKNOWN;
    }

    public boolean hasStock() {
        return stockLine != null;
    }
}
