package com.yangc.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FtpUtilsApache {

	private static final String UTF_8 = "UTF-8";

	private static final int TIMEOUT = 6000;

	private static final int BUFFER_SIZE = 1024 * 4;

	/**
	 * @功能: 登录FTP服务器
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 * @param ipAddress IP地址(192.168.112.128)
	 * @param port 端口(21)
	 * @param username 用户名(root)
	 * @param password 密码(123456)
	 */
	public FTPClient login(String ipAddress, int port, String username, String password) {
		FTPClient client = new FTPClient();
		client.setDefaultTimeout(TIMEOUT);
		client.setConnectTimeout(TIMEOUT);
		client.setDataTimeout(TIMEOUT);
		client.setBufferSize(BUFFER_SIZE);
		client.setControlEncoding(UTF_8);
		try {
			client.connect(ipAddress, port);
			client.login(username, password);
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return client;
	}

	/**
	 * @功能: 登出FTP服务器
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 */
	public void logout(FTPClient client) {
		if (client != null) {
			try {
				client.logout();
				client.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @功能: 获取指定路径下的文件列表
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 */
	public FTPFile[] getFTPFiles(FTPClient client, String path) {
		if (client == null || !client.isConnected()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		try {
			return client.listFiles(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new FTPFile[] {};
	}

	/**
	 * @功能: 在指定目录下创建目录
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param path 在哪个目录下创建
	 * @param dirName 要创建的目录名称
	 */
	public boolean mkDir(FTPClient client, String path, String dirName) {
		if (client == null || !client.isConnected()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		try {
			client.changeWorkingDirectory(path);
			return client.makeDirectory(dirName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @功能: 在指定目录下删除文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param path 在哪个目录下删除
	 * @param fileName 要删除的文件名称
	 */
	public boolean deleteFile(FTPClient client, String path, String fileName) {
		if (client == null || !client.isConnected()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		try {
			client.changeWorkingDirectory(path);
			return client.deleteFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @功能: 向FTP服务器上传文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param file 要上传的文件
	 * @param path 上传路径(/var/ftp/pub/)
	 */
	public boolean upload(FTPClient client, File file, String path) {
		return this.upload(client, Arrays.asList(file), path);
	}

	/**
	 * @功能: 向FTP服务器上传文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param files 要上传的文件
	 * @param path 上传路径(/var/ftp/pub/)
	 */
	public boolean upload(FTPClient client, List<File> files, String path) {
		if (client == null || !client.isConnected()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		BufferedInputStream bis = null;
		try {
			client.changeWorkingDirectory(path);
			client.setFileType(FTPClient.BINARY_FILE_TYPE);
			for (File file : files) {
				bis = new BufferedInputStream(new FileInputStream(file));
				client.storeFile(file.getName(), bis);
				bis.close();
				bis = null;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 从FTP服务器下载文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 * @param fileName 要下载的文件名("test_1.txt")
	 * @param srcPath FTP服务器文件的路径(/var/ftp/pub/)
	 * @param destPath 下载后保存的路径(E:/workspace/utils/)
	 */
	public boolean download(FTPClient client, String fileName, String srcPath, String destPath) {
		return this.download(client, Arrays.asList(fileName), srcPath, destPath);
	}

	/**
	 * @功能: 从FTP服务器下载文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 * @param fileNames 要下载的文件名(Arrays.asList("test_1.txt", "test_2.txt");)
	 * @param srcPath FTP服务器文件的路径(/var/ftp/pub/)
	 * @param destPath 下载后保存的路径(E:/workspace/utils/)
	 */
	public boolean download(FTPClient client, List<String> fileNames, String srcPath, String destPath) {
		if (client == null || !client.isConnected()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		BufferedOutputStream bos = null;
		try {
			client.changeWorkingDirectory(srcPath);
			client.setFileType(FTPClient.BINARY_FILE_TYPE);
			for (String fileName : fileNames) {
				bos = new BufferedOutputStream(new FileOutputStream(destPath + "/" + fileName));
				client.retrieveFile(fileName, bos);
				bos.close();
				bos = null;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
