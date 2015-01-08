package com.yangc.utils.encryption;

import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

import com.yangc.utils.lang.ConvertUtils;

public class AesUtils {

	private static final String AES_PASSWORD = "1234567890abcdef";

	private static final String UTF_8 = "UTF-8";

	private AesUtils() {
	}

	public static String encode(String str) {
		if (StringUtils.isNotEmpty(str)) {
			try {
				KeyGenerator kgen = KeyGenerator.getInstance("AES");
				kgen.init(128, new SecureRandom(AES_PASSWORD.getBytes(UTF_8)));
				SecretKey skey = kgen.generateKey();
				byte[] raw = skey.getEncoded();
				SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
				byte[] bytes = cipher.doFinal(str.getBytes(UTF_8));
				return ConvertUtils.byteToHexString(bytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String decode(String str) {
		if (StringUtils.isNotEmpty(str)) {
			try {
				KeyGenerator kgen = KeyGenerator.getInstance("AES");
				kgen.init(128, new SecureRandom(AES_PASSWORD.getBytes(UTF_8)));
				SecretKey skey = kgen.generateKey();
				byte[] raw = skey.getEncoded();
				SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, skeySpec);
				byte[] bytes = cipher.doFinal(ConvertUtils.hexStringToByte(str));
				return new String(bytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 生一个长度为len的随机数
	 * @param int len 生成的随机密码的长度
	 * @return String pwd 返回生成的随机密码
	 */
	public static String getRandomKey(int len) {
		if (len >= 0) {
			Random r = new Random();
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < len; i++) {
				sb.append(r.nextInt(10));
			}
			return sb.toString();
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(AesUtils.encode("你好"));
		System.out.println(AesUtils.decode("4D74B44A332CE88D9B3BD2431EB5FC73"));
		System.out.println(AesUtils.getRandomKey(5));
	}

}
