package com.yangc.utils.test;

import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.yangc.utils.cache.RedisUtils;

public class RedisSentinelTest {

	public static void main(String[] args) {
		RedisUtils cache = RedisUtils.getInstance();
		List<User> users = cache.get("users", new TypeToken<List<User>>() {
		});

		if (users != null && !users.isEmpty()) {
			for (User user : users) {
				System.out.println(user);
			}
		}
	}

}
