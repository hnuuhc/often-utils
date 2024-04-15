package org.haic.often.net.ftp;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.io.CopyStreamListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * FTPConnection 响应接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/8 13:29
 */
public abstract class FTPResponse {

	public abstract void disconnect();

	/**
	 * 获取FTP服务器连接状态码
	 *
	 * @return 连接状态码
	 */
	public abstract int statusCode();

	/**
	 * 获取FTP服务器目录文件列表
	 *
	 * @param remote FTP服务器目录
	 * @return 文件列表
	 */
	public abstract List<FTPFile> listFiles(@NotNull String remote);

	/**
	 * 设置执行存储/检索操作时要使用的侦听器
	 *
	 * @param listener 侦听器
	 * @return 此会话, 用于操作
	 */
	public abstract FTPResponse listener(@NotNull CopyStreamListener listener);

	/**
	 * 删除FTP服务器的文件
	 *
	 * @param remote FTP服务器文件路径
	 * @return 执行状态码
	 */
	public abstract int delete(@NotNull String remote);

	/**
	 * 移动FTP服务器的文件
	 *
	 * @param remoteIn  待移动的路径
	 * @param renameOut 移动后的路径
	 * @return 执行状态码
	 */
	public abstract int rename(@NotNull String remoteIn, @NotNull String renameOut);

	/**
	 * 上传文件到FTP服务器
	 *
	 * @param local  本地文件路径
	 * @param remote FTP服务器文件路径
	 * @return 上传结果
	 */
	public abstract int upload(@NotNull String local, @NotNull String remote);

	/**
	 * 从FTP服务器下载文件
	 *
	 * @param remote FTP服务器文件路径
	 * @param local  下载后的文件路径
	 * @return true or false
	 */
	public abstract int download(@NotNull String remote, @NotNull String local);

}
