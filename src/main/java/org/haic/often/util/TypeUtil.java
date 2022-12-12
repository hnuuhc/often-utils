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
			Object[] objs = obj instanceof Collection ? ((Collection<?>) obj).toArray() : (Object[]) obj;
			Constructor<?> type = TypeUtil.getArrayConstructor(itemClass, "java.lang.String");
			if (type == null) throw new TypeException("不支持的转换类型");
			Object[] arrays = (Object[]) Array.newInstance(itemClass.getComponentType(), objs.length);
			try {
				for (int i = 0; i < objs.length; i++) arrays[i] = type.newInstance(String.valueOf(objs[i]));
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new TypeException("转换类型不匹配");
			}
			return (T) arrays;
		}
		if (itemClass == JSONObject.class) {
			return (T) (obj instanceof Map ? JSONObject.parseObject((Map<?, ?>) obj) : JSONObject.parseObject(String.valueOf(obj)));
		} else if (itemClass == JSONArray.class) {
			return (T) (obj instanceof Collection ? JSONArray.parseArray((Collection<?>) obj) : JSONArray.parseArray(String.valueOf(obj)));
		} else if (itemClass.isPrimitive()) {
			return (T) convert(obj, TypeUtil.getBasicPackType(itemClass));
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
	 * @param obj       数组
	 * @param itemClass 转换类型
	 * @param <T>       返回类型
	 * @return 转换后的类型对象
	 */
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> ArrayList<T> convertList(@NotNull Object obj, Class<T> itemClass) {
		ArrayList<T> list = new ArrayList<>();
		Object[] objs = obj instanceof Collection ? ((Collection<?>) obj).toArray() : (Object[]) obj;
		if (itemClass.isArray()) {
			Constructor<?> type = TypeUtil.getArrayConstructor(itemClass, "java.lang.String");
			if (type == null) throw new TypeException("不支持的转换类型");
			Object[] arrays = (Object[]) Array.newInstance(itemClass.getComponentType(), objs.length);
			for (var o : objs) {
				Object[] os = o instanceof Collection ? ((Collection<?>) o).toArray() : (Object[]) o;
				try {
					for (int i = 0; i < os.length; i++) arrays[i] = type.newInstance(String.valueOf(os[i]));
				} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
					throw new TypeException("转换类型不匹配");
				}
				list.add((T) arrays);
			}
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
			Constructor<?> type = getConstructor(itemClass, "java.lang.String");
			if (type == null) throw new TypeException("不支持的转换类型");
			try {
				for (var o : objs) list.add((T) type.newInstance(String.valueOf(o)));
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new TypeException("转换类型不匹配");
			}
		}
		return list;
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
	 * 获取指定数组参数类型的Constructor类型
	 *
	 * @param itemClass 类型
	 * @param item      数组参数类型
	 * @return Constructor类型
	 */
	@Contract(pure = true)
	public static Constructor<?> getArrayConstructor(@NotNull Class<?> itemClass, @NotNull String item) {
		Class<?> itemClazz = itemClass.getComponentType();
		if (itemClazz.isPrimitive()) throw new TypeException("不支持基本数组类型");
		return TypeUtil.getConstructor(itemClazz, item);
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
			if (parameterTypes.length == 1 && parameterTypes[0].getName().equals(item)) type = con;
		}
		return type;
	}

}
