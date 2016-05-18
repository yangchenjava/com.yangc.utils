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

public class ZHConverterUtils {

	public enum ConverterType {
		/** 繁体 */
		TRADITIONAL,
		/** 简体 */
		SIMPLIFIED;
	}

	private Properties charMap = new Properties();
	private Set<String> conflictingSets = new HashSet<String>();

	private static final ZHConverterUtils[] converters = new ZHConverterUtils[ConverterType.values().length];
	private static final String[] propertyFiles = new String[ConverterType.values().length];

	static {
		propertyFiles[ConverterType.TRADITIONAL.ordinal()] = "/zh2Hant.properties";
		propertyFiles[ConverterType.SIMPLIFIED.ordinal()] = "/zh2Hans.properties";
	}

	public static ZHConverterUtils getInstance(ZHConverterUtils.ConverterType converterType) {
		int i = converterType.ordinal();
		if (converters[i] == null) {
			synchronized (ZHConverterUtils.class) {
				if (converters[i] == null) {
					converters[i] = new ZHConverterUtils(propertyFiles[i]);
				}
			}
		}
		return converters[i];
	}

	public static String convert(String text, ZHConverterUtils.ConverterType converterType) {
		ZHConverterUtils instance = getInstance(converterType);
		return instance.convert(text);
	}

	private ZHConverterUtils(String propertyFile) {
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
