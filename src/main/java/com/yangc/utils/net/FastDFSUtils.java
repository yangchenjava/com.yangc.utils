package com.yangc.utils.net;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.csource.common.MyException;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;

public class FastDFSUtils {

	private static final String FILE_PATH = "src/main/resources/fdfs.properties";

	private FastDFSUtils() {
	}

	private static TrackerServer getTrackerServer() throws FileNotFoundException, IOException, MyException {
		ClientGlobal.init(FILE_PATH);
		return new TrackerClient().getConnection();
	}

	private static NameValuePair[] convertNameValuePair(Map<String, String> metaMap) {
		if (MapUtils.isNotEmpty(metaMap)) {
			int i = 0;
			NameValuePair[] metaArray = new NameValuePair[metaMap.size()];
			for (Map.Entry<String, String> entry : metaMap.entrySet()) {
				metaArray[i++] = new NameValuePair(entry.getKey(), entry.getValue());
			}
			return metaArray;
		}
		return null;
	}

	/**
	 * @功能: 获取文件http地址
	 * @作者: yangc
	 * @创建日期: 2015年2月6日 下午1:56:14
	 * @param groupName
	 * @param remoteFileName
	 * @return
	 */
	public static String getFileUrl(String groupName, String remoteFileName) {
		TrackerServer trackerServer = null;
		try {
			trackerServer = getTrackerServer();
			InetAddress address = trackerServer.getInetSocketAddress().getAddress();
			if (address != null) {
				return "http://" + address.getHostAddress() + ":" + ClientGlobal.g_tracker_http_port + "/" + groupName + "/" + remoteFileName;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} finally {
			try {
				if (trackerServer != null) trackerServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: 删除文件
	 * @作者: yangc
	 * @创建日期: 2015年2月6日 下午5:54:00
	 * @param groupName
	 * @param remoteFileName
	 * @return
	 */
	public static boolean deleteFile(String groupName, String remoteFileName) {
		TrackerServer trackerServer = null;
		try {
			trackerServer = getTrackerServer();
			StorageClient storageClient = new StorageClient(trackerServer, null);
			if (storageClient.delete_file(groupName, remoteFileName) == 0) return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} finally {
			try {
				if (trackerServer != null) trackerServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 上传文件到storage server
	 * @作者: yangc
	 * @创建日期: 2015年2月4日 下午9:47:23
	 * @param b 文件字节数组
	 * @param fileExtName 文件扩展名(不包含".")
	 * @param metaMap 文件元信息
	 * @return 成功,results[0]:the group name to store the file,results[1]:the new created filename;<br/>
	 *         失败,返回null
	 */
	public static String[] upload(byte[] b, String fileExtName, Map<String, String> metaMap) {
		TrackerServer trackerServer = null;
		try {
			trackerServer = getTrackerServer();
			StorageClient storageClient = new StorageClient(trackerServer, null);
			return storageClient.upload_file(b, fileExtName, convertNameValuePair(metaMap));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} finally {
			try {
				if (trackerServer != null) trackerServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: 上传文件到storage server
	 * @作者: yangc
	 * @创建日期: 2015年2月4日 下午9:47:23
	 * @param filePath 文件路径
	 * @param fileExtName 文件扩展名(不包含".")
	 * @param metaMap 文件元信息
	 * @return 成功,results[0]:the group name to store the file,results[1]:the new created filename;<br/>
	 *         失败,返回null
	 */
	public static String[] upload(String filePath, String fileExtName, Map<String, String> metaMap) {
		TrackerServer trackerServer = null;
		try {
			trackerServer = getTrackerServer();
			StorageClient storageClient = new StorageClient(trackerServer, null);
			return storageClient.upload_file(filePath, fileExtName, convertNameValuePair(metaMap));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} finally {
			try {
				if (trackerServer != null) trackerServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: 下载文件
	 * @作者: yangc
	 * @创建日期: 2015年2月6日 下午5:54:19
	 * @param groupName
	 * @param remoteFileName
	 * @return
	 */
	public static byte[] download(String groupName, String remoteFileName) {
		TrackerServer trackerServer = null;
		try {
			trackerServer = getTrackerServer();
			StorageClient storageClient = new StorageClient(trackerServer, null);
			return storageClient.download_file(groupName, remoteFileName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} finally {
			try {
				if (trackerServer != null) trackerServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: 下载文件
	 * @作者: yangc
	 * @创建日期: 2015年2月6日 下午5:54:42
	 * @param groupName
	 * @param remoteFileName
	 * @param filePath
	 * @return
	 */
	public static boolean download(String groupName, String remoteFileName, String filePath) {
		TrackerServer trackerServer = null;
		try {
			trackerServer = getTrackerServer();
			StorageClient storageClient = new StorageClient(trackerServer, null);
			if (storageClient.download_file(groupName, remoteFileName, filePath) == 0) return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MyException e) {
			e.printStackTrace();
		} finally {
			try {
				if (trackerServer != null) trackerServer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static void main(String[] args) {
		System.out.println(FastDFSUtils.getFileUrl("group1", "M00/00/00/ChdmXFTTSeqM8CjbAADXeZmael4225.JPG"));
		System.out.println(FastDFSUtils.deleteFile("group1", "M00/00/00/ChdmXFTTe9Psr866AAAL9FEAIEE343.xml"));
		System.out.println(Arrays.toString(FastDFSUtils.upload("E:/settings_localhost.xml", "xml", null)));
		System.out.println(FastDFSUtils.download("group1", "M00/00/00/ChdmXFTTSeqM8CjbAADXeZmael4225.JPG", "E:/ddd.jpg"));
	}

}
