package org.haic.often.parser.json;

import org.haic.often.exception.JSONException;
import org.haic.often.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/12/6 0:30
 */
public class JSONArray extends ArrayList<Object> {

	public JSONArray() {
		super();
	}

	private <T> JSONArray(@NotNull List<? super T> body) {
		this.addAll(body);
	}

	private JSONArray(@NotNull String body) {
		this(new StringBuilder(body.strip()));
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
					case '"' -> {
						StringBuilder value = new StringBuilder();
						i = StringUtil.interceptString(body, value, i) + 1;
						this.add(value.toString());
					}
					case 'n' -> {
						if (body.charAt(++i) == 'u' && body.charAt(++i) == 'l' && body.charAt(++i) == 'l') {
							this.add(null);
						} else {
							throw new JSONException("在下标 " + (i - 3) + " 处期待值不为\"null\"");
						}
						i++;
					}
					case 't' -> {
						if (body.charAt(++i) == 'r' && body.charAt(++i) == 'u' && body.charAt(++i) == 'e') {
							this.add(true);
						} else {
							throw new JSONException("在下标 " + (i - 3) + " 处期待值不为\"true\"");
						}
						i++;
					}
					case 'f' -> {
						if (body.charAt(++i) == 'a' && body.charAt(++i) == 'l' && body.charAt(++i) == 's' && body.charAt(++i) == 'e') {
							this.add(false);
						} else {
							throw new JSONException("在下标 " + (i - 4) + " 处期待值不为\"false\"");
						}
						i++;
					}
					case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
						StringBuilder value = new StringBuilder();
						do {
							value.append(body.charAt(i++));
						} while (Character.isDigit(body.charAt(i)) || body.charAt(i) == '.');
						if (body.charAt(i) == 'e') {
							if (body.charAt(++i) == '+') {
								value.append("e");
								do {
									value.append(body.charAt(i++));
								} while (Character.isDigit(body.charAt(i)));
							} else {
								throw new JSONException("在下标 " + i + " 处期待值不为'+'");
							}
						}
						this.add(value);
					}
					case '\'' -> {
						this.add(body.charAt(++i));
						if (body.charAt(++i) != '\'') {
							throw new JSONException("在下标 " + i + " 处期待值不为\"'\"");
						}
						i++;
					}
					default -> {
						StringBuilder value = new StringBuilder();
						do {
							value.append(body.charAt(i++));
						} while (body.charAt(i) != ',' && body.charAt(i) != ' ' && body.charAt(i) != ']');
						this.add(value.toString());
					}
				}
				while (Character.isWhitespace(body.charAt(i))) i++; // 跳过空格
				if (body.charAt(i) == ']') {
					body.delete(0, i + 1);
					return;
				} else if (body.charAt(i) != ',') {
					throw new JSONException("在下标 " + i + " 处期待值不为','");
				}
			}
		} else {
			throw new JSONException("格式错误: " + body);
		}
	}

	public static JSONArray parseArray(@NotNull String body) {
		return new JSONArray(body);
	}

	public static <T> JSONArray parseArray(@NotNull List<? super T> body) {
		return new JSONArray(body);
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

	public char getChar(int i) {
		return (char) super.get(i);
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

		if (itemClass == Short.class) {
			for (var obj : this) list.add((T) Short.valueOf(String.valueOf(obj)));
		} else if (itemClass == Integer.class) {
			for (var obj : this) list.add((T) Integer.valueOf(String.valueOf(obj)));
		} else if (itemClass == Long.class) {
			for (var obj : this) list.add((T) Long.valueOf(String.valueOf(obj)));
		} else if (itemClass == Float.class) {
			for (var obj : this) list.add((T) Float.valueOf(String.valueOf(obj)));
		} else if (itemClass == Double.class) {
			for (var obj : this) list.add((T) Double.valueOf(String.valueOf(obj)));
		} else if (itemClass == Byte.class) {
			for (var obj : this) list.add((T) Byte.valueOf(String.valueOf(obj)));
		} else if (itemClass == Character.class) {
			for (var obj : this) list.add((T) Character.valueOf((char) obj));
		} else if (itemClass == String.class) {
			for (var obj : this) list.add((T) String.valueOf(obj));
		} else if (itemClass == Boolean.class) {
			for (var obj : this) list.add((T) Boolean.valueOf(String.valueOf(obj)));
		} else if (itemClass == JSONObject.class) {
			for (var obj : this) list.add((T) (obj instanceof JSONObject ? obj : JSONObject.parseObject(String.valueOf(obj))));
		} else if (itemClass == JSONArray.class) {
			for (var obj : this) list.add((T) (obj instanceof JSONArray ? obj : new JSONArray(String.valueOf(obj))));
		} else {
			throw new JSONException("不支持的类型转换");
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
				sb.append('"').append(token).append('"').append(',');
			} else {
				sb.append(token).append(',');
			}
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
				sb.append('"').append(token).append('"').append(',');
			} else {
				sb.append(token).append(',');
			}
		}
		if (sb.charAt(sb.length() - 1) == ',') sb.deleteCharAt(sb.length() - 1);
		return sb.append('\n').append("    ".repeat(depth)).append(']').toString();
	}

}
