package com.yangc.utils.excel;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

public class WriteExcel {

	private Workbook workbook;
	private CreationHelper helper;

	private CellStyle titleCellStyle; // 标题的样式
	private CellStyle headCellStyle; // 表头的样式
	private CellStyle contentCellStyle; // 内容的样式
	private CellStyle numberCellStyle; // 保留两位小数的样式

	/**
	 * @功能: 写excel
	 * @作者: yangc
	 * @创建日期: 2014年5月5日 下午8:08:41
	 * @param excelBean
	 * @return
	 */
	public boolean write(ExcelBean excelBean) {
		if (excelBean.getPath().endsWith("xlsx")) {
			// 2007以上
			workbook = new SXSSFWorkbook(200);
		} else {
			// 97-2003
			workbook = new HSSFWorkbook();
		}
		List<SheetBean> sheets = excelBean.getSheets();
		for (SheetBean sheetBean : sheets) {
			this.createSheet(sheetBean);
		}
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(excelBean.getPath());
			workbook.write(fos);
			fos.flush();
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fos != null) fos.close();
				if (workbook != null) workbook.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	private void createSheet(SheetBean sheetBean) {
		helper = workbook.getCreationHelper();

		Font titleFont = workbook.createFont();
		titleFont.setFontHeightInPoints((short) 28);
		titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		Font headFont = workbook.createFont();
		headFont.setFontHeightInPoints((short) 12);
		headFont.setBoldweight(Font.BOLDWEIGHT_BOLD);

		Font contentFont = workbook.createFont();
		contentFont.setFontHeightInPoints((short) 12);

		titleCellStyle = workbook.createCellStyle();
		titleCellStyle.setFont(titleFont);
		titleCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		titleCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		headCellStyle = workbook.createCellStyle();
		headCellStyle.setFont(headFont);
		headCellStyle.setBorderTop(CellStyle.BORDER_MEDIUM);
		headCellStyle.setBorderRight(CellStyle.BORDER_MEDIUM);
		headCellStyle.setBorderBottom(CellStyle.BORDER_MEDIUM);
		headCellStyle.setBorderLeft(CellStyle.BORDER_MEDIUM);
		headCellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.index);
		headCellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
		headCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		headCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		contentCellStyle = workbook.createCellStyle();
		contentCellStyle.setFont(contentFont);
		contentCellStyle.setBorderTop(CellStyle.BORDER_THIN);
		contentCellStyle.setBorderRight(CellStyle.BORDER_THIN);
		contentCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		contentCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		contentCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		contentCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		numberCellStyle = workbook.createCellStyle();
		numberCellStyle.setDataFormat(workbook.createDataFormat().getFormat("#.##"));
		numberCellStyle.setFont(contentFont);
		numberCellStyle.setBorderTop(CellStyle.BORDER_THIN);
		numberCellStyle.setBorderRight(CellStyle.BORDER_THIN);
		numberCellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		numberCellStyle.setBorderLeft(CellStyle.BORDER_THIN);
		numberCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		numberCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);

		// 工作簿
		Sheet sheet = workbook.createSheet(sheetBean.getSheetName());

		// 标题
		Cell titleCell = sheet.createRow(0).createCell(0);
		titleCell.setCellStyle(titleCellStyle);
		titleCell.setCellValue(helper.createRichTextString(sheetBean.getTitle()));
		sheet.addMergedRegion(CellRangeAddress.valueOf(new StringBuilder("$A$1:$").append((char) ('A' + sheetBean.getMergeColCount() - 1)).append("$1").toString()));

		// 表头
		int rownum = sheetBean.getRownum();
		Map<Integer, String> headNames = sheetBean.getHeadNames();
		if (headNames != null) {
			Row headRow = sheet.createRow(rownum);
			for (Entry<Integer, String> entry : headNames.entrySet()) {
				Cell headCell = headRow.createCell(entry.getKey());
				headCell.setCellStyle(headCellStyle);
				headCell.setCellValue(helper.createRichTextString(entry.getValue()));
				sheet.setColumnWidth(entry.getKey(), 5000);
			}
			rownum++;
		}

		// 内容
		List<Map<String, Object>> tableContents = sheetBean.getTableContents();
		if (headNames != null && tableContents != null) {
			for (Map<String, Object> rowContents : tableContents) {
				Row row = sheet.createRow(rownum);
				this.createCell(row, headNames, rowContents);
				rownum++;
			}
		}
	}

	private void createCell(Row row, Map<Integer, String> headNames, Map<String, Object> rowContents) {
		for (Entry<Integer, String> entry : headNames.entrySet()) {
			Cell cell = row.createCell(entry.getKey());
			cell.setCellStyle(contentCellStyle);

			Object cellContents = rowContents.get(entry.getValue());
			if (cellContents instanceof String) {
				cell.setCellValue(helper.createRichTextString((String) cellContents));
			} else if (cellContents instanceof Integer || cellContents instanceof Long) {
				cell.setCellValue(helper.createRichTextString(cellContents.toString()));
			} else if (cellContents instanceof Short || cellContents instanceof Double) {
				cell.setCellStyle(numberCellStyle);
				cell.setCellValue((Double) cellContents);
			} else if (cellContents instanceof Date) {
				cell.setCellValue(helper.createRichTextString(DateFormatUtils.format((Date) cellContents, "yyyy-MM-dd")));
			} else if (cellContents instanceof Boolean) {
				cell.setCellValue((Boolean) cellContents);
			}
		}
	}

}
