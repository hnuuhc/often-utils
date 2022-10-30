package org.haic.often.net.download;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.Connection;
import org.haic.often.net.http.HttpStatus;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.net.http.Response;
import org.haic.often.util.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 网络文件 工具类
 * <p>
 * 用于m3u8视频的下载,非线程安全
 * <p>
 * 默认10线程下载,不应设置过高
 *
 * @author haicdust
 * @version 1.8.2
 * @since 2021/12/24 23:07
 */
public class HLSDownload {

	private HLSDownload() {
	}

	/**
	 * 公共静态连接newSession ()
	 * <p>
	 * 创建一个新Connection以用作会话。将为会话维护连接设置（用户代理、超时、URL 等）和 cookie
	 *
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static HLSConnection newSession() {
		return new HttpConnection();
	}

	/**
	 * 公共静态连接连接（ 字符串 网址）<br/> 使用定义的请求 URL 创建一个新的Connection （会话）
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static HLSConnection connect(@NotNull String url) {
		return newSession().url(url);
	}

	/**
	 * 获取新的Download对象并设置配置文件<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param src session文件路径
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static HLSConnection session(@NotNull String src) {
		return session(new File(src));
	}

	/**
	 * 获取新的Download对象并设置配置文件<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param file session文件
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static HLSConnection session(@NotNull File file) {
		return newSession().session(file);
	}

	/**
	 * 获取新的Download对象并设置m3u8内容<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param body m3u8文本
	 * @return 此连接，用于链接
	 */
	@Contract(pure = true)
	public static HLSConnection body(@NotNull String body) {
		return newSession().body(body);
	}

	private static class HttpResponse extends SionResponse {

		private final HLSConnection conn;
		private final SionRequest request;

		private HttpResponse(HLSConnection conn, SionRequest request) {
			this.conn = conn;
			this.request = request;
		}

		@Contract(pure = true)
		public int statusCode() {
			return request.statusCode();
		}

		@Contract(pure = true)
		public String fileName() {
			return request.getStorage().getName();
		}

		@Contract(pure = true)
		public String filePath() {
			return request.getStorage().getAbsolutePath();
		}

		@Contract(pure = true)
		public long fileSize() {
			return request.getFileSize();
		}

		@Contract(pure = true)
		public String header(@NotNull String name) {
			return request.headers().get(name);
		}

		@Contract(pure = true)
		public Map<String, String> headers() {
			return request.headers();
		}

		@Contract(pure = true)
		public String cookie(@NotNull String name) {
			return request.cookies().get(name);
		}

		@Contract(pure = true)
		public Map<String, String> cookies() {
			return request.cookies();
		}

		@Contract(pure = true)
		public String hash() {
			return request.getHash();
		}

		@Contract(pure = true)
		public String url() {
			return request.getUrl();
		}

		@Contract(pure = true)
		public SionResponse restart() {
			return URIUtil.statusIsOK(statusCode()) ? this : conn.execute();
		}

		@Contract(pure = true)
		public boolean clear() {
			return new File(request.getStorage().getPath() + ".session").exists() && delete();
		}

		@Contract(pure = true)
		public boolean delete() {
			File storage = request.getStorage();
			File session = new File(storage.getPath() + ".session");
			return (!storage.exists() || storage.delete()) && (!session.exists() || session.delete());
		}
	}

	private static class HttpConnection extends HLSConnection {

		private String url; // 请求URL
		private String fileName; // 文件名
		private final String SESSION_SUFFIX = ".hlsion";
		private String body;
		private String key = "";
		private String iv = "";
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private int retry; // 请求异常重试次数
		private int MAX_THREADS = 10; // 默认10线程下载
		private boolean unlimit;// 请求异常无限重试
		private boolean failThrow; // 错误异常
		private boolean rename; // 重命名
		private Proxy proxy = Proxy.NO_PROXY; // 代理
		private File storage; // 本地存储文件
		private File session; // 配置信息文件
		private File DEFAULT_FOLDER = SystemUtil.DEFAULT_DOWNLOAD_FOLDER; // 存储目录
		private List<Integer> retryStatusCodes = new ArrayList<>();

		private Map<String, String> headers = new HashMap<>(); // headers
		private Map<String, String> cookies = new HashMap<>(); // cookies

		private String method = "FULL";// 下载模式
		private final SionRequest request = new SionRequest();
		private JSONObject fileInfo = new JSONObject();
		private Runnable listener;
		private final AtomicBoolean writeStatus = new AtomicBoolean(true);
		private int pieceTotal;
		private int site;
		private long schedule; // 文件大小
		private final Map<String, byte[]> writeData = new ConcurrentHashMap<>();
		private List<String> links;

		private HttpConnection() {
		}

