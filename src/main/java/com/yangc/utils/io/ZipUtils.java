package com.yangc.utils.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

	private static final int BUFFER_SIZE = 1024 * 4;

	private ZipUtils() {
	}

	/**
	 * @功能: 压缩集合中的文件到指定路径
	 * @作者: yangc
	 * @创建日期: 2013-11-19 下午03:25:30
	 */
	public static void zipFile(List<File> files, String zipFilePath) {
		int len = -1;
		byte[] b = null;
		BufferedInputStream bis = null;
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFilePath)));
			for (File file : files) {
				if (file.isFile()) {
					len = -1;
					b = new byte[BUFFER_SIZE];
					bis = new BufferedInputStream(new FileInputStream(file));
					ZipEntry ze = new ZipEntry(file.getName());
					zos.putNextEntry(ze);
					while ((len = bis.read(b)) != -1) {
						zos.write(b, 0, len);
					}
					zos.flush();
					bis.close();
					bis = null;
				}
			}
			zos.close();
			zos = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) bis.close();
				if (zos != null) zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @功能: 压缩给定路径的文件或文件夹到指定路径
	 * @作者: yangc
	 * @创建日期: 2013-11-19 下午03:27:26
	 */
	public static void zip(String srcFilePath, String zipFilePath) {
		ZipOutputStream zos = null;
		try {
			zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFilePath)));
			zip(new File(srcFilePath), zos, "");
			zos.close();
			zos = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (zos != null) zos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void zip(File file, ZipOutputStream zos, String base) {
		if (file.isDirectory()) {
			base += file.getName() + "/";
			try {
				zos.putNextEntry(new ZipEntry(base));
			} catch (IOException e) {
				e.printStackTrace();
			}
			for (File f : file.listFiles()) {
				zip(f, zos, base);
			}
		} else {
			int len = -1;
			byte[] b = new byte[BUFFER_SIZE];
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				zos.putNextEntry(new ZipEntry(base + file.getName()));
				while ((len = bis.read(b)) != -1) {
					zos.write(b, 0, len);
				}
				zos.flush();
				bis.close();
				bis = null;
			} catch (FileNotFoundException e) {
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
		}
	}

	/**
	 * @功能: 解压给定路径的文件到指定路径
	 * @作者: yangc
	 * @创建日期: 2013-11-19 下午03:28:21
	 */
	public static void unzip(String zipFilePath, String destFilePath) {
		int len = -1;
		byte[] b = null;
		ZipFile zipFile = null;
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			zipFile = new ZipFile(zipFilePath);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while (zipEntries.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
				if (zipEntry.isDirectory()) {
					new File(destFilePath + "/" + zipEntry.getName()).mkdirs();
					continue;
				}
				File file = new File(destFilePath + "/" + zipEntry.getName());
				File parent = file.getParentFile();
				if (parent != null && !parent.exists()) {
					parent.mkdirs();
				}
				len = -1;
				b = new byte[BUFFER_SIZE];
				bis = new BufferedInputStream(zipFile.getInputStream(zipEntry));
				bos = new BufferedOutputStream(new FileOutputStream(file));
				while ((len = bis.read(b)) != -1) {
					bos.write(b, 0, len);
				}
				bos.flush();
				bos.close();
				bos = null;
				bis.close();
				bis = null;
			}
			zipFile.close();
			zipFile = null;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null) bos.close();
				if (bis != null) bis.close();
				if (zipFile != null) zipFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		// ZipUtils.zipFile(Arrays.asList(new File("E:/workspace/utils/libs").listFiles()), "E:/workspace/utils/libs_1.zip");
		// ZipUtils.zip("E:/workspace/utils/test", "E:/workspace/utils/libs_2.zip");
		ZipUtils.unzip("E:/workspace/utils/libs_2.zip", "E:/workspace/utils/yangc");
		System.out.println("ok");
	}

}
