package org.example.springboot3java21demo.payment.support;

import java.util.function.LongSupplier;

/**
 * 支付入口限流器门面。
 *
 * <p>被限流时应该快速失败并返回 429，而不是排队——支付链路上排队会放大 RT，
 * 进而拖垮上游线程池，属于典型的雪崩触发路径。</p>
 */
public class PaymentRateLimiter {

    private final SlidingWindowRateLimiter delegate;

    public PaymentRateLimiter(int windowSeconds, long maxRequestsPerWindow) {
        this.delegate = new SlidingWindowRateLimiter(windowSeconds, maxRequestsPerWindow);
    }

    /**
     * 可注入时钟，便于测试精确控制窗口边界
     */
    public PaymentRateLimiter(int windowSeconds, long maxRequestsPerWindow, LongSupplier secondsClock) {
        this.delegate = new SlidingWindowRateLimiter(windowSeconds, maxRequestsPerWindow, secondsClock);
    }

    public boolean tryAcquire() {
        return delegate.tryAcquire();
    }

    public long currentWindowCount() {
        return delegate.currentWindowCount();
    }
}
