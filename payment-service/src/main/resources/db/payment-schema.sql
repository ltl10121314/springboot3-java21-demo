-- 支付服务数据库结构。字段注释会保存在 MySQL information_schema.COLUMNS 中。
CREATE TABLE IF NOT EXISTS op_payment (
    payment_no       VARCHAR(48)    NOT NULL COMMENT '平台支付单号，支付流水主键',
    request_id       VARCHAR(96)    NOT NULL COMMENT '支付请求幂等键，同一请求只能创建一条流水',
    order_no         VARCHAR(40)    NOT NULL COMMENT '关联订单号',
    amount           DECIMAL(12, 2) NOT NULL COMMENT '本次支付金额',
    channel          VARCHAR(24)    NOT NULL COMMENT '支付渠道，如ALIPAY或WECHAT',
    status           VARCHAR(24)    NOT NULL COMMENT '支付状态：PROCESSING/SUCCESS/FAILED/UNKNOWN',
    channel_trade_no VARCHAR(96)             NULL COMMENT '支付渠道返回的第三方交易号',
    fail_reason      VARCHAR(512)            NULL COMMENT '渠道明确失败时的原因',
    version          BIGINT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号，状态变更时递增',
    created_at       DATETIME(3)    NOT NULL COMMENT '支付流水创建时间',
    updated_at       DATETIME(3)    NOT NULL COMMENT '支付流水最后更新时间',
    PRIMARY KEY (payment_no),
    UNIQUE KEY uk_op_payment_request (request_id),
    KEY idx_op_payment_order (order_no, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
