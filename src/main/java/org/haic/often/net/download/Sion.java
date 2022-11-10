package org.haic.often.net.download;

import org.haic.often.Symbol;
import org.haic.often.Terminal;
import org.haic.often.list.SafetyLinkedHashMap;
import org.haic.often.util.SystemUtil;
import org.haic.often.util.ThreadUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 下载接口,内部维护了一个线程池
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/27 0:33
 */
public class Sion {

	private boolean outPrint;
	private boolean isOutPrint = true;
	private boolean DEFAULT_RENAME;
	private int MAX_THREADS = 10;
	private int MAX_TASK_THREADS = 2;
	private int MAX_LISTIN_INTERVAL = 1000;
	private int MILLISECONDS_SLEEP; // 重试等待时间
	private int retry; // 请求异常重试次数
	private File DEFAULT_FOLDER = SystemUtil.DEFAULT_DOWNLOAD_FOLDER;
	private Proxy proxy = Proxy.NO_PROXY;
	private ExecutorService pool = Executors.newFixedThreadPool(MAX_TASK_THREADS); // 任务线程池

	private final StringBuffer print = new StringBuffer(); // 控制台输出
	private final SionListener listenerSion = (fileName, rate, schedule, fileSize) -> print.append(new StringBuilder().append(fileName).append(" - 下载进度: ").append(String.format("%.2f", (float) schedule * 100 / fileSize)).append("% 下载速率: ").append(rate > 1048575 ? String.format("%.2f", (float) rate / 1048576) + "mb" : String.format("%.2f", (float) rate / 1024) + "kb").append("/s\n"));
	private final HLSListener listenerHLS = (fileName, rate, written, total) -> print.append(new StringBuilder().append(fileName).append(" - 下载进度: ").append(written).append("/").append(total).append(" ")).append("下载速率:" + " ").append(rate > 1048575 ? String.format("%.2f", (float) rate / 1048576) + "mb" : String.format("%.2f", (float) rate / 1024) + "kb").append("/s\n");
	private final Set<String> listTask = new CopyOnWriteArraySet<>(); // 任务列表
	private final SafetyLinkedHashMap<String, SionResponse> result = new SafetyLinkedHashMap<>(1000); // 存储下载结果
	private final Timer timer = new Timer();
	private final TimerTask task = new TimerTask() {
		@Override
		public void run() {
			Terminal.cls();  // 清空控制台
			int activeCount = Math.min(listTask.size(), MAX_TASK_THREADS); // 正在下载的任务数量
			System.out.println(print.append("正在下载: ").append(activeCount).append(" 等待下载: ").append(activeCount == MAX_TASK_THREADS ? listTask.size() - MAX_TASK_THREADS : 0));
			print.setLength(0); // 清空输出内容
			if (pool.isTerminated()) { // 线程池为空退出监听
				timer.cancel();
			}
		}
	};

	public Sion() {}

	/**
	 * 添加下载链接
	 *
	 * @param url 下载链接
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion download(@NotNull String url) {
		return download(url, new HashMap<>());
	}

	/**
	 * 添加下载链接
	 *
	 * @param url     下载链接
	 * @param headers 请求头参数
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion download(@NotNull String url, @NotNull Map<String, String> headers) {
		if (url.startsWith("http") && !listTask.contains(url)) {
			listTask.add(url);
			if ((url.contains(Symbol.QUESTION) ? url.substring(0, url.indexOf(Symbol.QUESTION)) : url).endsWith(".m3u8")) {
				pool.execute(() -> {
					result.put(url, HLSDownload.connect(url).headers(headers).thread(MAX_THREADS).listener(listenerHLS, MAX_LISTIN_INTERVAL).rename(DEFAULT_RENAME).proxy(proxy).folder(DEFAULT_FOLDER).retry(retry, MILLISECONDS_SLEEP).execute());
					listTask.remove(url);
				});
			} else {
				pool.execute(() -> {
					result.put(url, SionDownload.connect(url).headers(headers).thread(MAX_THREADS).listener(listenerSion, MAX_LISTIN_INTERVAL).rename(DEFAULT_RENAME).proxy(proxy).folder(DEFAULT_FOLDER).retry(retry, MILLISECONDS_SLEEP).execute());
					listTask.remove(url);
				});
			}
		}
		return outPrint();
	}

	/**
	 * 在请求超时或者指定状态码发生时，进行重试
	 *
	 * @param retry 重试次数
	 * @return this
	 */
	@Contract(pure = true)
	public Sion retry(int retry) {
		this.retry = retry;
		return this;
	}

