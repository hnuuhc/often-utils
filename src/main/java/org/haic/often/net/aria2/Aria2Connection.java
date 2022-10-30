package org.haic.often.net.aria2;

import com.alibaba.fastjson2.JSONArray;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/10/24 12:39
 */
public abstract class Aria2Connection {

	@Contract(pure = true)
	public abstract Aria2Connection newRequest();

	/**
	 * 设置用于此请求的 SOCKS 代理
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract Aria2Connection socks(@NotNull String ipAddr);

	/**
	 * 设置用于此请求的 SOCKS 代理
	 *
	 * @param host 代理主机名
	 * @param port 代理端口
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract Aria2Connection socks(@NotNull String host, int port);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract Aria2Connection proxy(@NotNull String ipAddr);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param host 代理地址
	 * @param port 代理端口
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract Aria2Connection proxy(@NotNull String host, int port);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param proxy 要使用的代理
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract Aria2Connection proxy(@NotNull Proxy proxy);

	/**
	 * 设置文件夹路径,用于存放下载文件,如果不设置,将使用aria2全局配置路径
	 *
	 * @param folderPath 文件夹路径
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection folder(@NotNull String folderPath);

	/**
	 * 设置文件夹路径,用于存放下载文件,如果不设置,将使用aria2全局配置路径
	 *
	 * @param folder 文件夹
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection folder(@NotNull File folder);

	/**
	 * 设置rpc密钥
	 *
	 * @param token rpc密钥
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection rpcToken(@NotNull String token);

	/**
	 * 添加公共参数,所有的下载链接遵守该配置
	 *
	 * @param name  key
	 * @param value value
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection rpcParams(@NotNull String name, @NotNull String value);

	/**
	 * 添加公共参数,所有的下载链接遵守该配置
	 * <p>
	 * 警告:公共参数应该在添加链接之前使用,否则无效
	 *
	 * @param params 公共参数
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection rpcParams(@NotNull Map<String, String> params);

	/**
	 * 设置公共参数,所有的下载链接遵守该配置
	 * <p>
	 * 警告:公共参数应该在添加链接之前使用,否则无效
	 *
	 * @param params 公共参数
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection setRpcParams(@NotNull Map<String, String> params);

	/**
	 * 设置链接下载时使用的代理
	 *
	 * @param host 代理HOST
	 * @param post 代理端口
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection rpcProxy(@NotNull String host, int post);

	/**
	 * 设置链接下载时使用的代理
	 *
	 * @param ipAddr 代理
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection rpcProxy(@NotNull String ipAddr);

	/**
	 * 添加公共请求头参数,所有的下载链接遵守该配置
	 * <p>
	 * 警告:公共参数应该在添加链接之前使用,否则无效
	 *
	 * @param name  key
	 * @param value value
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection rpcHeader(@NotNull String name, @NotNull String value);

	/**
	 * 添加公共请求头参数,所有的下载链接遵守该配置
	 * <p>
	 * 警告:公共参数应该在添加链接之前使用,否则无效
	 *
	 * @param headers 请求头参数
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection rpcHeaders(@NotNull Map<String, String> headers);

	/**
	 * 设置公共请求头参数,所有的下载链接遵守该配置
	 * <p>
	 * 警告:公共参数应该在添加链接之前使用,否则无效
	 *
	 * @param headers 请求头参数
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection setRpcHeaders(@NotNull Map<String, String> headers);

	/**
	 * 设置 user-agent
	 *
	 * @param userAgent userAgent
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection setRpcUserAgent(@NotNull String userAgent);

	/**
	 * 添加链接 (Url or Magnet or Torrent)
	 *
	 * @param url 链接
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection addUrl(@NotNull String url);

	/**
	 * 添加链接 (Url or Magnet or Torrent)
	 *
	 * @param url     链接
	 * @param headers 请求头参数
	 * @return this
	 */
	public abstract Aria2Connection addUrl(@NotNull String url, @NotNull Map<String, String> headers);

	/**
	 * 添加链接 (Url or Magnet or Torrent)
	 *
	 * @param listUrl URL列表
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection addUrl(@NotNull List<String> listUrl);

	/**
	 * 添加链接 (Url or Magnet or Torrent)
	 *
	 * @param listUrl URL列表
	 * @param headers 请求头参数
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection addUrl(@NotNull List<String> listUrl, @NotNull Map<String, String> headers);

	/**
	 * 指定由Aria2Method表示的会话,常用于获取Aria2状态信息
	 *
	 * @param method Aria2Method
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection session(@NotNull Aria2Method method);

	/**
	 * 指定由Aria2Method表示的会话,常用于获取Aria2状态信息
	 *
	 * @param method Aria2Method
	 * @param params Aria2Method params
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection session(@NotNull Aria2Method method, @NotNull JSONArray params);

	/**
	 * 指定由gid（字符串）表示的会话,常用于获取指定gid表示的信息
	 *
	 * @param method Aria2Method
	 * @param gid    gid
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection session(@NotNull Aria2Method method, @NotNull String gid);

	/**
	 * 删除由gid（字符串）表示的下载
	 *
	 * @param gid gid
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection remove(@NotNull String gid);

	/**
	 * 暂停由gid（字符串）表示的下载
	 *
	 * @param gid gid
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection pause(@NotNull String gid);

	/**
	 * 暂停所有下载
	 *
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection pauseAll();

	/**
	 * 继续开始由gid（字符串）表示的下载
	 *
	 * @param gid gid
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection unpause(@NotNull String gid);

	/**
	 * 继续开始所有下载
	 *
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection unpauseAll();

	/**
	 * 获取由 gid（字符串）表示的下载进度和状态
	 *
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection tellStatus(@NotNull String gid);

	/**
	 * 变更指定由gid（字符串）表示的下载配置
	 *
	 * @param gid    gid
	 * @param option option信息
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Aria2Connection changeOption(@NotNull String gid, @NotNull Map<String, String> option);

	/**
	 * Socket推送 JSON数据,仅ws,wss协议有效,http和https协议会抛出异常,注意该方法会一直等待数据交互,直至获得返回数据为止
	 *
	 * @return 返回的json信息
	 */
	@Contract(pure = true)
	public abstract String send();

	/**
	 * GET请求 JSON数据
	 *
	 * @return result or webstatus
	 */
	@Contract(pure = true)
	public abstract String get();

	/**
	 * POST请求 JSON数据
	 *
	 * @return result or webstatus
	 */
	@Contract(pure = true)
	public abstract String post();

}
