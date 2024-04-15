package org.haic.often.logger;

import org.jetbrains.annotations.NotNull;

/**
 * <p>日志配置</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public final class LoggerConfig {

	private static Level DEFAULT_LEVEL = Level.DEBUG; // 日志等级
	private static boolean DEFAULT_CONSOLE_ENABLE = true; // 控制台输出
	private static boolean DEFAULT_FILE_ENABLE = true; // 文件输出
	private static String DEFAULT_FILE_FOLDER = "logs"; // 日志目录
	private static String DEFAULT_FILE_NAME = "log"; // 日志文件名
	private static String DEFAULT_ERROR_FILE_NAME = "log"; // 错误日志文件名
	private static int DEFAULT_FILE_BUFFER = 4096; // 日志缓存
	private static int DEFAULT_FILE_MAX_DAY = 30; // 日志保留天数

	private LoggerConfig() {}

	/**
	 * 设置日志等级
	 *
	 * @param level 日志等级
	 */
	public static void setLevel(@NotNull Level level) {
		DEFAULT_LEVEL = level;
	}

	/**
	 * <p>获取日志级别</p>
	 *
	 * @return 日志级别
	 */
	public static int getLevel() {
		return DEFAULT_LEVEL.value();
	}

	/**
	 * <p>设置控制台输出是否启用</p>
	 *
	 * @param enable 是否启用
	 */
	public static void setConsoleEnable(boolean enable) {
		DEFAULT_CONSOLE_ENABLE = enable;
	}

	/**
	 * <p>获取控制台输出是否启用</p>
	 *
	 * @return 是否启用
	 */
	public static boolean getConsoleEnable() {
		return DEFAULT_CONSOLE_ENABLE;
	}

	/**
	 * <p>设置是否启用文件输出</p>
	 *
	 * @param enable 是否启用
	 */
	public static void setFileEnable(boolean enable) {
		DEFAULT_FILE_ENABLE = enable;
	}

	/**
	 * <p>获取文件输出是否启用</p>
	 *
	 * @return 是否启用
	 */
	public static boolean getFileEnable() {
		return DEFAULT_FILE_ENABLE;
	}

	/**
	 * <p>设置日志文件目录</p>
	 *
	 * @param folder 日志文件目录
	 */
	public static void setFileFolder(@NotNull String folder) {
		DEFAULT_FILE_FOLDER = folder;
	}

	/**
	 * <p>获取日志文件目录</p>
	 *
	 * @return 日志文件目录
	 */
	public static String getFileFolder() {
		return DEFAULT_FILE_FOLDER;
	}

	/**
	 * <p>设置日志文件名称</p>
	 *
	 * @param name 日志文件名称
	 */
	public static void setFileName(@NotNull String name) {
		DEFAULT_FILE_NAME = name;
	}

	/**
	 * <p>获取日志文件名称</p>
	 *
	 * @return 日志文件名称
	 */
	public static String getFileName() {
		return DEFAULT_FILE_FOLDER + "/" + DEFAULT_FILE_NAME;
	}

	/**
	 * <p>设置错误日志文件名称</p>
	 *
	 * @param name 日志文件名称
	 */
	public static void setErrorFileName(@NotNull String name) {
		DEFAULT_ERROR_FILE_NAME = name;
	}

	/**
	 * <p>获取错误日志文件名称</p>
	 *
	 * @return 日志文件名称
	 */
	public static String getErrorFileName() {
		return DEFAULT_FILE_FOLDER + "/" + DEFAULT_ERROR_FILE_NAME;
	}

	/**
	 * <p>设置文件日志缓存（byte）</p>
	 *
	 * @param bufferSize 文件日志缓存（byte）
	 */
	public static void setFileBuffer(int bufferSize) {
		DEFAULT_FILE_BUFFER = bufferSize;
	}

	/**
	 * <p>获取文件日志缓存（byte）</p>
	 *
	 * @return 文件日志缓存（byte）
	 */
	public static int getFileBuffer() {
		return DEFAULT_FILE_BUFFER;
	}

	/**
	 * <p>设置文件日志最大备份时间（天）</p>
	 *
	 * @param day 文件日志最大备份时间（天）
	 */
	public static void setFileMaxDay(int day) {
		DEFAULT_FILE_MAX_DAY = day;
	}

	/**
	 * <p>获取文件日志最大备份时间（天）</p>
	 *
	 * @return 文件日志最大备份时间（天）
	 */
	public static int getFileMaxDay() {
		return DEFAULT_FILE_MAX_DAY;
	}

}
