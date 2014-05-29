package com.yangc.utils.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class EhCacheUtils {

	private static String FILE_PATH = "/ehcache.xml";

	private static CacheManager cacheManager;

	private static EhCacheUtils ehCacheUtils;

	private EhCacheUtils() {
	}

	public synchronized static EhCacheUtils getInstance(String filePath) {
		if (ehCacheUtils == null) {
			if (filePath != null && !filePath.equals("")) {
				FILE_PATH = filePath;
			}
			cacheManager = new CacheManager(EhCacheUtils.class.getResourceAsStream(FILE_PATH));
			ehCacheUtils = new EhCacheUtils();
		}
		return ehCacheUtils;
	}

	public boolean add(String cacheName, Object key, Object value) {
		try {
			Cache cache = cacheManager.getCache(cacheName);
			cache.put(new Element(key, value));
			return true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (CacheException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean replace(String cacheName, Object key, Object value) {
		try {
			Cache cache = cacheManager.getCache(cacheName);
			cache.replace(new Element(key, value));
			return true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		return false;
	}

	public <T> T get(String cacheName, Object key, Class<T> clazz) {
		try {
			Cache cache = cacheManager.getCache(cacheName);
			Element element = cache.get(key);
			if (element != null) {
				return clazz.cast(element.getObjectValue());
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (CacheException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean delete(String cacheName, Object key) {
		try {
			Cache cache = cacheManager.getCache(cacheName);
			return cache.remove(key);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean flushAll(String cacheName) {
		try {
			Cache cache = cacheManager.getCache(cacheName);
			cache.removeAll();
			return true;
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} catch (CacheException e) {
			e.printStackTrace();
		}
		return false;
	}

}
