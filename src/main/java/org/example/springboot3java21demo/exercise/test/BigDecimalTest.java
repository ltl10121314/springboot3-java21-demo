package org.example.springboot3java21demo.exercise.test;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 金额计算
 */
@Slf4j
public class BigDecimalTest {
    public static void main(String[] args) {
        List<Map<String, Object>> list = getMaps();
        List<String> itemCodes = new ArrayList<>();
        itemCodes.add("F_N_1");
        Map<String, Object> result_1;
        Map<String, Object> result_2;
        // 错误代码
        result_1 = itemCodes.stream().filter(BigDecimalTest::itemCodeIsNum).collect(
                Collectors.toMap(
                        key -> key,
                        key -> list.stream().filter(map -> map.get(key) != null)
                                .mapToDouble(map -> Double.parseDouble(map.get(key).toString()))
                                .sum()
                )
        );
        // 正确代码
        result_2 = itemCodes.stream().filter(BigDecimalTest::itemCodeIsNum).collect(
                Collectors.toMap(
                        key -> key,
                        key -> list.stream().filter(map -> map.get(key) != null)
                                .map(map -> (BigDecimal) map.get(key))
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
        );
        log.info(result_1.toString());
        log.info(result_2.toString());
    }

    private static List<Map<String, Object>> getMaps() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>(1);
        map1.put("F_N_1", new BigDecimal("101.90000000"));
        list.add(map1);
        Map<String, Object> map2 = new HashMap<>(1);
        map2.put("F_N_1", new BigDecimal("91.90000000"));
        list.add(map2);
        Map<String, Object> map3 = new HashMap<>(1);
        map3.put("F_N_1", new BigDecimal("81.91000000"));
        list.add(map3);
        Map<String, Object> map4 = new HashMap<>(1);
        map4.put("F_N_1", new BigDecimal("77.09000000"));
        list.add(map4);
        Map<String, Object> map5 = new HashMap<>(1);
        map5.put("F_N_1", new BigDecimal("74.68000000"));
        list.add(map5);
        Map<String, Object> map6 = new HashMap<>(1);
        map6.put("F_N_1", new BigDecimal("86.64000000"));
        list.add(map6);
        Map<String, Object> map7 = new HashMap<>(1);
        map7.put("F_N_1", new BigDecimal("91.90000000"));
        list.add(map7);
        Map<String, Object> map8 = new HashMap<>(1);
        map8.put("F_N_1", new BigDecimal("78.40000000"));
        list.add(map8);
        Map<String, Object> map9 = new HashMap<>(1);
        map9.put("F_N_1", new BigDecimal("50.69000000"));
        list.add(map9);
        Map<String, Object> map10 = new HashMap<>(1);
        map10.put("F_N_1", new BigDecimal("74.68000000"));
        list.add(map10);
        Map<String, Object> map11 = new HashMap<>(1);
        map11.put("F_N_1", new BigDecimal("91.90000000"));
        list.add(map11);
        Map<String, Object> map12 = new HashMap<>(1);
        map12.put("F_N_1", new BigDecimal("87.65000000"));
        list.add(map12);
        Map<String, Object> map13 = new HashMap<>(1);
        map13.put("F_N_1", new BigDecimal("48.06000000"));
        list.add(map13);
        Map<String, Object> map14 = new HashMap<>(1);
        map14.put("F_N_1", new BigDecimal("91.90000000"));
        list.add(map14);
        Map<String, Object> map15 = new HashMap<>(1);
        map15.put("F_N_1", new BigDecimal("91.90000000"));
        list.add(map15);
        Map<String, Object> map16 = new HashMap<>(1);
        map16.put("F_N_1", new BigDecimal("91.90000000"));
        list.add(map16);
        Map<String, Object> map17 = new HashMap<>(1);
        map17.put("F_N_1", new BigDecimal("85.81000000"));
        list.add(map17);
        Map<String, Object> map18 = new HashMap<>(1);
        map18.put("F_N_1", new BigDecimal("91.90000000"));
        list.add(map18);
        return list;
    }

    public static boolean itemCodeIsNum(String itemCode) {
        if (itemCode == null) {
            return false;
        }
        return itemCode.contains("f_n_") || itemCode.contains("f_i_") || itemCode.contains("F_N_") || itemCode.contains("F_I_");
    }

}
