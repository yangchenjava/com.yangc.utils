package com.yangc.utils.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.yangc.utils.cache.RedisUtils;

public class RedisTest {

	public static void main(String[] args) {
		RedisUtils cache = RedisUtils.getInstance();

		RedisTest test = new RedisTest();
		test.entity(cache);
		// test.queue(cache);
		// test.stack(cache);
		// test.map(cache);
		// test.batch(cache);
	}

	public void entity(RedisUtils cache) {
		User u_1 = new User(1, "yangc_1", "123456_1");
		User u_2 = new User(2, "yangc_2", "123456_2");
		User u_3 = new User(3, "yangc_3", "123456_3");

		List<User> list = new ArrayList<User>();
		list.add(u_1);
		list.add(u_2);
		list.add(u_3);

		cache.set("users", list);
		List<User> users = cache.get("users", new TypeToken<List<User>>() {
		});

		if (users != null && !users.isEmpty()) {
			for (User user : users) {
				System.out.println(user);
			}
		}
	}

	public void queue(RedisUtils cache) {
		cache.addQueue("queue", "a", "b", "c");
		String value = cache.pollQueue("queue", new TypeToken<String>() {
		});
		System.out.println(value);
	}

	public void stack(RedisUtils cache) {
		cache.pushStack("stack", "a", "b", "c");
		String value = cache.popStack("stack", new TypeToken<String>() {
		});
		System.out.println(value);
	}

	public void map(RedisUtils cache) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", "yangc");
		map.put("password", "123456");
		cache.putHashMap("map", map);
		List<String> list = cache.getHashMap("map", "username", "password");
		if (list != null && !list.isEmpty()) {
			System.out.println(list.toString());
		}
	}

	public void batch(RedisUtils cache) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("test_1", "yangc");
		map.put("test_2", 123);
		map.put("test_3", new User(12, "jim", "123456"));
		map.put("test_4", true);
		cache.batchSet(map);
	}

}
