package org.example.springboot3java21demo.payment.support;

import org.example.springboot3java21demo.payment.constant.PaymentConstants;
import org.slf4j.MDC;

import java.util.UUID;

/**
 * 全链路追踪上下文。
 *
 * <p>「可复盘」的前提是能定位：一笔支付从网关进入、经过哪些服务、调了哪次渠道、
 * 写了哪条消息，必须能用同一个 traceId 串起来。这里用 MDC 承载，
 * 生产上接入 SkyWalking / OpenTelemetry 时把 traceId 换成 traceparent 即可，业务代码不用改。</p>
 */
public final class TraceContext {

    private TraceContext() {
    }

    /**
     * 读取当前 traceId，没有则生成一个（保证非 Web 入口，如定时任务、MQ 消费，也有 traceId）
     */
    public static String currentOrCreate() {
        String traceId = MDC.get(PaymentConstants.TRACE_ID_MDC_KEY);
        if (traceId == null || traceId.isBlank()) {
            traceId = newTraceId();
            MDC.put(PaymentConstants.TRACE_ID_MDC_KEY, traceId);
        }
        return traceId;
    }

    public static String current() {
        return MDC.get(PaymentConstants.TRACE_ID_MDC_KEY);
    }

    public static void set(String traceId) {
        MDC.put(PaymentConstants.TRACE_ID_MDC_KEY, traceId);
    }

    public static void clear() {
        MDC.remove(PaymentConstants.TRACE_ID_MDC_KEY);
    }

    public static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
