INSERT INTO `test`.`op_inventory`
    (sku_id, sku_name, price, total_stock, reserved_stock, sold_stock)
VALUES
    ('SKU-1001', '机械键盘', 397.00, 1000, 0, 0),
    ('SKU-1002', '降噪耳机', 899.00, 500, 0, 0) AS new
ON DUPLICATE KEY UPDATE
    sku_name = new.sku_name,
    price = new.price;
