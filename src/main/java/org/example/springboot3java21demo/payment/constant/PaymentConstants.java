package org.example.springboot3java21demo.payment.constant;

import java.time.Duration;

/**
 * 支付相关常量
 */
public final class PaymentConstants {

    /**
     * Redis 幂等键前缀
     */
    public static final String IDEMPOTENCY_KEY_PREFIX = "payment:idempotency:";

    /**
     * 幂等键过期时间，建议大于一次支付流程的最长耗时
     */
    public static final Duration IDEMPOTENCY_TTL = Duration.ofMinutes(30);

    /**
     * 幂等键的占位值。
     *
     * <p>注意：支付单的正式状态以 {@code domain.PaymentStatus} 枚举为准，
     * 这里的字符串只用于幂等存储中的「尚未落定」标记与查询未命中的返回。</p>
     */
    public static final String STATUS_PROCESSING = "PROCESSING";

    /**
     * 对外返回「未找到支付记录」的状态
     */
    public static final String STATUS_NOT_FOUND = "NOT_FOUND";

    /**
     * 消息表 / 事件相关配置
     */
    public static final int OUTBOX_MAX_RETRY = 8;

    /**
     * 本地消息表每批扫描条数
     */
    public static final int OUTBOX_BATCH_SIZE = 200;

    /**
     * 库存预扣键前缀，Redis 中 value 形态为 {total, reserved, sold}
     */
    public static final String STOCK_KEY_PREFIX = "payment:stock:";

    /**
     * 库存预扣的 Lua 脚本：一次 RTT 内完成「判断 + 预扣」，避免超卖。
     * KEYS[1]=库存 key, ARGV[1]=预扣数量。返回 1 表示成功，0 表示库存不足。
     */
    public static final String STOCK_RESERVE_LUA = """
            local total    = tonumber(redis.call('HGET', KEYS[1], 'total')    or '0')
            local reserved = tonumber(redis.call('HGET', KEYS[1], 'reserved') or '0')
            local sold     = tonumber(redis.call('HGET', KEYS[1], 'sold')     or '0')
            local qty      = tonumber(ARGV[1])
            if total - reserved - sold < qty then
                return 0
            end
            redis.call('HINCRBY', KEYS[1], 'reserved', qty)
            return 1
            """;

    /**
     * 支付成功事件：确认扣减库存（reserved -> sold）
     */
    public static final String EVENT_PAYMENT_SUCCESS = "PAYMENT_SUCCESS";

    /**
     * 支付失败事件：释放库存预扣
     */
    public static final String EVENT_PAYMENT_FAILED = "PAYMENT_FAILED";

    /**
     * 支付结果未知事件：必须由补偿任务向渠道查询后闭环，绝不能直接当失败处理
     */
    public static final String EVENT_PAYMENT_UNKNOWN = "PAYMENT_UNKNOWN";

    /**
     * 库存释放事件
     */
    public static final String EVENT_STOCK_RELEASED = "STOCK_RELEASED";

    /**
     * 对账差异事件
     */
    public static final String EVENT_RECONCILE_DIFF = "RECONCILE_DIFF";

    /**
     * MDC / 链路透传头
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * MDC key
     */
    public static final String TRACE_ID_MDC_KEY = "traceId";

    public static final String AGGREGATE_PAYMENT = "PAYMENT";

    private PaymentConstants() {
    }
}
