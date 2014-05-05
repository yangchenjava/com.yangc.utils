package com.yangc.utils.excel;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class ReadExcel2007 {

	enum CellDataType {
		BOOL, ERROR, FORMULA, INLINESTR, SSTINDEX, NUMBER, DATE, NULL
	}

	private class ExcelSAXHandler extends DefaultHandler {
		private SharedStringsTable sst; // 共享字符串表

		private List<Map<String, String>> tableContents = new ArrayList<Map<String, String>>();

		private StringBuilder lastContents = new StringBuilder(); // 上一次的内容
		private int currentRow; // 当前行
		private int currentCol; // 当前列
		private boolean isTElement; // T元素标识
		private CellDataType nextDataType = CellDataType.NUMBER; // 单元格数据类型

		private ExcelSAXHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (qName.equals("c")) {
				String cellType = attributes.getValue("t");
				String cellStyleStr = attributes.getValue("s");

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
					
				}
			}
			this.isTElement = qName.equals("t");
			this.lastContents.setLength(0);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			// 得到单元格内容的值
			this.lastContents.append(ch, start, length);
		}
	}

	public List<Map<String, String>> read(String path, Map<Integer, String> headNames, int beginRownum) {
		if (!path.endsWith("xlsx")) {
			throw new IllegalArgumentException("This is not 2007 Excel");
		}

		try {
			XSSFReader reader = new XSSFReader(OPCPackage.open(path));
			XMLReader xmlReader = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			xmlReader.setContentHandler(new ExcelSAXHandler(reader.getSharedStringsTable()));

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
		return null;
	}

}
