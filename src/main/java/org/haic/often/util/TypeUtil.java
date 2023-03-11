package org.haic.often.util;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.TypeException;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.parser.xml.Document;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Function;

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
	 * @param obj  待转换的参数
	 * @param type 参考类型
	 * @param <T>  返回类型
	 * @return 转换后的数据
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, TypeReference<T> type) {
		var rawType = type.getRawType();
		var arguments = type.getArguments();
		Function<Class<?>, Constructor<?>> convert = convertType -> {
			for (var constructor : convertType.getConstructors()) {
				if (constructor.getParameterCount() == arguments.length) {
					var bool = true;
					var parameterTypes = constructor.getParameterTypes();
					for (int i = 0; i < arguments.length; i++) {
						if ((parameterTypes[i].isPrimitive() ? TypeUtil.getBasicPackType(parameterTypes[i]) : parameterTypes[i]) != arguments[i].getClass()) {
							bool = false;
							break;
						}
					}
					if (bool) return constructor;
				}
			}
			throw new TypeException("无匹配参数的对应对象");
		};
		var actualTypeArguments = type.getActualTypeArguments();
		if (Map.class.isAssignableFrom(rawType)) {
			if (rawType.isInterface()) rawType = HashMap.class;
			return (T) TypeUtil.convertMap(obj, TypeUtil.getRawType(actualTypeArguments[0]), TypeUtil.getRawType(actualTypeArguments[1]), convert.apply(rawType), arguments);
		} else if (Collection.class.isAssignableFrom(rawType)) {
			if (rawType.isInterface()) rawType = ArrayList.class;
			return (T) TypeUtil.convertList(obj, TypeUtil.getRawType(actualTypeArguments[0]), convert.apply(rawType), arguments);
		} else if (!type.isActualType()) {
			return (T) TypeUtil.convert(obj, rawType);
		} else {
			throw new TypeException("不支持的转换类型");
		}
	}

	/**
	 * 类型转换
	 *
	 * @param obj  待转换的参数
	 * @param type 转换类型
	 * @param <T>  返回类型
	 * @return 转换后的数据
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, @NotNull Type type) {return convert(obj, (Class<T>) type);}

	/**
	 * 类型转换
	 *
	 * @param obj       Object对象
	 * @param itemClass 转换类型
	 * @param <T>       返回类型
	 * @return 转换后的数据
	 */
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, @NotNull Class<T> itemClass) {
		if (obj == null) return null;
		if (itemClass.isInstance(obj)) return (T) obj;
		if (itemClass.isArray()) {
			var clazz = itemClass.getComponentType();
			var objs = obj instanceof Collection<?> c ? c.toArray() : (Object[]) obj;
			if (clazz.isPrimitive() && !clazz.isArray()) { // 基本类型转换
				if (clazz == int.class) {
					var arrays = new int[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = Integer.parseInt(String.valueOf(objs[i]));
					return (T) arrays;
				}
				if (clazz == long.class) {
					var arrays = new long[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = Long.parseLong(String.valueOf(objs[i]));
					return (T) arrays;
				}
				if (clazz == double.class) {
					var arrays = new double[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = Double.parseDouble(String.valueOf(objs[i]));
					return (T) arrays;
				}
				if (clazz == float.class) {
					var arrays = new float[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = Float.parseFloat(String.valueOf(objs[i]));
					return (T) arrays;
				}
				if (clazz == short.class) {
					var arrays = new short[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = Short.parseShort(String.valueOf(objs[i]));
					return (T) arrays;
				}
				if (clazz == byte.class) {
					var arrays = new byte[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = Byte.parseByte(String.valueOf(objs[i]));
					return (T) arrays;
				}
				if (clazz == boolean.class) {
					var arrays = new boolean[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = Boolean.parseBoolean(String.valueOf(objs[i]));
					return (T) arrays;
				}
				if (itemClass == char.class) {
					var arrays = new char[objs.length];
					for (int i = 0; i < objs.length; i++) arrays[i] = (char) objs[i];
					return (T) arrays;
				}
			}
			var arrays = (Object[]) Array.newInstance(clazz, objs.length);
			for (int i = 0; i < objs.length; i++) arrays[i] = convert(objs[i], clazz);
			return (T) arrays;
		}
		if (itemClass == JSONObject.class) return (T) (obj instanceof Map<?, ?> map ? JSONObject.parseObject(map) : JSONObject.parseObject(String.valueOf(obj)));
		if (itemClass == JSONArray.class) {
			return (T) (obj instanceof Collection<?> c ? JSONArray.parseArray(c) : obj instanceof Object[] os ? JSONArray.parseArray(Arrays.asList(os)) : JSONArray.parseArray(String.valueOf(obj)));
		}
		if (itemClass == Document.class) return (T) Document.parse(String.valueOf(obj));
		if (itemClass.isPrimitive()) return convertBasicType(obj, itemClass);
		// 上方为处理特殊类型,下方对单对象类型转换
		var thisClassName = obj.getClass().getName();
		var type = TypeUtil.getConstructor(itemClass, thisClassName);
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

	/**
	 * 转换为基本类型
	 *
	 * @param obj       待转换的参数
	 * @param itemClass 转换参数类型
	 * @param <T>       返回类型
	 * @return 转换后的参数
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertBasicType(@NotNull Object obj, @NotNull Class<T> itemClass) {
		if (itemClass == int.class) return (T) ((Object) Integer.parseInt(String.valueOf(obj)));
		if (itemClass == long.class) return (T) ((Object) Long.parseLong(String.valueOf(obj)));
		if (itemClass == double.class) return (T) ((Object) Double.parseDouble(String.valueOf(obj)));
		if (itemClass == float.class) return (T) ((Object) Float.parseFloat(String.valueOf(obj)));
		if (itemClass == short.class) return (T) ((Object) Short.parseShort(String.valueOf(obj)));
		if (itemClass == byte.class) return (T) ((Object) Byte.parseByte(String.valueOf(obj)));
		if (itemClass == boolean.class) return (T) ((Object) Boolean.parseBoolean(String.valueOf(obj)));
		if (itemClass == char.class) return (T) ((Object) (char) obj);
		throw new TypeException("未知的基本类型");
	}

	/**
	 * 类型转换
	 *
	 * @param obj  数组
	 * @param type 转换参数类型
	 * @param <T>  返回类型
	 * @return 转换后的类型
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
	 * @param itemClass 转换参数类型
	 * @param <T>       返回参数类型
	 * @return 转换后的类型
	 */
	@Contract(pure = true)
	public static <T> List<T> convertList(@NotNull Object obj, @NotNull Class<T> itemClass) {
		return convertList(obj, new ArrayList<>(), itemClass);
	}

	/**
	 * 类型转换
	 *
	 * @param obj         数组
	 * @param itemClass   转换参数类型
	 * @param constructor Constructor对象,用于构建存储类对象
	 * @param arguments   Constructor构建参数
	 * @param <T>         返回参数类型
	 * @return 转换后的类型
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> convertList(@NotNull Object obj, @NotNull Class<T> itemClass, @NotNull Constructor<?> constructor, @NotNull Object... arguments) {
		try {
			return convertList(obj, (List<T>) constructor.newInstance(arguments), itemClass);
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new TypeException(e);
		}
	}

	/**
	 * 类型转换
	 *
	 * @param obj       数组
	 * @param list      数组对象,用于存储转换后数据
	 * @param itemClass 转换参数类型
	 * @param <T>       返回参数类型
	 * @return 转换后的类型
	 */
	public static <T> List<T> convertList(@NotNull Object obj, @NotNull List<T> list, @NotNull Class<T> itemClass) {
		if (obj == null) return null;
		var objs = obj instanceof Collection<?> c ? c.toArray() : obj instanceof Object[] os ? os : JSONArray.parseArray(String.valueOf(obj)).toArray();
		for (Object o : objs) list.add(TypeUtil.convert(o, itemClass));
		return list;
	}

	/**
	 * 类型转换
	 *
	 * @param obj        Map对象
	 * @param keyClass   转换参数类型
	 * @param valueClass 转换参数类型
	 * @param <K>        返回参数类型
	 * @param <V>        返回参数类型
	 * @return 转换后的类型
	 */
	public static <K, V> Map<K, V> convertMap(@NotNull Object obj, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		return convertMap(obj, new HashMap<>(), keyClass, valueClass);
	}

	/**
	 * 类型转换
	 *
	 * @param obj         Map对象
	 * @param keyClass    转换参数类型
	 * @param valueClass  转换参数类型
	 * @param constructor Constructor对象,用于构建存储类对象
	 * @param arguments   Constructor构建参数
	 * @param <K>         返回参数类型
	 * @param <V>         返回参数类型
	 * @return 转换后的类型
	 */
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> convertMap(@NotNull Object obj, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass, @NotNull Constructor<?> constructor, @NotNull Object... arguments) {
		try {
			return convertMap(obj, (Map<K, V>) constructor.newInstance(arguments), keyClass, valueClass);
		} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
			throw new TypeException(e);
		}
	}

	/**
	 * 类型转换
	 *
	 * @param obj        Map对象
	 * @param m          Map对象,用于存储转换后数据
	 * @param keyClass   转换参数类型
	 * @param valueClass 转换参数类型
	 * @param <K>        返回参数类型
	 * @param <V>        返回参数类型
	 * @return 转换后的类型
	 */
	public static <K, V> Map<K, V> convertMap(@NotNull Object obj, @NotNull Map<K, V> m, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		if (obj == null) return null;
		var objs = obj instanceof Map<?, ?> map ? map : JSONObject.parseObject(String.valueOf(obj));
		for (var entry : objs.entrySet()) m.put(TypeUtil.convert(entry.getKey(), keyClass), TypeUtil.convert(entry.getValue(), valueClass));
		return m;
	}

	/**
	 * 获取包装类型对应的基本类型
	 *
	 * @param itemClass 包装类型
	 * @return 对应的基本类型
	 */
	public static Class<?> getPackBasicType(@NotNull Class<?> itemClass) {
		if (itemClass == Integer.class) return int.class;
		if (itemClass == Long.class) return long.class;
		if (itemClass == Double.class) return double.class;
		if (itemClass == Float.class) return float.class;
		if (itemClass == Short.class) return short.class;
		if (itemClass == Byte.class) return byte.class;
		if (itemClass == Boolean.class) return boolean.class;
		if (itemClass == Character.class) return char.class;
		throw new TypeException("未知的基本类型");
	}

	/**
	 * 获取基本类型对应的包装类型
	 *
	 * @param itemClass 基本类型
	 * @return 对应的包装类型
	 */
	@Contract(pure = true)
	public static Class<?> getBasicPackType(@NotNull Class<?> itemClass) {
		if (itemClass == int.class) return Integer.class;
		if (itemClass == long.class) return Long.class;
		if (itemClass == double.class) return Double.class;
		if (itemClass == float.class) return Float.class;
		if (itemClass == short.class) return Short.class;
		if (itemClass == byte.class) return Byte.class;
		if (itemClass == boolean.class) return Boolean.class;
		if (itemClass == char.class) return Character.class;
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
				var parameterTypes = con.getParameterTypes();
				if (parameterTypes.length == 1 && parameterTypes[0].getName().equals(item)) {
					type = con;
					break items;
				}
			}
		}
		return type;
	}

	/**
	 * 返回表示声明此类型的类或接口对象
	 *
	 * @param type 类型对象
	 * @return 类或接口对象
	 */
	public static Class<?> getRawType(Type type) {
		if (type instanceof Class<?> classType) {
			return classType;
		} else if (type instanceof ParameterizedType parameterizedType) {
			return (Class<?>) parameterizedType.getRawType();
		} else if (type instanceof GenericArrayType genericArrayType) {
			return Array.newInstance(getRawType(genericArrayType.getGenericComponentType()), 0).getClass();
		} else if (type instanceof TypeVariable) {
			return Object.class;
		} else if (type instanceof WildcardType wildcardType) {
			return getRawType(wildcardType.getUpperBounds()[0]);
		} else {
			throw new IllegalArgumentException("Expected a Class, ParameterizedType or GenericArrayType, but <" + type + "> is of type " + (type == null ? "null" : type.getClass().getName()));
		}
	}

}
