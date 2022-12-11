package org.haic.often.net.download;

import org.haic.often.annotations.NotNull;

/**
 * Sion下载类监听器接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/8 15:06
 */
public interface SionListener {

	/**
	 * 在开始之前调用它以通知侦听器传输
	 *
	 * @param fileName 最终确定的文件名
	 * @param rate     最近一次写入复制的字节数
	 * @param schedule 到目前为止复制操作传输的字节总数
	 * @param fileSize 正在复制的流中的字节数。如果大小未知，则为0
	 */
	void bytesTransferred(@NotNull String fileName, long rate, long schedule, long fileSize);

}
