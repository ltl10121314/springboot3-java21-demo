package org.example.springboot3java21demo.exercise.stack;

import org.junit.Test;
import org.quartz.TriggerUtils;
import org.quartz.impl.triggers.CronTriggerImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Administrator
 */

public class CronUtil {
    /**
     * 校验cron表达式；
     * 成功返回最近5次运行时间；
     * corn从左到右（用空格隔开）：秒 分 小时 月份中的日期 月份 星期中的日期；
     * 此方法不支持年份；
     *
     * @param cron
     * @return
     * @author Liu Tianlong
     */
    public List<String> validateCronExpressionTest(String cron) {
        List<String> dateString = new ArrayList<String>();
        try {
            CronTriggerImpl cronTriggerImpl = new CronTriggerImpl();
            cronTriggerImpl.setCronExpression(cron);
            // 指定获取最近几次的运行时间，这里指定为5次;
            List<Date> dates = TriggerUtils.computeFireTimes(cronTriggerImpl, null, 5);
            for (Date date : dates) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String format = formatter.format(date);
                dateString.add(format);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;
    }

    @Test
    public void getCronRunTimeTest() {
        String cron = "  0  0 0,13,18,21 * * ?";
        char[] chars = cron.toCharArray();
        int count = 0;
        boolean flag = false;
        for (char aChar : chars) {
            if (chars[0] == ' ' || chars[chars.length - 1] == ' ') {
                flag = false;
                break;
            }
            if (aChar == ' ') {
                count++;
            } else {
                count = 0;
                flag = true;
            }
            if (count > 1) {
                flag = false;
                break;
            }
        }
        System.out.println(flag);
    }

}
