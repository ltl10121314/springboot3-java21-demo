package org.example.springboot3java21demo.payment.support;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * 支付链路指标（内存实现）。
 *
 * <p>覆盖「可复盘」需要的核心指标：成功率、P99 耗时、结果未知率、限流/熔断次数、
 * 库存拒绝次数、消息积压。生产用 Micrometer + Prometheus + Grafana 暴露，本类只做演示与断言。</p>
 */
public class PaymentMetrics {

    private static final int LATENCY_WINDOW = 8192;

    private final LongAdder requests = new LongAdder();
    private final LongAdder success = new LongAdder();
    private final LongAdder failed = new LongAdder();
    private final LongAdder unknown = new LongAdder();
    private final LongAdder rateLimited = new LongAdder();
    private final LongAdder circuitRejected = new LongAdder();
    private final LongAdder stockRejected = new LongAdder();
    private final LongAdder outboxRetried = new LongAdder();
    private final LongAdder compensated = new LongAdder();

    private final long[] latencyMicros = new long[LATENCY_WINDOW];
    private final AtomicInteger latencyCursor = new AtomicInteger();
    private final AtomicLong latencyMax = new AtomicLong();

    public void recordRequest() {
        requests.increment();
    }

    public void recordSuccess() {
        success.increment();
    }

    public void recordFailed() {
        failed.increment();
    }

    public void recordUnknown() {
        unknown.increment();
    }

    public void recordRateLimited() {
        rateLimited.increment();
    }

    public void recordCircuitRejected() {
        circuitRejected.increment();
    }

    public void recordStockRejected() {
        stockRejected.increment();
    }

    public void recordOutboxRetry() {
        outboxRetried.increment();
    }

    public void recordCompensated() {
        compensated.increment();
    }

    public void recordLatency(long nanos) {
        long micros = nanos / 1000L;
        int index = Math.floorMod(latencyCursor.getAndIncrement(), LATENCY_WINDOW);
        latencyMicros[index] = micros;
        latencyMax.accumulateAndGet(micros, Math::max);
    }

    /**
     * P99 耗时（微秒）。窗口未满时用实际样本数计算。
     */
    public long p99LatencyMicros() {
        int size = Math.min(latencyCursor.get(), LATENCY_WINDOW);
        if (size == 0) {
            return 0L;
        }
        long[] copy = Arrays.copyOf(latencyMicros, size);
        Arrays.sort(copy);
        int index = Math.min((int) Math.ceil(size * 0.99d) - 1, size - 1);
        return copy[Math.max(index, 0)];
    }

    public Map<String, Long> snapshot() {
        Map<String, Long> snapshot = new LinkedHashMap<>();
        snapshot.put("requests", requests.sum());
        snapshot.put("success", success.sum());
        snapshot.put("failed", failed.sum());
        snapshot.put("unknown", unknown.sum());
        snapshot.put("rateLimited", rateLimited.sum());
        snapshot.put("circuitRejected", circuitRejected.sum());
        snapshot.put("stockRejected", stockRejected.sum());
        snapshot.put("outboxRetried", outboxRetried.sum());
        snapshot.put("compensated", compensated.sum());
        snapshot.put("p99LatencyMicros", p99LatencyMicros());
        snapshot.put("maxLatencyMicros", latencyMax.get());
        return snapshot;
    }

    public long requests() {
        return requests.sum();
    }

    public long success() {
        return success.sum();
    }

    public long failed() {
        return failed.sum();
    }

    public long unknown() {
        return unknown.sum();
    }

    public long stockRejected() {
        return stockRejected.sum();
    }

    public long rateLimited() {
        return rateLimited.sum();
    }

    public long circuitRejected() {
        return circuitRejected.sum();
    }

    public long compensated() {
        return compensated.sum();
    }
}
