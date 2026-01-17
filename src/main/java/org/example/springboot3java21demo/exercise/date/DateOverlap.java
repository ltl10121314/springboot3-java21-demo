package org.example.springboot3java21demo.exercise.date;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * 日期交叉
 */
public class DateOverlap {


    /**
     * 思路：将有交集的情况列出,若不符合有交集的情况,则无交集
     * 有交集的两种情况
     * 1.第一个时间段的开始时间在第二个时间段的开始时间和结束时间当中
     * 2.第二个时间段的开始时间在第一个时间段的开始时间和结束时间当中
     * 判断两个时间段是否有交集
     *
     * @param leftStartDate  第一个时间段的开始时间
     * @param leftEndDate    第一个时间段的结束时间
     * @param rightStartDate 第二个时间段的开始时间
     * @param rightEndDate   第二个时间段的结束时间
     * @return 若有交集, 返回true, 否则返回false
     */
    public boolean hasOverlap(Date leftStartDate, Date leftEndDate, Date rightStartDate, Date rightEndDate) {

        return ((leftStartDate.getTime() >= rightStartDate.getTime())
                && leftStartDate.getTime() < rightEndDate.getTime())
                ||
                ((leftStartDate.getTime() > rightStartDate.getTime())
                        && leftStartDate.getTime() <= rightEndDate.getTime())
                ||
                ((rightStartDate.getTime() >= leftStartDate.getTime())
                        && rightStartDate.getTime() < leftEndDate.getTime())
                ||
                ((rightStartDate.getTime() > leftStartDate.getTime())
                        && rightStartDate.getTime() <= leftEndDate.getTime());

    }


    /**
     * 测试
     * 第一个时间段 2021-12-20 12:00:00 -- 2021-12-22 12:00:00
     * 第二个时间段 2021-12-21 12:00:00 -- 2021-12-23 12:00:00
     * <p>
     * 两者含有交集,则输出true
     *
     * @param args
     */
    public static void main(String[] args) {
        DateOverlap dateOverlap = new DateOverlap();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String leftStart = "2021-12-20 12:00:00";
        String leftEnd = "2021-12-20 12:00:00";
        String rightStart = "2021-12-21 12:00:00";
        String rightEnd = "2021-12-20 12:00:00";
        try {
            Date leftStartDate = simpleDateFormat.parse(leftStart);
            Date leftEndDate = simpleDateFormat.parse(leftEnd);
            Date rightStartDate = simpleDateFormat.parse(rightStart);
            Date rightEndDate = simpleDateFormat.parse(rightEnd);
            // 判断是否有交集
            boolean b = dateOverlap.hasOverlap(leftStartDate, leftEndDate, rightStartDate, rightEndDate);
            System.out.println("是否有交集 = " + b);
        } catch (ParseException e) {
            throw new RuntimeException("时间类型转换失败");
        }

    }

    @Test
    public void test1() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String downLoadDate = simpleDateFormat.format(System.currentTimeMillis());
        System.out.println(downLoadDate);
    }
}