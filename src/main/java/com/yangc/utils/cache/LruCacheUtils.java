package com.yangc.utils.cache;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Cache保存一个强引用来限制内容数量, 每当Item被访问的时候, 此Item就会移动到队列的头部. 当cache已满的时候加入新的item时, 在队列尾部的item会被回收
 * <p>
 * 如果cache的某个值需要明确释放, 重写{@link #entryRemoved}
 * <p>
 * 如果key相对应的item丢失, 重写{@link #create}. 这简化了调用代码, 即使丢失了也总会返回
 * <p>
 * 默认cache大小是测量的item的数量, 重写{@link #sizeOf}计算不同item的 大小
 * 
 * <pre>
 * {@code
 *   int cacheSize = 4 * 1024 * 1024; // 4MB
 *   LruCache<String, Bitmap> bitmapCache = new LruCache<String, Bitmap>(cacheSize) {
 *       protected int sizeOf(String key, Bitmap value) {
 *           return value.getByteCount();
 *       }
 *   }}
 * </pre>
 * 
 * <p>
 * This class is thread-safe. Perform multiple cache operations atomically by synchronizing on the cache:
 * 
 * <pre>
 * {@code
 *   synchronized (cache) {
 *   	if (cache.get(key) == null) {
 *      	cache.put(key, value);
 *     	}
 *   }}
 * </pre>
 * 
 * <p>
 * 不允许key或者value为null, 当{@link #get}, {@link #put}, {@link #remove}返回值为null时, key相应的项不在cache中
 */
public class LruCacheUtils<K, V> {

	private final LinkedHashMap<K, V> map;

	private int size; // 已经存储的大小
	private final int maxSize; // 规定的最大存储空间

	private int putCount; // put的次数
	private int createCount; // create的次数
	private int evictionCount; // 回收的次数
	private int hitCount; // 命中的次数
	private int missCount; // 丢失的次数

	/**
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:29:01
	 * @param maxSize 缓存的最大容量
	 */
	public LruCacheUtils(int maxSize) {
		if (maxSize <= 0) {
			throw new IllegalArgumentException("maxSize <= 0");
		}
		this.maxSize = maxSize;
		this.map = new LinkedHashMap<K, V>(0, 0.75f, true) {
			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<K, V> eldest) {
				if (this.size() > LruCacheUtils.this.maxSize) {
					LruCacheUtils.this.size -= safeSizeOf(eldest.getKey(), eldest.getValue());
					return true;
				}
				return false;
			}
		};
	}

	/**
	 * @功能: 通过key返回相应的item, 或者创建返回相应的item, 相应的item会移动到队列的头部, 如果item的value没有被cache或者不能被创建, 则返回null
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:29:01
	 * @param key
	 * @return
	 */
	public final V get(K key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

		V mapValue;
		synchronized (this) {
			mapValue = map.get(key);
			if (mapValue != null) {
				hitCount++; // 命中
				return mapValue;
			}
			missCount++; // 丢失
		}

		// 如果不存在则试图创建一个item
		V createdValue = create(key);
		if (createdValue == null) {
			return null;
		}

		synchronized (this) {
			createCount++; // 创建++
			mapValue = map.put(key, createdValue);

			if (mapValue != null) {
				// 如果前面存在oldValue, 那么撤销put()
				map.put(key, mapValue);
			} else {
				size += safeSizeOf(key, createdValue);
			}
		}

		if (mapValue != null) {
			entryRemoved(false, key, createdValue, mapValue);
			return mapValue;
		} else {
			return createdValue;
		}
	}

	/**
	 * @功能: 装载缓存, 并且移到队列的头部
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:30:14
	 * @param key
	 * @param value
	 * @return 当前key对应的上一个值
	 */
	public final V put(K key, V value) {
		if (key == null || value == null) {
			throw new NullPointerException("key == null || value == null");
		}

		V previous;
		synchronized (this) {
			putCount++;
			size += safeSizeOf(key, value);
			previous = map.put(key, value);
			if (previous != null) {
				// 返回的先前的value值
				size -= safeSizeOf(key, previous);
			}
		}

		if (previous != null) {
			entryRemoved(false, key, previous, value);
		}
		return previous;
	}

	/**
	 * @功能: 删除key相应的cache项, 返回相应的value
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:49:37
	 * @param key
	 * @return
	 */
	public final V remove(K key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

		V previous;
		synchronized (this) {
			previous = map.remove(key);
			if (previous != null) {
				size -= safeSizeOf(key, previous);
			}
		}

		if (previous != null) {
			entryRemoved(false, key, previous, null);
		}
		return previous;
	}

	/**
	 * @功能: 当item被回收或者删掉时调用
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:52:21
	 * @param evicted true: 为释放空间被删除, false: put或remove导致
	 * @param key
	 * @param oldValue
	 * @param newValue
	 */
	protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
	}

	/**
	 * @功能: 当某Item丢失时会调用到, 返回计算的相应的value或者null
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:54:52
	 * @param key
	 * @return
	 */
	protected V create(K key) {
		return null;
	}

	private int safeSizeOf(K key, V value) {
		int result = sizeOf(key, value);
		if (result < 0) {
			throw new IllegalStateException("Negative size: " + key + "=" + value);
		}
		return result;
	}

	/**
	 * @功能: 返回用户定义的item的大小, 默认返回1代表item的数量, 最大size就是最大item值
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:56:35
	 * @param key
	 * @param value
	 * @return
	 */
	protected int sizeOf(K key, V value) {
		return 1;
	}

	/**
	 * @功能: 清空cache
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:56:57
	 */
	public synchronized final void evictAll() {
		evictionCount += size;
		size = 0;
		map.clear();
	}

	/**
	 * @功能: 当前缓存长度
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:57:25
	 * @return
	 */
	public synchronized final int size() {
		return size;
	}

	/**
	 * @功能: 缓存最大容量
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:57:55
	 * @return
	 */
	public synchronized final int maxSize() {
		return maxSize;
	}

	/**
	 * @功能: 命中的次数
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:58:15
	 * @return
	 */
	public synchronized final int hitCount() {
		return hitCount;
	}

	/**
	 * @功能: 丢失的次数
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:59:03
	 * @return
	 */
	public synchronized final int missCount() {
		return missCount;
	}

	/**
	 * @功能: create的次数
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午5:59:27
	 * @return
	 */
	public synchronized final int createCount() {
		return createCount;
	}

	/**
	 * @功能: put的次数
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午6:02:25
	 * @return
	 */
	public synchronized final int putCount() {
		return putCount;
	}

	/**
	 * @功能: 回收的次数
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午6:02:50
	 * @return
	 */
	public synchronized final int evictionCount() {
		return evictionCount;
	}

	/**
	 * @功能: 返回当前cache的副本, 从最近最少访问到最多访问
	 * @作者: yangc
	 * @创建日期: 2014年6月6日 下午6:03:07
	 * @return
	 */
	public synchronized final Map<K, V> snapshot() {
		return new LinkedHashMap<K, V>(map);
	}

	@Override
	public synchronized final String toString() {
		int accesses = hitCount + missCount;
		int hitPercent = accesses != 0 ? (100 * hitCount / accesses) : 0;
		return String.format("LruCache[maxSize=%d,hits=%d,misses=%d,hitRate=%d%%]", maxSize, hitCount, missCount, hitPercent);
	}

}
