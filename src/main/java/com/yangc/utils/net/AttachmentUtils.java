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

	/**
	 * @功能: 根据不同浏览器防止下载的附件名称乱码
	 * @作者: yangc
	 * @创建日期: 2016年4月26日 上午10:13:15
	 * @param request
	 * @param response
	 * @param fileName
	 */
	public static void convertAttachmentFileName(HttpServletRequest request, HttpServletResponse response, String fileName) {
		if (StringUtils.isNotBlank(fileName)) {
			try {
				fileName = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
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
					// Chrome浏览器只能采用MimeUtility编码或ISO编码的中文输出
					else if (userAgent.indexOf("chrome") != -1) {
						fileName = "filename=\"" + MimeUtility.encodeText(fileName, "UTF-8", "B") + "\"";
					}
					// Safari浏览器只能采用ISO编码的中文输出
					else if (userAgent.indexOf("safari") != -1) {
						fileName = "filename=\"" + new String(fileName.getBytes("UTF-8"), "ISO-8859-1") + "\"";
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

}
