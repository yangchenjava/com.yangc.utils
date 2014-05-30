package com.yangc.utils.prop;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

public class PropertiesUtils {

	private static final String UTF_8 = "UTF-8";

	private static String FILE_PATH = "/jdbc.properties";
	private static final Properties prop = new Properties();

	private static PropertiesUtils propertiesUtils;

	private PropertiesUtils() {
	}

	/**
	 * @功能: 获取PropertiesUtils实例
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午06:59:57
	 * @param filePath properties文件路径(classpath中的相对路径)
	 * @return
	 */
	public synchronized static PropertiesUtils getInstance(String filePath) {
		if (propertiesUtils == null || !StringUtils.equals(filePath, FILE_PATH)) {
			if (StringUtils.isNotBlank(filePath)) {
				FILE_PATH = filePath;
			}
			try {
				prop.load(PropertiesUtils.class.getResourceAsStream(FILE_PATH));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			propertiesUtils = new PropertiesUtils();
		}
		return propertiesUtils;
	}

	/**
	 * @功能: 根据name获取properties文件中的value
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午07:01:48
	 * @param name
	 * @return
	 */
	public String getProperty(String name) {
		if (prop != null && !prop.isEmpty()) {
			return prop.getProperty(name);
		}
		return null;
	}

	/**
	 * @功能: 根据name获取properties文件中的value, 如果为空返回默认值
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午07:01:48
	 * @param name
	 * @param defaultValue
	 * @return
	 */
	public String getProperty(String name, String defaultValue) {
		if (StringUtils.isBlank(defaultValue)) {
			throw new IllegalArgumentException("The defaultValue must not be null");
		}
		if (prop != null && !prop.isEmpty()) {
			String value = prop.getProperty(name);
			return value == null ? defaultValue : value;
		}
		return defaultValue;
	}

	/**
	 * @功能: properties文件中的name,value (已过期,会打乱properties文件中顺序)
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午07:02:38
	 * @param name
	 * @param value
	 */
	@Deprecated
	public void setProperty(String name, String value) {
		if (prop != null) {
			prop.put(name, value);
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(PropertiesUtils.class.getResource(FILE_PATH).getFile()), UTF_8));
				prop.store(bw, "保存properties配置文件");
				bw.close();
				bw = null;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (bw != null) bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) {
		PropertiesUtils p = PropertiesUtils.getInstance("/application.properties");
		p.setProperty("yangc", "yangchen");
		System.out.println(p.getProperty("oracle.url"));
	}

}
