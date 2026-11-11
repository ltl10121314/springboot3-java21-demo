package org.example.springboot3java21demo.common.trace;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * 请求链路上下文。只有 HTTP Controller 入口负责生成 traceId；
 * MQ、Outbox、线程池等关联任务只继承已经存在的 traceId。
 */
public final class TraceContext {

    private static final Pattern VALID_TRACE_ID = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    private TraceContext() {
    }

    public static String current() {
        return sanitize(MDC.get(TraceConstants.TRACE_ID_MDC_KEY));
    }

    public static void set(String traceId) {
        String normalized = sanitize(traceId);
        if (normalized == null) {
            clear();
            return;
        }
        MDC.put(TraceConstants.TRACE_ID_MDC_KEY, normalized);
    }

    public static void clear() {
        MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
    }

    public static Scope open() {
        return open(null);
    }

    public static Scope open(String candidateTraceId) {
        String previous = MDC.get(TraceConstants.TRACE_ID_MDC_KEY);
        String traceId = sanitize(candidateTraceId);
        if (traceId == null) {
            traceId = newTraceId();
        }
        MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
        return new Scope(previous);
    }

    /**
     * 仅当任务已经携带 traceId 时建立作用域。返回 null 表示该任务与请求无关。
     */
    public static Scope openIfPresent(String candidateTraceId) {
        String traceId = sanitize(candidateTraceId);
        if (traceId == null) {
            return null;
        }
        String previous = MDC.get(TraceConstants.TRACE_ID_MDC_KEY);
        MDC.put(TraceConstants.TRACE_ID_MDC_KEY, traceId);
        return new Scope(previous);
    }

    public static String sanitize(String traceId) {
        if (traceId == null) {
            return null;
        }
        String normalized = traceId.trim();
        if (normalized.isEmpty() || !VALID_TRACE_ID.matcher(normalized).matches()) {
            return null;
        }
        return normalized;
    }

    public static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static final class Scope implements AutoCloseable {

        private final String previousTraceId;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Scope(String previousTraceId) {
            this.previousTraceId = previousTraceId;
        }

        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            if (previousTraceId == null || previousTraceId.isBlank()) {
                MDC.remove(TraceConstants.TRACE_ID_MDC_KEY);
            } else {
                MDC.put(TraceConstants.TRACE_ID_MDC_KEY, previousTraceId);
            }
        }
    }
}
