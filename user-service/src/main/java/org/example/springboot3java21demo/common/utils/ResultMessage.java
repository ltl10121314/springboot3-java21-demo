package org.example.springboot3java21demo.common.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@Getter
public class ResultMessage {
    private static final Logger logger = LoggerFactory.getLogger(ResultMessage.class);
    public static final String DATE_FORMATE = "yyyy-MM-dd HH:mm:ss";
    private final Number code;
    private final String message;
    private final Object data;
    private final String traceId;

    public ResultMessage(Number code, String message, Object data) {
        message = prehandleMsgInConstructor(message);
        this.code = code;
        this.message = message;
        this.data = data;
        this.traceId = ExtMDCLogUtil.getTraceId();
    }

    private String prehandleMsgInConstructor(String message) {
        if (message != null) {
            message = message.replaceFirst("^.*Exception:\\s*", "");
        }
        if (StringUtils.isBlank(message)) {
            message = "默认异常";
        }
        return message;
    }

    public static String data(Object data, String[] keys) {
        return getJsonResult(200, "操作成功", data, keys);
    }

    public static String data(Object data) {
        return data(data, null);
    }

    public static String data(Number code, String message, Object data) {
        return getJsonResult(code, message, data, null);
    }

    /**
     * 对时间日期格式化 并序列化为JSON
     */
    public static String formatDate(Object data) {
        return formatDate(data, DATE_FORMATE);
    }

    /**
     * 对时间日期格式化 并序列化为JSON
     */
    public static String formatDate(Object data, String dateFormat) {
        return formatDate(data, dateFormat, null);
    }

    /**
     * 对时间日期格式化 并序列化为JSON
     */
    public static String formatDate(Object data, String[] keys) {
        return formatDate(data, DATE_FORMATE, keys);
    }

    public static String formatDate(Object data, String DATE_FORMATE, String[] keys) {
        return getJsonResult(200, "操作成功", data, keys);
    }

    public static String success() {
        return data(null);
    }

    public static String success(String message) {
        return getJsonResult(200, message, null, null);
    }

    public static String error(String message) {
        return getJsonResult(999, message, null, null);
    }

    public static String error(Long errorCode, String message) {
        if (null != errorCode) {
            return getJsonResult(errorCode, message, null, null);
        }
        return getJsonResult(999, message, null, null);
    }

    public static String NoLogin(String message) {
        return getJsonResult(900, message, null, null);
    }

    private static String getJsonResult(Number code, String message, Object data, String[] keys) {
        ResultMessage result = new ResultMessage(code, message, data);
        return getJsonResult(result, keys);
    }

    private static volatile Gson gsonInstance = null;

    private static Gson getGsonInstance() {
        if (gsonInstance == null) {
            synchronized (ResultMessage.class) {
                if (gsonInstance == null) {
                    GsonBuilder builder = new GsonBuilder();
                    // 与 DATE_FORMATE 常量保持一致，避免 java.util.Date 走 Gson 默认的英文日期格式
                    builder.setDateFormat(DATE_FORMATE);
                    gsonInstance = builder.create();
                }
            }
        }
        return gsonInstance;
    }

    private static String getJsonResult(ResultMessage result, String[] keys) {
        if (keys == null || keys.length == 0) {
            return getGsonInstance().toJson(result);
        } else {
            int len = keys.length;
            keys = Arrays.copyOf(keys, len + 2);
            keys[len] = "cPassword";
            keys[len + 1] = "cSalt";
            return gsonInstance.toJson(result);
        }
    }
}
