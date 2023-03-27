package org.haic.often.util;

import org.haic.often.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

/**
 * Base64编码器,用于Base64的编码和解码
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/2/15 1:05
 */
public class Base64Util {

	/**
	 * 普通字符串转Base64编码的字符串
	 *
	 * @param data 普通字符串
	 * @return base64编码格式的字符串
	 */
	@NotNull
	public static String encode(@NotNull String data) {
		return Base64.getEncoder().encodeToString(data.getBytes());
	}

	/**
	 * byte数组转Base64编码
	 *
	 * @param data byte数组
	 * @return base64编码格式数组
	 */
	public static byte[] encode(byte[] data) {
		return Base64.getEncoder().encode(data);
	}

	/**
	 * Base64编码的字符串转普通字符串
	 *
	 * @param data base64编码格式的字符串
	 * @return 转换后的字符串
	 */
	@NotNull
	public static String decode(@NotNull String data) {
		return decode(data, StandardCharsets.UTF_8);
	}

	/**
	 * Base64编码的字符串转普通字符串
	 *
	 * @param data        base64编码格式的字符串
	 * @param charsetName 需要转换的字符集编码格式
	 * @return 转换后的字符串
	 */
	@NotNull
	public static String decode(@NotNull String data, @NotNull String charsetName) {
		return decode(data, Charset.forName(charsetName));
	}

	/**
	 * Base64编码的字符串转普通字符串
	 *
	 * @param data    base64编码格式的字符串
	 * @param charset 需要转换的字符集编码格式
	 * @return 转换后的字符串
	 */
	@NotNull
	public static String decode(@NotNull String data, Charset charset) {
		return new String(Base64.getDecoder().decode(data), charset);
	}

	/**
	 * 解码Base64编码格式数组
	 *
	 * @param data base64编码格式数组
	 * @return 解码后的数组
	 */
	public static byte[] decode(byte[] data) {
		return Base64.getDecoder().decode(data);
	}

	/**
	 * 判断字符串是否为Base64编码
	 *
	 * @param str 需要判断的字符串
	 * @return 判断结果
	 */
	public static boolean isBase64(@NotNull String str) {
		return Pattern.matches("^([A-Za-z\\d+/]{4})*([A-Za-z\\d+/]{4}|[A-Za-z\\d+/]{3}=|[A-Za-z\\d+/]{2}==)$", str);
	}

}
