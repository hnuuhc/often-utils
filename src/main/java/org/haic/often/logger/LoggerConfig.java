package org.haic.often.logger;

import org.haic.often.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Properties;

/**
 * <p>日志配置</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public final class LoggerConfig {

	public static final String DEFAULT_CONFIG = """
												logger.level=DEBUG
												logger.adapter=file,console
												logger.file=logs/log.log
												logger.file.buffer=8192
												logger.file.max.day=30
												""";

	private static final LoggerConfig INSTANCE = new LoggerConfig();

	public static LoggerConfig getInstance() {
		return INSTANCE;
	}

	/**
	 * <p>配置文件：{@value}</p>
	 */
	private static final String LOGGER_CONFIG = "/logger.properties";

	static {
		INSTANCE.init();
	}

	private LoggerConfig() {
	}

	/**
	 * <p>日志级别</p>
	 */
	private int level;
	/**
	 * <p>日志适配</p>
	 */
	private String adapter;
	/**
	 * <p>文件日志名称</p>
	 */
	private String fileName;
	/**
	 * <p>文件日志缓存（byte）</p>
	 */
	private int fileBuffer;
	/**
	 * <p>文件日志最大备份时间（天）</p>
	 */
	private int fileMaxDay;

	/**
	 * <p>初始化配置</p>
	 */
	private void init() {
		final Properties properties = new Properties();
		File LOGGER_FILE = new File(LOGGER_CONFIG);
		if (LOGGER_FILE.exists()) {
			try (final InputStream input = LOGGER_FILE.exists() ? Objects.requireNonNull(LoggerConfig.class.getResourceAsStream(LOGGER_CONFIG)) : StringUtil.toStream(DEFAULT_CONFIG)) {
				properties.load(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try (final var input = StringUtil.toStream(DEFAULT_CONFIG)) {
				properties.load(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		this.level = Level.of(properties.getProperty("logger.level")).value();
		this.adapter = properties.getProperty("logger.adapter");
		this.fileName = properties.getProperty("logger.file");
		this.fileName = fileName.endsWith(".log") ? fileName.substring(0, fileName.length() - 4) : fileName;
		this.fileBuffer = Integer.parseInt(properties.getProperty("logger.file.buffer", "8192"));
		this.fileMaxDay = Integer.parseInt(properties.getProperty("logger.file.max.day", "30"));
	}

	/**
	 * <p>关闭日志</p>
	 */
	public static void off() {
		INSTANCE.level = Level.OFF.value();
	}

	/**
	 * <p>获取日志级别</p>
	 *
	 * @return 日志级别
	 */
	public static int getLevel() {
		return INSTANCE.level;
	}

	/**
	 * <p>获取日志适配</p>
	 *
	 * @return 日志适配
	 */
	public static String getAdapter() {
		return INSTANCE.adapter;
	}

	/**
	 * <p>获取文件日志名称</p>
	 *
	 * @return 文件日志名称
	 */
	public static String getFileName() {
		return INSTANCE.fileName;
	}

	/**
	 * <p>获取文件日志缓存（byte）</p>
	 *
	 * @return 文件日志缓存（byte）
	 */
	public static int getFileBuffer() {
		return INSTANCE.fileBuffer;
	}

	/**
	 * <p>获取文件日志最大备份时间（天）</p>
	 *
	 * @return 文件日志最大备份时间（天）
	 */
	public static int getFileMaxDay() {
		return INSTANCE.fileMaxDay;
	}

}
