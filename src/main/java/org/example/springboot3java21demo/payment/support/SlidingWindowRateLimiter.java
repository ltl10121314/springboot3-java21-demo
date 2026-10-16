package org.example.springboot3java21demo.payment.support;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.LongSupplier;

/**
 * 滑动窗口限流器（单机版）。
 *
 * <p>按秒分桶，统计最近 {@code windowSeconds} 个桶的总量。相比固定窗口，它避免了
 * 「窗口边界双倍放量」问题；相比令牌桶，实现更直观且无锁。</p>
 *
 * <p>生产上单机限流只是第一层，全局限流要放在网关（Sentinel / 网关插件）或 Redis 滑动窗口上，
 * 并且应该按「用户 + 商户 + 接口」多维限流，而不是只按机器。</p>
 */
public class SlidingWindowRateLimiter {

    private final int windowSeconds;
    private final long maxRequestsPerWindow;
    private final LongAdder[] buckets;
    private final AtomicLong lastSecond = new AtomicLong();
    private final LongSupplier secondsClock;

    public SlidingWindowRateLimiter(int windowSeconds, long maxRequestsPerWindow) {
        this(windowSeconds, maxRequestsPerWindow, () -> System.currentTimeMillis() / 1000L);
    }

    public SlidingWindowRateLimiter(int windowSeconds, long maxRequestsPerWindow, LongSupplier secondsClock) {
        if (windowSeconds <= 0 || maxRequestsPerWindow <= 0) {
            throw new IllegalArgumentException("windowSeconds 和 maxRequestsPerWindow 必须大于 0");
        }
        this.windowSeconds = windowSeconds;
        this.maxRequestsPerWindow = maxRequestsPerWindow;
        this.secondsClock = secondsClock;
        this.buckets = new LongAdder[windowSeconds];
        for (int i = 0; i < windowSeconds; i++) {
            buckets[i] = new LongAdder();
        }
        this.lastSecond.set(currentSecond());
    }

    /**
     * 尝试获取一个配额
     */
    public boolean tryAcquire() {
        rotateIfNeeded();
        long total = 0L;
        for (LongAdder bucket : buckets) {
            total += bucket.sum();
        }
        if (total >= maxRequestsPerWindow) {
            return false;
        }
        // 先计数后判断会有超发风险，这里用 sum 判断 + 立即自增，误差在一个窗口内的并发数级别，
        // 生产上如需严格限流请用 Redis + Lua 或 Sentinel 的集群流控。
        buckets[(int) (currentSecond() % windowSeconds)].increment();
        return true;
    }

    public long currentWindowCount() {
        long total = 0L;
        for (LongAdder bucket : buckets) {
            total += bucket.sum();
        }
        return total;
    }

    /**
     * 跨秒时清空过期桶。
     *
     * <p>环形数组的下标是 {@code second % windowSeconds}，所以第 S 秒的计数落在下标 {@code S % W}。
     * 从第 S 秒推进到第 S+i 秒时，过期的是第 {@code S+i-W} 秒，它的下标正是 {@code (S+i) % W}。
     * 也就是说要清的是「新进入的这一秒对应的桶」，而不是「刚过去的那一秒」——
     * 后者会把当前窗口内的计数误清，造成限流放量。</p>
     */
    private void rotateIfNeeded() {
        long now = currentSecond();
        long last = lastSecond.get();
        if (now == last) {
            return;
        }
        if (!lastSecond.compareAndSet(last, now)) {
            return;
        }
        long gap = Math.min(now - last, windowSeconds);
        for (long i = 0; i < gap; i++) {
            buckets[(int) ((last + i + 1) % windowSeconds)].reset();
        }
    }

    private long currentSecond() {
        return secondsClock.getAsLong();
    }
}
