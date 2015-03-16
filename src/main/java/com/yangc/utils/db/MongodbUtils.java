package com.yangc.utils.db;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;

public class MongodbUtils {

	/**
	 * @功能: 连接mongodb服务器
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:45:32
	 * @param host
	 * @param port
	 * @return
	 */
	public MongoClient connect(String host, int port) {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(new ServerAddress(host, port));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return mongoClient;
	}

	/**
	 * @功能: 连接mongodb服务器(多个分片服务器的地址)
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:46:26
	 * @param hosts
	 * @return
	 */
	public MongoClient connect(Map<String, Integer> hosts) {
		MongoClient mongoClient = null;
		if (MapUtils.isNotEmpty(hosts)) {
			try {
				List<ServerAddress> seeds = new ArrayList<ServerAddress>();
				for (Map.Entry<String, Integer> entry : hosts.entrySet()) {
					seeds.add(new ServerAddress(entry.getKey(), entry.getValue()));
				}
				mongoClient = new MongoClient(seeds);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
		return mongoClient;
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
		DB db = mongoClient.getDB(databaseName);
		DBCollection dbCollection = db.getCollection(collectionName);

		if (CollectionUtils.isNotEmpty(records)) {
			List<DBObject> list = new ArrayList<DBObject>(records.size());
			for (LinkedHashMap<String, Object> record : records) {
				list.add(new BasicDBObject(record));
			}
			dbCollection.insert(list);
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
	 * @param conditions 条件
	 * @return
	 */
	public boolean remove(MongoClient mongoClient, String databaseName, String collectionName, Map<String, Object> conditions) {
		DB db = mongoClient.getDB(databaseName);
		DBCollection dbCollection = db.getCollection(collectionName);

		dbCollection.remove(MapUtils.isEmpty(conditions) ? new BasicDBObject() : new BasicDBObject(conditions));
		return true;
	}

	/**
	 * @功能: 修改数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:50:02
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param conditions 条件
	 * @param newValue 更新的新值
	 * @param upsert true,如果查询不到记录,则插入修改的数据
	 * @param multi 是否修改多条数据
	 * @return
	 */
	public boolean update(MongoClient mongoClient, String databaseName, String collectionName, Map<String, Object> conditions, Map<String, Object> newValue, boolean upsert, boolean multi) {
		DB db = mongoClient.getDB(databaseName);
		DBCollection dbCollection = db.getCollection(collectionName);

		if (MapUtils.isNotEmpty(newValue)) {
			dbCollection.update(MapUtils.isEmpty(conditions) ? new BasicDBObject() : new BasicDBObject(conditions), new BasicDBObject(newValue), upsert, multi);
			return true;
		}
		return false;
	}

	/**
	 * @功能: 分页查询数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午3:59:14
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param conditions 条件
	 * @param fields 要查询的字段
	 * @param skip 跳过多少数据
	 * @param limit 最多查询多少数据
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> find(MongoClient mongoClient, String databaseName, String collectionName, Map<String, Object> conditions, Map<String, Object> fields, int skip, int limit) {
		DB db = mongoClient.getDB(databaseName);
		DBCollection dbCollection = db.getCollection(collectionName);

		DBCursor dbCursor = dbCollection.find(MapUtils.isEmpty(conditions) ? null : new BasicDBObject(conditions), MapUtils.isEmpty(fields) ? null : new BasicDBObject(fields)).skip(skip).limit(limit);

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(dbCursor.size());
		while (dbCursor.hasNext()) {
			result.add(dbCursor.next().toMap());
		}
		return result;
	}

	/**
	 * @功能: 查询全部数据
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午4:01:44
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param conditions 条件
	 * @param fields 要查询的字段
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> findAll(MongoClient mongoClient, String databaseName, String collectionName, Map<String, Object> conditions, Map<String, Object> fields) {
		DB db = mongoClient.getDB(databaseName);
		DBCollection dbCollection = db.getCollection(collectionName);

		DBCursor dbCursor = dbCollection.find(MapUtils.isEmpty(conditions) ? null : new BasicDBObject(conditions), MapUtils.isEmpty(fields) ? null : new BasicDBObject(fields));

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>(dbCursor.size());
		while (dbCursor.hasNext()) {
			result.add(dbCursor.next().toMap());
		}
		return result;
	}

	/**
	 * @功能: 查询记录数
	 * @作者: yangc
	 * @创建日期: 2015年3月14日 下午4:02:35
	 * @param mongoClient
	 * @param databaseName 数据库名
	 * @param collectionName 集合名(表)
	 * @param conditions 条件
	 * @return
	 */
	public long getCount(MongoClient mongoClient, String databaseName, String collectionName, Map<String, Object> conditions) {
		DB db = mongoClient.getDB(databaseName);
		DBCollection dbCollection = db.getCollection(collectionName);

		return dbCollection.count(MapUtils.isEmpty(conditions) ? new BasicDBObject() : new BasicDBObject(conditions));
	}

}
