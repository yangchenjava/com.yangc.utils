package com.yangc.utils.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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

	public ChannelSftp login(String username, String password, String ipAddress, int port) {
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

	public void logout(ChannelSftp sftp) {
		if (sftp != null) {
			sftp.quit();
			sftp.disconnect();
		}
	}

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
