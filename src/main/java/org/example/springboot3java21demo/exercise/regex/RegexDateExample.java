package org.example.springboot3java21demo.exercise.regex;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexDateExample {
    private static final Logger log = LoggerFactory.getLogger(RegexDateExample.class);

    public static void main(String[] args) {
        // 定义正则表达式，确保月份在1到12之间
        // String regex = "\\d{4}-(0[1-9]|1[0-2])";
        String regex = "\\d";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);
        log.info(String.valueOf(pattern.matcher("").matches()));
        // 测试字符串
        String testDate1 = "2023-4"; // 合法日期
        String testDate2 = "2023-13"; // 不合法日期，月份超过12
        String testDate3 = "2023-04"; // 有效日期，但前面例子已经测试过，这里重复为了完整性
        String testDate4 = "2023-Apr"; // 不合法日期，月份不是数字
        String testDate5 = "2023-00"; // 不合法日期，月份不能为00等非法值
        // 创建 matcher 对象并检查匹配结果
        log.info("Is " + testDate1 + " a valid date? " + pattern.matcher(testDate1).matches()); // true
        log.info("Is " + testDate2 + " a valid date? " + pattern.matcher(testDate2).matches()); // false
        log.info("Is " + testDate3 + " a valid date? " + pattern.matcher(testDate3).matches()); // true（重复测试）
        log.info("Is " + testDate4 + " a valid date? " + pattern.matcher(testDate4).matches()); // false
        log.info("Is " + testDate5 + " a valid date? " + pattern.matcher(testDate4).matches());
    }

    // public static final Pattern NUMBER_WITH_TRAILING_ZEROS_PATTERN = Pattern.compile("\\.0*");
    public static final Pattern NUMBER_WITH_TRAILING_ZEROS_PATTERN = Pattern.compile("^[GCDZTSPKXLY1-9]\\d{1,4}$");
    public static final Pattern pattern = Pattern.compile("waitem.f_[i,v,d,b,n]_\\d+");

    @Test
    public void test1() {
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
}