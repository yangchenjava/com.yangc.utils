package com.yangc.utils.lang;

import java.util.Calendar;
import java.util.Date;

public class DateUtils extends org.apache.commons.lang.time.DateUtils {

	private DateUtils() {
	}

	/**
	 * @功能: 获取相差天数
	 * @作者: yangc
	 * @创建日期: 2013-11-12 下午03:00:30
	 */
	public static long getOffsetDays(Date date1, Date date2) {
		if (date1 == null || date2 == null) {
			throw new IllegalArgumentException("The date must not be null");
		}

		Calendar c1 = Calendar.getInstance();
		c1.setTime(date1);
		c1 = DateUtils.truncate(c1, Calendar.DATE);

		Calendar c2 = Calendar.getInstance();
		c2.setTime(date2);
		c2 = DateUtils.truncate(c2, Calendar.DATE);

		return Math.abs(c1.getTimeInMillis() - c2.getTimeInMillis()) / (1000 * 60 * 60 * 24);
	}

	/**
	 * @功能: 指定日期中月份的最大天数
	 * @作者: yangc
	 * @创建日期: 2013-11-12 下午03:44:16
	 */
	public static int getMaxDaysOfMonth(Date date) {
		if (date == null) {
			throw new IllegalArgumentException("The date must not be null");
		}

		Calendar c = Calendar.getInstance();
		c.setTime(date);
		return c.getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @功能: 当前时间是否为白昼
	 * @作者: yangc
	 * @创建日期: 2013-11-12 下午03:12:18
	 */
	public static boolean isDaytime() {
		Calendar c = Calendar.getInstance();
		int hour = c.get(Calendar.HOUR_OF_DAY);
		if (hour >= 6 && hour <= 17) {
			return true;
		}
		return false;
	}

	/**
	 * @功能: 是否为闰年
	 * @作者: yangc
	 * @创建日期: 2013-11-12 下午03:27:02
	 */
	public static boolean isLeapYear(int year) {
		if (year <= 0) {
			throw new IllegalArgumentException("The year must > 0");
		}
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	public static void main(String[] args) {
		System.out.println(DateUtils.getMaxDaysOfMonth(new Date()));
	}

}
