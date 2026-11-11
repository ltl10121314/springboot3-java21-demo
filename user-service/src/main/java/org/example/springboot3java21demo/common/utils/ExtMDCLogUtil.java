package org.example.springboot3java21demo.common.utils;

import org.example.springboot3java21demo.common.trace.TraceContext;
import org.slf4j.MDC;

/**
 * MDC工具类
 */
public class ExtMDCLogUtil {


    public static String getTraceId() {
        return TraceContext.current();
    }

    public static String getProfile() {
        return MDC.get("profile");
    }

    public static String getPSpanId() {
        return MDC.get("pSpanId");
    }

    public static String getSpanId() {
        return MDC.get("spanId");
    }

    public static String getLogLevel() {
        return MDC.get("level");
    }
}
