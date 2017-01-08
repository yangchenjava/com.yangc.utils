package com.yangc.utils.test;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;

public class CSVTest {

	public static void main(String[] args) throws IOException {
		Reader reader = new InputStreamReader(new BOMInputStream(new FileInputStream("src/test/resources/test.csv")), "UTF-8");
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader("a", "b", "c", "d", "e", "f"));
		try {
			for (CSVRecord record : parser) {
				System.out.println(record.get("a"));
				System.out.println(record.get("b"));
				System.out.println(record.get("c"));
				System.out.println(record.get("d"));
				System.out.println(record.get("e"));
				System.out.println(record.get("f"));
			}
		} finally {
			parser.close();
			reader.close();
		}

		Reader in = new FileReader("src/test/resources/test.csv");
		Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader("a", "b", "c", "d", "e", "f").parse(in);
		for (CSVRecord record : records) {
			System.out.println(record.get("a"));
			System.out.println(record.get("b"));
			System.out.println(record.get("c"));
			System.out.println(record.get("d"));
			System.out.println(record.get("e"));
			System.out.println(record.get("f"));
		}
		in.close();
	}

}
