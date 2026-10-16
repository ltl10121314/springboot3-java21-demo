package org.example.springboot3java21demo.payment.support;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 面向支付渠道的熔断器。
 *
 * <pre>
 *   CLOSED ──(失败率/连续失败超阈值)──▶ OPEN ──(等待窗口结束)──▶ HALF_OPEN
 *      ▲                                                          │
 *      └────────────(探测成功)────────────────────────────────────┘
 *                    (探测失败) ──▶ OPEN
 * </pre>
 *
 * <p>关键取舍：熔断打开后，支付请求不能直接判失败，必须降级为 UNKNOWN 并落库，
 * 交给补偿任务查询渠道真实结果。因为「没调到渠道」和「渠道处理中」在资金上完全不同。</p>
 */
public class PaymentCircuitBreaker {

    public enum State {
        CLOSED, OPEN, HALF_OPEN
    }

    private final int failureThreshold;
    private final long openDurationMillis;
    private final int halfOpenProbeCount;

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private final AtomicLong openedAt = new AtomicLong();
    private final AtomicInteger halfOpenInflight = new AtomicInteger();

    public PaymentCircuitBreaker(int failureThreshold, long openDurationMillis, int halfOpenProbeCount) {
        this.failureThreshold = failureThreshold;
        this.openDurationMillis = openDurationMillis;
        this.halfOpenProbeCount = halfOpenProbeCount;
    }

    /**
     * 是否放行本次调用
     */
    public boolean allowRequest() {
        State current = state.get();
        if (current == State.CLOSED) {
            return true;
        }
        if (current == State.OPEN) {
            if (System.currentTimeMillis() - openedAt.get() < openDurationMillis) {
                return false;
            }
            // 冷却结束，进入半开，只放少量探测流量
            if (state.compareAndSet(State.OPEN, State.HALF_OPEN)) {
                halfOpenInflight.set(0);
            }
            current = state.get();
        }
        if (current == State.HALF_OPEN) {
            return halfOpenInflight.incrementAndGet() <= halfOpenProbeCount;
        }
        return true;
    }

    public void recordSuccess() {
        consecutiveFailures.set(0);
        if (state.get() == State.HALF_OPEN) {
            state.set(State.CLOSED);
        }
    }

    public void recordFailure() {
        if (state.get() == State.HALF_OPEN) {
            open();
            return;
        }
        if (consecutiveFailures.incrementAndGet() >= failureThreshold) {
            open();
        }
    }

    public State state() {
        return state.get();
    }

    public int consecutiveFailures() {
        return consecutiveFailures.get();
    }

    private void open() {
        state.set(State.OPEN);
        openedAt.set(System.currentTimeMillis());
        consecutiveFailures.set(0);
    }
}
