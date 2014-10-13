package com.yangc.utils.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.yangc.utils.net.FtpUtilsApache;

public class FtpApacheTest {

	public static void main(String[] args) {
		FtpUtilsApache ftp = new FtpUtilsApache();
		FTPClient client = ftp.login("10.23.102.92", 21, "video", "clt");
		System.out.println("mkdir=" + ftp.mkDir(client, "/opt/video", "13718922561"));
		List<String> fileNameList = new ArrayList<String>();
		for (FTPFile file : ftp.getFTPFiles(client, "/opt/video/13718922561")) {
			fileNameList.add(file.getName());
			System.out.println(file.getName() + " == " + file.getSize());
		}
		System.out.println(fileNameList.toString());
		System.out.println(fileNameList.size());
		if (fileNameList.contains("test.mp4")) {
			boolean b = ftp.download(client, Arrays.asList("test.mp4"), "/opt/video/test", "E:/");
			System.out.println(b);
		}
		ftp.logout(client);
	}

}
