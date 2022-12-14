package org.haic.often.parser.json;

import org.haic.often.annotations.NotNull;

/**
 * 针对JSON数据中的未知类型数字实现的包装类,内部仅存储数字字符串
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/14 15:14
 */
public class JSONNumber extends Number {

	private final String number;

	public JSONNumber(@NotNull String number) {
		this.number = number;
	}

	@Override
	public int intValue() {
		return Integer.parseInt(number);
	}

	@Override
	public long longValue() {
		return Long.parseLong(number);
	}

	@Override
	public float floatValue() {
		return Float.parseFloat(number);
	}

	@Override
	public double doubleValue() {
		return Double.parseDouble(number);
	}

	@Override
	public String toString() {
		return number;
	}

}
