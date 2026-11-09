package org.example.springboot3java21demo.rocketmq.service;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 轻量级进程内指标，仅用于练习观察。生产环境应接入 Micrometer/Prometheus。
 */
@Component
public class RocketMqMetrics {

    private final Map<Metric, AtomicLong> counters = new ConcurrentHashMap<>();

    public RocketMqMetrics() {
        Arrays.stream(Metric.values())
                .forEach(metric -> counters.put(metric, new AtomicLong()));
    }

    public void increment(Metric metric) {
        counters.get(metric).incrementAndGet();
    }

    public long value(Metric metric) {
        return counters.get(metric).get();
    }

    public Map<String, Long> snapshot() {
        Map<String, Long> result = new TreeMap<>();
        counters.forEach((metric, value) -> result.put(metric.getKey(), value.get()));
        return result;
    }

    public enum Metric {
        SEND_SYNC_SUCCESS("sendSyncSuccess"),
        SEND_ASYNC_SUCCESS("sendAsyncSuccess"),
        SEND_FAILURE("sendFailure"),
        CONSUME_ATTEMPT("consumeAttempt"),
        CONSUME_SUCCESS("consumeSuccess"),
        CONSUME_RETRY("consumeRetry"),
        CONSUME_DUPLICATE("consumeDuplicate"),
        DEAD_LETTER_RECEIVED("deadLetterReceived"),
        DEAD_LETTER_REPLAYED("deadLetterReplayed");

        private final String key;

        Metric(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
