package org.haic.often.net.http;

import org.brotli.dec.BrotliInputStream;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.UserAgent;
import org.haic.often.util.IOUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.util.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Jsoup 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/25 18:40
 */
public class JsoupUtil {

	private JsoupUtil() {
	}

	/**
	 * 公共静态连接连接（ 字符串 网址）<br/>
	 * 使用定义的请求 URL 创建一个新的Connection （会话），用于获取和解析 HTML 页面
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static Connection connect(@NotNull String url) {
		return newSession().url(url);
	}

	/**
	 * 公共静态连接newSession ()
	 * <p>
	 * 创建一个新Connection以用作会话。将为会话维护连接设置（用户代理、超时、URL 等）和 cookie
	 *
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static Connection newSession() {
		return new HttpConnection(Jsoup.newSession());
	}

	private static class HttpConnection extends Connection {
		private String auth; // 身份识别标识
		private int timeout = 10000; // 连接超时时间
		private int retry; // 请求异常重试次数
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private boolean unlimit;// 请求异常无限重试
		private boolean failThrow; // 错误异常

		private List<Integer> retryStatusCodes = new ArrayList<>(); // 重试的错误状态码
		private Parser parser = Parser.htmlParser();

		private org.jsoup.Connection conn;

		private HttpConnection(@NotNull org.jsoup.Connection conn) {
			initialization(conn);
		}

		@Contract(pure = true)
		private Connection initialization(@NotNull org.jsoup.Connection conn) {
			this.conn = conn.maxBodySize(0).timeout(timeout).ignoreContentType(true).ignoreHttpErrors(true);
			header("accept", "text/html, application/json, application/xhtml+xml;q=0.9, */*;q=0.8");
			header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
			header("accept-encoding", "gzip, deflate, br"); // 允许压缩gzip,br-Brotli
			return header("user-agent", UserAgent.chrome()); // 设置随机请求头
		}

		@Contract(pure = true)
		public Connection url(@NotNull String url) {
			if (!(url = url.strip()).isEmpty() && !url.startsWith("http")) {
				throw new RuntimeException("Only http & https protocols supported : " + url);
			}
			conn.url(url);
			return this;
		}

		@Contract(pure = true)
		public Connection newRequest() {
			initialization(conn.newRequest());
			return Judge.isEmpty(auth) ? this : auth(auth);
		}

		@Contract(pure = true)
		public Connection sslSocketFactory(SSLContext sslSocket) {
			conn.sslSocketFactory(sslSocket.getSocketFactory());
			return this;
		}

		@Contract(pure = true)
		public Connection userAgent(@NotNull String userAgent) {
			return header("user-agent", userAgent);
		}

		@Contract(pure = true)
		public Connection isPhone(boolean isPhone) {
			return isPhone ? userAgent(UserAgent.chromeAsPhone()) : userAgent(UserAgent.chrome());
		}

		@Contract(pure = true)
		public Connection request(@NotNull org.jsoup.Connection.Request request) {
			conn.request(request);
			return this;
		}

		@Contract(pure = true)
		public Connection followRedirects(boolean followRedirects) {
			conn.followRedirects(followRedirects);
			return this;
		}

		@Contract(pure = true)
		public Connection referrer(@NotNull String referrer) {
			return header("referer", referrer);
		}

		@Contract(pure = true)
		public Connection auth(@NotNull String auth) {
			return header("authorization", (this.auth = auth.contains(Symbol.SPACE) ? auth : "Bearer " + auth));
		}

		@Contract(pure = true)
		public Connection timeout(int millis) {
			conn.timeout(timeout = millis);
			return this;
		}

		@Contract(pure = true)
		public Connection parser(@NotNull Parser parser) {
			this.parser = parser;
			return this;
		}

		@Contract(pure = true)
		public Connection contentType(@NotNull String type) {
			return header("content-type", type);
		}

		@Contract(pure = true)
		public Connection header(@NotNull String name, @NotNull String value) {
			conn.header(name, value);
			return this;
		}

		@Contract(pure = true)
		public Connection headers(@NotNull Map<String, String> headers) {
			conn.headers(headers);
			return this;
		}

		@Contract(pure = true)
		public Connection setHeaders(@NotNull Map<String, String> headers) {
			conn.request().headers().clear();
			conn.headers(headers);
			return this;
		}

		@Contract(pure = true)
		public Connection cookie(@NotNull String name, @NotNull String value) {
			conn.cookie(name, value);
			return this;
		}

		@Contract(pure = true)
		public Connection cookies(@NotNull Map<String, String> cookies) {
			conn.cookies(cookies);
			return this;
		}

		@Contract(pure = true)
		public Connection setCookies(@NotNull Map<String, String> cookies) {
			conn.request().cookies().clear();
			conn.cookies(cookies);
			return this;
		}

		@Contract(pure = true)
		public Map<String, String> cookieStore() {
			return conn.cookieStore().getCookies().stream().collect(Collectors.toMap(HttpCookie::getName, HttpCookie::getValue, (e1, e2) -> e2));
		}

		@Contract(pure = true)
		public Connection data(String... keyvals) {
			conn.data(keyvals);
			return this;
		}

		@Contract(pure = true)
		public Connection data(@NotNull String key, @NotNull String value) {
			conn.data(key, value);
			return this;
		}

		@Contract(pure = true)
		public Connection data(@NotNull Map<String, String> params) {
			conn.request().data().clear();
			conn.data(params);
			return this;
		}

		@Contract(pure = true)
		public Connection data(@NotNull String key, @NotNull String fileName, @NotNull InputStream inputStream) {
			conn.data(key, fileName, inputStream);
			return this;
		}

		@Contract(pure = true)
		public Connection data(String key, String fileName, InputStream inputStream, String contentType) {
			conn.data(key, fileName, inputStream, contentType);
			return this;
		}

		@Contract(pure = true)
		public Connection file(@NotNull String fileName, @NotNull InputStream inputStream) {
			return data("file", fileName, inputStream);
		}

		@Contract(pure = true)
		public Connection requestBody(@NotNull String body) {
			conn.requestBody(body);
			return StringUtil.isJson(body) ? contentType("application/json;charset=UTF-8") : contentType("application/x-www-form-urlencoded;charset=UTF-8");
		}

		@Contract(pure = true)
		public Connection socks(@NotNull String ipAddr) {
			if (ipAddr.startsWith(Symbol.OPEN_BRACKET)) {
				return socks(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(Symbol.COLON) + 1)));
			} else {
				int index = ipAddr.lastIndexOf(Symbol.COLON);
				return socks(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		@Contract(pure = true)
		public Connection socks(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		}

		@Contract(pure = true)
		public Connection proxy(@NotNull String ipAddr) {
			if (ipAddr.startsWith(Symbol.OPEN_BRACKET)) {
				return proxy(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(Symbol.COLON) + 1)));
			} else {
				int index = ipAddr.lastIndexOf(Symbol.COLON);
				return proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		@Contract(pure = true)
		public Connection proxy(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		}

		@Contract(pure = true)
		public Connection proxy(@NotNull Proxy proxy) {
			conn.proxy(proxy);
			return this;
		}

		@Contract(pure = true)
		public Connection method(@NotNull Method method) {
			conn.method(org.jsoup.Connection.Method.valueOf(method.name()));
			return this;
		}

		@Contract(pure = true)
		public Connection retry(int retry) {
			this.retry = retry;
			return this;
		}

		@Contract(pure = true)
		public Connection retry(int retry, int millis) {
			this.retry = retry;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		@Contract(pure = true)
		public Connection retry(boolean unlimit) {
			this.unlimit = unlimit;
			return this;
		}

		@Contract(pure = true)
		public Connection retry(boolean unlimit, int millis) {
			this.unlimit = unlimit;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		@Contract(pure = true)
		public Connection retryStatusCodes(int... statusCode) {
			retryStatusCodes = Arrays.stream(statusCode).boxed().toList();
			return this;
		}

		@Contract(pure = true)
		public Connection retryStatusCodes(List<Integer> retryStatusCodes) {
			this.retryStatusCodes = retryStatusCodes;
			return this;
		}

		@Contract(pure = true)
		public Connection failThrow(boolean exit) {
			failThrow = exit;
			return this;
		}

		@Contract(pure = true)
		public Document get() {
			return method(Method.GET).execute().parse();
		}

		@Contract(pure = true)
		public Document post() {
			return method(Method.POST).execute().parse();
		}

		@NotNull
		@Contract(pure = true)
		public Response execute() {
			Response res = executeProgram(conn);
			int statusCode = res.statusCode();
			for (int i = 0; (URIUtil.statusIsTimeout(statusCode) || retryStatusCodes.contains(statusCode)) && (i < retry || unlimit); i++) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP); // 程序等待
				res = executeProgram(conn);
				statusCode = res.statusCode();
			}
			if (failThrow && !URIUtil.statusIsNormal(statusCode)) {
				throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + conn.request().url());
			}
			return res;
		}

		@NotNull
		@Contract(pure = true)
		private Response executeProgram(@NotNull org.jsoup.Connection conn) {
			org.jsoup.Connection.Response res;
			try {
				res = conn.execute();
			} catch (IOException e) {
				return new HttpResponse(this, null);
			}
			return new HttpResponse(this, res);
		}
	}

	/**
	 * 响应接口
	 *
	 * @author haicdust
	 * @version 1.0
	 * @since 2022/3/16 10:28
	 */
	private static class HttpResponse extends Response {

		private final HttpConnection conn;
		private final org.jsoup.Connection.Response res;
		private Charset charset;
		private ByteArrayOutputStream body;
		private Map<String, String> headers;
		private Map<String, String> cookies;

		private HttpResponse(HttpConnection conn, org.jsoup.Connection.Response res) {
			this.conn = conn;
			this.res = res;
		}

		@Contract(pure = true)
		public String url() {
			return res.url().toExternalForm();
		}

		@Contract(pure = true)
		public int statusCode() {
			return res == null ? HttpStatus.SC_REQUEST_TIMEOUT : res.statusCode();
		}

		@Contract(pure = true)
		public String statusMessage() {
			return res.statusMessage();
		}

		@Contract(pure = true)
		public String header(@NotNull String name) {
			return headers().get(name);
		}

		@Contract(pure = true)
		public Map<String, String> headers() {
			if (headers == null) {
				headers = res.headers().entrySet().stream().collect(Collectors.toMap(l -> l.getKey().toLowerCase(), Map.Entry::getValue));
				headers.put("set-cookie", cookies().entrySet().stream().map(l -> l.getKey() + Symbol.EQUALS + l.getValue()).collect(Collectors.joining("; ")));
			}
			return headers;
		}

		@Contract(pure = true)
		public Response header(@NotNull String key, @NotNull String value) {
			res.header(key, value);
			return this;
		}

		@Contract(pure = true)
		public Response removeHeader(@NotNull String key) {
			res.removeHeader(key);
			return this;
		}

		@Contract(pure = true)
		public String cookie(@NotNull String name) {
			return res.cookie(name);
		}

		@Contract(pure = true)
		public Map<String, String> cookies() {
			return cookies == null ? cookies = res.cookies() : cookies;
		}

		@Contract(pure = true)
		public Response cookie(@NotNull String name, @NotNull String value) {
			res.cookie(name, value);
			return this;
		}

		@Contract(pure = true)
		public Response removeCookie(@NotNull String name) {
			res.removeCookie(name);
			return this;
		}

		@Contract(pure = true)
		public Response charset(@NotNull String charsetName) {
			return charset(Charset.forName(charsetName));
		}

		@Contract(pure = true)
		public Response charset(@NotNull Charset charset) {
			this.charset = charset;
			return charset(charset.name());
		}

		@Contract(pure = true)
		public Charset charset() {
			if (charset == null) {
				if (headers().containsKey("content-type")) {
					String type = headers().get("content-type");
					if (type.contains(Symbol.SEMICOLON)) {
						return charset = Charset.forName(type.substring(type.lastIndexOf(Symbol.EQUALS) + 1));
					} else if (!type.contains("html")) {
						return charset = StandardCharsets.UTF_8;
					}
				}
				charset = URIUtil.encoding(bodyAsBytes());
			}
			return charset;
		}

		@Contract(pure = true)
		public String contentType() {
			return res.contentType();
		}

		@Contract(pure = true)
		public Document parse() {
			String body = body();
			return body == null ? null : Jsoup.parse(body, conn.parser);
		}

		@Contract(pure = true)
		public String body() {
			return body == null && bodyAsByteArray() == null ? null : body.toString(charset());
		}

		@Contract(pure = true)
		public InputStream bodyStream() {
			return res.bodyStream();
		}

		@Contract(pure = true)
		public byte[] bodyAsBytes() {
			return body == null && (body = bodyAsByteArray()) == null ? null : body.toByteArray();
		}

		@Contract(pure = true)
		public ByteArrayOutputStream bodyAsByteArray() {
			try (InputStream in = bodyStream()) {
				String encoding = header("content-encoding");
				InputStream body = "br".equals(encoding) ? new BrotliInputStream(in) : in;
				this.body = IOUtil.stream(body).toByteArrayOutputStream();
			} catch (Exception e) {
				return null;
			}
			return this.body;
		}

		@Contract(pure = true)
		public Response method(@NotNull Method method) {
			conn.method(method);
			return this;
		}

	}

}
