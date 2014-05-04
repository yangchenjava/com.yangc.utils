package com.yangc.utils.json;

import com.google.gson.Gson;

public class JsonUtils {

	private static Gson gson;

	private JsonUtils() {
	}

	private synchronized static Gson getInstance() {
		if (gson == null) {
			gson = new Gson();
		}
		return gson;
	}

	public static <T> T fromJson(String json, Class<T> clazz) {
		if (json == null || json.equals("") || json.equals("null")) {
			return null;
		}
		return JsonUtils.getInstance().fromJson(json, clazz);
	}

	public static String toJson(Object src) {
		return JsonUtils.getInstance().toJson(src);
	}

	public static String toJsonTree(Object src) {
		return JsonUtils.getInstance().toJsonTree(src).toString();
	}

}
