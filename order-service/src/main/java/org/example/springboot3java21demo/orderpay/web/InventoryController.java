package org.example.springboot3java21demo.orderpay.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.orderpay.service.InventoryApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/order-pay/inventory")
public class InventoryController {

    private final InventoryApplicationService inventoryApplicationService;

    public InventoryController(InventoryApplicationService inventoryApplicationService) {
        this.inventoryApplicationService = inventoryApplicationService;
    }

    @PostMapping
    public ResponseEntity<ApiResult<InventoryView>> create(
            @Valid @RequestBody CreateInventoryRequest request) {
        log.info("创建库存请求开始 skuId={}, totalStock={}",
                request.skuId(), request.totalStock());
        InventoryView inventory = inventoryApplicationService.create(request);
        log.info("创建库存请求完成 skuId={}, available={}",
                inventory.skuId(), inventory.availableStock());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.ok(inventory));
    }

    @GetMapping
    public ApiResult<InventoryPage> list(
            @RequestParam(required = false) @Size(max = 128) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) @Max(1_000_000) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        InventoryPage result = inventoryApplicationService.list(keyword, page, size);
        log.debug("查询库存列表完成 keyword={}, page={}, size={}, total={}",
                keyword, page, size, result.total());
        return ApiResult.ok(result);
    }

    @GetMapping("/{skuId}")
    public ApiResult<InventoryView> get(
            @PathVariable @Size(min = 1, max = 64) String skuId) {
        InventoryView inventory = inventoryApplicationService.get(skuId);
        log.debug("查询库存完成 skuId={}, available={}, reserved={}, sold={}",
                inventory.skuId(), inventory.availableStock(),
                inventory.reservedStock(), inventory.soldStock());
        return ApiResult.ok(inventory);
    }

    @PutMapping("/{skuId}")
    public ApiResult<InventoryView> update(
            @PathVariable @Size(min = 1, max = 64) String skuId,
            @Valid @RequestBody UpdateInventoryRequest request) {
        log.info("更新库存请求开始 skuId={}, totalStock={}", skuId, request.totalStock());
        InventoryView inventory = inventoryApplicationService.update(skuId, request);
        log.info("更新库存请求完成 skuId={}, available={}",
                inventory.skuId(), inventory.availableStock());
        return ApiResult.ok(inventory);
    }

    @PutMapping("/{skuId}/stock")
    public ApiResult<InventoryView> updateStock(
            @PathVariable @Size(min = 1, max = 64) String skuId,
            @Valid @RequestBody UpdateInventoryStockRequest request) {
        log.info("调整库存总量请求开始 skuId={}, totalStock={}", skuId, request.totalStock());
        InventoryView inventory = inventoryApplicationService.updateTotalStock(
                skuId, request.totalStock());
        log.info("调整库存总量请求完成 skuId={}, totalStock={}, available={}",
                inventory.skuId(), inventory.totalStock(), inventory.availableStock());
        return ApiResult.ok(inventory);
    }

    @DeleteMapping("/{skuId}")
    public ApiResult<Void> delete(
            @PathVariable @Size(min = 1, max = 64) String skuId) {
        log.info("删除库存请求开始 skuId={}", skuId);
        inventoryApplicationService.delete(skuId);
        log.info("删除库存请求完成 skuId={}", skuId);
        return ApiResult.ok(null);
    }
}
