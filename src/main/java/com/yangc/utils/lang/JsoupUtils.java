package com.yangc.utils.lang;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

public class JsoupUtils {

	private JsoupUtils() {
	}

	/**
	 * @功能: 过滤非法标签
	 * @作者: yangc
	 * @创建日期: 2014年8月14日 下午6:38:31
	 * @param html
	 * @return
	 */
	public static String safe(String html) {
		return Jsoup.clean(html, Whitelist.relaxed().addTags("label", "span", "div"));
	}

	/**
	 * @功能: 过滤html标签
	 * @作者: yangc
	 * @创建日期: 2014年7月24日 下午3:00:26
	 * @param content
	 * @return
	 */
	public static String filterHtml(String html) {
		if (StringUtils.isNotBlank(html)) {
			return Jsoup.clean(html, Whitelist.none()).trim();
		}
		return null;
	}

	/**
	 * @功能: 过滤指定html标签
	 * @作者: yangc
	 * @创建日期: 2014年7月24日 下午3:00:49
	 * @param html
	 * @param tag
	 * @return
	 */
	public static String filterHtml(String html, String tag) {
		if (StringUtils.isNotBlank(html)) {
			if (StringUtils.isBlank(tag)) {
				return filterHtml(html);
			} else {
				Document doc = Jsoup.parse(html);
				for (Element el : doc.select(tag)) {
					html = html.replace(el.outerHtml(), el.text());
				}
				return html.trim();
			}
		}
		return null;
	}

	/**
	 * @功能: 根据属性名获取属性值
	 * @作者: yangc
	 * @创建日期: 2014年8月18日 上午10:54:34
	 * @param html
	 * @param tag
	 * @param attributeKey
	 * @return
	 */
	public static List<String> getAttributeValue(String html, String tag, String attributeKey) {
		if (StringUtils.isNotBlank(html) && StringUtils.isNotBlank(tag) && StringUtils.isNotBlank(attributeKey)) {
			Document doc = Jsoup.parse(html);
			List<String> list = new ArrayList<String>();
			for (Element el : doc.select(tag)) {
				String attributeValue = el.attr(attributeKey);
				if (StringUtils.isNotBlank(attributeValue)) {
					list.add(attributeValue);
				}
			}
			return list;
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
			System.out.println(filterHtml(sb.toString()));
			System.out.println(Arrays.toString(getAttributeValue(sb.toString(), "span", "style").toArray()));
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
