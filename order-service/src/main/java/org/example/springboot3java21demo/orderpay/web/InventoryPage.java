package org.example.springboot3java21demo.orderpay.web;

import java.util.List;

public record InventoryPage(
        long total,
        int page,
        int size,
        List<InventoryView> items) {
}
