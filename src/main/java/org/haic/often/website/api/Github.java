package org.haic.often.website.api;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

import java.util.Map;

/**
 * Github接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:31
 */
public abstract class Github {

	/**
	 * 获取Github仓库最新发布的文件下载链接
	 *
	 * @param warehouse 仓库路径或仓库首页URL地址
	 * @return 列表 文件名 - 下载链接
	 */
	@Contract(pure = true)
	public abstract Map<String, String> getLatestReleases(@NotNull String warehouse);

}
