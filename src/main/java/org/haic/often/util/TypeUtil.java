package org.haic.often.util;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.TypeException;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
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
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, @NotNull Class<T> itemClass) {
		if (obj == null) return null;
		if (obj.getClass() == itemClass) return (T) obj;
		if (itemClass.isArray()) {
			Constructor<?> type = TypeUtil.getConstructor(itemClass.getComponentType(), "java.lang.String");
			if (type == null) throw new TypeException("不支持的转换类型");
			Object[] objs = obj instanceof Collection ? ((Collection<?>) obj).toArray() : (Object[]) obj;
			Class<?> clazz = itemClass.getComponentType();
			Object array = Array.newInstance(clazz, objs.length);
			Object[] arrays = (Object[]) array;
			try {
				for (int i = 0; i < objs.length; i++) {
					arrays[i] = type.newInstance(String.valueOf(objs[i]));
				}
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new TypeException("转换类型不匹配");
			}
			return (T) arrays;
		}
		if (itemClass == JSONObject.class) {
			return (T) (obj instanceof Map ? JSONObject.parseObject((Map<?, ?>) obj) : JSONObject.parseObject(String.valueOf(obj)));
		} else if (itemClass == JSONArray.class) {
			return (T) (obj instanceof Collection ? JSONArray.parseArray((Collection<?>) obj) : JSONArray.parseArray(String.valueOf(obj)));
		} else {
			Constructor<?> type = TypeUtil.getConstructor(itemClass, "java.lang.String");
			if (type == null) throw new TypeException("不支持的转换类型");
			try {
				return (T) type.newInstance(String.valueOf(obj));
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new TypeException("转换类型不匹配");
			}
		}
	}

	/**
	 * 类型转换
	 *
	 * @param objs      数组
	 * @param itemClass 转换类型
	 * @param <T>       返回类型
	 * @return 转换后的类型对象
	 */
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> convert(@NotNull Collection<Object> objs, Class<T> itemClass) {
		ArrayList<T> list = new ArrayList<>();
		if (itemClass == JSONObject.class) {
			for (var obj : objs) list.add((T) (obj instanceof JSONObject ? obj : obj instanceof Map ? JSONObject.parseObject((Map<?, ?>) obj) : JSONObject.parseObject(String.valueOf(obj))));
		} else if (itemClass == JSONArray.class) {
			for (var obj : objs)
				list.add((T) (obj instanceof JSONArray ? obj : obj instanceof Collection ? JSONArray.parseArray((Collection<?>) obj) : JSONObject.parseObject(String.valueOf(obj))));
		} else {
			Constructor<?> type = getConstructor(itemClass, "java.lang.String");
			if (type == null) throw new TypeException("不支持的转换类型");
			try {
				for (var obj : objs) list.add((T) type.newInstance(String.valueOf(obj)));
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new TypeException("转换类型不匹配");
			}
		}
		return list;
	}

	/**
	 * 获取指定参数类型的Constructor类型
	 *
	 * @param itemClass 类型
	 * @param item      参数类型
	 * @return Constructor类型
	 */
	@Contract(pure = true)
	public static Constructor<?> getConstructor(@NotNull Class<?> itemClass, @NotNull String item) {
		Constructor<?> type = null;
		for (var con : itemClass.getConstructors()) {
			Class<?>[] parameterTypes = con.getParameterTypes();
			if (parameterTypes.length == 1 && parameterTypes[0].getName().equals(item)) {
				type = con;
			}
		}
		return type;
	}

}
