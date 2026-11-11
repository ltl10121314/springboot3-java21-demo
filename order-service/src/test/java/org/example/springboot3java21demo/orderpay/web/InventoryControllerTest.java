package org.example.springboot3java21demo.orderpay.web;

import org.example.springboot3java21demo.orderpay.service.InventoryApplicationService;
import org.example.springboot3java21demo.orderpay.web.InventoryView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class InventoryControllerTest {

    private InventoryApplicationService inventoryApplicationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        inventoryApplicationService = mock(InventoryApplicationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new InventoryController(inventoryApplicationService))
                .setControllerAdvice(new OrderPayExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("创建库存返回 201 和完整库存信息")
    void createsInventory() throws Exception {
        when(inventoryApplicationService.create(any())).thenReturn(inventoryView());

        mockMvc.perform(post("/api/order-pay/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skuId": "SKU-CRUD-001",
                                  "skuName": "机械键盘",
                                  "price": 199.00,
                                  "totalStock": 20
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.skuId").value("SKU-CRUD-001"))
                .andExpect(jsonPath("$.data.availableStock").value(18));
    }

    @Test
    @DisplayName("非法库存参数返回 400")
    void rejectsInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/order-pay/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "skuId": "",
                                  "skuName": "",
                                  "price": 0,
                                  "totalStock": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("库存总量调整接口返回最新可售数量")
    void updatesTotalStock() throws Exception {
        when(inventoryApplicationService.updateTotalStock("SKU-CRUD-001", 30L))
                .thenReturn(new InventoryView(
                        "SKU-CRUD-001",
                        "机械键盘",
                        new BigDecimal("199.00"),
                        30,
                        2,
                        0,
                        28,
                        Instant.parse("2026-11-11T00:00:00Z")));

        mockMvc.perform(put("/api/order-pay/inventory/SKU-CRUD-001/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"totalStock\":30}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalStock").value(30))
                .andExpect(jsonPath("$.data.availableStock").value(28));
    }

    @Test
    @DisplayName("库存详情接口返回列表字段")
    void getsInventory() throws Exception {
        when(inventoryApplicationService.get("SKU-CRUD-001")).thenReturn(inventoryView());

        mockMvc.perform(get("/api/order-pay/inventory/SKU-CRUD-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skuName").value("机械键盘"))
                .andExpect(jsonPath("$.data.reservedStock").value(2));
    }

    private InventoryView inventoryView() {
        return new InventoryView(
                "SKU-CRUD-001",
                "机械键盘",
                new BigDecimal("199.00"),
                20,
                2,
                0,
                18,
                Instant.parse("2026-11-11T00:00:00Z"));
    }
}
