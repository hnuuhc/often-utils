package org.haic.often.net.http;

import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
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
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import org.brotli.dec.BrotliInputStream;
import org.haic.often.Judge;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.HttpException;
import org.haic.often.net.IgnoreSSLSocket;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.UserAgent;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.IOUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.util.ThreadUtil;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
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

	static {  // 屏蔽HtmlUnit日志
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
	}

	private HttpClientUtil() {
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
		private int retry; // 请求异常重试次数
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private int timeout = 10000; // 连接超时时间
		private boolean unlimit;// 请求异常无限重试
		private boolean failThrow; // 错误异常
		private boolean followRedirects = true; // 重定向
		private HttpHost proxy;
		private Method method = Method.GET;
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

		private void initialization(@NotNull String url) {
			header("accept", "text/html, application/json, application/xhtml+xml;q=0.9, */*;q=0.8");
			header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
			header("accept-encoding", "gzip, deflate, br"); // 允许压缩gzip,br-Brotli
			header("user-agent", UserAgent.chrome()).url(url); // 设置随机请求头;
		}

		public Connection url(@NotNull String url) {
			if (!(url = url.strip()).isEmpty() && !url.startsWith("http")) {
				throw new HttpException("Only http & https protocols supported : " + url);
			}
			this.url = url;
			return this;
		}

		public Connection newRequest() {
			entity = null;
			params = new ArrayList<>();
			headers = new HashMap<>();
			method = Method.GET;
			initialization("");
			return Judge.isEmpty(auth) ? this : auth(auth);
		}

		public Connection sslSocketFactory(SSLContext sslSocket) {
			httpClientBuilder.setSSLContext(IgnoreSSLSocket.ignoreSSLContext());
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
			this.cookies.put(name, value);
			return this;
		}

		public Connection cookies(@NotNull Map<String, String> cookies) {
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
			this.params.add(new BasicNameValuePair(key, value));
			return this;
		}

		public Connection data(@NotNull Map<String, String> params) {
			this.params = params.entrySet().stream().map(l -> new BasicNameValuePair(l.getKey(), l.getValue())).collect(Collectors.toList());
			return this;
		}

		public Connection data(@NotNull InputStream in) {
			return data(in, "multipart/form-data");
		}

		public Connection data(@NotNull InputStream in, @NotNull String mimiType) {
			entity = MultipartEntityBuilder.create().addBinaryBody("", in).build();
			return contentType(mimiType);
		}

		public Connection data(@NotNull String key, @NotNull String fileName, @NotNull InputStream in) {
			return data(key, fileName, in, "multipart/form-data");
		}

		public Connection data(@NotNull String key, @NotNull String fileName, @NotNull InputStream in, @NotNull String mimiType) {
			String boundary = UUID.randomUUID().toString();
			entity = MultipartEntityBuilder.create().addBinaryBody(key, in, ContentType.APPLICATION_OCTET_STREAM, fileName).setBoundary(boundary).build();
			return contentType(mimiType);
		}

		public Connection requestBody(@NotNull Object body) {
			if (body instanceof JSONObject json) {
				try {
					entity = new StringEntity(json.toJSONString());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				return contentType("application/json;charset=UTF-8");
			}
			return requestBody(String.valueOf(body));
		}

		public Connection requestBody(@NotNull String body) {
			try {
				entity = new StringEntity(body);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return StringUtil.isJson(body) ? contentType("application/json;charset=UTF-8") : contentType("application/x-www-form-urlencoded;charset=UTF-8");
		}

		public Connection socks(@NotNull String host, int port) {
			httpClientBuilder = httpClientBuilder.setConnectionManager(HttpClientHelper.PoolingHttpClientConnectionManager());
			this.context.setAttribute("socks.address", new InetSocketAddress(host, port));
			return this;
		}

		public Connection socks(@NotNull String host, int port, @NotNull String user, @NotNull String password) {
			socks(host, port);
			var provider = new BasicCredentialsProvider();
			provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(user, password));
			context.setCredentialsProvider(provider);
			return this;
		}

		public Connection proxy(@NotNull String host, int port) {
			this.proxy = new HttpHost(host, port, "HTTP");
			return this;
		}

		public Connection proxy(@NotNull String host, int port, @NotNull String user, @NotNull String password) {
			proxy(host, port);
			var provider = new BasicCredentialsProvider();
			provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials(user, password));
			context.setCredentialsProvider(provider);
			return this;
		}

		public Connection proxy(@NotNull Proxy proxy) {
			String proxyText = proxy.toString();
			if (proxyText.equals("DIRECT")) {
				return this;
			}
			String[] proxyStr = proxyText.substring(proxyText.indexOf("/") + 1).split(":");
			if (proxyText.startsWith("SOCKS")) {
				return socks(proxyStr[0], Integer.parseInt(proxyStr[1]));
			} else {
				return proxy(proxyStr[0], Integer.parseInt(proxyStr[1]));
			}
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

		public Connection failThrow(boolean errorExit) {
			this.failThrow = errorExit;
			return this;
		}

		public Response get() {
			return method(Method.GET).execute();
		}

		public Response post() {
			return method(Method.POST).execute();
		}

		@NotNull

		public Response execute() {
			HttpUriRequest request;
			try {
				var builder = new URIBuilder(url).setParameters(params).build();
				var requestConfig = RequestConfig.custom().setRedirectsEnabled(followRedirects).setConnectTimeout(timeout).setConnectionRequestTimeout(timeout).setSocketTimeout(timeout).setProxy(proxy).build();
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
					default -> throw new HttpException("Unknown mode");
				}
			} catch (Exception e) {
				return new HttpResponse(null, context, null);
			}

			// 设置通用的请求属性
			for (var entry : headers.entrySet()) {
				request.setHeader(entry.getKey(), entry.getValue());
			}

			// 设置cookies
			request.setHeader("cookie", cookies.entrySet().stream().map(l -> l.getKey() + "=" + l.getValue()).collect(Collectors.joining("; ")));

			httpclient = httpclient == null ? httpClientBuilder.build() : httpclient;

			Response response = executeProgram(request);
			int statusCode = response.statusCode();
			for (int i = 0; (URIUtil.statusIsTimeout(statusCode) || retryStatusCodes.contains(statusCode)) && (i < retry || unlimit); i++) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP); // 程序等待
				response = executeProgram(request);
				statusCode = response.statusCode();
			}
			if (failThrow && !URIUtil.statusIsNormal(statusCode)) {
				throw new HttpException("连接URL失败，状态码: " + statusCode + " URL: " + url);
			}
			return response;
		}

		@NotNull

		private Response executeProgram(@NotNull HttpUriRequest request) {
			CloseableHttpResponse httpResponse;
			try {
				httpResponse = httpclient.execute(request, context);
			} catch (IOException e) {
				return new HttpResponse(request, context, null);
			}
			Response response = new HttpResponse(request, context, httpResponse);
			cookies(response.cookies()); // 维护cookies

			String redirectUrl; // 修复重定向
			if (followRedirects && URIUtil.statusIsOK(response.statusCode()) && !Judge.isEmpty(redirectUrl = response.header("location"))) {
				response = url(URIUtil.toAbsoluteUrl(url, redirectUrl)).method(Method.GET).execute();  // 跳转修正为GET
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

		private final HttpUriRequest request;
		private final HttpClientContext context;
		private final CloseableHttpResponse res;

		private HttpResponse(HttpUriRequest request, HttpClientContext context, CloseableHttpResponse res) {
			this.request = request;
			this.context = context;
			this.res = res;
		}

		public String url() {
			var reLocs = context.getRedirectLocations();
			return reLocs == null ? request.getURI().toString() : reLocs.get(reLocs.size() - 1).toString();
		}

		public int statusCode() {
			return res == null ? HttpStatus.SC_REQUEST_TIMEOUT : res.getStatusLine().getStatusCode();
		}

		public String statusMessage() {
			return res.getStatusLine().getReasonPhrase();
		}

		public String contentType() {
			return res.getEntity().getContentType().getValue();
		}

		public Map<String, String> headers() {
			if (headers == null) {
				var headers = new HashMap<String, String>();
				for (var header : res.getAllHeaders()) {
					var name = header.getName().toLowerCase();
					var value = header.getValue();
					if (name.equals("set-cookie") && !value.equals("-")) {
						var cookie = headers.get(name);
						value = value.substring(0, value.indexOf(";"));
						headers.put(name, cookie == null ? value : cookie + "; " + value);
					} else {
						headers.put(name, value);
					}
				}
				this.headers = headers;
			}
			return headers;
		}

		public Map<String, String> cookies() {
			return cookies == null ? cookies = res.containsHeader("Set-Cookie") ? Arrays.stream(res.getHeaders("Set-Cookie")).filter(l -> !l.getValue().equals("-")).map(l -> l.getValue().substring(0, l.getValue().indexOf(";"))).collect(Collectors.toMap(l -> l.substring(0, l.indexOf("=")), l -> l.substring(l.indexOf("=") + 1), (e1, e2) -> e2)) : new HashMap<>() : cookies;
		}

		public InputStream bodyStream() throws IOException {
			return res.getEntity().getContent();
		}

		protected ByteArrayOutputStream bodyAsByteArray() {
			if (this.body != null) return this.body;
			try (var in = bodyStream()) {
				var encoding = header("content-encoding");
				var body = "br".equals(encoding) ? new BrotliInputStream(in) : in;
				return this.body = IOUtil.stream(body).toByteArrayOutputStream();
			} catch (Exception e) {
				return null;
			}
		}

	}

}
