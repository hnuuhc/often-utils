package org.haic.often.parser.yaml;

import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
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
public class YAMLFormat {

	/**
	 * 对未知类型进行格式化,并且字符串类型前后加双引号,用于文本输出
	 *
	 * @param value 值
	 * @param depth 指定深度,用于格式化
	 * @return 处理后的文本
	 */
	public static String toOutFormat(Object value, int depth) {
		if (value == null) return null;
		if (value instanceof String s) {
			return " " + StringUtil.toEscape(s);
		} else if (value instanceof StringBuilder || value instanceof StringBuffer) {
			return " " + StringUtil.toEscape(value.toString());
		} else if (value instanceof Number || value instanceof Boolean) {
			return " " + value;
		} else if (value instanceof JSONArray json) {
			return " " + json;
		} else if (value instanceof JSONObject json) {
			return " " + json;
		} else if (value instanceof YAMLArray yaml) {
			return "\n" + yaml.toString(depth + 1);
		} else if (value instanceof YAMLObject yaml) {
			return "\n" + yaml.toString(depth + 1);
		} else if (value instanceof Collection<?> c) {
			return "\n" + YAMLArray.parseArray(c).toString(depth + 1);
		} else if (value instanceof Map<?, ?> m) {
			return "\n" + YAMLObject.parseObject(m).toString(depth + 1);
		} else if (value.getClass().isArray()) {
			return "\n" + YAMLArray.parseArray(Arrays.asList((Object[]) value)).toString(depth + 1);
		} else {
			return " " + StringUtil.toEscape(String.valueOf(value));
		}
	}

}
