package org.haic.often.logger;

import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

/**
 * <p>日志单元</p>
 * <p>每条日志对应一个日志单元</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public final class LoggerUnit {

	/**
	 * <p>格式化符号</p>
	 */
	public static final String FORMAT_CODE = "{}";
	/**
	 * <p>格式化符号长度</p>
	 */
	public static final int FORMAT_CODE_LENGTH = FORMAT_CODE.length();

	/**
	 * <p>原始信息</p>
	 */
	private final String message;
	/**
	 * <p>格式化信息</p>
	 */
	private final String[] format;
	/**
	 * <p>后缀文本</p>
	 */
	private final String suffix;

	/**
	 * @param message 原始日志
	 */
	public LoggerUnit(String message) {
		this.message = message;
		int pos;
		int last = 0;
		int index = 0;
		String[] format = new String[Byte.SIZE];
		final int messageLength = message.length();
		while ((pos = message.indexOf(FORMAT_CODE, last)) >= 0) {
			format[index++] = message.substring(last, pos);
			last = pos + FORMAT_CODE_LENGTH;
			if (index >= format.length) {
				final String[] resizeFormat = new String[format.length + Byte.SIZE];
				System.arraycopy(format, 0, resizeFormat, 0, format.length);
				format = resizeFormat;
			}
		}
		if (last != 0 && last < messageLength) this.suffix = message.substring(last);
		else this.suffix = null;
		this.format = new String[index];
		System.arraycopy(format, 0, this.format, 0, index);
	}

	/**
	 * <p>格式化日志</p>
	 *
	 * @param objects 参数列表
	 * @return 日志信息
	 */
	public String format(Object... objects) {
		final StringBuilder builder = new StringBuilder();
		return this.format(builder, objects).toString();
	}

	/**
	 * <p>格式化日志</p>
	 *
	 * @param builder 日志Builder
	 * @param objects 参数列表
	 * @return 日志信息
	 */
	public StringBuilder format(@NotNull StringBuilder builder, Object... objects) {
		if (objects == null || objects.length == 0 || this.format.length == 0) return builder.append(this.message);
		for (int index = 0; index < this.format.length; index++) {
			builder.append(this.format[index]);
			if (index < objects.length) {
				if (objects[index] != null && objects[index].getClass().isArray()) builder.append(this.array(objects[index]));
				else builder.append(objects[index]);
			} else {
				builder.append(FORMAT_CODE);
			}
		}
		if (this.suffix != null) builder.append(this.suffix);
		return builder;
	}

	/**
	 * <p>获取异常参数</p>
	 *
	 * @param objects 参数列表
	 * @return 异常
	 */
	public Throwable throwable(Object... objects) {
		if (objects == null || objects.length == 0) return null;
		final Object object = objects[objects.length - 1];
		if (object instanceof Throwable t) return t;
		return null;
	}

	/**
	 * <p>处理数组参数</p>
	 *
	 * @param object 参数
	 * @return 字符输出
	 */
	private String array(Object object) {
		var joiner = new StringJoiner(", ", "[", "]");
		if (object instanceof boolean[] array) {
			for (boolean value : array) joiner.add(Boolean.toString(value));
		} else if (object instanceof byte[] array) {
			for (byte value : array) joiner.add(Byte.toString(value));
		} else if (object instanceof char[] array) {
			for (char value : array) joiner.add(Character.toString(value));
		} else if (object instanceof short[] array) {
			for (short value : array) joiner.add(Short.toString(value));
		} else if (object instanceof int[] array) {
			for (int value : array) joiner.add(Integer.toString(value));
		} else if (object instanceof long[] array) {
			for (long value : array) joiner.add(Long.toString(value));
		} else if (object instanceof float[] array) {
			for (float value : array) joiner.add(Float.toString(value));
		} else if (object instanceof double[] array) {
			for (double value : array) joiner.add(Double.toString(value));
		} else if (object instanceof Object[] array) {
			for (Object value : array) joiner.add(String.valueOf(value));
		} else return String.valueOf(object);
		return joiner.toString();
	}

}
