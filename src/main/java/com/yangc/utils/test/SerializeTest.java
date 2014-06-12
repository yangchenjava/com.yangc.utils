package com.yangc.utils.test;

import com.yangc.utils.io.SerializeUtils;

public class SerializeTest {

	public static void main(String[] args) {
		User user = new User(10, "yangc", "123456");
		byte[] b = SerializeUtils.serialize(user);
		User u = (User) SerializeUtils.deserialize(b);
		System.out.println(u.toString());
	}

}
