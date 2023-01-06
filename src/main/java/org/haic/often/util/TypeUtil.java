package org.haic.often.util;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.TypeException;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 泛类型工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/9 22:41
 */
public class TypeUtil {

	public static String[][] BasicType = { { "int", "java.lang.Integer" }, //
										   { "double", "java.lang.Double" }, //
										   { "float", "java.lang.Float" }, //
										   { "short", "java.lang.Short" }, //
										   { "byte", "java.lang.Byte" },//
										   { "boolean", "java.lang.Boolean" },//
										   { "char", "java.lang.Character" } };

	/**
	 * 类型转换
	 *
	 * @param obj  Object对象
	 * @param type 转换类型
	 * @param <T>  返回类型
	 * @return 转换后的类型对象
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, @NotNull Type type) {
		return convert(obj, (Class<T>) type);
	}

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
		Class<?> objClass = obj.getClass();
		if (objClass == itemClass) return (T) obj;
		if (itemClass.isArray()) {
			Class<?> clazz = itemClass.getComponentType();
			if (clazz.isPrimitive()) throw new TypeException("不支持基本数组类型");
			Object[] objs = obj instanceof Collection ? ((Collection<?>) obj).toArray() : (Object[]) obj;
			Object[] arrays = (Object[]) Array.newInstance(clazz, objs.length);
			for (int i = 0; i < objs.length; i++) arrays[i] = convert(objs[i], clazz);
			return (T) arrays;
		}
		if (itemClass == JSONObject.class) {
			return (T) (obj instanceof Map ? JSONObject.parseObject((Map<?, ?>) obj) : JSONObject.parseObject(String.valueOf(obj)));
		} else if (itemClass == JSONArray.class) {
			return (T) (obj instanceof Collection ? JSONArray.parseArray((Collection<?>) obj) : JSONArray.parseArray(String.valueOf(obj)));
		} else if (itemClass.isPrimitive()) {
			return (T) convert(obj, TypeUtil.getBasicPackType(itemClass));
		} else {
			String thisClassName = objClass.getName();
			Constructor<?> type = TypeUtil.getConstructor(itemClass, thisClassName);
			try {
				if (type == null) {
					if (thisClassName.equals("java.lang.String")) throw new TypeException("不支持的转换类型");
					type = TypeUtil.getConstructor(itemClass);
					if (type == null) throw new TypeException("不支持的转换类型");
					return (T) type.newInstance(String.valueOf(obj));
				} else {
					return (T) type.newInstance(obj);
				}
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new TypeException("转换类型不匹配");
			}
		}
	}

	/**
	 * 类型转换
	 *
	 * @param obj  数组
	 * @param type 转换类型
	 * @param <T>  返回类型
	 * @return 转换后的类型对象
	 */
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> List<T> convertList(@NotNull Object obj, @NotNull Type type) {
		return convertList(obj, (Class<T>) type);
	}

	/**
	 * 类型转换
	 *
	 * @param obj       数组
	 * @param itemClass 转换类型
	 * @param <T>       返回类型
	 * @return 转换后的类型对象
	 */
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> List<T> convertList(@NotNull Object obj, @NotNull Class<T> itemClass) {
		if (obj == null) return null;
		List<T> list = new ArrayList<>();
		Object[] objs = obj instanceof Collection ? ((Collection<?>) obj).toArray() : (Object[]) obj;
		if (itemClass.isArray()) {
			if (itemClass.isPrimitive()) throw new TypeException("不支持基本数组类型");
			for (Object o : objs) list.add(TypeUtil.convert(o, itemClass));
			return list;
		}
		if (itemClass == JSONObject.class) {
			for (var o : objs) list.add((T) (o instanceof JSONObject ? o : o instanceof Map ? JSONObject.parseObject((Map<?, ?>) o) : JSONObject.parseObject(String.valueOf(o))));
		} else if (itemClass == JSONArray.class) {
			for (var o : objs) list.add((T) (o instanceof JSONArray ? o : o instanceof Collection ? JSONArray.parseArray((Collection<?>) o) : JSONObject.parseObject(String.valueOf(o))));
		} else if (itemClass.isPrimitive()) {
			Class<?> clazz = TypeUtil.getBasicPackType(itemClass);
			for (var o : objs) list.add((T) convert(o, clazz));
		} else {
			Constructor<?> type = getConstructor(itemClass);
			try {
				for (var o : objs) {
					if (o == null) list.add(null);
					else {
						String thisClassName = o.getClass().getName();
						if (thisClassName.equals("java.lang.String")) {
							if (type == null) throw new TypeException("不支持的转换类型");
							list.add((T) type.newInstance(String.valueOf(o)));
						} else {
							Constructor<?> thisType = TypeUtil.getConstructor(itemClass, thisClassName);
							if (thisType == null) {
								if (type == null) throw new TypeException("不支持的转换类型");
								list.add((T) type.newInstance(String.valueOf(o)));
							} else {
								list.add((T) thisType.newInstance(o));
							}
						}
					}
				}
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new TypeException("转换类型不匹配");
			}
		}
		return list;
	}

	public static <K, V> Map<K, V> convertMap(@NotNull Object obj, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		if (obj == null) return null;
		Map<K, V> m = new HashMap<>();
		if (obj instanceof Map<?, ?> map) {
			for (var entry : map.entrySet()) m.put(TypeUtil.convert(entry.getKey(), keyClass), TypeUtil.convert(entry.getValue(), valueClass));
		} else {
			throw new TypeException("不支持的转换类型");
		}
		return m;
	}

	/**
	 * 获取基本类型对应的包装类型
	 *
	 * @param itemClass 基本类型
	 * @return 对应的包装类型
	 */
	@Contract(pure = true)
	public static Class<?> getBasicPackType(@NotNull Class<?> itemClass) {
		String name = itemClass.getTypeName();
		try {
			for (String[] strings : BasicType) if (strings[0].equals(name)) return Class.forName(strings[1]);
		} catch (ClassNotFoundException e) {
			throw new TypeException(e);
		}
		throw new TypeException("未知的基本类型");
	}

	/**
	 * 获取指定参数类型的Constructor类型
	 *
	 * @param itemClass 类型
	 * @return Constructor类型
	 */
	@Contract(pure = true)
	public static Constructor<?> getConstructor(@NotNull Class<?> itemClass) {
		return getConstructor(itemClass, "java.lang.String");
	}

	/**
	 * 获取指定参数类型的Constructor类型
	 *
	 * @param itemClass 类型
	 * @param items     参数类型
	 * @return Constructor类型
	 */
	@Contract(pure = true)
	public static Constructor<?> getConstructor(@NotNull Class<?> itemClass, @NotNull String... items) {
		Constructor<?> type = null;
		items:
		for (var item : items) {
			for (var con : itemClass.getConstructors()) {
				Class<?>[] parameterTypes = con.getParameterTypes();
				if (parameterTypes.length == 1 && parameterTypes[0].getName().equals(item)) {
					type = con;
					break items;
				}
			}
		}
		return type;
	}

}
