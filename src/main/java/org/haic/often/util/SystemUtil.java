package org.haic.often.util;

import org.haic.often.Symbol;
import org.haic.often.Terminal;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * 系统工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/24 18:54
 */
public class SystemUtil {

	/**
	 * 系统默认字符集编码
	 */
	public static final Charset DEFAULT_CHARSET = Charset.forName(System.getProperty("sun.jnu.encoding"));
	/**
	 * 系统名称
	 */
	public static final String OS = System.getProperty("os.name").toLowerCase();
	/**
	 * 是否 mac 系统
	 */
	public static final boolean osIsMacOsX = OS.startsWith("mac os");
	/**
	 * 是否 windows 系统
	 */
	public static final boolean osIsWindows = OS.startsWith("windows");
	/**
	 * 是否 windows xp 系统
	 */
	public static final boolean osIsWindowsXP = "windows xp".equals(OS);
	/**
	 * 是否 windows 2003 系统
	 */
	public static final boolean osIsWindows2003 = "windows 2003".equals(OS);
	/**
	 * 是否 windows vista 系统
	 */
	public static final boolean osIsWindowsVista = "windows vista".equals(OS);
	/**
	 * 是否 linux 系统
	 */
	public static final boolean osIsLinux = OS.startsWith("linux");
	/**
	 * 是否 windows 7 系统
	 */
	public static final boolean osIsWindowsWin7 = OS.startsWith("windows 7");
	/**
	 * 是否 windows 8 系统
	 */
	public static final boolean osIsWindowsWin8 = OS.startsWith("windows 8");
	/**
	 * 是否 android 系统
	 */
	public static final boolean osIsAndroid = OS.startsWith("android");
	/**
	 * 系统默认下载文件夹,未知系统将为根目录
	 */
	public static final File DEFAULT_DOWNLOAD_FOLDER = osIsWindows ? new File(SystemUtil.getDownloadsPath()) : osIsLinux ? new File(System.getProperty("user" + ".home"), "Download") : osIsAndroid ? new File("/sdcard/Download" + "/") : new File("/");
	/**
	 * 系统默认文件字符集编码
	 */
	public static final Charset DEFAULT_FILE_CHARSET = Charset.forName(System.getProperty("file.encoding"));
	/**
	 * 用户默认语言
	 */
	public static final String DEFAULT_LANGUAGE = System.getProperty("user.language");
	/**
	 * 用户默认主目录
	 */
	public static final String DEFAULT_USER_HOME = System.getProperty("user.home");
	/**
	 * 系统缓存文件夹
	 */
	public static final String DEFAULT_TEMP_DIR = System.getProperty("java.io.tmpdir");

	/**
	 * 打开资源管理器窗口
	 *
	 * @param folderPath 文件夹路径
	 * @return 操作是否成功
	 */
	@Contract(pure = true)
	public static boolean openDesktop(@NotNull String folderPath) {
		File folder = new File(folderPath);
		if (folder.isDirectory()) {
			try {
				Desktop.getDesktop().open(new File(folderPath));
				return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	/**
	 * 获取 默认下载文件夹路径
	 *
	 * @return 下载文件夹路径
	 */
	@Contract(pure = true)
	public static String getDownloadsPath() {
		return getDefaultDirectory("{374DE290-123F-4565-9164-39C4925E467B}");
	}

	/**
	 * 获取 默认文档文件夹路径
	 *
	 * @return 文档文件夹路径
	 */
	@Contract(pure = true)
	public static String getDocumentsPath() {
		return getDefaultDirectory("{F42EE2D3-909F-4907-8871-4C22FC0BF756}");
	}

	/**
	 * 获取Windows默认图片文件夹路径
	 *
	 * @return 图片文件夹路径
	 */
	@Contract(pure = true)
	public static String getPicturesPath() {
		return getDefaultDirectory("{0DDD015D-B06C-45D5-8C4C-F59713854639}");
	}

	/**
	 * 获取Windows默认音乐文件夹路径
	 *
	 * @return 音乐文件夹路径
	 */
	@Contract(pure = true)
	public static String getMusicPath() {
		return getDefaultDirectory("{A0C69A99-21C8-4671-8703-7934162FCF1D}");
	}

	/**
	 * 获取Windows默认视频文件夹路径
	 *
	 * @return 视频文件夹路径
	 */
	@Contract(pure = true)
	public static String getVideosPath() {
		return getDefaultDirectory("{35286A68-3C57-41A1-BBB1-0EAE73D76C95}");
	}

	/**
	 * 获取Windows系统默认文件夹路径
	 *
	 * @param id 字符串项名称
	 * @return 文件夹路径
	 */
	@Contract(pure = true)
	private static String getDefaultDirectory(String id) {
		String[] value = Terminal.command("REG", "QUERY", "HKEY_CURRENT_USER\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders", "/v", id).read().split(Symbol.SPACE);
		String src = value[value.length - 1];
		return src.startsWith("%USERPROFILE%") ? System.getenv("USERPROFILE") + src.substring(13) : src;
	}

}
