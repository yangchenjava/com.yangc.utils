package com.yangc.utils.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yangc.utils.excel.ExcelBean;
import com.yangc.utils.excel.SheetBean;
import com.yangc.utils.excel.WriteExcel;

public class WriteExcelTest {

	public static void main(String[] args) {
		SheetBean sheet_1 = new SheetBean();
		sheet_1.setSheetName("工作簿_1");
		sheet_1.setTitle("我  是  大  标  题_1");
		sheet_1.setRownum(1);
		String[] headNames_1 = { "姓名", "职务", "电话", "住址", "邮编", "备注" };
		sheet_1.setHeadNames(headNames_1);
		List<Map<String, Object>> tableContents_1 = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 1000; i++) {
			Map<String, Object> rowContents = new HashMap<String, Object>();
			for (int j = 0; j < headNames_1.length; j++) {
				rowContents.put(headNames_1[j], headNames_1[j] + i);
			}
			tableContents_1.add(rowContents);
		}
		sheet_1.setTableContents(tableContents_1);

		SheetBean sheet_2 = new SheetBean();
		sheet_2.setSheetName("工作簿_2");
		sheet_2.setTitle("我是大标题_2");
		sheet_2.setRownum(5);
		String[] headNames_2 = { "姓名", "职务", "电话", "住址", "邮编", "备注" };
		sheet_2.setHeadNames(headNames_2);
		List<Map<String, Object>> tableContents_2 = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < 1000; i++) {
			Map<String, Object> rowContents = new HashMap<String, Object>();
			for (int j = 0; j < headNames_2.length; j++) {
				if (j == 2) {
					rowContents.put(headNames_2[j], i);
				} else if (j == 5) {
					rowContents.put(headNames_2[j], new Date());
				} else {
					rowContents.put(headNames_2[j], headNames_2[j] + i);
				}
			}
			tableContents_2.add(rowContents);
		}
		sheet_2.setTableContents(tableContents_2);

		List<SheetBean> sheets = new ArrayList<SheetBean>();
		sheets.add(sheet_1);
		sheets.add(sheet_2);

		ExcelBean excelBean = new ExcelBean();
		excelBean.setPath("src/main/resources/test.xlsx");
		excelBean.setSheets(sheets);

		WriteExcel excel = new WriteExcel();
		System.out.println(excel.write(excelBean));
	}

}
