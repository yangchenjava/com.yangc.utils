package com.yangc.utils.json;

import com.google.gson.Gson;

public class JsonUtils {

	private static Gson gson;

	static {
		gson = new Gson();
	}

	private JsonUtils() {
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		if (json == null || json.equals("") || json.equals("null")) {
			return null;
		}
		return JsonUtils.gson.fromJson(json, clazz);
	}

	public static String toJson(Object src) {
		return JsonUtils.gson.toJson(src);
	}

	public static String toJsonTree(Object src) {
		return JsonUtils.gson.toJsonTree(src).toString();
	}

}
