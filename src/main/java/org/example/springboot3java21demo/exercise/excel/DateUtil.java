package org.example.springboot3java21demo.exercise.excel;

import org.example.springboot3java21demo.exercise.date.DateStyle;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;


public class DateUtil {
    private static final Logger log = LoggerFactory.getLogger(DateUtil.class);
    private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal();
    private static final Object object = new Object();

    public DateUtil() {
    }

    private static SimpleDateFormat getDateFormat(String pattern) throws RuntimeException {
        SimpleDateFormat dateFormat = (SimpleDateFormat) threadLocal.get();
        if (dateFormat == null) {
            synchronized (object) {
                if (dateFormat == null) {
                    dateFormat = new SimpleDateFormat(pattern);
                    dateFormat.setLenient(false);
                    threadLocal.set(dateFormat);
                }
            }
        }

        dateFormat.applyPattern(pattern);
        return dateFormat;
    }

    public static void remove() {
        threadLocal.remove();
    }

    private static int getInteger(Date date, int dateType) {
        int num = 0;
        Calendar calendar = Calendar.getInstance();
        if (date != null) {
            calendar.setTime(date);
            num = calendar.get(dateType);
        }

        return num;
    }

    private static String addInteger(String date, int dateType, int amount) {
        String dateString = null;
        DateStyle dateStyle = getDateStyle(date);
        if (dateStyle != null) {
            Date myDate = StringToDate(date, dateStyle);
            myDate = addInteger(myDate, dateType, amount);
            dateString = DateToString(myDate, dateStyle);
        }

        return dateString;
    }

    private static Date addInteger(Date date, int dateType, int amount) {
        Date myDate = null;
        if (date != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(dateType, amount);
            myDate = calendar.getTime();
        }

        return myDate;
    }

    public static String stampToDate(String s) {
        if (StringUtils.isEmpty(s)) {
            return null;
        } else {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            long lt = new Long(s);
            Date date = new Date(lt);
            String res = simpleDateFormat.format(date);
            return res;
        }
    }

    private static Date getAccurateDate(List<Long> timestamps) {
        Date date = null;
        long timestamp = 0L;
        Map<Long, long[]> map = new HashMap();
        List<Long> absoluteValues = new ArrayList();
        if (timestamps != null && timestamps.size() > 0) {
            if (timestamps.size() > 1) {
                for (int i = 0; i < timestamps.size(); ++i) {
                    for (int j = i + 1; j < timestamps.size(); ++j) {
                        long absoluteValue = Math.abs((Long) timestamps.get(i) - (Long) timestamps.get(j));
                        absoluteValues.add(absoluteValue);
                        long[] timestampTmp = new long[]{(Long) timestamps.get(i), (Long) timestamps.get(j)};
                        map.put(absoluteValue, timestampTmp);
                    }
                }

                long minAbsoluteValue = -1L;
                if (!absoluteValues.isEmpty()) {
                    minAbsoluteValue = (Long) absoluteValues.get(0);

                    for (int i = 1; i < absoluteValues.size(); ++i) {
                        if (minAbsoluteValue > (Long) absoluteValues.get(i)) {
                            minAbsoluteValue = (Long) absoluteValues.get(i);
                        }
                    }
                }

                if (minAbsoluteValue != -1L) {
                    long[] timestampsLastTmp = (long[]) map.get(minAbsoluteValue);
                    long dateOne = timestampsLastTmp[0];
                    long dateTwo = timestampsLastTmp[1];
                    if (absoluteValues.size() > 1) {
                        timestamp = Math.abs(dateOne) > Math.abs(dateTwo) ? dateOne : dateTwo;
                    }
                }
            } else {
                timestamp = (Long) timestamps.get(0);
            }
        }

        if (timestamp != 0L) {
            date = new Date(timestamp);
        }

        return date;
    }

    public static boolean isDate(String date) {
        boolean isDate = false;
        if (date != null && getDateStyle(date) != null) {
            isDate = true;
        }

        return isDate;
    }

    public static DateStyle getDateStyle(String date) {
        DateStyle dateStyle = null;
        Map<Long, DateStyle> map = new HashMap();
        List<Long> timestamps = new ArrayList();
        DateStyle[] var4 = DateStyle.values();
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            DateStyle style = var4[var6];
            if (!style.isShowOnly()) {
                Date dateTmp = null;
                if (date != null) {
                    try {
                        ParsePosition pos = new ParsePosition(0);
                        dateTmp = getDateFormat(style.getValue()).parse(date, pos);
                        if (pos.getIndex() != date.length()) {
                            dateTmp = null;
                        }
                    } catch (Exception var10) {
                    }
                }

                if (dateTmp != null) {
                    timestamps.add(dateTmp.getTime());
                    map.put(dateTmp.getTime(), style);
                }
            }
        }

