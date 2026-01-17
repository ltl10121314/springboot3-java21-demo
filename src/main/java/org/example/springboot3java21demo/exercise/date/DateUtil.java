package org.example.springboot3java21demo.exercise.date;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class DateUtil {

	private static final ThreadLocal<SimpleDateFormat> threadLocal = new ThreadLocal<SimpleDateFormat>();

	private static final Object object = new Object();

	/**
	 * 获取SimpleDateFormat
	 *
	 * @param pattern 日期格式
	 * @return SimpleDateFormat对象
	 * @throws RuntimeException 异常：非法日期格式
	 */
	private static SimpleDateFormat getDateFormat(String pattern) throws RuntimeException {
		SimpleDateFormat dateFormat = threadLocal.get();
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

	public static void remove(){
		threadLocal.remove();
	}

	/**
	 * 获取日期中的某数值。如获取月份
	 *
	 * @param date 日期
	 * @param dateType 日期格式
	 * @return 数值
	 */
	private static int getInteger(Date date, int dateType) {
		int num = 0;
		Calendar calendar = Calendar.getInstance();
		if (date != null) {
			calendar.setTime(date);
			num = calendar.get(dateType);
		}
		return num;
	}

	/**
	 * 增加日期中某类型的某数值。如增加日期
	 *
	 * @param date 日期字符串
	 * @param dateType 类型
	 * @param amount 数值
	 * @return 计算后日期字符串
	 */
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

	/**
	 * 增加日期中某类型的某数值。如增加日期
	 *
	 * @param date 日期
	 * @param dateType 类型
	 * @param amount 数值
	 * @return 计算后日期
	 */
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

	/**
	 * 获取精确的日期
	 *
	 * @param timestamps 时间long集合
	 * @return 日期
	 */
	private static Date getAccurateDate(List<Long> timestamps) {
		Date date = null;
		long timestamp = 0;
		Map<Long, long[]> map = new HashMap<Long, long[]>();
		List<Long> absoluteValues = new ArrayList<Long>();

		if (timestamps != null && timestamps.size() > 0) {
			if (timestamps.size() > 1) {
				for (int i = 0; i < timestamps.size(); i++) {
					for (int j = i + 1; j < timestamps.size(); j++) {
						long absoluteValue = Math.abs(timestamps.get(i) - timestamps.get(j));
						absoluteValues.add(absoluteValue);
						long[] timestampTmp = { timestamps.get(i), timestamps.get(j) };
						map.put(absoluteValue, timestampTmp);
					}
				}

				// 有可能有相等的情况。如2012-11和2012-11-01。时间戳是相等的。此时minAbsoluteValue为0
				// 因此不能将minAbsoluteValue取默认值0
				long minAbsoluteValue = -1;
				if (!absoluteValues.isEmpty()) {
					minAbsoluteValue = absoluteValues.get(0);
					for (int i = 1; i < absoluteValues.size(); i++) {
						if (minAbsoluteValue > absoluteValues.get(i)) {
							minAbsoluteValue = absoluteValues.get(i);
						}
					}
				}

				if (minAbsoluteValue != -1) {
					long[] timestampsLastTmp = map.get(minAbsoluteValue);

					long dateOne = timestampsLastTmp[0];
					long dateTwo = timestampsLastTmp[1];
					if (absoluteValues.size() > 1) {
						timestamp = Math.abs(dateOne) > Math.abs(dateTwo) ? dateOne : dateTwo;
					}
				}
			} else {
				timestamp = timestamps.get(0);
			}
		}

		if (timestamp != 0) {
			date = new Date(timestamp);
		}
		return date;
	}

	/**
	 * 判断字符串是否为日期字符串
	 *
	 * @param date 日期字符串
	 * @return true or false
	 */
	public static boolean isDate(String date) {
		boolean isDate = false;
		if (date != null) {
			if (getDateStyle(date) != null) {
				isDate = true;
			}
		}
		return isDate;
	}

	/**
	 * 获取日期字符串的日期风格。失敗返回null。
	 *
	 * @param date 日期字符串
	 * @return 日期风格
	 */
	public static DateStyle getDateStyle(String date) {
		DateStyle dateStyle = null;
		Map<Long, DateStyle> map = new HashMap<Long, DateStyle>();
		List<Long> timestamps = new ArrayList<Long>();
		for (DateStyle style : DateStyle.values()) {
			if (style.isShowOnly()) {
				continue;
			}
			Date dateTmp = null;
			if (date != null) {
				try {
					ParsePosition pos = new ParsePosition(0);
					dateTmp = getDateFormat(style.getValue()).parse(date, pos);
					if (pos.getIndex() != date.length()) {
						dateTmp = null;
					}
				} catch (Exception e) {
				}
			}
			if (dateTmp != null) {
				timestamps.add(dateTmp.getTime());
				map.put(dateTmp.getTime(), style);
			}
		}
		Date accurateDate = getAccurateDate(timestamps);
		if (accurateDate != null) {
			dateStyle = map.get(accurateDate.getTime());
		}
		return dateStyle;
	}

	/**
	 * 将日期字符串转化为日期。失败返回null。
	 *
	 * @param date 日期字符串
	 * @return 日期
	 */
	public static Date StringToDate(String date) {
		DateStyle dateStyle = getDateStyle(date);
		return StringToDate(date, dateStyle);
	}

	/**
	 * 将日期字符串转化为日期。失败返回null。
	 *
	 * @param date 日期字符串
	 * @param pattern 日期格式
	 * @return 日期
	 */
	public static Date StringToDate(String date, String pattern) {
		Date myDate = null;
		if (date != null) {
			try {
				myDate = getDateFormat(pattern).parse(date);
			} catch (Exception e) {
			}
		}
		return myDate;
	}

	/**
	 * 将日期字符串转化为日期。失败返回null。
	 *
	 * @param date 日期字符串
	 * @param dateStyle 日期风格
	 * @return 日期
	 */
	public static Date StringToDate(String date, DateStyle dateStyle) {
		Date myDate = null;
		if (dateStyle != null) {
			myDate = StringToDate(date, dateStyle.getValue());
		}
		return myDate;
	}

	/**
	 * 将日期转化为日期字符串。失败返回null。
	 *
	 * @param date 日期
	 * @param pattern 日期格式
	 * @return 日期字符串
	 */
	public static String DateToString(Date date, String pattern) {
		String dateString = null;
		if (date != null) {
			try {
				dateString = getDateFormat(pattern).format(date);
			} catch (Exception e) {
			}
		}
		return dateString;
	}

	/**
	 * 将日期转化为日期字符串。失败返回null。
	 *
	 * @param date 日期
	 * @param dateStyle 日期风格
	 * @return 日期字符串
	 */
	public static String DateToString(Date date, DateStyle dateStyle) {
		String dateString = null;
		if (dateStyle != null) {
			dateString = DateToString(date, dateStyle.getValue());
		}
		return dateString;
	}

	/**
	 * 将日期字符串转化为另一日期字符串。失败返回null。
	 *
	 * @param date 旧日期字符串
	 * @param newPattern 新日期格式
	 * @return 新日期字符串
	 */
	public static String StringToString(String date, String newPattern) {
		DateStyle oldDateStyle = getDateStyle(date);
		return StringToString(date, oldDateStyle, newPattern);
	}

	/**
	 * 将日期字符串转化为另一日期字符串。失败返回null。
	 *
	 * @param date 旧日期字符串
	 * @param newDateStyle 新日期风格
	 * @return 新日期字符串
	 */
	public static String StringToString(String date, DateStyle newDateStyle) {
		DateStyle oldDateStyle = getDateStyle(date);
		return StringToString(date, oldDateStyle, newDateStyle);
	}

	/**
	 * 将日期字符串转化为另一日期字符串。失败返回null。
	 *
	 * @param date 旧日期字符串
	 * @param olddPattern 旧日期格式
	 * @param newPattern 新日期格式
	 * @return 新日期字符串
	 */
	public static String StringToString(String date, String olddPattern, String newPattern) {
		return DateToString(StringToDate(date, olddPattern), newPattern);
	}

	/**
	 * 将日期字符串转化为另一日期字符串。失败返回null。
	 *
	 * @param date 旧日期字符串
	 * @param olddDteStyle 旧日期风格
	 * @param newParttern 新日期格式
	 * @return 新日期字符串
	 */
	public static String StringToString(String date, DateStyle olddDteStyle, String newParttern) {
		String dateString = null;
		if (olddDteStyle != null) {
			dateString = StringToString(date, olddDteStyle.getValue(), newParttern);
		}
		return dateString;
	}

	/**
	 * 将日期字符串转化为另一日期字符串。失败返回null。
	 *
	 * @param date 旧日期字符串
	 * @param olddPattern 旧日期格式
	 * @param newDateStyle 新日期风格
	 * @return 新日期字符串
	 */
	public static String StringToString(String date, String olddPattern, DateStyle newDateStyle) {
		String dateString = null;
		if (newDateStyle != null) {
			dateString = StringToString(date, olddPattern, newDateStyle.getValue());
		}
		return dateString;
	}

	/**
	 * 将日期字符串转化为另一日期字符串。失败返回null。
	 *
	 * @param date 旧日期字符串
	 * @param olddDteStyle 旧日期风格
	 * @param newDateStyle 新日期风格
	 * @return 新日期字符串
	 */
	public static String StringToString(String date, DateStyle olddDteStyle, DateStyle newDateStyle) {
		String dateString = null;
		if (olddDteStyle != null && newDateStyle != null) {
			dateString = StringToString(date, olddDteStyle.getValue(), newDateStyle.getValue());
		}
		return dateString;
	}

	/**
	 * 增加日期的年份。失败返回null。
	 *
	 * @param date 日期
	 * @param yearAmount 增加数量。可为负数
	 * @return 增加年份后的日期字符串
	 */
	public static String addYear(String date, int yearAmount) {
		return addInteger(date, Calendar.YEAR, yearAmount);
	}

	/**
	 * 增加日期的年份。失败返回null。
	 *
	 * @param date 日期
	 * @param yearAmount 增加数量。可为负数
	 * @return 增加年份后的日期
	 */
	public static Date addYear(Date date, int yearAmount) {
		return addInteger(date, Calendar.YEAR, yearAmount);
	}

	/**
	 * 增加日期的月份。失败返回null。
	 *
	 * @param date 日期
	 * @param monthAmount 增加数量。可为负数
	 * @return 增加月份后的日期字符串
	 */
	public static String addMonth(String date, int monthAmount) {
		return addInteger(date, Calendar.MONTH, monthAmount);
	}

	/**
	 * 增加日期的月份。失败返回null。
	 *
	 * @param date 日期
	 * @param monthAmount 增加数量。可为负数
	 * @return 增加月份后的日期
	 */
	public static Date addMonth(Date date, int monthAmount) {
		return addInteger(date, Calendar.MONTH, monthAmount);
	}

	/**
	 * 增加日期的天数。失败返回null。
	 *
	 * @param date 日期字符串
	 * @param dayAmount 增加数量。可为负数
	 * @return 增加天数后的日期字符串
	 */
	public static String addDay(String date, int dayAmount) {
		return addInteger(date, Calendar.DATE, dayAmount);
	}

	/**
	 * 增加日期的天数。失败返回null。
	 *
	 * @param date 日期
	 * @param dayAmount 增加数量。可为负数
	 * @return 增加天数后的日期
	 */
	public static Date addDay(Date date, int dayAmount) {
		return addInteger(date, Calendar.DATE, dayAmount);
	}

	/**
	 * 增加日期的小时。失败返回null。
	 *
	 * @param date 日期字符串
	 * @param hourAmount 增加数量。可为负数
	 * @return 增加小时后的日期字符串
	 */
	public static String addHour(String date, int hourAmount) {
		return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
	}

	/**
	 * 增加日期的小时。失败返回null。
	 *
	 * @param date 日期
	 * @param hourAmount 增加数量。可为负数
	 * @return 增加小时后的日期
	 */
	public static Date addHour(Date date, int hourAmount) {
		return addInteger(date, Calendar.HOUR_OF_DAY, hourAmount);
	}

	/**
	 * 增加日期的分钟。失败返回null。
	 *
	 * @param date 日期字符串
	 * @param minuteAmount 增加数量。可为负数
	 * @return 增加分钟后的日期字符串
	 */
	public static String addMinute(String date, int minuteAmount) {
		return addInteger(date, Calendar.MINUTE, minuteAmount);
	}

	/**
	 * 增加日期的分钟。失败返回null。
	 *
	 * @param date 日期
	 * @param minuteAmount 增加数量。可为负数
	 * @return 增加分钟后的日期
	 */
	public static Date addMinute(Date date, int minuteAmount) {
		return addInteger(date, Calendar.MINUTE, minuteAmount);
	}

	/**
	 * 增加日期的秒钟。失败返回null。
	 *
	 * @param date 日期字符串
	 * @param secondAmount 增加数量。可为负数
	 * @return 增加秒钟后的日期字符串
	 */
	public static String addSecond(String date, int secondAmount) {
		return addInteger(date, Calendar.SECOND, secondAmount);
	}

	/**
	 * 增加日期的秒钟。失败返回null。
	 *
	 * @param date 日期
	 * @param secondAmount 增加数量。可为负数
	 * @return 增加秒钟后的日期
	 */
	public static Date addSecond(Date date, int secondAmount) {
		return addInteger(date, Calendar.SECOND, secondAmount);
	}

	/**
	 * 获取日期的年份。失败返回0。
	 *
	 * @param date 日期字符串
	 * @return 年份
	 */
	public static int getYear(String date) {
		return getYear(StringToDate(date));
	}

	/**
	 * 获取日期的年份。失败返回0。
	 *
	 * @param date 日期
	 * @return 年份
	 */
	public static int getYear(Date date) {
		return getInteger(date, Calendar.YEAR);
	}

	/**
	 * 获取日期的月份。失败返回0。
	 *
	 * @param date 日期字符串
	 * @return 月份
	 */
	public static int getMonth(String date) {
		return getMonth(StringToDate(date));
	}

	/**
	 * 获取日期的月份。失败返回0。
	 *
	 * @param date 日期
	 * @return 月份
	 */
	public static int getMonth(Date date) {
		return getInteger(date, Calendar.MONTH) + 1;
	}

	/**
	 * 获取日期的天数。失败返回0。
	 *
	 * @param date 日期字符串
	 * @return 天
	 */
	public static int getDay(String date) {
		return getDay(StringToDate(date));
	}

	/**
	 * 获取日期的天数。失败返回0。
	 *
	 * @param date 日期
	 * @return 天
	 */
	public static int getDay(Date date) {
		return getInteger(date, Calendar.DATE);
	}

	/**
	 * 获取日期的小时。失败返回0。
	 *
	 * @param date 日期字符串
	 * @return 小时
	 */
	public static int getHour(String date) {
		return getHour(StringToDate(date));
	}

	/**
	 * 获取日期的小时。失败返回0。
	 *
	 * @param date 日期
	 * @return 小时
	 */
	public static int getHour(Date date) {
		return getInteger(date, Calendar.HOUR_OF_DAY);
	}

	/**
	 * 获取日期的分钟。失败返回0。
	 *
	 * @param date 日期字符串
	 * @return 分钟
	 */
	public static int getMinute(String date) {
		return getMinute(StringToDate(date));
	}

	/**
	 * 获取日期的分钟。失败返回0。
	 *
	 * @param date 日期
	 * @return 分钟
	 */
	public static int getMinute(Date date) {
		return getInteger(date, Calendar.MINUTE);
	}

	/**
	 * 获取日期的秒钟。失败返回0。
	 *
	 * @param date 日期字符串
	 * @return 秒钟
	 */
	public static int getSecond(String date) {
		return getSecond(StringToDate(date));
	}

	/**
	 * 获取日期的秒钟。失败返回0。
	 *
	 * @param date 日期
	 * @return 秒钟
	 */
	public static int getSecond(Date date) {
		return getInteger(date, Calendar.SECOND);
	}

	/**
	 * 获取日期 。默认yyyy-MM-dd格式。失败返回null。
	 *
	 * @param date 日期字符串
	 * @return 日期
	 */
	public static String getDate(String date) {
		return StringToString(date, DateStyle.YYYY_MM_DD);
	}

	/**
	 * 获取日期。默认yyyy-MM-dd格式。失败返回null。
	 *
	 * @param date 日期
	 * @return 日期
	 */
	public static String getDate(Date date) {
		return DateToString(date, DateStyle.YYYY_MM_DD);
	}

	/**
	 * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
	 *
	 * @param date 日期字符串
	 * @return 时间
	 */
	public static String getTime(String date) {
		return StringToString(date, DateStyle.HH_MM_SS);
	}

	/**
	 * 获取日期的时间。默认HH:mm:ss格式。失败返回null。
	 *
	 * @param date 日期
	 * @return 时间
	 */
	public static String getTime(Date date) {
		return DateToString(date, DateStyle.HH_MM_SS);
	}

	/**
	 * 获取两个日期相差的天数
	 *
	 * @param date 日期字符串
	 * @param otherDate 另一个日期字符串
	 * @return 相差天数。如果失败则返回-1
	 */
	public static int getIntervalDays(String date, String otherDate) {
		return getIntervalDays(StringToDate(date), StringToDate(otherDate));
	}

	/**
	 * @param date 日期
	 * @param otherDate 另一个日期
	 * @return 相差天数。如果失败则返回-1
	 */
	public static int getIntervalDays(Date date, Date otherDate) {
		int num = -1;
		Date dateTmp = DateUtil.StringToDate(DateUtil.getDate(date), DateStyle.YYYY_MM_DD);
		Date otherDateTmp = DateUtil.StringToDate(DateUtil.getDate(otherDate), DateStyle.YYYY_MM_DD);
		if (dateTmp != null && otherDateTmp != null) {
			long time = Math.abs(dateTmp.getTime() - otherDateTmp.getTime());
			num = (int) (time / (24 * 60 * 60 * 1000));
		}
		return num;
	}

	/**
	 * 获得某天的开始时间
	 *
	 * @param date
	 * @return
	 */
	public static Date getDayStart(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		return StringToDate(sdf.format(date));
	}

	/**
	 * 获得某天的结束时间
	 *
	 * @param date
	 * @return
	 */
	public static Date getDayEnd(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		return StringToDate(sdf.format(date));
	}

	/**
	 * 或者本周开始时间
	 *
	 * @return
	 */
	public static Date getCurrentWeekStart(Date date) {
		Map<String, String> map = new HashMap<String, String>();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); // 获取本周一的日期
		return StringToDate(df.format(cal.getTime()));
	}

	/**
	 * 获取本周结束日期
	 *
	 * @return
	 */
	public static Date getCurrentWeekEnd() {
		Map<String, String> map = new HashMap<String, String>();
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		// 增加一个星期，才是我们中国人理解的本周日的日期
		cal.add(Calendar.WEEK_OF_YEAR, 1);
		return StringToDate(df.format(cal.getTime()));
	}

	/**
	 * 或者本月开始时间
	 *
	 * @return
	 */
	public static Date getCurrentMonthStart() {
		Calendar cal = Calendar.getInstance();// 获取当前日期
		cal.add(Calendar.MONTH, 0);
		cal.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		return StringToDate(df.format(cal.getTime()));
	}

	/**
	 * 获取本月结束日期
	 *
	 * @return
	 */
	public static Date getCurrentMonthEnd() {
		Calendar cal = Calendar.getInstance();
		// 设置为1号,当前日期既为本月第一天
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
		return StringToDate(df.format(cal.getTime()));
	}

	/**
	 * 获得月末时间
	 * @param date
	 * @return
	 */
	public static Date getMonthEnd(Date date){
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(5, cal.getActualMaximum(5));
		return DateUtil.StringToDate(sd.format(cal.getTime()), DateStyle.YYYY_MM_DD);
	}

	/**
	 * 获得某年某月的开始时间
	 *
	 * @param strYear
	 * @param strMonth
	 * @return
	 */
	public static Date getMonthStart(String strYear, String strMonth) {
		Calendar cal = Calendar.getInstance();// 获取当前日期
		cal.set(Calendar.YEAR, Integer.parseInt(strYear));
		cal.set(Calendar.MONTH, Integer.parseInt(strMonth));
		cal.set(Calendar.DAY_OF_MONTH, 1);// 设置为1号,当前日期既为本月第一天
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date firstDate = cal.getTime();

		return StringToDate(df.format(firstDate));
	}

	/**
	 * 获得某年某月的最后的时间
	 *
	 * @param strYear
	 * @param strMonth
	 * @return
	 */
	public static Date getMonthEnd(String strYear, String strMonth) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd 23:59:59");

		Calendar cal = Calendar.getInstance();
		// 不加下面2行，就是取当前时间前一个月的第一天及最后一天
		cal.set(Calendar.YEAR, Integer.parseInt(strYear));
		cal.set(Calendar.MONTH, Integer.parseInt(strMonth));
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date lastDate = cal.getTime();
		return StringToDate(df.format(lastDate));
	}

	/**
	 * 得到某天是周几
	 *
	 * @param “2013-06-03”格式的日期
	 * @return 返回1是星期日、2是星期一、3是星期二、4是星期三、5是星期四、6是星期五、7是星期六
	 */
	public static int getDayofweek(String date) {
		Calendar cal = Calendar.getInstance();

		if ("".equals(date)) {
			cal.setTime(new Date(System.currentTimeMillis()));
		} else {
			cal.setTime(new Date(getDateByStr2(date).getTime()));
		}
		// cal.setFirstDayOfWeek(Calendar.MONDAY);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	private static Date getDateByStr2(String dd) {
		SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		try {
			date = sd.parse(dd);
		} catch (ParseException e) {
			date = null;
			//LogUtils.error(e.getMessage(),e);
		}
		return date;
	}

	/**
	 * 得到某个月的周数
	 *
	 * @param date
	 * @throws Exception
	 */
	public static int getMonthWeek(String date) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date s = sdf.parse(date);
		Calendar ca = Calendar.getInstance();
		ca.setTime(s);
		ca.setFirstDayOfWeek(Calendar.MONDAY);
