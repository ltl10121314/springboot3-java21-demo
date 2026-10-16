package org.example.springboot3java21demo.payment.domain;

/**
 * 库存快照。
 *
 * <p>用 total / reserved / sold 三段式建模，而不是单字段 stock，
 * 因为「支付中」的库存必须被冻结（reserved），既不能卖给别人，也还没算卖出。
 * 关键不变式：{@code available() >= 0} 且 {@code reserved >= 0} 且 {@code sold <= total}。</p>
 *
 * @param total    总库存
 * @param reserved 已预扣、等待支付结果的库存
 * @param sold     已支付成功、真正卖出的库存
 */
public record StockSnapshot(String skuId, long total, long reserved, long sold) {

    /**
     * 可售库存 = 总量 - 冻结 - 已售
     */
    public long available() {
        return total - reserved - sold;
    }

    /**
     * 追加预扣，返回新快照；库存不足返回 null
     */
    public StockSnapshot reserve(long quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }
        if (available() < quantity) {
            return null;
        }
        return new StockSnapshot(skuId, total, reserved + quantity, sold);
    }

    /**
     * 预扣转已售：reserved -> sold
     */
    public StockSnapshot confirm(long quantity) {
        if (reserved < quantity) {
            throw new IllegalStateException("确认量超过预扣量, skuId=" + skuId);
        }
        return new StockSnapshot(skuId, total, reserved - quantity, sold + quantity);
    }

    /**
     * 释放预扣：reserved -> available
     */
    public StockSnapshot release(long quantity) {
        if (reserved < quantity) {
            throw new IllegalStateException("释放量超过预扣量, skuId=" + skuId);
        }
        return new StockSnapshot(skuId, total, reserved - quantity, sold);
    }
}
