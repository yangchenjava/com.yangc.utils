package com.yangc.utils.lang;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.apache.commons.lang3.StringUtils;

public class PinyinUtils {

	private PinyinUtils() {
	}

	/**
	 * @功能: 获取字符串的拼音
	 * @作者: yangc
	 * @创建日期: 2013-11-19 下午11:54:09
	 */
	public static String getPinyin(String chinese) {
		String pinyin = "";
		if (StringUtils.isNotBlank(chinese)) {
			char[] nameChar = chinese.toCharArray();
			HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
			defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			try {
				for (int i = 0; i < nameChar.length; i++) {
					if (nameChar[i] > 128) {
						pinyin += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0];
					} else {
						pinyin += nameChar[i];
					}
				}
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				e.printStackTrace();
			}
		}
		return pinyin;
	}

	/**
	 * @功能: 获取字符串的拼音首字母
	 * @作者: yangc
	 * @创建日期: 2013-11-19 下午11:54:37
	 */
	public static String getPinyinHead(String chinese) {
		String pinyin = "";
		if (StringUtils.isNotBlank(chinese)) {
			char[] nameChar = chinese.toCharArray();
			HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
			defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
			defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			try {
				for (int i = 0; i < nameChar.length; i++) {
					if (nameChar[i] > 128) {
						pinyin += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
					} else {
						pinyin += nameChar[i];
					}
				}
			} catch (BadHanyuPinyinOutputFormatCombination e) {
				e.printStackTrace();
			}
		}
		return pinyin;
	}

	public static void main(String[] args) {
		System.out.println(PinyinUtils.getPinyin("杨晨"));
		System.out.println(PinyinUtils.getPinyinHead("杨晨"));
	}

}
