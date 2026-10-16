package org.example.springboot3java21demo.payment.support;

import org.example.springboot3java21demo.payment.constant.PaymentErrorCode;
import org.example.springboot3java21demo.payment.domain.PaymentOrder;
import org.example.springboot3java21demo.payment.domain.PaymentStatus;
import org.example.springboot3java21demo.payment.exception.PaymentException;
import org.example.springboot3java21demo.payment.repository.PaymentOrderRepository;

/**
 * 状态机推进器：乐观锁抢占 + 有界重试。
 *
 * <p>支付主流程和补偿任务可能同时推进同一张单，因此所有状态变更都必须走这里，
 * 而不是各自写一套 CAS 逻辑。</p>
 */
public final class PaymentStateMachine {

    private static final int MAX_ATTEMPT = 3;

    private PaymentStateMachine() {
    }

    public static PaymentOrder advance(PaymentOrderRepository repository,
                                       PaymentOrder order,
                                       PaymentStatus target,
                                       String reason,
                                       long now) {
        PaymentOrder current = order;
        for (int attempt = 0; attempt < MAX_ATTEMPT; attempt++) {
            PaymentOrder next = current.transitionTo(target, reason, now);
            if (repository.compareAndSet(current, next)) {
                return next;
            }
            // CAS 失败说明有并发推进，重读最新状态再判断，绝不基于过期状态覆盖写
            PaymentOrder latest = repository.findByRequestId(current.requestId())
                    .orElseThrow(() -> new PaymentException(PaymentErrorCode.SYSTEM_ERROR, "支付单丢失"));
            if (latest.status() == target) {
                return latest;
            }
            if (latest.status().isTerminal()) {
                throw new PaymentException(PaymentErrorCode.ORDER_STATE_ILLEGAL,
                        "支付单已被其它流程置为终态 " + latest.status() + ", paymentNo=" + latest.paymentNo());
            }
            current = latest;
        }
        throw new PaymentException(PaymentErrorCode.SYSTEM_ERROR, "并发更新支付单失败");
    }
}
