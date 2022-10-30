package org.haic.often.logger.adapter;

import org.haic.often.logger.LoggerAdapter;

/**
 * <p>控制台适配器</p>
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public final class ConsoleLoggerAdapter extends LoggerAdapter {

	/**
	 * <p>控制台适配器名称：{@value}</p>
	 */
	public static final String ADAPTER = "console";

	public ConsoleLoggerAdapter() {
		super(System.out, System.err);
	}

}
