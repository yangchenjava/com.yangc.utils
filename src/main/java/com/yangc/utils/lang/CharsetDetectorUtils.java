package com.yangc.utils.lang;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.mozilla.universalchardet.UniversalDetector;

public class CharsetDetectorUtils {

	private CharsetDetectorUtils() {
	}

	/**
	 * @功能: 编码格式: ANSI, Unicode(UTF-16LE), Unicode big endian(UTF-16BE), UTF-8
	 * @作者: yangc
	 * @创建日期: 2013-2-1 下午02:34:12
	 */
	public static String getCharsetName(File file) {
		byte[] b = new byte[4096];
		int len = -1;
		UniversalDetector detector = new UniversalDetector(null);
		BufferedInputStream bi = null;
		try {
			bi = new BufferedInputStream(new FileInputStream(file));
			// 判断是否查出编码格式
			while ((len = bi.read(b)) != -1 && !detector.isDone()) {
				// 需要检测的文件字节
				detector.handleData(b, 0, len);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != bi) bi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 完成编码格式的判断
		detector.dataEnd();
		// 获取文件编码的名称字符串
		String charsetName = detector.getDetectedCharset();
		// 重置判断文件编码的检测器
		detector.reset();
		return charsetName;
	}

}