		@Contract(pure = true)
		public HLSConnection url(@NotNull String url) {
			request.setUrl(this.url = url);
			fileInfo.put("url", url);
			this.fileName = null;
			key = "";
			iv = "";
			method = "FULL";
			return this;
		}

		@Contract(pure = true)
		public HLSConnection session(@NotNull String src) {
			return session(new File(src));
		}

		@Contract(pure = true)
		public HLSConnection session(@NotNull File session) {
			if (!session.getName().endsWith(SESSION_SUFFIX)) {
				throw new RuntimeException("Not is session file: " + session);
			} else if (!session.exists()) { // 配置文件不存在，抛出异常
				throw new RuntimeException("Not found or not is file " + session);
			}
			this.method = "FILE";
			this.session = session;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection key(@NotNull String key) {
			return key(key, "");
		}

		@Contract(pure = true)
		public HLSConnection key(@NotNull String key, @NotNull String iv) {
			this.key = key;
			this.iv = iv;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection body(@NotNull String body) {
			this.body = body;
			this.method = "BODY";
			return this;
		}

		@Contract(pure = true)
		public HLSConnection userAgent(@NotNull String userAgent) {
			return header("user-agent", userAgent);
		}

		@Contract(pure = true)
		public HLSConnection referrer(@NotNull String referrer) {
			return header("referer", referrer);
		}

		@Contract(pure = true)
		public HLSConnection header(@NotNull String name, @NotNull String value) {
			headers.put(name, value);
			return this;
		}

		@Contract(pure = true)
		public HLSConnection headers(@NotNull Map<String, String> headers) {
			this.headers.putAll(headers);
			return this;
		}

		@Contract(pure = true)
		public HLSConnection cookie(@NotNull String name, @NotNull String value) {
			cookies.put(name, value);
			return this;
		}

		@Contract(pure = true)
		public HLSConnection cookies(@NotNull Map<String, String> cookies) {
			this.cookies.putAll(cookies);
			return this;
		}

		@Contract(pure = true)
		public HLSConnection auth(@NotNull String auth) {
			return header("authorization", auth.startsWith("Bearer ") ? auth : "Bearer " + auth);
		}

		@Contract(pure = true)
		public HLSConnection thread(int nThread) {
			if (nThread < 1) {
				throw new RuntimeException("thread Less than 1");
			}
			this.MAX_THREADS = nThread;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection fileName(@NotNull String fileName) {
			this.fileName = FileUtil.illegalFileName(URIUtil.decode(fileName));
			FileUtil.fileNameValidity(fileName);
			return this;
		}

		@Contract(pure = true)
		public HLSConnection rename(boolean rename) {
			this.rename = rename;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection socks(@NotNull String ipAddr) {
			if (ipAddr.startsWith(Symbol.OPEN_BRACKET)) {
				return socks(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(Symbol.COLON) + 1)));
			} else {
				int index = ipAddr.lastIndexOf(Symbol.COLON);
				return socks(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		@Contract(pure = true)
		public HLSConnection socks(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		}

		@Contract(pure = true)
		public HLSConnection proxy(@NotNull String ipAddr) {
			if (ipAddr.startsWith(Symbol.OPEN_BRACKET)) {
				return proxy(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(Symbol.COLON) + 1)));
			} else {
				int index = ipAddr.lastIndexOf(Symbol.COLON);
				return proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		@Contract(pure = true)
		public HLSConnection proxy(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		}

		@Contract(pure = true)
		public HLSConnection proxy(@NotNull Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection failThrow(boolean exit) {
			this.failThrow = exit;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection retry(int retry) {
			this.retry = retry;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection retry(int retry, int millis) {
			this.retry = retry;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection retry(boolean unlimit) {
			this.unlimit = unlimit;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection retry(boolean unlimit, int millis) {
			this.unlimit = unlimit;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection retryStatusCodes(int... statusCode) {
			this.retryStatusCodes = Arrays.stream(statusCode).boxed().toList();
			return this;
		}

		@Contract(pure = true)
		public HLSConnection retryStatusCodes(List<Integer> retryStatusCodes) {
			this.retryStatusCodes = retryStatusCodes;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection folder(@NotNull String folder) {
			return folder(new File(folder));
		}

		@Contract(pure = true)
		public HLSConnection folder(@NotNull File folder) {
			this.DEFAULT_FOLDER = folder;
			return this;
		}

		@Contract(pure = true)
		public HLSConnection listener(@NotNull HLSListener listener) {
			return listener(listener, 1000);
		}

		@Contract(pure = true)
		public HLSConnection listener(@NotNull HLSListener listener, int millis) {
			this.listener = () -> {
				long rate = schedule;
				do {
					ThreadUtil.waitThread(millis);
					// noinspection ConstantConditions
					listener.bytesTransferred(fileName, schedule - rate, schedule, pieceTotal - links.size() + site, pieceTotal);
					rate = schedule;
				} while (!Thread.currentThread().isInterrupted());
			};
			return this;
		}

		@Contract(pure = true)
		public SionResponse execute() {
			return execute(method);
		}

		@Contract(pure = true)
		private SionResponse execute(@NotNull String method) {
			Connection conn = HttpsUtil.newSession().proxy(proxy).headers(headers).cookies(cookies).retry(retry, MILLISECONDS_SLEEP).retry(unlimit).retryStatusCodes(retryStatusCodes).failThrow(failThrow);
			List<String> renewLink = new ArrayList<>();
			switch (method) {
				case "BODY" -> {
					if (Judge.isEmpty(fileName)) {
						throw new RuntimeException("fileName is exists");
					}
					String head = url.substring(0, url.lastIndexOf(Symbol.SLASH) + 1);
					if (body.contains("#EXT-X-STREAM-INF")) {
						List<String> info = body.lines().toList();
						int index = info.indexOf(info.stream().filter(l -> l.startsWith("#EXT-X-STREAM-INF")).findFirst().orElseThrow());
						String redirectUrl = info.get(index + 1);
						if (!redirectUrl.contains("://")) {
							if (redirectUrl.startsWith(Symbol.SLASH)) {
								redirectUrl = URIUtil.getDomain(redirectUrl) + redirectUrl;
							} else {
								redirectUrl = head + redirectUrl;
							}
						}
						request.setUrl(this.url = redirectUrl);
						fileInfo.put("url", redirectUrl);
						return execute("FULL");
					}
					if (body.contains("#EXT-X-KEY")) {
						String[] extKey = StringUtil.extract(body, "#EXT-X-KEY.*").substring(11).split(Symbol.COMMA);
						String encryptMethod = extKey[0].substring(7);
						if (!encryptMethod.contains("AES")) {
							throw new RuntimeException("unknown encryption method " + encryptMethod);
						}
						String keyUrl = StringUtil.strip(extKey[1].substring(4), Symbol.DOUBLE_QUOTE);
						Response res = conn.url(keyUrl).execute();
						int statusCode = res.statusCode();
						if (!URIUtil.statusIsOK(statusCode)) {
							return new HttpResponse(this, request.statusCode(statusCode));
						}
						request.headers(res.headers()).cookies(res.cookies());
						key = res.body();
						if (extKey.length == 3) {
							iv = extKey[2].substring(3);
						}
					}
					links = body.substring(body.indexOf("#EXTINF"), body.indexOf("#EXT-X-ENDLIST")).lines().filter(l -> !l.startsWith(Symbol.POUND)).map(l -> l.contains("://") ? l : head + l).toList();
					pieceTotal = links.size();
					// 创建并写入文件配置信息
					fileInfo.put("fileName", fileName);
					fileInfo.put("fileSize", 0);
					fileInfo.put("header", JSONObject.toJSONString(headers));
					fileInfo.put("cookie", JSONObject.toJSONString(cookies));
					fileInfo.put("key", key);
					fileInfo.put("iv", iv);
					fileInfo.put("data", links);
					fileInfo.put("pieceSize", pieceTotal);
					ReadWriteUtil.orgin(session).write(fileInfo.toJSONString());
				}
				case "FULL" -> {
					if (Judge.isEmpty(fileName)) {
						fileName = url.substring(url.lastIndexOf(Symbol.SLASH) + 1);
						fileName = URIUtil.decode(fileName.contains(Symbol.QUESTION) ? fileName.substring(0, fileName.indexOf(Symbol.QUESTION)) : fileName);
						fileName = fileName.substring(0, fileName.lastIndexOf(Symbol.DOT) + 1) + "mp4";
						fileName = FileUtil.illegalFileName(fileName); // 文件名排除非法字符
						FileUtil.fileNameValidity(fileName);
					}
					// 获取待下载文件和配置文件对象
					request.setStorage(storage = new File(DEFAULT_FOLDER, fileName)); // 获取其file对象
					session = new File(storage + SESSION_SUFFIX); // 配置信息文件后缀
					if (session.exists()) { // 转为会话配置
						return execute("FILE");
					} else if (storage.exists()) { // 文件已存在
						if (rename) { // 重命名
							int count = 1, index = fileName.lastIndexOf(Symbol.DOT);
							String suffix = index > 0 ? fileName.substring(index) : "";
							do {
								fileName = fileName.substring(0, index) + " - " + count++ + suffix;
								storage = new File(DEFAULT_FOLDER, fileName);
								session = new File(storage + SESSION_SUFFIX);
								if (session.exists()) { // 转为会话配置
									return execute("FILE");
								}
							} while (storage.exists());
							request.setStorage(storage); // 配置信息文件
						} else {
							return new HttpResponse(this, request.statusCode(HttpStatus.SC_OK));
						}
					}

					Response res = conn.url(url).execute();
					int statusCode = res.statusCode();
					if (!URIUtil.statusIsOK(statusCode)) {
						return new HttpResponse(this, request.statusCode(statusCode));
					}
					request.headers(res.headers()).cookies(res.cookies());
					this.body = res.body();
					return execute("BODY");
				}
				case "FILE" -> {
					fileInfo = JSON.parseObject(ReadWriteUtil.orgin(session).readBytes());
					request.setUrl(url = fileInfo.getString("url"));
					fileName = fileInfo.getString("fileName");
					schedule = fileInfo.getLong("fileSize");
					headers = StringUtil.jsonToMap(fileInfo.getString("header"));
					cookies = StringUtil.jsonToMap(fileInfo.getString("cookie"));
					key = fileInfo.getString("key");
					iv = fileInfo.getString("iv");
					links = fileInfo.getList("data", String.class);
					pieceTotal = links.size();
					JSONObject renew = fileInfo.getJSONObject("renew");
					if (storage.exists() && renew != null) {
						links.subList(0, pieceTotal - renew.getInteger("status")).clear();
						writeData.putAll(JSON.parseObject(renew.getString("data"), new TypeReference<HashMap<String, byte[]>>() {}));
						renewLink.addAll(writeData.keySet().stream().toList());
					}
					fileInfo.remove("renew");
					ReadWriteUtil.orgin(session).append(false).write(fileInfo.toJSONString());  // 配置文件可能占用过多内存,重置配置文件
				}
				default -> throw new RuntimeException("Unknown mode");
			}

			FileUtil.createFolder(DEFAULT_FOLDER); // 创建文件夹
			Runnable breakPoint = () -> ReadWriteUtil.orgin(session).append(false).write(fileInfo.fluentPut("fileSize", schedule).fluentPut("renew", new JSONObject().fluentPut("status", links.size() - site).fluentPut("data", new JSONObject(writeData))).toJSONString());
			Thread abnormal;
			Runtime.getRuntime().addShutdownHook(abnormal = new Thread(breakPoint));
			Thread listenTask = ThreadUtil.start(listener);
			AtomicInteger statusCodes = new AtomicInteger(HttpStatus.SC_OK);
			ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
			for (String link : links) {
				if (renewLink.contains(link)) {
					renewLink.remove(link);
					continue;
				}
				executor.execute(() -> {
					int statusCode = FULL(link, retry);
					if (!URIUtil.statusIsOK(statusCode)) {
						statusCodes.set(statusCode);
						executor.shutdownNow(); // 结束未开始的线程，并关闭线程池
					}
				});
			}
			ThreadUtil.waitEnd(executor); // 等待线程结束
			Runtime.getRuntime().removeShutdownHook(abnormal);
			ThreadUtil.interrupt(listenTask);
			if (!URIUtil.statusIsOK(statusCodes.get())) { // 验证下载状态
				breakPoint.run();
				if (failThrow) {
					throw new RuntimeException("M3U8文件下载失败，状态码: " + statusCodes + " URL: " + url);
				}
				return new HttpResponse(this, request.statusCode(statusCodes.get()));
			}

			session.delete(); // 删除会话信息文件
			return new HttpResponse(this, request.statusCode(HttpStatus.SC_OK));
		}

		@Contract(pure = true)
		private int FULL(@NotNull String link, int retry) {
			Response piece = HttpsUtil.connect(link).timeout(0).proxy(proxy).headers(headers).cookies(cookies).failThrow(failThrow).execute();
			int statusCode = piece.statusCode();
			return URIUtil.statusIsOK(statusCode) ? FULL(piece, link, retry) : unlimit || retry > 0 ? FULL(link, retry - 1) : statusCode;
		}

		@Contract(pure = true)
		private int FULL(Response piece, @NotNull String link, int retry) {
			try (InputStream inputStream = piece.bodyStream()) {
				byte[] data = IOUtil.stream(inputStream).toByteArray();
				if (Integer.parseInt(piece.header("content-length")) == data.length) {
					writeData.put(link, data);
					if (writeStatus.get()) {
						writeStatus.set(false);
						writePiece();
						writeStatus.set(true);
					}
					return HttpStatus.SC_OK;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (unlimit || retry > 0) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP);
				return FULL(link, retry - 1);
			}
			return HttpStatus.SC_REQUEST_TIMEOUT;
		}

		@Contract(pure = true)
		private void writePiece() {
			try (FileOutputStream out = new FileOutputStream(storage, true)) {
				while (site < links.size()) {
					String link = links.get(site);
					if (!writeData.containsKey(link)) {
						break;
					}
					byte[] bytes = Judge.isEmpty(key) ? writeData.get(link) : AESUtil.decode(writeData.get(link), key, iv);
					out.write(bytes, 0, bytes.length);
					request.setFileSize(schedule += bytes.length);
					writeData.remove(link);
					site++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
