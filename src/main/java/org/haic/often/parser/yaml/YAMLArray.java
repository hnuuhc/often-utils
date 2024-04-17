package org.haic.often.parser.yaml;

import org.haic.often.exception.YAMLException;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.parser.json.JSONFormat;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class YAMLArray extends ArrayList<Object> {

	public YAMLArray() {super();}

	public YAMLArray(Collection<?> c) {super(c);}

	public YAMLArray(@NotNull String s) {
		this(new ParserStringBuilder(s), 0);
	}

	public YAMLArray(@NotNull ParserStringBuilder body, int depth) {
		for (var thisdepth = Yaml.indentation(body); body.isNoOutBounds(); body.offset(1), thisdepth = Yaml.indentation(body)) {
			if (thisdepth < depth) return;
			if (thisdepth > depth) throw new YAMLException("缩进错误");
			body.offset(thisdepth);
			if (body.charAt() != '-') throw new YAMLException("数组必须以'-'符号开头");

			body.offset(1);

			if (body.charAt() == ' ') {
				var value = new StringBuilder();
				for (var c = body.offset(1).charAt(); c != '\n'; c = body.offset(1).charAt()) {
					if (c == '#') {
						body.site(body.indexOf("\n"));
						break;
					}
					value.append(c);
				}

				value.trimToSize();
				if (value.isEmpty()) {
					var nextdepth = Yaml.indentation(body.offset(1));
					this.add(body.charAt(body.site() + nextdepth) == '-' ? new YAMLArray(body, nextdepth) : new YAMLObject(body, nextdepth));
				} else this.add(Yaml.deserialization(value.toString()));
			} else if (body.charAt() == '\n') {
				var nextdepth = Yaml.indentation(body.offset(1));
				this.add(body.charAt(body.site() + nextdepth) == '-' ? new YAMLArray(body, nextdepth) : new YAMLObject(body, nextdepth));
			} else {
				throw new YAMLException("符号'-'后面必须跟随空格");
			}
		}
	}

	/**
	 * 解析并获取YAML数组
	 *
	 * @param list 数组
	 * @return YAML数组
	 */
	public static YAMLArray parseArray(@NotNull Collection<?> list) {
		return new YAMLArray().fluentAddAll(list);
	}

	/**
	 * 使用规则对YAML进行快捷解析,查询规则键{@link YAMLPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	public Object select(@NotNull String cssQuery) {
		return new YAMLPath(this).select(cssQuery);
	}

	/**
	 * 使用规则对Y进行快捷解析,查询规则键{@link YAMLPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @param type     指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, TypeReference<T> type) {
		return new YAMLPath(this).select(cssQuery, type);
	}

	/**
	 * 使用规则对YAML进行快捷解析,查询规则键{@link YAMLPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @param clazz    指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, Class<T> clazz) {
		return new YAMLPath(this).select(cssQuery, clazz);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public Object get(int i) {
		return JSONFormat.format(super.get(i));
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param i      要返回的元素的索引
	 * @param mapper 函数式接口,用于指定转换类型
	 * @param <T>    返回泛型
	 * @return 值
	 */
	public <T> T get(int i, @NotNull Function<Object, ? extends T> mapper) {
		return mapper.apply(this.get(i));
	}

	/**
	 * 获取名称对应键的值
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
	 * 获取对应索引的值
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
	 * 获取名称对应键的数组
	 *
	 * @param i         要返回的元素的索引
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 数组
	 */
	public <T> List<T> getList(int i, @NotNull Class<T> itemClass) {
		return TypeUtil.convertList(this.get(i), itemClass);
	}

	/**
	 * @param i          要返回的元素的索引
	 * @param keyClass   指定键类型
	 * @param valueClass 指定值类型
	 * @param <K>        键泛型
	 * @param <V>        值泛型
	 * @return Map对象
	 */
	public <K, V> Map<K, V> getMap(int i, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		return TypeUtil.convertMap(this.get(i), keyClass, valueClass);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public String getString(int i) {
		return this.get(i, String.class);
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
	 * 获取名称对应键的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public YAMLObject getYAMLObject(int i) {
		return this.get(i, YAMLObject.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	public YAMLArray getYAMLArray(int i) {
		return this.get(i, YAMLArray.class);
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
	 * @param obj 待添加的元素
	 * @return 自身
	 */
	public YAMLArray fluentAdd(Object obj) {
		super.add(obj);
		return this;
	}

	/**
	 * 添加数组所有元素并返回自身
	 *
	 * @param c 数组
	 * @return 自身
	 */
	public YAMLArray fluentAddAll(Collection<?> c) {
		super.addAll(c);
		return this;
	}

	/**
	 * 按照所给比较函数获取排序后的数组
	 *
	 * @param comparator 一个比较函数，它对某些对象集合施加总排序
	 * @return 排序后的数组
	 */
	public YAMLArray sorted(Comparator<Object> comparator) {
		return YAMLArray.parseArray(this.stream().sorted(comparator).toList());
	}

	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * 以指定的深度格式化当前标签
	 *
	 * @param depth 深度
	 * @return 格式化的标签
	 */
	@NotNull
	public String toString(int depth) {
		return this.stream().map(token -> "  ".repeat(depth) + "-" + YAMLFormat.toOutFormat(token, depth)).collect(Collectors.joining("\n"));
	}
}
