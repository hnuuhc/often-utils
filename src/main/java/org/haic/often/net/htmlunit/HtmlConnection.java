package org.haic.often.net.htmlunit;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.Method;
import org.haic.often.parser.xml.Document;

import java.net.Proxy;
import java.util.List;
import java.util.Map;

/**
 * Connection 接口是一个方便的 HTTP 客户端和会话对象，用于从 Web 获取内容，并将它们解析为 Documents。
 * <p>
 * 使用的“连接”并不意味着在连接对象的生命周期内与服务器保持长期连接。套接字连接仅在请求执行（ execute() 、 get()或post() ）时建立，并消耗服务器的响应。
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/16 14:04
 */
public abstract class HtmlConnection {

	/**
	 * 设置要获取的请求 URL，协议必须是 HTTP 或 HTTPS
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection url(@NotNull String url);

	/**
	 * 连接newRequest () 创建一个新请求，使用此 Connection 作为会话状态并初始化连接设置（然后可以独立于返回的 Connection.Request 对象）。
	 *
	 * @return 一个新的 Connection 对象，具有共享的 Cookie 存储和来自此 Connection 和 Request 的初始化设置
	 */
	@Contract(pure = true)
	public abstract HtmlConnection newRequest();

	/**
	 * 连接用户代理（ 字符串 用户代理）<br/> 设置请求用户代理标头
	 *
	 * @param userAgent 要使用的用户代理
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection userAgent(@NotNull String userAgent);

	/**
	 * 添加请求头user-agent，以移动端方式访问页面
	 *
	 * @param isPhone true or false
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection isPhone(boolean isPhone);

	/**
	 * 连接followRedirects （布尔followRedirects）<br/> 将连接配置为（不）遵循服务器重定向，默认情况下这是true
	 *
	 * @param followRedirects 如果应该遵循服务器重定向，则为 true
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection followRedirects(boolean followRedirects);

	/**
	 * 连接引荐来源网址（ 字符串 引荐来源网址）<br/> 设置请求引荐来源网址（又名“引荐来源网址”）标头
	 *
	 * @param referrer 要使用的来源网址
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection referrer(@NotNull String referrer);

	/**
	 * 设置授权码或身份识别标识
	 * <p>
	 * 有些服务器不使用cookie验证身份,使用authorization进行验证
	 * <p>
	 * 一般信息在cookie或local Storage中存储
	 * <p>
	 * 如果没有协议类型,默认使用Bearer
	 *
	 * @param auth 授权码或身份识别标识
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection auth(@NotNull String auth);

	/**
	 * 设置连接超时时间，连接超时（ int millis）<br/>
	 * 默认超时为 10000 毫秒，超时为零被视为无限超时<br/>
	 *
	 * @param millis 超时连接或读取之前的毫秒数（千分之一秒）
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection timeout(int millis);

	/**
	 * 设置连接请求类型参数,用于服务器识别内容类型
	 *
	 * @param type 请求类型
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection contentType(@NotNull String type);

	/**
	 * 连接头（ 字符串 名称， 字符串 值）<br/> 设置请求标头
	 *
	 * @param name  标题名称
	 * @param value 标头值
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection header(@NotNull String name, @NotNull String value);

	/**
	 * 连接头（ Map  < String  , String  > 头）<br/> 将每个提供的标头添加到请求中
	 *
	 * @param headers 标头名称映射 -> 值对
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection headers(@NotNull Map<String, String> headers);

	/**
	 * 连接头（ Map  < String  , String  > 头）
	 * <p>
	 * 将为连接设置全新的请求标头
	 *
	 * @param headers 标头名称映射 -> 值对
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection setHeaders(@NotNull Map<String, String> headers);

	/**
	 * 删除在此请求/响应中设置 header。
	 *
	 * @param key header的键
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection removeHeader(@NotNull String key);

	/**
	 * 设置要在请求中发送的 cookie
	 *
	 * @param name  cookie 的名称
	 * @param value cookie 的值
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection cookie(@NotNull String name, @NotNull String value);

	/**
	 * 连接 cookies （ Map < String  , String  >cookies）<br/> 将每个提供的 cookie 添加到请求中
	 *
	 * @param cookies 名称映射 -> 值对
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection cookies(@NotNull Map<String, String> cookies);

	/**
	 * 连接 cookies （ Map < String  , String  >cookies）
	 * <p>
	 * 将为连接设置全新的 cookie
	 *
	 * @param cookies 名称映射 -> 值对
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection setCookies(@NotNull Map<String, String> cookies);

	/**
	 * 删除在此请求/响应中设置 cookie。
	 *
	 * @param name cookie的名称
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection removeCookie(@NotNull String name);

	/**
	 * 连接数据（ 字符串 键、 字符串 值）<br/> 添加请求数据参数。请求参数在 GET 的请求查询字符串中发送，在 POST 的请求正文中发送。一个请求可能有多个同名的值。
	 *
	 * @param key   数据键
	 * @param value 数据值
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection data(@NotNull String key, @NotNull String value);

	/**
	 * 根据所有提供的数据设置全新的请求数据参数
	 *
	 * @param params 数据参数
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection data(@NotNull Map<String, String> params);

	/**
	 * 设置 POST（或 PUT）请求正文<br/> 当服务器需要一个普通请求正文，而不是一组 URL 编码形式的键/值对时很有用<br/> 一般为JSON格式,若不是则作为普通数据发送
	 *
	 * @param body 请求正文
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection requestBody(@NotNull String body);

	/**
	 * 设置用于此请求的 SOCKS 代理
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection socks(@NotNull String ipAddr);

	/**
	 * 设置用于此请求的 SOCKS 代理
	 *
	 * @param host 代理主机名
	 * @param port 代理端口
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection socks(@NotNull String host, int port);

	/**
	 * 设置用于此请求的 SOCKS 代理<br/> 需要验证的代理服务器<br/> 接口存在问题，SOCKS协议代理访问外网会失败，应使用Http协议代理
	 *
	 * @param host     代理URL
	 * @param port     代理端口
	 * @param username 代理用户名
	 * @param password 代理用户密码
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection socks(@NotNull String host, int port, @NotNull String username, @NotNull String password);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于此请求的代理
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection proxy(@NotNull String ipAddr);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/> * 设置用于此请求的代理
	 *
	 * @param host 代理地址
	 * @param port 代理端口
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection proxy(@NotNull String host, int port);

	/**
	 * 设置用于此请求的 HTTP 代理<br/> 需要验证的代理服务器
	 *
	 * @param host     代理URL
	 * @param port     代理端口
	 * @param username 代理用户名
	 * @param password 代理用户密码
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection proxy(@NotNull String host, int port, @NotNull String username, @NotNull String password);

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/> 设置用于此请求的代理
	 *
	 * @param proxy 要使用的代理
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection proxy(@NotNull Proxy proxy);

	/**
	 * 连接方法（ Connection.Method方法） 设置要使用的请求方法，GET 或 POST。默认为 GET。
	 *
	 * @param method HTTP 请求方法
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection method(@NotNull Method method);

	/**
	 * 在请求超时或者指定状态码发生时，进行重试，重试超过次数或者状态码正常返回
	 *
	 * @param retry 重试次数
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection retry(int retry);

	/**
	 * 在请求超时或者指定状态码发生时，进行重试，重试超过次数或者状态码正常返回
	 *
	 * @param retry  重试次数
	 * @param millis 重试等待时间(毫秒)
	 * @return 此链接, 用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection retry(int retry, int millis);

	/**
	 * 在请求超时或者指定状态码发生时，无限进行重试，直至状态码正常返回
	 *
	 * @param unlimit 启用无限重试, 默认false
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection retry(boolean unlimit);

	/**
	 * 在请求超时或者指定状态码发生时，无限进行重试，直至状态码正常返回
	 *
	 * @param unlimit 启用无限重试, 默认false
	 * @param millis  重试等待时间(毫秒)
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection retry(boolean unlimit, int millis);

	/**
	 * 额外指定错误状态码码，在指定状态发生时，也进行重试，可指定多个
	 *
	 * @param statusCode 状态码
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection retryStatusCodes(int... statusCode);

	/**
	 * 额外指定错误状态码码，在指定状态发生时，也进行重试，可指定多个
	 *
	 * @param retryStatusCodes 状态码列表
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection retryStatusCodes(List<Integer> retryStatusCodes);

	/**
	 * 在状态码不为200+或300+时，抛出执行异常，并获取一些参数，一般用于调试<br/> 默认情况下为false
	 *
	 * @param exit 启用错误退出
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection failThrow(boolean exit);

	/**
	 * 启用/禁用 CSS 支持。默认情况下，禁用此属性。如果禁用 HtmlUnit 将不会下载链接的 css 文件，也不会触发相关的 onload/onerror 事件
	 *
	 * @param enableCSS true启用 CSS 支持
	 * @return 此链接, 用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection enableCSS(boolean enableCSS);

	/**
	 * 设置 JavaScript 最大运行时间,默认1000毫秒.值为0则不加载JS
	 *
	 * @param millis JS超时时间(毫秒)
	 * @return 此链接, 用于链接
	 */
	@Contract(pure = true)
	public abstract HtmlConnection waitJSTime(int millis);

	/**
	 * 关闭WebClient客户端
	 *
	 * @return 此链接, 用于链接
	 */

	@Contract(pure = true)
	public abstract HtmlConnection close();

	/**
	 * 将请求作为 GET 执行，并解析结果
	 *
	 * @return HTML文档
	 */
	@Contract(pure = true)
	public abstract Document get();

	/**
	 * 将请求作为 POST 执行，并解析结果
	 *
	 * @return HTML文档
	 */
	@Contract(pure = true)
	public abstract Document post();

	/**
	 * 运行程序，获取 响应结果
	 *
	 * @return Response
	 */
	@Contract(pure = true)
	public abstract HtmlResponse execute();

}
