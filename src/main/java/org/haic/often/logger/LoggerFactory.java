package org.haic.often.logger;

import org.haic.often.logger.adapter.ConsoleLoggerAdapter;
import org.haic.often.logger.adapter.FileLoggerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>日志工厂</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public final class LoggerFactory {

	private static final LoggerFactory INSTANCE = new LoggerFactory();

	public static LoggerFactory getInstance() {
		return INSTANCE;
	}

	private final Map<String, Logger> loggers = new ConcurrentHashMap<>();
	private final List<LoggerAdapter> adapters = new ArrayList<>();

	private LoggerFactory() {
		if (LoggerConfig.getFileEnable()) adapters.add(new FileLoggerAdapter());
		if (LoggerConfig.getConsoleEnable()) adapters.add(new ConsoleLoggerAdapter());
	}

	/**
	 * <p>获取日志</p>
	 *
	 * @param clazz class
	 * @return 日志
	 */
	public static Logger getLogger(Class<?> clazz) {
		return INSTANCE.loggers.computeIfAbsent(clazz.getName(), Logger::new);
	}

	/**
	 * <p>获取日志适配器</p>
	 *
	 * @return 日志适配器
	 */
	public static List<LoggerAdapter> getAdapters() {
		return INSTANCE.adapters;
	}

	/**
	 * <p>关闭日志</p>
	 */
	public static void shutdown() {
		INSTANCE.adapters.forEach(LoggerAdapter::release);
	}

}
