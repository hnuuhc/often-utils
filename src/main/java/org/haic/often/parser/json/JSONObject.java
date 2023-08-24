package org.haic.often.parser.json;

import org.haic.often.annotations.NotNull;
import org.haic.often.exception.JSONException;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.parser.xml.Element;
import org.haic.often.util.StringUtil;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * JSON对象类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/4 23:07
 */
public class JSONObject extends LinkedHashMap<String, Object> {

	public JSONObject() {super();}

	public JSONObject(Map<? extends String, ?> m) {super(m);}

	/**
	 * 这是解析用构建,切勿使用
	 *
	 * @param body 字符串
	 */
	public JSONObject(@NotNull ParserStringBuilder body) {
		if (body.charAt() != '{') throw new JSONException("位置 " + body.pos() + " 处格式错误期望值不为'{'");
		if (body.offset(1).stripLeading().charAt() == '}') return;
		while (body.isNoOutBounds()) {
			if (body.charAt() == ':') throw new JSONException("位置 " + body.pos() + " 处不存在键");
			String key;
			switch (body.charAt()) {
				case '"', '\'' -> {
					key = body.intercept();
					body.offset(1);
				}
				default -> {
					var keySb = new StringBuilder();
					for (char c = body.charAt(); Character.isLetterOrDigit(c) || c == '_'; c = body.offset(1).charAt()) {
						keySb.append(body.charAt());
					}
					key = keySb.toString();
				}
			}
			if (body.stripLeading().charAt() != ':') throw new JSONException("位置 " + body.pos() + " 处期望值不为':'");
			body.offset(1).stripLeading();
			switch (body.charAt()) {
				case '"', '\'' -> this.put(key, body.intercept());
				case '{' -> this.put(key, new JSONObject(body));
				case '[' -> this.put(key, new JSONArray(body));
				case 'n' -> {
					if (body.startsWith("null")) this.put(key, null);
					else throw new JSONException("位置 " + body.pos() + " 处期望值不为'null'");
					body.offset(3);
				}
				case 't' -> {
					if (body.startsWith("true")) this.put(key, true);
					else throw new JSONException("位置 " + body.pos() + " 处期望值不为'true'");
					body.offset(3);
				}
				case 'f' -> {
					if (body.startsWith("false")) this.put(key, false);
					else throw new JSONException("位置 " + body.pos() + " 处期望值不为'false'");
					body.offset(4);
				}
				case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
					var value = new StringBuilder();
					do {
						value.append(body.charAt());
					} while (Character.isDigit(body.offset(1).charAt()) || body.charAt() == '.');
					if (body.charAt() == 'e' || body.charAt() == 'E') { // 自然数
						value.append('e');
						if (body.offset(1).charAt() == '+') {
							value.append('+');
							body.offset(1);
						}
						do value.append(body.charAt()); while (Character.isDigit(body.offset(1).charAt()));
					}
					this.put(key, new JSONNumber(value.toString()));
					body.offset(-1); // 修正索引
				}
				default -> throw new JSONException("位置 " + body.pos() + " 处期望值不为'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '['");
			}
			if (body.offset(1).stripLeading().charAt() == '}') return;
			if (body.charAt() != ',') throw new JSONException("位置 " + body.pos() + " 处期望值不为分隔符','");
			body.offset(1).stripLeading();
		}
		throw new JSONException("数据未封闭");
	}

	/**
	 * 解析并获取JSON对象
	 *
	 * @param body JSON字符串
	 * @return JSON对象
	 */
	public static JSONObject parseObject(@NotNull String body) {
		var builder = new ParserStringBuilder(body).strip();
		var object = new JSONObject(builder);
		if (builder.pos() + 1 != builder.length()) throw new JSONException("格式错误,在封闭符号之后仍然存在数据");
		return object;
	}

	/**
	 * 解析并获取JSON对象
	 *
	 * @param m Map数组
	 * @return JSON对象
	 */
	public static <K, V> JSONObject parseObject(@NotNull Map<? super K, ? super V> m) {
		var object = new JSONObject();
		m.forEach((key, value) -> object.put(String.valueOf(key), value));
		return object;
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	public Object select(@NotNull String cssQuery) {
		return new JSONPath(this).select(cssQuery);
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @param type     指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, TypeReference<T> type) {
		return new JSONPath(this).select(cssQuery, type);
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param cssQuery 查询规则
	 * @param clazz    指定返回类型
	 * @param <T>      返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String cssQuery, Class<T> clazz) {
		return new JSONPath(this).select(cssQuery, clazz);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public Object get(@NotNull String key) {
		return JSONFormat.format(super.get(key));
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
		return JSONFormat.format(this_value == null ? value : this_value);
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
		return JSONFormat.format(this_value == null ? super.get(value) : this_value);
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
	public JSONObject getJSONObject(@NotNull String key) {
		return this.get(key, JSONObject.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public JSONObject getJSONObjectValue(@NotNull String key, JSONObject value) {
		return TypeUtil.convert(this.getOrDefault(key, value), JSONObject.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	public JSONArray getJSONArray(@NotNull String key) {
		return this.get(key, JSONArray.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key   名称
	 * @param value 不存在对应键时返回该值
	 * @return 值
	 */
	public JSONArray getJSONArrayValue(@NotNull String key, JSONArray value) {
		return TypeUtil.convert(this.getOrDefault(key, value), JSONArray.class);
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
	 * 添加元素并返回自身
	 *
	 * @param key   键
	 * @param value 值
	 * @return 自身
	 */
	public JSONObject fluentPut(String key, Object value) {
		super.put(key, value);
		return this;
	}

	/**
	 * 添加所有元素并返回自身
	 *
	 * @param m Map数组
	 * @return 自身
	 */
	public JSONObject fluentPutAll(Map<? extends String, ?> m) {
		super.putAll(m);
		return this;
	}

	/**
	 * 将树状结构转化为 {@link Element} 类型
	 * <p>
	 * 在每个节点中必须包含指定键键,详见: {@link #toXmlTree(Element)}
	 *
	 * @return Element对象
	 */
	public Element toXmlTree() {
		return toXmlTree(null);
	}

	/**
	 * 将树状结构转化为 {@link Element} 类型
	 * <p>
	 * 在每个节点中必须包含键:
	 * <pre>	name - 节点名称</pre>
	 * <pre>	attr - 节点属性</pre>
	 * <pre>	child - 子节点</pre>
	 * <pre>	close - 是否为自闭合</pre>
	 *
	 * @param parent 父节点
	 * @return Element对象
	 */
	public Element toXmlTree(Element parent) {
		var xml = new Element(parent, this.getString("name")).close(this.getBoolean("close")).addAttrs(this.get("attr", new TypeReference<>() {}));
		return xml.addChilds(this.get("child", JSONArray.class).toXmlChilds(xml));
	}

	/**
	 * 输出当前JSON数据,并对中文进行转义,使其符合JSON传输要求
	 *
	 * @return JSON字符串
	 */
	@NotNull
	public String toJSONString() {
		return '{' + this.entrySet().stream().map(token -> '"' + StringUtil.chineseToUnicode(StringUtil.toEscape(token.getKey())) + "\":" + JSONFormat.toJSONFormat(token.getValue())).collect(Collectors.joining(",")) + '}';
	}

	@Override
	public String toString() {
		return '{' + this.entrySet().stream().map(token -> '"' + StringUtil.toEscape(token.getKey()) + "\":" + JSONFormat.toOutFormat(token.getValue())).collect(Collectors.joining(",")) + '}';
	}

	/**
	 * 以指定的深度格式化当前JSON
	 *
	 * @param depth 深度
	 * @return 格式化的JSON
	 */
	@NotNull
	public String toString(int depth) {
		return this.isEmpty() ? "{}" : '{' + this.entrySet().stream().map(token -> '\n' + "    ".repeat(depth + 1) + '"' + StringUtil.toEscape(token.getKey()) + "\":" + JSONFormat.toOutFormat(token.getValue(), depth)).collect(Collectors.joining(",")) + "\n" + "    ".repeat(depth) + '}';
	}

}
