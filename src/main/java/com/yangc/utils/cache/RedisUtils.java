package com.yangc.utils.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;

import com.google.gson.reflect.TypeToken;
import com.yangc.utils.json.JsonUtils;
import com.yangc.utils.prop.PropertiesUtils;

public class RedisUtils {

	private static final Logger logger = Logger.getLogger(RedisUtils.class);

	private static final String FILE_PATH = "/redis.properties";

	/** 集群模式 */
	enum Cluster {
		/** 分片式一致性hash */
		SHARD("shard"),

		/** 分片式一致性hash + master-slave主从灾备 */
		SHARD_MASTER_SLAVE("shard_master_slave");

		private String value;

		private Cluster(String value) {
			this.value = value;
		}

		public String value() {
			return this.value;
		}
	}

	private static List<String> servers;
	private static final Map<String, String> serverConfig = new HashMap<String, String>();

	private static ShardedJedisPool pool;

	private static RedisUtils redisUtils;

	private void initConfig() {
		String cluster = PropertiesUtils.getProperty(FILE_PATH, "redis.cluster");
		// 只采用分片式一致性hash
		if (StringUtils.equals(cluster, Cluster.SHARD.value())) {
			servers = Arrays.asList(PropertiesUtils.getProperty(FILE_PATH, "redis.servers").split(","));
		}
		// 分片式一致性hash + master-slave主从灾备, 通过sentinel自动切换主从结构
		else {
			servers = new ArrayList<String>();
			String[] sentinels = PropertiesUtils.getProperty(FILE_PATH, "redis.sentinels").split(",");
			for (String sentinel : sentinels) {
				String[] hostPort = sentinel.split(":");
				Jedis jedis = new Jedis(hostPort[0], Integer.parseInt(hostPort[1]));
				for (Map<String, String> map : jedis.sentinelMasters()) {
					String masterServer = map.get("ip") + ":" + map.get("port");
					servers.add(masterServer);
					// 启动线程用来查看主从结构并进行切换
					new Thread(new CheckRedisSentinel(jedis, masterServer, map.get("name"), Long.parseLong(map.get("down-after-milliseconds")))).start();
				}
			}
		}

		serverConfig.put("maxIdle", PropertiesUtils.getProperty(FILE_PATH, "redis.maxIdle", "8"));
		serverConfig.put("maxTotal", PropertiesUtils.getProperty(FILE_PATH, "redis.maxTotal", "8"));
		serverConfig.put("maxWaitMillis", PropertiesUtils.getProperty(FILE_PATH, "redis.maxWaitMillis", "-1"));
		serverConfig.put("testOnBorrow", PropertiesUtils.getProperty(FILE_PATH, "redis.testOnBorrow", "false"));
		serverConfig.put("testOnReturn", PropertiesUtils.getProperty(FILE_PATH, "redis.testOnReturn", "false"));
		serverConfig.put("testWhileIdle", PropertiesUtils.getProperty(FILE_PATH, "redis.testWhileIdle", "false"));
	}

	private void initRedis() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(Integer.parseInt(serverConfig.get("maxIdle")));
		poolConfig.setMaxTotal(Integer.parseInt(serverConfig.get("maxTotal")));
		poolConfig.setMaxWaitMillis(Long.parseLong(serverConfig.get("maxWaitMillis")));
		poolConfig.setTestOnBorrow(Boolean.parseBoolean(serverConfig.get("testOnBorrow")));
		poolConfig.setTestOnReturn(Boolean.parseBoolean(serverConfig.get("testOnReturn")));
		poolConfig.setTestWhileIdle(Boolean.parseBoolean(serverConfig.get("testWhileIdle")));

		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>(servers.size());
		for (String server : servers) {
			String[] hostPort = server.split(":");
			JedisShardInfo shard = new JedisShardInfo(hostPort[0], Integer.parseInt(hostPort[1]));
			shards.add(shard);
		}

