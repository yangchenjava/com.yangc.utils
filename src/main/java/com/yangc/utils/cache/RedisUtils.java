package com.yangc.utils.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
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

	enum Cluster {
		SHARD("shard"), SHARD_MASTER_SLAVE("shard_master_slave");

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
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance(FILE_PATH);

		String cluster = propertiesUtils.getProperty("redis.cluster");
		if (StringUtils.equals(cluster, Cluster.SHARD.value())) {
			servers = Arrays.asList(propertiesUtils.getProperty("redis.servers").split(","));
		} else {
			servers = new ArrayList<String>();
			String[] sentinels = propertiesUtils.getProperty("redis.sentinels").split(",");
			for (String sentinel : sentinels) {
				String[] hostPort = sentinel.split(":");
				Jedis jedis = new Jedis(hostPort[0], Integer.parseInt(hostPort[1]));
				for (Map<String, String> map : jedis.sentinelMasters()) {
					String masterServer = map.get("ip") + ":" + map.get("port");
					servers.add(masterServer);
					new Thread(new CheckRedisSentinel(jedis, masterServer, map.get("name"), Long.parseLong(map.get("down-after-milliseconds")))).start();
				}
			}
		}

		serverConfig.put("maxIdle", propertiesUtils.getProperty("redis.maxIdle", "8"));
		serverConfig.put("maxTotal", propertiesUtils.getProperty("redis.maxTotal", "8"));
		serverConfig.put("maxWaitMillis", propertiesUtils.getProperty("redis.maxWaitMillis", "-1"));
		serverConfig.put("testOnBorrow", propertiesUtils.getProperty("redis.testOnBorrow", "false"));
		serverConfig.put("testOnReturn", propertiesUtils.getProperty("redis.testOnReturn", "false"));
		serverConfig.put("testWhileIdle", propertiesUtils.getProperty("redis.testWhileIdle", "false"));
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

	private class CheckRedisSentinel implements Runnable {
		private Jedis jedis;
		private String masterName;
		private String masterServer;
		private long downAfterMilliseconds;

		private CheckRedisSentinel(Jedis jedis, String masterServer, String masterName, long downAfterMilliseconds) {
			this.jedis = jedis;
			this.masterName = masterName;
			this.masterServer = masterServer;
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

	private String getHost(ShardedJedis jedis, String key) {
		JedisShardInfo shard = jedis.getShardInfo(key);
		return shard.getHost() + ":" + shard.getPort();
	}

	public boolean expire(String key, int seconds) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
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

	public boolean set(String key, Object value) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
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

	public <T> T get(String key, TypeToken<T> typeToken) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
			return JsonUtils.fromJson(jedis.get(key), typeToken);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	public boolean del(String key) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
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

	public boolean addQueue(String key, Object... values) {
		int len = values.length;
		String[] strings = new String[len];
		for (int i = 0; i < len; i++) {
			strings[i] = JsonUtils.toJson(values[i]);
		}

		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
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

	public <T> T pollQueue(String key, TypeToken<T> typeToken) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
			return JsonUtils.fromJson(jedis.lpop(key), typeToken);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	public boolean pushStack(String key, Object... values) {
		int len = values.length;
		String[] strings = new String[len];
		for (int i = 0; i < len; i++) {
			strings[i] = JsonUtils.toJson(values[i]);
		}

		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
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

	public <T> T popStack(String key, TypeToken<T> typeToken) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
			return JsonUtils.fromJson(jedis.rpop(key), typeToken);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

	public boolean putHashMap(String key, Map<String, String> map) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
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

	public List<String> getHashMap(String key, String... fields) {
		ShardedJedis jedis = null;
		try {
			jedis = pool.getResource();
			logger.info(this.getHost(jedis, key));
			return jedis.hmget(key, fields);
		} catch (Exception e) {
			e.printStackTrace();
			pool.returnBrokenResource(jedis);
		} finally {
			pool.returnResource(jedis);
		}
		return null;
	}

}
