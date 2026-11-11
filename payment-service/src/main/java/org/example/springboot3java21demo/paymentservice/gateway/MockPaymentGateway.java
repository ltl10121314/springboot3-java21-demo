package org.example.springboot3java21demo.paymentservice.gateway;

import lombok.extern.slf4j.Slf4j;
import org.example.springboot3java21demo.paymentservice.config.PaymentServiceProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
public class MockPaymentGateway implements PaymentGateway {

    private final PaymentServiceProperties properties;

    public MockPaymentGateway(PaymentServiceProperties properties) {
        this.properties = properties;
    }

    @Override
    public ChargeResult charge(String paymentNo, String orderNo, BigDecimal amount, String channel) {
        log.debug("支付微服务调用渠道 paymentNo={}, orderNo={}, channel={}, amount={}",
                paymentNo, orderNo, channel, amount);
        if (properties.getGateway().getLatencyMillis() > 0) {
            try {
                Thread.sleep(properties.getGateway().getLatencyMillis());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                return ChargeResult.unknown("INTERRUPTED", "渠道调用被中断");
            }
        }
        double random = ThreadLocalRandom.current().nextDouble();
        double unknownRate = properties.getGateway().getUnknownRate();
        if (random < unknownRate) {
            log.warn("支付渠道结果未知 paymentNo={}", paymentNo);
            return ChargeResult.unknown("TIMEOUT", "渠道响应超时，结果待确认");
        }
        if (random < unknownRate + properties.getGateway().getSuccessRate()) {
            log.info("支付渠道成功 paymentNo={}, channel={}", paymentNo, channel);
            return ChargeResult.success("CH-" + channel + "-" + paymentNo);
        }
        log.warn("支付渠道明确失败 paymentNo={}, reason=余额不足", paymentNo);
        return ChargeResult.failed("BALANCE_NOT_ENOUGH", "渠道余额不足");
    }
}