		pool = new ShardedJedisPool(poolConfig, shards);
	}

	/**
	 * @功能: 检查主从结构, 如果出现主从变更则切换变更状态并重启当前资源池
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:24:44
	 */
	private class CheckRedisSentinel implements Runnable {
		private Jedis jedis;
		private String masterServer;
		private String masterName;
		private long downAfterMilliseconds;

		private CheckRedisSentinel(Jedis jedis, String masterServer, String masterName, long downAfterMilliseconds) {
			this.jedis = jedis;
			this.masterServer = masterServer;
			this.masterName = masterName;
			this.downAfterMilliseconds = downAfterMilliseconds;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(this.downAfterMilliseconds);
					List<String> hostPort = this.jedis.sentinelGetMasterAddrByName(this.masterName);
					String newMasterServer = hostPort.get(0) + ":" + hostPort.get(1);
					if (!StringUtils.equals(this.masterServer, newMasterServer)) {
						pool.destroy();
						logger.info("断开redis客户端");

						servers.remove(this.masterServer);
						logger.info("移除服务端" + this.masterServer);

						servers.add(newMasterServer);
						this.masterServer = newMasterServer;
						logger.info("添加服务端" + this.masterServer);

						initRedis();
						logger.info("开启redis客户端");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private RedisUtils() {
		logger.info("=================== 初始化配置文件 ===================");
		initConfig();
		logger.info("=================== 初始化 redis ===================");
		initRedis();
	}

	public synchronized static RedisUtils getInstance() {
		if (redisUtils == null) {
			redisUtils = new RedisUtils();
		}
		return redisUtils;
	}

	/**
	 * @功能: 获取jedis资源
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:24:44
	 * @return
	 */
	public ShardedJedis getJedis() {
		return pool.getResource();
	}

	/**
	 * @功能: 释放出现异常的jedis资源
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:27:48
	 * @param jedis
	 */
	public void returnBrokenResource(ShardedJedis jedis) {
		if (jedis != null) {
			pool.returnBrokenResource(jedis);
		}
	}

	/**
	 * @功能: 释放jedis资源
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:28:47
	 * @param jedis
	 */
	public void returnResource(ShardedJedis jedis) {
		if (jedis != null) {
			pool.returnResource(jedis);
		}
	}

	/**
	 * @功能: 获取当前k-v所在的地址
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:29:13
	 * @param jedis
	 * @param key
	 * @return
	 */
	private String getHost(ShardedJedis jedis, String key) {
		JedisShardInfo shard = jedis.getShardInfo(key);
		return shard.getHost() + ":" + shard.getPort();
	}

	/**
	 * @功能: 获取所有满足条件的key
	 * @作者: yangc
	 * @创建日期: 2014年6月9日 下午2:18:04
	 * @param pattern
	 * @return
	 */
	public Set<String> keys(String pattern) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			Set<String> keys = new HashSet<String>();
			for (Jedis j : jedis.getAllShards()) {
				keys.addAll(j.keys(pattern));
				j.close();
			}
			return keys;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/**
	 * @功能: 设置k-v多少秒后过期
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:30:45
	 * @param key
	 * @param seconds
	 * @return
	 */
	public boolean expire(String key, int seconds) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.expire(key, seconds);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/**
	 * @功能: 设置k-v
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:31:57
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean set(String key, Object value) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.set(key, JsonUtils.toJson(value));
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/**
	 * @功能: 批量设置k-v
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:32:17
	 * @param map
	 * @return
	 */
	public boolean batchSet(Map<String, Object> map) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			ShardedJedisPipeline pipeline = jedis.pipelined();
			for (Entry<String, Object> entry : map.entrySet()) {
				pipeline.set(entry.getKey(), JsonUtils.toJson(entry.getValue()));
			}
			pipeline.sync();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/**
	 * @功能: 获取值
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:34:15
	 * @param key
	 * @param typeToken
	 * @return
	 */
	public <T> T get(String key, TypeToken<T> typeToken) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return JsonUtils.fromJson(jedis.get(key), typeToken);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/**
	 * @功能: 删除k-v
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:35:06
	 * @param key
	 * @return
	 */
	public boolean del(String key) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.del(key);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/**
	 * @功能: 删除多组k-v
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:35:06
	 * @param keys
	 * @return
	 */
	public boolean del(String... keys) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			for (Jedis j : jedis.getAllShards()) {
				j.del(keys);
				j.close();
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/** ----------------------------- 操作队列 ------------------------------ */

	/**
	 * @功能: 插入队列
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:59:01
	 * @param key
	 * @param values
	 * @return
	 */
	public boolean addQueue(String key, Object... values) {
		int len = values.length;
		String[] strings = new String[len];
		for (int i = 0; i < len; i++) {
			strings[i] = JsonUtils.toJson(values[i]);
		}

		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.rpush(key, strings);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/**
	 * @功能: 移出队列
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午10:59:29
	 * @param key
	 * @param typeToken
	 * @return
	 */
	public <T> T pollQueue(String key, TypeToken<T> typeToken) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return JsonUtils.fromJson(jedis.lpop(key), typeToken);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/** ----------------------------- 操作栈 ------------------------------ */

	/**
	 * @功能: 压栈
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午11:32:40
	 * @param key
	 * @param values
	 * @return
	 */
	public boolean pushStack(String key, Object... values) {
		int len = values.length;
		String[] strings = new String[len];
		for (int i = 0; i < len; i++) {
			strings[i] = JsonUtils.toJson(values[i]);
		}

		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.rpush(key, strings);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/**
	 * @功能: 出栈
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午11:33:09
	 * @param key
	 * @param typeToken
	 * @return
	 */
	public <T> T popStack(String key, TypeToken<T> typeToken) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return JsonUtils.fromJson(jedis.rpop(key), typeToken);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/** ----------------------------- 操作hashmap ------------------------------ */

	/**
	 * @功能: 设置hashmap
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午11:33:27
	 * @param key
	 * @param map
	 * @return
	 */
	public boolean putHashMap(String key, Map<String, String> map) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.hmset(key, map);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

	/**
	 * @功能: 获取hashmap中的值
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午11:33:56
	 * @param key
	 * @param fields
	 * @return
	 */
	public List<String> getHashMap(String key, String... fields) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/**
	 * @功能: 获取hashmap中所有的key
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午11:33:56
	 * @param key
	 * @return
	 */
	public Set<String> getHashMapKeys(String key) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return jedis.hkeys(key);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/**
	 * @功能: 获取hashmap中所有的value
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 上午11:33:56
	 * @param key
	 * @return
	 */
	public List<String> getHashMapValues(String key) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return jedis.hvals(key);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

}
