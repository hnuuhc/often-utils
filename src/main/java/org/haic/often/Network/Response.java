package org.haic.often.Network;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * 响应接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/16 11:52
 */
public abstract class Response {

	/**
	 * 返回此页面的 URL
	 *
	 * @return 此页面的 URL
	 */
	@Contract(pure = true) public abstract String url();

	/**
	 * 获取响应的状态码
	 *
	 * @return 请求响应代码
	 */
	@Contract(pure = true) public abstract int statusCode();

	/**
	 * 获取与响应代码一起从服务器返回的 HTTP 响应消息（如果有）。来自以下回复：
	 * <p>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;HTTP/1.0 200 OK
	 * <p>
	 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;HTTP/1.0 404 Not Found
	 * <p>
	 * 分别提取字符串“OK”和“Not Found”。如果无法从响应中辨别出任何内容（结果不是有效的 HTTP），则返回 null。
	 *
	 * @return 状态消息
	 */
	@Contract(pure = true) public abstract String statusMessage();

	/**
	 * 获取 请求头的值
	 *
	 * @return 请求头的值
	 */
	@Contract(pure = true) public abstract String header(@NotNull String name);

	/**
	 * 获取 请求头
	 *
	 * @return 请求头
	 */
	@Contract(pure = true) public abstract Map<String, String> headers();

	/**
	 * 在此请求/响应中设置 header。
	 *
	 * @param key   header的键
	 * @param value header的值
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true) public abstract Response header(@NotNull String key, @NotNull String value);

	/**
	 * 删除在此请求/响应中设置 header。
	 *
	 * @param key header的键
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true) public abstract Response removeHeader(@NotNull String key);

	/**
	 * 获取 cookie
	 *
	 * @param name cookie name
	 * @return cookie value
	 */
	@Contract(pure = true) public abstract String cookie(@NotNull String name);

	/**
	 * 获取 cookies
	 *
	 * @return cookies
	 */
	@Contract(pure = true) public abstract Map<String, String> cookies();

	/**
	 * @param name  cookie的名称
	 * @param value cookie的值
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true) public abstract Response cookie(@NotNull String name, @NotNull String value);

	/**
	 * 删除在此请求/响应中设置 cookie。
	 *
	 * @param name cookie的名称
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true) public abstract Response removeCookie(@NotNull String name);

	/**
	 * Response字符集（ 字符串 字符集）<br/>
	 * 设置/覆盖响应字符集。解析文档正文时，它将使用此字符集。
	 *
	 * @param charsetName 字符集格式名称
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true) public abstract Response charset(@NotNull String charsetName);

	/**
	 * Response字符集（ 字符串 字符集）<br/>
	 * 设置/覆盖响应字符集。解析文档正文时，它将使用此字符集。
	 *
	 * @param charset 字符集格式
	 * @return 此连接，用于链接
	 */

	@Contract(pure = true) public abstract Response charset(@NotNull Charset charset);

	/**
	 * 获取响应内容类型（例如“text/html”）
	 *
	 * @return 响应内容类型，如果未设置则为null
	 */
	@Contract(pure = true) public abstract String contentType();

	/**
	 * 读取响应的正文并将其解析为文档,如果连接超时或IO异常会返回null
	 *
	 * @return 已解析的文档
	 */
	@Contract(pure = true) public abstract Document parse();

	/**
	 * Get the body of the response as a plain string.
	 *
	 * @return body
	 */
	@Contract(pure = true) public abstract String body();

	/**
	 * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
	 * Other body methods (like bufferUp, body, parse, etc) will not work in conjunction with this method.
	 * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
	 *
	 * @return the response body input stream
	 */
	@Contract(pure = true) public abstract InputStream bodyStream() throws IOException;

	/**
	 * Get the body of the response as an array of bytes.
	 *
	 * @return body bytes
	 */
	@Contract(pure = true) public abstract byte[] bodyAsBytes();

	/**
	 * 设置请求方式
	 * <p>
	 * method - 新方法
	 *
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true) public abstract Response method(@NotNull Method method);

}