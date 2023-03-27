package org.haic.often.util;

import org.haic.often.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Base16编码器,用于Base16的编码和解码
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/19 19:13
 */
public class Base16Util {

	/**
	 * 将字节数组数据编码为 base16 十六进制字符串
	 *
	 * @param data 待编码的数据
	 * @return base16 十六进制字符串
	 */
	public static String encode(@NotNull String data) {
		StringBuilder sb = new StringBuilder(data.length() * 2);
		for (byte b : data.getBytes()) {
			int i = b & 0xff;
			if (i < 0x10) {
				sb.append("0");
			}
			sb.append(Integer.toHexString(i));
		}
		return sb.toString().toUpperCase();
	}

	/**
	 * 将 base16 十六进制字符串解码为字节数组
	 *
	 * @param data 待解码的数据
	 * @return 字符串数据
	 */
	@NotNull
	public static String decode(@NotNull String data) {
		return decode(data, StandardCharsets.UTF_8);
	}

	/**
	 * 将 base16 十六进制字符串解码为字节数组
	 *
	 * @param data        待解码的数据
	 * @param charsetName 需要转换的字符集编码格式名称
	 * @return 字符串数据
	 */
	@NotNull
	public static String decode(@NotNull String data, @NotNull String charsetName) {
		return decode(data, Charset.forName(charsetName));
	}

	/**
	 * 将 base16 十六进制字符串解码为字节数组
	 *
	 * @param data    待解码的数据
	 * @param charset 需要转换的字符集编码格式
	 * @return 字符串数据
	 */
	@NotNull
	public static String decode(@NotNull String data, Charset charset) {
		byte[] bts = new byte[data.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(data.substring(2 * i, 2 * i + 2), 16);
		}
		return new String(bts, charset);
	}

}
