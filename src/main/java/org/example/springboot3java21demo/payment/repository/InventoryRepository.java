package org.example.springboot3java21demo.payment.repository;

import org.example.springboot3java21demo.payment.domain.StockSnapshot;

import java.util.Optional;

/**
 * 库存仓储端口 —— 「不超卖」的执行者。
 *
 * <p>生产实现推荐用 MySQL 条件更新（单条 UPDATE 天然原子，无需显式加锁）：</p>
 * <pre>
 *   -- 预扣：affected rows = 1 才算抢到
 *   UPDATE inventory SET reserved = reserved + #{qty}, updated_at = NOW()
 *    WHERE sku_id = #{skuId} AND total - reserved - sold &gt;= #{qty};
 *
 *   -- 支付成功确认：reserved -> sold
 *   UPDATE inventory SET reserved = reserved - #{qty}, sold = sold + #{qty}
 *    WHERE sku_id = #{skuId} AND reserved &gt;= #{qty};
 *
 *   -- 支付失败释放
 *   UPDATE inventory SET reserved = reserved - #{qty}
 *    WHERE sku_id = #{skuId} AND reserved &gt;= #{qty};
 *
 *   -- 最后防线，任何绕过应用的写入都会被数据库拒绝
 *   ALTER TABLE inventory ADD CONSTRAINT ck_stock_non_negative
 *     CHECK (reserved &gt;= 0 AND sold &gt;= 0 AND total - reserved - sold &gt;= 0);
 * </pre>
 *
 * <p>热点 SKU 单行会被打爆，生产上再叠加 Redis Lua 预扣削峰（见 PaymentConstants.STOCK_RESERVE_LUA）
 * 与分段库存（把一条库存行拆成 N 段，请求按 hash 分散）。</p>
 */
public interface InventoryRepository {

    /**
     * 原子预扣库存。库存不足返回 false，绝不出现「扣成负数」。
     */
    boolean tryReserve(String skuId, int quantity);

    /**
     * 支付成功：预扣转为已售
     */
    boolean confirm(String skuId, int quantity);

    /**
     * 支付失败/关单：释放预扣
     */
    boolean release(String skuId, int quantity);

    Optional<StockSnapshot> find(String skuId);
}
