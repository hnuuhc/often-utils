package org.haic.often.util;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.haic.often.Judge;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NonNls;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.JSONException;
import org.haic.often.exception.StringException;
import org.haic.often.function.ByteFunction;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
public class StringUtil extends StringUtils {

	/**
	 * 将字符串的转义字符以文本方式显示
	 *
	 * @param body 待处理字符串
	 * @return 处理后的字符串
	 */
	public static String toEscape(@NotNull String body) {
		StringBuilder sb = new StringBuilder();
		char[] chars = body.toCharArray();
		for (char c : chars) {
			switch (c) {
				case '\\' -> sb.append("\\\\");
				case '"' -> sb.append("\\\"");
				case '\r' -> sb.append("\\r");
				case '\n' -> sb.append("\\n");
				default -> sb.append(c);
			}
		}
		return sb.toString();
	}

	/**
	 * 在StringBuilder中从指定位置开始截取字符串,以符号'"'结束
	 *
	 * @param body  待截取的字符串
	 * @param sb    截取后存放
	 * @param index 当前位置下标
	 * @return 字符串结束位置(符号 ' " ')下标
	 */
	public static int interceptString(@NotNull StringBuilder body, @NotNull StringBuilder sb, int index) {
		while (body.charAt(++index) != '"') {
			if (body.charAt(index) == '\\') {
				switch (body.charAt(++index)) {
					case 'u' -> sb.append((char) Integer.parseInt(body.substring(++index, (index += 3) + 1), 16));
					case '\\' -> sb.append('\\');
					case '\'' -> sb.append('\'');
					case '/' -> sb.append("/");
					case '"' -> sb.append('"');
					case 'r' -> sb.append("\\r");
					case 'n' -> sb.append("\\n");
					case '0' -> {}
					case '\r' -> throw new JSONException("存在非法换行符: \\r");
					case '\n' -> throw new JSONException("存在非法换行符: \\n");
					default -> throw new JSONException("存在非法转义字符: \\" + body.charAt(index));
				}
			} else {
				sb.append(body.charAt(index));
			}
		}
		return index;
	}

