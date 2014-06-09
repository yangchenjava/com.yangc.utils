package com.yangc.utils.test;

import redis.clients.jedis.Jedis;

public class RedisSentinelTest {

	public static void main(String[] args) {
		Jedis jedis = new Jedis("10.23.6.12", 6379);
		String str = jedis.get("menuMain_1_1");
		jedis.close();
		System.out.println(str);
	}

}