        Date accurateDate = getAccurateDate(timestamps);
        if (accurateDate != null) {
            dateStyle = (DateStyle) map.get(accurateDate.getTime());
        }

        return dateStyle;
    }

    public static Date StringToDate(String date) {
        DateStyle dateStyle = getDateStyle(date);
        return StringToDate(date, dateStyle);
    }

    public static Date StringToDate(String date, String pattern) {
        Date myDate = null;
        if (date != null) {
            try {
                myDate = getDateFormat(pattern).parse(date);
            } catch (Exception var4) {
            }
        }

        return myDate;
    }

    public static Date StringToDate(String date, DateStyle dateStyle) {
        Date myDate = null;
        if (dateStyle != null) {
            myDate = StringToDate(date, dateStyle.getValue());
        }

        return myDate;
    }

    public static String DateToString(Date date, String pattern) {
        String dateString = null;
        if (date != null) {
            try {
                dateString = getDateFormat(pattern).format(date);
            } catch (Exception var4) {
            }
        }

        return dateString;
    }

    public static String DateToString(Date date, DateStyle dateStyle) {
        String dateString = null;
        if (dateStyle != null) {
            dateString = DateToString(date, dateStyle.getValue());
        }

        return dateString;
    }

    public static String StringToString(String date, String newPattern) {
        DateStyle oldDateStyle = getDateStyle(date);
        return StringToString(date, oldDateStyle, newPattern);
    }

    public static String StringToString(String date, DateStyle newDateStyle) {
        DateStyle oldDateStyle = getDateStyle(date);
        return StringToString(date, oldDateStyle, newDateStyle);
    }

    public static String StringToString(String date, String olddPattern, String newPattern) {
        return DateToString(StringToDate(date, olddPattern), newPattern);
    }

    public static String StringToString(String date, DateStyle olddDteStyle, String newParttern) {
        String dateString = null;
        if (olddDteStyle != null) {
            dateString = StringToString(date, olddDteStyle.getValue(), newParttern);
        }

        return dateString;
    }

    public static String StringToString(String date, String olddPattern, DateStyle newDateStyle) {
        String dateString = null;
        if (newDateStyle != null) {
            dateString = StringToString(date, olddPattern, newDateStyle.getValue());
        }

        return dateString;
    }

    public static String StringToString(String date, DateStyle olddDteStyle, DateStyle newDateStyle) {
        String dateString = null;
        if (olddDteStyle != null && newDateStyle != null) {
            dateString = StringToString(date, olddDteStyle.getValue(), newDateStyle.getValue());
        }

        return dateString;
    }

    public static String addYear(String date, int yearAmount) {
        return addInteger((String) date, 1, yearAmount);
    }

    public static Date addYear(Date date, int yearAmount) {
        return addInteger((Date) date, 1, yearAmount);
    }

    public static String addMonth(String date, int monthAmount) {
        return addInteger((String) date, 2, monthAmount);
    }

    public static Date addMonth(Date date, int monthAmount) {
        return addInteger((Date) date, 2, monthAmount);
    }

    public static String addDay(String date, int dayAmount) {
        return addInteger((String) date, 5, dayAmount);
    }

    public static Date addDay(Date date, int dayAmount) {
        return addInteger((Date) date, 5, dayAmount);
    }

    public static String addHour(String date, int hourAmount) {
        return addInteger((String) date, 11, hourAmount);
    }

    public static Date addHour(Date date, int hourAmount) {
        return addInteger((Date) date, 11, hourAmount);
    }

    public static String addMinute(String date, int minuteAmount) {
        return addInteger((String) date, 12, minuteAmount);
    }

    public static Date addMinute(Date date, int minuteAmount) {
        return addInteger((Date) date, 12, minuteAmount);
    }

    public static String addSecond(String date, int secondAmount) {
        return addInteger((String) date, 13, secondAmount);
    }

    public static Date addSecond(Date date, int secondAmount) {
        return addInteger((Date) date, 13, secondAmount);
    }

    public static int getYear(String date) {
        return getYear(StringToDate(date));
    }

    public static int getYear(Date date) {
        return getInteger(date, 1);
    }

    public static int getMonth(String date) {
        return getMonth(StringToDate(date));
    }

    public static int getMonth(Date date) {
        return getInteger(date, 2) + 1;
    }

    public static int getDay(String date) {
        return getDay(StringToDate(date));
    }

    public static int getDay(Date date) {
        return getInteger(date, 5);
    }

    public static int getHour(String date) {
        return getHour(StringToDate(date));
    }

    public static int getHour(Date date) {
        return getInteger(date, 11);
    }

    public static int getMinute(String date) {
        return getMinute(StringToDate(date));
    }

    public static int getMinute(Date date) {
        return getInteger(date, 12);
    }

    public static int getSecond(String date) {
        return getSecond(StringToDate(date));
    }

    public static int getSecond(Date date) {
        return getInteger(date, 13);
    }

    public static String getDate(String date) {
        return StringToString(date, DateStyle.YYYY_MM_DD);
    }

    public static String getDate(Date date) {
        return DateToString(date, DateStyle.YYYY_MM_DD);
    }

