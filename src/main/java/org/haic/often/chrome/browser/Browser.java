package org.haic.often.chrome.browser;

import org.jetbrains.annotations.NotNull;
import org.haic.often.parser.json.JSONObject;

import java.io.File;
import java.util.Map;

public abstract class Browser {

	protected File home;
	protected File storage;

	/**
	 * 设置个人资料名称,用于切换不同用户
	 *
	 * @param name 个人资料名称
	 * @return 此方法
	 */
	public abstract Browser setProfile(@NotNull String name);

	/**
	 * 设置待解密数据复制到指定目录,默认为系统缓存文件夹
	 *
	 * @param folder 文件夹路径
	 * @return 此方法
	 */
	public abstract Browser setTempDir(@NotNull String folder);

	/**
	 * 获取并解密全部数据
	 *
	 * @return 全部数据
	 */
	public abstract JSONObject getForAll();

	/**
	 * 获取并解密指定域名的数据
	 *
	 * @param domain 域名
	 * @return 指定域名的数据
	 */
	public abstract Map<String, String> getForDomain(@NotNull String domain);

}
