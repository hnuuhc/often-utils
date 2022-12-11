package org.haic.often.parser.json;

import org.haic.often.exception.JSONException;
import org.haic.often.util.StringUtil;
import org.haic.often.util.TypeUtil;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * JSON数组类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/6 0:30
 */
public class JSONArray extends ArrayList<Object> {

	public JSONArray() {
		super();
	}

	/**
	 * 这是解析用构建,切勿使用
	 *
	 * @param body 字符串
	 */
	public JSONArray(StringBuilder body) {
		if (body.charAt(0) == '{') {
			this.add(new JSONObject(body));
		} else if (body.charAt(0) == '[') {
			if (body.charAt(1) == ']') { // 分隔符或结束符
				body.delete(0, 2);
				return;
			}
			body.deleteCharAt(0);
			for (int i = 0; i < body.length(); i++) {
				while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
				switch (body.charAt(i)) {
					case '"' -> {
						StringBuilder value = new StringBuilder();
						i = StringUtil.interceptString(body, value, i) + 1;
						this.add(value.toString());
					}
					case 'n' -> {
						if (body.charAt(++i) == 'u' && body.charAt(++i) == 'l' && body.charAt(++i) == 'l') {
							this.add(null);
						} else {
							throw new JSONException("期望值不为'NULL'");
						}
						i++;
					}
					case 't' -> {
						if (body.charAt(++i) == 'r' && body.charAt(++i) == 'u' && body.charAt(++i) == 'e') {
							this.add(true);
						} else {
							throw new JSONException("期望值不为'TRUE'");
						}
						i++;
					}
					case 'f' -> {
						if (body.charAt(++i) == 'a' && body.charAt(++i) == 'l' && body.charAt(++i) == 's' && body.charAt(++i) == 'e') {
							this.add(false);
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
						this.add(value);
					}
					case '{' -> {
						body.delete(0, i);
						this.add(new JSONObject(body));
						i = 0;
					}
					case '[' -> {
						body.delete(0, i);
						this.add(new JSONArray(body));
						i = 0;
					}
					default -> throw new JSONException("期望值不为'STRING', 'NUMBER', 'NULL', 'TRUE', 'FALSE', '{', '['");
				}
				while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
				if (body.charAt(i) == ']') {
					body.delete(0, i + 1);
					return;
				} else if (body.charAt(i) != ',') {
					throw new JSONException("分隔符期待值不为','");
				}
			}
		} else {
			throw new JSONException("格式错误: " + body);
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
		return new JSONArray(new StringBuilder(body.strip()));
	}

	/**
	 * 解析并获取JSON数组
	 *
	 * @param list 数组
	 * @return JSON数组
	 */
	@Contract(pure = true)
	public static JSONArray parseArray(@NotNull List<?> list) {
		return new JSONArray().fluentAddAll(list);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public Object get(int i) {
		return super.get(i);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public String getString(int i) {
		return TypeUtil.convert(super.get(i), String.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public boolean getBoolean(int i) {
		return TypeUtil.convert(super.get(i), Boolean.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public byte getByte(int i) {
		return TypeUtil.convert(super.get(i), Byte.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public short getShort(int i) {
		return TypeUtil.convert(super.get(i), Short.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public int getInteger(int i) {
		return TypeUtil.convert(super.get(i), Integer.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public long getLong(int i) {
		return TypeUtil.convert(super.get(i), Long.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public float getFloat(int i) {
		return TypeUtil.convert(super.get(i), Float.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public double getDouble(int i) {
		return TypeUtil.convert(super.get(i), Double.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public JSONObject getJSONObject(int i) {
		return TypeUtil.convert(super.get(i), JSONObject.class);
	}

	/**
	 * 获取对应索引的值
	 *
	 * @param i index of the element to return
	 * @return 值
	 */
	@Contract(pure = true)
	public JSONArray getJSONArray(int i) {
		return TypeUtil.convert(super.get(i), JSONArray.class);
	}

	/**
	 * 转换为指定类型数组
	 *
	 * @param itemClass 指定类型
	 * @param <T>       数组泛型
	 * @return 指定类型的数组
	 */
	@Contract(pure = true)
	public <T> List<T> toList(Class<T> itemClass) {
		return TypeUtil.convert(this, itemClass);
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param obj 待添加的元素
	 * @return 自身
	 */
	public JSONArray fluentAdd(@NotNull Object obj) {
		super.add(obj);
		return this;
	}

	/**
	 * 添加数组所有元素并返回自身
	 *
	 * @param c 数组
	 * @return 自身
	 */
	public JSONArray fluentAddAll(@NotNull Collection<?> c) {
		super.addAll(c);
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append('[');
		for (Object token : this) {
			if (token instanceof String) {
				sb.append('"').append(StringUtil.toEscapeString((String) token)).append('"');
			} else if (token instanceof JSONArray) {
				sb.append(token);
			} else if (token instanceof List) {
				//noinspection unchecked
				sb.append(JSONArray.parseArray((List<Object>) token));
			} else if (token instanceof JSONObject) {
				sb.append(token);
			} else if (token instanceof Map) {
				//noinspection unchecked
				sb.append(JSONObject.parseObject((Map<String, Object>) token));
			} else {
				sb.append(token);
			}
			sb.append(',');
		}
		if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
		return sb.append(']').toString();
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
		StringBuilder sb = new StringBuilder().append('[');
		for (Object token : this) {
			sb.append('\n').append("    ".repeat(depth + 1));
			if (token instanceof String) {
				sb.append('"').append(StringUtil.toEscapeString((String) token)).append('"');
			} else if (token instanceof JSONArray) {
				sb.append(((JSONArray) token).toString(depth + 1));
			} else if (token instanceof List) {
				//noinspection unchecked
				sb.append(JSONArray.parseArray((List<Object>) token).toString(depth + 1));
			} else if (token instanceof JSONObject) {
				sb.append(((JSONObject) token).toString(depth + 1));
			} else if (token instanceof Map) {
				//noinspection unchecked
				sb.append(JSONObject.parseObject((Map<String, Object>) token).toString(depth + 1));
			} else {
				sb.append(token);
			}
			sb.append(',');
		}
		if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
		return sb.append('\n').append("    ".repeat(depth)).append(']').toString();
	}

}
