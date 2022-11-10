package org.haic.often.net.http;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.brotli.dec.BrotliInputStream;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.haic.often.net.IgnoreSSLSocket;
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
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * HttpClient工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/2/20 9:51
 */
public class HttpClientUtil {

	private HttpClientUtil() {
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
		private int retry; // 请求异常重试次数
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private int timeout = 10000; // 连接超时时间
		private boolean unlimit;// 请求异常无限重试
		private boolean failThrow; // 错误异常
		private boolean followRedirects = true; // 重定向
		private HttpHost proxy;
		private Method method = Method.GET;
		private Parser parser = Parser.htmlParser();

		private Map<String, String> headers = new HashMap<>(); // 请求头
		private Map<String, String> cookies = new HashMap<>(); // 请求头
		private List<Integer> retryStatusCodes = new ArrayList<>();
		private List<NameValuePair> params = new ArrayList<>();
		private final HttpClientContext context = HttpClientContext.create();
		private CloseableHttpClient httpclient;
		private HttpClientBuilder httpClientBuilder = HttpClients.custom();
		private HttpEntity entity;

		private HttpConnection(@NotNull String url) {
			Logger.getLogger("org.apache.http").setLevel(Level.OFF); // 关闭日志
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
				throw new RuntimeException("Only http & https protocols supported : " + url);
			}
			this.url = url;
			return this;
		}

		@Contract(pure = true)
		public Connection newRequest() {
			entity = null;
			params = new ArrayList<>();
			headers = new HashMap<>();
			method = Method.GET;
			initialization("");
			return Judge.isEmpty(auth) ? this : auth(auth);
		}

