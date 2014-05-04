package com.yangc.utils.encryption;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64Utils {

	private Base64Utils() {
	}

	/**
	 * 将字符串进行Base64编码
	 * @param str 字符串
	 * @return 编码后的字符串
	 */
	public static String encode(String str) {
		if (StringUtils.isNotEmpty(str)) {
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(str.getBytes());
		}
		return null;
	}

	/**
	 * 将数据进行Base64解码
	 * @param str 字符串
	 * @return 解码后的字符串
	 */
	public static String decode(String str) {
		if (StringUtils.isNotEmpty(str)) {
			BASE64Decoder decoder = new BASE64Decoder();
			try {
				return new String(decoder.decodeBuffer(str));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(Base64Utils.encode("yangc"));
		System.out.println(Base64Utils.decode("eWFuZ2M="));
	}

}
