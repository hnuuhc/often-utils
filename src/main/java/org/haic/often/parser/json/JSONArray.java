package org.haic.often.parser.json;

import org.haic.often.exception.JSONException;
import org.haic.often.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
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
			this.add(new JSONObject(body, false));
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
						this.add(new JSONObject(body, true));
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

	public static JSONArray parseArray(@NotNull String body) {
		return new JSONArray(new StringBuilder(body.strip()));
	}

	public static <T> JSONArray parseArray(@NotNull List<? super T> list) {
		return new JSONArray().fluentAddAll(list);
	}

	public Object get(int i) {
		return super.get(i);
	}

	public String getString(int i) {
		return String.valueOf(super.get(i));
	}

	public boolean getBoolean(int i) {
		return Boolean.parseBoolean(String.valueOf(super.get(i)));
	}

	public byte getByte(int i) {
		return Byte.parseByte(String.valueOf(super.get(i)));
	}

	public short getShort(int i) {
		return Short.parseShort(String.valueOf(super.get(i)));
	}

	public int getInteger(int i) {
		return Integer.parseInt(String.valueOf(super.get(i)));
	}

	public long getLong(int i) {
		return Long.parseLong(String.valueOf(super.get(i)));
	}

	public float getFloat(int i) {
		return Float.parseFloat(String.valueOf(super.get(i)));
	}

	public double getDouble(int i) {
		return Double.parseDouble(String.valueOf(super.get(i)));
	}

	public JSONObject getJSONObject(int i) {
		return (JSONObject) this.get(i);
	}

	public JSONArray getJSONArray(int i) {
		return (JSONArray) this.get(i);
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> toList(Class<T> itemClass) {
		List<T> list = new ArrayList<>();

		if (itemClass == Character.class) {
			for (var obj : this) list.add((T) Character.valueOf((char) obj));
		} else if (itemClass == Boolean.class) {
			for (var obj : this) list.add((T) obj);
		} else if (itemClass == JSONObject.class) {
			for (var obj : this) list.add((T) (obj instanceof JSONObject ? obj : JSONObject.parseObject(String.valueOf(obj))));
		} else if (itemClass == JSONArray.class) {
			for (var obj : this) list.add((T) (obj instanceof JSONArray ? obj : JSONArray.parseArray(String.valueOf(obj))));
		} else {
			Constructor<?> type = null;
			for (var con : itemClass.getConstructors()) {
				Class<?>[] parameterTypes = con.getParameterTypes();
				if (parameterTypes.length == 1 && parameterTypes[0].getName().equals("java.lang.String")) {
					type = con;
				}
			}
			if (type == null) {
				throw new JSONException("不支持的类型转换");
			}
			try {
				for (var obj : this) list.add((T) type.newInstance(obj));
			} catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
				throw new JSONException("转换类型不匹配");
			}
		}
		return list;
	}

	public <T> JSONArray fluentAdd(@NotNull T t) {
		super.add(t);
		return this;
	}

	public <T> JSONArray fluentAddAll(@NotNull Collection<T> c) {
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
