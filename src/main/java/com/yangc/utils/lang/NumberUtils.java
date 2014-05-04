package com.yangc.utils.lang;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

public class NumberUtils extends org.apache.commons.lang.math.NumberUtils {

	private static final double KB = 1024D;
	private static final double MB = 1024D * 1024D;
	private static final double GB = 1024D * 1024D * 1024D;
	private static final double TB = 1024D * 1024D * 1024D * 1024D;

	private NumberUtils() {
	}

	/**
	 * @功能: 根据文件字节数(B)计算文件大小
	 * @作者: yangc
	 * @创建日期: 2012-12-18 上午11:52:35
	 */
	public static String getSize(long size) {
		if (size < 0) {
			throw new IllegalArgumentException("The size must >= 0");
		}

		String unit = "";
		if (size <= KB) {
			unit = size + "B";
		} else if (size <= MB) {
			unit = String.format("%.2f", size / KB) + "KB";
		} else if (size <= GB) {
			unit = String.format("%.2f", size / MB) + "MB";
		} else if (size <= TB) {
			unit = String.format("%.2f", size / GB) + "GB";
		} else {
			unit = "" + size;
		}
		return unit;
	}

	/**
	 * @功能: 计算比例(1280x720 -> 16:9)
	 * @作者: yangc
	 * @创建日期: 2013-2-15 下午10:03:09
	 */
	public static int[] getScale(int[] dataSet) {
		if (dataSet == null || dataSet.length < 2) {
			throw new IllegalArgumentException("The dataSet's length must >= 2");
		}

		dataSet = Arrays.copyOf(dataSet, dataSet.length);
		runScale(dataSet, 2);
		runScale(dataSet, 3);
		runScale(dataSet, 5);
		runScale(dataSet, 7);
		runScale(dataSet, 11);
		runScale(dataSet, 13);
		runScale(dataSet, 17);
		runScale(dataSet, 19);
		return dataSet;
	}

	private static void runScale(int[] dataSet, int ratio) {
		boolean b = true;
		while (b) {
			for (int i = 0; i < dataSet.length; i++) {
				if (dataSet[i] % ratio != 0) b = false;
			}
			if (b) {
				for (int i = 0; i < dataSet.length; i++) {
					dataSet[i] /= ratio;
				}
			}
		}
	}

	/**
	 * @功能: 加
	 * @作者: yangc
	 * @创建日期: 2013-4-27 下午1:28:19
	 */
	public static double add(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.add(b2).doubleValue();
	}

	/**
	 * @功能: 减
	 * @作者: yangc
	 * @创建日期: 2013-4-27 下午1:28:19
	 */
	public static double subtract(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.subtract(b2).doubleValue();
	}

	/**
	 * @功能: 乘
	 * @作者: yangc
	 * @创建日期: 2013-4-27 下午1:28:19
	 */
	public static double multiply(double v1, double v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.multiply(b2).doubleValue();
	}

	/**
	 * @功能: 除,小数点后保留几位
	 * @作者: yangc
	 * @创建日期: 2013-4-27 下午1:27:34
	 */
	public static double divide(double v1, double v2, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b1 = new BigDecimal(Double.toString(v1));
		BigDecimal b2 = new BigDecimal(Double.toString(v2));
		return b1.divide(b2, scale, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * @功能: 四舍五入,小数点后保留几位
	 * @作者: yangc
	 * @创建日期: 2013-4-27 下午1:23:10
	 */
	public static double round(double v, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b = new BigDecimal(Double.toString(v));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public static void main(String[] args) {
		System.out.println("max=" + NumberUtils.getSize(Runtime.getRuntime().maxMemory()));
		System.out.println("total=" + NumberUtils.getSize(Runtime.getRuntime().totalMemory()));
		System.out.println("free=" + NumberUtils.getSize(Runtime.getRuntime().freeMemory()));
		System.out.println(NumberUtils.getSize(2935190718L));
		System.out.println(NumberUtils.add(16, 4));
		System.out.println(NumberUtils.subtract(16, 4));
		System.out.println(NumberUtils.multiply(16, 4));
		System.out.println(NumberUtils.divide(16, 4, 2));
		System.out.println(NumberUtils.divide(16, 456, 2));
		System.out.println(NumberUtils.round(16.2345, 2));
	}

}
