package org.haic.often.website;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.parser.json.JSONObject;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Github
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:31
 */
public class Github {

	private static final String latestUrlformat = "https://api.github.com/repos/%s/releases/latest";

	private static String proxy = ""; // 代理

	/**
	 * 设置代理,用于链接
	 *
	 * @param proxy 代理
	 */
	public static void proxy(@NotNull String proxy) {
		Github.proxy = proxy;
	}

	/**
	 * 获取Github仓库最新发布的文件下载链接
	 *
	 * @param warehouse 仓库路径或仓库首页URL地址
	 * @return 列表 文件名 - 下载链接
	 */
	@Contract(pure = true)
	public static Map<String, String> getLatestReleases(@NotNull String warehouse) {
		var latestUrl = String.format(latestUrlformat, warehouse.startsWith("https://") ? warehouse.substring(warehouse.indexOf("com") + 4) : warehouse);
		return JSONObject.parseObject(HttpsUtil.connect(latestUrl).proxy(proxy).execute().body()).getList("assets", JSONObject.class).stream().collect(Collectors.toMap(l -> l.getString("name"), l -> l.getString("browser_download_url")));
	}

}
