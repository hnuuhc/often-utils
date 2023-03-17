package org.haic.often.parser.json;

import org.haic.often.util.StringUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * 用于对不同类型处理
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/14 15:34
 */
public class JSONFormat {

	/**
	 * 对未知类型进行格式化
	 *
	 * @param value 值
	 * @return 处理后的类型
	 */
	public static Object format(Object value) {
		if (value == null) return null;
		if (value instanceof String s) {
			if (s.equals("null")) return null;
			return StringUtil.toEscape(s);
		} else if (value instanceof StringBuilder || value instanceof StringBuffer) {
			return format(value.toString());
		} else if (value instanceof JSONArray || value instanceof JSONObject || value instanceof Number || value instanceof Boolean) {
			return value;
		} else if (value instanceof Collection<?> c) {
			return JSONArray.parseArray(c);
		} else if (value instanceof Map<?, ?> m) {
			return JSONObject.parseObject(m);
		} else if (value.getClass().isArray()) {
			return JSONArray.parseArray(Arrays.asList((Object[]) value));
		} else {
			return StringUtil.toEscape(String.valueOf(value));
		}
	}

	/**
	 * 对未知类型进行格式化,并且字符串类型前后加双引号,用于网络传输
	 *
	 * @param value 值
	 * @return 处理后的文本
	 */
	public static String toNetOutFormat(Object value) {
		if (value == null) return "null";
		if (value instanceof String s) {
			if (s.equals("null")) return "null";
			return '"' + StringUtil.chineseToUnicode(StringUtil.toEscape(s)) + '"';
		} else if (value instanceof StringBuilder || value instanceof StringBuffer) {
			return StringUtil.chineseToUnicode(toOutFormat(value.toString()));
		} else if (value instanceof JSONArray json) {
			return json.toJSONString();
		} else if (value instanceof JSONObject json) {
			return json.toJSONString();
		} else if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		} else if (value instanceof Collection<?> c) {
			return JSONArray.parseArray(c).toJSONString();
		} else if (value instanceof Map<?, ?> m) {
			return JSONObject.parseObject(m).toJSONString();
		} else if (value.getClass().isArray()) {
			return JSONArray.parseArray(Arrays.asList((Object[]) value)).toJSONString();
		} else {
			return '"' + StringUtil.chineseToUnicode(StringUtil.toEscape(String.valueOf(value))) + '"';
		}
	}

	/**
	 * 对未知类型进行格式化,并且字符串类型前后加双引号,用于文本输出
	 *
	 * @param value 值
	 * @return 处理后的文本
	 */
	public static String toOutFormat(Object value) {
		if (value == null) return "null";
		if (value instanceof String s) {
			if (s.equals("null")) return "null";
			return '"' + StringUtil.toEscape(s) + '"';
		} else if (value instanceof StringBuilder || value instanceof StringBuffer) {
			return toOutFormat(value.toString());
		} else if (value instanceof JSONArray || value instanceof JSONObject || value instanceof Number || value instanceof Boolean) {
			return value.toString();
		} else if (value instanceof Collection<?> c) {
			return JSONArray.parseArray(c).toString();
		} else if (value instanceof Map<?, ?> m) {
			return JSONObject.parseObject(m).toString();
		} else if (value.getClass().isArray()) {
			return JSONArray.parseArray(Arrays.asList((Object[]) value)).toString();
		} else {
			return '"' + StringUtil.toEscape(String.valueOf(value)) + '"';
		}
	}

	/**
	 * 对未知类型进行格式化,并且字符串类型前后加双引号,用于文本输出
	 *
	 * @param value 值
	 * @param depth 指定深度,用于格式化
	 * @return 处理后的文本
	 */
	public static String toOutFormat(Object value, int depth) {
		if (value == null) return "null";
		if (value instanceof String s) {
			if (s.equals("null")) return "null";
			return '"' + StringUtil.toEscape(s) + '"';
		} else if (value instanceof StringBuilder || value instanceof StringBuffer) {
			return toOutFormat(value.toString(), depth);
		} else if (value instanceof Number || value instanceof Boolean) {
			return value.toString();
		} else if (value instanceof JSONArray) {
			return ((JSONArray) value).toString(depth + 1);
		} else if (value instanceof JSONObject) {
			return ((JSONObject) value).toString(depth + 1);
		} else if (value instanceof Collection<?> c) {
			return JSONArray.parseArray(c).toString(depth + 1);
		} else if (value instanceof Map<?, ?> m) {
			return JSONObject.parseObject(m).toString(depth + 1);
		} else if (value.getClass().isArray()) {
			return JSONArray.parseArray(Arrays.asList((Object[]) value)).toString(depth + 1);
		} else {
			return '"' + StringUtil.toEscape(String.valueOf(value)) + '"';
		}
	}

}
