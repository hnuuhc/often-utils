package org.haic.often.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 表示泛型类型 {@code T}。 Java 还没有提供一种方法来
 * 代表泛型类型，所以这个类也是。 强制客户创建一个
 * 此类的子类，即使在运行时也能检索类型信息。
 * <p>
 * 此语法不能用于创建具有通配符的类型文字
 * 参数，例如 {@code Class<T>} 或 {@code List<?> extends  CharSequence>}。
 * <p>
 * 例如，要为 {@code List<String>} 创建类型文字，您可以
 * 创建一个空的匿名内部类：
 * <pre>
 *     {@code TypeReference<List<String>> typeReference = new TypeReference<List<String>>(){};}
 * </pre>
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/6 17:56
 */
public abstract class TypeReference<T> {

	private final Type type;
	private final Class<? super T> rawType;
	private final Type[] actualTypeArguments;
	private final Object[] arguments;

	@SuppressWarnings("unchecked")
	public TypeReference(Object... arguments) {
		Type superClass = this.getClass().getGenericSuperclass();
		this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
		this.rawType = (Class<? super T>) TypeUtil.getRawType(type);
		this.actualTypeArguments = type instanceof ParameterizedType parameterizedType ? parameterizedType.getActualTypeArguments() : new Type[0];
		this.arguments = arguments;
	}

	public Type getType() {
		return this.type;
	}

	public Class<?> getRawType() {
		return rawType;
	}

	public Type[] getActualTypeArguments() {
		return actualTypeArguments;
	}

	public Object[] getArguments() {
		return arguments;
	}

	/**
	 * 判断是否为参数类
	 *
	 * @return 判断结果
	 */
	public boolean isActualType() {
		return actualTypeArguments.length != 0;
	}

}
