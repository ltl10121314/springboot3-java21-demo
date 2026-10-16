package org.example.springboot3java21demo.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot3java21demo.payment.infra.gateway.MockPaymentChannelGateway;
import org.example.springboot3java21demo.payment.infra.gateway.PaymentChannelGateway;
import org.example.springboot3java21demo.payment.infra.idempotent.IdempotencyStore;
import org.example.springboot3java21demo.payment.infra.idempotent.InMemoryIdempotencyStore;
import org.example.springboot3java21demo.payment.infra.idempotent.RedisIdempotencyStore;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryChannelBillRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryInventoryRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryOutboxRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryPaymentOrderRepository;
import org.example.springboot3java21demo.payment.infra.memory.InMemoryReconcileTaskRepository;
import org.example.springboot3java21demo.payment.infra.outbox.LoggingOutboxPublisher;
import org.example.springboot3java21demo.payment.infra.outbox.OutboxPublisher;
import org.example.springboot3java21demo.payment.repository.ChannelBillRepository;
import org.example.springboot3java21demo.payment.repository.InventoryRepository;
import org.example.springboot3java21demo.payment.repository.OutboxRepository;
import org.example.springboot3java21demo.payment.repository.PaymentOrderRepository;
import org.example.springboot3java21demo.payment.repository.ReconcileTaskRepository;
import org.example.springboot3java21demo.payment.service.OutboxRelay;
import org.example.springboot3java21demo.payment.service.PaymentService;
import org.example.springboot3java21demo.payment.service.PaymentTimeoutCompensator;
import org.example.springboot3java21demo.payment.service.ReconcileService;
import org.example.springboot3java21demo.payment.service.impl.PaymentServiceImpl;
import org.example.springboot3java21demo.payment.service.impl.ReconcileServiceImpl;
import org.example.springboot3java21demo.payment.support.PaymentCircuitBreaker;
import org.example.springboot3java21demo.payment.support.PaymentMetrics;
import org.example.springboot3java21demo.payment.support.PaymentRateLimiter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 支付框架装配。
 *
 * <p>这里用的是内存实现，方便在没有 MySQL/Redis 的环境直接跑通全链路和压测。
 * 接入真实中间件时只需要实现同一组端口接口并替换这里的 Bean，业务代码零改动：</p>
 * <table border="1">
 *   <tr><th>端口</th><th>演示实现</th><th>生产实现</th></tr>
 *   <tr><td>PaymentOrderRepository</td><td>内存 Map</td><td>MySQL + 乐观锁 + 唯一索引</td></tr>
 *   <tr><td>InventoryRepository</td><td>CAS 自旋</td><td>MySQL 条件更新 + 分段库存</td></tr>
 *   <tr><td>OutboxRepository</td><td>内存 Map</td><td>与业务同库的 outbox 表</td></tr>
 *   <tr><td>IdempotencyStore</td><td>ConcurrentHashMap</td><td>Redis SET NX EX</td></tr>
 *   <tr><td>OutboxPublisher</td><td>日志</td><td>Kafka / RocketMQ</td></tr>
 * </table>
 */
@Configuration
@EnableScheduling
public class PaymentConfig {

    @Bean
    public InventoryRepository paymentInventoryRepository(
            @Value("${payment.demo.sku-id:DEMO-SKU-001}") String demoSkuId,
            @Value("${payment.demo.stock-total:1000}") long stockTotal) {
        return new InMemoryInventoryRepository().seed(demoSkuId, stockTotal);
    }

    @Bean
    public PaymentOrderRepository paymentOrderRepository() {
        return new InMemoryPaymentOrderRepository();
    }

    @Bean
    public OutboxRepository paymentOutboxRepository() {
        return new InMemoryOutboxRepository();
    }

    @Bean
    public ChannelBillRepository paymentChannelBillRepository() {
        return new InMemoryChannelBillRepository();
    }

