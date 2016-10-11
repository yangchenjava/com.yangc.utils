package com.yangc.utils.encryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.StringUtils;

public class AesUtils {

	private static final String AES = "AES";
	// 随机算法
	private static final String SHA1PRNG = "SHA1PRNG";
	private static final String AES_CBC_PKCS5Padding = "AES/CBC/PKCS5Padding";
	// 密钥偏移量(非ECB模式下)
	private static final String IV = "1234567890123456";
	private static final String UTF_8 = "UTF-8";

	private AesUtils() {
	}

	public static String encode(String str, String key) {
		if (StringUtils.isNotEmpty(str)) {
			try {
				SecureRandom random = SecureRandom.getInstance(SHA1PRNG);
				random.setSeed(key.getBytes(UTF_8));
				KeyGenerator kgen = KeyGenerator.getInstance(AES);
				kgen.init(128, random);
				SecretKeySpec skeySpec = new SecretKeySpec(kgen.generateKey().getEncoded(), AES);
				Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5Padding);
				cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(IV.getBytes(UTF_8)));
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
				SecureRandom random = SecureRandom.getInstance(SHA1PRNG);
				random.setSeed(key.getBytes(UTF_8));
				KeyGenerator kgen = KeyGenerator.getInstance(AES);
				kgen.init(128, random);
				SecretKeySpec skeySpec = new SecretKeySpec(kgen.generateKey().getEncoded(), AES);
				Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5Padding);
				cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(IV.getBytes(UTF_8)));
				byte[] bytes = cipher.doFinal(Base64Utils.decode2Bytes(str));
				return new String(bytes, UTF_8);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static void encodeFile(String srcFilePath, String destFilePath, String key) {
		InputStream in = null;
		OutputStream out = null;
		try {
			SecureRandom random = SecureRandom.getInstance(SHA1PRNG);
			random.setSeed(key.getBytes(UTF_8));
			KeyGenerator kgen = KeyGenerator.getInstance(AES);
			kgen.init(128, random);
			SecretKeySpec skeySpec = new SecretKeySpec(kgen.generateKey().getEncoded(), AES);
			Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5Padding);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, new IvParameterSpec(IV.getBytes(UTF_8)));

			in = new FileInputStream(srcFilePath);
			out = new FileOutputStream(validateFile(destFilePath));
			crypto(in, out, cipher);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void decodeFile(String srcFilePath, String destFilePath, String key) {
		InputStream in = null;
		OutputStream out = null;
		try {
			SecureRandom random = SecureRandom.getInstance(SHA1PRNG);
			random.setSeed(key.getBytes(UTF_8));
			KeyGenerator kgen = KeyGenerator.getInstance(AES);
			kgen.init(128, random);
			SecretKeySpec skeySpec = new SecretKeySpec(kgen.generateKey().getEncoded(), AES);
			Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5Padding);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, new IvParameterSpec(IV.getBytes(UTF_8)));

			in = new FileInputStream(srcFilePath);
			out = new FileOutputStream(validateFile(destFilePath));
			crypto(in, out, cipher);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static File validateFile(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
		if (file.exists()) file.delete();
		file.createNewFile();
		return file;
	}

	private static void crypto(InputStream in, OutputStream out, Cipher cipher) throws IOException, GeneralSecurityException {
		int blockSize = cipher.getBlockSize() * 1000;
		int outputSize = cipher.getOutputSize(blockSize);

		byte[] inBytes = new byte[blockSize];
		byte[] outBytes = new byte[outputSize];

		int inLength = -1;
		boolean more = true;
		while (more) {
			inLength = in.read(inBytes);
			if (inLength == blockSize) {
				int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
				out.write(outBytes, 0, outLength);
			} else {
				more = false;
			}
		}
		if (inLength > 0) {
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		} else {
			outBytes = cipher.doFinal();
		}
		out.write(outBytes);
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
		System.out.println(AesUtils.encode("你好", "123456"));
		System.out.println(AesUtils.decode("Hu9mVG0CXIVEOXYB5iw5KA==", "123456"));
		System.out.println(AesUtils.getRandomKey(5));
		AesUtils.encodeFile("E:/settings_localhost.xml", "E:/dd.xml", "123456");
		AesUtils.decodeFile("E:/dd.xml", "E:/result.xml", "123456");
	}

}
