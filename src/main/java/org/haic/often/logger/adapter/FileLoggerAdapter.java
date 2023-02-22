package org.haic.often.logger.adapter;

import org.haic.often.logger.LoggerAdapter;
import org.haic.often.logger.LoggerConfig;
import org.haic.often.util.FileUtil;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

/**
 * <p>文件适配器</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public final class FileLoggerAdapter extends LoggerAdapter {

	private static final SimpleDateFormat FILE_SUFFIX_FORMAT = new SimpleDateFormat(".yyyy-MM-dd");

	public FileLoggerAdapter() {
		this.deleteFile();
		this.output = buildOutput();
		this.errorOutput = LoggerConfig.getFileName().equals(LoggerConfig.getErrorFileName()) ? output : buildErrorOutput();
	}

	@Override
	public void release() {
		super.release();
		this.deleteFile();
	}

	/**
	 * <p>新建日志输出流</p>
	 *
	 * @return 日志输出流
	 */
	private PrintStream buildOutput() {
		try {
			return new PrintStream(new BufferedOutputStream(new FileOutputStream(buildFile(), true), LoggerConfig.getFileBuffer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * <p>新建错误日志输出流</p>
	 *
	 * @return 错误日志输出流
	 */
	private PrintStream buildErrorOutput() {
		try {
			return new PrintStream(new BufferedOutputStream(new FileOutputStream(buildErrorFile(), true), LoggerConfig.getFileBuffer()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * <p>新建日志文件</p>
	 *
	 * @return 日志文件
	 */
	private File buildFile() {
		var file = new File(LoggerConfig.getFileName() + FILE_SUFFIX_FORMAT.format(new Date()) + ".log");
		var parent = file.getParentFile(); // 创建上级目录
		if (parent != null) FileUtil.createFolder(parent);
		return file;
	}

	/**
	 * <p>新建错误日志文件</p>
	 *
	 * @return 日志文件
	 */
	private File buildErrorFile() {
		var file = new File(LoggerConfig.getErrorFileName() + FILE_SUFFIX_FORMAT.format(new Date()) + ".log");
		var parent = file.getParentFile(); // 创建上级目录
		if (parent != null) FileUtil.createFolder(parent);
		return file;
	}

	/**
	 * <p>判断文件是否能够删除</p>
	 *
	 * @param logFile 日志文件
	 * @param time    当前时间
	 * @param maxDay  最大备份数量
	 * @return 是否可以删除
	 */
	private boolean deleteable(File logFile, LocalDateTime time, int maxDay) {
		return Duration.between(LocalDateTime.ofInstant(Instant.ofEpochMilli(logFile.lastModified()), ZoneId.systemDefault()), time).toDays() > maxDay;
	}

	/**
	 * <p>删除日志</p>
	 */
	private void deleteFile() {
		var time = LocalDateTime.now();
		int maxDay = LoggerConfig.getFileMaxDay();
		var folder = new File(LoggerConfig.getFileFolder());
		if (folder.exists()) {
			for (File child : Objects.requireNonNull(folder.listFiles())) {
				if (child.getName().endsWith(".log") && deleteable(child, time, maxDay)) child.delete();
			}
		}
	}

}
