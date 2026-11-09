package org.example.springboot3java21demo.rocketmq.model;

import java.time.Instant;

/**
 * RocketMQ 接口的统一错误响应，保证异常原因能出现在响应体中。
 */
public record RocketMqErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message) {
}
