package org.haic.often.util;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Base32编码器,用于Base32的编码和解码
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/19 19:13
 */
public class Base32Util {

	/**
	 * 将字节数组数据编码为 base32 字符串
	 *
	 * @param data 待编码的数据
	 * @return base32 编码字符串
	 */
	public static String encode(@NotNull String data) {
		Function<Integer, Byte> idx = c -> c < 26 ? (byte) (c + 'A') : (byte) (c - 26 + '2');
		byte[] bts = data.getBytes(), ans = new byte[(bts.length + 4) / 5 * 8];
		int i = 0, num = 0, pos = -1, in = 0;
		while (i < bts.length) {
			int val = 0, cnt = 5;
			while (cnt-- > 0) {
				if (pos == -1) {
					pos = 7;
					num = bts[i++];
				}
				val |= (num >> pos-- & 1) << cnt;
			}
			ans[in++] = idx.apply(val);
		}
		while (~pos != 0) {
			int val = 0, cnt = 5;
			while (cnt-- > 0 && ~pos != 0) val |= (num >> pos-- & 1) << cnt;
			ans[in++] = idx.apply(val);
		}
		return new String(ans, 0, in);
	}

	/**
	 * 将 base32 字符串解码为字节数组
	 *
	 * @param data 待解码的数据
	 * @return 字符串数据
	 */
	@NotNull
	public static String decode(@NotNull String data) {
		return decode(data, StandardCharsets.UTF_8);
	}

	/**
	 * 将 base32 字符串解码为字节数组
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
	 * 将 base32 字符串解码为字节数组
	 *
	 * @param data    待解码的数据
	 * @param charset 需要转换的字符集编码格式
	 * @return 字符串数据
	 */
	@NotNull
	public static String decode(@NotNull String data, Charset charset) {
		Function<Byte, Integer> idx = c -> Character.isLetter(c) ? c - 'A' : c - '2' + 26;
		byte[] bts = data.getBytes(), ans = new byte[bts.length * 5 / 8];
		int i = 0, cnt = 0, num = 0, pos = -1, in = 0;
		while (i < bts.length) {
			cnt = 8;
			int val = 0;
			while (i < bts.length && cnt-- > 0) {
				if (pos == -1) {
					pos = 4;
					num = idx.apply(bts[i++]);
				}
				val |= (num >> pos-- & 1) << cnt;
			}
			ans[in++] = (byte) val;
		}
		if (cnt <= pos + 1) {
			while (cnt-- > 0) {
				ans[ans.length - 1] |= (num >> pos-- & 1) << cnt;
			}
		} else {
			ans = Arrays.copyOf(ans, ans.length - 1);
		}
		return new String(ans, charset);
	}

}
