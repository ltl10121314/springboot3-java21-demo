-- 为现有 MySQL 表补充列注释，可重复执行。
-- 本脚本只修改列注释，不改变字段类型、默认值、索引和业务数据。
SET NAMES utf8mb4;

ALTER TABLE `test`.`op_inventory`
    MODIFY COLUMN `sku_id` VARCHAR(64) NOT NULL COMMENT '商品SKU标识，主键',
    MODIFY COLUMN `sku_name` VARCHAR(128) NOT NULL COMMENT '商品名称',
    MODIFY COLUMN `price` DECIMAL(12, 2) NOT NULL COMMENT '商品当前销售单价',
    MODIFY COLUMN `total_stock` BIGINT NOT NULL COMMENT '总库存，须不小于预占库存与已售库存之和',
    MODIFY COLUMN `reserved_stock` BIGINT NOT NULL DEFAULT 0 COMMENT '已下单但尚未支付成功的预占库存',
    MODIFY COLUMN `sold_stock` BIGINT NOT NULL DEFAULT 0 COMMENT '支付成功后确认售出的库存',
    MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
        ON UPDATE CURRENT_TIMESTAMP(3) COMMENT '库存记录最后更新时间';

ALTER TABLE `test`.`op_order`
    MODIFY COLUMN `order_no` VARCHAR(40) NOT NULL COMMENT '订单业务主键',
    MODIFY COLUMN `request_id` VARCHAR(96) NOT NULL COMMENT '创建订单幂等键',
    MODIFY COLUMN `user_id` VARCHAR(64) NOT NULL COMMENT '下单用户标识',
    MODIFY COLUMN `sku_id` VARCHAR(64) NOT NULL COMMENT '商品SKU标识',
    MODIFY COLUMN `sku_name` VARCHAR(128) NOT NULL COMMENT '下单时的商品名称快照',
    MODIFY COLUMN `unit_price` DECIMAL(12, 2) NOT NULL COMMENT '下单时的商品单价快照',
    MODIFY COLUMN `quantity` INT NOT NULL COMMENT '购买数量',
    MODIFY COLUMN `total_amount` DECIMAL(12, 2) NOT NULL COMMENT '订单总金额，等于单价乘数量',
    MODIFY COLUMN `status` VARCHAR(24) NOT NULL
        COMMENT '订单状态：CREATED/PAYING/PAID/CANCELLED/CLOSED',
    MODIFY COLUMN `payment_no` VARCHAR(48) NULL
        COMMENT '当前支付尝试的支付单号，未支付为空',
    MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0
        COMMENT '乐观锁版本号，每次状态变更递增',
    MODIFY COLUMN `created_at` DATETIME(3) NOT NULL COMMENT '订单创建时间',
    MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL COMMENT '订单最后更新时间',
    MODIFY COLUMN `paid_at` DATETIME(3) NULL COMMENT '支付成功时间，未支付为空';

