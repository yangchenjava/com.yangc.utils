package com.yangc.utils.test;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import oracle.jdbc.OracleTypes;

import com.yangc.utils.jdbc.JdbcUtils;
import com.yangc.utils.prop.PropertiesUtils;

@SuppressWarnings("unchecked")
public class DBTest {

	public static void main(String[] args) {
		PropertiesUtils prop = PropertiesUtils.getInstance("/jdbc.properties");
		String driver = prop.getProperty("oracle.driver");
		String url = prop.getProperty("oracle.url");
		String username = prop.getProperty("oracle.username");
		String password = prop.getProperty("oracle.password");

		JdbcUtils jdbc = new JdbcUtils();
		Connection conn = jdbc.getConnection(driver, url, username, password);

		String sql = "{call my_procedure(?, ?)}";
		Map<Integer, Object> in = new HashMap<Integer, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put(1, 1);
			}
		};
		Map<Integer, Integer> out = new HashMap<Integer, Integer>() {
			private static final long serialVersionUID = 1L;
			{
				put(2, OracleTypes.CURSOR);
			}
		};
		List<Map<String, Object>> mapList = (List<Map<String, Object>>) jdbc.executeProcedure(conn, sql, in, out).get(2);
		for (Map<String, Object> map : mapList) {
			for (Entry<String, Object> en : map.entrySet()) {
				System.out.println(en.getKey() + " ---> " + en.getValue());
			}
			System.out.println();
		}

		// sql = "{call my_procedure_2(?, ?)}";
		// in.clear();
		// in.put(1, 10);
		// in.put(2, "yangc");
		// jdbc.executeProcedure(sql, in, null);

		sql = "{call my_procedure_3(?, ?, ?, ?, ?, ?)}";
		in.clear();
		in.put(1, "t_sys_department");
		in.put(2, 10);
		in.put(3, 10);
		out.clear();
		out.put(4, OracleTypes.NUMBER);
		out.put(5, OracleTypes.NUMBER);
		out.put(6, OracleTypes.CURSOR);
		Map<Integer, Object> result = jdbc.executeProcedure(conn, sql, in, out);
		System.out.println("totalCount=" + result.get(4));
		System.out.println("totalPage=" + result.get(5));
		mapList = (List<Map<String, Object>>) result.get(6);
		for (Map<String, Object> map : mapList) {
			for (Entry<String, Object> en : map.entrySet()) {
				System.out.println(en.getKey() + " ---> " + en.getValue());
			}
			System.out.println();
		}

		jdbc.close(conn);
	}
}
