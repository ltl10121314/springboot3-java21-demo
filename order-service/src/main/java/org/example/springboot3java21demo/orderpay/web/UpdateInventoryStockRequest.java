package org.example.springboot3java21demo.orderpay.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateInventoryStockRequest(
        @NotNull @Min(0) Long totalStock) {
}
