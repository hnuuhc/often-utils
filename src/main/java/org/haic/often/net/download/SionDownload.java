package org.haic.often.net.download;

import org.haic.often.Judge;
import org.jetbrains.annotations.NotNull;
import org.haic.often.exception.DownloadException;
import org.haic.often.net.MimeType;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.HttpStatus;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.net.http.Response;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.thread.ConsumerThread;
import org.haic.often.util.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 网络文件 工具类
 * <p>
 * 用于文件的下载,非线程安全
 * <p>
 * 默认10线程下载,不应设置过高
 *
 * @author haicdust
 * @version 1.8.2
 * @since 2021/12/24 23:07
 */
public class SionDownload {

	private SionDownload() {
	}

	/**
	 * 公共静态连接newSession ()
	 * <p>
	 * 创建一个新Connection以用作会话。将为会话维护连接设置（用户代理、超时、URL 等）和 cookie
	 *
	 * @return 此连接，用于链接
	 */
	public static SionConnection newSession() {
		return new HttpConnection();
	}

	/**
	 * 公共静态连接连接（ 字符串 网址）<br/> 使用定义的请求 URL 创建一个新的Connection （会话）
	 *
	 * @param url 要连接的 URL
	 * @return 此连接，用于链接
	 */
	public static SionConnection connect(@NotNull String url) {
		return newSession().alterUrl(url);
	}

	/**
	 * 获取新的Download对象并设置配置文件<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param src session文件路径
	 * @return 此连接，用于链接
	 */
	public static SionConnection session(@NotNull String src) {
		return session(new File(src));
	}

	/**
	 * 获取新的Download对象并设置配置文件<br/> 配置文件 -> 包含待下载文件的下载信息的文件
	 *
	 * @param file session文件
	 * @return 此连接，用于链接
	 */
	public static SionConnection session(@NotNull File file) {
		return newSession().session(file);
	}

	private static class HttpResponse extends SionResponse {

		private final SionConnection conn;
		private final SionRequest request;

		private HttpResponse(SionConnection conn, SionRequest request) {
			this.conn = conn;
			this.request = request;
		}

		public int statusCode() {
			return request.statusCode();
		}

