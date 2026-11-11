package org.example.springboot3java21demo.paymentservice.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    ChargeResult charge(String paymentNo, String orderNo, BigDecimal amount, String channel);

    record ChargeResult(
            boolean success,
            boolean unknown,
            String channelTradeNo,
            String code,
            String message) {

        public static ChargeResult success(String tradeNo) {
            return new ChargeResult(true, false, tradeNo, "SUCCESS", "支付成功");
        }

        public static ChargeResult failed(String code, String message) {
            return new ChargeResult(false, false, null, code, message);
        }

        public static ChargeResult unknown(String code, String message) {
            return new ChargeResult(false, true, null, code, message);
        }
    }
}
