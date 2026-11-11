package org.example.springboot3java21demo.orderpay.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateInventoryRequest(
        @NotBlank @Size(max = 128) String skuName,
        @NotNull @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 10, fraction = 2) BigDecimal price,
        @NotNull @Min(0) Long totalStock) {
}
