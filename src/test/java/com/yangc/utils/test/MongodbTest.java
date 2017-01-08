package com.yangc.utils.test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.yangc.utils.db.MongodbUtils;

public class MongodbTest {

	public static void main(String[] args) {
		MongodbUtils mongodbUtils = new MongodbUtils();
		MongoClient mongoClient = mongodbUtils.connect("192.168.7.31", 27017, "root", "root");
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

		LinkedHashMap<String, Object> record_3 = new LinkedHashMap<String, Object>();
		record_3.put("username", "ccc");
		record_3.put("password", "333");
		record_3.put("age", 18);
		records.add(record_3);
		System.out.println("insert=" + mongodbUtils.batchInsert(mongoClient, "test", "t_user", records));
	}

	public static void findAll(MongodbUtils mongodbUtils, MongoClient mongoClient) {
		Bson filter = Filters.gte("age", 25);
		Bson projection = Projections.fields(Projections.exclude("_id"), Projections.include("username", "age"));
		List<Map<String, Object>> mapList = mongodbUtils.findAll(mongoClient, "test", "t_user", filter, projection, null);
		for (Map<String, Object> map : mapList) {
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				System.out.println(entry.getKey() + " == " + entry.getValue());
			}
			System.out.println("=======================");
		}
	}

	public static void getCount(MongodbUtils mongodbUtils, MongoClient mongoClient) {
		System.out.println("count=" + mongodbUtils.getCount(mongoClient, "test", "t_user", null));
	}

}
