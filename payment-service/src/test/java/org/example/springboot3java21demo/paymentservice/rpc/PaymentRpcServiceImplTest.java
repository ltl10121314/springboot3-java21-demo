package org.example.springboot3java21demo.paymentservice.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot3java21demo.common.trace.TraceContext;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcRequest;
import org.example.springboot3java21demo.orderpay.rpc.PaymentRpcResponse;
import org.example.springboot3java21demo.paymentservice.config.NacosPaymentConfig;
import org.example.springboot3java21demo.paymentservice.domain.PaymentRecord;
import org.example.springboot3java21demo.paymentservice.domain.PaymentStatus;
import org.example.springboot3java21demo.paymentservice.gateway.PaymentGateway;
import org.example.springboot3java21demo.paymentservice.repository.PaymentJdbcRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentRpcServiceImplTest {

    private PaymentJdbcRepository repository;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private PaymentGateway gateway;
    private NacosPaymentConfig nacosConfig;
    private PaymentRpcServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(PaymentJdbcRepository.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        gateway = mock(PaymentGateway.class);
        nacosConfig = mock(NacosPaymentConfig.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new PaymentRpcServiceImpl(
                repository, redisTemplate, gateway, nacosConfig,
                new ObjectMapper().findAndRegisterModules());
    }

    @AfterEach
    void clearTrace() {
        TraceContext.clear();
    }

    @Test
    @DisplayName("首次 RPC 支付成功后落成功流水并保留请求 traceId")
    void paysSuccessfullyWithRequestTraceId() {
        PaymentRpcRequest request = request("PAY-REQ-1", "trace-payment-1");
        when(nacosConfig.channelAllowed("ALIPAY")).thenReturn(true);
        when(repository.findByRequestId("PAY-REQ-1")).thenReturn(Optional.empty());
        when(repository.findLatestByOrderNo("ORD-1")).thenReturn(Optional.empty());
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(gateway.charge(anyString(), anyString(), any(), eq("ALIPAY")))
                .thenReturn(PaymentGateway.ChargeResult.success("CHANNEL-1"));
        when(repository.complete(anyString(), any(), any(), any(), any())).thenReturn(true);

        PaymentRpcResponse response = service.pay(request);

        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getChannelTradeNo()).isEqualTo("CHANNEL-1");
        assertThat(TraceContext.current()).isNull();
        verify(repository).insert(any(PaymentRecord.class));
        verify(repository).complete(
                eq("PAY-1"), eq(PaymentStatus.SUCCESS), eq("CHANNEL-1"), isNull(), any(Instant.class));
    }

    @Test
    @DisplayName("渠道运行异常时标记 UNKNOWN，不能误判为失败")
    void marksUnknownWhenGatewayThrows() {
        PaymentRpcRequest request = request("PAY-REQ-2", "trace-payment-2");
        when(nacosConfig.channelAllowed("ALIPAY")).thenReturn(true);
        when(repository.findByRequestId("PAY-REQ-2")).thenReturn(Optional.empty());
        when(repository.findLatestByOrderNo("ORD-1")).thenReturn(Optional.empty());
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(true);
        when(gateway.charge(anyString(), anyString(), any(), eq("ALIPAY")))
                .thenThrow(new IllegalStateException("channel network error"));
        when(repository.complete(anyString(), any(), any(), any(), any())).thenReturn(true);

        PaymentRpcResponse response = service.pay(request);

        ArgumentCaptor<PaymentStatus> target = ArgumentCaptor.forClass(PaymentStatus.class);
        verify(repository).complete(
                eq("PAY-2"), target.capture(), isNull(), anyString(), any(Instant.class));
        assertThat(target.getValue()).isEqualTo(PaymentStatus.UNKNOWN);
        assertThat(response.getStatus()).isEqualTo("UNKNOWN");
        verify(repository, never()).complete(
                anyString(), eq(PaymentStatus.FAILED), any(), any(), any());
    }

    @Test
    @DisplayName("相同 requestId 直接返回首次结果，不再次调用渠道")
    void reusesExistingPaymentByRequestId() {
        when(nacosConfig.channelAllowed("ALIPAY")).thenReturn(true);
        PaymentRecord existing = new PaymentRecord(
                "PAY-3", "PAY-REQ-3", "ORD-3", new BigDecimal("9.90"), "ALIPAY",
                PaymentStatus.SUCCESS, "CHANNEL-3", null, 1,
                Instant.now().minusSeconds(5), Instant.now());
        when(repository.findByRequestId("PAY-REQ-3")).thenReturn(Optional.of(existing));

        PaymentRpcResponse response = service.pay(request("PAY-REQ-3", "trace-payment-3"));

        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.isDuplicate()).isTrue();
        verifyNoInteractions(gateway);
    }

    @Test
    @DisplayName("查询不存在的支付单抛出明确错误")
    void reportsMissingPayment() {
        when(repository.findByRequestId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.query("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("支付记录不存在");
    }

    private PaymentRpcRequest request(String requestId, String traceId) {
        return new PaymentRpcRequest(
                requestId, "ORD-1", requestId.replace("-REQ", ""),
                "ALIPAY", new BigDecimal("10.00"), traceId);
    }
}
