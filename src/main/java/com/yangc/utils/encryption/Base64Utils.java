package com.yangc.utils.encryption;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;

public class Base64Utils {

	private Base64Utils() {
	}

	/**
	 * @功能: 将字节数组进行Base64编码
	 * @作者: yangc
	 * @创建日期: 2016年9月1日 下午2:56:56
	 * @param data
	 * @return
	 */
	public static String encode(byte[] data) {
		return Base64.encodeBase64String(data);
	}

	/**
	 * 将字符串进行Base64编码
	 * @param data 字符串
	 * @return 编码后的字符串
	 */
	public static String encode(String data) {
		if (data != null && data.length() > 0) {
			// BASE64Encoder encoder = new BASE64Encoder();
			// encoder.encode(data.getBytes());
			return Base64.encodeBase64String(StringUtils.getBytesUtf8(data));
		}
		return null;
	}

	/**
	 * @功能: 将数据进行Base64解码
	 * @作者: yangc
	 * @创建日期: 2016年9月1日 下午3:12:08
	 * @param data
	 * @return
	 */
	public static byte[] decode2Bytes(String data) {
		if (data != null && data.length() > 0) {
			return Base64.decodeBase64(data);
		}
		return null;
	}

	/**
	 * 将数据进行Base64解码
	 * @param data 字符串
	 * @return 解码后的字符串
	 */
	public static String decode2String(String data) {
		if (data != null && data.length() > 0) {
			// BASE64Decoder decoder = new BASE64Decoder();
			// decoder.decodeBuffer(data);
			return StringUtils.newStringUtf8(Base64.decodeBase64(data));
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(Base64Utils.encode("yangc"));
		System.out.println(Base64Utils.decode2String("eWFuZ2M="));
	}

}
