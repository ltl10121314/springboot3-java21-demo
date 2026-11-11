package org.example.springboot3java21demo.orderpay.domain;

import java.math.BigDecimal;

public record InventoryRecord(
        String skuId,
        String skuName,
        BigDecimal price,
        long totalStock,
        long reservedStock,
        long soldStock) {

    public long availableStock() {
        return totalStock - reservedStock - soldStock;
    }
}
