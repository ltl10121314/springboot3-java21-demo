package org.example.springboot3java21demo.exercise.map;

import org.junit.Test;

import java.time.DayOfWeek;
import java.util.EnumMap;
import java.util.Map;

/**
 * 
 * @date 2020/7/2 0:34
 * @description
 */
public class MapTest {
    @Test
    public void mapTest() {
        Map<DayOfWeek, String> map = new EnumMap<>(DayOfWeek.class);
        map.put(DayOfWeek.MONDAY, "星期一");
        map.put(DayOfWeek.TUESDAY, "星期二");
        map.put(DayOfWeek.WEDNESDAY, "星期三");
        map.put(DayOfWeek.THURSDAY, "星期四");
        map.put(DayOfWeek.FRIDAY, "星期五");
        map.put(DayOfWeek.SATURDAY, "星期六");
        map.put(DayOfWeek.SUNDAY, "星期日");
        System.out.println(map);
        System.out.println(map.get(DayOfWeek.MONDAY));
    }
}
