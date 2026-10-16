package org.example.springboot3java21demo.payment.infra.gateway;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟支付渠道：按配置的成功率/超时率返回三态，用于压测和演示降级路径。
 *
 * <p>生产替换为真实 SDK 实现，接口不变。真实实现里 pay 的超时必须显著小于上游 HTTP 超时，
 * 否则会出现「上游已重试、渠道仍在处理」的重复扣款窗口。</p>
 */
@Slf4j
public class MockPaymentChannelGateway implements PaymentChannelGateway {

    private final double successRate;
    private final double unknownRate;
    private final long latencyMillis;

    private final AtomicInteger payCallCount = new AtomicInteger();
    private final Map<String, Result> settledResults = new ConcurrentHashMap<>();

    public MockPaymentChannelGateway(double successRate, double unknownRate, long latencyMillis) {
        this.successRate = successRate;
        this.unknownRate = unknownRate;
        this.latencyMillis = latencyMillis;
    }

    public static MockPaymentChannelGateway alwaysSuccess() {
        return new MockPaymentChannelGateway(1.0d, 0.0d, 0L);
    }

    @Override
    public Result pay(PayCommand command) {
        payCallCount.incrementAndGet();
        sleep();

        // 渠道侧幂等：同一 paymentNo 重复请求返回首次结果
        Result settled = settledResults.get(command.paymentNo());
        if (settled != null) {
            log.info("[{}] 渠道重复请求，返回首次结果 paymentNo={}", command.traceId(), command.paymentNo());
            return settled;
        }

        double roll = ThreadLocalRandom.current().nextDouble();
        Result result;
        if (roll < unknownRate) {
            result = Result.unknown("TIMEOUT", "渠道响应超时");
        } else if (roll < unknownRate + successRate) {
            result = Result.success("CH" + command.paymentNo(), "SUCCESS");
        } else {
            result = Result.rejected("BALANCE_NOT_ENOUGH", "渠道余额不足");
        }

        // 超时不代表失败：渠道侧可能已经扣款成功，因此不落 settledResults，等补偿查询
        if (!result.unknown()) {
            settledResults.put(command.paymentNo(), result);
        }
        return result;
    }

    @Override
    public Result queryTrade(String paymentNo) {
        Result settled = settledResults.get(paymentNo);
        return settled != null ? settled : Result.unknown("PROCESSING", "渠道仍在处理中");
    }

    /**
     * 演示/测试用：手工把某笔交易置为渠道侧成功
     */
    public void settleAsSuccess(String paymentNo) {
        settledResults.put(paymentNo, Result.success("CH" + paymentNo, "SUCCESS"));
    }

    public int payCallCount() {
        return payCallCount.get();
    }

    private void sleep() {
        if (latencyMillis > 0) {
            try {
                Thread.sleep(latencyMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("渠道调用被中断", e);
            }
            return;
        }
        Thread.onSpinWait();
    }
}
