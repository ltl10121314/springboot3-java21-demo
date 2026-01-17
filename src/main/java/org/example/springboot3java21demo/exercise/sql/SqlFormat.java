package org.example.springboot3java21demo.exercise.sql;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * @author Administrator
 */
public class SqlFormat {
    public static void main(String[] args) {
        String sql = "select etl_calc_resource.Queue_Name as resourceQueueName from etl_calc_resource " +
                " where id = '" + "' " +
                " and tenant_id = '" + "'";
        sqlFormat(sql);
    }

    /**
     * 需要换行的字段
     */
    private static final Map<String, String> map = new HashMap<String, String>();
    /**
     * 括号前关键字
     */
    private static final Map<String, String> bracket = new HashMap<String, String>();

    static {
        map.put("select", "select");
        map.put("from", "from");
        map.put("group by", "group by");
        map.put("where", "where");
        map.put("order by", "order by");

        bracket.put("not", "not");

        map.put("(", "(");
        map.put(")", ")");

    }

    static String sqlFormat(String sql) {
        sql = sql.trim().replaceAll(",", ", ").replaceAll(" +", " ").replaceAll("\\s+", " ").replace(" ", " |").replace("(", "|(|").replace(")", "|)|").replace("| |", " |").replace("||", "|");
        sql = sql.toLowerCase();
        int sj = 0;
        String[] sqlArray = sql.split("\\|");
        int length = sqlArray.length;
        Stack stack = new Stack();
        Stack stackTemp = new Stack();

        for (int i = 0; i < length; i++) {
            if ("(".equals(sqlArray[i].trim())) {
                //后有关键字
                if (map.get(sqlArray[i + 1].trim()) != null) {
                    stack.push("(");
                    printlnSql(sqlArray[i], sj);
                    //前有关键字
                } else if (bracket.get(sqlArray[i - 1].trim()) != null) {
                    stack.push("(");
                    printlnSql(sqlArray[i], sj);
                } else {
                    stackTemp.push("(");
                    System.out.println(sqlArray[i]);
                }
            } else if (")".equals(sqlArray[i].trim())) {
                if (stackTemp.size() > 0) {
                    stackTemp.pop();
                    System.out.println(sqlArray[i]);
                } else {
                    stackTemp.pop();
                    sj = stack.size();
                    printlnSql(sqlArray[i], sj);
                }
            } else if ("".equals(sqlArray[i].trim())) {

            } else if ("(".equals(sqlArray[i].trim()) && map.get(sqlArray[i - 1].trim()) != null) {

            } else if (map.get(sqlArray[i].trim()) != null) {
                printlnSql(sqlArray[i], sj);
            } else {
                System.out.print(sqlArray[i]);
            }
        }
        return sql;
    }

    private static void printlnSql(String sqlSub, int i) {
        if (i > 0) {
            System.out.println("\n");
            for (int j = 0; j < i; j++) {
                System.out.println("\t\t");
            }
        } else {
            System.out.println('\n' + sqlSub);
        }
    }
}