    @Bean
    public ReconcileTaskRepository paymentReconcileTaskRepository() {
        return new InMemoryReconcileTaskRepository();
    }

    /**
     * 幂等存储：payment.idempotency.store=redis 时启用 Redis 实现，否则用内存实现。
     */
    @Bean
    @ConditionalOnProperty(name = "payment.idempotency.store", havingValue = "redis")
    public IdempotencyStore redisIdempotencyStore(StringRedisTemplate stringRedisTemplate,
                                                  ObjectMapper objectMapper) {
        return new RedisIdempotencyStore(stringRedisTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(IdempotencyStore.class)
    public IdempotencyStore inMemoryIdempotencyStore() {
        return new InMemoryIdempotencyStore();
    }

    @Bean
    public PaymentChannelGateway paymentChannelGateway(
            @Value("${payment.demo.channel.success-rate:1.0}") double successRate,
            @Value("${payment.demo.channel.unknown-rate:0.0}") double unknownRate,
            @Value("${payment.demo.channel.latency-ms:0}") long latencyMillis) {
        return new MockPaymentChannelGateway(successRate, unknownRate, latencyMillis);
    }

    @Bean
    public PaymentCircuitBreaker paymentCircuitBreaker(
            @Value("${payment.channel.circuit.failure-threshold:20}") int failureThreshold,
            @Value("${payment.channel.circuit.open-duration-ms:10000}") long openDurationMillis,
            @Value("${payment.channel.circuit.half-open-probe:5}") int halfOpenProbeCount) {
        return new PaymentCircuitBreaker(failureThreshold, openDurationMillis, halfOpenProbeCount);
    }

    @Bean
    public PaymentRateLimiter paymentRateLimiter(
            @Value("${payment.rate-limit.window-seconds:1}") int windowSeconds,
            @Value("${payment.rate-limit.max-requests:5000}") long maxRequests) {
        return new PaymentRateLimiter(windowSeconds, maxRequests);
    }

    @Bean
    public PaymentMetrics paymentMetrics() {
        return new PaymentMetrics();
    }

    @Bean
    public OutboxPublisher outboxPublisher() {
        return new LoggingOutboxPublisher();
    }

    @Bean
    public PaymentService paymentService(PaymentOrderRepository orderRepository,
                                         InventoryRepository inventoryRepository,
                                         OutboxRepository outboxRepository,
                                         IdempotencyStore idempotencyStore,
                                         PaymentChannelGateway channelGateway,
                                         PaymentCircuitBreaker circuitBreaker,
                                         PaymentRateLimiter rateLimiter,
                                         PaymentMetrics metrics) {
        return new PaymentServiceImpl(orderRepository, inventoryRepository, outboxRepository,
                idempotencyStore, channelGateway, circuitBreaker, rateLimiter, metrics);
    }

    @Bean
    public OutboxRelay outboxRelay(OutboxRepository outboxRepository,
                                   OutboxPublisher publisher,
                                   PaymentMetrics metrics) {
        return new OutboxRelay(outboxRepository, publisher, metrics);
    }

    @Bean
    public PaymentTimeoutCompensator paymentTimeoutCompensator(
            PaymentOrderRepository orderRepository,
            InventoryRepository inventoryRepository,
            OutboxRepository outboxRepository,
            PaymentChannelGateway channelGateway,
            PaymentMetrics metrics,
            @Value("${payment.compensate.unsettled-timeout-ms:30000}") long unsettledTimeoutMillis) {
        return new PaymentTimeoutCompensator(orderRepository, inventoryRepository, outboxRepository,
                channelGateway, metrics, unsettledTimeoutMillis);
    }

    @Bean
    public ReconcileService reconcileService(PaymentOrderRepository orderRepository,
                                             ChannelBillRepository channelBillRepository,
                                             ReconcileTaskRepository reconcileTaskRepository) {
        return new ReconcileServiceImpl(orderRepository, channelBillRepository, reconcileTaskRepository);
    }
}
