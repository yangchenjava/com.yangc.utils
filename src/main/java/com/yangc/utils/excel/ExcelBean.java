package com.yangc.utils.excel;

import java.util.List;

public class ExcelBean {

	private String path; // excel输出的路径
	private List<SheetBean> sheets; // 工作簿

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<SheetBean> getSheets() {
		return sheets;
	}

	public void setSheets(List<SheetBean> sheets) {
		this.sheets = sheets;
	}

}
