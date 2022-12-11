package org.haic.often.util;

import org.haic.often.exception.JSONException;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * 泛类型工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/9 22:41
 */
public class TypeUtil {

	/**
	 * 类型转换
	 *
	 * @param obj       Object对象
	 * @param itemClass 转换类型
	 * @param <T>       返回类型
	 * @return 转换后的类型对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(@NotNull Object obj, Class<T> itemClass) {
		if (obj.getClass() == itemClass) {
			return (T) obj;
		}
		if (itemClass == JSONObject.class) {
			return (T) (obj instanceof Map ? JSONObject.parseObject((Map<String, Object>) obj) : JSONObject.parseObject(String.valueOf(obj)));
		} else if (itemClass == JSONArray.class) {
			return (T) (obj instanceof List ? JSONArray.parseArray((List<Object>) obj) : JSONArray.parseArray(String.valueOf(obj)));
		} else {
			Constructor<?> type = null;
			for (var con : itemClass.getConstructors()) {
				Class<?>[] parameterTypes = con.getParameterTypes();
				if (parameterTypes.length == 1 && parameterTypes[0].getName().equals("java.lang.String")) {
					type = con;
				}
			}
			if (type == null) {
				throw new JSONException("不支持的类型转换");
			}
			try {
				return (T) type.newInstance(obj);
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new JSONException("转换类型不匹配");
			}
		}
	}

}
