package org.example.springboot3java21demo.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.springboot3java21demo.payment.domain.StockSnapshot;
import org.example.springboot3java21demo.payment.model.ApiResponse;
import org.example.springboot3java21demo.payment.model.PaymentRequest;
import org.example.springboot3java21demo.payment.model.PaymentResult;
import org.example.springboot3java21demo.payment.repository.InventoryRepository;
import org.example.springboot3java21demo.payment.service.PaymentService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 支付接口。
 *
 * <p>接口设计要点：{@code requestId} 由客户端生成并全局唯一，同一笔业务重试必须复用同一个值，
 * 这是幂等的契约基础。发起支付与查询支付结果分离，客户端不确定时永远走查询而不是重试支付。</p>
 */
@Tag(name = "幂等支付", description = "通过 requestId 保证同一笔支付不会重复扣款")
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final InventoryRepository inventoryRepository;

    @Operation(summary = "发起支付", description = "同一 requestId 重复调用只扣款一次，重复请求返回首次结果")
    @PostMapping("/pay")
    public ApiResponse<PaymentResult> pay(@RequestBody PaymentRequest request) {
        return ApiResponse.success(paymentService.pay(request));
    }

    @Operation(summary = "查询支付结果", description = "支付超时或结果未知时，用同一个 requestId 查询最终结果")
    @GetMapping("/query")
    public ApiResponse<PaymentResult> query(@RequestParam String requestId) {
        return ApiResponse.success(paymentService.query(requestId));
    }

    @Operation(summary = "查询库存", description = "展示 total/reserved/sold 三段式库存，用于验证不超卖")
    @GetMapping("/stock")
    public ApiResponse<StockSnapshot> stock(@RequestParam String skuId) {
        return ApiResponse.success(inventoryRepository.find(skuId)
                .orElseGet(() -> new StockSnapshot(skuId, 0L, 0L, 0L)));
    }
}
