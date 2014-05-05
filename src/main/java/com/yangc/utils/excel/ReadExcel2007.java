package com.yangc.utils.excel;

import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ReadExcel2007 {

	private DefaultHandler excelSAXHandler = new DefaultHandler() {
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			super.endElement(uri, localName, qName);
		}

		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);
		}
	};

	public List<Map<String, String>> read(String path, Map<Integer, String> headNames, int beginRownum) {
		if (!path.endsWith("xlsx")) {
			throw new IllegalArgumentException("This is not 2007 Excel");
		}
		return null;
	}

}
