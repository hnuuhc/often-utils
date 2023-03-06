package org.haic.often.website;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.Method;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.util.StringUtil;

import java.util.Map;

/**
 * 微博
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:18
 */
public class Weibo {

	private static final String genvisitorUrl = "https://passport.weibo.com/visitor/genvisitor";
	private static final String visitorUrl = "https://passport.weibo.com/visitor/visitor?a=incarnate&cb=cross_domain&from=weibo&t=";

	private static String proxy = ""; // 代理

	/**
	 * 设置代理,用于链接
	 *
	 * @param proxy 代理
	 */
	public static void proxy(@NotNull String proxy) {
		Weibo.proxy = proxy;
	}

	/**
	 * 获取 新浪微博临时访客Cookies
	 *
	 * @return 新浪微博Cookies
	 */
	@Contract(pure = true)
	public static Map<String, String> getVisitorCookies() {
		return HttpsUtil.connect(visitorUrl + StringUtil.jsonpToJSONObject(HttpsUtil.connect(genvisitorUrl).data("cb", "gen_callback").method(Method.POST).proxy(proxy).execute().body()).getJSONObject("data").getString("tid")).proxy(proxy).execute().cookies();
	}

}