//	public static String getTime(String date) {
//		return StringToString(date, DateStyle.HH_MM_SS);
//	}
//
//	public static String getTime(Date date) {
//		return DateToString(date, DateStyle.HH_MM_SS);
//	}

    public static int getIntervalDays(String date, String otherDate) {
        return getIntervalDays(StringToDate(date), StringToDate(otherDate));
    }

    public static int getIntervalDays(Date date, Date otherDate) {
        int num = -1;
        Date dateTmp = StringToDate(getDate(date), DateStyle.YYYY_MM_DD);
        Date otherDateTmp = StringToDate(getDate(otherDate), DateStyle.YYYY_MM_DD);
        if (dateTmp != null && otherDateTmp != null) {
            long time = Math.abs(dateTmp.getTime() - otherDateTmp.getTime());
            num = (int) (time / 86400000L);
        }

        return num;
    }

    public static Date getDayStart(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        return StringToDate(sdf.format(date));
    }

    public static Date getDayEnd(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        return StringToDate(sdf.format(date));
    }

    public static Date getCurrentWeekStart(Date date) {
        new HashMap();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        cal.set(7, 2);
        return StringToDate(df.format(cal.getTime()));
    }

    public static Date getCurrentWeekEnd() {
        new HashMap();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        cal.set(7, 1);
        cal.add(3, 1);
        return StringToDate(df.format(cal.getTime()));
    }

    public static Date getCurrentMonthStart() {
        Calendar cal = Calendar.getInstance();
        cal.add(2, 0);
        cal.set(5, 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        return StringToDate(df.format(cal.getTime()));
    }

    public static Date getCurrentMonthEnd() {
        Calendar cal = Calendar.getInstance();
        cal.set(5, cal.getActualMaximum(5));
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        return StringToDate(df.format(cal.getTime()));
    }

    public static Date getMonthStart(String strYear, String strMonth) {
        Calendar cal = Calendar.getInstance();
        cal.set(1, Integer.parseInt(strYear));
        cal.set(2, Integer.parseInt(strMonth));
        cal.set(5, 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        cal.set(5, 1);
        cal.add(5, -1);
        cal.set(5, 1);
        Date firstDate = cal.getTime();
        return StringToDate(df.format(firstDate));
    }

    public static Date getMonthEnd(String strYear, String strMonth) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        Calendar cal = Calendar.getInstance();
        cal.set(1, Integer.parseInt(strYear));
        cal.set(2, Integer.parseInt(strMonth));
        cal.set(5, 1);
        cal.add(5, -1);
        Date lastDate = cal.getTime();
        return StringToDate(df.format(lastDate));
    }

    public static int getDayofweek(String date) {
        Calendar cal = Calendar.getInstance();
        if (StringUtils.isEmpty(date)) {
            cal.setTime(new Date(System.currentTimeMillis()));
        } else {
            cal.setTime(new Date(getDateByStr2(date).getTime()));
        }

        return cal.get(7);
    }

    private static Date getDateByStr2(String dd) {
        SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");

        Date date;
        try {
            date = sd.parse(dd);
        } catch (ParseException var4) {
            date = null;
        }

        return date;
    }

    public static int getMonthWeek(String date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        Date s = sdf.parse(date);
        Calendar ca = Calendar.getInstance();
        ca.setTime(s);
        ca.setFirstDayOfWeek(2);
        // System.out.println(ca.getActualMaximum(4));
        return ca.getActualMaximum(4);
    }

//	public static List<String> getMonthWithOffset(String month, int offset) {
//		ArrayList list = null;
//
//		try {
//			list = new ArrayList();
//			String firstDay = "";
//			Date date = (new SimpleDateFormat("yyyy-MM")).parse(month);
//			Date now = new Date();
//			if (date.after(now)) {
//				date = now;
//			}
//
//			List<String> mlist = getYearMonthBetweenDate(DateToString(date, "yyyy-MM"), DateToString(now, "yyyy-MM"));
//			int i;
//			if (mlist.size() <= offset) {
//				i = offset * 2 + 1 - mlist.size();
//
//				for(int i = i; i >= 1; --i) {
//					Date d = addMonth(date, i * -1);
//					list.add(DateToString(d, "yyyy-MM"));
//				}
//
//				list.addAll(mlist);
//				return list;
//			} else {
//				Date d;
//				for(i = offset; i >= 1; --i) {
//					d = addMonth(date, i * -1);
//					list.add(DateToString(d, "yyyy-MM"));
//				}
//
//				list.add(month);
//
//				for(i = 1; i <= offset; ++i) {
//					d = addMonth(date, i);
//					list.add(DateToString(d, "yyyy-MM"));
//				}
//
//				return list;
//			}
//		} catch (ParseException var10) {
//			return null;
//		}
//	}

    public static List<String> getYearMonthBetweenDate(String startDate, String endDate) {
        ArrayList list = null;

        try {
            list = new ArrayList();
            String firstDay = "";
            String lastDay = "";
            Date d1 = (new SimpleDateFormat("yyyy-MM")).parse(startDate);
            Date d2 = (new SimpleDateFormat("yyyy-MM")).parse(endDate);
            Calendar dd = Calendar.getInstance();
            dd.setTime(d1);
            Calendar cale = Calendar.getInstance();
            Calendar c = Calendar.getInstance();
            c.setTime(d2);
            int startDay = d1.getDate();
            int endDay = d2.getDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
            String keyValueForDate = null;

            while (dd.getTime().compareTo(d2) <= 0) {
                new String();
                cale.setTime(dd.getTime());
                if (dd.getTime().equals(d1)) {
                    cale.set(5, dd.getActualMaximum(5));
                    keyValueForDate = sdf.format(d1);
                } else if (dd.get(2) == d2.getMonth() && dd.get(1) == c.get(1)) {
                    cale.set(5, 1);
                    firstDay = sdf.format(cale.getTime());
                    keyValueForDate = firstDay;
                } else {
                    cale.set(5, 1);
                    firstDay = sdf.format(cale.getTime());
                    cale.set(5, dd.getActualMaximum(5));
                    keyValueForDate = firstDay;
                }

                list.add(keyValueForDate);
                dd.add(2, 1);
            }

            if (endDay < startDay) {
                new String();
                cale.setTime(d2);
                cale.set(5, 1);
                firstDay = sdf.format(cale.getTime());
                list.add(firstDay);
            }

            return list;
        } catch (ParseException var14) {
            return null;
        }
    }

    public static Calendar getMonthStartTime(Integer year, Integer month) {
        Calendar startDate = Calendar.getInstance();
        startDate.set(1, year);
        startDate.set(2, month - 1);
        startDate.set(5, 1);
        startDate.set(11, 0);
        startDate.set(12, 0);
        startDate.set(13, 0);
        startDate.set(14, 0);
        return startDate;
    }

    public static Calendar getMonthEndTime(Integer year, Integer month) {
        Calendar endDate = Calendar.getInstance();
        endDate.set(1, year);
        endDate.set(2, month);
        endDate.set(5, 1);
        endDate.add(5, -1);
        endDate.set(11, 0);
        endDate.set(12, 0);
        endDate.set(13, 0);
        endDate.set(14, 0);
        return endDate;
    }

    public static BigDecimal getDateTimeDifferenceHours(Date beginTime, Date endDate) {
        double nh = 3600000.0D;
        long diff = endDate.getTime() - beginTime.getTime();
        return (new BigDecimal((double) diff / nh)).setScale(2, 4);
    }

    public static void main(String[] args) {
    }

    /**
     * 将时间戳转换为时间格式
     *
     * @param time
     * @return
     */
    public static Date timeStamp2Date(String time) {
        Long timeLong = Long.parseLong(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//要转换的时间格式
        Date date;
        try {
            return sdf.parse(sdf.format(timeLong));

        } catch (Exception e) {

            return null;
        }
    }

    /**
     * 将时间戳转换为时间格式
     *
     * @param time
     * @return
     */
    public static Date timeStamp2Date(String time, DateStyle dateStyle) {
        Long timeLong = Long.parseLong(time);
        SimpleDateFormat sdf = new SimpleDateFormat(dateStyle.getValue());//要转换的时间格式
        Date date;
        try {
            return sdf.parse(sdf.format(timeLong));

        } catch (Exception e) {
            return null;
        }
    }
}
