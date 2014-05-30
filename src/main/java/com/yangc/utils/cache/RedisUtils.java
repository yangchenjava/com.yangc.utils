package com.yangc.utils.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.telnet.TelnetClient;

import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import com.yangc.utils.prop.PropertiesUtils;

public class RedisUtils {

	private static final String FILE_PATH = "/redis.properties";

	private static String[] servers = { "127.0.0.1:6379" };
	private static final Map<String, String> serverConfig = new HashMap<String, String>();

	// 是否启用redis
	private static boolean isUsed = true;

	private static ShardedJedisPool pool;

	private static RedisUtils redisUtils;

	static {
		initConfig();

		JedisPoolConfig poolConfig = new JedisPoolConfig();
		poolConfig.setMaxIdle(Integer.parseInt(serverConfig.get("maxIdle")));
		poolConfig.setMaxTotal(Integer.parseInt(serverConfig.get("maxTotal")));
		poolConfig.setMaxWaitMillis(Long.parseLong(serverConfig.get("maxWaitMillis")));
		poolConfig.setTestOnBorrow(Boolean.parseBoolean(serverConfig.get("testOnBorrow")));
		poolConfig.setTestOnReturn(Boolean.parseBoolean(serverConfig.get("testOnReturn")));
		poolConfig.setTestWhileIdle(Boolean.parseBoolean(serverConfig.get("testWhileIdle")));

		List<JedisShardInfo> shards = new ArrayList<JedisShardInfo>(servers.length);
		for (String server : servers) {
			String[] hostPort = server.split(":");
			JedisShardInfo shard = new JedisShardInfo(hostPort[0], Integer.parseInt(hostPort[1]));
			shards.add(shard);
		}

		pool = new ShardedJedisPool(poolConfig, shards);
	}

	private static void initConfig() {
		PropertiesUtils propertiesUtils = PropertiesUtils.getInstance(FILE_PATH);

		String[] sers = propertiesUtils.getProperty("redis.servers").split(",");
		List<Integer> serverUsed = new ArrayList<Integer>();

		TelnetClient telnet = new TelnetClient();
		for (int i = 0; i < sers.length; i++) {
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
			isUsed = false;
			return;
		}

		servers = new String[serverCount];
		for (int i = 0; i < serverCount; i++) {
			servers[i] = sers[serverUsed.get(i)];
		}

		serverConfig.put("maxIdle", propertiesUtils.getProperty("redis.maxIdle", "8"));
		serverConfig.put("maxTotal", propertiesUtils.getProperty("redis.maxTotal", "8"));
		serverConfig.put("maxWaitMillis", propertiesUtils.getProperty("redis.maxWaitMillis", "-1"));
		serverConfig.put("testOnBorrow", propertiesUtils.getProperty("redis.testOnBorrow", "false"));
		serverConfig.put("testOnReturn", propertiesUtils.getProperty("redis.testOnReturn", "false"));
		serverConfig.put("testWhileIdle", propertiesUtils.getProperty("redis.testWhileIdle", "false"));
	}

	private RedisUtils() {
	}

	public synchronized static RedisUtils getInstance() {
		if (redisUtils == null) {
			redisUtils = new RedisUtils();
		}
		return redisUtils;
	}

	public boolean isUsedRedis() {
		return isUsed;
	}

	public boolean set(String key, String value) {
		if (isUsed) {
			ShardedJedis jedis = pool.getResource();
			String code = jedis.set(key, value);
			System.out.println(code);
			pool.returnResource(jedis);
		}
		return false;
	}

	public String get(String key) {
		if (isUsed) {
			ShardedJedis jedis = pool.getResource();
			String value = jedis.get(key);
			pool.returnResource(jedis);
			return value;
		}
		return null;
	}

}
