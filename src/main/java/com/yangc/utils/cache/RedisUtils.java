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

import com.google.gson.reflect.TypeToken;
import com.yangc.utils.json.JsonUtils;
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

	public boolean set(String key, Object value) {
		if (isUsed) {
			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.set(key, JsonUtils.toJson(value));
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return false;
	}

	public boolean set(String key, Object value, long expireTime) {
		if (isUsed) {
			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.set(key, JsonUtils.toJson(value));
				jedis.expireAt(key, expireTime);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return false;
	}

	public <T> T get(String key, TypeToken<T> typeToken) {
		if (isUsed) {
			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				return JsonUtils.fromJson(jedis.get(key), typeToken);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return null;
	}

	public boolean del(String key) {
		if (isUsed) {
			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.del(key);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return false;
	}

	public boolean addQueue(String key, Object... values) {
		if (isUsed) {
			int len = values.length;
			String[] strings = new String[len];
			for (int i = 0; i < len; i++) {
				strings[i] = JsonUtils.toJson(values[i]);
			}

			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.rpush(key, strings);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return false;
	}

	public <T> T pollQueue(String key, TypeToken<T> typeToken) {
		if (isUsed) {
			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				return JsonUtils.fromJson(jedis.lpop(key), typeToken);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return null;
	}

	public boolean pushStack(String key, Object... values) {
		if (isUsed) {
			int len = values.length;
			String[] strings = new String[len];
			for (int i = 0; i < len; i++) {
				strings[i] = JsonUtils.toJson(values[i]);
			}

			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.rpush(key, strings);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return false;
	}

	public <T> T popStack(String key, TypeToken<T> typeToken) {
		if (isUsed) {
			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				return JsonUtils.fromJson(jedis.rpop(key), typeToken);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return null;
	}

	public boolean putHashMap(String key, String[] fields, Object[] values) {
		if (isUsed && fields.length == values.length) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0, len = fields.length; i < len; i++) {
				map.put(fields[i], JsonUtils.toJson(values[i]));
			}

			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				jedis.hmset(key, map);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return false;
	}

	public <T> Object[] getHashMap(String key, String[] fields, TypeToken<T>... typeToken) {
		if (isUsed) {
			ShardedJedis jedis = null;
			try {
				jedis = pool.getResource();
				List<String> jsons = jedis.hmget(key, fields);

				int size = jsons.size();
				Object[] values = new Object[size];
				if (typeToken.length == 1) {
					for (int i = 0; i < size; i++) {
						values[i] = JsonUtils.fromJson(jsons.get(i), typeToken[0]);
					}
				} else if (typeToken.length == size) {
					for (int i = 0; i < size; i++) {
						values[i] = JsonUtils.fromJson(jsons.get(i), typeToken[i]);
					}
				}
				return values;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				pool.returnResource(jedis);
			}
		}
		return null;
	}
}
