package com.yangc.utils.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class JsonUtils {

	private static Gson gson;

	static {
		gson = new Gson();
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		if (json == null || json.equals("") || json.equals("null")) {
			return null;
		}
		return JsonUtils.gson.fromJson(json, clazz);
	}

	public static <T> T fromJson(String json, TypeToken<T> typeToken) {
		if (json == null || json.equals("") || json.equals("null")) {
			return null;
		}
		return JsonUtils.gson.fromJson(json, typeToken.getType());
	}

	public static String toJson(Object src) {
		return JsonUtils.gson.toJson(src);
	}

	public static String toJsonTree(Object src) {
		return JsonUtils.gson.toJsonTree(src).toString();
	}

}
