package org.haic.often.logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>日志工具</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public final class Logger {

	/**
	 * <p>默认字符长度</p>
	 */
	private static final int DEFAULT_CAPACITY = 128;
	/**
	 * <p>时间格式</p>
	 */
	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

	/**
	 * <p>日志级别</p>
	 */
	private final int level;

	/**
	 * <p>日志上下文</p>
	 */
	private final List<LoggerAdapter> adapters;
	/**
	 * <p>日志单元</p>
	 */
	private final Map<String, LoggerUnit> tupleMap;

	/**
	 * @param name 日志名称
	 */
	public Logger(String name) {
		this.level = LoggerConfig.getLevel();
		this.adapters = LoggerFactory.getAdapters();
		this.tupleMap = new ConcurrentHashMap<>();
	}

	/**
	 * <p>日志格式化</p>
	 *
	 * @param level  级别
	 * @param format 格式
	 * @param args   参数
	 * @return 日志
	 */
	private String format(Level level, String format, Object... args) {
		LoggerUnit loggerUnit = this.tupleMap.computeIfAbsent(format, LoggerUnit::new);
		StringBuilder builder = new StringBuilder(DEFAULT_CAPACITY).append("[").append(level).append("] ").append(DATE_TIME_FORMATTER.format(LocalDateTime.now())).append(" [").append(Thread.currentThread().getName()).append("] ");
		loggerUnit.format(builder, args);
		Throwable throwable = loggerUnit.throwable(args);
		if (throwable != null) {
			StringWriter stringWriter = new StringWriter();
			throwable.printStackTrace(new PrintWriter(stringWriter));
			builder.append(stringWriter).append("\n");
		}
		return builder.toString();
	}

	/**
	 * <p>判断是否支持日志级别</p>
	 *
	 * @param level 级别
	 * @return 是否支持
	 */
	private boolean isEnabled(Level level) {
		return this.level <= level.value();
	}

	/**
	 * <p>记录日志</p>
	 *
	 * @param level  级别
	 * @param format 日志
	 * @param args   参数
	 */
	private void log(Level level, String format, Object... args) {
		if (this.isEnabled(level)) {
			String message = this.format(level, format, args);
			// 减少判断
			if (level.value() >= Level.ERROR.value()) {
				this.adapters.forEach(adapter -> adapter.errorOutput(message));
			} else {
				this.adapters.forEach(adapter -> adapter.output(message));
			}
		}
	}

	public boolean isDebugEnabled() {
		return this.isEnabled(Level.DEBUG);
	}

	public void debug(String format, Object... args) {
		this.log(Level.DEBUG, format, args);
	}

	public boolean isInfoEnabled() {
		return this.isEnabled(Level.INFO);
	}

	public void info(String format, Object... args) {
		this.log(Level.INFO, format, args);
	}

	public boolean isWarnEnabled() {
		return this.isEnabled(Level.WARN);
	}

	public void warn(String format, Object... args) {
		this.log(Level.WARN, format, args);
	}

	public boolean isErrorEnabled() {
		return this.isEnabled(Level.ERROR);
	}

	public void error(String format, Object... args) {
		this.log(Level.ERROR, format, args);
	}

}
