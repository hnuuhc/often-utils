package org.haic.often;

import org.haic.often.util.IOUtil;
import org.haic.often.util.SystemUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * 终端控制台
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/12/25 06:50
 */
public class Terminal {

	private Charset charset = SystemUtil.DEFAULT_CHARSET; // 字符集格式
	private final ProcessBuilder builder = new ProcessBuilder().redirectErrorStream(true);

	private Terminal() {
	}

	/**
	 * 创建一个新的终端会话
	 *
	 * @return 此方法
	 */
	public static Terminal newTerminal() {
		return new Terminal();
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return 此方法
	 */
	public static Terminal command(@NotNull String... dos) {
		return newTerminal().setCommand(dos);
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return 此方法
	 */
	public static Terminal command(@NotNull List<String> dos) {
		return newTerminal().setCommand(dos);
	}

	/**
	 * 一些文件路径需要单引号处理才能正确识别命令
	 *
	 * @param dos 命令
	 * @return 添加单引号后的命令
	 */
	public static String quote(@NotNull String dos) {
		return "'" + dos + "'";
	}

	/**
	 * 一些文件路径需要双引号处理才能正确识别命令
	 *
	 * @param dos 命令
	 * @return 添加双号后的命令
	 */
	public static String doubleQuote(@NotNull String dos) {
		return "\"" + dos + "\"";
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return 此方法
	 */
	public Terminal setCommand(@NotNull String... dos) {
		return dos.length == 0 ? this : setCommand(Arrays.asList(dos));
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return 此方法
	 */
	public Terminal setCommand(@NotNull List<String> dos) {
		builder.command(dos);
		return this;
	}

	/**
	 * 设置 字符集编码名
	 *
	 * @param charsetName 字符集编码名
	 * @return 此方法
	 */
	public Terminal charset(@NotNull String charsetName) {
		charset(Charset.forName(charsetName));
		return this;
	}

	/**
	 * 设置 字符集编码
	 *
	 * @param charset 字符集编码
	 * @return 此方法
	 */
	public Terminal charset(@NotNull Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录路径
	 * @return 此方法
	 */
	public Terminal directory(@NotNull String directory) {
		return directory(new File(directory));
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录
	 * @return 此方法
	 */
	public Terminal directory(@NotNull File directory) {
		builder.directory(directory);
		return this;
	}

	/**
	 * 执行终端命令并获取退出值
	 *
	 * @return 进程的退出值, 一般情况下, 0为正常终止
	 */
	public int execute() {
		int status = 1;
		Process process;
		try (InputStream inputStream = (process = builder.start()).getInputStream()) {
			inputStream.close();
			status = process.waitFor();
			process.destroy();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return status;
	}

	/**
	 * 执行终端命令并获取执行的信息
	 *
	 * @return 执行的信息
	 */
	@NotNull
	public String read() {
		String result = "";
		Process process;
		try (InputStream inputStream = (process = builder.start()).getInputStream()) {
			result = IOUtil.stream(inputStream).charset(charset).read();
			process.waitFor();
			process.destroy();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		return result.strip();
	}

	/**
	 * 将子进程标准 I/O 的源和目标设置为与当前 Java 进程的相同。
	 * 这是一种方便的方法。表单的调用
	 */
	public void inheritIO() {
		try {
			builder.inheritIO().start().waitFor();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

}
