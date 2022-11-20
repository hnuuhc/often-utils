package org.haic.often.net.http;

import org.brotli.dec.BrotliInputStream;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.haic.often.exception.HttpException;
import org.haic.often.net.IgnoreSSLSocket;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.UserAgent;
import org.haic.often.tuple.ThreeTuple;
import org.haic.often.tuple.Tuple;
import org.haic.often.util.IOUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.util.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Https 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/3/9 14:26
 */
public class HttpsUtil {

	static {
		System.setProperty("http.keepAlive", "false"); // 关闭长连接复用,防止流阻塞
	}

	private HttpsUtil() {
	}

	/**
	 * 公共静态连接连接（ 字符串 网址）<br/> 使用定义的请求 URL 创建一个新的Connection （会话），用于获取和解析 HTML 页面
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static Connection connect(@NotNull String url) {
		return new HttpConnection(url);
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
		return new HttpConnection("");
	}

	private static class HttpConnection extends Connection {

		private String url; // URL
		private String auth; // 身份识别标识
		private String params = ""; // 表格请求参数
		private int retry; // 请求异常重试次数
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private int timeout = 10000; // 超时时间
		private boolean unlimit;// 请求异常无限重试
		private boolean failThrow; // 错误异常
		private boolean followRedirects = true; // 重定向
		private Proxy proxy = Proxy.NO_PROXY; // 代理
		private Method method = Method.GET;
		private Map<String, String> headers = new HashMap<>(); // 请求头
		private Map<String, String> cookies = new HashMap<>(); // cookies
		private List<Integer> retryStatusCodes = new ArrayList<>();
		private ThreeTuple<String, InputStream, String> file;
		private SSLSocketFactory sslSocketFactory = IgnoreSSLSocket.MyX509TrustManager().getSocketFactory();

		private HttpConnection(@NotNull String url) {
			initialization(url);
		}

		@Contract(pure = true)
		private Connection initialization(@NotNull String url) {
			header("accept", "text/html, application/json, application/xhtml+xml;q=0.9, */*;q=0.8");
			header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
			header("accept-encoding", "gzip, deflate, br"); // 允许压缩gzip,br-Brotli
			header("user-agent", UserAgent.chrome()); // 设置随机请求头;
			return url(url);
		}

		@Contract(pure = true)
		public Connection url(@NotNull String url) {
			if (!(url = url.strip()).isEmpty() && !url.startsWith("http")) {
				throw new HttpException("Only http & https protocols supported : " + url);
			}
			if (url.contains(Symbol.QUESTION)) {
				if (url.endsWith(Symbol.QUESTION)) {
					url = url.substring(0, url.length() - 1);
				} else {
					int index = url.indexOf(Symbol.QUESTION);
					url = url.substring(0, index + 1) + StringUtil.lines(url.substring(index + 1), Symbol.AND).map(key -> {
						int keyIndex = key.indexOf(Symbol.EQUALS);
						return key.substring(0, keyIndex + 1) + URIUtil.encodeValue(key.substring(keyIndex + 1));
					}).collect(Collectors.joining(Symbol.AND));
				}
			}
			this.url = url;
			params = "";
			file = null;
			return this;
		}

		@Contract(pure = true)
		public Connection newRequest() {
			params = "";
			file = null;
			headers = new HashMap<>();
			method = Method.GET;
			initialization("");
			return Judge.isEmpty(auth) ? this : auth(auth);
		}

		@Contract(pure = true)
		public Connection sslSocketFactory(SSLContext sslSocket) {
			sslSocketFactory = sslSocket.getSocketFactory();
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
		public Connection followRedirects(boolean followRedirects) {
			this.followRedirects = followRedirects;
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
			this.timeout = millis;
			return this;
		}

		@Contract(pure = true)
		public Connection contentType(@NotNull String type) {
			return header("content-type", type);
		}

		@Contract(pure = true)
		public Connection header(@NotNull String name, @NotNull String value) {
			this.headers.put(name, value);
			return this;
		}

		@Contract(pure = true)
		public Connection headers(@NotNull Map<String, String> headers) {
			this.headers.putAll(headers);
			return this;
		}

		@Contract(pure = true)
		public Connection setHeaders(@NotNull Map<String, String> headers) {
			this.headers = new HashMap<>();
			return headers(headers);
		}

		@Contract(pure = true)
		public Connection removeHeader(@NotNull String key) {
			this.headers.remove(key);
			return this;
		}

		@Contract(pure = true)
		public Connection cookie(@NotNull String name, @NotNull String value) {
			cookies.put(name, value);
			return this;
		}

		@Contract(pure = true)
		public Connection cookies(@NotNull Map<String, String> cookies) {
			cookies.entrySet().removeIf(entry -> entry.getValue() == null);
			this.cookies.putAll(cookies);
			return this;
		}

		@Contract(pure = true)
		public Connection setCookies(@NotNull Map<String, String> cookies) {
			this.cookies = new HashMap<>();
			return cookies(cookies);
		}

		@Contract(pure = true)
		public Connection removeCookie(@NotNull String name) {
			this.cookies.remove(name);
			return this;
		}

		@Contract(pure = true)
		public Map<String, String> cookieStore() {
			return cookies;
		}

		@Contract(pure = true)
		public Connection data(@NotNull String key, @NotNull String value) {
			params += (Judge.isEmpty(params) ? "" : Symbol.AND) + key + Symbol.EQUALS + URIUtil.encodeValue(value);
			return this;
		}

		@Contract(pure = true)
		public Connection data(@NotNull Map<String, String> params) {
			this.params = params.entrySet().stream().filter(l -> l.getValue() != null).map(l -> l.getKey() + Symbol.EQUALS + URIUtil.encodeValue(l.getValue())).collect(Collectors.joining(Symbol.AND));
			return this;
		}

		@Contract(pure = true)
		public Connection data(@NotNull String key, @NotNull String fileName, @NotNull InputStream inputStream) {
			String boundary = UUID.randomUUID().toString();
			file = Tuple.of("\r\n--" + boundary + "\r\n" + "content-disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\ncontent-type: application/octet-stream; charset=utf-8\r\n\r\n", inputStream, "\r\n--" + boundary + "--\r\n");
			return contentType("multipart/form-data; boundary=" + boundary);
		}

		@Contract(pure = true)
		public Connection file(@NotNull String fileName, @NotNull InputStream inputStream) {
			return data("file", fileName, inputStream);
		}

		@Contract(pure = true)
		public Connection requestBody(@NotNull String body) {
			params = body;
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
			this.proxy = proxy;
			return this;
		}

		@Contract(pure = true)
		public Connection method(@NotNull Method method) {
			this.method = method;
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
			Response response = executeProgram(url, params);
			int statusCode = response.statusCode();
			for (int i = 0; (URIUtil.statusIsTimeout(statusCode) || retryStatusCodes.contains(statusCode)) && (i < retry || unlimit); i++) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP); // 程序等待
				response = executeProgram(url, params);
				statusCode = response.statusCode();
			}
			if (failThrow && !URIUtil.statusIsNormal(statusCode)) {
				throw new HttpException("连接URL失败，状态码: " + statusCode + " URL: " + url);
			}
			return response;
		}

		/**
		 * 主程序
		 *
		 * @return this
		 */
		@NotNull
		@Contract(pure = true)
		private Response executeProgram(@NotNull String requestUrl, @NotNull String params) {
			try {
				HttpURLConnection conn;
				switch (method) {
					case GET -> {
						conn = connection(Judge.isEmpty(params) ? requestUrl : requestUrl + (requestUrl.contains(Symbol.QUESTION) ? Symbol.AND : Symbol.QUESTION) + params);
						conn.connect();
					}
					case POST, PUT, PATCH -> {
						conn = connection(requestUrl);
						// 发送POST请求必须设置如下
						conn.setUseCaches(false); // POST请求不能使用缓存（POST不能被缓存）
						conn.setDoOutput(true); // 设置是否向HttpUrlConnction输出，因为这个是POST请求，参数要放在http正文内，因此需要设为true，默认情况下是false
						conn.setDoInput(true); // 设置是否向HttpUrlConnection读入，默认情况下是true
						try (DataOutputStream output = new DataOutputStream(conn.getOutputStream())) {
							if (!Judge.isEmpty(params)) {
								output.writeBytes(params); // 发送请求参数
							}
							if (file != null) { // 发送文件
								output.writeBytes(file.first);
								file.second.transferTo(output);
								output.writeBytes(file.third);
							}
							output.flush(); // flush输出流的缓冲
						} catch (IOException e) {
							conn.disconnect();
							return new HttpResponse(conn, cookies);
						}
					}
					case OPTIONS, DELETE, HEAD, TRACE -> {
						conn = connection(requestUrl);
						conn.connect();
					}
					default -> throw new HttpException("Unknown mode");
				}
				conn.disconnect();
				// 维护cookies
				Map<String, List<String>> headerFields = conn.getHeaderFields();
				List<String> cookies = headerFields.getOrDefault("Set-Cookie", headerFields.get("set-cookie"));
				cookies(cookies == null ? new HashMap<>() : cookies.stream().filter(l -> !l.equals("-")).collect(Collectors.toMap(l -> l.substring(0, l.indexOf(Symbol.EQUALS)), l -> l.substring(l.indexOf(Symbol.EQUALS) + 1, l.indexOf(Symbol.SEMICOLON)), (e1, e2) -> e2)));
				Response res = new HttpResponse(conn, this.cookies);

				String redirectUrl; // 修复重定向
				if (followRedirects && URIUtil.statusIsNormal(res.statusCode()) && !Judge.isEmpty(redirectUrl = res.header("location"))) {
					return executeProgram(URIUtil.getRedirectUrl(requestUrl, redirectUrl), "");
				}
				return res;
			} catch (IOException e) {
				return new HttpResponse(null, cookies);
			}
		}

		/**
		 * 创建HttpURLConnection实例
		 *
		 * @param url url链接
		 * @return HttpURLConnection实例
		 * @throws IOException 如果发生 I/O 异常
		 */
		private HttpURLConnection connection(@NotNull String url) throws IOException {
			HttpURLConnection conn = (HttpURLConnection) URIUtil.getURL(url).openConnection(proxy);
			conn.setRequestProperty("connection", "close");
			conn.setRequestMethod(method.name()); // 请求方法
			conn.setConnectTimeout(timeout < 10000 && !Judge.isEmpty(timeout) ? timeout : 10000); // 连接超时
			conn.setReadTimeout(timeout); // 读取超时
			conn.setInstanceFollowRedirects(false); // 重定向,http和https之间无法遵守重定向
			// https 忽略证书验证
			if (url.startsWith("https")) { // 在握手期间，如果 URL 的主机名和服务器的标识主机名不匹配，则验证机制可以回调此接口的实现程序来确定是否应该允许此连接。
				((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
				((HttpsURLConnection) conn).setHostnameVerifier((arg0, arg1) -> true);
			}
			// 设置通用的请求属性
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
			// 设置cookies
			conn.setRequestProperty("cookie", cookies.entrySet().stream().map(l -> l.getKey() + Symbol.EQUALS + l.getValue()).collect(Collectors.joining("; ")));
			return conn;
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

		private final HttpURLConnection conn;
		private final Map<String, String> cookies;
		private Map<String, String> headers;
		private Parser parser = Parser.htmlParser();
		private Charset charset;
		private ByteArrayOutputStream body;

		private HttpResponse(HttpURLConnection conn, Map<String, String> cookies) {
			this.conn = conn;
			this.cookies = cookies;
		}

		@Contract(pure = true)
		public String url() {
			return conn.getURL().toExternalForm();
		}

		@Contract(pure = true)
		public int statusCode() {
			try {
				return conn.getResponseCode();
			} catch (Exception e) {
				return HttpStatus.SC_REQUEST_TIMEOUT;
			}
		}

		@Contract(pure = true)
		public String statusMessage() {
			try {
				return conn.getResponseMessage();
			} catch (IOException e) {
				return null;
			}
		}

		@Contract(pure = true)
		public String contentType() {
			return conn.getContentType();
		}

		@Contract(pure = true)
		public String header(@NotNull String name) {
			return headers().get(name);
		}

		@Contract(pure = true)
		public Map<String, String> headers() {
			return headers == null ? headers = conn.getHeaderFields().entrySet().stream().filter(l -> l.getKey() != null).collect(Collectors.toMap(l -> l.getKey().toLowerCase(), l -> l.getKey().equalsIgnoreCase("set-cookie") ? l.getValue().stream().filter(v -> !v.equals("-")).map(v -> v.substring(0, v.indexOf(Symbol.SEMICOLON))).collect(Collectors.joining("; ")) : String.join("; ", l.getValue()), (e1, e2) -> e2)) : headers;
		}

		@Contract(pure = true)
		public String cookie(@NotNull String name) {
			return cookies().get(name);
		}

		@Contract(pure = true)
		public Map<String, String> cookies() {
			return cookies;
		}

		@Contract(pure = true)
		public Response charset(@NotNull String charsetName) {
			return charset(Charset.forName(charsetName));
		}

		@Contract(pure = true)
		public Response charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
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
		public Response parser(@NotNull Parser parser) {
			this.parser = parser;
			return this;
		}

		@Contract(pure = true)
		public Document parse() {
			String body = body();
			return body == null ? null : Jsoup.parse(body, parser);
		}

		@Contract(pure = true)
		public String body() {
			return body == null && bodyAsByteArray() == null ? null : body.toString(charset());
		}

		@Contract(pure = true)
		public InputStream bodyStream() throws IOException {
			return URIUtil.statusIsNormal(statusCode()) ? conn.getInputStream() : conn.getErrorStream();
		}

		@Contract(pure = true)
		public byte[] bodyAsBytes() {
			return body == null && (body = bodyAsByteArray()) == null ? null : body.toByteArray();
		}

		@Contract(pure = true)
		public ByteArrayOutputStream bodyAsByteArray() {
			try (InputStream in = bodyStream()) {
				String encoding = header("content-encoding");
				InputStream body = "gzip".equals(encoding) ? new GZIPInputStream(in) : "deflate".equals(encoding) ? new InflaterInputStream(in, new Inflater(true)) : "br".equals(encoding) ? new BrotliInputStream(in) : in;
				this.body = IOUtil.stream(body).toByteArrayOutputStream();
			} catch (Exception e) {
				return null;
			}
			return this.body;
		}

	}

}
