package org.example.springboot3java21demo.payment.support;

import org.example.springboot3java21demo.payment.constant.PaymentConstants;
import org.example.springboot3java21demo.payment.domain.OutboxEvent;
import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.repository.OutboxRepository;

/**
 * 领域事件落库工具。
 *
 * <p>所有状态变更都必须留痕：事件表就是「复盘」时回放一笔支付完整轨迹的数据来源。
 * 生产环境这里应该与业务表写在同一个本地事务里（同库同事务），本演示顺序写入以保持代码可读。</p>
 */
public final class PaymentEvents {

    private PaymentEvents() {
    }

    public static void emit(OutboxRepository outboxRepository,
                            PaymentOrder order,
                            String eventType,
                            String detail,
                            String traceId,
                            long now) {
        String payload = "{\"paymentNo\":\"" + order.paymentNo()
                + "\",\"orderNo\":\"" + order.orderNo()
                + "\",\"status\":\"" + order.status().name()
                + "\",\"amount\":\"" + order.amount().toPlainString()
                + "\",\"detail\":\"" + (detail == null ? "" : detail) + "\"}";
        outboxRepository.save(OutboxEvent.pending(PaymentConstants.AGGREGATE_PAYMENT,
                order.paymentNo(), eventType, payload, traceId, now));
    }
}
