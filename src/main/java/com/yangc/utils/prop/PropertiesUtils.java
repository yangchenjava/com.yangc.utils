package com.yangc.utils.prop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class PropertiesUtils {

	private PropertiesUtils() {
	}

	/**
	 * @功能: 根据name获取properties文件中的value
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午07:01:48
	 * @param filePath properties文件路径(classpath中的相对路径)
	 * @param name
	 * @return
	 */
	public static String getProperty(String filePath, String name) {
		if (StringUtils.isBlank(filePath) || StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("The parameters must not be null");
		}
		return getProperty(filePath, name, null);
	}

	/**
	 * @功能: 根据name获取properties文件中的value, 如果为空返回默认值
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午07:01:48
	 * @param filePath properties文件路径(classpath中的相对路径)
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public static String getProperty(String filePath, String name, String defaultValue) {
		if (StringUtils.isBlank(filePath) || StringUtils.isBlank(name)) {
			throw new IllegalArgumentException("The parameters must not be null");
		}
		Properties prop = new Properties();
		InputStream in = null;
		try {
			in = PropertiesUtils.class.getResourceAsStream(filePath);
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return prop.getProperty(name, defaultValue);
	}

	public static void main(String[] args) {
		System.out.println(PropertiesUtils.getProperty("/jdbc.properties", "oracle.driver"));
	}

}
