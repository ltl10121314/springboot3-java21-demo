package org.example.springboot3java21demo.exercise.sql;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mysql查询
 */
@Slf4j
public class MySQLExample {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            // 加载JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            // 连接数据库
            String url = "jdbc:mysql://localhost:3306/my_test";
            String username = "root";
            String password = "ltl10121314";
            conn = DriverManager.getConnection(url, username, password);
            // 执行查询语句
            String query = "select * from student;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            // 处理查询结果
            List<Map<String, Object>> list = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                String id = rs.getString("id");
                String name = rs.getString("name");
                Integer age = rs.getInt("age");
                Date beginDate = rs.getDate("beginDate");
                Date endDate = rs.getDate("endDate");
                map.put("id", id);
                map.put("name", name);
                map.put("age", age);
                map.put("beginDate", beginDate);
                map.put("endDate", endDate);
                list.add(map);
            }
            log.info("list: {}", list);
            // 关闭连接
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}