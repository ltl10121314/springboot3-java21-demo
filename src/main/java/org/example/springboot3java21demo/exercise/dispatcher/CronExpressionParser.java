package org.example.springboot3java21demo.exercise.dispatcher;

// 文件: CronExpressionParser.java
// 说明：简单的 Cron 表达式解析器，仅支持秒、分、时的 "*" 或固定数字，示例用作演示，生产环境请使用成熟开源库（如 quartz）
// 仅供学习参考，真实项目强烈建议使用 Apache Commons CronParser 或 Quartz

import java.time.LocalDateTime;
import java.util.StringTokenizer;

public class CronExpressionParser {
    private final String expression;
    private final String[] fields; // ["秒域", "分域", "时域"]

    /**
     * 构造方法
     * 示例表达式："0/30 * *" 表示每 30 秒执行一次："秒 域/步长 分 域 时 域"
     *
     * @param expression 三段式 Cron 表达式，格式："秒 分 时"，用空格分隔
     */
    public CronExpressionParser(String expression) {
        this.expression = expression.trim();
        StringTokenizer st = new StringTokenizer(this.expression, " ");
        if (st.countTokens() != 3) {
            throw new IllegalArgumentException("Cron 表达式格式错误，仅支持 \"秒 分 时\" 三段式，例如：\"0/30 * *\"");
        }
        fields = new String[3];
        for (int i = 0; i < 3; i++) {
            fields[i] = st.nextToken();
        }
    }

    /**
     * 判断给定时间点是否满足 Cron 表达式
     *
     * @param dateTime 当前本地时间
     * @return true 表示应触发执行，false 表示跳过
     */
    public boolean matches(LocalDateTime dateTime) {
        int second = dateTime.getSecond();
        int minute = dateTime.getMinute();
        int hour = dateTime.getHour();

        return matchField(fields[0], second) &&
                matchField(fields[1], minute) &&
                matchField(fields[2], hour);
    }

    /**
     * 匹配单个域（秒、分、时）支持三种语法：
     * <li>a) “*”：任意值；
     * <li>b) “* / n”：每隔 n 单位触发一次，例如 “* / 30” 表示每 30 单位触发一次;
     * <li>c) 具体数字 “m”：在对应单位等于 m 时触发。
     *
     * @param field 域表达式
     * @param value 当前域的数值（0-59 或 0-23）
     * @return 是否匹配
     */
    private boolean matchField(String field, int value) {
        if ("*".equals(field)) {
            return true;
        }
        // 支持 "*/n" 格式
        if (field.startsWith("*/")) {
            String stepStr = field.substring(2);
            int step = Integer.parseInt(stepStr);
            return value % step == 0;
        }
        // 支持固定数字
        int fixed = Integer.parseInt(field);
        return value == fixed;
    }

    @Override
    public String toString() {
        return "CronExpressionParser{expression='" + expression + "'}";
    }
}
