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
		return intercept(charAt());
	}

	public String intercept(char eof) {
		var sb = new StringBuilder();
		for (char c = body.charAt(++index); c != eof; c = body.charAt(++index)) sb.append(c == '\\' ? interceptChar() : c);
		return sb.toString();
	}

	public String interceptOrEof(char eof) {
		var sb = new StringBuilder();
		while (++index < length) {
			var c = body.charAt(index);
			if (c == eof) break;
			sb.append(c == '\\' ? interceptChar() : c);
		}
		return sb.toString();
	}

	private char interceptChar() {
		switch (body.charAt(++index)) {
			case 'u' -> {return (char) Integer.parseInt(body.substring(++index, (index += 3) + 1), 16);}
			case '\\' -> {return '\\';}
			case '/' -> {return '/';}
			case '\'' -> {return '\'';}
			case '"' -> {return '"';}
			case 'r' -> {return '\r';}
			case 'n' -> {return '\n';}
			case 't' -> {return '\t';}
			case '0' -> {return (char) Integer.parseInt(body.substring(++index, ++index + 1), 8);}
			case 'x' -> {return (char) Integer.parseInt(body.substring(++index, ++index + 1), 16);}
			default -> throw new JSONException("存在非法转义字符: \\" + body.charAt(index));
		}
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

	public boolean isNoOutBounds() {
		return index < length;
	}

	@Override
	public String toString() {
		return body;
	}

}
