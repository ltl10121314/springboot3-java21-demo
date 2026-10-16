package org.example.springboot3java21demo.payment.domain;

/**
 * 支付单关联的库存行。为空表示该笔支付不涉及库存（纯资金流转）。
 *
 * @param skuId    商品/SKU 编号
 * @param quantity 购买数量，必须大于 0
 */
public record StockLine(String skuId, int quantity) {

    public StockLine {
        if (skuId == null || skuId.isBlank()) {
            throw new IllegalArgumentException("skuId 不能为空");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity 必须大于 0");
        }
    }

    public static StockLine of(String skuId, Integer quantity) {
        if (skuId == null || skuId.isBlank()) {
            return null;
        }
        return new StockLine(skuId, quantity == null ? 1 : quantity);
    }
}
