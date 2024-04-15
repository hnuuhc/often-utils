package org.haic.often.net.http;

import org.jetbrains.annotations.NotNull;
import org.haic.often.exception.HttpException;
import org.haic.often.net.URIUtil;
import org.haic.often.parser.csv.CSV;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.parser.xml.Document;
import org.haic.often.util.TypeUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 响应接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/16 11:52
 */
public abstract class Response {

	protected Map<String, String> headers;
	protected Map<String, String> cookies;
	protected Charset charset;
	protected ByteArrayOutputStream body;

	/**
	 * 返回此页面的 URL
	 *
	 * @return 此页面的 URL
	 */
	public abstract String url();

	/**
	 * 获取响应的状态码
	 *
	 * @return 请求响应代码
	 */
	public abstract int statusCode();

	/**
	 * 获取与响应代码一起从服务器返回的 HTTP 响应消息（如果有）。来自以下回复：
	 * <blockquote>
	 * <pre>	HTTP/1.0 200 OK</pre>
	 * <pre>	HTTP/1.0 404 Not Found</pre>
	 * </blockquote>
	 * 分别提取字符串“OK”和“Not Found”。如果无法从响应中辨别出任何内容（结果不是有效的 HTTP），则返回 null。
	 *
	 * @return 状态消息
	 */
	public abstract String statusMessage();

	/**
	 * 获取响应内容类型（例如“text/html”）
	 *
	 * @return 响应内容类型，如果未设置则为null
	 */
	public abstract String contentType();

	/**
	 * 获取 请求头的值
	 *
	 * @return 请求头的值
	 */
	public String header(@NotNull String name) {
		return headers().get(name);
	}

	/**
	 * 获取 请求头
	 *
	 * @return 请求头
	 */
	public abstract Map<String, String> headers();

	/**
	 * 获取 cookie
	 *
	 * @param name cookie name
	 * @return cookie value
	 */
	public String cookie(@NotNull String name) {
		return cookies().get(name);
	}

	/**
	 * 获取 cookies
	 *
	 * @return cookies
	 */
	public abstract Map<String, String> cookies();

	/**
	 * Response字符集（ 字符串 字符集）<br/>
	 * 设置/覆盖响应字符集。解析文档正文时，它将使用此字符集。
	 *
	 * @param charsetName 字符集格式名称
	 * @return 此连接，用于链接
	 */
	public Response charset(@NotNull String charsetName) {
		return charset(Charset.forName(charsetName));
	}

	/**
	 * Response字符集（ 字符串 字符集）<br/>
	 * 设置/覆盖响应字符集。解析文档正文时，它将使用此字符集。
	 *
	 * @param charset 字符集格式
	 * @return 此连接，用于链接
	 */
	public Response charset(@NotNull Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 获取当前Response字符集编码<br/>
	 *
	 * @return 字符集编码
	 */
	public Charset charset() {
		if (charset == null) {
			if (headers().containsKey("content-type")) {
				var type = headers().get("content-type");
				if (type.contains(";")) return charset = Charset.forName(type.substring(type.lastIndexOf("=") + 1));
				else if (type.contains("html")) { // 网页为html且未在连接类型中获取字符集编码格式
					if (bodyAsByteArray() == null) throw new HttpException("解析字符集编码时出错,未能获取网页数据");
					var metas = Document.parse(body.toString(StandardCharsets.UTF_8)).select("@head @meta");
					var meta = metas.stream().filter(e -> e.containsAttr("charset")).findFirst().orElse(null);
					if (meta != null) return charset = Charset.forName(meta.attr("charset")); // 判断方式 meta[charset]
					meta = metas.stream().filter(e -> e.containsAttrValue("http-equiv", "Content-Type")).findFirst().orElse(null);
					if (meta == null) return charset = URIUtil.encoding(bodyAsBytes()); // meta未成功获取,则在本地判断UTF8或GBK
					var content = meta.attr("content"); // 判断方式 meta[http-equiv=Content-Type]
					return charset = Charset.forName(content.substring(content.lastIndexOf("=") + 1));
				}
			}
			charset = StandardCharsets.UTF_8;
		}
		return charset;
	}

	/**
	 * 读取响应的正文并将其解析为CSV,如果连接超时或IO异常会返回null
	 *
	 * @return 已解析的CSV
	 */
	public CSV csv() {
		return parse(CSV.class);
	}

	/**
	 * 读取响应的正文并将其解析为JSON,如果连接超时或IO异常会返回null
	 *
	 * @return 已解析的JSON
	 */
	public JSONObject json() {
		return parse(JSONObject.class);
	}

	/**
	 * 读取响应的正文并将其解析为JSON,如果连接超时或IO异常会返回null
	 *
	 * @return 已解析的JSON
	 */
	public JSONArray jsonArray() {
		return parse(JSONArray.class);
	}

	/**
	 * 读取响应的正文并将其解析为XML或HTML,如果连接超时或IO异常会返回null
	 *
	 * @return 已解析的XML或HTML
	 */
	public Document xml() {
		return parse(Document.class);
	}

	/**
	 * 读取响应的正文并将其解析为Yaml,如果连接超时或IO异常会返回null
	 *
	 * @return 已解析的Yaml
	 */
	public Yaml yaml() {
		return parse(Yaml.class);
	}

	/**
	 * 读取响应的正文并将其解析为文档,如果连接超时或IO异常会返回null
	 *
	 * @return 已解析的文档
	 */
	public Document parse() {
		return parse(Document.class);
	}

	/**
	 * 读取响应的正文并将其解析,如果连接超时或IO异常会返回null
	 *
	 * @param itemClass 返回解析类型
	 * @param <T>       解析类型
	 * @return 已解析的文档
	 */
	public <T> T parse(@NotNull Class<T> itemClass) {
		return TypeUtil.convert(body(), itemClass);
	}

	/**
	 * Get the body of the response as a plain string.
	 *
	 * @return body
	 */
	public String body() {
		return body == null && bodyAsByteArray() == null ? null : body.toString(charset());
	}

	/**
	 * Get the body of the response as a (buffered) InputStream. You should close the input stream when you're done with it.
	 * Other body methods (like bufferUp, body, parse, etc.) will not work in conjunction with this method.
	 * <p>This method is useful for writing large responses to disk, without buffering them completely into memory first.</p>
	 *
	 * @return the response body input stream
	 */
	public abstract InputStream bodyStream() throws IOException;

	/**
	 * Get the body of the response as an array of bytes.
	 *
	 * @return body bytes
	 */
	public byte[] bodyAsBytes() {
		return body == null && (body = bodyAsByteArray()) == null ? null : body.toByteArray();
	}

	/**
	 * Get And Update Body ByteArrayOutputStream
	 *
	 * @return ByteArrayOutputStream
	 */
	protected abstract ByteArrayOutputStream bodyAsByteArray();

	/**
	 * 关闭当前会话,如果不获取返回数据,例:{@link #body()},则会导致socket通信堆积,对于一些服务器可能导致429错误
	 */
	public abstract void close();

}
