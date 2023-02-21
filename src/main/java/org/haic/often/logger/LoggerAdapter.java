package org.haic.often.logger;

import java.io.PrintStream;

/**
 * <p>日志适配器</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public abstract class LoggerAdapter {

	/**
	 * <p>是否可用</p>
	 */
	private boolean available = true;
	/**
	 * <p>常规日志输出流</p>
	 */
	protected PrintStream output;
	/**
	 * <p>异常日志输出流</p>
	 */
	protected PrintStream errorOutput;

	protected LoggerAdapter() {
	}

	/**
	 * @param output      常规日志输出流
	 * @param errorOutput 异常日志输出流
	 */
	protected LoggerAdapter(PrintStream output, PrintStream errorOutput) {
		this.output = output;
		this.errorOutput = errorOutput;
	}

	/**
	 * <p>输出日志</p>
	 *
	 * @param message 日志
	 */
	public void output(String message) {
		if (this.available) {
			output.println(message);
			output.flush();
		}
	}

	/**
	 * <p>输出错误日志</p>
	 *
	 * @param message 日志
	 */
	public void errorOutput(String message) {
		if (this.available) {
			errorOutput.println(message);
			errorOutput.flush();
		}
	}

	/**
	 * <p>释放资源</p>
	 */
	public void release() {
		this.available = false;
		// 是否需要关闭错误输出
		boolean closeErrorOutput = this.output != this.errorOutput;
		if (this.output != null) {
			this.output.flush();
			this.output.close();
		}
		if (closeErrorOutput && this.errorOutput != null) {
			this.errorOutput.flush();
			this.errorOutput.close();
		}
	}

}
