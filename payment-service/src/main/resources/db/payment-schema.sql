CREATE TABLE IF NOT EXISTS op_payment (
    payment_no       VARCHAR(48)    NOT NULL,
    request_id       VARCHAR(96)    NOT NULL,
    order_no         VARCHAR(40)    NOT NULL,
    amount           DECIMAL(12, 2) NOT NULL,
    channel          VARCHAR(24)    NOT NULL,
    status           VARCHAR(24)    NOT NULL,
    channel_trade_no VARCHAR(96)             NULL,
    fail_reason      VARCHAR(512)             NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    created_at       DATETIME(3)    NOT NULL,
    updated_at       DATETIME(3)    NOT NULL,
    PRIMARY KEY (payment_no),
    UNIQUE KEY uk_op_payment_request (request_id),
    KEY idx_op_payment_order (order_no, created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
