package org.haic.often.net.download;

import org.jetbrains.annotations.NotNull;

/**
 * HLS下载类监听器接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/8 17:45
 */
public interface HLSListener {

	/**
	 * 在开始之前调用它以通知侦听器传输
	 *
	 * @param fileName 最终确定的文件名
	 * @param rate     最近一次写入复制的字节数
	 * @param schedule 到目前为止复制操作传输的字节总数
	 * @param written  已写入的块数量,实际上是按照顺序写入
	 * @param total    下载块总数
	 */
	void bytesTransferred(@NotNull String fileName, long rate, long schedule, int written, int total);

}
