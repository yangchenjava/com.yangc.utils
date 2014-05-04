package com.yangc.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.ftp.FTPClient;

public class FtpUtilsApache {

	private static final String UTF_8 = "UTF-8";

	private static final int TIMEOUT = 6000;

	private static final int BUFFER_SIZE = 1024 * 4;

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

	public List<String> getFileNameList(FTPClient client, String path) {
		if (client == null || !client.isConnected()) {
			throw new IllegalArgumentException("FtpClient has bean closed!");
		}

		try {
			return Arrays.asList(client.listNames(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ArrayList<String>();
	}

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
