package com.yangc.utils.lang;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class ZHConverter {

	private Properties charMap = new Properties();
	private Set<String> conflictingSets = new HashSet<String>();

	/** 繁体 */
	public static final int TRADITIONAL = 0;
	/** 简体 */
	public static final int SIMPLIFIED = 1;
	private static final int NUM_OF_CONVERTERS = 2;
	private static final ZHConverter[] converters = new ZHConverter[NUM_OF_CONVERTERS];
	private static final String[] propertyFiles = new String[2];

	static {
		propertyFiles[TRADITIONAL] = "/zh2Hant.properties";
		propertyFiles[SIMPLIFIED] = "/zh2Hans.properties";
	}

	/**
	 * @param converterType 0 for traditional and 1 for simplified
	 * @return
	 */
	public static ZHConverter getInstance(int converterType) {
		if (converterType >= 0 && converterType < NUM_OF_CONVERTERS) {
			if (converters[converterType] == null) {
				synchronized (ZHConverter.class) {
					if (converters[converterType] == null) {
						converters[converterType] = new ZHConverter(propertyFiles[converterType]);
					}
				}
			}
			return converters[converterType];
		} else {
			return null;
		}
	}

	public static String convert(String text, int converterType) {
		ZHConverter instance = getInstance(converterType);
		return instance.convert(text);
	}

	private ZHConverter(String propertyFile) {
		InputStream is = this.getClass().getResourceAsStream(propertyFile);
		if (is != null) {
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(is));
				charMap.load(reader);
			} catch (FileNotFoundException e) {
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (reader != null) reader.close();
					if (is != null) is.close();
				} catch (IOException e) {
				}
			}
		}
		initializeHelper();
	}

	private void initializeHelper() {
		Map<String, Integer> stringPossibilities = new HashMap<String, Integer>();
		for (Map.Entry<Object, Object> entry : charMap.entrySet()) {
			String key = (String) entry.getKey();
			for (int i = 0; i < key.length(); i++) {
				String keySubstring = key.substring(0, i + 1);
				if (stringPossibilities.containsKey(keySubstring)) {
					stringPossibilities.put(keySubstring, stringPossibilities.get(keySubstring) + 1);
				} else {
					stringPossibilities.put(keySubstring, 1);
				}
			}
		}

		for (Map.Entry<String, Integer> entry : stringPossibilities.entrySet()) {
			if (entry.getValue() > 1) conflictingSets.add(entry.getKey());
		}
	}

	public String convert(String in) {
		StringBuilder outString = new StringBuilder();
		StringBuilder stackString = new StringBuilder();

		for (int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			String key = "" + c;
			stackString.append(key);

			if (conflictingSets.contains(stackString.toString())) {
			} else if (charMap.containsKey(stackString.toString())) {
				outString.append(charMap.get(stackString.toString()));
				stackString.setLength(0);
			} else {
				CharSequence sequence = stackString.subSequence(0, stackString.length() - 1);
				stackString.delete(0, stackString.length() - 1);
				flushStack(outString, new StringBuilder(sequence));
			}
		}
		flushStack(outString, stackString);
		return outString.toString();
	}

	private void flushStack(StringBuilder outString, StringBuilder stackString) {
		while (stackString.length() > 0) {
			if (charMap.containsKey(stackString.toString())) {
				outString.append(charMap.get(stackString.toString()));
				stackString.setLength(0);
			} else {
				outString.append("" + stackString.charAt(0));
				stackString.delete(0, 1);
			}
		}
	}

}
