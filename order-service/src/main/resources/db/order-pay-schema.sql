CREATE TABLE IF NOT EXISTS op_inventory (
    sku_id          VARCHAR(64)    NOT NULL,
    sku_name        VARCHAR(128)   NOT NULL,
    price           DECIMAL(12, 2) NOT NULL,
    total_stock     BIGINT         NOT NULL,
    reserved_stock  BIGINT         NOT NULL DEFAULT 0,
    sold_stock      BIGINT         NOT NULL DEFAULT 0,
    updated_at      DATETIME(3)    NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
                                   ON UPDATE CURRENT_TIMESTAMP(3),
    PRIMARY KEY (sku_id),
    CONSTRAINT chk_op_inventory_stock
        CHECK (total_stock >= 0 AND reserved_stock >= 0 AND sold_stock >= 0
               AND reserved_stock + sold_stock <= total_stock)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS op_order (
    order_no       VARCHAR(40)    NOT NULL,
    request_id     VARCHAR(96)    NOT NULL,
    user_id        VARCHAR(64)    NOT NULL,
    sku_id         VARCHAR(64)    NOT NULL,
    sku_name       VARCHAR(128)   NOT NULL,
    unit_price     DECIMAL(12, 2) NOT NULL,
    quantity       INT            NOT NULL,
    total_amount   DECIMAL(12, 2) NOT NULL,
    status         VARCHAR(24)    NOT NULL,
    payment_no     VARCHAR(48)             NULL,
    version        BIGINT         NOT NULL DEFAULT 0,
    created_at     DATETIME(3)    NOT NULL,
    updated_at     DATETIME(3)    NOT NULL,
    paid_at        DATETIME(3)             NULL,
    PRIMARY KEY (order_no),
    UNIQUE KEY uk_op_order_request (request_id),
    KEY idx_op_order_user_created (user_id, created_at),
    KEY idx_op_order_status_updated (status, updated_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS op_outbox_event (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    event_id       VARCHAR(48)  NOT NULL,
    aggregate_id   VARCHAR(64)  NOT NULL,
    event_type     VARCHAR(40)  NOT NULL,
    destination    VARCHAR(16)  NOT NULL,
    payload        LONGTEXT     NOT NULL,
    available_at   DATETIME(3)  NOT NULL,
    deliver_at     DATETIME(3)  NOT NULL,
    status         VARCHAR(16)  NOT NULL,
    retry_count    INT          NOT NULL DEFAULT 0,
    last_error     VARCHAR(1000)         NULL,
    created_at     DATETIME(3)  NOT NULL,
    sent_at        DATETIME(3)           NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_op_outbox_event (event_id),
    KEY idx_op_outbox_due (status, available_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;
