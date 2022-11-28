package org.haic.often.website.api;

import org.jetbrains.annotations.Contract;

import java.util.Map;

/**
 * 微博接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:18
 */
public abstract class Weibo {

	/**
	 * 获取 新浪微博临时访客Cookies
	 *
	 * @return 新浪微博Cookies
	 */
	@Contract(pure = true)
	public abstract Map<String, String> getVisitorCookies();

}
