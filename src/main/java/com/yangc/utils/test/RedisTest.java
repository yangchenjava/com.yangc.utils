package com.yangc.utils.test;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.yangc.utils.cache.RedisUtils;

public class RedisTest {

	public static void main(String[] args) {
		User u_1 = new User(1, "yangc_1", "123456_1");
		User u_2 = new User(2, "yangc_2", "123456_2");
		User u_3 = new User(3, "yangc_3", "123456_3");

		List<User> list = new ArrayList<User>();
		list.add(u_1);
		list.add(u_2);
		list.add(u_3);

		RedisUtils cache = RedisUtils.getInstance();
		cache.set("users", list);
		List<User> users = cache.get("users", new TypeToken<List<User>>() {
		});

		for (User user : users) {
			System.out.println(user);
		}
	}
}
