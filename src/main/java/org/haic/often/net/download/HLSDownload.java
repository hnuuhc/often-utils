package org.haic.often.net.download;

import org.haic.often.Judge;
import org.haic.often.exception.AESException;
import org.haic.often.exception.HLSDownloadException;
import org.haic.often.function.StringFunction;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.HttpStatus;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.net.http.Response;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.thread.ConsumerThread;
import org.haic.often.util.*;
import org.jetbrains.annotations.NotNull;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

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
	public static HLSConnection newSession() {
		return new HttpConnection();
	}

	/**
	 * 公共静态连接连接（ 字符串 网址）<br/> 使用定义的请求 URL 创建一个新的Connection （会话）
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	public static HLSConnection connect(@NotNull String url) {
		return newSession().url(url);
	}

	/**
	 * 获取新的Download对象并设置配置文件<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param src session文件路径
	 * @return 此连接，用于链接
	 */
	public static HLSConnection session(@NotNull String src) {
		return session(new File(src));
	}

	/**
	 * 获取新的Download对象并设置配置文件<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param file session文件
	 * @return 此连接，用于链接
	 */
	public static HLSConnection session(@NotNull File file) {
		return newSession().session(file);
	}

	/**
	 * 获取新的Download对象并设置m3u8内容<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param body m3u8文本
	 * @return 此连接，用于链接
	 */
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

		public int statusCode() {
			return request.statusCode();
		}

		public String fileName() {
			return request.getStorage().getName();
		}

		public File file() {
			return request.getStorage();
		}

		public String filePath() {
			return request.getStorage().getAbsolutePath();
		}

		public long fileSize() {
			return request.getFileSize();
		}

		public String header(@NotNull String name) {
			return request.headers().get(name);
		}

		public Map<String, String> headers() {
			return request.headers();
		}

		public String cookie(@NotNull String name) {
			return request.cookies().get(name);
		}

		public Map<String, String> cookies() {
			return request.cookies();
		}

		public String hash() {
			return request.getHash();
		}

		public String url() {
			return request.getUrl();
		}

		public SionResponse restart() {
			return URIUtil.statusIsOK(statusCode()) ? this : conn.execute();
		}

		public boolean clear() {
			return new File(request.getStorage().getPath() + ".session").exists() && delete();
		}

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
		private int DEFAULT_BUFFER_SIZE = 8192;
		private String body;
		private String key = "";
		private String iv = "";
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private int MAX_RETRY; // 请求异常重试次数
		private int MAX_THREADS = 10; // 默认10线程下载
		private boolean unlimit;// 请求异常无限重试
		private boolean failThrow; // 错误异常
		private boolean rename; // 重命名
		private Proxy proxy = Proxy.NO_PROXY; // 代理
		private File session; // 配置信息文件
		private File DEFAULT_FOLDER = SystemUtil.DEFAULT_DOWNLOAD_FOLDER; // 存储目录
		private List<Integer> retryStatusCodes = new ArrayList<>();
		private StringFunction<String> keyDecrypt = key -> key;
		private Predicate<String> select = l -> true;

		private Map<String, String> headers = new HashMap<>(); // headers
		private Map<String, String> cookies = new HashMap<>(); // cookies

		private String method = "FULL";// 下载模式
		private final SionRequest request = new SionRequest();
		private JSONObject fileInfo = new JSONObject();
		private Runnable listener;
		private int site;
		private final AtomicLong schedule = new AtomicLong();
		private final Map<File, Long> status = new ConcurrentHashMap<>();
		private List<String> links;

		private HttpConnection() {
		}

		public HLSConnection url(@NotNull String url) {
			request.setUrl(this.url = url);
			fileInfo.put("url", url);
			fileName = null;
			key = "";
			iv = "";
			method = "FULL";
			return this;
		}

		public HLSConnection session(@NotNull String src) {
			return session(new File(src));
		}

		public HLSConnection session(@NotNull File session) {
			if (!session.getName().endsWith(SESSION_SUFFIX)) {
				throw new HLSDownloadException("Not is session file: " + session);
			} else if (!session.exists()) { // 配置文件不存在，抛出异常
				throw new HLSDownloadException("Not found or not is file " + session);
			}
			this.method = "FILE";
			this.session = session;
			return this;
		}

		public HLSConnection keyDecrypt(@NotNull StringFunction<String> keyDecrypt) {
			this.keyDecrypt = keyDecrypt;
			return this;
		}

		public HLSConnection body(@NotNull String body) {
			this.body = body;
			this.method = "BODY";
			return this;
		}

		public HLSConnection key(@NotNull String key) {
			this.key = keyDecrypt.apply(key);
			return this;
		}

		public HLSConnection iv(@NotNull String iv) {
			this.iv = iv;
			return this;
		}

		public HLSConnection select(@NotNull Predicate<String> select) {
			this.select = select;
			return this;
		}

		public HLSConnection userAgent(@NotNull String userAgent) {
			return header("user-agent", userAgent);
		}

		public HLSConnection referrer(@NotNull String referrer) {
			return header("referer", referrer);
		}

		public HLSConnection header(@NotNull String name, @NotNull String value) {
			headers.put(name, value);
			return this;
		}

		public HLSConnection headers(@NotNull Map<String, String> headers) {
			this.headers.putAll(headers);
			return this;
		}

		public HLSConnection cookie(@NotNull String name, @NotNull String value) {
			cookies.put(name, value);
			return this;
		}

		public HLSConnection cookies(@NotNull Map<String, String> cookies) {
			this.cookies.putAll(cookies);
			return this;
		}

		public HLSConnection auth(@NotNull String auth) {
			return header("authorization", auth.startsWith("Bearer ") ? auth : "Bearer " + auth);
		}

		public HLSConnection thread(int nThread) {
			if (nThread < 1) {
				throw new HLSDownloadException("thread Less than 1");
			}
			this.MAX_THREADS = nThread;
			return this;
		}

		public HLSConnection fileName(@NotNull String fileName) {
			if (!fileName.contains(".")) {
				throw new HLSDownloadException("文件名必须存在后缀: " + fileName);
			}
			this.fileName = FileUtil.illegalFileName(URIUtil.decode(fileName));
			FileUtil.fileNameValidity(fileName);
			return this;
		}

		public HLSConnection rename(boolean rename) {
			this.rename = rename;
			return this;
		}

		public HLSConnection socks(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		}

		public HLSConnection proxy(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		}

		public HLSConnection proxy(@NotNull Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		public HLSConnection failThrow(boolean exit) {
			this.failThrow = exit;
			return this;
		}

		public HLSConnection retry(int retry) {
			this.MAX_RETRY = retry;
			return this;
		}

		public HLSConnection retry(int retry, int millis) {
			this.MAX_RETRY = retry;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		public HLSConnection retry(boolean unlimit) {
			this.unlimit = unlimit;
			return this;
		}

		public HLSConnection retry(boolean unlimit, int millis) {
			this.unlimit = unlimit;
			this.MILLISECONDS_SLEEP = millis;
			return this;
		}

		public HLSConnection retryStatusCodes(int... statusCode) {
			this.retryStatusCodes = Arrays.stream(statusCode).boxed().toList();
			return this;
		}

		public HLSConnection retryStatusCodes(List<Integer> retryStatusCodes) {
			this.retryStatusCodes = retryStatusCodes;
			return this;
		}

		public HLSConnection bufferSize(int bufferSize) {
			DEFAULT_BUFFER_SIZE = bufferSize;
			return this;
		}

		public HLSConnection folder(@NotNull String folder) {
			return folder(new File(folder));
		}

		public HLSConnection folder(@NotNull File folder) {
			this.DEFAULT_FOLDER = folder;
			return this;
		}

		public HLSConnection listener(@NotNull HLSListener listener) {
			return listener(listener, 1000);
		}

		public HLSConnection listener(@NotNull HLSListener listener, int millis) {
			this.listener = () -> {
				long schedule, rate = 0;
				do {
					ThreadUtil.waitThread(millis);
					schedule = this.schedule.get();
					listener.bytesTransferred(fileName, schedule - rate, site, links.size());
					rate = schedule;
				} while (!Thread.currentThread().isInterrupted());
			};
			return this;
		}

		public SionResponse execute() {
			return execute(method);
		}

		private SionResponse execute(@NotNull String method) {
			initializationStatus(); // 初始化进度
			File storage;
			switch (method) {
				case "BODY" -> {
					var info = body.lines().toList();
					if (!info.get(0).equals("#EXTM3U")) {
						throw new HLSDownloadException("内容不是M3U8格式");
					}
					if (info.get(1).startsWith("#EXT-X-STREAM-INF")) {
						String redirectUrl = null;
						for (int i = 1; i < info.size(); i += 2) {
							if (select.test(info.get(i))) {
								redirectUrl = URIUtil.toAbsoluteUrl(url, info.get(++i));
								break;
							}
						}
						if (redirectUrl == null) {
							throw new HLSDownloadException("EXT-X-STREAM-INF筛选结果为空");
						}
						request.setUrl(this.url = redirectUrl);
						fileInfo.put("url", redirectUrl);
						return execute("FULL");
					}
					if (Judge.isEmpty(fileName)) { // 随机命名
						fileName = RandomUtil.randomAlphanumeric(32) + ".mp4";
					}
					storage = new File(DEFAULT_FOLDER, fileName);
					if (storage.exists()) {
						throw new HLSDownloadException("存储文件已经存在: " + storage);
					}
					var keyInfo = info.stream().filter(l -> l.startsWith("#EXT-X-KEY")).findFirst().orElse(null);
					if (keyInfo != null) {
						var extKey = keyInfo.substring(11).split(",");
						var encryptMethod = extKey[0].substring(7);

						for (var ext : extKey) {
							var entry = ext.split("=");
							switch (entry[0]) {
								case "METHO" -> {
									if (!encryptMethod.contains("AES") && !encryptMethod.equals("QINIU-PROTECTION-10")) {
										throw new HLSDownloadException("未知的解密方法: " + encryptMethod);
									}
								}
								case "URI" -> {
									var keyUrl = URIUtil.toAbsoluteUrl(url, StringUtil.strip(entry[1], "\""));
									//noinspection DuplicatedCode
									var res = HttpsUtil.connect(keyUrl).proxy(proxy).headers(headers).cookies(cookies).retry(MAX_RETRY, MILLISECONDS_SLEEP).retry(unlimit).retryStatusCodes(retryStatusCodes).failThrow(failThrow).execute();
									int statusCode = res.statusCode();
									if (!URIUtil.statusIsOK(statusCode)) {
										return new HttpResponse(this, request.statusCode(statusCode));
									}
									request.headers(res.headers()).cookies(res.cookies());
									key = keyDecrypt.apply(res.body()); // key解密
								}
								case "KEY" -> key = keyDecrypt.apply(entry[1]);
								case "IV" -> iv = entry[1].startsWith("0x") ? entry[1].substring(2) : entry[1];
							}
						}

						// 效验key格式是否正确
						switch (key.length()) {
							case 16, 24, 32 -> {
								if (!key.matches("^[0-9a-zA-Z]+$")) throw new HLSDownloadException("KEY存在非法字符,可能被加密: " + key);
							}
							default -> throw new HLSDownloadException("KEY长度为" + key.length() + "不正确: " + key);
						}
					}
					links = info.stream().filter(l -> !l.startsWith("#")).map(l -> URIUtil.toAbsoluteUrl(url, l)).toList();
					// 创建并写入文件配置信息
					fileInfo.put("fileName", fileName);
					fileInfo.put("fileSize", 0);
					fileInfo.put("header", headers);
					fileInfo.put("cookie", cookies);
					fileInfo.put("key", key);
					fileInfo.put("iv", iv);
					fileInfo.put("data", links);
					fileInfo.put("pieceTotal", links.size());
					ReadWriteUtil.orgin(session).write(fileInfo.toString());
				}
				case "FULL" -> {
					if (Judge.isEmpty(fileName)) {
						fileName = url.substring(url.lastIndexOf("/") + 1);
						fileName = URIUtil.decode(fileName.contains("?") ? fileName.substring(0, fileName.indexOf("?")) : fileName);
						fileName = FileUtil.illegalFileName(fileName); // 文件名排除非法字符
						fileName = fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf(".") + 1) + "mp4" : fileName + ".mp4";
						FileUtil.fileNameValidity(fileName);
					}
					// 获取待下载文件和配置文件对象
					request.setStorage(storage = new File(DEFAULT_FOLDER, fileName)); // 获取其file对象
					var folder = new File(DEFAULT_FOLDER, fileName.substring(0, fileName.lastIndexOf(".")));
					session = new File(folder, SESSION_SUFFIX); // 配置信息文件后缀
					if (session.exists()) { // 转为会话配置
						return execute("FILE");
					} else if (storage.exists()) { // 文件已存在
						if (rename) { // 重命名
							int count = 1, index = fileName.lastIndexOf(".");
							var head = fileName.substring(0, index);
							var suffix = fileName.substring(index);
							do {
								String newPath = head + " - " + count++;
								fileName = newPath + suffix;
								storage = new File(DEFAULT_FOLDER, fileName);
								session = new File(new File(DEFAULT_FOLDER, newPath), SESSION_SUFFIX); // 配置信息文件后缀
								if (session.exists()) { // 转为会话配置
									return execute("FILE");
								}
							} while (storage.exists());
							request.setStorage(storage); // 配置信息文件
						} else {
							return new HttpResponse(this, request.statusCode(HttpStatus.SC_OK));
						}
					}

					//noinspection DuplicatedCode
					var res = HttpsUtil.connect(url).proxy(proxy).headers(headers).cookies(cookies).retry(MAX_RETRY, MILLISECONDS_SLEEP).retry(unlimit).retryStatusCodes(retryStatusCodes).failThrow(failThrow).execute();
					int statusCode = res.statusCode();
					if (!URIUtil.statusIsOK(statusCode)) {
						return new HttpResponse(this, request.statusCode(statusCode));
					}

					request.headers(res.headers()).cookies(res.cookies());
					this.body = res.body();
					return execute("BODY");
				}
				case "FILE" -> {
					fileInfo = ReadWriteUtil.orgin(session).readJSON();
					request.setUrl(url = fileInfo.getString("url"));
					fileName = fileInfo.getString("fileName");
					headers = StringUtil.jsonToMap(fileInfo.getString("header"));
					cookies = StringUtil.jsonToMap(fileInfo.getString("cookie"));
					key = fileInfo.getString("key");
					iv = fileInfo.getString("iv");
					links = fileInfo.getList("data", String.class);
					storage = new File(DEFAULT_FOLDER, fileName);
					var renew = fileInfo.getJSONObject("renew");
					if (renew != null) {
						status.putAll(renew.toMap(File.class, Long.class));
						fileInfo.remove("renew");
					}
					ReadWriteUtil.orgin(session).append(false).write(fileInfo.toString());  // 重置配置文件
				}
				default -> throw new HLSDownloadException("Unknown mode");
			}

			var folder = new File(DEFAULT_FOLDER, fileName.substring(0, fileName.lastIndexOf(".")));
			session = new File(folder, SESSION_SUFFIX); // 配置信息文件后缀
			FileUtil.createFolder(folder); // 创建文件夹
			Runnable breakPoint = () -> ReadWriteUtil.orgin(session).append(false).write(fileInfo.fluentPut("renew", status).toString());
			Thread abnormal;
			Runtime.getRuntime().addShutdownHook(abnormal = new Thread(breakPoint));
			var listenTask = ThreadUtil.start(listener);
			var statusCodes = new AtomicInteger(HttpStatus.SC_OK);
			var executor = Executors.newFixedThreadPool(MAX_THREADS); // 限制多线程
			for (int i = 0; i < links.size(); i++) {
				var file = new File(folder, i + ".ts");
				if (file.exists() && !status.containsKey(file)) {
					site++;
				} else {
					executor.execute(new ConsumerThread(i, (index) -> {
						int statusCode = FULL(links.get(index), status.getOrDefault(file, 0L), MAX_RETRY, file);
						if (URIUtil.statusIsOK(statusCode)) {
							status.remove(file);
							site++;
						} else {
							statusCodes.set(statusCode);
							executor.shutdownNow(); // 结束未开始的线程，并关闭线程池
						}
					}));
				}
			}
			ThreadUtil.waitEnd(executor); // 等待线程结束
			ThreadUtil.interrupt(listenTask); // 结束监听
			Runtime.getRuntime().removeShutdownHook(abnormal);

			long fileSize = 0;
			if (URIUtil.statusIsOK(statusCodes.get())) { // 验证下载状态
				try (var out = new FileOutputStream(storage)) {
					try {
						if (key.isEmpty()) {
							for (int i = 0; i < links.size(); i++) {
								var file = new File(folder, i + ".ts");
								var data = ReadWriteUtil.orgin(file).readBytes();
								out.write(data, 0, data.length);
								fileSize += data.length;
								file.delete();
							}
						} else { // AES/CBC/PKCS7Padding解密,PKCS5Padding兼容
							var cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
							var keySpec = new SecretKeySpec(AESUtil.decodeHex(key), "AES");
							var ivSpec = new IvParameterSpec(iv.isEmpty() ? new byte[16] : AESUtil.decodeHex(iv));
							cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
							for (int i = 0; i < links.size(); i++) {
								var file = new File(folder, i + ".ts");
								var data = cipher.doFinal(ReadWriteUtil.orgin(file).readBytes());
								out.write(data, 0, data.length);
								fileSize += data.length;
								file.delete();
							}
						}
						session.delete(); // 删除会话信息文件
						folder.delete(); // 删除文件夹
					} catch (Exception e) {
						throw new AESException(e);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				breakPoint.run();
				if (failThrow) {
					throw new HLSDownloadException("M3U8文件下载失败，状态码: " + statusCodes + " URL: " + url);
				}
				return new HttpResponse(this, request.statusCode(statusCodes.get()));
			}

			return new HttpResponse(this, request.setFileSize(fileSize).statusCode(HttpStatus.SC_OK));
		}

		private void initializationStatus() {
			schedule.set(0);
			site = 0;
			status.clear();
		}

		private int FULL(String url, long complete, int retry, File storage) {
			var piece = HttpsUtil.connect(url).proxy(proxy).headers(headers).header("range", "bytes=" + complete + "-").cookies(cookies).failThrow(failThrow).execute();
			int statusCode = piece.statusCode();
			return URIUtil.statusIsOK(statusCode) ? FULL(url, piece, complete, retry, storage) : unlimit || retry > 0 ? FULL(url, complete, retry - 1, storage) : statusCode;
		}

		private int FULL(String url, Response res, long complete, int retry, File storage) {
			var length = res.header("content-length"); // 获取文件大小
			long fileSize = length == null ? 0 : Long.parseLong(length);
			try (var in = res.bodyStream(); var out = new RandomAccessFile(storage, "rw")) {
				out.seek(complete);
				var buffer = new byte[DEFAULT_BUFFER_SIZE];
				for (int len; (len = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1; schedule.addAndGet(len), complete += len, status.put(storage, complete)) {
					out.write(buffer, 0, len);
				}
				if (fileSize == 0 || complete >= fileSize) return HttpStatus.SC_OK;
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (unlimit || retry > 0) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP);
				return FULL(url, complete, retry - 1, storage);
			}
			return HttpStatus.SC_REQUEST_TIMEOUT;
		}

	}

}
