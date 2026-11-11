package org.example.springboot3java21demo.orderpay.rpc;

/**
 * 订单服务与支付服务共享的 Dubbo 契约。
 *
 * <p>接口和 DTO 只存在于 payment-api 模块，避免两个服务各自维护同名类导致
 * Dubbo 序列化类型不一致。</p>
 */
public interface PaymentRpcFacade {

    PaymentRpcResponse pay(PaymentRpcRequest request);

    PaymentRpcResponse query(String requestId);
}
