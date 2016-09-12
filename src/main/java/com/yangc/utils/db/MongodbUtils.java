package com.yangc.utils.db;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

public class MongodbUtils {

	/**
	 * @功能: 连接mongodb服务器
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:45:32
	 * @param host
	 * @param port
	 * @return
	 */
	public MongoClient connect(String host, int port, String username, String password) {
		List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
		credentialsList.add(MongoCredential.createCredential(username, "admin", password.toCharArray()));

		MongoClientOptions options = MongoClientOptions.builder().readPreference(ReadPreference.secondaryPreferred()).build();

		return new MongoClient(new ServerAddress(host, port), credentialsList, options);
	}

	/**
	 * @功能: 连接mongodb服务器(多个分片服务器的地址)
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:46:26
	 * @param hosts
	 * @return
	 */
	public MongoClient connect(List<ServerAddress> addrs, String username, String password) {
		List<MongoCredential> credentialsList = new ArrayList<MongoCredential>();
		credentialsList.add(MongoCredential.createCredential(username, "admin", password.toCharArray()));

		MongoClientOptions options = MongoClientOptions.builder().readPreference(ReadPreference.secondaryPreferred()).build();

		return new MongoClient(addrs, credentialsList, options);
	}

	/**
	 * @功能: 关闭连接
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:47:20
	 * @param mongoClient
	 */
	public void close(MongoClient mongoClient) {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	/**
	 * @功能: 插入数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:47:44
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param records 插入的记录
	 * @return
	 */
	public boolean insert(MongoClient mongoClient, String databaseName, String collectionName, List<LinkedHashMap<String, Object>> records) {
		MongoDatabase database = mongoClient.getDatabase(databaseName);
		MongoCollection<Document> collection = database.getCollection(collectionName);

		if (CollectionUtils.isNotEmpty(records)) {
			List<Document> documents = new ArrayList<Document>(records.size());
			for (LinkedHashMap<String, Object> record : records) {
				Document document = new Document(record);
				documents.add(document);
			}
			collection.insertMany(documents);
			return true;
		}
		return false;
	}

	/**
	 * @功能: 删除数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:48:59
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param filter 条件
	 * @return
	 */
	public boolean remove(MongoClient mongoClient, String databaseName, String collectionName, Bson filter) {
		MongoDatabase database = mongoClient.getDatabase(databaseName);
		MongoCollection<Document> collection = database.getCollection(collectionName);
		collection.deleteMany(filter);
		return true;
	}

	/**
	 * @功能: 修改数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:50:02
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param filter 条件
	 * @param update 更新的新值
	 * @param upsert true,如果查询不到记录,则插入修改的数据
	 * @return
	 */
	public boolean update(MongoClient mongoClient, String databaseName, String collectionName, Bson filter, Bson update, boolean upsert) {
		MongoDatabase database = mongoClient.getDatabase(databaseName);
		MongoCollection<Document> collection = database.getCollection(collectionName);
		collection.updateMany(filter, update, new UpdateOptions().upsert(upsert));
		return false;
	}

	/**
	 * @功能: 查询数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:59:14
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param filter 条件(nullable)
	 * @param projection 要查询的字段(nullable)
	 * @param sort 排序(nullable)
	 * @param skip 跳过多少数据
	 * @param limit 最多查询多少数据
	 * @return
	 */
	public List<Map<String, Object>> find(MongoClient mongoClient, String databaseName, String collectionName, Bson filter, Bson projection, Bson sort, int skip, int limit) {
		final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		MongoDatabase database = mongoClient.getDatabase(databaseName);
		MongoCollection<Document> collection = database.getCollection(collectionName);
		FindIterable<Document> iterable = filter == null ? collection.find() : collection.find(filter);
		iterable.projection(projection).sort(sort).skip(skip).limit(limit).forEach(new Block<Document>() {
			@Override
			public void apply(Document t) {
				result.add(new LinkedHashMap<String, Object>(t));
			}
		});
		return result;
	}

	/**
	 * @功能: 查询数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:59:14
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param filter 条件(nullable)
	 * @param projection 要查询的字段(nullable)
	 * @param sort 排序(nullable)
	 * @return
	 */
	public List<Map<String, Object>> findAll(MongoClient mongoClient, String databaseName, String collectionName, Bson filter, Bson projection, Bson sort) {
		final List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		MongoDatabase database = mongoClient.getDatabase(databaseName);
		MongoCollection<Document> collection = database.getCollection(collectionName);
		FindIterable<Document> iterable = filter == null ? collection.find() : collection.find(filter);
		iterable.projection(projection).sort(sort).forEach(new Block<Document>() {
			@Override
			public void apply(Document t) {
				result.add(new LinkedHashMap<String, Object>(t));
			}
		});
		return result;
	}

	/**
	 * @功能: 查询记录数
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午4:02:35
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param filter 条件
	 * @return
	 */
	public long getCount(MongoClient mongoClient, String databaseName, String collectionName, Bson filter) {
		MongoDatabase database = mongoClient.getDatabase(databaseName);
		MongoCollection<Document> collection = database.getCollection(collectionName);
		return collection.count(filter);
	}

}
