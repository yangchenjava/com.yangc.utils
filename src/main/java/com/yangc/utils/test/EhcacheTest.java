package com.yangc.utils.test;

import com.yangc.utils.cache.EhCacheUtils;

public class EhcacheTest {

	public static void main(String[] args) {
		EhCacheUtils cache = EhCacheUtils.getInstance(null);
		cache.add("myCache", "user", new User(1, "yangc", "yangc"));
		User user = cache.get("myCache", "user", User.class);
		System.out.println(user);
	}

}
