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

	private final StringBuilder body;
	private int index;

	public ParserStringBuilder(@NotNull String body) {
		this.body = new StringBuilder(body);
	}

	public char charAt(int index) {
		return body.charAt(index);
	}

	public int length() {
		return body.length();
	}

	public StringBuilder builderString() {
		return body;
	}

	public int pos() {
		return index;
	}

	public ParserStringBuilder pos(int index) {
		this.index = index;
		return this;
	}

	public ParserStringBuilder offset(int index) {
		this.index += index;
		return this;
	}

	public int indexOf(@NotNull String str) {
		return body.indexOf(str, index);
	}

	public int indexOf(@NotNull String str, int fromIndex) {
		return body.indexOf(str, fromIndex);
	}

	public String substring(int start) {
		return body.substring(start);
	}

	public String substring(int start, int end) {
		return body.substring(start, end);
	}

	/**
	 * 在StringBuilder中从指定(起始符号)位置开始截取字符串,自动对转义符转义,同样以起始符号为结束位,pos参数值会同步更新
	 *
	 * @return 字符串
	 */
	public String interceptString() {
		char eof = body.charAt(index);
		StringBuilder sb = new StringBuilder();
		while (body.charAt(++index) != eof) {
			if (body.charAt(index) == '\\') {
				switch (body.charAt(++index)) {
					case 'u' -> sb.append((char) Integer.parseInt(body.substring(++index, (index += 3) + 1), 16));
					case '\\' -> sb.append('\\');
					case '/' -> sb.append("/");
					case '\'' -> sb.append('\'');
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
		return sb.toString();
	}

	/**
	 * 从pos参数位置开始跳过空格
	 *
	 * @return 当前pos参数值
	 */
	public int skipWhitespace() {
		while (Character.isWhitespace(body.charAt(index))) index++; // 跳过空格
		return index;
	}

}