		public String fileName() {
			return request.getStorage().getName();
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

	private static class HttpConnection extends SionConnection {

		private String url; // 请求URL
		private String lastHash; // hash值,md5算法,用于判断服务器文件损坏
		private String fileName; // 文件名
		private String hash; // hash值
		private final String SESSION_SUFFIX = ".sion";

		private long fileSize; // 文件大小
		private int DEFAULT_BUFFER_SIZE = 8192;
		private int MILLISECONDS_SLEEP; // 重试等待时间
		private int MAX_RETRY; // 请求异常重试次数
		private int MAX_THREADS = 10; // 默认10线程下载
		private long PIECE_MAX_SIZE = 1048576; // 默认块大小，1M
		private boolean valid = true; // MD5效验
		private boolean unlimit;// 请求异常无限重试
		private boolean failThrow; // 错误异常
		private boolean rename; // 重命名
		private Proxy proxy = Proxy.NO_PROXY; // 代理
		private File storage; // 本地存储文件
		private File session; // 配置信息文件
		private File DEFAULT_FOLDER = SystemUtil.DEFAULT_DOWNLOAD_FOLDER;  // 存储目录
		private List<Integer> retryStatusCodes = new ArrayList<>();

		private Map<String, String> headers = new HashMap<>(); // headers
		private Map<String, String> cookies = new HashMap<>(); // cookies
		private SionMethod method = SionMethod.MULTITHREAD;// 下载模式

		private final SionRequest request = new SionRequest();
		private JSONObject fileInfo = new JSONObject();
		private Runnable listener;
		private long MAX_COMPLETED;
		private final AtomicLong schedule = new AtomicLong();
		private final Map<Long, Long> status = new ConcurrentHashMap<>();

		private HttpConnection() {
		}

		public SionConnection url(@NotNull String url) {
			request.setHash(this.hash = null);
			fileName = null;
			fileSize = 0;
			method = method == SionMethod.FILE ? SionMethod.MULTITHREAD : method;
			return alterUrl(url);
		}

		public SionConnection alterUrl(@NotNull String url) {
			request.setUrl(this.url = url);
			fileInfo.put("url", url);
			return this;
		}

		public SionConnection session(@NotNull String src) {
			return session(new File(src));
		}

		public SionConnection session(@NotNull File session) {
			if (!session.getName().endsWith(SESSION_SUFFIX)) {
				throw new DownloadException("Not is session file: " + session);
			} else if (session.isFile()) { // 如果设置配置文件下载，并且配置文件存在，获取信息
				fileInfo = ReadWriteUtil.orgin(session).readJSON();
				request.setUrl(url = fileInfo.getString("url"));
				request.setFileSize(fileSize = fileInfo.getLong("fileSize"));
				request.setHash(hash = fileInfo.getString("hash"));
				fileName = fileInfo.getString("fileName");
				headers = StringUtil.jsonToMap(fileInfo.getString("header"));
				cookies = StringUtil.jsonToMap(fileInfo.getString("cookie"));
			} else { // 配置文件不存在，抛出异常
				throw new DownloadException("Not found or not is file " + session);
			}
			this.method = SionMethod.FILE;
			this.session = session;
			return this;
		}

		public SionConnection userAgent(@NotNull String userAgent) {
			return header("user-agent", userAgent);
		}

		public SionConnection referrer(@NotNull String referrer) {
			return header("referer", referrer);
		}

		public SionConnection header(@NotNull String name, @NotNull String value) {
			headers.put(name, value);
			return this;
		}

		public SionConnection headers(@NotNull Map<String, String> headers) {
			this.headers.putAll(headers);
			return this;
		}

		public SionConnection cookie(@NotNull String name, @NotNull String value) {
			cookies.put(name, value);
			return this;
		}

		public SionConnection cookies(@NotNull Map<String, String> cookies) {
			this.cookies.putAll(cookies);
			return this;
		}

		public SionConnection auth(@NotNull String auth) {
			return header("authorization", auth.startsWith("Bearer ") ? auth : "Bearer " + auth);
		}

		public SionConnection thread(int nThread) {
			if (nThread < 1) {
				throw new DownloadException("thread Less than 1");
			}
			MAX_THREADS = nThread;
			return this;
		}

		public SionConnection fileSize(long fileSize) {
			this.fileSize = fileSize;
			return this;
		}

		public SionConnection method(@NotNull SionMethod method) {
			this.method = method;
			return this;
		}

		public SionConnection fileName(@NotNull String fileName) {
			this.fileName = FileUtil.illegalFileName(URIUtil.decode(fileName));
			FileUtil.fileNameValidity(fileName);
			return this;
		}

		public SionConnection rename(boolean rename) {
			this.rename = rename;
			return this;
		}

		public SionConnection socks(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		}

		public SionConnection proxy(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		}

		public SionConnection proxy(@NotNull Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		public SionConnection failThrow(boolean exit) {
			failThrow = exit;
			return this;
		}

		public SionConnection retry(int retry) {
			MAX_RETRY = retry;
			return this;
		}

		public SionConnection retry(int retry, int millis) {
			MAX_RETRY = retry;
			MILLISECONDS_SLEEP = millis;
			return this;
		}

		public SionConnection retry(boolean unlimit) {
			this.unlimit = unlimit;
			return this;
		}

		public SionConnection retry(boolean unlimit, int millis) {
			this.unlimit = unlimit;
			MILLISECONDS_SLEEP = millis;
			return this;
		}

		public SionConnection retryStatusCodes(int... statusCode) {
			retryStatusCodes = Arrays.stream(statusCode).boxed().toList();
			return this;
		}

		public SionConnection retryStatusCodes(List<Integer> retryStatusCodes) {
			this.retryStatusCodes = retryStatusCodes;
			return this;
		}

		public SionConnection bufferSize(int bufferSize) {
			DEFAULT_BUFFER_SIZE = bufferSize;
			return this;
		}

		public SionConnection hash(@NotNull String hash) {
			request.setHash(this.hash = hash.toLowerCase());
			return this;
		}

		public SionConnection valid(boolean valid) {
			this.valid = valid;
			return this;
		}

		public SionConnection pieceSize(long kb) {
			PIECE_MAX_SIZE = kb * 1024;
			return this;
		}

		public SionConnection folder(@NotNull String folder) {
			return folder(new File(folder));
		}

		public SionConnection folder(@NotNull File folder) {
			DEFAULT_FOLDER = folder;
			return this;
		}

		public SionConnection listener(@NotNull SionListener listener) {
			return listener(listener, 1000);
		}

		public SionConnection listener(@NotNull SionListener listener, int millis) {
			this.listener = () -> {
				long schedule, rate = this.schedule.get();
				do {
					ThreadUtil.waitThread(millis);
					schedule = this.schedule.get();
					listener.bytesTransferred(fileName, schedule - rate, schedule, fileSize);
					rate = schedule;
				} while (!Thread.currentThread().isInterrupted());
			};
			return this;
		}

		public SionResponse execute() {
			return execute(method);
		}

		private SionResponse execute(@NotNull SionMethod method) {
			initializationStatus(); // 初始化
			int MAX_THREADS = this.MAX_THREADS; // 复制全局线程被篡改
			switch (method) { // 配置信息
				case FILE -> {
					method = SionMethod.valueOf(fileInfo.getString("method"));
					MAX_THREADS = fileInfo.getInteger("threads");
					request.setStorage(storage = new File(DEFAULT_FOLDER, fileName));  // 获取其file对象
					var renew = fileInfo.getJSONObject("renew");
					if (storage.exists() && renew != null) {
						schedule.set(MAX_COMPLETED = renew.getLong("completed"));
						status.putAll(renew.getJSONObject("status").toMap(Long.class, Long.class));
						schedule.addAndGet(status.entrySet().stream().mapToLong(l -> l.getValue() - l.getKey()).sum());
					}
					fileInfo.remove("renew");
					ReadWriteUtil.orgin(session).append(false).write(fileInfo.toString());  // 重置配置文件
				}
				case FULL, PIECE, MULTITHREAD, MANDATORY -> {    // 获取文件信息
					var res = HttpsUtil.connect(url).proxy(proxy).headers(headers).cookies(cookies).retry(MAX_RETRY, MILLISECONDS_SLEEP).retry(unlimit).retryStatusCodes(retryStatusCodes).failThrow(failThrow).execute();
					// 获取URL连接状态
					int statusCode = res.statusCode();
					if (!URIUtil.statusIsOK(statusCode)) {
						return new HttpResponse(this, request.statusCode(statusCode));
					}
					request.headers(res.headers()).cookies(res.cookies());
					// 获取文件名
					if (Judge.isEmpty(fileName)) {
						var disposition = res.header("content-disposition");
						if (disposition == null || !disposition.contains("filename")) {
							var url = res.url(); // 可能为跳转链接,使用最终URL
							fileName = url.substring(url.lastIndexOf("/") + 1);
							fileName = URIUtil.decode(fileName.contains("?") ? fileName.substring(0, fileName.indexOf("?")) : fileName);
							fileName = fileName.contains(".") ? fileName : fileName + MimeType.getMimeSuffix(res.header("content-type")); // 尝试修复后缀
						} else {
							fileName = URIUtil.getFileNameForDisposition(disposition);
						}
						fileName = FileUtil.illegalFileName(fileName); // 文件名排除非法字符
						FileUtil.fileNameValidity(fileName);
					}

					// 获取待下载文件和配置文件对象
					request.setStorage(storage = new File(DEFAULT_FOLDER, fileName)); // 获取其file对象
					session = new File(storage + SESSION_SUFFIX); // 配置信息文件
					if (session.exists()) { // 转为会话配置
						session(session).method(method); // 防止下载类型篡改
						return execute(SionMethod.FILE);
					} else if (storage.exists()) { // 文件已存在
						if (rename) { // 重命名
							int count = 1, index = fileName.lastIndexOf(".");
							var head = index > 0 ? fileName.substring(0, index) : fileName;
							var suffix = index > 0 ? fileName.substring(index) : "";
							do {
								fileName = head + " - " + count++ + suffix;
								storage = new File(DEFAULT_FOLDER, fileName);
								session = new File(storage + SESSION_SUFFIX);
								if (session.exists()) { // 转为会话配置
									return session(session).execute();
								}
							} while (storage.exists());
							request.setStorage(storage); // 配置信息文件
						} else {
							return new HttpResponse(this, request.statusCode(HttpStatus.SC_OK));
						}
					}

					var contentLength = res.header("content-length"); // 获取文件大小
					request.setFileSize(fileSize = contentLength == null ? fileSize : Long.parseLong(contentLength));
					method = fileSize == 0 ? SionMethod.FULL : method;// 如果文件大小获取失败或线程为1，使用全量下载模式
					request.setHash(hash = Judge.isEmpty(hash) ? URIUtil.getHash(request.headers()) : hash);  // 获取文件hash
					// 创建并写入文件配置信息
					fileInfo.put("fileName", fileName);
					fileInfo.put("fileSize", fileSize);
					fileInfo.put("hash", hash);
					fileInfo.put("threads", MAX_THREADS);
					fileInfo.put("method", method.name());
					fileInfo.put("header", headers);
					fileInfo.put("cookie", cookies);
					ReadWriteUtil.orgin(session).write(fileInfo.toString());
				}
				default -> throw new DownloadException("Unknown mode");
			}

			FileUtil.createFolder(DEFAULT_FOLDER); // 创建文件夹
			Runnable breakPoint = () -> ReadWriteUtil.orgin(session).append(false).write(fileInfo.fluentPut("renew", new JSONObject().fluentPut("completed", MAX_COMPLETED).fluentPut("status", status)).toString());
			Thread abnormal;
			Runtime.getRuntime().addShutdownHook(abnormal = new Thread(breakPoint));
			var listenTask = ThreadUtil.start(listener);
			int statusCode;
			switch (method) {  // 开始下载
				case FULL -> statusCode = FULL(MAX_RETRY);
				case PIECE -> statusCode = MULTITHREAD((int) Math.ceil((double) fileSize / PIECE_MAX_SIZE), PIECE_MAX_SIZE, MAX_THREADS);
				case MULTITHREAD -> {
					int PIECE_COUNT = Math.min((int) Math.ceil((double) fileSize / PIECE_MAX_SIZE), MAX_THREADS);
					statusCode = MULTITHREAD(PIECE_COUNT, (long) Math.ceil((double) fileSize / PIECE_COUNT), MAX_THREADS);
				}
				case MANDATORY -> statusCode = MULTITHREAD(MAX_THREADS, (long) Math.ceil((double) fileSize / MAX_THREADS), MAX_THREADS);
				default -> throw new DownloadException("Unknown mode");
			}
			ThreadUtil.interrupt(listenTask);
			Runtime.getRuntime().removeShutdownHook(abnormal);
			if (!URIUtil.statusIsOK(statusCode)) { // 验证下载状态
				breakPoint.run(); // 下载失败写入断点
				if (failThrow) {
					throw new DownloadException("文件下载失败，状态码: " + statusCode + " URL: " + url);
				}
				return new HttpResponse(this, request.statusCode(statusCode));
			}

			// 效验文件完整性
			String fileHash;
			if (valid && !Judge.isEmpty(hash) && !(fileHash = FileUtil.hashGet(storage, hash)).equalsIgnoreCase(hash)) {
				storage.delete(); // 删除下载错误的文件
				String failThrowText;
				if (unlimit) {
					if (fileHash.equals(lastHash)) {
						failThrowText = "Server file is corrupt";
					} else {
						lastHash = fileHash;
						return execute(SionMethod.FILE);
					}
				} else {
					failThrowText = "File verification is not accurate";
				}
				if (failThrow) {
					throw new DownloadException(failThrowText + ", Server md5:" + hash + " Local md5: " + fileHash + " URL: " + url);
				} else {
					return new HttpResponse(this, request.statusCode(HttpStatus.SC_SERVER_RESOURCE_ERROR));
				}
			}

			session.delete(); // 删除会话信息文件
			return new HttpResponse(this, request.statusCode(HttpStatus.SC_OK));
		}

		private void initializationStatus() {
			schedule.set(0);
			MAX_COMPLETED = 0;
			status.clear();
		}

		/**
		 * 全量下载，下载获取文件信息并写入文件
		 *
		 * @param retry 重试次数
		 * @return 下载并写入是否成功(状态码)
		 */
		private int FULL(int retry) {
			var piece = HttpsUtil.connect(url).proxy(proxy).headers(headers).header("range", "bytes=" + MAX_COMPLETED + "-").cookies(cookies).failThrow(failThrow).execute();
			int statusCode = piece.statusCode();
			return URIUtil.statusIsOK(statusCode) ? FULL(piece, retry) : unlimit || retry > 0 ? FULL(retry - 1) : statusCode;
		}

		/**
		 * 全量下载，下载获取文件信息并写入文件
		 *
		 * @param res   网页Response对象
		 * @param retry 重试次数
		 * @return 下载并写入是否成功(状态码)
		 */
		private int FULL(Response res, int retry) {
			try (var in = res.bodyStream(); var out = new RandomAccessFile(storage, "rw")) {
				out.seek(MAX_COMPLETED);
				var buffer = new byte[DEFAULT_BUFFER_SIZE];
				for (int len; (len = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) != -1; MAX_COMPLETED = schedule.addAndGet(len)) {
					out.write(buffer, 0, len);
				}
				if (fileSize == 0 || MAX_COMPLETED >= fileSize) return HttpStatus.SC_OK;
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (unlimit || retry > 0) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP);
				return FULL(retry - 1);
			}
			return HttpStatus.SC_REQUEST_TIMEOUT;
		}

		private int MULTITHREAD(int PIECE_COUNT, long PIECE_SIZE, int MAX_THREADS) {
			var statusCodes = new AtomicInteger(HttpStatus.SC_OK);
			var addCompleted = new AtomicBoolean(true);
			var executor = Executors.newFixedThreadPool(MAX_THREADS); // 下载线程池
			for (long i = MAX_COMPLETED / PIECE_SIZE; i < PIECE_COUNT; i++) {
				executor.execute(new ConsumerThread(i, (index) -> { // 执行多线程程
					long start = index * PIECE_SIZE;
					long end = (index + 1 == PIECE_COUNT ? fileSize : (index + 1) * PIECE_SIZE) - 1;
					long flip = status.getOrDefault(start, start);
					int statusCode = flip >= end ? HttpStatus.SC_PARTIAL_CONTENT : writePiece(start, flip, end, MAX_RETRY);
					if (addCompleted.get() && end > MAX_COMPLETED) {
						addCompleted.set(false);
						long completed;
						while ((completed = status.getOrDefault(MAX_COMPLETED, MAX_COMPLETED)) == MAX_COMPLETED + PIECE_SIZE) {
							status.remove(MAX_COMPLETED);
							MAX_COMPLETED = completed;
						}
						addCompleted.set(true);
					}
					if (!URIUtil.statusIsOK(statusCode)) {
						statusCodes.set(statusCode);
						executor.shutdownNow(); // 结束未开始的线程，并关闭线程池
					}
				}));
			}
			ThreadUtil.waitEnd(executor); // 等待线程结束
			return statusCodes.get();
		}

		/**
		 * 分块下载，下载获取文件区块信息并写入文件
		 *
		 * @param start 块起始位
		 * @param flip  断点位置,用于修正
		 * @param end   块结束位
		 * @param retry 重试次数
		 * @return 下载并写入是否成功(状态码)
		 */
		private int writePiece(long start, long flip, long end, int retry) {
			var piece = HttpsUtil.connect(url).proxy(proxy).headers(headers).header("range", "bytes=" + flip + "-" + end).cookies(cookies).execute();
			int statusCode = piece.statusCode();
			return URIUtil.statusIsOK(statusCode) ? writePiece(start, flip, end, piece, retry) : unlimit || retry > 0 ? writePiece(start, flip, end, retry - 1) : statusCode;
		}

		/**
		 * 下载获取文件区块信息并写入文件
		 *
		 * @param start 块起始位
		 * @param flip  断点位置,用于修正
		 * @param end   块结束位
		 * @param piece 块Response对象
		 * @param retry 重试次数
		 * @return 下载并写入是否成功(状态码)
		 */
		private int writePiece(long start, long flip, long end, Response piece, int retry) {
			long count = 0;
			try (var inputStream = piece.bodyStream(); var out = new RandomAccessFile(storage, "rw")) {
				out.seek(flip);
				var buffer = new byte[DEFAULT_BUFFER_SIZE];
				for (int len; (len = inputStream.read(buffer)) != -1; count += len, status.put(start, flip + count), schedule.addAndGet(len)) {
					out.write(buffer, 0, len);
				}
				if (end - flip + 1 == count) return HttpStatus.SC_PARTIAL_CONTENT;
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (unlimit || retry > 0) {
				ThreadUtil.waitThread(MILLISECONDS_SLEEP);
				return writePiece(start, flip + count, end, retry - 1);
			}
			return HttpStatus.SC_REQUEST_TIMEOUT;
		}

	}

}
