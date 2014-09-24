package com.yangc.utils.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.yangc.utils.excel.ReadExcel2003;

public class ReadExcel2003Test {

	public static void main(String[] args) {
		Map<Integer, String> headNames = new HashMap<Integer, String>();
		headNames.put(0, "test_0");
		headNames.put(1, "test_1");
		headNames.put(2, "test_2");
		headNames.put(3, "test_3");
		headNames.put(4, "test_4");

		ReadExcel2003 excel = new ReadExcel2003();
		List<Map<String, String>> list = excel.read("src/main/resources/test.xls", headNames, 0);
		for (Map<String, String> map : list) {
			System.out.print(StringUtils.isBlank(map.get("test_0")) ? "" : map.get("test_0") + "\t");
			System.out.print(StringUtils.isBlank(map.get("test_1")) ? "" : map.get("test_1") + "\t");
			System.out.print(StringUtils.isBlank(map.get("test_2")) ? "" : map.get("test_2") + "\t");
			System.out.print(StringUtils.isBlank(map.get("test_3")) ? "" : map.get("test_3") + "\t");
			System.out.print(StringUtils.isBlank(map.get("test_4")) ? "" : map.get("test_4") + "\t");
			System.out.println();
		}
	}

}
