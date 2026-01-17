package org.example.springboot3java21demo.exercise.test;

import org.example.springboot3java21demo.exercise.domain.User;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 
 */
@Slf4j
public class Task1Test {
    private static final Logger LOGGER = LoggerFactory.getLogger(Task1Test.class);

    private static NavigableMap<Integer, Integer> rankingMap = Maps.newTreeMap();
    private static Map<String, User> map = new HashMap<>();
    private static Map<String, User> map3 = new HashMap<>();
    private static Map<String, String> map2 = new HashMap<>();


    static {
        rankingMap.put(1, 1);  //ranking 1 ->1
        rankingMap.put(2, 2); //ranking 2 ->2
        rankingMap.put(3, 3); //ranking 3 -> 3
        rankingMap.put(4, 4); //ranking 4-10 ->4
        rankingMap.put(11, 5); //ranking 11-20 ->5
        rankingMap.put(21, 6); //ranking 21-30 ->6
        rankingMap.put(31, 7); //ranking >= 31 ->7
        User user1 = new User();
        user1.setId("1");
        user1.setName("张三");
        User user2 = new User();
        user2.setId("2");
        user2.setName("王麻子");
        user1.setChildren(user2);
        map.put("1", user1);
        User user3 = new User();
        user3.setId("3");
        user3.setName("李四");
        User user4 = new User();
        user4.setId("4");
        user4.setName("王五");
        user3.setChildren(user4);
        User user5 = new User();
        user5.setId("5");
        user5.setName("李六");
        user4.setChildren(user5);
        map.put("3", user3);
        map3.put("1", user1);
        map3.put("2", user2);
        map3.put("3", user3);
        map3.put("4", user4);
        map3.put("5", user5);
    }

    public static void main(String[] args) {
        long a =1732982400000L;
        String dateStr = new SimpleDateFormat("yyyy-MM").format(new Date((Long) a));
        System.out.println(dateStr);

        String time = "2024-09-09";
        System.out.println(time.substring(0,7)+"-01");
        String aa = "";
        System.out.println(aa instanceof String);
        String[] columns = new String[]{"a","b"};
        for (String column : columns) {
            System.out.println(column);
        }
        Set<String> tableNames = new HashSet<>();
        tableNames.add("aa");
        tableNames.add("bb");
        dealProduceJoinTableByTableNames(tableNames, "cc", "c");
    }

    public static String dealProduceJoinTableByTableNames(Set<String> tableNames,String mulDbTableName,String aliasDetail) {
        StringBuffer joinTable = new StringBuffer();
        int tableNum =1;
        if(CollectionUtils.isEmpty(tableNames)){
            tableNames.add(mulDbTableName);
        }
        for(String tableName:tableNames){
            if(tableNum==1){
                joinTable.append(" "+tableName+" "+aliasDetail);
            }else{
                String aliasOther = aliasDetail+tableNum;
                joinTable.append(" left join").append(" "+tableName).append(" "+aliasOther).append(" ON ").append(aliasDetail+".").append("ID = ").append(aliasOther+".").append("ID");
            }
            tableNum++;
        }
        log.error("dealProduceJoinOnlyDetailTable  sql=====\n"+joinTable.toString());
        return joinTable.toString();
    }

    @Test
    public void test1() {
        BigDecimal v1 = new BigDecimal("1");
        BigDecimal v2 = new BigDecimal(v1.toString());
        v1 = v1.subtract(new BigDecimal("1"));
        System.out.println(v1);
        System.out.println(v2);
    }

