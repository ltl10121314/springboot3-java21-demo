package org.example.springboot3java21demo.orderpay.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank String requestId,
        @NotBlank String userId,
        @NotBlank String skuId,
        @Min(1) @Max(100) int quantity) {
}
