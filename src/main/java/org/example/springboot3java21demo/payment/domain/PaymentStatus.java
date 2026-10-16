package org.example.springboot3java21demo.payment.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 支付单状态机。
 *
 * <pre>
 *   INIT ──▶ PAYING ──▶ SUCCESS（终态）
 *     │         ├─────▶ FAILED （终态，渠道明确拒绝）
 *     │         └─────▶ UNKNOWN ──▶ SUCCESS / FAILED / CLOSED（补偿任务闭环）
 *     └─────▶ CLOSED（库存不足 / 风控拒绝，未发起支付）
 * </pre>
 *
 * <p>状态流转只能通过本枚举校验，禁止在业务代码里直接 set 状态。
 * UNKNOWN 是「渠道超时/熔断降级」的专用状态，语义是「钱可能已经扣了」，
 * 因此不允许从 UNKNOWN 直接认为未支付，必须先向渠道确认真实结果。</p>
 */
public enum PaymentStatus {

    /** 已创建，尚未发起支付 */
    INIT,
    /** 已向渠道发起支付，结果未返回 */
    PAYING,
    /** 支付成功（终态） */
    SUCCESS,
    /** 支付失败且已确认未扣款（终态） */
    FAILED,
    /** 结果未知，等待补偿确认 */
    UNKNOWN,
    /** 未发起支付即关闭，例如库存不足（终态） */
    CLOSED;

    private static final Map<PaymentStatus, Set<PaymentStatus>> TRANSITIONS;

    static {
        Map<PaymentStatus, Set<PaymentStatus>> transitions = new EnumMap<>(PaymentStatus.class);
        transitions.put(INIT, EnumSet.of(PAYING, CLOSED));
        transitions.put(PAYING, EnumSet.of(SUCCESS, FAILED, UNKNOWN));
        transitions.put(UNKNOWN, EnumSet.of(SUCCESS, FAILED, CLOSED));
        transitions.put(SUCCESS, Collections.emptySet());
        transitions.put(FAILED, Collections.emptySet());
        transitions.put(CLOSED, Collections.emptySet());
        TRANSITIONS = Collections.unmodifiableMap(transitions);
    }

    /**
     * 判断是否允许流转到目标状态
     */
    public boolean canTransferTo(PaymentStatus target) {
        return TRANSITIONS.getOrDefault(this, Collections.emptySet()).contains(target);
    }

    /**
     * 终态不允许再变更
     */
    public boolean isTerminal() {
        return TRANSITIONS.getOrDefault(this, Collections.emptySet()).isEmpty();
    }

    /**
     * 是否已经产生实际扣款（成功）或可能已扣款（未知）
     */
    public boolean isMoneyPossiblyMoved() {
        return this == SUCCESS || this == UNKNOWN;
    }

    /**
     * 是否已收到渠道明确结果
     */
    public boolean isSettled() {
        return this == SUCCESS || this == FAILED;
    }
}
