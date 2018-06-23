package com.yangc.utils.cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LruCacheUtils<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 1L;

	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	private int capacity;

	public LruCacheUtils() {
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public LruCacheUtils(int capacity) {
		super(capacity, 0.75f, true);
		this.capacity = capacity;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return this.size() > this.capacity;
	}

	public static void main(String[] args) {
		LruCacheUtils<String, String> t = new LruCacheUtils<String, String>(5);
		t.put("a", "a");
		t.put("b", "b");
		t.put("c", "c");
		t.put("d", "d");
		t.put("e", "e");
		t.get("b");
		t.get("a");
		t.get("d");
		t.put("f", "f");
		for (Map.Entry<String, String> entry : t.entrySet()) {
			System.out.println(entry.getKey() + " == " + entry.getValue());
		}
	}

}
