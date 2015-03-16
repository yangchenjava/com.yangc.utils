package com.yangc.utils.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mongodb.MongoClient;
import com.yangc.utils.db.MongodbUtils;

public class MongodbTest {

	public static void main(String[] args) {
		MongodbUtils mongodbUtils = new MongodbUtils();
		MongoClient mongoClient = mongodbUtils.connect("127.0.0.1", 27017);
		insert(mongodbUtils, mongoClient);
		findAll(mongodbUtils, mongoClient);
		getCount(mongodbUtils, mongoClient);
		mongodbUtils.close(mongoClient);
	}

	public static void insert(MongodbUtils mongodbUtils, MongoClient mongoClient) {
		List<LinkedHashMap<String, Object>> records = new ArrayList<LinkedHashMap<String, Object>>();

		LinkedHashMap<String, Object> record_1 = new LinkedHashMap<String, Object>();
		record_1.put("username", "aaa");
		record_1.put("password", "111");
		record_1.put("age", 34);
		records.add(record_1);

		LinkedHashMap<String, Object> record_2 = new LinkedHashMap<String, Object>();
		record_2.put("username", "bbb");
		record_2.put("password", "222");
		record_2.put("age", 38);
		records.add(record_2);
		System.out.println("insert=" + mongodbUtils.insert(mongoClient, "blog", "T_SYS_TEST", records));
	}

	public static void findAll(MongodbUtils mongodbUtils, MongoClient mongoClient) {
		Map<String, Object> conditions = new HashMap<String, Object>();
		Map<String, Object> condition_1 = new HashMap<String, Object>();
		condition_1.put("$gte", 25);
		conditions.put("age", condition_1);

		Map<String, Object> fields = new HashMap<String, Object>();
		fields.put("_id", 0);
		fields.put("username", 1);
		fields.put("age", 1);
		List<Map<String, Object>> mapList = mongodbUtils.findAll(mongoClient, "blog", "T_SYS_TEST", conditions, fields);
		for (Map<String, Object> map : mapList) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				System.out.println(entry.getKey() + " == " + entry.getValue());
			}
			System.out.println("=======================");
		}
	}

	public static void getCount(MongodbUtils mongodbUtils, MongoClient mongoClient) {
		System.out.println("count=" + mongodbUtils.getCount(mongoClient, "blog", "T_SYS_TEST", null));
	}

}