	/**
	 * 在请求超时或者指定状态码发生时，进行重试
	 *
	 * @param retry  重试次数
	 * @param millis 重试等待时间(毫秒)
	 * @return this
	 */
	@Contract(pure = true)
	public Sion retry(int retry, int millis) {
		this.retry = retry;
		this.MILLISECONDS_SLEEP = millis;
		return this;
	}

	/**
	 * 设置运行文件存在时重命名,默认为false
	 *
	 * @param rename 布尔值
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion rename(boolean rename) {
		DEFAULT_RENAME = rename;
		return this;
	}

	/**
	 * 设置监听器最大监听间隔,默认为 1000 毫秒
	 *
	 * @param millis 最大监听间隔(毫秒)
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion listen(int millis) {
		MAX_LISTIN_INTERVAL = millis;
		return this;
	}

	/**
	 * 修改最大存储结果数量,默认默认值为1000
	 *
	 * @param max 最大存储结果数量
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion maxResult(int max) {
		result.maxCapacity(max);
		return this;
	}

	/**
	 * 设置下载任务使用的代理
	 *
	 * @param ipAddr 代理
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion proxy(@NotNull String ipAddr) {
		if (ipAddr.startsWith(Symbol.OPEN_BRACKET)) {
			proxy(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(Symbol.COLON) + 1)));
		} else {
			int index = ipAddr.lastIndexOf(Symbol.COLON);
			proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
		}
		return this;
	}

	/**
	 * 设置下载任务使用的代理
	 *
	 * @param host 代理地址
	 * @param port 代理端口
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion proxy(@NotNull String host, int port) {
		proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
		return this;
	}

	/**
	 * 设置文件存放目录
	 *
	 * @param folder 存放目录
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion folder(@NotNull String folder) {
		return folder(new File(folder));
	}

	/**
	 * 设置文件存放目录
	 *
	 * @param folder 存放目录
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion folder(@NotNull File folder) {
		DEFAULT_FOLDER = folder;
		return this;
	}

	/**
	 * 获取所有下载链接的结果
	 *
	 * @return 所有的下载结果
	 */
	@Contract(pure = true)
	public Map<String, SionResponse> getResults() {
		return result;
	}

	/**
	 * 获取指定下载链接的结果
	 *
	 * @param url 下载链接
	 * @return 下载结果
	 */
	@Contract(pure = true)
	public SionResponse getResult(@NotNull String url) {
		return result.getOrDefault(url, null);
	}

	/**
	 * 控制台输出
	 *
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	private Sion outPrint() {
		if (isOutPrint) {
			isOutPrint = false;
			if (outPrint) {
				timer.schedule(task, MAX_LISTIN_INTERVAL, MAX_LISTIN_INTERVAL);
			}
		}
		return this;
	}

	/**
	 * 设置多线程下载的线程数,默认值为 10
	 * <p>
	 * 必须在添加下载任务之前设置,否则仅之后添加的任务生效
	 *
	 * @param nThread 线程数量
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion thread(int nThread) {
		MAX_THREADS = nThread;
		return this;
	}

	/**
	 * 修改同时下载任务线程数量,默认值为 2
	 * <p>
	 * 如果设置值小于当前默认值,则正在进行的任务将会继续完成
	 * <p>
	 * 如果更大，则将在需要时启动新线程以执行任何排队的任务
	 *
	 * @param nThread 线程数量
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion taskThread(int nThread) {
		((ThreadPoolExecutor) pool).setCorePoolSize(MAX_TASK_THREADS = nThread);
		return this;
	}

	/**
	 * 启用控制台的下载状态输出
	 *
	 * @param outPrint 是否启用,默认关闭
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion outPrint(boolean outPrint) {
		this.outPrint = outPrint;
		return outPrint();
	}

	/**
	 * 如果线程是关闭的,将重启线程池
	 *
	 * @return 此连接, 用于链接
	 */
	@Contract(pure = true)
	public Sion reboot() {
		if (pool.isShutdown()) {
			pool = Executors.newFixedThreadPool(MAX_TASK_THREADS);
		}
		return outPrint();
	}

	/**
	 * 阻塞当前线程,等待下载任务全部结束
	 * <p>
	 * 该方法会关闭线程池,如果之后还要添加任务,必须使用 {@link #reboot} 方法重建线程池
	 */
	@Contract(pure = true)
	public Sion waitEnd() {
		ThreadUtil.waitEnd(pool);
		return this;
	}

}