    private void calculateDayValue(Map<String, BigDecimal> map, BigDecimal upperLimit, BigDecimal notOnDuty, boolean flag) {
        if (map == null || map.isEmpty()) {
            return;
        }
        // 如果只有一段数据则直接判断是否超过边界
        if (map.keySet().size() == 1) {
            for (Map.Entry<String, BigDecimal> entry : map.entrySet()) {
                String key = entry.getKey();
                BigDecimal value = entry.getValue();
                if (!flag) {
                    if (notOnDuty != null && BigDecimal.ZERO.compareTo(notOnDuty) < 0) {
                        if (upperLimit.compareTo(value) < 0) {
                            map.put(key, upperLimit.subtract(notOnDuty));
                        }
                    } else {
                        map.put(key, upperLimit);
                    }

                } else {
                    // 超过上限进行处理
                    if (upperLimit.compareTo(value) < 0) {
                        // 不在岗天数不为空
                        if (notOnDuty != null && BigDecimal.ZERO.compareTo(notOnDuty) < 0) {
                            // 不在岗天数超过上限则正算,否则反算
                            if (upperLimit.compareTo(notOnDuty) < 0) {
                                map.put(key, upperLimit.subtract(value));
                            } else {
                                map.put(key, upperLimit.subtract(notOnDuty));
                            }
                        } else {
                            map.put(key, upperLimit);
                        }
                    }
                }
            }
            return;
        }
        List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        System.out.println(list);
        BigDecimal addValue = BigDecimal.ZERO;
        BigDecimal subtractValue = BigDecimal.ZERO;
        // 分段数据超过两段
        if (list.size() > 1) {
            if (!flag) {
                // 反算
                for (int i = list.size() - 2; i >= 0; i--) {
                    String currentKey = list.get(i);
                    String beforeKey = list.get(i + 1);
                    BigDecimal beforeValue = map.get(beforeKey);
                    // 正算
                    if (upperLimit.compareTo(addValue.add(beforeValue)) < 0) {
                        for (int j = 1; j <= i + 1; j++) {
                            beforeKey = list.get(j - 1);
                            currentKey = list.get(j);
                            addValue = addValue.add(map.get(beforeKey));
                            subtractValue = upperLimit.subtract(addValue);
                            if (j == i + 1) {
                                map.put(currentKey, subtractValue);
                            }
                        }
                        break;
                    } else {
                        addValue = addValue.add(beforeValue);
                        subtractValue = upperLimit.subtract(addValue);
                    }
                    if (i == 0) {
                        map.put(currentKey, subtractValue);
                    }
                }
            } else {
                // 正算
                for (int i = 0; i < list.size(); i++) {
                    String currentKey = list.get(i);
                    BigDecimal currentValue = map.get(currentKey);
                    // 反算
                    if (upperLimit.compareTo(addValue.add(currentValue)) < 0) {
                        subtractValue = upperLimit.subtract(notOnDuty);
                        for (int j = list.size() - 2; j >= 0; j--) {
                            String beforeKey = list.get(j + 1);
                            BigDecimal beforeValue = map.get(beforeKey);
                            subtractValue = subtractValue.subtract(beforeValue);
                            if (j == 0) {
                                currentKey = list.get(j);
                                map.put(currentKey, subtractValue);
                            }
                        }
                        break;
                    }
                    addValue = addValue.add(currentValue);
                }
            }
        }
    }

    @Test
    public void test7() throws Exception {
        String begin = "2023-12-21";
        String end = "2022-05-02";
        SimpleDateFormat dft = new SimpleDateFormat("yyyy-MM-dd");
        Date date1 = dft.parse(begin);
        Date date2 = dft.parse(end);
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar1.setTime(date1);
        calendar2.setTime(date2);
        long l = (calendar1.get(Calendar.YEAR) - calendar2.get(Calendar.YEAR));
        System.out.println(l);
        BigDecimal a = new BigDecimal("19");
        BigDecimal b = new BigDecimal("22");
        BigDecimal divide = a.divide(b, 9, BigDecimal.ROUND_HALF_UP);
        System.out.println(divide);
        Map<String, BigDecimal> numeratorMap = Collections.emptyMap();
        BigDecimal bigDecimal = numeratorMap.get("a");
        System.out.println(bigDecimal);
    }
}
