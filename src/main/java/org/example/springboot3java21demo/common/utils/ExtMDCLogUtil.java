package org.example.springboot3java21demo.common.utils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

/**
 * MDC工具类
 */
public class ExtMDCLogUtil {


    public static String getTraceId() {
        String traceId = MDC.get("traceId");
        if (StringUtils.isEmpty(traceId)) {
            traceId = HexCodec.getRandomId();
            MDC.put("traceId", traceId);
        }
        return traceId;
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
