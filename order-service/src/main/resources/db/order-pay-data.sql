-- 库存初始化数据。
-- 字段顺序：SKU标识、商品名称、销售单价、总库存、预占库存、已售库存。
-- 重复执行时只刷新商品名称和价格，不覆盖现有预占库存与已售库存。
INSERT INTO `test`.`op_inventory`
    (sku_id, sku_name, price, total_stock, reserved_stock, sold_stock)
VALUES
    ('SKU-1001', '机械键盘', 397.00, 1000, 0, 0),
    ('SKU-1002', '降噪耳机', 899.00, 500, 0, 0) AS new
ON DUPLICATE KEY UPDATE
    sku_name = new.sku_name,
    price = new.price;
