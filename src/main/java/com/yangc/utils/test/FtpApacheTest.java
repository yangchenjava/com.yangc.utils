package com.yangc.utils.test;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

import com.yangc.utils.net.FtpUtilsApache;

public class FtpApacheTest {

	public static void main(String[] args) {
		FtpUtilsApache ftp = new FtpUtilsApache();
		FTPClient client = ftp.login("10.23.101.92", 21, "video", "clt");
		List<String> fileNameList = ftp.getFileNameList(client, "/opt/video/test");
		System.out.println(fileNameList.size());
		if (fileNameList.contains("test.mp4")) {
			boolean b = ftp.download(client, Arrays.asList("test.mp4"), "/opt/video/test", "E:/");
			System.out.println(b);
		}
	}

}