	/**
	 * 提取html标签内属性值
	 *
	 * @param tag html标签
	 * @return 列表: 属性名称 - 属性值
	 */
	@Contract(pure = true)
	public static Map<String, String> htmlAttributes(@NotNull String tag) {
		Map<String, String> attrs = new HashMap<>();
		char[] tagChars = tag.toCharArray(); // 存在标签属性i
		for (int i = 2; i < tagChars.length; i++) {
			if (tagChars[i] == ' ' && tagChars[i + 1] != ' ' && tagChars[++i] != '/' && tagChars[i] != '>') {
				StringBuilder key = new StringBuilder();
				do {
					key.append(tagChars[i++]);
					if (tagChars[i] == ' ') {
						attrs.put(key.toString(), "");
						break;
					} else if (tagChars[i] == '/' || tagChars[i] == '>') {
						attrs.put(key.toString(), "");
						return attrs;
					}
				} while (tagChars[i] != '=');
				StringBuilder value = new StringBuilder();
				if (tagChars[++i] == '"') {
					while (tagChars[++i] != '"') {
						value.append(tagChars[i]);
					}
					if (Character.isLetter(tagChars[i + 1])) {
						tagChars[i] = ' ';
						i--;
					}
				} else if (tagChars[i] == '\'') {
					while (tagChars[++i] != '\'') {
						value.append(tagChars[i]);
					}
				} else if (tagChars[i] == '&' && tagChars[i + 1] == 'q' && tagChars[i + 2] == 'u' && tagChars[i + 3] == 'o' && tagChars[i + 4] == 't' && tagChars[i + 5] == ';') {
					i = i + 6;
					do {
						value.append(tagChars[i++]);
					} while (tagChars[i] == '&' && tagChars[i + 1] == 'q' && tagChars[i + 2] == 'u' && tagChars[i + 3] == 'o' && tagChars[i + 4] == 't' && tagChars[i + 5] == ';');
					i += 5;
				} else {
					do {
						value.append(tagChars[i++]);
					} while (tagChars[i] != ' ' && tagChars[i] != '/' && tagChars[i] != '>');
					if (tagChars[i] == ' ') i--;
				}
				attrs.put(key.toString(), StringEscapeUtils.unescapeHtml4(value.toString()));
			}
		}
		return attrs;
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public static Map<String, String> toMap(@NotNull String str, @NotNull String split) {
		return lines(str, split).collect(Collectors.toMap(l -> l.substring(0, l.indexOf("=")), l -> l.substring(l.indexOf("=") + 1), (e1, e2) -> e2));
	}

	@Contract(pure = true)
	public static Stream<String> lines(@NotNull String str, @NotNull String split) {
		return Arrays.stream(str.split(split));
	}

	/**
	 * JSON格式字符串转换Map类
	 *
	 * @param str 源字符串
	 * @return Map - String String
	 */
	@Contract(pure = true)
	public static Map<String, String> jsonToMap(@NotNull String str) {
		return JSONObject.parseObject(str).toMap(String.class, String.class);
	}

	/**
	 * JSONP格式字符串转换为JSON格式字符串
	 *
	 * @param str JSONP格式字符串
	 * @return JSON格式字符串
	 */
	@Contract(pure = true)
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
	 * JSONP格式字符串转换为JSONObject
	 *
	 * @param str JSONP格式字符串
	 * @return JSONObject对象
	 */
	@Contract(pure = true)
	public static JSONObject jsonpToJSONObject(@NotNull String str) {
		return JSONObject.parseObject(jsonpToJson(str));
	}

	/**
	 * JSONP格式字符串转换为JSONArray
	 *
	 * @param str JSONP格式字符串
	 * @return JSONArray对象
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public static boolean isJson(@NotNull String str) {
		return !str.isEmpty() && (((str = str.strip()).startsWith("{") && str.endsWith("}") || str.startsWith("[") && str.endsWith("]")));
	}

	/**
	 * 生成一个随机邮箱
	 *
	 * @return 随机邮箱
	 */
	@Contract(pure = true)
	public static String randomEmail() {
		return (RandomStringUtils.randomAlphanumeric(8, 16) + (char) 64 + RandomStringUtils.randomAlphabetic(4, 8) + (char) 46 + RandomStringUtils.randomAlphabetic(2, 4)).toLowerCase();
	}

	/**
	 * 生成一个指定域名的随机邮箱
	 *
	 * @param domain 域名
	 * @return 指定域名的随机邮箱
	 */
	@Contract(pure = true)
	public static String randomEmail(@NotNull String domain) {
		domain = Judge.isEmpty(domain.indexOf(46)) ? domain.substring(1) : domain;
		int count = StringUtil.countMatches(domain, (char) 46);
		if (Judge.isEmpty(count)) {
			throw new StringException(domain + " not is domain");
		}
		String[] subdomain = domain.split("\\.");
		return (RandomStringUtils.randomAlphanumeric(8, 16) + (char) 64 + subdomain[subdomain.length - 2] + (char) 46 + subdomain[subdomain.length - 1]).toLowerCase();
	}

	/**
	 * 生成一个随机手机号
	 *
	 * @return 随机手机号
	 */
	@Contract(pure = true)
	public static String randomPhoneNumber() {
		String[] identifier = { "134", "135", "136", "137", "138", "139", "150", "151", "152", "157", "158", "159", "182", "183", "184", "187", "188", "178", "147", "172", "198", "130", "131", "132", "145", "155", "156", "166", "171", "175", "176", "185", "186", "166", "133", "149", "153", "173", "177", "180", "181", "189", "199" };
		return identifier[(int) (Math.random() * identifier.length)] + RandomStringUtils.randomNumeric(8);
	}

	/**
	 * 正则提取第一个匹配的字符串
	 *
	 * @param str   待提取的字符串
	 * @param regex 正则表达式
	 * @return 匹配的字符串
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public static String filter(@NotNull String str, @NotNull String shift) {
		return str.replaceAll("\\p{C}", "");
	}

	/**
	 * 含有unicode 的字符串转一般字符串
	 *
	 * @param unicodeStr 混有 Unicode 的字符串
	 * @return 一般字符串
	 */
	@NotNull
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public static String utf8ToGBK(@NotNull String str) {
		try {
			return new String(str.getBytes(StandardCharsets.UTF_8), "GBK");
		} catch (UnsupportedEncodingException e) {
			throw new StringException(e);
		}
	}

}
