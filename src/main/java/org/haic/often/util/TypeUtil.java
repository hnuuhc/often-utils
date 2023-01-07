package org.haic.often.util;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.TypeException;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;

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

	public static String[][] BasicType = { { "int", "java.lang.Integer" }, //
										   { "double", "java.lang.Double" }, //
										   { "float", "java.lang.Float" }, //
										   { "short", "java.lang.Short" }, //
										   { "byte", "java.lang.Byte" },//
										   { "boolean", "java.lang.Boolean" },//
										   { "char", "java.lang.Character" } };

	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, TypeReference<T> type) {
		var rawType = type.getRawType();
		if (!type.isActualType()) return (T) TypeUtil.convert(obj, rawType);
		Object[] arguments = type.getArguments();
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
		try {
			if (Map.class.isAssignableFrom(rawType)) {
				if (rawType.isInterface()) rawType = Class.forName("java.util.HashMap");
				return (T) TypeUtil.convertMap(obj, TypeUtil.getRawType(actualTypeArguments[0]), TypeUtil.getRawType(actualTypeArguments[1]), convert.apply(rawType), arguments);
			} else if (Collection.class.isAssignableFrom(rawType)) {
				if (rawType.isInterface()) rawType = Class.forName("java.util.ArrayList");
				return (T) TypeUtil.convertList(obj, TypeUtil.getRawType(actualTypeArguments[0]), convert.apply(rawType), arguments);
			} else {
				throw new TypeException("不支持的转换类型");
			}
		} catch (ClassNotFoundException e) {
			throw new TypeException(e);
		}
	}

	/**
	 * 类型转换
	 *
	 * @param obj  Object对象
	 * @param type 转换类型
	 * @param <T>  返回类型
	 * @return 转换后的类型
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
	 * @return 转换后的类型
	 */
	@Contract(pure = true)
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object obj, @NotNull Class<T> itemClass) {
		if (obj == null) return null;
		var objClass = obj.getClass();
		if (objClass == itemClass) return (T) obj;
		if (itemClass.isArray()) {
			var clazz = itemClass.getComponentType();
			if (clazz.isPrimitive()) throw new TypeException("不支持基本数组类型");
			var objs = obj instanceof Collection ? ((Collection<?>) obj).toArray() : (Object[]) obj;
			var arrays = (Object[]) Array.newInstance(clazz, objs.length);
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
			var thisClassName = objClass.getName();
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
	@SuppressWarnings("unchecked")
	public static <T> List<T> convertList(@NotNull Object obj, @NotNull List<T> list, @NotNull Class<T> itemClass) {
		if (obj == null) return null;
		var objs = obj instanceof Collection ? ((Collection<?>) obj).toArray() : (Object[]) obj;
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
			for (var o : objs) list.add((T) convert(o, TypeUtil.getBasicPackType(itemClass)));
		} else {
			Constructor<?> type = getConstructor(itemClass);
			try {
				for (var o : objs) {
					if (o == null) list.add(null);
					else if (o.getClass() == itemClass) list.add((T) o);
					else {
						var thisClassName = o.getClass().getName();
						if (thisClassName.equals("java.lang.String")) {
							if (type == null) throw new TypeException("不支持的转换类型");
							list.add((T) type.newInstance(String.valueOf(o)));
						} else {
							var thisType = TypeUtil.getConstructor(itemClass, thisClassName);
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
		var name = itemClass.getTypeName();
		try {
			for (var strings : BasicType) if (strings[0].equals(name)) return Class.forName(strings[1]);
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
