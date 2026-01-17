package org.example.springboot3java21demo.exercise.sql;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.sql.SQLException;

public class SqlFormatTest {
    @Test
    public void sqlFormatTest(){
        String sql = "select uuid as oid, ? as report_id, ? as graph_type, ? as tenant_id, current_id, name, groupConcat  from(" +
                " select distinct co.current_id, co.name, substr(eel.event_key,instr(eel.event_key,'_')+1) event_key" +
                " from etl_events_list eel join common_object co on eel.workflwo_key = co.oid and co.is_current = 1 and co.delete_flag = 0" +
                " where eel.events_type = 1 " +
                " and not exists(select 1 from etl_events_list eel1 where eel.event_key = eel1.event_key and eel1.events_type = 0)" +
                " and co.tenant_id = ?" + " and eel.tenant_id = ? " +
                " and eel.delete_flag = 0) event group by current_id, name";
        SqlFormat.sqlFormat(sql);
    }
    /**
     * 使用属性文件的方式来连接数据库
     * 注意点:在配置user的时候不要使用username,在spring中username有特殊的含义,配置的时候
     * 会发现数据库连接抛出异常:Access denied for user 'Administrator'@'localhost'
     * @throws SQLException
     */
    @Test
    public void testdataSource() throws SQLException {
        ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationContext.xml");
        DataSource bean = (DataSource) applicationContext.getBean("dataSource");
        System.out.println(bean.getConnection());
    }

}
