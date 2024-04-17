package org.haic.often.parser.yaml;

import org.haic.often.exception.YAMLException;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.util.StringUtil;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class YAMLObject extends LinkedHashMap<String, Object> {

	public YAMLObject() {
		super();
	}

	public YAMLObject(@NotNull String s) {
		this(new ParserStringBuilder(s), 0);
	}

	public YAMLObject(Map<? extends String, ?> m) {
		super(m);
	}

	/**
	 * 这是解析用构建,切勿使用
	 *
	 * @param body 字符串
	 */
	public YAMLObject(@NotNull ParserStringBuilder body, int depth) {
		for (var thisdepth = Yaml.indentation(body); body.isNoOutBounds(); body.offset(1), thisdepth = Yaml.indentation(body)) {
			if (thisdepth < depth) return;
			if (thisdepth > depth) throw new YAMLException("缩进错误");
			if (body.charAt() == '-') throw new YAMLException("键名禁止'-'符号开头");

			body.offset(thisdepth);
			var keysb = new StringBuilder();
			for (var c = body.charAt(); c != ':'; c = body.offset(1).charAt()) {
				if (c == '\n') throw new YAMLException("格式错误,键名不存在");
				keysb.append(body.charAt());
			}
			var key = Yaml.deserialization(keysb.toString());
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
					this.put(key, body.charAt(body.site() + nextdepth) == '-' ? new YAMLArray(body, nextdepth) : new YAMLObject(body, nextdepth));
				} else this.put(key, Yaml.deserialization(value.toString()));
			} else if (body.charAt() == '\n') {
				var nextdepth = Yaml.indentation(body.offset(1));
				this.put(key, body.charAt(body.site() + nextdepth) == '-' ? new YAMLArray(body, nextdepth) : new YAMLObject(body, nextdepth));
			} else {
				throw new YAMLException("符号':'后面必须跟随空格");
			}
		}
	}

	/**
	 * 解析并获取YAML对象
	 *
	 * @param body YAML字符串
	 * @return YAML对象
	 */
	public static YAMLObject parseObject(@NotNull String body) {
		// if (builder.site() + 1 != builder.length()) throw new YAMLException("格式错误,在封闭符号之后仍然存在数据");
		return new YAMLObject(new ParserStringBuilder(body), 0);
	}

	/**
	 * 解析并获取YAML对象
	 *
	 * @param m Map数组
	 * @return YAML对象
	 */
	public static <K, V> YAMLObject parseObject(@NotNull Map<? super K, ? super V> m) {
		var object = new YAMLObject();
		m.forEach((key, value) -> object.put(String.valueOf(key), value));
		return object;
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
	 * 使用规则对YAML进行快捷解析,查询规则键{@link YAMLPath#select(String, Class)}
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
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public Object get(@NotNull String key) {
		return super.get(key);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public Object getOrDefault(@NotNull String key, @NotNull Object value) {
		var this_value = super.get(key);
		return this_value == null ? value : this_value;
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时获取另一个键值
	 * @return 值
	 */
	public Object getOrOther(@NotNull String key, @NotNull String value) {
		var this_value = super.get(key);
		return this_value == null ? super.get(value) : this_value;
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key       名称
	 * @param value     不存在对应键时返回该值
	 * @param itemClass 待转换等待类型
	 * @return 值
	 */
	public <T> T getOrDefault(@NotNull String key, @NotNull Object value, @NotNull Class<T> itemClass) {
		return TypeUtil.convert(this.getOrDefault(key, value), itemClass);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key       名称
	 * @param value     不存在对应键时获取另一个键值
	 * @param itemClass 待转换等待类型
	 * @return 值
	 */
	public <T> T getOrOther(@NotNull String key, @NotNull String value, @NotNull Class<T> itemClass) {
		return TypeUtil.convert(this.getOrOther(key, value), itemClass);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key    名称
	 * @param mapper 函数式接口,用于指定转换类型
	 * @param <T>    返回泛型
	 * @return 值
	 */
	public <T> T get(@NotNull String key, @NotNull Function<Object, ? extends T> mapper) {
		return mapper.apply(this.get(key));
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key  名称
	 * @param type TypeReference接口类型
	 * @param <T>  返回泛型
	 * @return 值
	 */
	public <T> T get(@NotNull String key, @NotNull TypeReference<T> type) {
		return TypeUtil.convert(this.get(key), type);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key       名称
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 值
	 */
	public <T> T get(@NotNull String key, @NotNull Class<T> itemClass) {
		return TypeUtil.convert(this.get(key), itemClass);
	}

	/**
	 * 获取名称对应键的数组
	 *
	 * @param key       名称
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 数组
	 */
	public <T> List<T> getList(@NotNull String key, @NotNull Class<T> itemClass) {
		return TypeUtil.convertList(this.get(key), itemClass);
	}

	/**
	 * @param key        名称
	 * @param keyClass   指定键类型
	 * @param valueClass 指定值类型
	 * @param <K>        键泛型
	 * @param <V>        值泛型
	 * @return Map对象
	 */
	public <K, V> Map<K, V> getMap(@NotNull String key, @NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		return TypeUtil.convertMap(this.get(key), keyClass, valueClass);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public String getString(@NotNull String key) {
		return this.get(key, String.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public String getStringValue(@NotNull String key, String value) {
		return TypeUtil.convert(this.getOrDefault(key, value), String.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public boolean getBoolean(@NotNull String key) {
		return this.get(key, Boolean.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public boolean getBooleanValue(@NotNull String key, boolean value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Boolean.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public byte getByte(@NotNull String key) {
		return this.get(key, Byte.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public byte getByteValue(@NotNull String key, byte value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Byte.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public short getShort(@NotNull String key) {
		return this.get(key, Short.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public short getShortValue(@NotNull String key, short value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Short.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public int getInteger(@NotNull String key) {
		return this.get(key, Integer.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public int getIntegerValue(@NotNull String key, int value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Integer.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public long getLong(@NotNull String key) {
		return this.get(key, Long.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public long getLongValue(@NotNull String key, long value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Long.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public float getFloat(@NotNull String key) {
		return this.get(key, Float.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public float getFloatValue(@NotNull String key, float value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Float.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public double getDouble(@NotNull String key) {
		return this.get(key, Double.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public double getDoubleValue(@NotNull String key, double value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Double.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public BigDecimal getBigDecimal(@NotNull String key) {
		return this.get(key, BigDecimal.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public BigDecimal getBigDecimalValue(@NotNull String key, double value) {
		return TypeUtil.convert(this.getOrDefault(key, value), BigDecimal.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public BigInteger getBigInteger(@NotNull String key) {
		return this.get(key, BigInteger.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public BigInteger getBigIntegerValue(@NotNull String key, double value) {
		return TypeUtil.convert(this.getOrDefault(key, value), BigInteger.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public YAMLObject getYAMLObject(@NotNull String key) {
		return this.get(key, YAMLObject.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public YAMLObject getYAMLObjectValue(@NotNull String key, YAMLObject value) {
		return TypeUtil.convert(this.getOrDefault(key, value), YAMLObject.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public YAMLArray getYAMLArray(@NotNull String key) {
		return this.get(key, YAMLArray.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public YAMLArray getYAMLArrayValue(@NotNull String key, YAMLArray value) {
		return TypeUtil.convert(this.getOrDefault(key, value), YAMLArray.class);
	}

	/**
	 * 转换为指定类型的Map数组
	 *
	 * @param keyClass   Map键类型
	 * @param valueClass Map值类型
	 * @param <K>        键泛型
	 * @param <V>        值泛型
	 * @return Map数组
	 */
	public <K, V> Map<K, V> toMap(@NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		var map = new LinkedHashMap<K, V>();
		this.forEach((key, value) -> map.put(TypeUtil.convert(key, keyClass), TypeUtil.convert(value, valueClass)));
		return map;
	}

	/**
	 * 获取排序后的YAMLOject对象
	 *
	 * @param comparator 排序规则
	 * @return 排序后的YAMLOject对象
	 */
	public YAMLObject sort(@NotNull Comparator<Map.Entry<String, YAMLObject>> comparator) {
		var thisList = new ArrayList<>(this.toMap(String.class, YAMLObject.class).entrySet());
		thisList.sort(comparator);
		var result = new YAMLObject();
		for (var info : thisList) result.put(info.getKey(), info.getValue());
		return result;
	}

	/**
	 * 获取排序后的YAMLOject对象
	 *
	 * @param comparator 排序规则
	 * @param limit      返回前N个数据
	 * @return 排序后的YAMLOject对象
	 */
	public YAMLObject sort(@NotNull Comparator<Map.Entry<String, YAMLObject>> comparator, int limit) {
		var thisList = new ArrayList<>(this.toMap(String.class, YAMLObject.class).entrySet());
		thisList.sort(comparator);
		var result = new YAMLObject();
		for (var info : thisList.stream().limit(limit).toList()) result.put(info.getKey(), info.getValue());
		return result;
	}

	/**
	 * 获取排序后的YAMLOject对象
	 *
	 * @param comparator 排序规则
	 * @param fromIndex  子列表的低端点（含）
	 * @param toIndex    子列表的高端点（不包括）
	 * @return 排序后的YAMLOject对象
	 */
	public YAMLObject sort(@NotNull Comparator<Map.Entry<String, YAMLObject>> comparator, int fromIndex, int toIndex) {
		var thisList = new ArrayList<>(this.toMap(String.class, YAMLObject.class).entrySet());
		thisList.sort(comparator);
		var result = new YAMLObject();
		for (var info : thisList.subList(fromIndex, toIndex)) result.put(info.getKey(), info.getValue());
		return result;
	}

	/**
	 * 返回此列表中指定的fromIndex （包含）和toIndex （不包含）之间的部分的视图。
	 * <p>
	 * （如果fromIndex和toIndex相等，则返回的列表为空。）返回的列表受此列表支持，因此返回列表中的非结构性更改会反映在此列表中，反之亦然。
	 * <p>
	 * 返回的列表支持所有可选列表操作。
	 * <p>
	 * 此方法消除了对显式范围操作（数组通常存在的类型）的需要。通过传递子列表视图而不是整个列表，任何需要列表的操作都可以用作范围操作。例如，以下惯用法从列表中删除一系列元素：
	 * list.subList(from, to).clear();
	 * <p>
	 * 可以为indexOf(Object)和lastIndexOf(Object)构建类似的习惯用法，并且Collections类中的所有算法都可以应用于子列表。
	 * <p>
	 * 如果支持列表（即此列表）以除返回列表之外的任何方式进行结构修改，则此方法返回的列表的语义将变得不确定。
	 * <p>
	 * （结构修改是那些改变此列表大小的修改，或者以其他方式扰乱它，以致正在进行的迭代可能会产生不正确的结果。）
	 *
	 * @param fromIndex 子列表的低端点（含）
	 * @param toIndex   子列表的高端点（不包括）
	 * @return 此列表中指定范围的视图
	 */
	public YAMLObject subList(int fromIndex, int toIndex) {
		var thisList = new ArrayList<>(this.toMap(String.class, YAMLObject.class).entrySet());
		return new YAMLObject(thisList.subList(fromIndex, toIndex).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param key   键
	 * @param value 值
	 * @return 自身
	 */
	public YAMLObject fluentPut(String key, Object value) {
		super.put(key, value);
		return this;
	}

	/**
	 * 添加所有元素并返回自身
	 *
	 * @param m Map数组
	 * @return 自身
	 */
	public YAMLObject fluentPutAll(Map<? extends String, ?> m) {
		super.putAll(m);
		return this;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	@NotNull
	public String toString(int depth) {
		return this.entrySet().stream().map(token -> "  ".repeat(depth) + StringUtil.toEscape(token.getKey()) + ":" + YAMLFormat.toOutFormat(token.getValue(), depth)).collect(Collectors.joining("\n"));
	}

}
