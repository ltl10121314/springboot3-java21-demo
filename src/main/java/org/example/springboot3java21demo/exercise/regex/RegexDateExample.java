package org.example.springboot3java21demo.exercise.regex;

import java.util.regex.Pattern;

public class RegexDateExample {
    public static void main(String[] args) {
        // 定义正则表达式，确保月份在1到12之间
//        String regex = "\\d{4}-(0[1-9]|1[0-2])";
        String regex = "\\d";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        System.out.println(pattern.matcher("").matches());
        // 测试字符串
        String testDate1 = "2023-4"; // 合法日期
        String testDate2 = "2023-13"; // 不合法日期，月份超过12
        String testDate3 = "2023-04"; // 有效日期，但前面例子已经测试过，这里重复为了完整性
        String testDate4 = "2023-Apr"; // 不合法日期，月份不是数字
        String testDate5 = "2023-00"; // 不合法日期，月份不能为00等非法值
        // 创建 matcher 对象并检查匹配结果
        System.out.println("Is " + testDate1 + " a valid date? " + pattern.matcher(testDate1).matches()); // true
        System.out.println("Is " + testDate2 + " a valid date? " + pattern.matcher(testDate2).matches()); // false
        System.out.println("Is " + testDate3 + " a valid date? " + pattern.matcher(testDate3).matches()); // true（重复测试）
        System.out.println("Is " + testDate4 + " a valid date? " + pattern.matcher(testDate4).matches()); // false
        System.out.println("Is " + testDate5 + " a valid date? " + pattern.matcher(testDate4).matches());
    }
}