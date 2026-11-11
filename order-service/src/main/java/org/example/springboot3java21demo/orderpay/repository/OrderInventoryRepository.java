package org.example.springboot3java21demo.orderpay.repository;

import org.example.springboot3java21demo.orderpay.domain.InventoryRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class OrderInventoryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OrderInventoryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<InventoryRecord> find(String skuId) {
        String sql = """
                SELECT sku_id, sku_name, price, total_stock, reserved_stock, sold_stock
                  FROM op_inventory
                 WHERE sku_id = :skuId
                """;
        return jdbc.query(sql, new MapSqlParameterSource("skuId", skuId), (rs, rowNum) ->
                new InventoryRecord(
                        rs.getString("sku_id"),
                        rs.getString("sku_name"),
                        rs.getBigDecimal("price"),
                        rs.getLong("total_stock"),
                        rs.getLong("reserved_stock"),
                        rs.getLong("sold_stock")))
                .stream()
                .findFirst();
    }

    /**
     * 单条条件更新完成“判断 + 预扣”，避免先查后改的并发超卖。
     */
    public boolean reserve(String skuId, int quantity) {
        String sql = """
                UPDATE op_inventory
                   SET reserved_stock = reserved_stock + :quantity
                 WHERE sku_id = :skuId
                   AND total_stock - reserved_stock - sold_stock >= :quantity
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("skuId", skuId)
                .addValue("quantity", quantity)) == 1;
    }

    public boolean confirm(String skuId, int quantity) {
        String sql = """
                UPDATE op_inventory
                   SET reserved_stock = reserved_stock - :quantity,
                       sold_stock = sold_stock + :quantity
                 WHERE sku_id = :skuId
                   AND reserved_stock >= :quantity
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("skuId", skuId)
                .addValue("quantity", quantity)) == 1;
    }

    public boolean release(String skuId, int quantity) {
        String sql = """
                UPDATE op_inventory
                   SET reserved_stock = reserved_stock - :quantity
                 WHERE sku_id = :skuId
                   AND reserved_stock >= :quantity
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("skuId", skuId)
                .addValue("quantity", quantity)) == 1;
    }
}
