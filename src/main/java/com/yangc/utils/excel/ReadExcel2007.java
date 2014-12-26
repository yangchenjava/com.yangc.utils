package com.yangc.utils.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class ReadExcel2007 {

	private List<Map<String, String>> tableContents = new ArrayList<Map<String, String>>();

	private Map<Integer, String> headNames;
	private int beginRownum;

	enum CellDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER, DATE, NULL
	}

	private class ExcelSAXHandler extends DefaultHandler {
		private SharedStringsTable sst; // 共享字符串表
		private StylesTable st; // 单元格样式表

		private Map<String, String> currentRow; // 当前行记录
		private StringBuilder lastContents = new StringBuilder(); // 上一次的内容
		private int currentRownum = -1; // 当前行下标
		private int row, col; // 正在解析的行列下标值
		private CellDataType nextDataType; // 单元格数据类型
		private DataFormatter df = new DataFormatter();
		private short formatIndex;
		private String formatString;

		private ExcelSAXHandler(SharedStringsTable sst, StylesTable st) {
			this.sst = sst;
			this.st = st;
		}

		/**
		 * @功能: 解析行列值 C5 表示5行3列, 解析的下标值为 row:4 col:2
		 * @作者: yangc
		 * @创建日期: 2014年5月6日 下午10:00:58
		 * @param colRowNum
		 */
		private void loadColRowNum(String colRowNum) {
			int firstDigit = -1;
			for (int i = 0; i < colRowNum.length(); i++) {
				if (Character.isDigit(colRowNum.charAt(i))) {
					firstDigit = i;
					break;
				}
			}
			this.row = Integer.parseInt(colRowNum.substring(firstDigit)) - 1;
			String columnName = colRowNum.substring(0, firstDigit);
			int column = 0;
			for (int i = 0; i < columnName.length(); i++) {
				column += 26 * i + columnName.charAt(i) - 'A';
			}
			this.col = column;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			this.lastContents.setLength(0);
			if (StringUtils.equals(qName, "c")) {
				String colRowNum = attributes.getValue("r").toUpperCase();
				String cellType = attributes.getValue("t");
				String cellStyleStr = attributes.getValue("s");

				// 载入当前行列
				this.loadColRowNum(colRowNum);

				this.nextDataType = CellDataType.NUMBER;
				this.formatIndex = -1;
				this.formatString = null;

				if (StringUtils.equals(cellType, "b")) {
					this.nextDataType = CellDataType.BOOL;
				} else if (StringUtils.equals(cellType, "e")) {
					this.nextDataType = CellDataType.ERROR;
				} else if (StringUtils.equals(cellType, "str")) {
					this.nextDataType = CellDataType.FORMULA;
				} else if (StringUtils.equals(cellType, "inlineStr")) {
					this.nextDataType = CellDataType.INLINESTR;
				} else if (StringUtils.equals(cellType, "s")) {
					this.nextDataType = CellDataType.SSTINDEX;
				} else if (StringUtils.isNotBlank(cellStyleStr)) {
					XSSFCellStyle style = st.getStyleAt(Integer.parseInt(cellStyleStr));
					this.formatIndex = style.getDataFormat();
					this.formatString = style.getDataFormatString();
					if (StringUtils.isBlank(this.formatString)) {
						this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
					}
				}
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (StringUtils.equals(qName, "t")) {
				this.addData(this.lastContents.toString());
			} else if (StringUtils.equals(qName, "v")) {
				String value = null;
				switch (this.nextDataType) {
				case BOOL:
					value = this.lastContents.charAt(0) == '0' ? "FALSE" : "TRUE";
					break;
				case ERROR:
					value = "ERROR:" + this.lastContents.toString();
					break;
				case FORMULA:
					value = this.lastContents.toString();
					break;
				case INLINESTR:
					value = new XSSFRichTextString(this.lastContents.toString()).getString();
					break;
				case SSTINDEX:
					// 这里解析出来的是索引值, 不是真正的单元格内容
					value = new XSSFRichTextString(this.sst.getEntryAt(Integer.parseInt(this.lastContents.toString()))).getString();
					break;
				case NUMBER:
					if (HSSFDateUtil.isADateFormat(this.formatIndex, this.formatString)) {
						value = DateFormatUtils.format(HSSFDateUtil.getJavaDate(Double.parseDouble(this.lastContents.toString())), "yyyy-MM-dd");
					} else if (StringUtils.isBlank(this.formatString)) {
						value = this.lastContents.toString();
					} else {
						value = df.formatRawCellContents(Double.parseDouble(this.lastContents.toString()), formatIndex, formatString);
					}
					break;
				case DATE:
					value = df.formatRawCellContents(Double.parseDouble(this.lastContents.toString()), formatIndex, formatString);
					break;
				case NULL:
					value = "";
					break;
				}
				this.addData(value);
			} else if (StringUtils.equals(qName, "row")) {
				// 读到行尾
				this.currentRownum = -1;
			}
		}

		private void addData(String value) {
			if (this.row >= beginRownum && headNames.containsKey(this.col)) {
				if (this.currentRownum != this.row) {
					this.currentRow = new HashMap<String, String>();
					tableContents.add(this.currentRow);
					this.currentRownum = this.row;
				}
				this.currentRow.put(headNames.get(this.col), value.trim());
			}
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// 得到单元格内容的值
			this.lastContents.append(ch, start, length);
		}
	}

	/**
	 * @功能: 解析2007版excel
	 * @作者: yangc
	 * @创建日期: 2014年5月5日 下午8:05:27
	 * @param path 文件路径
	 * @param headNames 要解析的列号(从0开始)
	 * @param beginRownum 开始解析的行号(从0开始)
	 * @return
	 */
	public List<Map<String, String>> read(String path, Map<Integer, String> headNames, int beginRownum) {
		if (!path.endsWith("xlsx")) {
			throw new IllegalArgumentException("This is not 2007 Excel");
		}
		this.headNames = headNames;
		this.beginRownum = beginRownum;

		try {
			XSSFReader reader = new XSSFReader(OPCPackage.open(path));
			XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			xmlReader.setContentHandler(new ExcelSAXHandler(reader.getSharedStringsTable(), reader.getStylesTable()));

			Iterator<InputStream> sheets = reader.getSheetsData();
			while (sheets.hasNext()) {
				InputStream sheet = sheets.next();
				xmlReader.parse(new InputSource(sheet));
				sheet.close();
			}
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OpenXML4JException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		}
		return tableContents;
	}

}
