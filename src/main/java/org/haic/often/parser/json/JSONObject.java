package org.haic.often.parser.json;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.JSONException;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.util.StringUtil;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
		if (body.charAt(body.pos()) != '{') throw new JSONException("位置 " + body.pos() + " 处格式错误期望值不为'{'");
		if (body.charAt(body.offset(1).stripLeading().pos()) == '}') return;
		for (int i = body.pos(); i < body.length(); i++) {
			while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
			if (body.charAt(i) == ':') throw new JSONException("位置 " + i + " 处不存在键");
			String key;
			switch (body.charAt(i)) {
				case '"', '\'' -> {
					key = body.pos(i).intercept();
					i = body.pos() + 1;
				}
				default -> {
					StringBuilder keySb = new StringBuilder();
					while (Character.isLetterOrDigit(body.charAt(i)) || body.charAt(i) == '_') keySb.append(body.charAt(i++));
					key = keySb.toString();
				}
			}
			while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
			if (body.charAt(i) != ':') throw new JSONException("位置 " + i + " 处期望值不为':'");
			do i++; while (Character.isWhitespace(body.charAt(i))); // 跳过空格
			switch (body.charAt(i)) {
				case '"', '\'' -> { // 键可能不存在引号
					String value = body.pos(i).intercept();
					i = body.pos() + 1;
					this.put(key, value);
				}
				case 'n' -> {
					if (body.charAt(++i) == 'u' && body.charAt(++i) == 'l' && body.charAt(++i) == 'l') {
						this.put(key, null);
					} else {
						throw new JSONException("位置 " + i + " 处期望值不为'NULL'");
					}
					i++;
				}
				case 't' -> {
					if (body.charAt(++i) == 'r' && body.charAt(++i) == 'u' && body.charAt(++i) == 'e') {
						this.put(key, true);
					} else {
						throw new JSONException("位置 " + i + " 处期望值不为'TRUE'");
					}
					i++;
				}
				case 'f' -> {
					if (body.charAt(++i) == 'a' && body.charAt(++i) == 'l' && body.charAt(++i) == 's' && body.charAt(++i) == 'e') {
						this.put(key, false);
					} else {
						throw new JSONException("位置 " + i + " 处期望值不为'FALSE'");
					}
					i++;
				}
				case '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
					StringBuilder value = new StringBuilder();
					do {
						value.append(body.charAt(i++));
					} while (Character.isDigit(body.charAt(i)) || body.charAt(i) == '.');
					if (body.charAt(i) == 'e' || body.charAt(i) == 'E') { // 自然数
						value.append(body.charAt(i++));
						if (body.charAt(i) == '+') {
							value.append('+');
							i++;
						}
						do value.append(body.charAt(i++)); while (Character.isDigit(body.charAt(i)));
					}
					this.put(key, new JSONNumber(value.toString()));
				}
				case '{' -> {
					this.put(key, new JSONObject(body.pos(i)));
					i = body.pos() + 1;
				}
				case '[' -> {
					this.put(key, new JSONArray(body.pos(i)));
					i = body.pos() + 1;
				}
				default -> throw new JSONException("位置 " + i + " 处期望值不为'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '['");
			}
			while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
			if (body.charAt(i) == '}') { // 分隔符或结束符
				body.pos(i);
				return;
			} else if (body.charAt(i) != ',') {
				throw new JSONException("位置 " + i + " 处期望值不为分隔符','");
			}
		}
	}

	/**
	 * 解析并获取JSON对象
	 *
	 * @param body JSON字符串
	 * @return JSON对象
	 */
	@Contract(pure = true)
	public static JSONObject parseObject(@NotNull String body) {
		ParserStringBuilder builder = new ParserStringBuilder(body).strip();
		JSONObject object = new JSONObject(builder);
		if (builder.pos() + 1 != builder.length()) throw new JSONException("格式错误,在封闭符号之后仍然存在数据");
		return object;
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param regex 查询规则
	 * @param type  指定返回类型
	 * @param <T>   返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String regex, TypeReference<T> type) {
		return new JSONPath(this).select(regex, type);
	}

	/**
	 * 使用规则对JSON进行快捷解析,查询规则键{@link JSONPath#select(String, Class)}
	 *
	 * @param regex 查询规则
	 * @param clazz 指定返回类型
	 * @param <T>   返回结果类型
	 * @return 查询结果
	 */
	public <T> T select(@NotNull String regex, Class<T> clazz) {
		return new JSONPath(this).select(regex, clazz);
	}

	/**
	 * 解析并获取JSON对象
	 *
	 * @param m Map数组
	 * @return JSON对象
	 */
	@Contract(pure = true)
	public static <K, V> JSONObject parseObject(@NotNull Map<? super K, ? super V> m) {
		JSONObject object = new JSONObject();
		m.forEach((key, value) -> object.put(String.valueOf(key), value));
		return object;
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public Object getOrDefault(@NotNull String key, @NotNull Object value) {
		return JSONFormat.format(super.getOrDefault(key, value));
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key       名称
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 值
	 */
	@Contract(pure = true)
	public <T> T get(@NotNull String key, @NotNull Class<T> itemClass) {
		return TypeUtil.convert(this.get(key), itemClass);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key  名称
	 * @param type TypeReference接口类型
	 * @param <T>  返回泛型
	 * @return 值
	 */
	@Contract(pure = true)
	public <T> T get(@NotNull String key, @NotNull TypeReference<T> type) {
		return TypeUtil.convert(this.get(key), type);
	}

	/**
	 * 获取名称对应键的数组
	 *
	 * @param key       名称
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 数组
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public String getStringValue(@NotNull String key, String value) {
		return TypeUtil.convert(this.getOrDefault(key, value), String.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public boolean getBooleanValue(@NotNull String key, boolean value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Boolean.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public byte getByteValue(@NotNull String key, byte value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Byte.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public short getShortValue(@NotNull String key, short value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Short.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public int getIntegerValue(@NotNull String key, int value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Integer.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public long getLongValue(@NotNull String key, long value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Long.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public float getFloatValue(@NotNull String key, float value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Float.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public double getDoubleValue(@NotNull String key, double value) {
		return TypeUtil.convert(this.getOrDefault(key, value), Double.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
	public JSONObject getJSONObjectValue(@NotNull String key, JSONObject value) {
		return TypeUtil.convert(this.getOrDefault(key, value), JSONObject.class);
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param key 名称
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public <K, V> Map<K, V> toMap(@NotNull Class<K> keyClass, @NotNull Class<V> valueClass) {
		Map<K, V> map = new HashMap<>();
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

	@Override
	public String toString() {
		return '{' + this.entrySet().stream().map(token -> '"' + StringUtil.toEscape(token.getKey()) + "\":" + JSONFormat.toOutFormat(token.getValue())).collect(Collectors.joining(",")) + '}';
	}

	/**
	 * 以指定的深度格式化当前标签
	 *
	 * @param depth 深度
	 * @return 格式化的标签
	 */
	@NotNull
	@Contract(pure = true)
	public String toString(int depth) {
		return '{' + this.entrySet().stream().map(token -> '\n' + "    ".repeat(depth + 1) + '"' + StringUtil.toEscape(token.getKey()) + "\":" + JSONFormat.toOutFormat(token.getValue(), depth)).collect(Collectors.joining(",")) + "\n" + "    ".repeat(depth) + '}';
	}

}
