package org.haic.often.parser;

import org.haic.often.annotations.NotNull;
import org.haic.often.exception.JSONException;

/**
 * 用于存储StringBuilder和位置下标
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/15 9:27
 */
public class ParserStringBuilder {

	private final String body;
	private int index;
	private int length;

	public ParserStringBuilder(@NotNull String body) {
		this.body = body;
		this.length = body.length();
	}

	public boolean startsWith(String prefix) {
		return body.startsWith(prefix, index);
	}

	public boolean startsWith(String prefix, int index) {
		return body.startsWith(prefix, index);
	}

	public char charAt() {
		return body.charAt(index);
	}

	public char charAt(int index) {
		return body.charAt(index);
	}

	public int length() {
		return length;
	}

	public int pos() {
		return index;
	}

	public ParserStringBuilder pos(int index) {
		this.index = index;
		return this;
	}

	public ParserStringBuilder offset(int i) {
		this.index += i;
		return this;
	}

	public int indexOf(@NotNull String str) {
		return body.indexOf(str, index);
	}

	public int indexOf(@NotNull String str, int fromIndex) {
		return body.indexOf(str, fromIndex);
	}

	public int lastIndexOf(@NotNull String str) {
		return body.lastIndexOf(str, length - 1);
	}

	public int lastIndexOf(@NotNull String str, int fromIndex) {
		return body.lastIndexOf(str, fromIndex);
	}

	public String substring(int start) {
		return body.substring(start, length);
	}

	public String substring(int start, int end) {
		return body.substring(start, end);
	}

	/**
	 * 在StringBuilder中从指定(起始符号)位置开始截取字符串,自动对转义符转义,同样以起始符号为结束位,pos参数值会同步更新
	 *
	 * @return 字符串
	 */
	public String intercept() {
		char eof = body.charAt(index);
		var sb = new StringBuilder();
		while (body.charAt(++index) != eof) {
			if (body.charAt(index) == '\\') {
				switch (body.charAt(++index)) {
					case 'u' -> sb.append((char) Integer.parseInt(body.substring(++index, (index += 3) + 1), 16));
					case '\\' -> sb.append('\\');
					case '/' -> sb.append('/');
					case '\'' -> sb.append('\'');
					case '"' -> sb.append('"');
					case 'r' -> sb.append('\r');
					case 'n' -> sb.append('\n');
					case 't' -> sb.append('\t');
					case '0' -> sb.append((char) Integer.parseInt(body.substring(++index, ++index + 1), 8));
					case 'x' -> sb.append((char) Integer.parseInt(body.substring(++index, ++index + 1), 16));
					case '\r' -> throw new JSONException("存在非法换行符: \\r");
					case '\n' -> throw new JSONException("存在非法换行符: \\n");
					default -> throw new JSONException("存在非法转义字符: \\" + body.charAt(index));
				}
			} else {
				sb.append(body.charAt(index));
			}
		}
		return sb.toString();
	}

	/**
	 * 从pos参数位置开始跳过空格
	 *
	 * @return 当前pos参数值
	 */
	public ParserStringBuilder strip() {
		return stripLeading().stripTrailing();
	}

	public ParserStringBuilder stripLeading() {
		while (index < length && Character.isWhitespace(body.charAt(index))) index++; // 跳过空格
		return this;
	}

	public ParserStringBuilder stripTrailing() {
		int i = length - 1;
		while (i > 0 && Character.isWhitespace(body.charAt(i))) i--; // 跳过空格
		this.length = i + 1;
		return this;
	}

	@Override
	public String toString() {
		return body;
	}

}
