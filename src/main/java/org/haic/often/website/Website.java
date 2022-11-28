package org.haic.often.website;

import com.alibaba.fastjson2.JSONObject;
import org.haic.often.function.StringFunction;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.website.api.Github;
import org.haic.often.website.api.Weibo;
import org.haic.often.website.api.Youtube;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 网站接口实现类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:19
 */
public class Website {

	private Website() {}

	/**
	 * 获取微博实现类
	 *
	 * @return 此实现
	 */
	public static Weibo weibo() {
		return new WeiboBuilder();
	}

	/**
	 * 获取Github实现类
	 *
	 * @return 此实现
	 */
	public static Github github() {
		return new GithubBuilder();
	}

	/**
	 * 获取Github实现类
	 *
	 * @return 此实现
	 */
	public static Youtube youtube() {
		return new YoutubeBuilder();
	}

	private static class WeiboBuilder extends Weibo {

		private static final String genvisitorUrl = "https://passport.weibo.com/visitor/genvisitor";
		private static final String visitorUrl = "https://passport.weibo.com/visitor/visitor?a=incarnate&cb=cross_domain&from=weibo&t=";

		@Contract(pure = true)
		public Map<String, String> getVisitorCookies() {
			return HttpsUtil.connect(visitorUrl + StringUtil.jsonpToJSONObject(HttpsUtil.connect(genvisitorUrl).data("cb", "gen_callback").method(Method.POST).execute().body()).getJSONObject("data").getString("tid")).execute().cookies();
		}

	}

	private static class GithubBuilder extends Github {

		private static final String latestUrlformat = "https://api.github.com/repos/%s/releases/latest";

		@Contract(pure = true)
		public Map<String, String> getLatestReleases(@NotNull String warehouse) {
			String latestUrl = String.format(latestUrlformat, warehouse.startsWith("https://") ? warehouse.substring(warehouse.indexOf("com") + 4) : warehouse);
			return JSONObject.parseObject(HttpsUtil.connect(latestUrl).execute().body()).getJSONArray("assets").toList(JSONObject.class).stream().collect(Collectors.toMap(l -> l.getString("name"), l -> l.getString("browser_download_url")));
		}

	}

	private static class YoutubeBuilder extends Youtube {

		@Contract(pure = true)
		public String signatureCipherDecrypt(@NotNull String signatureCipher) {
			BiConsumer<StringBuilder, Integer> xG = (a, b) -> a.delete(0, b);
			BiConsumer<StringBuilder, Integer> vd = (a, b) -> a.reverse();
			StringFunction<String> Dxa = s -> {
				StringBuilder a = new StringBuilder(s);
				xG.accept(a, 2);
				vd.accept(a, 3);
				xG.accept(a, 3);
				vd.accept(a, 61);
				xG.accept(a, 1);
				return a.toString();
			};
			Map<String, String> signatureCipherSplit = StringUtil.toMap(signatureCipher, "&");
			return URIUtil.decode(signatureCipherSplit.get("url")) + "&" + signatureCipherSplit.get("sp") + "=" + Dxa.apply(URIUtil.decode(signatureCipherSplit.get("s")));
		}

	}

}
