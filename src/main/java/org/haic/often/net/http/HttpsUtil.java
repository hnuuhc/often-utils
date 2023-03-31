package org.haic.often.net.http;

import org.brotli.dec.BrotliInputStream;
import org.haic.often.Judge;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.HttpException;
import org.haic.often.net.IgnoreSSLSocket;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.UserAgent;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.tuple.Tuple;
import org.haic.often.tuple.record.ThreeTuple;
import org.haic.often.util.IOUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.util.ThreadUtil;

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
		private SSLSocketFactory sslSocketFactory = IgnoreSSLSocket.IgnoreSSLContext().getSocketFactory();

		private HttpConnection(@NotNull String url) {
			initialization(url);
		}

		private void initialization(@NotNull String url) {
			header("accept", "text/html, application/json, application/xhtml+xml;q=0.9, */*;q=0.8");
			header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
			header("accept-encoding", "gzip, deflate, br"); // 允许压缩gzip,br-Brotli
			header("user-agent", UserAgent.chrome()).url(url);// 设置随机请求头;
		}

		public Connection url(@NotNull String url) {
			if (!(url = url.strip()).isEmpty() && !url.startsWith("http")) {
				throw new HttpException("Only http & https protocols supported : " + url);
			}
			if ((url = url.contains("#") ? url.substring(0, url.indexOf("#")) : url).contains("?")) {
				if (url.endsWith("?")) {
					url = url.substring(0, url.length() - 1);
				} else {
					int index = url.indexOf("?");
					url = url.substring(0, index + 1) + StringUtil.lines(url.substring(index + 1), "&").map(key -> {
						int keyIndex = key.indexOf("=");
						return key.substring(0, keyIndex + 1) + URIUtil.encodeValue(key.substring(keyIndex + 1));
					}).collect(Collectors.joining("&"));
				}
			}
			this.url = url;
			params = "";
			return this;
		}

		public Connection newRequest() {
			params = "";
			file = null;
			headers = new HashMap<>();
			method = Method.GET;
			initialization("");
			return Judge.isEmpty(auth) ? this : auth(auth);
		}

		public Connection sslSocketFactory(SSLContext sslSocket) {
			sslSocketFactory = sslSocket.getSocketFactory();
			return this;
		}

		public Connection userAgent(@NotNull String userAgent) {
			return header("user-agent", userAgent);
		}

		public Connection isPhone(boolean isPhone) {
			return isPhone ? userAgent(UserAgent.chromeAsPhone()) : userAgent(UserAgent.chrome());
		}

		public Connection followRedirects(boolean followRedirects) {
			this.followRedirects = followRedirects;
			return this;
		}

		public Connection referrer(@NotNull String referrer) {
			return header("referer", referrer);
		}

		public Connection auth(@NotNull String auth) {
			return header("authorization", (this.auth = auth.contains(" ") ? auth : "Bearer " + auth));
		}

		public Connection timeout(int millis) {
			this.timeout = millis;
			return this;
		}

		public Connection contentType(@NotNull String type) {
			return header("content-type", type);
		}

		public Connection header(@NotNull String name, @NotNull String value) {
			this.headers.put(name, value);
			return this;
		}

		public Connection headers(@NotNull Map<String, String> headers) {
			this.headers.putAll(headers);
			return this;
		}

		public Connection setHeaders(@NotNull Map<String, String> headers) {
			this.headers = new HashMap<>();
			return headers(headers);
		}

		public Connection removeHeader(@NotNull String key) {
			this.headers.remove(key);
			return this;
		}

		public Connection cookie(@NotNull String name, @NotNull String value) {
			cookies.put(name, value);
			return this;
		}

		public Connection cookies(@NotNull Map<String, String> cookies) {
			cookies.entrySet().removeIf(entry -> entry.getValue() == null);
			this.cookies.putAll(cookies);
			return this;
		}

		public Connection setCookies(@NotNull Map<String, String> cookies) {
			this.cookies = new HashMap<>();
			return cookies(cookies);
		}

		public Connection removeCookie(@NotNull String name) {
			this.cookies.remove(name);
			return this;
		}

		public Map<String, String> cookieStore() {
			return cookies;
		}

		public Connection data(@NotNull String key, @NotNull String value) {
			params += (Judge.isEmpty(params) ? "" : "&") + key + "=" + URIUtil.encodeValue(value);
			return this;
		}

		public Connection data(@NotNull Map<String, String> params) {
			this.params = params.entrySet().stream().filter(l -> l.getValue() != null).map(l -> l.getKey() + "=" + URIUtil.encodeValue(l.getValue())).collect(Collectors.joining("&"));
			return this;
		}

		public Connection data(@NotNull InputStream in) {
			file = Tuple.of("", in, "");
			return this;
		}

		public Connection data(@NotNull InputStream in, @NotNull String mimiType) {
			file = Tuple.of("", in, "");
			return contentType(mimiType);
		}

		public Connection data(@NotNull String key, @NotNull String fileName, @NotNull InputStream in) {
			return data(key, key, in, "multipart/form-data");
		}

		public Connection data(@NotNull String key, @NotNull String fileName, @NotNull InputStream inputStream, @NotNull String mimiType) {
			var boundary = UUID.randomUUID().toString();
			file = Tuple.of("\r\n--" + boundary + "\r\n" + "content-disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\ncontent-type: application/octet-stream; charset=utf-8\r\n\r\n", inputStream, "\r\n--" + boundary + "--\r\n");
			return contentType(mimiType);
		}

		public Connection requestBody(@NotNull Object body) {
			if (body instanceof JSONObject json) {
				this.params = json.toJSONString();
				return contentType("application/json;charset=UTF-8");
			}
			return requestBody(String.valueOf(body));
		}

		public Connection requestBody(@NotNull String body) {
			this.params = body;
			return StringUtil.isJson(body) ? contentType("application/json;charset=UTF-8") : contentType("application/x-www-form-urlencoded;charset=UTF-8");
		}

		public Connection socks(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		}

		public Connection proxy(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		}

		public Connection proxy(@NotNull Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		public Connection method(@NotNull Method method) {
			this.method = method;
			return this;
		}

		public Connection retry(int retry) {
			this.retry = retry;
			return this;
		}

		public Connection retry(int retry, int millis) {
			this.retry = retry;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		public Connection retry(boolean unlimit) {
			this.unlimit = unlimit;
			return this;
		}

		public Connection retry(boolean unlimit, int millis) {
			this.unlimit = unlimit;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		public Connection retryStatusCodes(int... statusCode) {
			retryStatusCodes = Arrays.stream(statusCode).boxed().toList();
			return this;
		}

		public Connection retryStatusCodes(List<Integer> retryStatusCodes) {
			this.retryStatusCodes = retryStatusCodes;
			return this;
		}

		public Connection failThrow(boolean exit) {
			failThrow = exit;
			return this;
		}

		@NotNull
		public Response execute() {
			var response = executeProgram(url, method, params);
			int statusCode = response.statusCode();
			for (int i = 0; (URIUtil.statusIsTimeout(statusCode) || retryStatusCodes.contains(statusCode)) && (i < retry || unlimit); i++) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP); // 程序等待
				response = executeProgram(url, method, params);
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
		private Response executeProgram(@NotNull String requestUrl, @NotNull Method method, @NotNull String params) {
			try {
				HttpURLConnection conn;
				switch (method) {
					case GET -> {
						conn = connection(Judge.isEmpty(params) ? requestUrl : requestUrl + (requestUrl.contains("?") ? "&" : "?") + params);
						conn.connect();
					}
					case POST, PUT, PATCH -> {
						conn = connection(requestUrl);
						// 发送POST请求必须设置如下
						conn.setUseCaches(false); // POST请求不能使用缓存（POST不能被缓存）
						conn.setDoOutput(true); // 设置是否向HttpUrlConnction输出，因为这个是POST请求，参数要放在http正文内，因此需要设为true，默认情况下是false
						conn.setDoInput(true); // 设置是否向HttpUrlConnection读入，默认情况下是true
						try (var output = new DataOutputStream(conn.getOutputStream())) {
							if (!Judge.isEmpty(params)) {
								output.write(params.getBytes()); // 发送请求参数
							}
							if (file != null) { // 发送文件
								output.writeBytes(file.first());
								//noinspection resource
								file.second().transferTo(output);
								output.writeBytes(file.third());
								file = null; // 删除流,防止复用
								removeHeader("content-type");
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
				var headerFields = conn.getHeaderFields();
				var cookies = headerFields.getOrDefault("Set-Cookie", headerFields.get("set-cookie"));
				cookies(cookies == null ? new HashMap<>() : cookies.stream().filter(l -> !l.equals("-") && !l.isBlank()).collect(Collectors.toMap(l -> l.substring(0, l.indexOf("=")), l -> l.substring(l.indexOf("=") + 1, l.indexOf(";")), (e1, e2) -> e2)));
				var res = new HttpResponse(conn, this.cookies);

				String redirectUrl; // 修复重定向
				if (followRedirects && URIUtil.statusIsNormal(res.statusCode()) && !Judge.isEmpty(redirectUrl = res.header("location"))) {
					return executeProgram(URIUtil.toAbsoluteUrl(requestUrl, redirectUrl), Method.GET, "");  // 跳转修正为GET
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
			var conn = (HttpURLConnection) URIUtil.getURL(url).openConnection(proxy);
			conn.setRequestProperty("connection", "close");
			conn.setRequestMethod(method.name()); // 请求方法
			conn.setConnectTimeout(timeout < 10000 && timeout != 0 ? timeout : 10000); // 连接超时
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
			conn.setRequestProperty("cookie", cookies.entrySet().stream().map(l -> l.getKey() + "=" + l.getValue()).collect(Collectors.joining("; ")));
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

		private HttpResponse(HttpURLConnection conn, Map<String, String> cookies) {
			this.conn = conn;
			this.cookies = cookies;
		}

		public String url() {
			return conn.getURL().toExternalForm();
		}

		public int statusCode() {
			try {
				return conn.getResponseCode();
			} catch (Exception e) {
				return HttpStatus.SC_REQUEST_TIMEOUT;
			}
		}

		public String statusMessage() {
			try {
				return conn.getResponseMessage();
			} catch (IOException e) {
				return null;
			}
		}

		public String contentType() {
			return conn.getContentType();
		}

		public Map<String, String> headers() {
			return headers == null ? headers = conn.getHeaderFields().entrySet().stream().filter(l -> l.getKey() != null).collect(Collectors.toMap(l -> l.getKey().toLowerCase(), l -> new String((l.getKey().equalsIgnoreCase("set-cookie") ? l.getValue().stream().filter(v -> !v.equals("-") && !v.isBlank()).map(v -> v.substring(0, v.indexOf(";"))).collect(Collectors.joining("; ")) : String.join("; ", l.getValue())).getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8), (e1, e2) -> e2)) : headers;
		}

		public Map<String, String> cookies() {
			return cookies;
		}

		public InputStream bodyStream() throws IOException {
			return URIUtil.statusIsNormal(statusCode()) ? conn.getInputStream() : conn.getErrorStream();
		}

		protected ByteArrayOutputStream bodyAsByteArray() {
			if (this.body != null) return this.body;
			try (InputStream in = bodyStream()) {
				var encoding = header("content-encoding");
				InputStream body = "gzip".equals(encoding) ? new GZIPInputStream(in) : "deflate".equals(encoding) ? new InflaterInputStream(in, new Inflater(true)) : "br".equals(encoding) ? new BrotliInputStream(in) : in;
				return this.body = IOUtil.stream(body).toByteArrayOutputStream();
			} catch (Exception e) {
				return null;
			}
		}

	}

}
