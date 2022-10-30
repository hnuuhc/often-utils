package org.haic.often.logger;

import org.haic.often.Symbol;
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

	/**
	 * <p>日志对象</p>
	 */
	private final Map<String, Logger> loggers;
	/**
	 * <p>日志适配器</p>
	 */
	private final List<LoggerAdapter> adapters;

	private LoggerFactory() {
		this.loggers = new ConcurrentHashMap<>();
		final String adapter = LoggerConfig.getAdapter();
		final List<LoggerAdapter> list = new ArrayList<>();
		if (adapter != null && !adapter.isEmpty()) {
			final String[] adapters = adapter.split(Symbol.COMMA);
			for (String value : adapters) {
				value = value.strip();
				if (FileLoggerAdapter.ADAPTER.equalsIgnoreCase(value)) {
					list.add(new FileLoggerAdapter());
				} else if (ConsoleLoggerAdapter.ADAPTER.equalsIgnoreCase(value)) {
					list.add(new ConsoleLoggerAdapter());
				}
			}
		}
		this.adapters = list;
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
