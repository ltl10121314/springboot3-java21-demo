package org.example.springboot3java21demo.orderpay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 库存表 {@code op_inventory}。一行代表一个 SKU 的库存汇总。
 *
 * @param skuId 商品 SKU 标识，同时作为主键
 * @param skuName 商品名称
 * @param price 商品当前销售单价
 * @param totalStock 总库存，必须覆盖预占库存与已售库存
 * @param reservedStock 已下单但尚未支付成功的预占库存
 * @param soldStock 已支付成功并确认售出的库存
 * @param updatedAt 库存记录最后更新时间
 */
@TableName("op_inventory")
public record InventoryRecord(
        @TableId(value = "sku_id", type = IdType.INPUT) String skuId,
        @TableField("sku_name") String skuName,
        @TableField("price") BigDecimal price,
        @TableField("total_stock") long totalStock,
        @TableField("reserved_stock") long reservedStock,
        @TableField("sold_stock") long soldStock,
        @TableField("updated_at") Instant updatedAt) {

    /**
     * 当前可售库存 = 总库存 - 预占库存 - 已售库存。
     */
    public long availableStock() {
        return totalStock - reservedStock - soldStock;
    }
}
