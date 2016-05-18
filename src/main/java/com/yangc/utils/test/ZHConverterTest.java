package com.yangc.utils.test;

import com.yangc.utils.lang.ZHConverterUtils;

public class ZHConverterTest {

	public static void main(String[] args) {
		System.out.println(ZHConverterUtils.convert("我是杨晨，helloworld，沈阳市皇姑区加华小城", ZHConverterUtils.ConverterType.TRADITIONAL));
		System.out.println(ZHConverterUtils.convert("我是楊晨，helloworld，瀋陽市皇姑區加華小城", ZHConverterUtils.ConverterType.SIMPLIFIED));
	}

}
