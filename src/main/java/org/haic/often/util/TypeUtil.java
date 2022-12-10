package org.haic.often.util;

import org.haic.often.exception.JSONException;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/12/9 22:41
 */
public class TypeUtil {

	@SuppressWarnings("unchecked")
	public static <T> T convert(@NotNull Object obj, Class<T> itemClass) {
		if (itemClass == Short.class) {
			return (T) Short.valueOf(String.valueOf(obj));
		} else if (itemClass == Integer.class) {
			return (T) Integer.valueOf(String.valueOf(obj));
		} else if (itemClass == Long.class) {
			return (T) Long.valueOf(String.valueOf(obj));
		} else if (itemClass == Float.class) {
			return (T) Float.valueOf(String.valueOf(obj));
		} else if (itemClass == Double.class) {
			return (T) Double.valueOf(String.valueOf(obj));
		} else if (itemClass == Byte.class) {
			return (T) Byte.valueOf(String.valueOf(obj));
		} else if (itemClass == Character.class) {
			return (T) Character.valueOf((char) obj);
		} else if (itemClass == String.class) {
			return (T) String.valueOf(obj);
		} else if (itemClass == Boolean.class) {
			return (T) Boolean.valueOf(String.valueOf(obj));
		} else if (itemClass == JSONObject.class) {
			return (T) (obj instanceof JSONObject ? obj : JSONObject.parseObject(String.valueOf(obj)));
		} else if (itemClass == JSONArray.class) {
			return (T) (obj instanceof JSONArray ? obj : JSONArray.parseArray(String.valueOf(obj)));
		} else if (itemClass == File.class) {
			return (T) (new File(String.valueOf(obj)));
		} else {
			throw new JSONException("不支持的类型转换");
		}
	}

}
