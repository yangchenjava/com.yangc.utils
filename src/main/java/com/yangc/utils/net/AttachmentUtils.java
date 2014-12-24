package com.yangc.utils.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.mail.internet.MimeUtility;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

public class AttachmentUtils {

	private AttachmentUtils() {
	}

	public static void convertAttachmentFileName(HttpServletRequest request, HttpServletResponse response, String fileName) {
		try {
			fileName = URLEncoder.encode(fileName, "UTF-8");
			String userAgent = request.getHeader("User-Agent");
			if (StringUtils.isNotBlank(userAgent)) {
				userAgent = userAgent.toLowerCase();
				// IE浏览器只能采用URLEncoder编码
				if (userAgent.indexOf("msie") != -1) {
					fileName = "filename=\"" + fileName + "\"";
				}
				// Opera浏览器只能采用filename*
				else if (userAgent.indexOf("opera") != -1) {
					fileName = "filename*=UTF-8''" + fileName;
				}
				// Safari浏览器只能采用ISO编码的中文输出
				else if (userAgent.indexOf("safari") != -1) {
					fileName = "filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO8859-1") + "\"";
				}
				// Chrome浏览器只能采用MimeUtility编码或ISO编码的中文输出
				else if (userAgent.indexOf("applewebkit") != -1) {
					fileName = "filename=\"" + MimeUtility.encodeText(fileName, "UTF-8", "B") + "\"";
				}
				// FireFox浏览器可以使用MimeUtility或filename*或ISO编码的中文输出
				else if (userAgent.indexOf("mozilla") != -1) {
					fileName = "filename*=UTF-8''" + fileName;
				}
			}
			response.setHeader("Content-Disposition", "attachment; " + fileName);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

}
