package org.example.springboot3java21demo.orderpay.web;

import jakarta.validation.constraints.NotBlank;

public record PayOrderRequest(
        @NotBlank String requestId,
        @NotBlank String channel) {
}
