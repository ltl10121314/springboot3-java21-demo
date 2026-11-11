package org.example.springboot3java21demo.orderpay.service;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.orderpay.config.NacosRuntimeConfig;
import org.example.springboot3java21demo.orderpay.domain.InventoryRecord;
import org.example.springboot3java21demo.orderpay.domain.OrderEvent;
import org.example.springboot3java21demo.orderpay.domain.OrderRecord;
import org.example.springboot3java21demo.orderpay.domain.OrderStatus;
import org.example.springboot3java21demo.orderpay.domain.OrderTimeoutCommand;
import org.example.springboot3java21demo.orderpay.exception.OrderPayException;
import org.example.springboot3java21demo.orderpay.repository.OrderInventoryRepository;
import org.example.springboot3java21demo.orderpay.repository.OrderJdbcRepository;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.example.springboot3java21demo.orderpay.web.CreateOrderRequest;
import org.example.springboot3java21demo.orderpay.web.OrderView;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class OrderApplicationService {

    private static final DateTimeFormatter DAY_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final OrderJdbcRepository orderRepository;
    private final OrderInventoryRepository inventoryRepository;
    private final OrderEventOutbox eventOutbox;
    private final TransactionTemplate transactionTemplate;
    private final NacosRuntimeConfig nacosRuntimeConfig;

    public OrderApplicationService(
            OrderJdbcRepository orderRepository,
            OrderInventoryRepository inventoryRepository,
            OrderEventOutbox eventOutbox,
            TransactionTemplate transactionTemplate,
            NacosRuntimeConfig nacosRuntimeConfig) {
        this.orderRepository = orderRepository;
        this.inventoryRepository = inventoryRepository;
        this.eventOutbox = eventOutbox;
        this.transactionTemplate = transactionTemplate;
        this.nacosRuntimeConfig = nacosRuntimeConfig;
    }

    public OrderView createOrder(CreateOrderRequest request) {
        var existing = orderRepository.findByRequestId(request.requestId());
        if (existing.isPresent()) {
            OrderRecord order = existing.get();
            log.info("订单创建幂等命中 requestId={}, orderNo={}, status={}",
                    request.requestId(), order.orderNo(), order.status());
            return OrderView.of(order, true);
        }
        log.debug("准备创建订单 requestId={}, skuId={}, quantity={}",
                request.requestId(), request.skuId(), request.quantity());
        return createNewOrder(request);
    }

    public OrderView getOrder(String orderNo) {
        return OrderView.of(requireOrder(orderNo), false);
    }

    public OrderView cancelOrder(String orderNo) {
        return transactionTemplate.execute(status -> {
            OrderRecord order = requireOrder(orderNo);
            if (order.status() == OrderStatus.CANCELLED) {
                return OrderView.of(order, true);
            }
            if (order.status() != OrderStatus.CREATED) {
                throw OrderPayException.conflict("当前订单状态不允许取消: " + order.status());
            }
            Instant now = Instant.now();
            if (!orderRepository.cancelCreated(orderNo, now)
                    || !inventoryRepository.release(order.skuId(), order.quantity())) {
                throw new IllegalStateException("取消订单失败，请重试");
            }
            OrderRecord cancelled = requireOrder(orderNo);
            appendOrderEvent(cancelled, "ORDER_CANCELLED");
            log.info("订单取消成功 orderNo={}, releasedQuantity={}", orderNo, order.quantity());
            return OrderView.of(cancelled, false);
        });
    }

    /**
     * 支付调用前先冻结订单，避免支付过程中被取消或超时关单。
     */
    public OrderRecord beginPayment(String orderNo) {
        return transactionTemplate.execute(status -> {
            OrderRecord order = requireOrder(orderNo);
            if (order.status() == OrderStatus.PAID || order.status() == OrderStatus.PAYING) {
                return order;
            }
            if (order.status() != OrderStatus.CREATED) {
                throw OrderPayException.conflict("当前订单状态不允许支付: " + order.status());
            }
            Instant now = Instant.now();
            String paymentNo = newPaymentNo();
            if (!orderRepository.markPaying(orderNo, order.version(), paymentNo, now)) {
                OrderRecord latest = requireOrder(orderNo);
                if (latest.status() == OrderStatus.PAYING || latest.status() == OrderStatus.PAID) {
                    return latest;
                }
                throw OrderPayException.conflict("订单状态已变化，请重新查询");
            }
            log.info("订单进入待支付确认状态 orderNo={}, paymentNo={}", orderNo, paymentNo);
            return requireOrder(orderNo);
        });
    }

    /**
     * 支付服务返回成功后，订单服务负责库存确认和订单成功事件。
     */
    public OrderRecord completePayment(
            String orderNo, String paymentNo, String channelTradeNo) {
        return transactionTemplate.execute(status -> {
            OrderRecord order = requireOrder(orderNo);
            if (order.status() == OrderStatus.PAID
                    && java.util.Objects.equals(order.paymentNo(), paymentNo)) {
                return order;
            }
            if (order.status() != OrderStatus.PAYING
                    || !java.util.Objects.equals(order.paymentNo(), paymentNo)) {
                throw OrderPayException.conflict(
                        "订单支付状态不一致, status=" + order.status() + ", paymentNo=" + order.paymentNo());
            }
            Instant now = Instant.now();
            if (!orderRepository.markPaid(orderNo, order.version(), now)
                    || !inventoryRepository.confirm(order.skuId(), order.quantity())) {
                throw new IllegalStateException("支付成功后订单闭环失败");
            }
            OrderRecord paid = requireOrder(orderNo);
            appendOrderEvent(paid, "PAYMENT_SUCCESS");
            log.info("支付结果已应用订单成功状态 orderNo={}, paymentNo={}, channelTradeNo={}",
                    orderNo, paymentNo, channelTradeNo);
            return paid;
        });
    }

    /**
     * 渠道明确失败时恢复订单，库存仍保持预扣以便使用新 requestId 重试。
     */
    public OrderRecord failPayment(String orderNo, String paymentNo, String reason) {
        return transactionTemplate.execute(status -> {
            OrderRecord order = requireOrder(orderNo);
            if (order.status() == OrderStatus.CREATED || order.status() == OrderStatus.PAID) {
                return order;
            }
            if (order.status() != OrderStatus.PAYING
                    || !java.util.Objects.equals(order.paymentNo(), paymentNo)) {
                throw OrderPayException.conflict("支付失败回调与订单状态不一致");
            }
            if (!orderRepository.revertToCreated(orderNo, order.version(), Instant.now())) {
                throw new IllegalStateException("支付失败后恢复订单状态失败");
            }
            OrderRecord reverted = requireOrder(orderNo);
            appendOrderEvent(reverted, "PAYMENT_FAILED");
            log.warn("支付失败结果已应用订单状态 orderNo={}, paymentNo={}, reason={}",
                    orderNo, paymentNo, reason);
            return reverted;
        });
    }

    /**
     * 结果未知时订单继续保持 PAYING，等待支付服务/渠道确认。
     */
    public OrderRecord markPaymentUnknown(String orderNo, String paymentNo, String reason) {
        return transactionTemplate.execute(status -> {
            OrderRecord order = requireOrder(orderNo);
            if (order.status() == OrderStatus.PAID) {
                return order;
            }
            if (order.status() != OrderStatus.PAYING
                    || !java.util.Objects.equals(order.paymentNo(), paymentNo)) {
                throw OrderPayException.conflict("支付未知结果与订单状态不一致");
            }
            log.warn("支付结果未知，订单保持待确认 orderNo={}, paymentNo={}, reason={}",
                    orderNo, paymentNo, reason);
            return order;
        });
    }

    /**
     * RocketMQ 延迟消息到期后调用。只有仍处于 CREATED 的订单才会关闭，
     * PAYING/PAID 状态不会被超时任务误伤。
     */
    public void closeTimedOutOrder(String orderNo) {
        transactionTemplate.executeWithoutResult(status -> {
            OrderRecord order = orderRepository.findByOrderNo(orderNo).orElse(null);
            if (order == null || order.status() != OrderStatus.CREATED) {
                return;
            }
            Instant now = Instant.now();
            if (!orderRepository.closeCreated(orderNo, now)) {
                return;
            }
            if (!inventoryRepository.release(order.skuId(), order.quantity())) {
                throw new IllegalStateException("超时关单释放库存失败: " + orderNo);
            }
            OrderRecord closed = requireOrder(orderNo);
            appendOrderEvent(closed, "ORDER_CLOSED");
            log.info("RocketMQ 延迟消息触发关单 orderNo={}", orderNo);
        });
    }

    public InventoryRecord inventory(String skuId) {
        return inventoryRepository.find(skuId)
                .orElseThrow(() -> OrderPayException.notFound("SKU 不存在: " + skuId));
    }

    private OrderView createNewOrder(CreateOrderRequest request) {
        InventoryRecord inventory = inventory(SKU(request));
        if (inventory.availableStock() < request.quantity()) {
            log.warn("订单创建库存不足 requestId={}, skuId={}, requested={}, available={}",
                    request.requestId(), inventory.skuId(), request.quantity(), inventory.availableStock());
            throw OrderPayException.conflict("库存不足: " + inventory.skuId());
        }
        Instant now = Instant.now();
        BigDecimal amount = inventory.price().multiply(BigDecimal.valueOf(request.quantity()));
        OrderRecord order = new OrderRecord(
                newOrderNo(),
                request.requestId(),
                request.userId(),
                inventory.skuId(),
                inventory.skuName(),
                inventory.price(),
                request.quantity(),
                amount,
                OrderStatus.CREATED,
                null,
                0,
                now,
                now,
                null);
        try {
            transactionTemplate.executeWithoutResult(status -> {
                orderRepository.insert(order);
                if (!inventoryRepository.reserve(order.skuId(), order.quantity())) {
                    throw OrderPayException.conflict("库存不足: " + order.skuId());
                }
                appendOrderEvent(order, "ORDER_CREATED");
                Instant expiresAt = now.plusSeconds(nacosRuntimeConfig.timeoutSeconds());
                eventOutbox.appendTimeoutCommand(new OrderTimeoutCommand(
                        UUID.randomUUID().toString(),
                        order.orderNo(),
                        expiresAt,
                        TraceContext.current()));
            });
            log.info("订单创建成功 orderNo={}, requestId={}, skuId={}, quantity={}, amount={}",
                    order.orderNo(), order.requestId(), order.skuId(), order.quantity(), order.totalAmount());
            return OrderView.of(order, false);
        } catch (DuplicateKeyException ex) {
            return orderRepository.findByRequestId(request.requestId())
                    .map(existing -> {
                        log.info("并发创建订单命中唯一索引，返回已有订单 requestId={}, orderNo={}",
                                request.requestId(), existing.orderNo());
                        return OrderView.of(existing, true);
                    })
                    .orElseThrow(() -> ex);
        }
    }

    private void appendOrderEvent(OrderRecord order, String eventType) {
        eventOutbox.appendKafka(new OrderEvent(
                UUID.randomUUID().toString(),
                eventType,
                order.orderNo(),
                order.userId(),
                order.status().name(),
                order.totalAmount(),
                Instant.now(),
                TraceContext.current()));
        log.debug("订单事件已写入 Outbox orderNo={}, eventType={}, status={}",
                order.orderNo(), eventType, order.status());
    }

    public OrderRecord requireOrder(String orderNo) {
        return orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> OrderPayException.notFound("订单不存在: " + orderNo));
    }

    private String newOrderNo() {
        return "ORD" + LocalDate.now().format(DAY_FORMAT)
                + System.currentTimeMillis() % 100_000_000
                + ThreadLocalRandom.current().nextInt(1000, 10_000);
    }

    private String newPaymentNo() {
        return "PAY" + System.currentTimeMillis()
                + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 8).toUpperCase();
    }

    private String SKU(CreateOrderRequest request) {
        return request.skuId().trim().toUpperCase();
    }
}
