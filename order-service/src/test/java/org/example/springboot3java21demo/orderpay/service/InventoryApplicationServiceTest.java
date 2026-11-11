package org.example.springboot3java21demo.orderpay.service;

import org.example.springboot3java21demo.orderpay.domain.InventoryRecord;
import org.example.springboot3java21demo.orderpay.exception.OrderPayException;
import org.example.springboot3java21demo.orderpay.repository.OrderInventoryRepository;
import org.example.springboot3java21demo.orderpay.web.CreateInventoryRequest;
import org.example.springboot3java21demo.orderpay.web.UpdateInventoryRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryApplicationServiceTest {

    @Mock
    private OrderInventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryApplicationService inventoryApplicationService;

    @Test
    @DisplayName("创建库存时规范化 SKU 并返回可售数量")
    void createsInventoryWithNormalizedSku() {
        InventoryRecord stored = inventory(
                "SKU-CRUD-001", "测试商品", "99.00", 20, 2, 3);
        when(inventoryRepository.find("SKU-CRUD-001")).thenReturn(Optional.of(stored));

        var result = inventoryApplicationService.create(new CreateInventoryRequest(
                " sku-crud-001 ", " 测试商品 ", new BigDecimal("99.00"), 20L));

        ArgumentCaptor<InventoryRecord> captor = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(inventoryRepository).insert(captor.capture());
        assertThat(captor.getValue().skuId()).isEqualTo("SKU-CRUD-001");
        assertThat(captor.getValue().reservedStock()).isZero();
        assertThat(result.availableStock()).isEqualTo(15);
    }

    @Test
    @DisplayName("库存总量不能小于预占量与已售量之和")
    void rejectsInvalidTotalStockUpdate() {
        when(inventoryRepository.update(
                "SKU-CRUD-001", "测试商品", new BigDecimal("88.00"), 2L)).thenReturn(false);
        when(inventoryRepository.find("SKU-CRUD-001")).thenReturn(Optional.of(
                inventory("SKU-CRUD-001", "测试商品", "99.00", 20, 4, 6)));

        assertThatThrownBy(() -> inventoryApplicationService.update(
                "sku-crud-001",
                new UpdateInventoryRequest("测试商品", new BigDecimal("88.00"), 2L)))
                .isInstanceOfSatisfying(OrderPayException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(409);
                    assertThat(ex.getMessage()).contains("已预占与已售数量");
                });
    }

    @Test
    @DisplayName("存在预占或已售库存时禁止删除")
    void rejectsDeletingUsedInventory() {
        when(inventoryRepository.deleteIfUnused("SKU-CRUD-001")).thenReturn(false);
        when(inventoryRepository.find("SKU-CRUD-001")).thenReturn(Optional.of(
                inventory("SKU-CRUD-001", "测试商品", "99.00", 20, 1, 2)));

        assertThatThrownBy(() -> inventoryApplicationService.delete("SKU-CRUD-001"))
                .isInstanceOfSatisfying(OrderPayException.class, ex -> {
                    assertThat(ex.getHttpStatus()).isEqualTo(409);
                    assertThat(ex.getMessage()).contains("不能删除");
                });
    }

    @Test
    @DisplayName("库存列表按分页参数查询并计算可售数量")
    void listsInventoryWithPagination() {
        when(inventoryRepository.count("键盘")).thenReturn(1L);
        when(inventoryRepository.findPage("键盘", 10, 20)).thenReturn(List.of(
                inventory("SKU-CRUD-001", "机械键盘", "199.00", 20, 2, 3)));

        var result = inventoryApplicationService.list("键盘", 2, 10);

        assertThat(result.total()).isEqualTo(1);
        assertThat(result.page()).isEqualTo(2);
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.items()).singleElement()
                .extracting(item -> item.availableStock())
                .isEqualTo(15L);
    }

    private InventoryRecord inventory(
            String skuId,
            String skuName,
            String price,
            long totalStock,
            long reservedStock,
            long soldStock) {
        return new InventoryRecord(
                skuId,
                skuName,
                new BigDecimal(price),
                totalStock,
                reservedStock,
                soldStock,
                Instant.parse("2026-11-11T00:00:00Z"));
    }
}
