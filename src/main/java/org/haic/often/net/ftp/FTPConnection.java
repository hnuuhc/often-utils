package org.haic.often.net.ftp;

import org.haic.often.annotations.NotNull;

import java.net.Proxy;
import java.nio.charset.Charset;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/10/8 13:15
 */
public abstract class FTPConnection {

	/**
	 * 被动模式,PASV模式。通知服务器打开一个数据端口，客户端将连接到这个端口进行数据传输。
	 * <p>
	 * 默认为被动模式
	 *
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection localPassiveMode();

	/**
	 * 主动模式,PORT模式。通知客户端打开一个数据端口，服务端将连接到这个端口进行数据传输。
	 * <p>
	 * 默认为被动模式
	 *
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection remotePassiveMode();

	/**
	 * 设置连接超时时间，连接超时（ int millis）<br/>
	 * 默认超时为 2000 毫秒，超时为零被视为无限超时<br/>
	 *
	 * @param millis 超时连接或读取之前的毫秒数（千分之一秒）
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection timeout(int millis);

	/**
	 * 设置写入文件时缓冲区大小,默认大小为8192字节
	 *
	 * @param bufferSize 缓冲区大小
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection bufferSize(int bufferSize);

	/**
	 * 设置写入文件时字符集格式
	 *
	 * @param charsetName 字符集格式名称
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection charset(@NotNull String charsetName);

	/**
	 * 设置写入文件时字符集格式
	 *
	 * @param charset 字符集格式
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection charset(@NotNull Charset charset);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection proxy(@NotNull String ipAddr);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param host 代理地址
	 * @param port 代理端口
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection proxy(@NotNull String host, int port);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param proxy 要使用的代理
	 * @return 此连接，用于链接
	 */
	public abstract FTPConnection proxy(@NotNull Proxy proxy);

	/**
	 * 运行程序，获取 响应结果
	 *
	 * @return Response
	 */
	public abstract FTPResponse execute();

}
