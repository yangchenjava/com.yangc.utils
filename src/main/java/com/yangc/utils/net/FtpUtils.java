package com.yangc.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sun.net.TelnetInputStream;
import sun.net.TelnetOutputStream;
import sun.net.ftp.FtpClient;

@SuppressWarnings("restriction")
public class FtpUtils {

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
	public FtpClient login(String ipAddress, int port, String username, String password) {
		FtpClient client = new FtpClient();
		client.setConnectTimeout(TIMEOUT);
		client.setReadTimeout(TIMEOUT);

		try {
			client.openServer(ipAddress, port);
			client.login(username, password);
			client.binary(); // 使用二进制模式上传下载
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
	public void logout(FtpClient client) {
		try {
			if (client != null) {
				client.closeServer();
			}
		} catch (IOException e) {
			e.printStackTrace();
			try {
				client.ascii();
				client.sendServer("QUIT\r\n");
				client.readServerResponse();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	/**
	 * @功能: 获取指定路径下的文件名列表
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 */
	public List<String> getFileNameList(FtpClient client, String path) {
		if (client == null || !client.serverIsOpen()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		List<String> fileNameList = new ArrayList<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(client.nameList(path)));
			String fileName = null;
			while ((fileName = br.readLine()) != null) {
				fileNameList.add(fileName);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return fileNameList;
	}

	/**
	 * @功能: 在指定目录下创建目录
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param path 在哪个目录下创建
	 * @param dirName 要创建的目录名称
	 */
	public boolean mkDir(FtpClient client, String path, String dirName) {
		if (client == null || !client.serverIsOpen()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		try {
			client.cd(path);
			client.ascii(); // 使用ascii模式发送命令
			client.sendServer("MKD " + dirName + "\r\n");
			client.readServerResponse();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				client.binary();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	public boolean deleteFile(FtpClient client, String path, String fileName) {
		if (client == null || !client.serverIsOpen()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		try {
			client.cd(path);
			client.ascii(); // 使用ascii模式发送命令
			client.sendServer("DELE " + fileName + "\r\n");
			client.readServerResponse();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				client.binary();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	public boolean upload(FtpClient client, File file, String path) {
		return this.upload(client, Arrays.asList(file), path);
	}

	/**
	 * @功能: 向FTP服务器上传文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param files 要上传的文件
	 * @param path 上传路径(/var/ftp/pub/)
	 */
	public boolean upload(FtpClient client, List<File> files, String path) {
		if (client == null || !client.serverIsOpen()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		int len = -1;
		byte[] b = null;
		BufferedInputStream bis = null;
		TelnetOutputStream tos = null;
		try {
			client.cd(path);
			for (File file : files) {
				len = -1;
				b = new byte[BUFFER_SIZE];
				bis = new BufferedInputStream(new FileInputStream(file));
				tos = client.put(file.getName());
				while ((len = bis.read(b)) != -1) {
					tos.write(b, 0, len);
				}
				tos.flush();
				tos.close();
				tos = null;
				bis.close();
				bis = null;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (tos != null) tos.close();
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
	public boolean download(FtpClient client, String fileName, String srcPath, String destPath) {
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
	public boolean download(FtpClient client, List<String> fileNames, String srcPath, String destPath) {
		if (client == null || !client.serverIsOpen()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		int len = -1;
		byte[] b = null;
		TelnetInputStream tis = null;
		BufferedOutputStream bos = null;
		try {
			client.cd(srcPath);
			for (String fileName : fileNames) {
				len = -1;
				b = new byte[BUFFER_SIZE];
				tis = client.get(fileName);
				bos = new BufferedOutputStream(new FileOutputStream(destPath + "/" + fileName));
				while ((len = tis.read(b)) != -1) {
					bos.write(b, 0, len);
				}
				bos.flush();
				bos.close();
				bos = null;
				tis.close();
				tis = null;
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) bos.close();
				if (tis != null) tis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