ALTER TABLE `test`.`op_outbox_event`
    MODIFY COLUMN `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据库自增主键',
    MODIFY COLUMN `event_id` VARCHAR(48) NOT NULL
        COMMENT '事件业务唯一标识，用于消费端幂等',
    MODIFY COLUMN `aggregate_id` VARCHAR(64) NOT NULL
        COMMENT '关联聚合标识，订单事件通常为订单号',
    MODIFY COLUMN `event_type` VARCHAR(40) NOT NULL
        COMMENT '业务事件类型，如ORDER_CREATED',
    MODIFY COLUMN `destination` VARCHAR(16) NOT NULL
        COMMENT '投递目标：KAFKA或ROCKETMQ',
    MODIFY COLUMN `payload` LONGTEXT NOT NULL
        COMMENT '事件JSON载荷，包含关联TraceId',
    MODIFY COLUMN `available_at` DATETIME(3) NOT NULL
        COMMENT 'Outbox最早可再次投递时间',
    MODIFY COLUMN `deliver_at` DATETIME(3) NOT NULL
        COMMENT '业务期望投递时间，RocketMQ延迟关单使用',
    MODIFY COLUMN `status` VARCHAR(16) NOT NULL
        COMMENT '投递状态：PENDING待投递，SENT已投递',
    MODIFY COLUMN `retry_count` INT NOT NULL DEFAULT 0
        COMMENT '投递失败后的重试次数',
    MODIFY COLUMN `last_error` VARCHAR(1000) NULL
        COMMENT '最近一次投递失败原因，成功后清空',
    MODIFY COLUMN `created_at` DATETIME(3) NOT NULL
        COMMENT 'Outbox事件创建时间',
    MODIFY COLUMN `sent_at` DATETIME(3) NULL
        COMMENT '成功投递时间，尚未投递为空';

ALTER TABLE `order_payment`.`op_payment`
    MODIFY COLUMN `payment_no` VARCHAR(48) NOT NULL
        COMMENT '平台支付单号，支付流水主键',
    MODIFY COLUMN `request_id` VARCHAR(96) NOT NULL
        COMMENT '支付请求幂等键，同一请求只能创建一条流水',
    MODIFY COLUMN `order_no` VARCHAR(40) NOT NULL COMMENT '关联订单号',
    MODIFY COLUMN `amount` DECIMAL(12, 2) NOT NULL COMMENT '本次支付金额',
    MODIFY COLUMN `channel` VARCHAR(24) NOT NULL
        COMMENT '支付渠道，如ALIPAY或WECHAT',
    MODIFY COLUMN `status` VARCHAR(24) NOT NULL
        COMMENT '支付状态：PROCESSING/SUCCESS/FAILED/UNKNOWN',
    MODIFY COLUMN `channel_trade_no` VARCHAR(96) NULL
        COMMENT '支付渠道返回的第三方交易号',
    MODIFY COLUMN `fail_reason` VARCHAR(512) NULL
        COMMENT '渠道明确失败时的原因',
    MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0
        COMMENT '乐观锁版本号，状态变更时递增',
    MODIFY COLUMN `created_at` DATETIME(3) NOT NULL
        COMMENT '支付流水创建时间',
    MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL
        COMMENT '支付流水最后更新时间';

-- test.op_payment 为历史遗留支付表，保留兼容并为字段补充与正式支付表一致的说明。
ALTER TABLE `test`.`op_payment`
    MODIFY COLUMN `payment_no` VARCHAR(48) NOT NULL
        COMMENT '平台支付单号，支付流水主键',
    MODIFY COLUMN `request_id` VARCHAR(96) NOT NULL
        COMMENT '支付请求幂等键，同一请求只能创建一条流水',
    MODIFY COLUMN `order_no` VARCHAR(40) NOT NULL COMMENT '关联订单号',
    MODIFY COLUMN `amount` DECIMAL(12, 2) NOT NULL COMMENT '本次支付金额',
    MODIFY COLUMN `channel` VARCHAR(24) NOT NULL
        COMMENT '支付渠道，如ALIPAY或WECHAT',
    MODIFY COLUMN `status` VARCHAR(24) NOT NULL
        COMMENT '支付状态：PROCESSING/SUCCESS/FAILED/UNKNOWN',
    MODIFY COLUMN `channel_trade_no` VARCHAR(96) NULL
        COMMENT '支付渠道返回的第三方交易号',
    MODIFY COLUMN `fail_reason` VARCHAR(512) NULL
        COMMENT '渠道明确失败时的原因',
    MODIFY COLUMN `version` BIGINT NOT NULL DEFAULT 0
        COMMENT '乐观锁版本号，状态变更时递增',
    MODIFY COLUMN `created_at` DATETIME(3) NOT NULL
        COMMENT '支付流水创建时间',
    MODIFY COLUMN `updated_at` DATETIME(3) NOT NULL
        COMMENT '支付流水最后更新时间';

ALTER TABLE `test`.`user`
    MODIFY COLUMN `id` VARCHAR(100) NOT NULL COMMENT '用户主键',
    MODIFY COLUMN `code` VARCHAR(100) NULL
        COMMENT '用户业务编码或外部账号编号',
    MODIFY COLUMN `name` VARCHAR(100) NULL COMMENT '用户名称',
    MODIFY COLUMN `age` VARCHAR(100) NULL
        COMMENT '用户年龄，现有表使用字符串保存',
    MODIFY COLUMN `address` VARCHAR(100) NULL COMMENT '用户地址';

ALTER TABLE `test`.`student`
    MODIFY COLUMN `id` VARCHAR(100) NOT NULL COMMENT '学生或成员档案主键',
    MODIFY COLUMN `name` VARCHAR(100) NULL COMMENT '学生或成员姓名',
    MODIFY COLUMN `age` INT NULL COMMENT '学生或成员年龄',
    MODIFY COLUMN `beginDate` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '当前档案记录开始时间，默认使用创建时间',
    MODIFY COLUMN `endDate` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '当前档案记录结束时间，随记录更新自动刷新';

-- practice-service 只读取以下外部示例库表，不负责建表。
ALTER TABLE `my_test`.`user`
    MODIFY COLUMN `id` VARCHAR(100) NOT NULL COMMENT '用户主键',
    MODIFY COLUMN `code` VARCHAR(100) NULL COMMENT '用户业务编码',
    MODIFY COLUMN `name` VARCHAR(100) NULL COMMENT '用户名称',
    MODIFY COLUMN `age` VARCHAR(100) NULL
        COMMENT '用户年龄，示例表使用字符串保存',
    MODIFY COLUMN `address` VARCHAR(100) NULL COMMENT '用户地址';

ALTER TABLE `my_test`.`student`
    MODIFY COLUMN `id` VARCHAR(100) NOT NULL COMMENT '学生主键',
    MODIFY COLUMN `name` VARCHAR(100) NULL COMMENT '学生姓名',
    MODIFY COLUMN `age` INT NULL COMMENT '学生年龄',
    MODIFY COLUMN `beginDate` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        COMMENT '记录开始时间，默认使用创建时间',
    MODIFY COLUMN `endDate` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
        COMMENT '记录结束时间，随记录更新自动刷新';