//		System.out.println(ca.getActualMaximum(Calendar.WEEK_OF_MONTH));
		return ca.getActualMaximum(Calendar.WEEK_OF_MONTH);
	}

	public static List<String> getMonthWithOffset(String month, int offset) {
		List<String> list = null;
		try {
			list = new ArrayList<String>();
			String firstDay = "";
			Date date = new SimpleDateFormat("yyyy-MM").parse(month);// 定义起始日期
			Date now = new Date();
			if (date.after(now)) {
				date = now;
			}
			List<String> mlist = getYearMonthBetweenDate(DateToString(date, "yyyy-MM"), DateToString(now, "yyyy-MM"));

			if (mlist.size() <= offset) {
				int preOffSet = offset * 2 + 1 - mlist.size();
				for (int i = preOffSet; i >= 1; i--) {
					Date d = addMonth(date, i * -1);
					list.add(DateToString(d, "yyyy-MM"));
				}
				list.addAll(mlist);
				return list;
			}

			// 往前数n月
			for (int i = offset; i >= 1; i--) {
				Date d = addMonth(date, i * -1);
				list.add(DateToString(d, "yyyy-MM"));
			}

			list.add(month);

			// 往后数n月
			for (int i = 1; i <= offset; i++) {
				Date d = addMonth(date, i);
				list.add(DateToString(d, "yyyy-MM"));
			}

		} catch (ParseException e) {
			return null;
		}

		return list;
	}

	public static List<String> getYearMonthBetweenDate(String startDate, String endDate) {
		List<String> list = null;
		try {
			list = new ArrayList<String>();

			String firstDay = "";
			String lastDay = "";
			Date d1 = new SimpleDateFormat("yyyy-MM").parse(startDate);// 定义起始日期
			Date d2 = new SimpleDateFormat("yyyy-MM").parse(endDate);// 定义结束日期

			Calendar dd = Calendar.getInstance();// 定义日期实例
			dd.setTime(d1);// 设置日期起始时间
			Calendar cale = Calendar.getInstance();

			Calendar c = Calendar.getInstance();
			c.setTime(d2);

			int startDay = d1.getDate();
			int endDay = d2.getDate();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");

			String keyValueForDate = null;

			while (dd.getTime().compareTo(d2) <= 0) {// 判断是否到结束日期
				keyValueForDate = "";
				cale.setTime(dd.getTime());

				if (dd.getTime().equals(d1)) {
					cale.set(Calendar.DAY_OF_MONTH, dd.getActualMaximum(Calendar.DAY_OF_MONTH));
					keyValueForDate = sdf.format(d1);

				} else if (dd.get(Calendar.MONTH) == d2.getMonth() && dd.get(Calendar.YEAR) == c.get(Calendar.YEAR)) {
					cale.set(Calendar.DAY_OF_MONTH, 1);// 取第一天
					firstDay = sdf.format(cale.getTime());
					keyValueForDate = firstDay;

				} else {
					cale.set(Calendar.DAY_OF_MONTH, 1);// 取第一天
					firstDay = sdf.format(cale.getTime());
					cale.set(Calendar.DAY_OF_MONTH, dd.getActualMaximum(Calendar.DAY_OF_MONTH));
					keyValueForDate = firstDay;

				}
				list.add(keyValueForDate);
				dd.add(Calendar.MONTH, 1);// 进行当前日期月份加1
			}

			if (endDay < startDay) {
				keyValueForDate = "";

				cale.setTime(d2);
				cale.set(Calendar.DAY_OF_MONTH, 1);// 取第一天
				firstDay = sdf.format(cale.getTime());
				keyValueForDate = firstDay;
				list.add(keyValueForDate);
			}
		} catch (ParseException e) {
			return null;
		}

		return list;
	}
	public static Calendar getMonthStartTime(Integer year,Integer month){
		Calendar startDate=Calendar.getInstance();
		startDate.set(Calendar.YEAR, year);
		startDate.set(Calendar.MONTH, month-1);
		startDate.set(Calendar.DAY_OF_MONTH,1);
		startDate.set(Calendar.HOUR_OF_DAY , 0);    //HOUR是12小时制
		startDate.set(Calendar.MINUTE, 0);
		startDate.set(Calendar.SECOND, 0);
		startDate.set(Calendar.MILLISECOND, 0);
		return startDate;
	}
	public static Calendar getMonthEndTime(Integer year,Integer month){
		Calendar endDate=Calendar.getInstance();
		endDate.set(Calendar.YEAR, year);
		endDate.set(Calendar.MONTH, month);
		//endDate.set(Calendar.DAY_OF_MONTH,endDate.getActualMaximum(Calendar.DATE));
		endDate.set(Calendar.DAY_OF_MONTH, 1);
		endDate.add(Calendar.DAY_OF_MONTH, -1);
		endDate.set(Calendar.HOUR_OF_DAY , 0);
		endDate.set(Calendar.MINUTE, 0);
		endDate.set(Calendar.SECOND, 0);
		endDate.set(Calendar.MILLISECOND, 0);
		return endDate;
	}

	/**
	 * 获得两个时间的小时差
	 * @param beginTime
	 * @param endDate
     * @return
     */
	public static BigDecimal getDateTimeDifferenceHours(Date beginTime, Date endDate) {

		double nh = 1000.00 * 60.00 * 60.00;
		// long ns = 1000;
		// 获得两个时间的毫秒时间差异
		long diff = endDate.getTime() - beginTime.getTime();
		return new BigDecimal(diff / nh).setScale(2,   BigDecimal.ROUND_HALF_UP);
	}

	/**
	 * 将java.sql.Timestamp对象转化为String字符串
	 * @param time
	 *            要格式的java.sql.Timestamp对象
	 * @param strFormat
	 *            输出的String字符串格式的限定（如："yyyy-MM-dd HH:mm:ss"）
	 * @return 表示日期的字符串
	 */
	public static String dateToStr(java.sql.Timestamp time, String strFormat) {
		DateFormat df = new SimpleDateFormat(strFormat);
		String str = df.format(time);
		return str;
	}

	public static void main(String[] args) {
		//getMonthWithOffset("2017-03", 3);
		TimeZone tz1 = TimeZone.getTimeZone("GMT");
		Calendar cal1 = Calendar.getInstance(tz1);//原文出自【易百教程】，商业转载请联系作者获得授权，非商业请保留原文链接：https://www.yiibai.com/java/util/calendar_getinstance_zone.html

//		System.out.println(getDateTimeDifferenceHours(
//				DateUtil.StringToDate("2017-10-11 20:09:09", DateStyle.YYYY_MM_DD_HH_MM_SS),
//				DateUtil.StringToDate("2017-10-11 23:41:11", DateStyle.YYYY_MM_DD_HH_MM_SS)));

	}

}