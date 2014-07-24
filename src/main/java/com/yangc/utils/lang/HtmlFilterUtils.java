package com.yangc.utils.lang;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class HtmlFilterUtils {

	private HtmlFilterUtils() {
	}

	/**
	 * @功能: 根据正则过滤内容
	 * @作者: yangc
	 * @创建日期: 2014年7月24日 下午3:15:24
	 * @param content
	 * @param regex
	 * @return
	 */
	private static String filter(String content, String regex) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, "");
		}
		matcher.appendTail(sb);
		return sb.toString().trim();
	}

	/**
	 * @功能: 过滤html标签
	 * @作者: yangc
	 * @创建日期: 2014年7月24日 下午3:00:26
	 * @param content
	 * @return
	 */
	public static String filterHtml(String content) {
		if (StringUtils.isNotBlank(content)) {
			return filter(content, "<([^>]*)>");
		}
		return null;
	}

	/**
	 * @功能: 过滤指定html标签
	 * @作者: yangc
	 * @创建日期: 2014年7月24日 下午3:00:49
	 * @param content
	 * @param tag
	 * @return
	 */
	public static String filterHtml(String content, String tag) {
		if (StringUtils.isNotBlank(content)) {
			if (StringUtils.isBlank(tag)) {
				return filter(content, "<([^>]*)>");
			} else {
				return filter(content, "<\\s*(/?)\\s*" + tag + "\\s*([^>]*)\\s*>");
			}
		}
		return null;
	}

	public static void main(String[] args) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("src/main/resources/test.txt"), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String str = null;
			while ((str = br.readLine()) != null) {
				sb.append(str);
			}
			br.close();
			br = null;
			System.out.println(HtmlFilterUtils.filterHtml(sb.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
