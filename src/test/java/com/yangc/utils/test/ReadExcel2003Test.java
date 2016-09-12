package com.yangc.utils.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;

import com.yangc.utils.excel.ReadExcel2003;

public class ReadExcel2003Test {

	public static void main(String[] args) {
		Map<Integer, String> headNames = new HashMap<Integer, String>();
		headNames.put(0, "test_0");
		headNames.put(1, "test_1");
		headNames.put(2, "test_2");
		headNames.put(3, "test_3");
		headNames.put(4, "test_4");
		headNames.put(5, "test_5");
		headNames.put(6, "test_6");

		ReadExcel2003 excel = new ReadExcel2003();
		List<Map<String, String>> list = excel.read("src/test/resources/test.xls", headNames, 0);
		for (Map<String, String> map : list) {
			System.out.print(MapUtils.getString(map, "test_0", "") + "\t");
			System.out.print(MapUtils.getString(map, "test_1", "") + "\t");
			System.out.print(MapUtils.getString(map, "test_2", "") + "\t");
			System.out.print(MapUtils.getString(map, "test_3", "") + "\t");
			System.out.print(MapUtils.getString(map, "test_4", "") + "\t");
			System.out.print(MapUtils.getString(map, "test_5", "") + "\t");
			System.out.print(MapUtils.getString(map, "test_6", "") + "\t");
			System.out.println();
		}
	}

}
