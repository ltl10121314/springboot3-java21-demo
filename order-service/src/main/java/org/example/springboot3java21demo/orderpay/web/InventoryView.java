package org.example.springboot3java21demo.orderpay.web;

import org.example.springboot3java21demo.orderpay.domain.InventoryRecord;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryView(
        String skuId,
        String skuName,
        BigDecimal price,
        long totalStock,
        long reservedStock,
        long soldStock,
        long availableStock,
        Instant updatedAt) {

    public static InventoryView of(InventoryRecord inventory) {
        return new InventoryView(
                inventory.skuId(),
                inventory.skuName(),
                inventory.price(),
                inventory.totalStock(),
                inventory.reservedStock(),
                inventory.soldStock(),
                inventory.availableStock(),
                inventory.updatedAt());
    }
}
