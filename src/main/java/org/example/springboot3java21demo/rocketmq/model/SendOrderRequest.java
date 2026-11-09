package org.example.springboot3java21demo.rocketmq.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SendOrderRequest(
        @NotBlank(message = "orderId 不能为空")
        String orderId,

        @NotNull(message = "amount 不能为空")
        @DecimalMin(value = "0.01", message = "amount 必须大于 0")
        BigDecimal amount,

        @NotNull(message = "eventType 不能为空")
        OrderEventType eventType,

        @Size(max = 200, message = "remark 最多 200 个字符")
        String remark) {
}
