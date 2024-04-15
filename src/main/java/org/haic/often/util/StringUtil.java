package org.haic.often.util;

import org.haic.often.exception.StringException;
import org.haic.often.function.ByteFunction;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 字符串常用工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/27 15:12
 */
public class StringUtil {

	/**
	 * 提取指定两个字符串之间的文本
	 *
	 * @param str   字符串
	 * @param start 起始位置字符串
	 * @param end   结束位置字符串
	 * @return 提取结果, 可能为null
	 */
	public static String between(String str, String start, String end) {
		var extract = extract(str, start + ".*" + end);
		return extract == null ? null : extract.substring(start.length(), extract.length() - end.length());
	}

	/**
	 * 判断字符串是否为整形数字
	 *
	 * @param str 字符串
	 * @return 判断结果
	 */
	public static boolean isDigit(String str) {
		if (null == str || str.length() == 0) return false;

		for (int i = str.length(); --i >= 0; ) {
			int c = str.charAt(i);
			if (c < 48 || c > 57) {
				return false;
			}
		}

		return true;
	}

	/**
	 * 将字符串转换为md5值
	 *
	 * @param text 字符串
	 * @return md5值
	 */
	public static String getMd5(String text) {
		var builder = new StringBuilder();
		try {
			var md5 = MessageDigest.getInstance("MD5");
			var bytes = md5.digest(text.getBytes());
			for (byte b : bytes) {
				builder.append(Integer.toHexString((0x000000FF & b) | 0xFFFFFF00).substring(6));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public static String getSha1(String text) {
		var builder = new StringBuilder();
		try {
			var sha1 = MessageDigest.getInstance("SHA1");
			var digests = sha1.digest(text.getBytes());
			for (byte digest : digests) {
				int halfbyte = (digest >>> 4) & 0x0F;
				for (int i = 0; i < 2; i++) {
					builder.append(halfbyte <= 9 ? (char) ('0' + halfbyte) : (char) ('a' + halfbyte - 10));
					halfbyte = digest & 0x0F;
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	/**
	 * 字符串转换unicode
	 *
	 * @param str 一般字符串
	 * @return Unicode字符串
	 */
	@NotNull
	public static String chineseToUnicode(@NotNull String str) {
		var result = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (isChinese(c)) {
				result.append("\\u").append(Integer.toHexString(c)); // 转换为unicode
			} else {
				result.append(c);
			}
		}
		return String.valueOf(result);
	}

	/**
	 * 判断字符是否为中文
	 *
	 * @param c 待判断的字符
	 * @return 判断结果
	 */
	public static boolean isChinese(char c) {
		return Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN;
	}

	/**
	 * 判断字符串是否存在中文
	 *
	 * @param s 待判断的字符串
	 * @return 判断结果
	 */
	public static boolean isChinese(String s) {
		for (int i = 0; i < s.length(); i++) if (isChinese(s.charAt(i))) return true;
		return false;
	}

	/**
	 * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
	 *
	 * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * <pre>
	 * StringUtils.isBlank(null)      = true
	 * StringUtils.isBlank("")        = true
	 * StringUtils.isBlank(" ")       = true
	 * StringUtils.isBlank("bob")     = false
	 * StringUtils.isBlank("  bob  ") = false
	 * </pre>
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is null, empty or whitespace only
	 * @since 2.0
	 * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
	 */
	public static boolean isBlank(final CharSequence cs) {
		int strLen = cs.length();
		if (strLen == 0) return true;
		for (int i = 0; i < strLen; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) return false;
		}
		return true;
	}

	/**
	 * 将字符串的转义字符以文本方式显示
	 *
	 * @param body 待处理字符串
	 * @return 处理后的字符串
	 */
	public static String toEscape(@NotNull String body) {
		var sb = new StringBuilder();
		char[] chars = body.toCharArray();
		for (char c : chars) {
			switch (c) {
				case '\\' -> sb.append("\\\\");
				case '"' -> sb.append("\\\"");
				case '\t' -> sb.append("\\t");
				case '\r' -> sb.append("\\r");
				case '\n' -> sb.append("\\n");
				default -> sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 字符串转输入流
	 *
	 * @param s 字符串
	 * @return 输入流
	 */
	public static ByteArrayInputStream toStream(@NotNull String s) {
		return new ByteArrayInputStream(s.getBytes());
	}

	/**
	 * 搜索bytes数组中的指定字符位置
	 *
	 * @param bytes bytes数组
	 * @param b     byte
	 * @return 位置
	 */
	public static int search(byte[] bytes, byte b) {
		int index = 0;
		while (index < bytes.length) {
			if (bytes[index] == b) {
				break;
			}
			index++;
		}
		return index;
	}

	/**
	 * 搜索数组中的指定类型信息位置
	 *
	 * @param arrays 数组
	 * @param a      信息
	 * @return 位置
	 */
	public static <T> int search(T[] arrays, T a) {
		int index = 0;
		while (index < arrays.length) {
			if (arrays[index] == a) {
				break;
			}
			index++;
		}
		return index;
	}

	/**
	 * 判断输入数据是否为UTF-8编码
	 *
	 * @param bytes 待判断的数据
	 * @return 判断结果
	 */
	public static boolean isUTF8(byte[] bytes) {
		ByteFunction<Integer> unsignedInt = data -> data & 0xff;
		boolean isUTF8 = true;
		boolean isASCII = true;
		for (int i = 0; i < bytes.length; i++) {
			int value = unsignedInt.apply(bytes[i]);
			if (value < 0x80) {    // (10000000): 值小于 0x80 的为 ASCII 字符
				if (i >= bytes.length - 1) {
					if (isASCII) { // 假设纯 ASCII 字符不是 UTF 格式
						isUTF8 = false;
					}
					break;
				}
			} else if (value < 0xC0) { // (11000000): 值介于 0x80 与 0xC0 之间的为无效 UTF-8 字符
				isUTF8 = false;
				break;
			} else if (value < 0xE0) { // (11100000): 此范围内为 2 字节 UTF-8 字符
				isASCII = false;
				if (i >= bytes.length - 1) {
					break;
				}
				int value1 = unsignedInt.apply(bytes[i + 1]);
				if ((value1 & (0xC0)) != 0x80) {
					isUTF8 = false;
					break;
				}
				i++;
			} else if (value < 0xF0) { // (11110000): 此范围内为 3 字节 UTF-8 字符
				isASCII = false;
				if (i >= bytes.length - 2) {
					break;
				}
				int value1 = unsignedInt.apply(bytes[i + 1]);
				int value2 = unsignedInt.apply(bytes[i + 2]);
				if ((value1 & (0xC0)) != 0x80 || (value2 & (0xC0)) != 0x80) {
					isUTF8 = false;
					break;
				}
				i += 2;
			} else if (value < 0xF8) { // (11111000): 此范围内为 4 字节 UTF-8 字符
				isASCII = false;
				if (i >= bytes.length - 3) {
					break;
				}
				int value1 = unsignedInt.apply(bytes[i + 1]);
				int value2 = unsignedInt.apply(bytes[i + 2]);
				int value3 = unsignedInt.apply(bytes[i + 3]);
				if ((value1 & (0xC0)) != 0x80 || (value2 & (0xC0)) != 0x80 || (value3 & (0xC0)) != 0x80) {
					isUTF8 = false;
					break;
				}
				i += 2;
			} else {
				isUTF8 = false;
				break;
			}
		}
		return isUTF8;
	}

	/**
	 * 替换最后一个匹配的字符串
	 *
	 * @param str         源字符串
	 * @param regex       待匹配的字符串
	 * @param replacement 替换的字符串
	 * @return 替换后的字符串
	 */
	public static String replaceLast(@NotNull String str, @NotNull @NonNls String regex, @NotNull String replacement) {
		return str.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
	}

	/**
	 * Map格式字符串转换Map类<br/>
	 * 字符串应不包含指定的分隔符,否则键或值内存在会导致严重错误
	 *
	 * @param str   源字符串
	 * @param split 分隔符
	 * @return Map - name, value
	 */
	public static Map<String, String> toMap(@NotNull String str, @NotNull String split) {
		return lines(str, split).map(String::strip).collect(Collectors.toMap(l -> l.substring(0, l.indexOf("=")), l -> l.substring(l.indexOf("=") + 1), (e1, e2) -> e2));
	}

	/**
	 * 将字符串按指定分隔符分割
	 *
	 * @param str   字符串
	 * @param split 分隔符
	 * @return 流
	 */
	public static Stream<String> lines(@NotNull String str, @NotNull String split) {
		return Arrays.stream(str.split(split));
	}

	/**
	 * JSON格式字符串转换Map类
	 *
	 * @param str 源字符串
	 * @return Map - String String
	 */
	public static Map<String, String> jsonToMap(@NotNull String str) {
		return JSONObject.parseObject(str).toMap(String.class, String.class);
	}

	/**
	 * JSONP格式字符串转换为JSON格式字符串
	 *
	 * @param str JSONP格式字符串
	 * @return JSON格式字符串
	 */
	public static String jsonpToJson(@NotNull String str) {
		if ((str = stripEnd(str.strip(), ";")).endsWith(")")) {
			return str.substring(str.indexOf('(') + 1, str.length() - 1);
		} else if (str.endsWith("}")) {
			return str.substring(str.indexOf("{"));
		} else if (str.endsWith("]")) {
			return str.substring(str.indexOf("["));
		} else {
			throw new StringException("非JSONP格式");
		}
	}

	/**
	 * <p>Strips any of a set of characters from the start and end of a String.
	 * This is similar to {@link String#trim()} but allows the characters
	 * to be stripped to be controlled.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * @param str        the String to remove characters from, may be null
	 * @param stripChars the characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String strip(String str, final String stripChars) {
		str = stripStart(str, stripChars);
		return stripEnd(str, stripChars);
	}

	/**
	 * <p>Strips any of a set of characters from the start of a String.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <p>If the stripChars String is {@code null}, whitespace is
	 * stripped as defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * @param str        the String to remove characters from, may be null
	 * @param stripChars the characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String stripStart(final String str, final String stripChars) {
		final int strLen = str.length();
		if (strLen == 0) {
			return str;
		}
		int start = 0;
		if (stripChars == null) {
			while (start != strLen && Character.isWhitespace(str.charAt(start))) {
				start++;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (start != strLen && stripChars.indexOf(str.charAt(start)) != -1) {
				start++;
			}
		}
		return str.substring(start);
	}

	/**
	 * <p>Strips any of a set of characters from the end of a String.</p>
	 *
	 * <p>A {@code null} input String returns {@code null}.
	 * An empty string ("") input returns the empty string.</p>
	 *
	 * <p>If the stripChars String is {@code null}, whitespace is
	 * stripped as defined by {@link Character#isWhitespace(char)}.</p>
	 *
	 * @param str        the String to remove characters from, may be null
	 * @param stripChars the set of characters to remove, null treated as whitespace
	 * @return the stripped String, {@code null} if null String input
	 */
	public static String stripEnd(final String str, final String stripChars) {
		int end = str.length();
		if (end == 0) {
			return str;
		}

		if (stripChars == null) {
			while (end != 0 && Character.isWhitespace(str.charAt(end - 1))) {
				end--;
			}
		} else if (stripChars.isEmpty()) {
			return str;
		} else {
			while (end != 0 && stripChars.indexOf(str.charAt(end - 1)) != -1) {
				end--;
			}
		}
		return str.substring(0, end);
	}

	/**
	 * JSONP格式字符串转换为JSONObject
	 *
	 * @param str JSONP格式字符串
	 * @return JSONObject对象
	 */
	public static JSONObject jsonpToJSONObject(@NotNull String str) {
		return JSONObject.parseObject(jsonpToJson(str));
	}

	/**
	 * JSONP格式字符串转换为JSONArray
	 *
	 * @param str JSONP格式字符串
	 * @return JSONArray对象
	 */
	public static JSONArray jsonpToJSONArray(@NotNull String str) {
		return JSONArray.parseArray(jsonpToJson(str));
	}

	/**
	 * Map格式字符串转换Json格式<br/>
	 *
	 * @param str   源字符串
	 * @param split 分隔符
	 * @return Json String
	 */
	public static String mapToJson(@NotNull String str, @NotNull String split) {
		return JSONObject.parseObject(toMap(str, split)).toString();
	}

	/**
	 * 判断字符串是否是JSON格式
	 * <p>
	 * 注意此方法为简单判断,并不能判定绝对JSON格式
	 *
	 * @param str 字符串
	 * @return 判断后结果
	 */
	public static boolean isJson(@NotNull String str) {
		return !str.isEmpty() && (((str = str.strip()).startsWith("{") && str.endsWith("}") || str.startsWith("[") && str.endsWith("]")));
	}

	/**
	 * 生成一个随机邮箱
	 *
	 * @return 随机邮箱
	 */
	public static String randomEmail() {
		return (RandomUtil.randomAlphanumeric(8, 16) + (char) 64 + RandomUtil.randomAlphabetic(4, 8) + (char) 46 + RandomUtil.randomAlphabetic(2, 4)).toLowerCase();
	}

	/**
	 * 生成一个指定域名的随机邮箱
	 *
	 * @param domain 域名
	 * @return 指定域名的随机邮箱
	 */
	public static String randomEmail(@NotNull String domain) {
		domain = domain.indexOf(46) == 0 ? domain.substring(1) : domain;
		int count = countMatches(domain, (char) 46);
		if (count == 0) {
			throw new StringException(domain + " not is domain");
		}
		String[] subdomain = domain.split("\\.");
		return (RandomUtil.randomAlphanumeric(8, 16) + (char) 64 + subdomain[subdomain.length - 2] + (char) 46 + subdomain[subdomain.length - 1]).toLowerCase();
	}

	/**
	 * Counts how many times the char appears in the given string.
	 * A null or empty ("") String input returns 0.
	 *
	 * @param str the CharSequence to check, may be null
	 * @param ch  the char to count
	 * @return the number of occurrences, 0 if the CharSequence is null
	 */
	public static int countMatches(final CharSequence str, final char ch) {
		if (str.isEmpty()) {
			return 0;
		}
		int count = 0;
		// We could also call str.toCharArray() for faster look ups but that would generate more garbage.
		for (int i = 0; i < str.length(); i++) {
			if (ch == str.charAt(i)) {
				count++;
			}
		}
		return count;
	}

	/**
	 * 生成一个随机手机号
	 *
	 * @return 随机手机号
	 */
	public static String randomPhoneNumber() {
		String[] identifier = { "134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159", "182", "183", "184", "187", "188", "178", "147", "172", "198", "130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166", "133", "149", "153", "173", "177", "180", "181", "189", "199" };
		return identifier[(int) (Math.random() * identifier.length)] + RandomUtil.randomNumeric(8);
	}

	/**
	 * 正则提取第一个匹配的字符串
	 *
	 * @param str   待提取的字符串
	 * @param regex 正则表达式
	 * @return 匹配的字符串
	 */
	public static String extract(@NotNull String str, @NotNull @NonNls String regex) {
		String result = null;
		Matcher matcher = Pattern.compile(regex).matcher(str);
		if (matcher.find()) {
			result = matcher.group();
		}
		return result;
	}

	/**
	 * 正则提取所有匹配的字符串
	 *
	 * @param str   待提取的字符串
	 * @param regex 正则表达式
	 * @return 匹配的字符串列表
	 */
	public static List<String> extractList(@NotNull String str, @NotNull @NonNls String regex) {
		List<String> result = new ArrayList<>();
		Matcher matcher = Pattern.compile(regex).matcher(str);
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}

	/**
	 * 正则提取最后一个匹配的字符串
	 *
	 * @param str   待提取的字符串
	 * @param regex 正则表达式
	 * @return 匹配的字符串
	 */
	public static String extractLast(@NotNull String str, @NotNull @NonNls String regex) {
		String result = null;
		Matcher matcher = Pattern.compile(regex).matcher(str);
		while (matcher.find()) {
			result = matcher.group();
		}
		return result;
	}

	/**
	 * 删除字符串不可见字符
	 *
	 * @param str 字符串
	 * @return 处理后的字符串
	 */
	@NotNull
	public static String filter(@NotNull String str) {
		return filter(str, "");
	}

	/**
	 * 替换字符串不可见字符
	 *
	 * @param str   字符串
	 * @param shift 替换字符
	 * @return 处理后的字符串
	 */
	@NotNull
	public static String filter(@NotNull String str, @NotNull String shift) {
		return str.replaceAll("\\p{C}", shift);
	}

	/**
	 * 含有unicode 的字符串转一般字符串
	 *
	 * @param unicodeStr 混有 Unicode 的字符串
	 * @return 一般字符串
	 */
	@NotNull
	public static String unicodeStrToString(@NotNull String unicodeStr) {
		final String regex = "\\\\u[a-f\\dA-F]{1,4}";
		int count = 0;
		Matcher matcher = Pattern.compile(regex).matcher(unicodeStr);
		StringBuilder result = new StringBuilder();
		while (matcher.find()) {
			String oldChar = matcher.group();// 原本的Unicode字符
			String newChar = unicodeToString(oldChar);// 转换为普通字符
			// 在遇见重复出现的unicode代码的时候会造成从源字符串获取非unicode编码字符的时候截取索引越界等
			int index = matcher.start();
			result.append(unicodeStr, count, index);// 添加前面不是unicode的字符
			result.append(newChar);// 添加转换后的字符
			count = index + oldChar.length();// 统计下标移动的位置
		}
		result.append(unicodeStr, count, unicodeStr.length());// 添加末尾不是Unicode的字符
		return String.valueOf(result);
	}

	/**
	 * 字符串转换unicode
	 *
	 * @param str 一般字符串
	 * @return Unicode字符串
	 */
	@NotNull
	public static String stringToUnicode(@NotNull String str) {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			result.append("\\u").append(Integer.toHexString(str.charAt(i))); // 转换为unicode
		}
		return String.valueOf(result);
	}

	/**
	 * unicode 转字符串
	 *
	 * @param unicode 全为 Unicode 的字符串
	 * @return 一般字符串
	 */
	@NotNull
	public static String unicodeToString(@NotNull String unicode) {
		StringBuilder result = new StringBuilder();
		String[] hex = unicode.split("\\\\u");
		for (int i = 1; i < hex.length; i++) {
			result.append((char) Integer.parseInt(hex[i], 16)); // 转换出每一个代码点
		}
		return String.valueOf(result);
	}

	/**
	 * gbk编码格式字符串转换为utf8编码格式字符串
	 *
	 * @param str gbk编码格式字符串
	 * @return utf8编码格式字符串
	 */
	@NotNull
	public static String utf8ByGBK(@NotNull String str) {
		try {
			return new String(str.getBytes("GBK"), StandardCharsets.UTF_8);
		} catch (UnsupportedEncodingException e) {
			throw new StringException(e);
		}
	}

	/**
	 * utf8编码格式字符串转换为gbk编码格式字符串
	 *
	 * @param str utf8编码格式字符串
	 * @return gbk编码格式字符串
	 */
	@NotNull
	public static String utf8ToGBK(@NotNull String str) {
		try {
			return new String(str.getBytes(StandardCharsets.UTF_8), "GBK");
		} catch (UnsupportedEncodingException e) {
			throw new StringException(e);
		}
	}

}
