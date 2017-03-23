package com.yangc.utils.lang;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;

public class PinyinUtils {

	private static final HanyuPinyinOutputFormat DEFAULT_FORMAT = new HanyuPinyinOutputFormat();

	static {
		DEFAULT_FORMAT.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		DEFAULT_FORMAT.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		DEFAULT_FORMAT.setVCharType(HanyuPinyinVCharType.WITH_V);
	}

	private PinyinUtils() {
	}

	/**
	 * @功能: 获取字符串的拼音
	 * @作者: yangc
	 * @创建日期: 2013-11-19 下午11:54:09
	 */
	public static String getPinyin(String chinese) {
		String[] pinyin = { "" };
		if (StringUtils.isNotBlank(chinese)) {
			try {
				char[] nameChar = chinese.toCharArray();
				for (int i = 0; i < nameChar.length; i++) {
					if (nameChar[i] > 128) {
						pinyin = handle(pinyin, PinyinHelper.toHanyuPinyinStringArray(nameChar[i], DEFAULT_FORMAT), false);
					} else {
						pinyin = handle(pinyin, new String[] { CharUtils.toString(nameChar[i]) }, false);
					}
				}
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				e.printStackTrace();
			}
		}
		return StringUtils.join(pinyin, ",");
	}

	/**
	 * @功能: 获取字符串的拼音首字母
	 * @作者: yangc
	 * @创建日期: 2013-11-19 下午11:54:37
	 */
	public static String getPinyinFirst(String chinese) {
		String[] pinyin = { "" };
		if (StringUtils.isNotBlank(chinese)) {
			try {
				char[] nameChar = chinese.toCharArray();
				for (int i = 0; i < nameChar.length; i++) {
					if (nameChar[i] > 128) {
						pinyin = handle(pinyin, PinyinHelper.toHanyuPinyinStringArray(nameChar[i], DEFAULT_FORMAT), true);
					} else {
						pinyin = handle(pinyin, new String[] { CharUtils.toString(nameChar[i]) }, true);
					}
				}
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				e.printStackTrace();
			}
		}
		return StringUtils.join(pinyin, ",");
	}

	private static String[] handle(String[] s1, String[] s2, boolean isFirst) {
		// 去重
		Set<String> set = new HashSet<String>(s2.length);
		CollectionUtils.addAll(set, s2);
		s2 = set.toArray(new String[set.size()]);

		String[] s = new String[s1.length * s2.length];
		int index = 0;
		for (int i = 0; i < s1.length; i++) {
			for (int j = 0; j < s2.length; j++) {
				s[index++] = s1[i] + (isFirst ? s2[j].charAt(0) : s2[j]);
			}
		}
		return s;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(PinyinUtils.getPinyin("长沙市长"));
		System.out.println(PinyinUtils.getPinyinFirst("长沙市长"));
	}

}
