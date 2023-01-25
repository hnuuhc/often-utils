package org.haic.often.parser.json;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.JSONException;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JSON数组类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/6 0:30
 */
public class JSONArray extends ArrayList<Object> {

	public JSONArray() {super();}

	public JSONArray(Collection<?> c) {super(c);}

	/**
	 * 这是解析用构建,切勿使用
	 *
	 * @param body 字符串
	 */
	public JSONArray(@NotNull ParserStringBuilder body) {
		if (body.charAt(body.pos()) == '[') {
			if (body.charAt(body.offset(1).stripLeading().pos()) == ']') return;
			for (int i = body.pos(); i < body.length(); i++) {
				while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
				switch (body.charAt(i)) {
					case '"', '\'' -> {
						String value = body.pos(i).interceptString();
						i = body.pos() + 1;
						this.add(value);
					}
					case 'n' -> {
						if (body.charAt(++i) == 'u' && body.charAt(++i) == 'l' && body.charAt(++i) == 'l') {
							this.add(null);
						} else {
							throw new JSONException("位置 " + i + " 处期望值不为'NULL'");
						}
						i++;
					}
					case 't' -> {
						if (body.charAt(++i) == 'r' && body.charAt(++i) == 'u' && body.charAt(++i) == 'e') {
							this.add(true);
						} else {
							throw new JSONException("位置 " + i + " 处期望值不为'TRUE'");
						}
						i++;
					}
					case 'f' -> {
						if (body.charAt(++i) == 'a' && body.charAt(++i) == 'l' && body.charAt(++i) == 's' && body.charAt(++i) == 'e') {
							this.add(false);
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
						this.add(new JSONNumber(value.toString()));
					}
					case '{' -> {
						this.add(new JSONObject(body.pos(i)));
						i = body.pos() + 1;
					}
					case '[' -> {
						this.add(new JSONArray(body.pos(i)));
						i = body.pos() + 1;
					}
					default -> throw new JSONException("位置 " + i + " 处期望值不为'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '['");
				}
				while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
				if (body.charAt(i) == ']') {
					body.pos(i);
					return;
				} else if (body.charAt(i) != ',') {
					throw new JSONException("位置 " + i + " 处期望值不为分隔符','");
				}
			}
		} else if (body.charAt(body.pos()) == '{') {
			this.add(new JSONObject(body));
		} else {
			throw new JSONException("位置 " + body.pos() + " 处格式错误期望值不为'['或'{'");
		}
	}

	/**
	 * 解析并获取JSON数组
	 *
	 * @param body JSON字符串
	 * @return JSON数组
	 */
	@Contract(pure = true)
	public static JSONArray parseArray(@NotNull String body) {
		ParserStringBuilder builder = new ParserStringBuilder(body).strip();
		JSONArray object = new JSONArray(builder);
		if (builder.pos() + 1 != builder.length()) throw new JSONException("格式错误,在封闭符号之后仍然存在数据");
		return object;
	}

	/**
	 * 解析并获取JSON数组
	 *
	 * @param list 数组
	 * @return JSON数组
	 */
	@Contract(pure = true)
	public static JSONArray parseArray(@NotNull Collection<?> list) {
		return new JSONArray().fluentAddAll(list);
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
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public Object get(int i) {
		return JSONFormat.format(super.get(i));
	}

	/**
	 * 获取名称对应键的值
	 *
	 * @param i    要返回的元素的索引
	 * @param type TypeReference接口类型
	 * @param <T>  返回泛型
	 * @return 值
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public String getString(int i) {
		return this.get(i, String.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public boolean getBoolean(int i) {
		return this.get(i, Boolean.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public byte getByte(int i) {
		return this.get(i, Byte.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public short getShort(int i) {
		return this.get(i, Short.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public int getInteger(int i) {
		return this.get(i, Integer.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public long getLong(int i) {
		return this.get(i, Long.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public float getFloat(int i) {
		return this.get(i, Float.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public double getDouble(int i) {
		return this.get(i, Double.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public JSONObject getJSONObject(int i) {
		return this.get(i, JSONObject.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i 要返回的元素的索引
	 * @return 值
	 */
	@Contract(pure = true)
	public JSONArray getJSONArray(int i) {
		return this.get(i, JSONArray.class);
	}

	/**
	 * 转换为指定类型数组
	 *
	 * @param itemClass 指定类型
	 * @param <T>       数组泛型
	 * @return 指定类型的数组
	 */
	@Contract(pure = true)
	public <T> List<T> toList(@NotNull Class<T> itemClass) {
		return TypeUtil.convertList(this, itemClass);
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param obj 待添加的元素
	 * @return 自身
	 */
	public JSONArray fluentAdd(Object obj) {
		super.add(obj);
		return this;
	}

	/**
	 * 添加数组所有元素并返回自身
	 *
	 * @param c 数组
	 * @return 自身
	 */
	public JSONArray fluentAddAll(Collection<?> c) {
		super.addAll(c);
		return this;
	}

	@Override
	public String toString() {
		return '[' + this.stream().map(JSONFormat::toOutFormat).collect(Collectors.joining(",")) + ']';
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
		return '[' + this.stream().map(token -> '\n' + "    ".repeat(depth + 1) + JSONFormat.toOutFormat(token, depth)).collect(Collectors.joining(",")) + "\n" + "    ".repeat(depth) + ']';
	}

}
