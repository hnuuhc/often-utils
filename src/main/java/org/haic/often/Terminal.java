package org.haic.often;

import org.haic.often.util.IOUtil;
import org.haic.often.util.SystemUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
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
	private String terminal = "cmd"; // 默认终端
	private final ProcessBuilder builder = new ProcessBuilder().redirectErrorStream(true);

	private Terminal() {
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return new RunCmd
	 */
	@Contract(pure = true)
	public static Terminal command(@NotNull String... dos) {
		return new Terminal().setCommand(dos);
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return new RunCmd
	 */
	@Contract(pure = true)
	public static Terminal command(@NotNull List<String> dos) {
		return new Terminal().setCommand(dos);
	}

	/**
	 * 清理控制台输出
	 */
	@Contract(pure = true)
	public static void cls() {
		Terminal.command("cls").inheritIO();
	}

	/**
	 * 一些文件路径需要单引号处理才能正确识别命令
	 *
	 * @param dos 命令
	 * @return 添加单引号后的命令
	 */
	@Contract(pure = true)
	public static String quote(@NotNull String dos) {
		return Symbol.SINGLE_QUOTE + dos + Symbol.SINGLE_QUOTE;
	}

	/**
	 * 一些文件路径需要双引号处理才能正确识别命令
	 *
	 * @param dos 命令
	 * @return 添加双号后的命令
	 */
	@Contract(pure = true)
	public static String doubleQuote(@NotNull String dos) {
		return Symbol.DOUBLE_QUOTE + dos + Symbol.DOUBLE_QUOTE;
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return this
	 */
	@Contract(pure = true)
	public Terminal setCommand(@NotNull String... dos) {
		return dos.length == 0 ? this : setCommand(Arrays.asList(dos));
	}

	/**
	 * 设置待执行的命令
	 *
	 * @param dos 待执行的命令
	 * @return this
	 */
	@Contract(pure = true)
	public Terminal setCommand(@NotNull List<String> dos) {
		List<String> terminalCommand = new ArrayList<>();
		terminalCommand.add(terminal);
		terminalCommand.add("/c");
		terminalCommand.addAll(dos);
		builder.command(terminalCommand);
		return this;
	}

	/**
	 * 设置执行命令时使用的终端
	 *
	 * @param terminal 终端
	 * @return this
	 */
	@Contract(pure = true)
	public Terminal terminal(@NotNull String terminal) {
		this.terminal = terminal;
		return this;
	}

	/**
	 * 设置 字符集编码名
	 *
	 * @param charsetName 字符集编码名
	 * @return this
	 */
	@Contract(pure = true)
	public Terminal charset(@NotNull String charsetName) {
		charset(Charset.forName(charsetName));
		return this;
	}

	/**
	 * 设置 字符集编码
	 *
	 * @param charset 字符集编码
	 * @return this
	 */
	@Contract(pure = true)
	public Terminal charset(@NotNull Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录路径
	 * @return this
	 */
	@Contract(pure = true)
	public Terminal directory(@NotNull String directory) {
		return directory(new File(directory));
	}

	/**
	 * 设置 工作目录
	 *
	 * @param directory 工作目录
	 * @return this
	 */
	@Contract(pure = true)
	public Terminal directory(@NotNull File directory) {
		builder.directory(directory);
		return this;
	}

	/**
	 * 执行终端命令并获取退出值
	 *
	 * @return 进程的退出值, 一般情况下, 0为正常终止
	 */
	@Contract(pure = true)
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
	@Contract(pure = true)
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
