package org.haic.often.net.download;

import org.haic.often.Terminal;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.list.SafetyLinkedHashMap;
import org.haic.often.util.SystemUtil;
import org.haic.often.util.ThreadUtil;

import java.io.File;
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

	private static boolean outPrint;
	private static boolean isOutPrint = true;
	private static boolean DEFAULT_RENAME;
	private static int MAX_THREADS = 10;
	private static int MAX_TASK_THREADS = 2;
	private static int MAX_LISTIN_INTERVAL = 1000;
	private static int retry; // 请求异常重试次数
	private static boolean unlimit;
	private static File DEFAULT_FOLDER = SystemUtil.DEFAULT_DOWNLOAD_FOLDER;
	private static String DEFAULT_PROXY = "";
	private static ExecutorService pool = Executors.newFixedThreadPool(MAX_TASK_THREADS); // 任务线程池

	private static final StringBuffer print = new StringBuffer(); // 控制台输出
	private static final SionListener listenerSion = (fileName, rate, schedule, fileSize) -> print.append(fileName).append(" - 下载进度: ").append(String.format("%.2f", (float) schedule * 100 / fileSize)).append("% 下载速率: ").append(rate > 1048575 ? String.format("%.2f", (float) rate / 1048576) + "mb" : String.format("%.2f", (float) rate / 1024) + "kb").append("/s\n");
	private static final HLSListener listenerHLS = (fileName, rate, written, total) -> print.append(fileName).append(" - 下载进度: ").append(written).append("/").append(total).append(" ").append("下载速率:" + " ").append(rate > 1048575 ? String.format("%.2f", (float) rate / 1048576) + "mb" : String.format("%.2f", (float) rate / 1024) + "kb").append("/s\n");
	private static final Set<String> listTask = new CopyOnWriteArraySet<>(); // 任务列表
	private static final SafetyLinkedHashMap<String, SionResponse> result = new SafetyLinkedHashMap<>(1000); // 存储下载结果
	private static final Timer timer = new Timer();

	private static final TimerTask task = new TimerTask() {
		@Override
		public void run() {
			Terminal.cls();  // 清空控制台
			int activeCount = Math.min(listTask.size(), MAX_TASK_THREADS); // 正在下载的任务数量
			System.out.println(print.append("正在下载: ").append(activeCount).append(" 等待下载: ").append(activeCount == MAX_TASK_THREADS ? listTask.size() - MAX_TASK_THREADS : 0));
			print.setLength(0); // 清空输出内容
			if (pool.isTerminated()) timer.cancel();  // 线程池为空退出监听
		}
	};

	/**
	 * 添加下载链接
	 *
	 * @param url 下载链接
	 */
	@Contract(pure = true)
	public static void download(@NotNull String url) {
		download(url, new HashMap<>());
	}

	/**
	 * 添加下载链接
	 *
	 * @param url     下载链接
	 * @param headers 请求头参数
	 */
	@Contract(pure = true)
	public static void download(@NotNull String url, @NotNull Map<String, String> headers) {
		if (url.startsWith("http") && !listTask.contains(url)) {
			listTask.add(url);
			if ((url.contains("?") ? url.substring(0, url.indexOf("?")) : url).endsWith(".m3u8")) {
				pool.execute(Thread.ofPlatform().unstarted(() -> {
					result.put(url, HLSDownload.connect(url).headers(headers).thread(MAX_THREADS).listener(listenerHLS, MAX_LISTIN_INTERVAL).rename(DEFAULT_RENAME).proxy(DEFAULT_PROXY).folder(DEFAULT_FOLDER).retry(retry).retry(unlimit).execute());
					listTask.remove(url);
				}));
			} else {
				pool.execute(Thread.ofPlatform().unstarted(() -> {
					result.put(url, SionDownload.connect(url).headers(headers).thread(MAX_THREADS).listener(listenerSion, MAX_LISTIN_INTERVAL).rename(DEFAULT_RENAME).proxy(DEFAULT_PROXY).folder(DEFAULT_FOLDER).retry(retry).retry(unlimit).execute());
					listTask.remove(url);
				}));
			}
			outPrint();
		}
	}

	/**
	 * 在请求超时或者指定状态码发生时，进行重试
	 *
	 * @param retry 重试次数
	 */
	@Contract(pure = true)
	public static void retry(int retry) {
		Sion.retry = retry;
	}

	/**
	 * 在请求超时或者指定状态码发生时，无限进行重试，直至状态码正常返回
	 *
	 * @param unlimit 启用无限重试, 默认false
	 */
	@Contract(pure = true)
	public static void retry(boolean unlimit) {
		Sion.unlimit = unlimit;
	}

	/**
	 * 设置运行文件存在时重命名,默认为false
	 *
	 * @param rename 布尔值
	 */
	@Contract(pure = true)
	public static void rename(boolean rename) {
		DEFAULT_RENAME = rename;
	}

	/**
	 * 设置监听器最大监听间隔,默认为 1000 毫秒
	 *
	 * @param millis 最大监听间隔(毫秒)
	 */
	@Contract(pure = true)
	public static void listen(int millis) {
		MAX_LISTIN_INTERVAL = millis;
	}

	/**
	 * 修改最大存储结果数量,默认默认值为1000
	 *
	 * @param max 最大存储结果数量
	 */
	@Contract(pure = true)
	public static void maxResult(int max) {
		result.maxCapacity(max);
	}

	/**
	 * 设置下载任务使用的代理
	 *
	 * @param ipAddr 代理
	 */
	@Contract(pure = true)
	public static void proxy(@NotNull String ipAddr) {
		DEFAULT_PROXY = ipAddr;
	}

	/**
	 * 设置文件存放目录
	 *
	 * @param folder 存放目录
	 */
	@Contract(pure = true)
	public static void folder(@NotNull String folder) {
		DEFAULT_FOLDER = new File(folder);
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
	 */
	@Contract(pure = true)
	private static void outPrint() {
		if (isOutPrint) {
			isOutPrint = false;
			if (outPrint) timer.schedule(task, MAX_LISTIN_INTERVAL, MAX_LISTIN_INTERVAL);
		}
	}

	/**
	 * 设置多线程下载的线程数,默认值为 10
	 * <p>
	 * 必须在添加下载任务之前设置,否则仅之后添加的任务生效
	 *
	 * @param nThread 线程数量
	 */
	@Contract(pure = true)
	public static void thread(int nThread) {
		MAX_THREADS = nThread;
	}

	/**
	 * 修改同时下载任务线程数量,默认值为 2
	 * <p>
	 * 如果设置值小于当前默认值,则正在进行的任务将会继续完成
	 * <p>
	 * 如果更大，则将在需要时启动新线程以执行任何排队的任务
	 *
	 * @param nThread 线程数量
	 */
	@Contract(pure = true)
	public static void taskThread(int nThread) {
		((ThreadPoolExecutor) pool).setCorePoolSize(MAX_TASK_THREADS = nThread);
	}

	/**
	 * 启用控制台的下载状态输出
	 *
	 * @param outPrint 是否启用,默认关闭
	 */
	@Contract(pure = true)
	public static void outPrint(boolean outPrint) {
		Sion.outPrint = outPrint;
		outPrint();
	}

	/**
	 * 如果线程是关闭的,将重启线程池
	 */
	@Contract(pure = true)
	public static void reboot() {
		if (pool.isShutdown()) {
			pool = Executors.newFixedThreadPool(MAX_TASK_THREADS);
		}
		outPrint();
	}

	/**
	 * 阻塞当前线程,等待下载任务全部结束
	 * <p>
	 * 该方法会关闭线程池,如果之后还要添加任务,必须使用 {@link #reboot} 方法重建线程池
	 */
	@Contract(pure = true)
	public static void waitEnd() {
		ThreadUtil.waitEnd(pool);
	}

}
