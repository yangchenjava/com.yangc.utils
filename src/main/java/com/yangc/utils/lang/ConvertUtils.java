package com.yangc.utils.lang;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

public class ConvertUtils {

	private static final String HEX = "0123456789ABCDEF";

	private ConvertUtils() {
	}

	public static byte[] hexStringToByte(String value) {
		if (StringUtils.isNotEmpty(value)) {
			value = value.toUpperCase();
			char[] hexChars = value.toCharArray();
			byte[] b = new byte[hexChars.length / 2];
			for (int i = 0; i < b.length; i++) {
				byte high = (byte) (HEX.indexOf(hexChars[i * 2]) << 4 & 0xf0);
				byte low = (byte) (HEX.indexOf(hexChars[i * 2 + 1]) & 0xf);
				b[i] = (byte) (high | low);
			}
			return b;
		}
		return null;
	}

	public static String byteToHexString(byte[] b) {
		if (ArrayUtils.isNotEmpty(b)) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < b.length; i++) {
				String s = Integer.toHexString(b[i] & 0xff);
				if (s.length() == 1) {
					sb.append("0");
				}
				sb.append(s);
			}
			return sb.toString().toUpperCase();
		}
		return null;
	}

	public static byte[] longToByte(long value) {
		long tmp = value;
		byte[] b = new byte[8];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) (tmp & 0xff);
			tmp >>= 8;
		}
		return b;
	}

	public static long byteToLong(byte[] b) {
		long value = 0;
		if (ArrayUtils.isNotEmpty(b)) {
			for (int i = 0; i < b.length; i++) {
				value |= ((long) (b[i] & 0xff)) << (i * 8);
			}
		}
		return value;
	}

	public static byte[] intToByte(int value) {
		int tmp = value;
		byte[] b = new byte[4];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) (tmp & 0xff);
			tmp >>= 8;
		}
		return b;
	}

	public static int byteToInt(byte[] b) {
		int value = 0;
		if (ArrayUtils.isNotEmpty(b)) {
			for (int i = 0; i < b.length; i++) {
				value |= ((int) (b[i] & 0xff)) << (i * 8);
			}
		}
		return value;
	}

	public static byte[] shortToByte(short value) {
		short tmp = value;
		byte[] b = new byte[2];
		for (int i = 0; i < b.length; i++) {
			b[i] = (byte) (tmp & 0xff);
			tmp >>= 8;
		}
		return b;
	}

	public static short byteToShort(byte[] b) {
		short value = 0;
		if (ArrayUtils.isNotEmpty(b)) {
			for (int i = 0; i < b.length; i++) {
				value |= ((short) (b[i] & 0xff)) << (i * 8);
			}
		}
		return value;
	}

	public static void main(String[] args) {
		System.out.println(byteToLong(longToByte(2935190718L)));
		System.out.println(byteToInt(intToByte(Integer.MAX_VALUE)));
		System.out.println(byteToShort(shortToByte(Short.MAX_VALUE)));
	}

}
