package org.example.practice.integration.database.jdbc;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;

@Slf4j
public class JDBC {
    private static final String URL = "jdbc:mysql://localhost:3306/my_test?useSSL=true";
    private static final String USERNAME = "dev";
    private static final String PASSWORD = "123456";

    public static void main(String[] args) {
        try {
            // Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
            String sql = "select * from my_test.user";
            ResultSet resultSet = statement.executeQuery(sql);
            JDBC.printResultSet(resultSet);
            resultSet.close();
            statement.close();
            conn.close();
        } catch (Exception e) {
            log.error("执行 SQL 失败", e);
        }

    }

    /**
     * 打印 ResultSet 的完整数据：列名、列类型、每行数据、总行数。
     *
     * @param resultSet 查询结果集
     * @throws SQLException 读取结果集失败
     */
    private static void printResultSet(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 打印列名和数据库类型，方便对照表结构
        StringBuilder columns = new StringBuilder();
        for (int i = 1; i <= columnCount; i++) {
            if (i > 1) {
                columns.append(" | ");
            }
            columns.append(metaData.getColumnLabel(i))
                    .append("(")
                    .append(metaData.getColumnTypeName(i))
                    .append(")");
        }
        log.info("查询结果列: {}", columns);

        // 逐行打印，NULL 单独标记，避免和字符串 "null" 混淆
        int rowCount = 0;
        while (resultSet.next()) {
            rowCount++;
            StringBuilder row = new StringBuilder();
            for (int i = 1; i <= columnCount; i++) {
                if (i > 1) {
                    row.append(" | ");
                }
                Object value = resultSet.getObject(i);
                row.append(value == null ? "NULL" : value);
            }
            log.info("第 {} 行: {}", rowCount, row);
        }

        log.info("查询结果共 {} 行", rowCount);
    }
}
