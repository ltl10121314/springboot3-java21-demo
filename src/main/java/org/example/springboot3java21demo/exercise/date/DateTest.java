package org.example.springboot3java21demo.exercise.date;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Date;

@Slf4j
public class DateTest {

    @Test
    public void dateTest1() {
        Object object = "2024";
        DateStyle dateStyle = DateUtil.getDateStyle(object.toString());
        log.error(dateStyle.getValue());
        Date date = DateUtil.StringToDate(String.valueOf(object), dateStyle.getValue());
        String string = DateUtil.DateToString(date, DateStyle.YYYY_MM_DD);
        log.error(string);
    }
}
