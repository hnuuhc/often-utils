package org.haic.often.net.download;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

/**
 * Connection 接口是一个方便的 HTTP 客户端和会话对象，用于从 Web 下载文件。
 * <p>
 * 使用的“连接”并不意味着在连接对象的生命周期内与服务器保持长期连接.
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/8/18 00:44
 */
public abstract class SionConnection {

	/**
	 * 设置新的要下载文件的 URL，协议必须是 HTTP 或 HTTPS
	 * <p>
	 * 在修改 URL 时，同步置空 fileName 和 hash 而不会修改其它参数，适用于相同域名或来源的下载
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection url(@NotNull String url);

	/**
	 * 设置要下载文件的 URL，协议必须是 HTTP 或 HTTPS
	 * <p>
	 * 此方法仅用于初始化设置或特殊情况下修改同一文件的 URL
	 * <p>
	 * 不同的下载链接不应复用类，内部被改变的 fileName 和 hash 参数会导致致命的下载错误，如果复用，应该同步修改或置空上述两个参数，建议使用 {@link #url} 方法
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection alterUrl(@NotNull String url);

	/**
	 * 设置会话文件,读取并配置参数<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param src session文件路径
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection session(@NotNull String src);

	/**
	 * 设置会话文件,读取并配置参数<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param file session文件
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection session(@NotNull File file);

	/**
	 * 连接用户代理（ 字符串 用户代理）<br/> 设置请求用户代理标头
	 *
	 * @param userAgent 要使用的用户代理
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection userAgent(@NotNull String userAgent);

	/**
	 * 连接引荐来源网址（ 字符串 引荐来源网址）<br/> 设置请求引荐来源网址（又名“引荐来源网址”）标头
	 *
	 * @param referrer 要使用的来源网址
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection referrer(@NotNull String referrer);

	/**
	 * 连接头（ 字符串 名称， 字符串 值）<br/> 设置请求标头
	 *
	 * @param name  标题名称
	 * @param value 标头值
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection header(@NotNull String name, @NotNull String value);

	/**
	 * 连接头（ Map  < String  , String  > 头）<br/> 将每个提供的标头添加到请求中
	 *
	 * @param headers 标头名称映射 -> 值对
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection headers(@NotNull Map<String, String> headers);

	/**
	 * 设置要在请求中发送的 cookie
	 *
	 * @param name  cookie 的名称
	 * @param value cookie 的值
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection cookie(@NotNull String name, @NotNull String value);

	/**
	 * 连接 cookies （ Map < String  , String  >cookies）<br/> 将每个提供的 cookie 添加到请求中
	 *
	 * @param cookies 名称映射 -> 值对
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection cookies(@NotNull Map<String, String> cookies);

	/**
	 * 设置授权码或身份识别标识<br/> 有些服务器不使用cookie验证身份,使用authorization进行验证<br/> 一般信息在cookie或local Storage中存储
	 *
	 * @param auth 授权码或身份识别标识
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection auth(@NotNull String auth);

	/**
	 * 设置多线程下载，线程数不小于1，否则抛出异常
	 *
	 * @param nThread 线程最大值,非零或负数
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection thread(int nThread);

	/**
	 * 设置文件大小, 请保证大小正确, 仅在多线程模式并且无法通过请求头获取文件大小时使用
	 *
	 * @param fileSize file size
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection fileSize(long fileSize);

	/**
	 * 设置文件的下载模式
	 *
	 * @param method 下载模式
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection method(@NotNull SionMethod method);

	/**
	 * 设置将要下载文件的文件名
	 * <p>
	 * 文件名字符串字符长度不能高于240,使用 FilesUtils.nameLength 获取实际字符长度
	 *
	 * @param fileName 文件名
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection fileName(@NotNull String fileName);

	/**
	 * 当待下载的文件已经存在时,将重命名文件
	 * <p>
	 * 例: xxx - 1.png
	 *
	 * @param rename 开启重命名
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection rename(boolean rename);

	/**
	 * 设置用于此请求的 SOCKS 代理
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 * @return 此连接，用于链接
	 */
	public SionConnection socks(@NotNull String ipAddr) {
		if (ipAddr.isEmpty()) return proxy(Proxy.NO_PROXY);
		if (ipAddr.startsWith("[")) {
			return socks(ipAddr.substring(1, ipAddr.indexOf(']')), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
		} else {
			int index = ipAddr.lastIndexOf(":");
			return socks(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
		}
	}

	/**
	 * 设置用于此请求的 SOCKS 代理
	 *
	 * @param host 代理主机名
	 * @param port 代理端口
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection socks(@NotNull String host, int port);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 * @return 此连接，用于链接
	 */
	public SionConnection proxy(@NotNull String ipAddr) {
		if (ipAddr.isEmpty()) return proxy(Proxy.NO_PROXY);
		if (ipAddr.startsWith("[")) {
			return proxy(ipAddr.substring(1, ipAddr.indexOf(']')), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
		} else {
			int index = ipAddr.lastIndexOf(":");
			return proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
		}
	}

	/**
	 * 设置用于此请求的 HTTP 代理
	 *
	 * @param host 代理主机名
	 * @param port 代理端口
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection proxy(@NotNull String host, int port);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/> 设置用于此请求的代理
	 *
	 * @param proxy 要使用的代理
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection proxy(@NotNull Proxy proxy);

	/**
	 * 在状态码不为200+或300+时，抛出执行异常，并获取一些参数，一般用于调试<br/> 默认情况下为false
	 *
	 * @param exit 启用错误退出
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection failThrow(boolean exit);

	/**
	 * 设置 重试次数
	 *
	 * @param retry 重试次数
	 * @return this
	 */
	public abstract SionConnection retry(int retry);

	/**
	 * 在请求超时或者指定状态码发生时，进行重试，重试超过次数或者状态码正常返回
	 *
	 * @param retry  重试次数
	 * @param millis 重试等待时间(毫秒)
	 * @return this
	 */
	public abstract SionConnection retry(int retry, int millis);

	/**
	 * 在请求超时或者指定状态码发生时，无限进行重试，直至状态码正常返回
	 *
	 * @param unlimit 启用无限重试, 默认false
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection retry(boolean unlimit);

	/**
	 * 在请求超时或者指定状态码发生时，无限进行重试，直至状态码正常返回
	 *
	 * @param unlimit 启用无限重试, 默认false
	 * @param millis  重试等待时间(毫秒)
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection retry(boolean unlimit, int millis);

	/**
	 * 额外指定错误状态码码，在指定状态发生时，也进行重试，可指定多个
	 *
	 * @param statusCode 状态码
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection retryStatusCodes(int... statusCode);

	/**
	 * 额外指定错误状态码码，在指定状态发生时，也进行重试，可指定多个
	 *
	 * @param retryStatusCodes 状态码列表
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection retryStatusCodes(List<Integer> retryStatusCodes);

	/**
	 * 设置写入文件时缓冲区大小,默认大小为8192字节
	 *
	 * @param bufferSize 缓冲区大小
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection bufferSize(int bufferSize);

	/**
	 * 设置md5效验值进行文件完整性效验<br/> 如果效验不正确会在下载完成后删除文件并重置配置文件<br/> 抛出异常信息
	 *
	 * @param hash 文件md5值
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection hash(@NotNull String hash);

	/**
	 * 启用下载文件的MD5效验
	 *
	 * @param valid 是否开启,默认为开启
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection valid(boolean valid);

	/**
	 * 设置在多线程模式下载时,分块最大大小
	 *
	 * @param kb 指定块大小(KB)
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection pieceSize(long kb);

	/**
	 * 设置下载文件的存储文件夹
	 *
	 * @param folder 存储路径
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection folder(@NotNull String folder);

	/**
	 * 设置下载文件的存储文件夹
	 *
	 * @param folder 存储文件夹
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection folder(@NotNull File folder);

	/**
	 * 设置监听器
	 *
	 * @param listener 监听器
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection listener(@NotNull SionListener listener);

	/**
	 * 设置监听器
	 *
	 * @param listener 监听器
	 * @param millis   监听频率(毫秒)
	 * @return 此连接，用于链接
	 */
	public abstract SionConnection listener(@NotNull SionListener listener, int millis);

	/**
	 * 下载网络文件,返回状态码
	 * <p>
	 * 自动寻找并存放下载文件夹路径
	 *
	 * @return 下载状态码
	 */
	public abstract SionResponse execute();

}
