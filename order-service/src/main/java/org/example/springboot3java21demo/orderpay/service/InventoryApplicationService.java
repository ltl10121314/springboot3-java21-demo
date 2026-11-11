package org.example.springboot3java21demo.orderpay.service;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.orderpay.domain.InventoryRecord;
import org.example.springboot3java21demo.orderpay.exception.OrderPayException;
import org.example.springboot3java21demo.orderpay.repository.OrderInventoryRepository;
import org.example.springboot3java21demo.orderpay.web.CreateInventoryRequest;
import org.example.springboot3java21demo.orderpay.web.InventoryPage;
import org.example.springboot3java21demo.orderpay.web.InventoryView;
import org.example.springboot3java21demo.orderpay.web.UpdateInventoryRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
public class InventoryApplicationService {

    private final OrderInventoryRepository inventoryRepository;

    public InventoryApplicationService(OrderInventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryView create(CreateInventoryRequest request) {
        String skuId = normalizeSkuId(request.skuId());
        InventoryRecord inventory = new InventoryRecord(
                skuId,
                request.skuName().trim(),
                request.price(),
                request.totalStock(),
                0,
                0,
                Instant.now());
        try {
            inventoryRepository.insert(inventory);
        } catch (DuplicateKeyException ex) {
            throw OrderPayException.conflict("SKU 已存在: " + skuId);
        }
        InventoryView created = InventoryView.of(requireInventory(skuId));
        log.info("库存创建成功 skuId={}, skuName={}, totalStock={}, price={}",
                created.skuId(), created.skuName(), created.totalStock(), created.price());
        return created;
    }

    public InventoryView get(String skuId) {
        return InventoryView.of(requireInventory(normalizeSkuId(skuId)));
    }

    public InventoryPage list(String keyword, int page, int size) {
        long total = inventoryRepository.count(keyword);
        int offset = Math.multiplyExact(page, size);
        List<InventoryView> items = total == 0
                ? List.of()
                : inventoryRepository.findPage(keyword, size, offset).stream()
                        .map(InventoryView::of)
                        .toList();
        log.debug("库存分页查询完成 keyword={}, page={}, size={}, total={}, returned={}",
                keyword, page, size, total, items.size());
        return new InventoryPage(total, page, size, items);
    }

    public InventoryView update(String skuId, UpdateInventoryRequest request) {
        String normalizedSkuId = normalizeSkuId(skuId);
        if (!inventoryRepository.update(
                normalizedSkuId, request.skuName().trim(), request.price(), request.totalStock())) {
            throw updateConflict(normalizedSkuId);
        }
        InventoryView updated = InventoryView.of(requireInventory(normalizedSkuId));
        log.info("库存信息更新成功 skuId={}, totalStock={}, price={}",
                updated.skuId(), updated.totalStock(), updated.price());
        return updated;
    }

    public InventoryView updateTotalStock(String skuId, long totalStock) {
        String normalizedSkuId = normalizeSkuId(skuId);
        if (!inventoryRepository.updateTotalStock(normalizedSkuId, totalStock)) {
            throw updateConflict(normalizedSkuId);
        }
        InventoryView updated = InventoryView.of(requireInventory(normalizedSkuId));
        log.info("库存总量调整成功 skuId={}, totalStock={}, available={}",
                updated.skuId(), updated.totalStock(), updated.availableStock());
        return updated;
    }

    public void delete(String skuId) {
        String normalizedSkuId = normalizeSkuId(skuId);
        if (!inventoryRepository.deleteIfUnused(normalizedSkuId)) {
            requireInventory(normalizedSkuId);
            throw OrderPayException.conflict("SKU 存在预占或已售库存，不能删除: " + normalizedSkuId);
        }
        log.info("库存删除成功 skuId={}", normalizedSkuId);
    }

    private OrderPayException updateConflict(String skuId) {
        InventoryRecord current = inventoryRepository.find(skuId).orElse(null);
        if (current == null) {
            return OrderPayException.notFound("SKU 不存在: " + skuId);
        }
        return OrderPayException.conflict(
                "库存总量不能小于已预占与已售数量之和: "
                        + current.reservedStock() + "+" + current.soldStock());
    }

    private InventoryRecord requireInventory(String skuId) {
        return inventoryRepository.find(skuId)
                .orElseThrow(() -> OrderPayException.notFound("SKU 不存在: " + skuId));
    }

    private String normalizeSkuId(String skuId) {
        if (skuId == null || skuId.isBlank()) {
            throw OrderPayException.badRequest("skuId 不能为空");
        }
        String normalized = skuId.trim().toUpperCase(Locale.ROOT);
        if (normalized.length() > 64) {
            throw OrderPayException.badRequest("skuId 长度不能超过 64");
        }
        return normalized;
    }
}
