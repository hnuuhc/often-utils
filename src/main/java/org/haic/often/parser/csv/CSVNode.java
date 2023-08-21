package org.haic.often.parser.csv;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CSV子节点
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/3/17 14:41
 */
public class CSVNode extends ArrayList<String> {

	public CSVNode() {
		super();
	}

	public CSVNode(@NotNull ParserStringBuilder node) {
		while (node.isNoOutBounds()) {
			char c = node.charAt();
			var sb = new StringBuilder();
			if (c == '"') {
				while (node.offset(1).isNoOutBounds()) {
					char n = node.charAt();
					if (n == '"') {
						if (node.offset(1).isNoOutBounds()) {
							char next = node.charAt();
							if (next == '"') {
								sb.append(next);
							} else if (next == ',') {
								break;
							} else if (next == '\r') {
								this.add(sb.toString());
								if (node.charAt(node.pos() + 1) == '\n') node.offset(1);
								return;
							} else if (next == '\n') {
								this.add(sb.toString());
								return;
							} else {
								throw new IllegalStateException("索引 " + node.pos() + " 处期待值不为 '\"' 或 ','");
							}
						} else {
							break;
						}
					} else {
						sb.append(n);
					}
					this.add(sb.toString());
				}
			} else {
				while (node.isNoOutBounds()) {
					char n = node.charAt();
					if (n == '"') {
						if (node.offset(1).isNoOutBounds()) {
							char next = node.charAt();
							if (next == '"') {
								sb.append(next);
							} else {
								throw new IllegalStateException("索引 " + node.pos() + " 处期待值不为 '\"'");
							}
						} else {
							throw new IllegalStateException("索引 " + node.pos() + " 处期待值不为 '\"'");
						}
					} else if (n == ',') {
						break;
					} else if (n == '\r') {
						this.add(sb.toString());
						if (node.charAt(node.pos() + 1) == '\n') node.offset(1);
						return;
					} else if (n == '\n') {
						this.add(sb.toString());
						return;
					} else {
						sb.append(n);
					}
					node.offset(1);
				}
				this.add(sb.toString());
			}
			node.offset(1);
		}
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param i      要返回的元素的索引
	 * @param mapper 函数式接口,用于指定转换类型
	 * @param <T>    返回泛型
	 * @return 值
	 */
	public <T> T get(int i, @NotNull Function<Object, ? extends T> mapper) {
		return mapper.apply(super.get(i));
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param i    要返回的元素的索引
	 * @param type TypeReference接口类型
	 * @param <T>  返回泛型
	 * @return 值
	 */
	public <T> T get(int i, @NotNull TypeReference<T> type) {
		return TypeUtil.convert(this.get(i), type);
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param i         要返回的元素的索引
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 值
	 */
	public <T> T get(int i, @NotNull Class<T> itemClass) {
		return TypeUtil.convert(this.get(i), itemClass);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public boolean getBoolean(int i) {
		return this.get(i, Boolean.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public byte getByte(int i) {
		return this.get(i, Byte.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public short getShort(int i) {
		return this.get(i, Short.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public int getInteger(int i) {
		return this.get(i, Integer.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public long getLong(int i) {
		return this.get(i, Long.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public float getFloat(int i) {
		return this.get(i, Float.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public double getDouble(int i) {
		return this.get(i, Double.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public BigDecimal getBigDecimal(int i) {
		return this.get(i, BigDecimal.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public BigInteger getBigInteger(int i) {
		return this.get(i, BigInteger.class);
	}

	/**
	 * 转换为指定类型数组
	 *
	 * @param itemClass 指定类型
	 * @param <T>       数组泛型
	 * @return 指定类型的数组
	 */
	public <T> List<T> toList(@NotNull Class<T> itemClass) {
		return TypeUtil.convertList(this, itemClass);
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param s 待添加的元素
	 * @return 自身
	 */
	public CSVNode fluentAdd(String s) {
		super.add(s);
		return this;
	}

	/**
	 * 添加数组所有元素并返回自身
	 *
	 * @param c 数组
	 * @return 自身
	 */
	public CSVNode fluentAddAll(Collection<String> c) {
		super.addAll(c);
		return this;
	}

	@Override
	public String toString() {
		return this.stream().map(e -> {
			var t = e.replaceAll("\"", "\"\"");
			return e.contains(",") ? '"' + t + '"' : t;
		}).collect(Collectors.joining(","));
	}

}
