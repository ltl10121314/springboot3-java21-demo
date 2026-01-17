package org.example.springboot3java21demo.exercise.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * mysql查询
 */
public class MySQLExample {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            // 加载JDBC驱动
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 连接数据库
            String url = "jdbc:mysql://ip:port/scheme";
            String username = "";
            String password = "";
            conn = DriverManager.getConnection(url, username, password);

            // 执行查询语句
            String query = getSQLStr();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            List<Map<String, String>> list = new ArrayList<>();
            // 处理查询结果
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                String id = rs.getString("id");
                String tenantid = rs.getString("TENANTID");
                String scheme_auth_id = rs.getString("SCHEME_AUTH_ID");
                String wa_scheme_name = rs.getString("WA_SCHEME_NAME");
                String staff_id = rs.getString("STAFF_ID");
                String staff_code = rs.getString("STAFF_CODE");
                String staff_name = rs.getString("STAFF_NAME");
                String staff_job_id = rs.getString("STAFF_JOB_ID");
                String last_flag = rs.getString("LAST_FLAG");
                String begin_date = rs.getString("BEGIN_DATE");
                String end_date = rs.getString("END_DATE");
                map.put("id", id);
                map.put("tenantid", tenantid);
                map.put("scheme_auth_id", scheme_auth_id);
                map.put("wa_scheme_name", wa_scheme_name);
                map.put("staff_id", staff_id);
                map.put("staff_code", staff_code);
                map.put("staff_name", staff_name);
                map.put("staff_job_id", staff_job_id);
                map.put("last_flag", last_flag);
                map.put("begin_date", begin_date);
                map.put("end_date", end_date);
                list.add(map);
            }
            analysis(list);
            // 关闭连接
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analysis(List<Map<String, String>> list) {
        Map<String, List<Map<String, String>>> map = new HashMap<>();
        for (Map<String, String> stringMap : list) {
            String authId = stringMap.get("scheme_auth_id");
            String staffId = stringMap.get("staff_id");
            String key = authId + "_" + staffId;
            if (map.containsKey(key)) {
                map.get(key).add(stringMap);
            } else {
                List<Map<String, String>> listTmp = new ArrayList<>();
                listTmp.add(stringMap);
                map.put(key, listTmp);
            }
        }
        List<String> ids = new ArrayList<>();
        for (String s : map.keySet()) {
            List<Map<String, String>> maps = map.get(s);
            int num = 0;
            for (Map<String, String> stringStringMap : maps) {
                String last_flag = stringStringMap.get("last_flag");
                if ("1".equals(last_flag)) {
                    num++;
                }
            }
            if (num == 0) {
                Map<String, String> map1 = map.get(s).get(0);
                String wa_scheme_name = map1.get("wa_scheme_name");
                String staff_code = map1.get("staff_code");
                String staff_name = map1.get("staff_name");
                s = s + "_" + wa_scheme_name + "_" + staff_code + "_" + staff_name;
                ids.add(s);
            }
        }
        System.out.println(ids);
    }

    private static String getSQLStr() {
        String str = "SELECT\n" +
                "\td.id\n" +
                "\t,d.TENANTID\n" +
                "\t,d.SCHEME_AUTH_ID \n" +
                "\t,d.WA_SCHEME_NAME \n" +
                "\t,d.STAFF_ID \n" +
                "\t,d.STAFF_CODE \n" +
                "\t,d.STAFF_NAME \n" +
                "\t,d.STAFF_JOB_ID \n" +
                "\t,d.LAST_FLAG \n" +
                "\t,d.BEGIN_DATE \n" +
                "\t,d.END_DATE \n" +
                "FROM\n" +
                "\tdiwork_wa.wa_staff_pay_doc d\n" +
                "WHERE\n" +
                "\td.TENANTID = 'mv0gr1z9'\n" +
                "\tAND d.SCHEME_AUTH_ID IN (\n" +
                "\t\tSELECT\n" +
                "\t\t\tSCHEME_AUTH_ID\n" +
                "\t\tFROM\n" +
                "\t\t\tdiwork_wa.wa_staff_pay_doc a\n" +
                "\t\tWHERE\n" +
                "\t\t\ta.TENANTID = 'mv0gr1z9'\n" +
                "\t\tGROUP BY\n" +
                "\t\t\ta.SCHEME_AUTH_ID\n" +
                "\t)\n" +
                "\tAND d.STAFF_ID IN (\n" +
                "\t\tSELECT\n" +
                "\t\t\tDISTINCT staff_id\n" +
                "\t\tFROM\n" +
                "\t\t\t(\n" +
                "\t\t\t\tSELECT\n" +
                "\t\t\t\t\tSTAFF_ID\n" +
                "\t\t\t\tFROM\n" +
                "\t\t\t\t\tdiwork_wa.wa_staff_pay_doc b\n" +
                "\t\t\t\tWHERE\n" +
                "\t\t\t\t\tb.TENANTID = 'mv0gr1z9'\n" +
                "\t\t\t\tGROUP BY\n" +
                "\t\t\t\t\tb.SCHEME_AUTH_ID,b.STAFF_ID\n" +
                "\t\t\t\tHAVING count(b.STAFF_ID) > 1\n" +
                "\t\t\t) c\n" +
                "\t) \n" +
                "\tORDER BY d.SCHEME_AUTH_ID,d.STAFF_ID,d.BEGIN_DATE ;";
        return str;
    }
}