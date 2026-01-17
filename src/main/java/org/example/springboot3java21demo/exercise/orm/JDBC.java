package org.example.springboot3java21demo.exercise.orm;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class JDBC {
    private static final String URL = "jdbc:mysql://localhost:3306/my_test?useSSL=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "ltl10121314";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement statement = conn.createStatement();
//            String sql = "INSERT INTO wa_group_doc\n" +
//                    "(ID, CODE, NAME, NAME2, NAME3, NAME4, NAME5, NAME6, ENABLE, CREATOR, CREATIONTIME, MODIFIER, MODIFIEDTIME, TS, DESCRIPTION, DESCRIPTION2, DESCRIPTION3, DESCRIPTION4, DESCRIPTION5, DESCRIPTION6, ORGID, TENANTID, YTENANT_ID)\n" +
//                    "VALUES('d39cdcdfc69e4357a5b5eadd26a88a48', 'wageGroup5', '薪资组11', 'wegeGroup3', '薪资组', NULL, NULL, NULL, 1, '96b61a89-f6a7-4a7e-b449-2f566fb82caa', '2022-04-18 01:20:04', NULL, NULL, '2022-04-18 01:20:04', '薪资组', 'wegeGroup', '薪资组', NULL, NULL, NULL, 'AA', '0000L0GCJTKI6JSSV00000', '0000L0GCJTKI6JSSV00000');\n";
            //把表名称包起来,这样就能实现数据的插入更新了
            String sql = "UPDATE my_test.wa_group_doc\n" +
                    "SET ID='d39cdcdfc69e4357a5b5eadd26a88a38', CODE='wageGroup5', NAME='薪资组11', NAME2='wegeGroup3', NAME3='薪资组', NAME4=NULL, NAME5=NULL, NAME6=NULL, ENABLE=1, CREATOR='96b61a89-f6a7-4a7e-b449-2f566fb82caa', CREATIONTIME='2022-04-18 01:20:04', MODIFIER=NULL, MODIFIEDTIME=NULL, TS='2022-04-18 01:20:04', DESCRIPTION='薪资组', DESCRIPTION2='wegeGroup', DESCRIPTION3='薪资组', DESCRIPTION4=NULL, DESCRIPTION5=NULL, DESCRIPTION6=NULL, ORGID='AA', TENANTID='0000L0GCJTKI6JSSV00000', YTENANT_ID='0000L0GCJTKI6JSSV00000';\n";
            statement.executeUpdate(sql);
            statement.close();
            conn.close();
        } catch (SQLException e) {
            if (StringUtils.indexOfIgnoreCase(e.getMessage(), "wa_group_doc.code") != -1) {
                log.error("编码重复");
            } else {

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
