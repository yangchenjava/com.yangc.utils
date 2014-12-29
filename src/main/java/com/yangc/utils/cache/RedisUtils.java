package com.yangc.utils.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPipeline;
import redis.clients.jedis.ShardedJedisPool;
import redis.clients.jedis.Tuple;

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

	private static final List<String> SERVERS = new ArrayList<String>();
	private static final Map<String, String> SERVER_CONFIG = new HashMap<String, String>();

	private static ShardedJedisPool pool;
	private static final List<SentinelListener> SENTINEL_LISTENERS = new ArrayList<SentinelListener>();

	private static RedisUtils instance;

	public interface SentinelListener {
		public void onSentinelListener();
	}

	private RedisUtils() {
		logger.info("=================== 初始化配置文件 ===================");
		initConfig();
		logger.info("=================== 初始化 redis ===================");
		initRedis();
	}

	public synchronized static RedisUtils getInstance() {
		if (instance == null) {
			instance = new RedisUtils();
		}
		return instance;
	}

	/**
	 * @功能: 初始化配置文件
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 上午10:41:45
	 */
	private void initConfig() {
		String cluster = PropertiesUtils.getProperty(FILE_PATH, "redis.cluster");
		// 只采用分片式一致性hash
		if (StringUtils.equals(cluster, Cluster.SHARD.value())) {
			SERVERS.addAll(Arrays.asList(PropertiesUtils.getProperty(FILE_PATH, "redis.servers").split(",")));
		}
		// 分片式一致性hash + master-slave主从灾备, 通过sentinel自动切换主从结构
		else {
			String[] sentinels = PropertiesUtils.getProperty(FILE_PATH, "redis.sentinels").split(",");
			for (String sentinel : sentinels) {
				String[] hostPort = sentinel.split(":");
				Jedis jedis = new Jedis(hostPort[0], Integer.parseInt(hostPort[1]));
				for (Map<String, String> map : jedis.sentinelMasters()) {
					String masterServer = map.get("ip") + ":" + map.get("port");
					SERVERS.add(masterServer);
					// 启动线程用来查看主从结构并进行切换
					new Thread(new CheckRedisSentinel(jedis, masterServer, map.get("name"), Long.parseLong(map.get("down-after-milliseconds")))).start();
				}
			}
		}

		SERVER_CONFIG.put("maxIdle", PropertiesUtils.getProperty(FILE_PATH, "redis.maxIdle", "8"));
		SERVER_CONFIG.put("maxTotal", PropertiesUtils.getProperty(FILE_PATH, "redis.maxTotal", "8"));
		SERVER_CONFIG.put("maxWaitMillis", PropertiesUtils.getProperty(FILE_PATH, "redis.maxWaitMillis", "-1"));
		SERVER_CONFIG.put("testOnBorrow", PropertiesUtils.getProperty(FILE_PATH, "redis.testOnBorrow", "false"));
		SERVER_CONFIG.put("testOnReturn", PropertiesUtils.getProperty(FILE_PATH, "redis.testOnReturn", "false"));
		SERVER_CONFIG.put("testWhileIdle", PropertiesUtils.getProperty(FILE_PATH, "redis.testWhileIdle", "false"));
	}

	/**
	 * @功能: 初始化redis
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 上午10:42:07
	 */
	private void initRedis() {
		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(MapUtils.getIntValue(SERVER_CONFIG, "maxIdle"));
		poolConfig.setMaxTotal(MapUtils.getIntValue(SERVER_CONFIG, "maxTotal"));
		poolConfig.setMaxWaitMillis(MapUtils.getLongValue(SERVER_CONFIG, "maxWaitMillis"));
		poolConfig.setTestOnBorrow(MapUtils.getBooleanValue(SERVER_CONFIG, "testOnBorrow"));
		poolConfig.setTestOnReturn(MapUtils.getBooleanValue(SERVER_CONFIG, "testOnReturn"));
		poolConfig.setTestWhileIdle(MapUtils.getBooleanValue(SERVER_CONFIG, "testWhileIdle"));

		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>(SERVERS.size());
		for (String server : SERVERS) {
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

						SERVERS.remove(this.masterServer);
						logger.info("移除服务端" + this.masterServer);

						SERVERS.add(newMasterServer);
						this.masterServer = newMasterServer;
						logger.info("添加服务端" + this.masterServer);

						initRedis();
						logger.info("开启redis客户端");

						if (!SENTINEL_LISTENERS.isEmpty()) {
							for (SentinelListener sentinelListener : SENTINEL_LISTENERS) {
								sentinelListener.onSentinelListener();
							}
							logger.info("如果有订阅消息,通知重新订阅");
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/** ----------------------------------------- Server ------------------------------------------- */

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

	/** ----------------------------------------- String ------------------------------------------- */

	/**
	 * @功能: 获取所有满足条件的key(慎用,低效)
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

	/** ----------------------------------------- Queue(FIFO) ------------------------------------------- */

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

	/** ----------------------------------------- Stack(FILO) ------------------------------------------- */

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

	/** ----------------------------------------- Hash ------------------------------------------- */

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

	/** ----------------------------------------- SortedSet ------------------------------------------- */

	/**
	 * @功能: 设置SortedSet
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午6:03:16
	 * @param key
	 * @param map
	 * @return
	 */
	public boolean zadd(String key, Map<String, Double> map) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.zadd(key, map);
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
	 * @功能: 为member的score加上增量increment
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午6:04:19
	 * @param key
	 * @param increment
	 * @param member
	 * @return
	 */
	public boolean zincrby(String key, double increment, String member) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.zincrby(key, increment, member);
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
	 * @功能: 删除下标为start到end的记录(包含start和stop)
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午6:05:52
	 * @param key
	 * @param start 从0开始,-1表示最后一个成员,-2表示倒数第二个成员,以此类推
	 * @param end
	 * @return
	 */
	public boolean zremrangeByRank(String key, long start, long end) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			jedis.zremrangeByRank(key, start, end);
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
	 * @功能: 按倒序取下标为start到end的记录(包含start和stop)
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午6:08:44
	 * @param key
	 * @param start 从0开始,-1表示最后一个成员,-2表示倒数第二个成员,以此类推
	 * @param end
	 * @return
	 */
	public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return jedis.zrevrangeWithScores(key, start, end);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/**
	 * @功能: 按倒序取SortedSet中对应key和member的下标
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午6:10:00
	 * @param key
	 * @param member
	 * @return
	 */
	public Long zrevrank(String key, String member) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, key));
			return jedis.zrevrank(key, member);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	/** ----------------------------------------- Pub/Sub ------------------------------------------- */

	/**
	 * @功能: 从指定通道订阅消息
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午8:53:17
	 * @param sentinelListener
	 * @param jedisPubSub
	 * @param channel
	 * @return
	 */
	public boolean subscribe(SentinelListener sentinelListener, JedisPubSub jedisPubSub, String channel) {
		SENTINEL_LISTENERS.add(sentinelListener);
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, channel));
			jedis.getShard(channel).subscribe(jedisPubSub, channel);
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
	 * @功能: 取消订阅
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午8:53:34
	 * @param jedisPubSub
	 */
	public void unsubscribe(SentinelListener sentinelListener, JedisPubSub jedisPubSub) {
		SENTINEL_LISTENERS.remove(sentinelListener);
		jedisPubSub.unsubscribe();
	}

	/**
	 * @功能: 指定通道发送消息
	 * @作者: yangc
	 * @创建日期: 2014年12月25日 下午8:53:44
	 * @param channel
	 * @param message
	 * @return
	 */
	public boolean publish(String channel, String message) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.debug(this.getHost(jedis, channel));
			jedis.getShard(channel).publish(channel, message);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return false;
	}

}
