package org.haic.often.net.htmlunit;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import org.apache.commons.logging.LogFactory;
import org.haic.often.Judge;
import org.jetbrains.annotations.NotNull;
import org.haic.often.exception.HttpException;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.UserAgent;
import org.haic.often.net.http.HttpStatus;
import org.haic.often.net.http.Response;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.IOUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.util.ThreadUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * HtmlUnit 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/25 21:05
 */
public class HtmlUnitUtil {

	static {  // 屏蔽HtmlUnit日志
		LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.OFF);
		Logger.getLogger("org.apache.commons.httpclient").setLevel(Level.OFF);
	}

	private HtmlUnitUtil() {
	}

	/**
	 * 公共静态连接连接（ 字符串 网址）
	 * <p>
	 * 使用定义的请求 URL 创建一个新的Connection （会话），用于获取和解析 HTML 页面
	 * <p>
	 * 需要注意方法会构造一个新的WebClient,用于链接,由于启动缓慢,不会再执行后关闭,在所有请求完成后使用close()关闭WebClient
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	public static HtmlConnection connect(@NotNull String url) {
		return new HttpConnection().url(url);
	}

	/**
	 * 公共静态连接newSession ()
	 * <p>
	 * 创建一个新Connection以用作会话。将为会话维护连接设置（用户代理、超时、URL 等）和 cookie
	 *
	 * @return 此连接，用于链接
	 */
	public static HtmlConnection newSession() {
		return new HttpConnection();
	}

	private static WebClient createClient() {
		// HtmlUnit 模拟浏览器,浏览器基本设置
		WebClient webClient = new WebClient(BrowserVersion.CHROME);
		webClient.getCookieManager().setCookiesEnabled(true); // 启动cookie
		webClient.getOptions().setThrowExceptionOnScriptError(false);// 当JS执行出错的时候是否抛出异常
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);// 当HTTP的状态非200时是否抛出异常
		webClient.getOptions().setPrintContentOnFailingStatusCode(false); // 响应失败不打印控制台
		webClient.getOptions().setDoNotTrackEnabled(true); // 启用不跟踪
		webClient.getOptions().setDownloadImages(false); // 不下载图片
		webClient.getOptions().setPopupBlockerEnabled(true); // 开启阻止弹窗程序
		webClient.getOptions().setCssEnabled(false); // 关闭CSS
		webClient.getOptions().setJavaScriptEnabled(true); //启用JS
		webClient.getOptions().isUseInsecureSSL(); // 允许不安全SSL
		webClient.setAjaxController(new NicelyResynchronizingAjaxController());// 设置支持AJAX
		webClient.getOptions().setTimeout(10000); // 设置连接超时时间
		return webClient;
	}

	private static class HttpConnection extends HtmlConnection {

		private String auth; // 身份识别标识
		private boolean failThrow; // 错误异常
		private boolean unlimit;// 请求异常无限重试
		private int waitJSTime = 1000; // JS最大运行时间
		private int retry; // 请求异常重试次数
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private Map<String, String> cookies = new HashMap<>(); // cookes
		private List<Integer> retryStatusCodes = new ArrayList<>();

		private final WebClient webClient = createClient();
		private WebRequest request; // 会话

		private HttpConnection() {
			initialization(new WebRequest(null));
		}

		private HtmlConnection initialization(@NotNull WebRequest request) {
			this.request = request;
			header("accept", "application/json, text/html;q=0.9, application/xhtml+xml;q=0.8, */*;q=0.7");
			header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
			header("accept-encoding", "gzip, deflate, br"); // 允许压缩gzip,br-Brotli
			return header("user-agent", UserAgent.chrome()); // 设置随机请求头
		}

		public HtmlConnection url(@NotNull String url) {
			if (!(url = url.strip()).isEmpty() && !url.startsWith("http")) {
				throw new HttpException("Only http & https protocols supported : " + url);
			}
			request.setUrl(URIUtil.createURL(url));
			return this;
		}

		public HtmlConnection newRequest() {
			initialization(new WebRequest(null)).cookies(cookies);
			return Judge.isEmpty(auth) ? this : auth(auth);
		}

		public HtmlConnection userAgent(@NotNull String userAgent) {
			return header("user-agent", userAgent);
		}

		public HtmlConnection isPhone(boolean isPhone) {
			return isPhone ? userAgent(UserAgent.chromeAsPhone()) : userAgent(UserAgent.chrome());
		}

		public HtmlConnection followRedirects(boolean followRedirects) {
			webClient.getOptions().setRedirectEnabled(followRedirects); // 是否启用重定向
			return this;
		}

		public HtmlConnection referrer(@NotNull String referrer) {
			return header("referer", referrer);
		}

		public HtmlConnection auth(@NotNull String auth) {
			return header("authorization", (this.auth = auth.contains(" ") ? auth : "Bearer " + auth));
		}

		public HtmlConnection timeout(int millis) {
			request.setTimeout(millis); // 设置连接超时时间
			return this;
		}

		public HtmlConnection contentType(@NotNull String type) {
			return header("content-type", type);
		}

		public HtmlConnection header(@NotNull String name, @NotNull String value) {
			request.setAdditionalHeader(name, value);
			return this;
		}

		public HtmlConnection headers(@NotNull Map<String, String> headers) {
			request.setAdditionalHeaders(headers);
			return this;
		}

		public HtmlConnection setHeaders(@NotNull Map<String, String> headers) {
			request.getAdditionalHeaders().clear();
			return headers(headers).cookies(cookies);
		}

		public HtmlConnection removeHeader(@NotNull String key) {
			request.removeAdditionalHeader(key);
			return this;
		}

		public HtmlConnection cookie(@NotNull String name, @NotNull String value) {
			return cookies(Map.of(name, value));
		}

		public HtmlConnection cookies(@NotNull Map<String, String> cookies) {
			this.cookies.putAll(cookies);
			return header("cookie", this.cookies.entrySet().stream().map(l -> l.getKey() + "=" + l.getValue()).collect(Collectors.joining("; ")));
		}

		public HtmlConnection setCookies(@NotNull Map<String, String> cookies) {
			this.cookies = new HashMap<>();
			return cookies(cookies);
		}

		public HtmlConnection removeCookie(@NotNull String name) {
			this.cookies.remove(name);
			return this;
		}

		public HtmlConnection data(@NotNull String key, @NotNull String value) {
			request.getRequestParameters().add(new NameValuePair(key, value));
			return this;
		}

		public HtmlConnection data(@NotNull Map<String, String> params) {
			request.setRequestParameters(params.entrySet().stream().map(l -> new NameValuePair(l.getKey(), l.getValue())).collect(Collectors.toList()));
			return this;
		}

		public HtmlConnection requestBody(@NotNull Object body) {
			method(Method.POST);
			if (body instanceof JSONObject json) {
				request.setRequestBody(json.toJSONString());
				return contentType("application/json;charset=UTF-8");
			}
			return requestBody(String.valueOf(body));
		}

		public HtmlConnection requestBody(@NotNull String body) {
			method(Method.POST);
			request.setRequestBody(body);
			return StringUtil.isJson(body) ? contentType("application/json;charset=UTF-8") : contentType("application/x-www-form-urlencoded;charset=UTF-8");
		}

		public HtmlConnection socks(@NotNull String ipAddr) {
			if (ipAddr.startsWith("[")) {
				return socks(ipAddr.substring(1, ipAddr.indexOf(']')), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
			} else {
				int index = ipAddr.lastIndexOf(":");
				return socks(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		public HtmlConnection socks(@NotNull String host, int port) {
			webClient.getOptions().setWebSocketEnabled(true); // WebSocket支持
			webClient.getOptions().setProxyConfig(new ProxyConfig() {{
				setSocksProxy(true);
				setProxyHost(host);
				setProxyPort(port);
			}});
			return this;
		}

		public HtmlConnection socks(@NotNull String host, int port, @NotNull String username, @NotNull String password) {
			((DefaultCredentialsProvider) webClient.getCredentialsProvider()).addCredentials(username, password);
			return socks(host, port);
		}

		public HtmlConnection proxy(@NotNull String ipAddr) {
			if (ipAddr.startsWith("[")) {
				return proxy(ipAddr.substring(1, ipAddr.indexOf(']')), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
			} else {
				int index = ipAddr.lastIndexOf(":");
				return proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		public HtmlConnection proxy(@NotNull String host, int port) {
			webClient.getOptions().setWebSocketEnabled(false); // WebSocket支持
			webClient.getOptions().setProxyConfig(new ProxyConfig() {{
				setSocksProxy(false);
				setProxyHost(host);
				setProxyPort(port);
			}});
			return this;
		}

		public HtmlConnection proxy(@NotNull String host, int port, @NotNull String username, @NotNull String password) {
			((DefaultCredentialsProvider) webClient.getCredentialsProvider()).addCredentials(username, password);
			return proxy(host, port);
		}

		public HtmlConnection proxy(@NotNull Proxy proxy) {
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

		public HtmlConnection method(@NotNull Method method) {
			request.setHttpMethod(HttpMethod.valueOf(method.name()));
			return this;
		}

		public HtmlConnection failThrow(boolean exit) {
			failThrow = exit;
			return this;
		}

		public HtmlConnection retry(int retry) {
			this.retry = retry;
			return this;
		}

		public HtmlConnection retry(int retry, int millis) {
			this.retry = retry;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		public HtmlConnection retry(boolean unlimit) {
			this.unlimit = unlimit;
			return this;
		}

		public HtmlConnection retry(boolean unlimit, int millis) {
			this.unlimit = unlimit;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		public HtmlConnection retryStatusCodes(int... statusCode) {
			retryStatusCodes = Arrays.stream(statusCode).boxed().toList();
			return this;
		}

		public HtmlConnection retryStatusCodes(List<Integer> retryStatusCodes) {
			this.retryStatusCodes = retryStatusCodes;
			return this;
		}

		/**
		 * 启用/禁用 CSS 支持。默认情况下，禁用此属性。如果禁用 HtmlUnit 将不会下载链接的 css 文件，也不会触发相关的 onload/onerror 事件
		 *
		 * @param enableCSS true启用 CSS 支持
		 * @return 此链接, 用于链接
		 */
		public HtmlConnection enableCSS(boolean enableCSS) {
			webClient.getOptions().setCssEnabled(enableCSS);
			return this;
		}

		public HtmlConnection waitJSTime(int millis) {
			webClient.getOptions().setJavaScriptEnabled(millis != 0); // 是否启用JS
			webClient.setJavaScriptTimeout(waitJSTime);
			waitJSTime = millis;
			return this;
		}

		public HtmlConnection close() {
			webClient.close(); // 设置连接超时时间
			return this;
		}

		@NotNull
		public Response execute() {
			var response = executeProgram(request);
			int statusCode = response.statusCode();
			for (int i = 0; (URIUtil.statusIsTimeout(statusCode) || retryStatusCodes.contains(statusCode)) && (i < retry || unlimit); i++) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP);
				response = executeProgram(request);
				statusCode = response.statusCode();
			}
			// webClient.close(); // 关闭webClient
			if (failThrow && !URIUtil.statusIsNormal(statusCode)) {
				throw new HttpException("连接URL失败，状态码: " + statusCode + " URL: " + request.getUrl());
			}
			return response;
		}

		@NotNull
		private Response executeProgram(@NotNull WebRequest request) {
			Response response;
			try { // 获得页面
				response = new HttpResponse(webClient.getPage(request));
			} catch (IOException e) {
				return new HttpResponse(null);
			}
			webClient.waitForBackgroundJavaScript(waitJSTime); // 阻塞并执行JS

			String redirectUrl; // 修复重定向
			if (webClient.getOptions().isRedirectEnabled() && URIUtil.statusIsOK(response.statusCode()) && !Judge.isEmpty(redirectUrl = response.header("location"))) {
				url(redirectUrl).method(Method.GET); // 跳转修正为GET
				response = executeProgram(request);
			}

			return response;
		}
	}

	private static class HttpResponse extends Response {

		private final Page page; // Page对象

		private HttpResponse(Page page) {
			this.page = page;
		}

		public String url() {
			return page.getUrl().toExternalForm();
		}

		public int statusCode() {
			return page == null ? HttpStatus.SC_REQUEST_TIMEOUT : page.getWebResponse().getStatusCode();
		}

		public String statusMessage() {
			return page.getWebResponse().getStatusMessage();
		}

		public String contentType() {
			return page.getWebResponse().getStatusMessage();
		}

		public Map<String, String> headers() {
			if (headers == null) {
				Map<String, String> headers = new HashMap<>();
				for (NameValuePair header : page.getWebResponse().getResponseHeaders()) {
					String name = header.getName().toLowerCase();
					String value = header.getValue();
					if (name.equals("set-cookie") && !value.equals("-")) {
						String cookie = headers.get(name);
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
			return cookies = cookies == null ? page.getWebResponse().getResponseHeaders().stream().filter(l -> l.getName().equalsIgnoreCase("set-cookie")).filter(l -> !l.getValue().equals("-")).map(l -> l.getValue().substring(0, l.getValue().indexOf(";"))).collect(Collectors.toMap(l -> l.substring(0, l.indexOf("=")), l -> l.substring(l.indexOf("=") + 1), (e1, e2) -> e2)) : cookies;
		}

		public InputStream bodyStream() throws IOException {
			return page.getWebResponse().getContentAsStream();
		}

		protected ByteArrayOutputStream bodyAsByteArray() {
			if (this.body != null) return this.body;
			try (InputStream in = bodyStream()) {
				this.body = IOUtil.stream(in).toByteArrayOutputStream();
			} catch (Exception e) {
				return null;
			}
			return this.body;
		}

		public void close() {
			page.cleanUp();
		}

	}

}
