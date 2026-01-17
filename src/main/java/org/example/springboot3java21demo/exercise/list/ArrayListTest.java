package org.example.springboot3java21demo.exercise.list;


import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author Administrator
 */
public class ArrayListTest {
    Logger Logger = LoggerFactory.getLogger(ArrayListTest.class);

    @Test
    public void arrayListTest() {
        ArrayList<String> strings = new ArrayList<String>();
        strings.add("hahah");
        Logger.info(strings.get(0).getClass().getName());
    }

    @Test
    public void mapTest() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("apple", 12);
        map.put("pear", 13);
        map.put("banana", 15);
        Collection<Integer> values = map.values();
        System.out.println(values);
        for (String key : map.keySet()) {
            Integer integer = map.get(key);
            System.out.println(integer);
        }
    }

    @Test
    public void map2Test() {
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        map.put("apple", 12);
        map.put("pear", 13);
        map.put("banana", 15);
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            System.out.println(key + "=" + value);
        }
    }

    @Test
    public void threadTest() {
        Logger.info(Thread.currentThread().getStackTrace()[1].getMethodName());
        String oid = UUID.randomUUID().toString().replace("-", "");
        Logger.info(oid);
        String message = "";
        Logger.info(String.valueOf(StringUtils.isEmpty(message)));
        BigDecimal bigDecimal = new BigDecimal(0);
        Logger.info(bigDecimal.toString());
        ArrayList<Object> objects = new ArrayList<>();
        objects.size();
        String[] aa = {};
        Logger.info(String.valueOf(aa.length));
    }

    @Test
    public void test1() {
        ArrayList<Object> objects = new ArrayList<>();
        objects = new ArrayList<Object>();
        objects.add("sadfa");
        Logger.info(objects.toString());
    }

    public static void main(String[] args) {
        String str = "02";
        String pattern1 = ".*[,-].*";
        String pattern2 = ".*/0*";
        boolean b = Pattern.matches(".*/0*", str) || Pattern.matches(".*[,-].*", str);
        System.out.println(b);
    }

}
