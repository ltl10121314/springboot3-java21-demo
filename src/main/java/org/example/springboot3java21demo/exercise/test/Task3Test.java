package org.example.springboot3java21demo.exercise.test;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class Task3Test {
    //    public static final Pattern NUMBER_WITH_TRAILING_ZEROS_PATTERN = Pattern.compile("\\.0*");
    public static final Pattern NUMBER_WITH_TRAILING_ZEROS_PATTERN = Pattern.compile("^[GCDZTSPKXLY1-9]\\d{1,4}$");
    public static final Pattern pattern = Pattern.compile("waitem.f_[i,v,d,b,n]_\\d+");

    public static void main(String[] args) {
        String dateStr = new SimpleDateFormat("yyyy-MM").format(new Date());
        Date date = new Date();
        long time = date.getTime();
        String string = String.valueOf(time);
        log.error(string);
        String strVal = "waitem.f_n_1+busi[waitem.f_n_2]";
        log.error(strVal);
        Matcher matcher = pattern.matcher(strVal);

        Set<String> set = new HashSet<>();
        while (matcher.find()) {
            String group = matcher.group();
            set.add(group);
        }
        log.error(set.toString());
    }

    @Test
    public void test4() {
        int[] numbers = {1, 2, 3, 4, 5};
        int index = 10; // 假设这是不断增加的索引
        int arrayIndex = index % numbers.length; // 使用模运算得到循环索引
        System.out.println(numbers[arrayIndex]); // 输出: 5
    }

    @Test
    public void test3() {
        long timestamp = System.currentTimeMillis() / 1000;
        long dataCenterId = 1;
        long workerId = 1;
        long reserved = 1;
        long sequence = 1;
        long id = timestamp - 1483200000L << 33 | dataCenterId << 25 | workerId << 19 | reserved << 18 | sequence;
        System.out.println(id);
        System.out.println((Long.MAX_VALUE >> 33) + 1483200000L);
        System.out.println((Long.MAX_VALUE - 1483200000L >> 33) / 3600 / 24 / 365);
        System.out.println(Long.MAX_VALUE);
    }

    @Test
    public void test2() {
        Deque<String> linkedList = new LinkedList<>();
        linkedList.addFirst("1");
        linkedList.push("2");
        linkedList.addLast("3");
        log.error(linkedList.toString());
        log.error(linkedList.peek());
        log.error(linkedList.toString());
        log.error(linkedList.pop());
        linkedList.push("4");
        log.error(linkedList.toString());
        log.error(linkedList.peek());
        log.error(linkedList.toString());
        linkedList.offer("5");
        log.error(linkedList.toString());
        log.error(linkedList.poll());
        log.error(linkedList.toString());
        String[] ss = {};
        int length = ss.length;
        System.out.println(length);
    }

    @Test
    public void test1() {
        Set<String> tableNames = new HashSet<>();
        tableNames.add("tableName1");
        tableNames.add("tableName");
        tableNames.add("tableName2");
        tableNames.add("tableName3");
        String tableName = "tableName";
        tableNames.remove(tableName);
        log.error(tableNames.toString());
        dealProduceJoinTableByTableNames(tableNames, "tableName", "wpd");
    }

    public static String dealProduceJoinTableByTableNames(Set<String> tableNames, String mulDbTableName, String aliasDetail) {
        StringBuffer joinTable = new StringBuffer();
        joinTable.append(mulDbTableName).append(" ").append(aliasDetail);
        int tableNum = 1;
        if (CollectionUtils.isEmpty(tableNames)) {
            tableNames.add(mulDbTableName);
        }
        for (String tableName : tableNames) {
            String aliasOther = aliasDetail + tableNum;
            joinTable.append(" LEFT JOIN").append(" ").append(tableName).append(" ").append(aliasOther).append(" ON ")
                    .append(aliasDetail).append(".").append("PK_PAYFILE = ").append(aliasOther).append(".").append("PK_PAYFILE").append(" AND ")
                    .append(aliasDetail).append(".").append("PK_PAYFILE_DOC = ").append(aliasOther).append(".").append("PK_PAYFILE_DOC").append(" AND ")
                    .append(aliasDetail).append(".").append("ytenant_id = ").append(aliasOther).append(".").append("ytenant_id");
            tableNum++;
        }
        log.error("dealProduceJoinOnlyDetailTable  sql=====:\n{}", joinTable);
        return joinTable.toString();
    }
}
