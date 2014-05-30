package com.yangc.utils.test;

import com.yangc.utils.cache.RedisUtils;

public class RedisTest {

	public static void main(String[] args) {
		RedisUtils cache = RedisUtils.getInstance();
		cache.set("test", "hello_world");
		System.out.println(cache.get("test"));
	}

}
