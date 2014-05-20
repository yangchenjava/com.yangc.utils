package com.yangc.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

public class SFtpUtils {

	private static final int TIMEOUT = 6000;

	/**
	 * @功能: 登录SFTP服务器
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 * @param ipAddress IP地址(192.168.112.128)
	 * @param port 端口(21)
	 * @param username 用户名(root)
	 * @param password 密码(123456)
	 */
	public ChannelSftp login(String ipAddress, int port, String username, String password) {
		JSch jsch = new JSch();
		try {
			Session session = jsch.getSession(username, ipAddress, port);
			session.setPassword(password);
			session.setConfig("StrictHostKeyChecking", "no");
			session.setTimeout(TIMEOUT);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			return (ChannelSftp) channel;
		} catch (JSchException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @功能: 登出SFTP服务器
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 */
	public void logout(ChannelSftp sftp) {
		if (sftp != null) {
			sftp.quit();
			sftp.disconnect();
		}
	}

	/**
	 * @功能: 获取指定路径下的文件名列表
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 */
	public List<String> getFileNameList(ChannelSftp sftp, String path) {
		if (sftp == null || sftp.isClosed()) {
			throw new IllegalArgumentException("ChannelSftp has bean closed!");
		}

		List<String> fileNameList = new ArrayList<String>();
		try {
			Vector<?> vector = sftp.ls(path);
			for (int i = 0; i < vector.size(); i++) {
				LsEntry entry = (LsEntry) vector.get(i);
				fileNameList.add(entry.getFilename());
			}
		} catch (SftpException e) {
			e.printStackTrace();
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
	public boolean mkDir(ChannelSftp sftp, String path, String dirName) {
		if (sftp == null || sftp.isClosed()) {
			throw new IllegalArgumentException("ChannelSftp has bean closed!");
		}

		try {
			sftp.cd(path);
			sftp.mkdir(dirName);
			return true;
		} catch (SftpException e) {
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
	public boolean deleteFile(ChannelSftp sftp, String path, String fileName) {
		if (sftp == null || sftp.isClosed()) {
			throw new IllegalArgumentException("ChannelSftp has bean closed!");
		}

		try {
			sftp.cd(path);
			sftp.rm(fileName);
			return true;
		} catch (SftpException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @功能: 向SFTP服务器上传文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param file 要上传的文件
	 * @param path 上传路径(/var/ftp/pub/)
	 * @param monitor 进度回调
	 */
	public boolean upload(ChannelSftp sftp, File file, String path, SftpProgressMonitor monitor) {
		return this.upload(sftp, Arrays.asList(file), path, monitor);
	}

	/**
	 * @功能: 向SFTP服务器上传文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午02:25:10
	 * @param files 要上传的文件
	 * @param path 上传路径(/var/ftp/pub/)
	 * @param monitor 进度回调
	 */
	public boolean upload(ChannelSftp sftp, List<File> files, String path, SftpProgressMonitor monitor) {
		if (sftp == null || sftp.isClosed()) {
			throw new IllegalArgumentException("ChannelSftp has bean closed!");
		}

		BufferedInputStream bis = null;
		try {
			for (File file : files) {
				bis = new BufferedInputStream(new FileInputStream(file));
				sftp.put(bis, path, monitor);
				bis.close();
				bis = null;
			}
			return true;
		} catch (SftpException e) {
			e.printStackTrace();
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
	 * @功能: 从SFTP服务器下载文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 * @param fileName 要下载的文件名("test_1.txt")
	 * @param srcPath FTP服务器文件的路径(/var/ftp/pub/)
	 * @param destPath 下载后保存的路径(E:/workspace/utils/)
	 * @param monitor 进度回调
	 */
	public boolean download(ChannelSftp sftp, String fileName, String srcPath, String destPath, SftpProgressMonitor monitor) {
		return this.download(sftp, Arrays.asList(fileName), srcPath, destPath, monitor);
	}

	/**
	 * @功能: 从SFTP服务器下载文件
	 * @作者: yangc
	 * @创建日期: 2013-11-21 下午03:31:51
	 * @param fileNames 要下载的文件名(Arrays.asList("test_1.txt", "test_2.txt");)
	 * @param srcPath FTP服务器文件的路径(/var/ftp/pub/)
	 * @param destPath 下载后保存的路径(E:/workspace/utils/)
	 * @param monitor 进度回调
	 */
	public boolean download(ChannelSftp sftp, List<String> fileNames, String srcPath, String destPath, SftpProgressMonitor monitor) {
		if (sftp == null || sftp.isClosed()) {
			throw new IllegalArgumentException("ChannelSftp has bean closed!");
		}

		BufferedOutputStream bos = null;
		try {
			for (String fileName : fileNames) {
				bos = new BufferedOutputStream(new FileOutputStream(destPath + "/" + fileName));
				sftp.get(srcPath + "/" + fileName, bos, monitor);
				bos.close();
				bos = null;
			}
			return true;
		} catch (SftpException e) {
			e.printStackTrace();
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
