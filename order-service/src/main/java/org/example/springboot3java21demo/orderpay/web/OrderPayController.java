package org.example.springboot3java21demo.orderpay.web;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.example.springboot3java21demo.orderpay.config.NacosRuntimeConfig;
import org.example.springboot3java21demo.orderpay.config.OrderPayProperties;
import org.example.springboot3java21demo.orderpay.domain.OrderRecord;
import org.example.springboot3java21demo.orderpay.repository.OutboxJdbcRepository;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcFacade;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcRequest;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcResponse;
import org.example.springboot3java21demo.orderpay.search.OrderSearchService;
import org.example.springboot3java21demo.orderpay.service.OrderApplicationService;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/order-pay")
@Slf4j
public class OrderPayController {

    private final OrderApplicationService orderApplicationService;
    private final OrderSearchService searchService;
    private final NacosRuntimeConfig nacosRuntimeConfig;
    private final OrderPayProperties properties;
    private final OutboxJdbcRepository outboxRepository;

    @DubboReference(check = false, timeout = 5000, retries = 0)
    private PaymentRpcFacade paymentRpcFacade;

    public OrderPayController(
            OrderApplicationService orderApplicationService,
            OrderSearchService searchService,
            NacosRuntimeConfig nacosRuntimeConfig,
            OrderPayProperties properties,
            OutboxJdbcRepository outboxRepository) {
        this.orderApplicationService = orderApplicationService;
        this.searchService = searchService;
        this.nacosRuntimeConfig = nacosRuntimeConfig;
        this.properties = properties;
        this.outboxRepository = outboxRepository;
    }

    @PostMapping("/orders")
    public ApiResult<OrderView> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        long startedAt = System.nanoTime();
        log.info("创建订单请求开始 requestId={}, skuId={}, quantity={}",
                request.requestId(), request.skuId(), request.quantity());
        OrderView order = orderApplicationService.createOrder(request);
        log.info("创建订单请求完成 orderNo={}, status={}, duplicate={}, elapsedMs={}",
                order.orderNo(), order.status(), order.duplicate(), elapsedMillis(startedAt));
        return ApiResult.ok(order);
    }

    @GetMapping("/orders/{orderNo}")
    public ApiResult<OrderView> getOrder(@PathVariable String orderNo) {
        OrderView order = orderApplicationService.getOrder(orderNo);
        log.debug("查询订单完成 orderNo={}, status={}", order.orderNo(), order.status());
        return ApiResult.ok(order);
    }

    @PostMapping("/orders/{orderNo}/pay")
    public ApiResult<PaymentView> pay(
            @PathVariable String orderNo,
            @Valid @RequestBody PayOrderRequest request) {
        long startedAt = System.nanoTime();
        log.info("支付请求开始 orderNo={}, requestId={}, channel={}",
                orderNo, request.requestId(), request.channel());
        OrderRecord paymentAttempt = orderApplicationService.beginPayment(orderNo);
        PaymentRpcResponse response;
        try {
            response = paymentRpcFacade.pay(
                    new PaymentRpcRequest(
                            request.requestId(),
                            orderNo,
                            paymentAttempt.paymentNo(),
                            request.channel(),
                            paymentAttempt.totalAmount(),
                            TraceContext.current()));
        } catch (RuntimeException ex) {
            orderApplicationService.markPaymentUnknown(
                    orderNo, paymentAttempt.paymentNo(), "支付服务调用异常");
            log.error("支付服务 Dubbo 调用失败 method=POST, uri=/api/order-pay/orders/{}/pay, "
                            + "orderNo={}, requestId={}, paymentNo={}, exception={}",
                    orderNo, orderNo, request.requestId(), paymentAttempt.paymentNo(),
                    ex.getClass().getSimpleName(), ex);
            throw ex;
        }
        if (response == null || response.getStatus() == null || response.getStatus().isBlank()) {
            orderApplicationService.markPaymentUnknown(
                    orderNo, paymentAttempt.paymentNo(), "支付服务返回空结果");
            throw new IllegalStateException("支付服务返回空结果");
        }
        switch (response.getStatus()) {
            case "SUCCESS" -> orderApplicationService.completePayment(
                    orderNo, response.getPaymentNo(), response.getChannelTradeNo());
            case "FAILED" -> orderApplicationService.failPayment(
                    orderNo, response.getPaymentNo(), response.getMessage());
            case "UNKNOWN" -> orderApplicationService.markPaymentUnknown(
                    orderNo, response.getPaymentNo(), response.getMessage());
            default -> {
                // PAYING/PROCESSING 等中间态由查询接口继续跟踪。
            }
        }
        log.info("支付请求完成 orderNo={}, requestId={}, paymentNo={}, status={}, duplicate={}, elapsedMs={}",
                orderNo, request.requestId(), response.getPaymentNo(), response.getStatus(),
                response.isDuplicate(), elapsedMillis(startedAt));
        return ApiResult.ok(PaymentView.of(response));
    }

    @GetMapping("/payments/{requestId}")
    public ApiResult<PaymentView> queryPayment(@PathVariable String requestId) {
        PaymentRpcResponse response = paymentRpcFacade.query(requestId);
        log.debug("查询支付结果完成 requestId={}, paymentNo={}, status={}",
                requestId, response.getPaymentNo(), response.getStatus());
        return ApiResult.ok(PaymentView.of(response));
    }

    @PostMapping("/orders/{orderNo}/cancel")
    public ApiResult<OrderView> cancel(@PathVariable String orderNo) {
        OrderView order = orderApplicationService.cancelOrder(orderNo);
        log.info("取消订单完成 orderNo={}, status={}, duplicate={}",
                order.orderNo(), order.status(), order.duplicate());
        return ApiResult.ok(order);
    }

    @GetMapping("/orders/search")
    public ApiResult<SearchResult> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        OrderSearchService.SearchPage result = searchService.search(keyword, status, page, size);
        log.debug("搜索订单完成 keyword={}, status={}, total={}", keyword, status, result.total());
        return ApiResult.ok(new SearchResult(result.total(), result.items()));
    }

    @PostMapping("/admin/search/reindex/{orderNo}")
    public ApiResult<OrderView> reindex(@PathVariable String orderNo) {
        searchService.index(orderApplicationService.requireOrder(orderNo));
        log.info("订单搜索索引重建完成 orderNo={}", orderNo);
        return ApiResult.ok(orderApplicationService.getOrder(orderNo));
    }

    @GetMapping("/info")
    public ApiResult<Map<String, Object>> info() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("application", "order-service");
        info.put("nacosConfigStatus", nacosRuntimeConfig.status());
        info.put("nacosConfigError", nacosRuntimeConfig.lastError());
        info.put("orderTimeoutSeconds", nacosRuntimeConfig.timeoutSeconds());
        info.put("kafkaTopic", properties.getKafka().getTopic());
        info.put("rocketMqTimeoutTopic", properties.getRocketmq().getTopic());
        info.put("elasticsearchIndex", properties.getSearch().getIndex());
        info.put("pendingOutboxEvents", outboxRepository.countPending());
        info.put("paymentService", "payment-service via Nacos + Dubbo");
        info.put("components", Map.of(
                "mysql", "test.op_order/op_inventory/op_outbox_event",
                "redis", "reminder-idempotency-locks",
                "nacos", "config-center + dubbo-registry",
                "dubbo", "PaymentRpcFacade",
                "kafka", "order-event-indexing",
                "rocketmq", "delayed-order-timeout",
                "elasticsearch", "order-search"));
        return ApiResult.ok(info);
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
