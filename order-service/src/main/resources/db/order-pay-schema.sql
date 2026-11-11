-- 订单服务数据库结构。所有字段注释会保存在 MySQL information_schema.COLUMNS 中。
CREATE TABLE IF NOT EXISTS op_inventory (
    sku_id          VARCHAR(64)    NOT NULL COMMENT '商品SKU标识，主键',
    sku_name        VARCHAR(128)   NOT NULL COMMENT '商品名称',
    price           DECIMAL(12, 2) NOT NULL COMMENT '商品当前销售单价',
    total_stock     BIGINT         NOT NULL COMMENT '总库存，须不小于预占库存与已售库存之和',
    reserved_stock  BIGINT         NOT NULL DEFAULT 0 COMMENT '已下单但尚未支付成功的预占库存',
    sold_stock      BIGINT         NOT NULL DEFAULT 0 COMMENT '支付成功后确认售出的库存',
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                   ON UPDATE CURRENT_TIMESTAMP(3)
                                   COMMENT '库存记录最后更新时间',
    PRIMARY KEY (sku_id),
    CONSTRAINT chk_op_inventory_stock
        CHECK (total_stock >= 0 AND reserved_stock >= 0 AND sold_stock >= 0
               AND reserved_stock + sold_stock <= total_stock)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS op_order (
    order_no       VARCHAR(40)    NOT NULL COMMENT '订单业务主键',
    request_id     VARCHAR(96)    NOT NULL COMMENT '创建订单幂等键',
    user_id        VARCHAR(64)    NOT NULL COMMENT '下单用户标识',
    sku_id         VARCHAR(64)    NOT NULL COMMENT '商品SKU标识',
    sku_name       VARCHAR(128)   NOT NULL COMMENT '下单时的商品名称快照',
    unit_price     DECIMAL(12, 2) NOT NULL COMMENT '下单时的商品单价快照',
    quantity       INT            NOT NULL COMMENT '购买数量',
    total_amount   DECIMAL(12, 2) NOT NULL COMMENT '订单总金额，等于单价乘数量',
    status         VARCHAR(24)    NOT NULL COMMENT '订单状态：CREATED/PAYING/PAID/CANCELLED/CLOSED',
    payment_no     VARCHAR(48)             NULL COMMENT '当前支付尝试的支付单号，未支付为空',
    version        BIGINT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，每次状态变更递增',
    created_at     DATETIME(3)    NOT NULL COMMENT '订单创建时间',
    updated_at     DATETIME(3)    NOT NULL COMMENT '订单最后更新时间',
    paid_at        DATETIME(3)             NULL COMMENT '支付成功时间，未支付为空',
    PRIMARY KEY (order_no),
    UNIQUE KEY uk_op_order_request (request_id),
    KEY idx_op_order_user_created (user_id, created_at),
    KEY idx_op_order_status_updated (status, updated_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS op_outbox_event (
    id             BIGINT         NOT NULL AUTO_INCREMENT COMMENT '数据库自增主键',
    event_id       VARCHAR(48)    NOT NULL COMMENT '事件业务唯一标识，用于消费端幂等',
    aggregate_id   VARCHAR(64)    NOT NULL COMMENT '关联聚合标识，订单事件通常为订单号',
    event_type     VARCHAR(40)    NOT NULL COMMENT '业务事件类型，如ORDER_CREATED',
    destination    VARCHAR(16)    NOT NULL COMMENT '投递目标：KAFKA或ROCKETMQ',
    payload        LONGTEXT       NOT NULL COMMENT '事件JSON载荷，包含关联TraceId',
    available_at   DATETIME(3)    NOT NULL COMMENT 'Outbox最早可再次投递时间',
    deliver_at     DATETIME(3)    NOT NULL COMMENT '业务期望投递时间，RocketMQ延迟关单使用',
    status         VARCHAR(16)    NOT NULL COMMENT '投递状态：PENDING待投递，SENT已投递',
    retry_count    INT            NOT NULL DEFAULT 0 COMMENT '投递失败后的重试次数',
    last_error     VARCHAR(1000)           NULL COMMENT '最近一次投递失败原因，成功后清空',
    created_at     DATETIME(3)    NOT NULL COMMENT 'Outbox事件创建时间',
    sent_at        DATETIME(3)             NULL COMMENT '成功投递时间，尚未投递为空',
    PRIMARY KEY (id),
    UNIQUE KEY uk_op_outbox_event (event_id),
    KEY idx_op_outbox_due (status, available_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
