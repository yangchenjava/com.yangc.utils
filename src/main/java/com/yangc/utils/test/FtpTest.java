package com.yangc.utils.test;

import java.util.Arrays;
import java.util.List;

import sun.net.ftp.FtpClient;

import com.yangc.utils.net.FtpUtils;

public class FtpTest {

	public static void main(String[] args) {
		FtpUtils ftp = new FtpUtils();
		FtpClient client = ftp.login("10.23.101.92", 21, "video", "clt");

		List<String> fileNameList = ftp.getFileNameList(client, "/opt/video");
		for (String fileName : fileNameList) {
			System.out.println(fileName);
		}

		// boolean b = ftp.mkDir(client, "/opt/video", "test");
		// if (b) System.out.println("文件夹创建成功");

		// boolean b = ftp.deleteFile(client, "/opt/video/test", "2010-01-04-13-43-51.mp4");
		// if (b) System.out.println("文件删除成功");

		// List<File> files = Arrays.asList(new File("src/main/resources/2010-01-04-13-43-51.mp4"));
		// boolean b = ftp.upload(client, files, "/opt/video/test");
		// if (b) System.out.println("文件上传成功");

		List<String> fileNames = Arrays.asList("2010-01-04-13-43-51.mp4");
		boolean b = ftp.download(client, fileNames, "/opt/video/test", "F:/");
		if (b) System.out.println("文件下载成功");

		ftp.logout(client);
	}

}
