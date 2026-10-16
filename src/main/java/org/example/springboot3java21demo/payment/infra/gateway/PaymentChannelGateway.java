package org.example.springboot3java21demo.payment.infra.gateway;

import java.math.BigDecimal;

/**
 * 第三方支付渠道端口（支付宝/微信/银联…）。
 *
 * <p>对接铁律：</p>
 * <ol>
 *   <li>支付与查询都必须带平台流水号 paymentNo，保证渠道侧幂等，重试不会重复扣款；</li>
 *   <li>调用必须设超时（连接/读），禁止无限等待；</li>
 *   <li>超时和异常一律归为 unknown，绝不能归为失败；</li>
 *   <li>结果未知的单必须由补偿任务调用 queryTrade 确认真实结果。</li>
 * </ol>
 */
public interface PaymentChannelGateway {

    /**
     * 发起支付
     */
    Result pay(PayCommand command);

    /**
     * 主动查询交易（补偿任务用），必须幂等
     */
    Result queryTrade(String paymentNo);

    /**
     * 支付指令
     */
    record PayCommand(String paymentNo,
                      String orderNo,
                      BigDecimal amount,
                      String channel,
                      String traceId) {
    }

    /**
     * 渠道结果三态。
     *
     * <p>注意这是三态而不是布尔：success=false 且 unknown=false 表示「渠道明确拒绝」，
     * 才可以释放库存；unknown=true 表示「不知道」，此时任何释放动作都可能造成钱货两失。</p>
     */
    record Result(boolean success, boolean unknown, String channelTradeNo, String code, String message) {

        public static Result success(String channelTradeNo, String code) {
            return new Result(true, false, channelTradeNo, code, "支付成功");
        }

        public static Result rejected(String code, String message) {
            return new Result(false, false, null, code, message);
        }

        public static Result unknown(String code, String message) {
            return new Result(false, true, null, code, message);
        }
    }
}
