package org.example.springboot3java21demo.orderpay.repository;

import org.example.springboot3java21demo.orderpay.domain.InventoryRecord;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderInventoryRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public OrderInventoryRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<InventoryRecord> find(String skuId) {
        String sql = """
                SELECT sku_id, sku_name, price, total_stock, reserved_stock, sold_stock, updated_at
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
                        rs.getLong("sold_stock"),
                        rs.getTimestamp("updated_at").toInstant()))
                .stream()
                .findFirst();
    }

    public List<InventoryRecord> findPage(String keyword, int limit, int offset) {
        String sql = """
                SELECT sku_id, sku_name, price, total_stock, reserved_stock, sold_stock, updated_at
                  FROM op_inventory
                 WHERE sku_id LIKE :keyword OR sku_name LIKE :keyword
                 ORDER BY updated_at DESC, sku_id ASC
                 LIMIT :limit OFFSET :offset
                """;
        return jdbc.query(sql, new MapSqlParameterSource()
                .addValue("keyword", likePattern(keyword))
                .addValue("limit", limit)
                .addValue("offset", offset), (rs, rowNum) ->
                new InventoryRecord(
                        rs.getString("sku_id"),
                        rs.getString("sku_name"),
                        rs.getBigDecimal("price"),
                        rs.getLong("total_stock"),
                        rs.getLong("reserved_stock"),
                        rs.getLong("sold_stock"),
                        rs.getTimestamp("updated_at").toInstant()));
    }

    public long count(String keyword) {
        String sql = """
                SELECT COUNT(*)
                  FROM op_inventory
                 WHERE sku_id LIKE :keyword OR sku_name LIKE :keyword
                """;
        Long count = jdbc.queryForObject(
                sql, new MapSqlParameterSource("keyword", likePattern(keyword)), Long.class);
        return count == null ? 0 : count;
    }

    public void insert(InventoryRecord inventory) {
        String sql = """
                INSERT INTO op_inventory
                    (sku_id, sku_name, price, total_stock, reserved_stock, sold_stock)
                VALUES
                    (:skuId, :skuName, :price, :totalStock, 0, 0)
                """;
        jdbc.update(sql, new MapSqlParameterSource()
                .addValue("skuId", inventory.skuId())
                .addValue("skuName", inventory.skuName())
                .addValue("price", inventory.price())
                .addValue("totalStock", inventory.totalStock()));
    }

    public boolean update(String skuId, String skuName, java.math.BigDecimal price, long totalStock) {
        String sql = """
                UPDATE op_inventory
                   SET sku_name = :skuName,
                       price = :price,
                       total_stock = :totalStock
                 WHERE sku_id = :skuId
                   AND :totalStock >= reserved_stock + sold_stock
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("skuId", skuId)
                .addValue("skuName", skuName)
                .addValue("price", price)
                .addValue("totalStock", totalStock)) == 1;
    }

    public boolean updateTotalStock(String skuId, long totalStock) {
        String sql = """
                UPDATE op_inventory
                   SET total_stock = :totalStock
                 WHERE sku_id = :skuId
                   AND :totalStock >= reserved_stock + sold_stock
                """;
        return jdbc.update(sql, new MapSqlParameterSource()
                .addValue("skuId", skuId)
                .addValue("totalStock", totalStock)) == 1;
    }

    public boolean deleteIfUnused(String skuId) {
        String sql = """
                DELETE FROM op_inventory
                 WHERE sku_id = :skuId
                   AND reserved_stock = 0
                   AND sold_stock = 0
                """;
        return jdbc.update(sql, new MapSqlParameterSource("skuId", skuId)) == 1;
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

    private String likePattern(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();
        return "%" + normalized + "%";
    }
}
