package org.example.springboot3java21demo.orderpay.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.Instant;

/**
 * Outbox 事件表 {@code op_outbox_event}。事件先与业务数据同事务落库，再异步投递。
 *
 * @param id 数据库自增主键
 * @param eventId 事件业务唯一标识，用于消费端幂等
 * @param aggregateId 事件关联的聚合标识，订单事件通常为订单号
 * @param eventType 业务事件类型，如 ORDER_CREATED、ORDER_TIMEOUT_CHECK
 * @param destination 投递目标，当前支持 KAFKA 和 ROCKETMQ
 * @param payload 事件载荷 JSON，包含恢复关联 TraceId 所需信息
 * @param availableAt Outbox 最早可再次投递时间，失败重试时会向后顺延
 * @param deliverAt 消息业务期望投递时间，RocketMQ 延迟关单依赖该字段
 * @param status 投递状态，PENDING 待投递，SENT 已成功投递
 * @param retryCount 已失败并重新入队等待投递的次数
 * @param lastError 最近一次投递失败原因，成功投递后清空
 * @param createdAt Outbox 事件创建时间
 * @param sentAt 成功投递时间，尚未投递时为空
 */
@TableName("op_outbox_event")
public record OutboxRecord(
        @TableId(value = "id", type = IdType.AUTO) long id,
        @TableField("event_id") String eventId,
        @TableField("aggregate_id") String aggregateId,
        @TableField("event_type") String eventType,
        @TableField("destination") String destination,
        @TableField("payload") String payload,
        @TableField("available_at") Instant availableAt,
        @TableField("deliver_at") Instant deliverAt,
        @TableField("status") String status,
        @TableField("retry_count") int retryCount,
        @TableField("last_error") String lastError,
        @TableField("created_at") Instant createdAt,
        @TableField("sent_at") Instant sentAt) {
}
