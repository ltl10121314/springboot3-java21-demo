package org.example.springboot3java21demo.exercise.thread.pool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 异步聚合服务
 * 演示 CompletableFuture 在生产环境中的标准用法
 */
@Service
public class AsyncAggregationService {

    private static final Logger log = LoggerFactory.getLogger(AsyncAggregationService.class);

    /** 聚合整体超时时间（秒） */
    private static final long AGGREGATE_TIMEOUT_SECONDS = 5;

    // 通过 @Qualifier 注入 IO 密集型线程池（微服务调用属于 IO 操作）
    private final Executor ioExecutor;

    public AsyncAggregationService(@Qualifier("ioTaskExecutor") Executor ioExecutor) {
        this.ioExecutor = ioExecutor;
    }

    /**
     * 并行调用三个微服务并聚合结果
     *
     * @param userId 用户ID
     * @return 聚合后的结果字符串
     */
    public CompletableFuture<String> aggregateUserData(String userId) {
        // 1. 并行发起三个微服务调用
        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(
                () -> callUserService(userId), ioExecutor
        );
        CompletableFuture<String> orderFuture = CompletableFuture.supplyAsync(
                () -> callOrderService(userId), ioExecutor
        );
        CompletableFuture<String> couponFuture = CompletableFuture.supplyAsync(
                () -> callCouponService(userId), ioExecutor
        );

        // 2. 先等待全部完成并聚合结果，再施加整体超时。
        //    关键点：聚合（join）必须放在 orTimeout 之前——超时后 orTimeout 的回调
        //    在 JDK 全局共享的 Delayer 定时线程上执行，若此时再 join 未完成的子任务，
        //    会阻塞 Delayer 线程，导致全 JVM 的超时机制失效。
        //    此处 join 不会阻塞：allOf 完成说明三个子任务都已结束（有失败的则跳过本阶段）。
        return CompletableFuture.allOf(userFuture, orderFuture, couponFuture)
                .thenApply(v -> String.format("用户:%s | 订单:%s | 优惠券:%s",
                        userFuture.join(), orderFuture.join(), couponFuture.join()))
                .orTimeout(AGGREGATE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                // 3. 统一兜底：成功、超时、业务异常最终都收敛到这一处
                .handle((result, ex) -> {
                    if (ex == null) {
                        return result;
                    }
                    // 超时/失败后取消仍在执行的子任务，及时释放线程池资源
                    userFuture.cancel(true);
                    orderFuture.cancel(true);
                    couponFuture.cancel(true);
                    // 剥掉 CompletionException 包装后再判断：
                    // orTimeout 抛出的是 TimeoutException 本身，allOf 失败则是包装后的业务异常
                    if (unwrap(ex) instanceof TimeoutException) {
                        log.error("聚合用户数据超时, userId={}", userId, ex);
                    } else {
                        log.error("聚合用户数据异常, userId={}", userId, ex);
                    }
                    return "聚合服务暂时不可用，请稍后重试";
                });
    }

    /**
     * 剥掉 CompletionException 包装，返回真正的根因异常
     */
    private Throwable unwrap(Throwable ex) {
        Throwable root = ex;
        while (root instanceof CompletionException && root.getCause() != null) {
            root = root.getCause();
        }
        return root;
    }

    /**
     * 模拟用户服务调用
     */
    private String callUserService(String userId) {
        return callService("用户", userId, 200, "用户[张三]");
    }

    /**
     * 模拟订单服务调用
     */
    private String callOrderService(String userId) {
        return callService("订单", userId, 300, "3个待支付订单");
    }

    /**
     * 模拟优惠券服务调用
     */
    private String callCouponService(String userId) {
        return callService("优惠券", userId, 150, "5张可用优惠券");
    }

    /**
     * 模拟一次远程服务调用
     *
     * @param serviceName 服务名（用于日志）
     * @param userId      用户ID
     * @param delayMillis 模拟的网络延迟
     * @param result      模拟的返回结果
     */
    private String callService(String serviceName, String userId, long delayMillis, String result) {
        log.info("调用{}服务, userId={}", serviceName, userId);
        try {
            Thread.sleep(delayMillis); // 模拟网络延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(serviceName + "服务调用被中断", e);
        }
        return result;
    }
}