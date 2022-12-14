package org.haic.often.parser.json;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.JSONException;
import org.haic.often.util.StringUtil;
import org.haic.often.util.TypeUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

	public JSONObject() {
		super();
	}

	/**
	 * 这是解析用构建,切勿使用
	 *
	 * @param body 字符串
	 */
	public JSONObject(@NotNull StringBuilder body) {
		if (body.charAt(0) != '{') {
			throw new JSONException("格式错误: " + body);
		}
		if (body.charAt(1) == '}') { // 分隔符或结束符
			body.delete(0, 2);
			return;
		}
		body.deleteCharAt(0);
		for (int i = 0; i < body.length(); i++) {
			while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
			if (body.charAt(i) == ':') throw new JSONException("在下标 " + i + " 处不存在键");
			StringBuilder key = new StringBuilder();
			if (body.charAt(i) == '"') {
				i = StringUtil.interceptString(body, key, i);
			} else {
				while (Character.isLetterOrDigit(body.charAt(i)) || body.charAt(i) == '_') {
					key.append(body.charAt(i++));
				}
			}
			do i++; while (Character.isWhitespace(body.charAt(i))); // 跳过空格

			if (body.charAt(i) != ':') {
				throw new JSONException("解析错误,期望值不为':'");
			}
			do i++; while (Character.isWhitespace(body.charAt(i))); // 跳过空格
			switch (body.charAt(i)) {
				case '"' -> { // 键可能不存在引号
					StringBuilder value = new StringBuilder();
					i = StringUtil.interceptString(body, value, i) + 1;
					this.put(key.toString(), value.toString());
				}
				case 'n' -> {
					if (body.charAt(++i) == 'u' && body.charAt(++i) == 'l' && body.charAt(++i) == 'l') {
						this.put(key.toString(), null);
					} else {
						throw new JSONException("期望值不为'NULL'");
					}
					i++;
				}
				case 't' -> {
					if (body.charAt(++i) == 'r' && body.charAt(++i) == 'u' && body.charAt(++i) == 'e') {
						this.put(key.toString(), true);
					} else {
						throw new JSONException("期望值不为'TRUE'");
					}
					i++;
				}
				case 'f' -> {
					if (body.charAt(++i) == 'a' && body.charAt(++i) == 'l' && body.charAt(++i) == 's' && body.charAt(++i) == 'e') {
						this.put(key.toString(), false);
					} else {
						throw new JSONException("期望值不为'FALSE'");
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
						do {
							value.append(body.charAt(i++));
						} while (Character.isDigit(body.charAt(i)));
					}
					this.put(key.toString(), new JSONNumber(value.toString()));
				}
				case '{' -> {
					body.delete(0, i);
					this.put(key.toString(), new JSONObject(body));
					i = 0;
				}
				case '[' -> {
					body.delete(0, i);
					this.put(key.toString(), new JSONArray(body));
					i = 0;
				}
				default -> throw new JSONException("期望值不为'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '['");
			}
			while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
			if (body.charAt(i) == '}') { // 分隔符或结束符
				body.delete(0, i + 1);
				return;
			} else if (body.charAt(i) != ',') {
				throw new JSONException("分隔符期待值不为','");
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
		return new JSONObject(new StringBuilder(body.strip()));
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
	 * 获取名称对应键的数组
	 *
	 * @param key       名称
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 数组
	 */
	@Contract(pure = true)
	public <T> ArrayList<T> getList(@NotNull String key, @NotNull Class<T> itemClass) {
		return TypeUtil.convertList(this.get(key), itemClass);
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
	 * @param keyItemClass   Map键类型
	 * @param valueItemClass Map值类型
	 * @param <K>            键泛型
	 * @param <V>            值泛型
	 * @return Map数组
	 */
	@Contract(pure = true)
	public <K, V> Map<K, V> toMap(Class<K> keyItemClass, Class<V> valueItemClass) {
		Map<K, V> map = new HashMap<>();
		this.forEach((key, value) -> map.put(TypeUtil.convert(key, keyItemClass), TypeUtil.convert(value, valueItemClass)));
		return map;
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param key   键
	 * @param value 值
	 * @return 自身
	 */
	public JSONObject fluentPut(@NotNull String key, Object value) {
		super.put(key, value);
		return this;
	}

	/**
	 * 添加所有元素并返回自身
	 *
	 * @param m Map数组
	 * @return 自身
	 */
	public JSONObject fluentPutAll(@NotNull Map<String, Object> m) {
		super.putAll(m);
		return this;
	}

	@Override
	public String toString() {
		return new StringBuilder().append('{').append(this.entrySet().stream().map(token -> '"' + StringUtil.toEscape(token.getKey()) + "\":" + JSONFormat.toOutFormat(token.getValue())).collect(Collectors.joining(","))).append('}').toString();
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
		return new StringBuilder().append('{').append(this.entrySet().stream().map(token -> '\n' + "    ".repeat(depth + 1) + '"' + StringUtil.toEscape(token.getKey()) + "\":" + JSONFormat.toOutFormat(token.getValue(), depth)).collect(Collectors.joining(","))).append("\n").append("    ".repeat(depth)).append('}').toString();
	}

}
