package org.example.springboot3java21demo.orderpay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 订单主表 {@code op_order}。一行代表一次已受理的下单请求。
 *
 * @param orderNo 订单业务主键，对外展示和支付链路使用
 * @param requestId 创建订单幂等键，同一请求重复提交时返回已有订单
 * @param userId 下单用户标识
 * @param skuId 商品 SKU 标识
 * @param skuName 下单时的商品名称快照
 * @param unitPrice 下单时的商品单价快照
 * @param quantity 购买数量
 * @param totalAmount 订单总金额，即单价乘购买数量
 * @param status 订单状态
 * @param paymentNo 当前支付尝试的支付单号，未支付时为空
 * @param version 乐观锁版本号，每次订单状态变更递增
 * @param createdAt 订单创建时间
 * @param updatedAt 订单最后更新时间
 * @param paidAt 支付成功时间，未支付时为空
 */
@TableName("op_order")
public record OrderRecord(
        @TableId(value = "order_no", type = IdType.INPUT) String orderNo,
        @TableField("request_id") String requestId,
        @TableField("user_id") String userId,
        @TableField("sku_id") String skuId,
        @TableField("sku_name") String skuName,
        @TableField("unit_price") BigDecimal unitPrice,
        @TableField("quantity") int quantity,
        @TableField("total_amount") BigDecimal totalAmount,
        @TableField("status") OrderStatus status,
        @TableField("payment_no") String paymentNo,
        @TableField("version") long version,
        @TableField("created_at") Instant createdAt,
        @TableField("updated_at") Instant updatedAt,
        @TableField("paid_at") Instant paidAt) {
}
