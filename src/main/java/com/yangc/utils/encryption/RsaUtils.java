package com.yangc.utils.encryption;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.StringUtils;

public class RsaUtils {

	private static final String RSA = "RSA";
	// 算法名/工作模式/填充模式
	public static final String RSA_ECB_PKCS5Padding = "RSA/ECB/PKCS1Padding";

	/**
	 * 签名算法
	 */
	public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

	private static final String PUBLIC_KEY = "public";
	private static final String PRIVATE_KEY = "private";

	/**
	 * RSA最大加密明文大小
	 */
	private static final int MAX_ENCRYPT_BLOCK = 117;

	/**
	 * RSA最大解密密文大小
	 */
	private static final int MAX_DECRYPT_BLOCK = 128;

	/**
	 * 生成密钥对
	 */
	public static Map<String, Object> getKeyMap() throws GeneralSecurityException {
		KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(RSA);
		keyPairGen.initialize(1024);
		KeyPair keyPair = keyPairGen.generateKeyPair();
		RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
		RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		Map<String, Object> keyMap = new HashMap<String, Object>();
		keyMap.put(PUBLIC_KEY, publicKey);
		keyMap.put(PRIVATE_KEY, privateKey);
		return keyMap;
	}

	/**
	 * 用私钥对信息生成数字签名
	 * 
	 * @param data 已加密数据
	 * @param privateKey 私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static String sign(byte[] data, String privateKey) throws GeneralSecurityException {
		byte[] keyBytes = Base64Utils.decode2Bytes(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		PrivateKey privateK = keyFactory.generatePrivate(keySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateK);
		signature.update(data);
		return Base64Utils.encode(signature.sign());
	}

	/**
	 * 校验数字签名
	 * 
	 * @param data 已加密数据
	 * @param publicKey 公钥(BASE64编码)
	 * @param sign 数字签名
	 * @return
	 * @throws Exception
	 */
	public static boolean verify(byte[] data, String publicKey, String sign) throws GeneralSecurityException {
		byte[] keyBytes = Base64Utils.decode2Bytes(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		PublicKey publicK = keyFactory.generatePublic(keySpec);
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initVerify(publicK);
		signature.update(data);
		return signature.verify(Base64Utils.decode2Bytes(sign));
	}

	/**
	 * 私钥加密
	 * 
	 * @param data 源数据
	 * @param privateKey 私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPrivateKey(byte[] data, String privateKey) throws GeneralSecurityException, IOException {
		byte[] keyBytes = Base64Utils.decode2Bytes(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		Key privateK = keyFactory.generatePrivate(keySpec);
		Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS5Padding);
		cipher.init(Cipher.ENCRYPT_MODE, privateK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/**
	 * 私钥解密
	 * 
	 * @param encryptedData 已加密数据
	 * @param privateKey 私钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptByPrivateKey(byte[] encryptedData, String privateKey) throws GeneralSecurityException, IOException {
		byte[] keyBytes = Base64Utils.decode2Bytes(privateKey);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		Key privateK = keyFactory.generatePrivate(keySpec);
		Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS5Padding);
		cipher.init(Cipher.DECRYPT_MODE, privateK);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	/**
	 * 公钥加密
	 * 
	 * @param data 源数据
	 * @param publicKey 公钥(BASE64编码)
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptByPublicKey(byte[] data, String publicKey) throws GeneralSecurityException, IOException {
		byte[] keyBytes = Base64Utils.decode2Bytes(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		Key publicK = keyFactory.generatePublic(keySpec);
		// 对数据加密
		Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS5Padding);
		cipher.init(Cipher.ENCRYPT_MODE, publicK);
		int inputLen = data.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段加密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
				cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(data, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_ENCRYPT_BLOCK;
		}
		byte[] encryptedData = out.toByteArray();
		out.close();
		return encryptedData;
	}

	/**
	 * 公钥解密
	 * 
	 * @param encryptedData 已加密数据
	 * @param publicKey 公钥(BASE64编码)
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static byte[] decryptByPublicKey(byte[] encryptedData, String publicKey) throws GeneralSecurityException, IOException {
		byte[] keyBytes = Base64Utils.decode2Bytes(publicKey);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		Key publicK = keyFactory.generatePublic(keySpec);
		Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS5Padding);
		cipher.init(Cipher.DECRYPT_MODE, publicK);
		int inputLen = encryptedData.length;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int offSet = 0;
		byte[] cache;
		int i = 0;
		// 对数据分段解密
		while (inputLen - offSet > 0) {
			if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
				cache = cipher.doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
			} else {
				cache = cipher.doFinal(encryptedData, offSet, inputLen - offSet);
			}
			out.write(cache, 0, cache.length);
			i++;
			offSet = i * MAX_DECRYPT_BLOCK;
		}
		byte[] decryptedData = out.toByteArray();
		out.close();
		return decryptedData;
	}

	/**
	 * 获取私钥
	 * 
	 * @param keyMap 密钥对
	 * @return
	 */
	public static String getPrivateKey(Map<String, Object> keyMap) {
		Key key = (Key) keyMap.get(PRIVATE_KEY);
		return Base64Utils.encode(key.getEncoded());
	}

	/**
	 * 获取公钥
	 * 
	 * @param keyMap 密钥对
	 * @return
	 */
	public static String getPublicKey(Map<String, Object> keyMap) {
		Key key = (Key) keyMap.get(PUBLIC_KEY);
		return Base64Utils.encode(key.getEncoded());
	}

	/**
	 * @功能: 移除微软前导0
	 * @作者: yangc
	 * @创建日期: 2017年2月14日 上午11:37:21
	 * @param data
	 * @return
	 */
	private static byte[] removeMSZero(byte[] data) {
		byte[] b;
		if (data[0] == 0) {
			b = new byte[data.length - 1];
			System.arraycopy(data, 1, b, 0, data.length - 1);
		} else {
			b = data;
		}
		return b;
	}

	/**
	 * 获取私钥(C#格式)
	 * 
	 * @param keyMap 密钥对
	 * @return
	 */
	public static String getPrivateKey2CSharp(Map<String, Object> keyMap) throws GeneralSecurityException {
		Key key = (Key) keyMap.get(PRIVATE_KEY);
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(key.getEncoded());
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		RSAPrivateCrtKey privateK = (RSAPrivateCrtKey) keyFactory.generatePrivate(keySpec);

		StringBuilder sb = new StringBuilder();
		sb.append("<RSAKeyValue>");
		sb.append("<Modulus>" + Base64Utils.encode(removeMSZero(privateK.getModulus().toByteArray())) + "</Modulus>");
		sb.append("<Exponent>" + Base64Utils.encode(removeMSZero(privateK.getPublicExponent().toByteArray())) + "</Exponent>");
		sb.append("<P>" + Base64Utils.encode(removeMSZero(privateK.getPrimeP().toByteArray())) + "</P>");
		sb.append("<Q>" + Base64Utils.encode(removeMSZero(privateK.getPrimeQ().toByteArray())) + "</Q>");
		sb.append("<DP>" + Base64Utils.encode(removeMSZero(privateK.getPrimeExponentP().toByteArray())) + "</DP>");
		sb.append("<DQ>" + Base64Utils.encode(removeMSZero(privateK.getPrimeExponentQ().toByteArray())) + "</DQ>");
		sb.append("<InverseQ>" + Base64Utils.encode(removeMSZero(privateK.getCrtCoefficient().toByteArray())) + "</InverseQ>");
		sb.append("<D>" + Base64Utils.encode(removeMSZero(privateK.getPrivateExponent().toByteArray())) + "</D>");
		sb.append("</RSAKeyValue>");
		return sb.toString();
	}

	/**
	 * 获取公钥(C#格式)
	 * 
	 * @param keyMap 密钥对
	 * @return
	 */
	public static String getPublicKey2CSharp(Map<String, Object> keyMap) throws GeneralSecurityException {
		Key key = (Key) keyMap.get(PUBLIC_KEY);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(key.getEncoded());
		KeyFactory keyFactory = KeyFactory.getInstance(RSA);
		RSAPublicKey publicK = (RSAPublicKey) keyFactory.generatePublic(keySpec);

		StringBuilder sb = new StringBuilder();
		sb.append("<RSAKeyValue>");
		sb.append("<Modulus>" + Base64Utils.encode(removeMSZero(publicK.getModulus().toByteArray())) + "</Modulus>");
		sb.append("<Exponent>" + Base64Utils.encode(removeMSZero(publicK.getPublicExponent().toByteArray())) + "</Exponent>");
		sb.append("</RSAKeyValue>");
		return sb.toString();
	}

	public static void main(String[] args) {
		testPublicEncryptPrivateDecrypt();
		testPrivateEncryptPublicDecrypt();
	}

	/**
	 * 加密
	 * @功能: 加密A要给B发送数据，要想让数据只有B能够解密，而其他人即使获得了数据也无法解密。那么，可以在A发送数据之前用B给的公钥加密，B收到之后就可以用他自己的私钥（也只有B知道）来解密。
	 *      <p style="color: red">
	 *      这种情况下，加密的是公钥，解密的是私钥。
	 *      </p>
	 * @作者: yangc
	 * @创建日期: 2016年9月1日 下午4:17:11
	 */
	static void testPublicEncryptPrivateDecrypt() {
		String data = "hello world";
		try {
			Map<String, Object> keyMap = RsaUtils.getKeyMap();
			byte[] encryptedData = RsaUtils.encryptByPublicKey(StringUtils.getBytesUtf8(data), RsaUtils.getPublicKey(keyMap));
			String base64 = Base64Utils.encode(encryptedData);
			System.out.println(base64 + "\r\n===============");
			byte[] decryptedData = RsaUtils.decryptByPrivateKey(Base64Utils.decode2Bytes(base64), RsaUtils.getPrivateKey(keyMap));
			System.out.println(StringUtils.newStringUtf8(decryptedData) + "\r\n===============");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 数字签名
	 * @功能: A想让网上的人确定某些文件是它发布的，它就可以：先生成自己的公钥，然后发布给其他人；然后在发布文件的时候，添加上自己的签名（可以通过对某个字符串X用A的私钥加密得到Y，将X和Y和文件一块发布）；其他人收到文件之后，会用A的公钥解密Y，得到Z，如果X=Z则说明该文件是A发布的。
	 *      <p style="color: red">
	 *      这种情况下，加密的是私钥，解密的是公钥
	 *      </p>
	 * @作者: yangc
	 * @创建日期: 2016年9月1日 下午4:20:25
	 */
	static void testPrivateEncryptPublicDecrypt() {
		String data = "hello world";
		try {
			Map<String, Object> keyMap = RsaUtils.getKeyMap();
			byte[] encryptedData = RsaUtils.encryptByPrivateKey(StringUtils.getBytesUtf8(data), RsaUtils.getPrivateKey(keyMap));
			System.out.println(Base64Utils.encode(encryptedData) + "\r\n===============");
			String sign = RsaUtils.sign(encryptedData, RsaUtils.getPrivateKey(keyMap));
			System.out.println(sign + "\r\n===============");
			boolean b = RsaUtils.verify(encryptedData, RsaUtils.getPublicKey(keyMap), sign);
			System.out.println(b);
			byte[] decryptedData = RsaUtils.decryptByPublicKey(encryptedData, RsaUtils.getPublicKey(keyMap));
			System.out.println(StringUtils.newStringUtf8(decryptedData) + "\r\n===============");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
