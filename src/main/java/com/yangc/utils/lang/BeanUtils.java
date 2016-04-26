package com.yangc.utils.lang;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class BeanUtils {

	/**
	 * @功能: map转成object
	 * @作者: yangc
	 * @创建日期: 2016年4月26日 下午12:26:07
	 * @param map
	 * @param clazz
	 * @return
	 */
	public static <T> T map2Object(Map<String, Object> map, Class<T> clazz) {
		if (map != null) {
			try {
				T obj = clazz.newInstance();
				BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
				PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
				for (PropertyDescriptor property : propertyDescriptors) {
					Method setter = property.getWriteMethod();
					if (setter != null) {
						setter.invoke(obj, map.get(property.getName()));
					}
				}
				return obj;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * @功能: object转成map
	 * @作者: yangc
	 * @创建日期: 2016年4月26日 下午12:26:30
	 * @param obj
	 * @return
	 */
	public static Map<String, Object> object2Map(Object obj) {
		if (obj != null) {
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
				PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
				for (PropertyDescriptor property : propertyDescriptors) {
					String key = property.getName();
					if (!key.equalsIgnoreCase("class")) {
						Method getter = property.getReadMethod();
						Object value = getter == null ? null : getter.invoke(obj);
						map.put(key, value);
					}
				}
				return map;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