		@Contract(pure = true)
		public Connection sslSocketFactory(SSLContext sslSocket) {
			httpClientBuilder.setSSLContext(IgnoreSSLSocket.MyX509TrustManager());
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
		public Connection cookie(@NotNull String name, @NotNull String value) {
			this.cookies.put(name, value);
			return this;
		}

		@Contract(pure = true)
		public Connection cookies(@NotNull Map<String, String> cookies) {
			this.cookies.putAll(cookies);
			return this;
		}

		@Contract(pure = true)
		public Connection setCookies(@NotNull Map<String, String> cookies) {
			this.cookies = new HashMap<>();
			return cookies(cookies);
		}

		@Contract(pure = true)
		public Map<String, String> cookieStore() {
			return cookies;
		}

		@Contract(pure = true)
		public Connection data(@NotNull String key, @NotNull String value) {
			this.params.add(new BasicNameValuePair(key, value));
			return this;
		}

		@Contract(pure = true)
		public Connection data(@NotNull Map<String, String> params) {
			this.params = params.entrySet().stream().map(l -> new BasicNameValuePair(l.getKey(), l.getValue())).collect(Collectors.toList());
			return this;
		}

		@Contract(pure = true)
		public Connection data(@NotNull String key, @NotNull String fileName, @NotNull InputStream inputStream) {
			String boundary = UUID.randomUUID().toString();
			entity = MultipartEntityBuilder.create().addBinaryBody(key, inputStream, ContentType.MULTIPART_FORM_DATA, fileName).setBoundary(boundary).build();
			return contentType("multipart/form-data; boundary=" + boundary);
		}

		@Contract(pure = true)
		public Connection file(@NotNull String fileName, @NotNull InputStream inputStream) {
			return data("file", fileName, inputStream);
		}

		@Contract(pure = true)
		public Connection requestBody(@NotNull String body) {
			try {
				entity = new StringEntity(body);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			header("accept", "application/json;charset=UTF-8");
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
			httpClientBuilder = httpClientBuilder.setConnectionManager(HttpClientHelper.PoolingHttpClientConnectionManager());
			this.context.setAttribute("socks.address", new InetSocketAddress(host, port));
			return this;
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
			this.proxy = new HttpHost(host, port, "HTTP");
			return this;
		}

		@Contract(pure = true)
		public Connection proxy(@NotNull Proxy proxy) {
			String proxyText = proxy.toString();
			if (proxyText.equals("DIRECT")) {
				return this;
			}
			String[] proxyStr = proxyText.substring(proxyText.indexOf(Symbol.SLASH) + 1).split(Symbol.COLON);
			if (proxyText.startsWith("SOCKS")) {
				return socks(proxyStr[0], Integer.parseInt(proxyStr[1]));
			} else {
				return proxy(proxyStr[0], Integer.parseInt(proxyStr[1]));
			}
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
		public Connection failThrow(boolean errorExit) {
			this.failThrow = errorExit;
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
			HttpUriRequest request;
			try {
				URI builder = new URIBuilder(url).setParameters(params).build();
				RequestConfig requestConfig = RequestConfig.custom().setRedirectsEnabled(followRedirects).setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).setProxy(proxy).build();
				entity = entity == null ? new UrlEncodedFormEntity(params) : entity;
				switch (method) {
					case GET -> request = new HttpGet(builder) {{
						setConfig(requestConfig);
					}};
					case POST -> request = new HttpPost(builder) {{
						setConfig(requestConfig);
						setEntity(entity);
					}};
					case PUT -> request = new HttpPut(builder) {{
						setConfig(requestConfig);
						setEntity(entity);
					}};
					case DELETE -> request = new HttpDelete(builder) {{
						setConfig(requestConfig);
					}};
					case HEAD -> request = new HttpHead(builder) {{
						setConfig(requestConfig);
					}};
					case OPTIONS -> request = new HttpOptions(builder) {{
						setConfig(requestConfig);
					}};
					case PATCH -> request = new HttpPatch(builder) {{
						setConfig(requestConfig);
						setEntity(entity);
					}};
					case TRACE -> request = new HttpTrace(builder) {{
						setConfig(requestConfig);
					}};
					default -> throw new RuntimeException("Unknown mode");
				}
			} catch (Exception e) {
				return new HttpResponse(null, null, null);
			}

			// 设置通用的请求属性
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				request.setHeader(entry.getKey(), entry.getValue());
			}

			// 设置cookies
			request.setHeader("cookie", cookies.entrySet().stream().map(l -> l.getKey() + Symbol.EQUALS + l.getValue()).collect(Collectors.joining("; ")));

			httpclient = httpclient == null ? httpClientBuilder.build() : httpclient;

			Response response = executeProgram(request);
			int statusCode = response.statusCode();
			for (int i = 0; (URIUtil.statusIsTimeout(statusCode) || retryStatusCodes.contains(statusCode)) && (i < retry || unlimit); i++) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP); // 程序等待
				response = executeProgram(request);
				statusCode = response.statusCode();
			}
			if (failThrow && !URIUtil.statusIsNormal(statusCode)) {
				throw new RuntimeException("连接URL失败，状态码: " + statusCode + " URL: " + url);
			}
			return response;
		}

		@NotNull
		@Contract(pure = true)
		private Response executeProgram(@NotNull HttpUriRequest request) {
			CloseableHttpResponse httpResponse;
			try {
				httpResponse = httpclient.execute(request, context);
			} catch (IOException e) {
				return new HttpResponse(this, request, null);
			}
			Response response = new HttpResponse(this, request, httpResponse);
			cookies(response.cookies()); // 维护cookies

			String redirectUrl; // 修复重定向
			if (followRedirects && URIUtil.statusIsOK(response.statusCode()) && !Judge.isEmpty(redirectUrl = response.header("location"))) {
				response = url(redirectUrl).execute();
			}

			return response;
		}
	}

	/**
	 * 实现socks代理 HttpClient 类
	 */
	public static class HttpClientHelper {
		/**
		 * 创建 支持socks代理 CloseableHttpClient 实例
		 *
		 * @return CloseableHttpClient实例
		 */
		public static CloseableHttpClient createClient() {
			return HttpClients.custom().setConnectionManager(PoolingHttpClientConnectionManager()).build();
		}

		public static PoolingHttpClientConnectionManager PoolingHttpClientConnectionManager() {
			return new PoolingHttpClientConnectionManager(RegistryBuilder.<ConnectionSocketFactory>create().register("http", new MyConnectionSocketFactory()).register("https", new MySSLConnectionSocketFactory()).build());
		}

		/**
		 * 实现 http 链接的socket 工厂
		 */
		private static class MyConnectionSocketFactory extends PlainConnectionSocketFactory {
			@Override
			public Socket createSocket(HttpContext context) {
				return new Socket(new Proxy(Proxy.Type.SOCKS, (InetSocketAddress) context.getAttribute("socks.address")));
			}
		}

		/**
		 * 实现 https 链接的socket 工厂
		 */
		private static class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {
			public MySSLConnectionSocketFactory() {
				super(SSLContexts.createDefault(), getDefaultHostnameVerifier());
			}

			@Override
			public Socket createSocket(HttpContext context) {
				return new Socket(new Proxy(Proxy.Type.SOCKS, (InetSocketAddress) context.getAttribute("socks.address")));
			}
		}
	}

	/**
	 * 响应接口
	 *
	 * @author haicdust
	 * @version 1.0
	 * @since 2022/3/16 10:33
	 */
	private static class HttpResponse extends Response {

		private final HttpConnection conn;
		private final CloseableHttpResponse res;
		private final HttpUriRequest request;
		private Charset charset;
		private ByteArrayOutputStream body;
		private Map<String, String> headers;
		private Map<String, String> cookies;

		private HttpResponse(HttpConnection conn, HttpUriRequest request, CloseableHttpResponse res) {
			this.conn = conn;
			this.request = request;
			this.res = res;
		}

		@Contract(pure = true)
		public String url() {
			List<URI> reLocs = conn.context.getRedirectLocations();
			return reLocs == null ? request.getURI().toString() : reLocs.get(reLocs.size() - 1).toString();
		}

		@Contract(pure = true)
		public int statusCode() {
			return res == null ? HttpStatus.SC_REQUEST_TIMEOUT : res.getStatusLine().getStatusCode();
		}

		@Contract(pure = true)
		public String statusMessage() {
			return res.getStatusLine().getReasonPhrase();
		}

		@Contract(pure = true)
		public String header(@NotNull String name) {
			return headers().get(name);
		}

		@Contract(pure = true)
		public Map<String, String> headers() {
			if (headers == null) {
				Map<String, String> headers = new HashMap<>();
				for (Header header : res.getAllHeaders()) {
					String name = header.getName().toLowerCase();
					String value = header.getValue();
					if (name.equals("set-cookie") && !value.equals("-")) {
						String cookie = headers.get(name);
						value = value.substring(0, value.indexOf(Symbol.SEMICOLON));
						headers.put(name, cookie == null ? value : cookie + "; " + value);
					} else {
						headers.put(name, value);
					}
				}
				this.headers = headers;
			}
			return headers;
		}

		@Contract(pure = true)
		public Response header(@NotNull String key, @NotNull String value) {
			conn.header(key, value);
			return this;
		}

		@Contract(pure = true)
		public Response removeHeader(@NotNull String key) {
			conn.headers.remove(key);
			return this;
		}

		@Contract(pure = true)
		public String cookie(@NotNull String name) {
			return cookies().get(name);
		}

		@Contract(pure = true)
		public Map<String, String> cookies() {
			return cookies == null ? cookies = res.containsHeader("Set-Cookie") ? Arrays.stream(res.getHeaders("Set-Cookie")).filter(l -> !l.getValue().equals("-")).map(l -> l.getValue().substring(0, l.getValue().indexOf(Symbol.SEMICOLON))).collect(Collectors.toMap(l -> l.substring(0, l.indexOf(Symbol.EQUALS)), l -> l.substring(l.indexOf(Symbol.EQUALS) + 1), (e1, e2) -> e2)) : new HashMap<>() : cookies;
		}

		@Contract(pure = true)
		public Response cookie(@NotNull String name, @NotNull String value) {
			conn.cookie(name, value);
			return this;
		}

		@Contract(pure = true)
		public Response removeCookie(@NotNull String name) {
			conn.cookies.remove(name);
			return this;
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
		public String contentType() {
			return res.getEntity().getContentType().getValue();
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
		public InputStream bodyStream() throws IOException {
			return res.getEntity().getContent();
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
