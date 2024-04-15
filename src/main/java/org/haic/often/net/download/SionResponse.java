package org.haic.often.net.download;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Download响应接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/8/18 00:48
 */
public abstract class SionResponse {

	/**
	 * 获取当前下载的完成状态
	 *
	 * @return 当前下载的完成状态
	 */
	public abstract int statusCode();

	/**
	 * 获取当前下载的文件名
	 *
	 * @return 文件名
	 */
	public abstract String fileName();

	/**
	 * 获取当前下载的存储在本地的文件路径
	 *
	 * @return 本地的文件路径
	 */
	public abstract String filePath();

	/**
	 * 获取当前下载的的文件大小
	 *
	 * @return 文件大小
	 */
	public abstract long fileSize();

	/**
	 * 获取当前下载的的请求头值
	 *
	 * @return 请求头值
	 */
	public abstract String header(@NotNull String name);

	/**
	 * 获取当前下载的的所有请求头
	 *
	 * @return 所有请求头
	 */
	public abstract Map<String, String> headers();

	/**
	 * 获取当前下载的的cookie值
	 *
	 * @return cookie值
	 */
	public abstract String cookie(@NotNull String name);

	/**
	 * 获取当前下载的的cookies
	 *
	 * @return cookies
	 */
	public abstract Map<String, String> cookies();

	/**
	 * 获取当前下载的的网络md5
	 * <p>
	 * 此md5可能是hash()方法设置的,也可能是服务器返回的,如果都没有则返回null
	 *
	 * @return md5
	 */
	public abstract String hash();

	/**
	 * 获取当前下载的URL
	 *
	 * @return URL
	 */
	public abstract String url();

	/**
	 * 如果完成状态不为成功,则重启当前下载任务
	 *
	 * @return 此连接, 用于连接
	 */
	public abstract SionResponse restart();

	/**
	 * 清理未完成的存储文件,如果文件下载完成则不做处理
	 *
	 * @return 执行状态
	 */
	public abstract boolean clear();

	/**
	 * 删除当前下载的本地存储文件,无论是否完成
	 *
	 * @return 执行状态
	 */
	public abstract boolean delete();
}
