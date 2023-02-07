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

	/**
	 * <p>文件适配器名称：{@value}</p>
	 */
	public static final String ADAPTER = "file";
	/**
	 * <p>文件格式：{@value}</p>
	 */
	private static final SimpleDateFormat FILE_SUFFIX_FORMAT = new SimpleDateFormat(".yyyy-MM-dd");

	public FileLoggerAdapter() {
		var output = this.buildOutput();
		this.output = output;
		this.errorOutput = output;
	}

	@Override
	public void release() {
		super.release();
		final LocalDateTime time = LocalDateTime.now();
		final int maxDay = LoggerConfig.getFileMaxDay();
		for (File children : Objects.requireNonNull(this.buildFile().getParentFile().listFiles())) {
			if (children.getName().endsWith(".log") && this.deleteable(children, time, maxDay)) {
				children.delete();
			}
		}
	}

	/**
	 * <p>新建文件输出流</p>
	 *
	 * @return 文件输出流
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
	 * <p>新建日志文件</p>
	 *
	 * @return 日志文件
	 */
	private File buildFile() {
		File file = new File(LoggerConfig.getFileName() + FILE_SUFFIX_FORMAT.format(new Date()) + ".log");
		// 创建上级目录
		final File parent = file.getParentFile();
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

}
