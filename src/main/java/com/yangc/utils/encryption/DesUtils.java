package com.yangc.utils.encryption;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.apache.commons.lang3.StringUtils;

public class DesUtils {

	private static final String DES = "DES";
	private static final String UTF_8 = "UTF-8";

	private DesUtils() {
	}

	public static String encode(String str, String key) {
		if (StringUtils.isNotEmpty(str)) {
			try {
				DESKeySpec dks = new DESKeySpec(key.getBytes(UTF_8));
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
				SecretKey securekey = keyFactory.generateSecret(dks);
				Cipher cipher = Cipher.getInstance(DES);
				cipher.init(Cipher.ENCRYPT_MODE, securekey, new SecureRandom());
				byte[] bytes = cipher.doFinal(str.getBytes(UTF_8));
				return Base64Utils.encode(bytes);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static String decode(String str, String key) {
		if (StringUtils.isNotEmpty(str)) {
			try {
				DESKeySpec dks = new DESKeySpec(key.getBytes(UTF_8));
				SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
				SecretKey securekey = keyFactory.generateSecret(dks);
				Cipher cipher = Cipher.getInstance(DES);
				cipher.init(Cipher.DECRYPT_MODE, securekey, new SecureRandom());
				byte[] bytes = cipher.doFinal(Base64Utils.decode2Bytes(str));
				return new String(bytes, UTF_8);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(DesUtils.encode("你好", "1234567a"));
		System.out.println(DesUtils.decode("/MCrZVkAumE=", "1234567a"));
	}

}
