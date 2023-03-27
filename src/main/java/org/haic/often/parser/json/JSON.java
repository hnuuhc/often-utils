package org.haic.often.parser.json;

import org.haic.often.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * JSON数据解析接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/3/10 14:41
 */
public abstract class JSON {

	public static JSONObject of(String k, Object v) {
		return new JSONObject().fluentPut(k, v);
	}

	/**
	 * 解析并获取JSON对象
	 *
	 * @param body JSON字符串
	 * @return JSON对象
	 */
	public static JSONObject parseObject(@NotNull String body) {
		return JSONObject.parseObject(body);
	}

	/**
	 * 解析并获取JSON对象
	 *
	 * @param m Map数组
	 * @return JSON对象
	 */
	public static <K, V> JSONObject parseObject(@NotNull Map<? super K, ? super V> m) {
		return JSONObject.parseObject(m);
	}

	/**
	 * 解析并获取JSON数组
	 *
	 * @param body JSON字符串
	 * @return JSON数组
	 */
	public static JSONArray parseArray(@NotNull String body) {
		return JSONArray.parseArray(body);
	}

	/**
	 * 解析并获取JSON数组
	 *
	 * @param list 数组
	 * @return JSON数组
	 */
	public static JSONArray parseArray(@NotNull Collection<?> list) {
		return JSONArray.parseArray(list);
	}

}
