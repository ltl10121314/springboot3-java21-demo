package org.example.springboot3java21demo.payment.service;

import org.example.springboot3java21demo.payment.model.PaymentRequest;
import org.example.springboot3java21demo.payment.model.PaymentResult;

/**
 * 幂等支付服务
 */
public interface PaymentService {

    /**
     * 发起支付
     *
     * @param request 支付请求
     * @return 支付结果
     */
    PaymentResult pay(PaymentRequest request);

    /**
     * 查询支付结果
     *
     * @param requestId 幂等键
     * @return 支付结果
     */
    PaymentResult query(String requestId);
}
