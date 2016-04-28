package com.yangc.utils.cache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import net.rubyeye.xmemcached.MemcachedClient;
import net.rubyeye.xmemcached.XMemcachedClientBuilder;
import net.rubyeye.xmemcached.exception.MemcachedException;
import net.rubyeye.xmemcached.utils.AddrUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.yangc.utils.prop.PropertiesUtils;

public class XMemcachedUtils {

	private static final Logger logger = LogManager.getLogger(XMemcachedUtils.class);

	private static final String FILE_PATH = "/memcached.properties";

	private static String[] servers = { "127.0.0.1:11211" };
	private static int[] weights = { 1 };

	private static MemcachedClient memcachedClient;

	private static class InstanceHolder {
		private static final XMemcachedUtils instance = new XMemcachedUtils();
	}

	private XMemcachedUtils() {
		logger.info("=================== 初始化配置文件 ===================");
		initConfig();
		logger.info("=================== 初始化 memcached ===============");
		initMemcached();
	}

	public static XMemcachedUtils getInstance() {
		return InstanceHolder.instance;
	}

	/**
	 * @功能: 初始化参数配置
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:46:55
	 */
	private void initConfig() {
		String[] sers = PropertiesUtils.getProperty(FILE_PATH, "memcached.servers").split(",");
		String[] wgts = PropertiesUtils.getProperty(FILE_PATH, "memcached.weights").split(",");
		List<Integer> serverUsed = new ArrayList<Integer>();

		TelnetClient telnet = new TelnetClient();
		for (int i = 0, len = sers.length; i < len; i++) {
			int seg = sers[i].indexOf(":");
			try {
				telnet.connect(sers[i].substring(0, seg), Integer.parseInt(sers[i].substring(seg + 1)));
				telnet.disconnect();
				serverUsed.add(i);
			} catch (Exception e) {
			}
		}
		int serverCount = serverUsed.size();
		if (serverCount == 0) {
			servers = null;
			return;
		}

		servers = new String[serverCount];
		weights = new int[serverCount];
		for (int i = 0; i < serverCount; i++) {
			servers[i] = sers[serverUsed.get(i)];
			weights[i] = Integer.parseInt(wgts[serverUsed.get(i)]);
		}
	}

	private void initMemcached() {
		if (servers == null) {
			logger.error("=================== 初始化 memcached 失败 ===============");
			return;
		}

		XMemcachedClientBuilder builder = new XMemcachedClientBuilder(AddrUtil.getAddresses(StringUtils.join(servers, " ")), weights);
		try {
			memcachedClient = builder.build();
		} catch (IOException e) {
			logger.error("=================== 初始化 memcached 失败 ===============", e.getCause());
			e.printStackTrace();
		} finally {
			try {
				if (memcachedClient != null && !memcachedClient.isShutdown()) {
					memcachedClient.shutdown();
					memcachedClient = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * @功能: 重新加载memcached客户端
	 * @作者: yangc
	 * @创建日期: 2016年4月27日 下午5:11:58
	 * @return
	 */
	public synchronized boolean reloadMemcached() {
		try {
			if (memcachedClient != null && !memcachedClient.isShutdown()) {
				memcachedClient.shutdown();
				memcachedClient = null;
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		logger.info("=================== 初始化配置文件 ===================");
		initConfig();
		logger.info("=================== 初始化 memcached ===============");
		initMemcached();
		return !memcachedClient.isShutdown();
	}

	/**
	 * @功能: 获取memcached客户端
	 * @作者: yangc
	 * @创建日期: 2016年4月27日 下午5:10:40
	 * @return
	 */
	public MemcachedClient getMemcachedClient() {
		return memcachedClient;
	}

	/**
	 * @功能: 是否关闭memcached
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:47:28
	 * @return
	 */
	public boolean isShutdown() {
		return this.getMemcachedClient().isShutdown();
	}

	/**
	 * @功能: 插入新记录, 前提是记录的Key在缓存中不存在
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:47:50
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean add(String key, Object value) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().add(key, 0, value);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 插入新记录, 前提是记录的Key在缓存中不存在
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:49:21
	 * @param key
	 * @param value
	 * @param exp 失效时间(单位秒)
	 * @return
	 */
	public boolean add(String key, Object value, int exp) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().add(key, exp, value);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 插入新记录或更新已有记录, 记录的Key在缓存中不存在则插入否则更新
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:50:17
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, Object value) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().set(key, 0, value);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 插入新记录或更新已有记录, 记录的Key在缓存中不存在则插入否则更新
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:51:40
	 * @param key
	 * @param value
	 * @param exp 失效时间(单位秒)
	 * @return
	 */
	public boolean set(String key, Object value, int exp) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().set(key, exp, value);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 更新已有记录, 前提是记录的Key在缓存中已经存在
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:52:00
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean replace(String key, Object value) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().replace(key, 0, value);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 更新已有记录, 前提是记录的Key在缓存中已经存在
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午10:52:06
	 * @param key
	 * @param value
	 * @param exp 失效时间(单位秒)
	 * @return
	 */
	public boolean replace(String key, Object value, int exp) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().replace(key, exp, value);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 返回单条记录
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午11:05:06
	 * @param key
	 * @return
	 */
	public <T> T get(String key) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().get(key);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: 返回多条记录
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午11:05:29
	 * @param keys
	 * @return
	 */
	public Map<String, Object> get(List<String> keys) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().get(keys);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: 删除记录, 执行该方法之后, 使用stats的统计结果会同步更新
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午11:05:53
	 * @param key
	 * @return
	 */
	public boolean delete(String key) {
		if (!this.isShutdown()) {
			try {
				return this.getMemcachedClient().delete(key);
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * @功能: 清空全部缓存数据, 执行该方法之后, 使用stats的统计结果不会马上发生变化, 每get一个不存在的item之后, 该item的值才会被动清空
	 * @作者: yangc
	 * @创建日期: 2014年3月29日 下午11:06:02
	 * @return
	 */
	public boolean flushAll() {
		if (!this.isShutdown()) {
			try {
				this.getMemcachedClient().flushAll();
				return true;
			} catch (TimeoutException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (MemcachedException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

}
