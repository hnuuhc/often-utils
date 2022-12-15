package org.haic.often.parser.json;

import org.haic.often.annotations.NotNull;

/**
 * 用于存储StringBuilder和位置下标
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/15 9:27
 */
public class JSONBuilder {

	private final StringBuilder body;
	private int index;

	public JSONBuilder(@NotNull String body) {
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

	public JSONBuilder pos(int index) {
		this.index = index;
		return this;
	}

}
