package org.haic.often.website;

import com.alibaba.fastjson2.JSONObject;
import org.haic.often.Symbol;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.website.api.Github;
import org.haic.often.website.api.Weibo;
import org.haic.often.website.api.Youtube;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网站接口实现类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:19
 */
public class Website {

	private static Proxy foreignProxy = Proxy.NO_PROXY; // 代理

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

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于请求的代理,仅需要代理的网站才会使用
	 *
	 * @param ipAddr 代理地址 格式 - host:port
	 */
	@Contract(pure = true)
	public static void proxy(@NotNull String ipAddr) {
		if (ipAddr.startsWith(Symbol.OPEN_BRACKET)) {
			proxy(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
		} else {
			int index = ipAddr.lastIndexOf(":");
			proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
		}
	}

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于请求的代理,仅需要代理的网站才会使用
	 *
	 * @param host 代理地址
	 * @param port 代理端口
	 */
	@Contract(pure = true)
	public static void proxy(@NotNull String host, int port) {
		proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
	}

	/**
	 * 连接代理（ @NotNull  Proxy 代理）<br/>
	 * 设置用于请求的代理,仅需要代理的网站才会使用
	 *
	 * @param proxy 要使用的代理
	 */
	@Contract(pure = true)
	public static void proxy(@NotNull Proxy proxy) {
		foreignProxy = proxy;
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

		private static final String playerBaseUrl = "https://www.youtube.com/s/player/4eb6b35d/player_ias.vflset/zh_CN/base.js";

		@Contract(pure = true)
		public String signatureCipherDecrypt(@NotNull String signatureCipher) {
			Map<String, String> signatureCipherSplit = StringUtil.toMap(signatureCipher, "&");
			StringBuilder sb = new StringBuilder(URIUtil.decode(signatureCipherSplit.get("s")));

			String body = HttpsUtil.connect(playerBaseUrl).proxy(foreignProxy).execute().body();
			// 截取解密代码段
			String methodCode = body.substring(body.indexOf("a=a.split(\"\")"));
			methodCode = methodCode.substring(0, methodCode.indexOf("}"));
			// 分割代码段
			List<String> methodCodeList = methodCode.replaceAll(";", "\n").lines().toList();
			// 截取解密代码段调用函数
			String command = methodCodeList.get(1);
			String functionName = command.substring(0, command.indexOf("."));
			String function = body.substring(body.indexOf("var " + functionName + "={"));
			function = function.substring(0, function.indexOf("};") + 1);
			// 去除已提取的函数名
			methodCodeList = methodCodeList.stream().map(l -> l.replaceAll(functionName + ".", "")).toList();
			// 开始解密
			for (int i = 1; i < methodCodeList.size() - 1; i++) {
				command = methodCodeList.get(i);
				String f = command.substring(0, command.indexOf("("));
				if (function.contains(f + ":function(a){a.reverse()}")) {
					sb.reverse();
				} else if (function.contains(f + ":function(a,b){a.splice(0,b)}")) {
					//noinspection DuplicateExpressions
					sb.delete(0, Integer.parseInt(command.substring(command.indexOf(",") + 1, command.length() - 1)));
				} else if (function.contains(f + "function(a,b){var c=a[0];")) {
					//noinspection DuplicateExpressions
					int index = Integer.parseInt(command.substring(command.indexOf(",") + 1, command.length() - 1)) % sb.length();
					char c = sb.charAt(0);
					sb.setCharAt(0, sb.charAt(index));
					sb.setCharAt(index, c);
				} else {
					// 格式化内容,用于抛出异常
					function = function.replaceAll(";", ";\n    ");
					function = function.replaceAll("=\\{", "={\n");
					function = function.replaceAll("\\)\\{", "){\n    ");
					function = function.replaceAll("},", "}");
					function = function.replaceAll("\\)}", ")\n}");
					function = function.replaceAll("}}", "}\n}");
					function = function.replaceAll("=", " = ");
					throw new RuntimeException("\n" + methodCode + "\n----------\n" + function);
				}
			}
			return URIUtil.decode(signatureCipherSplit.get("url")) + "&" + signatureCipherSplit.get("sp") + "=" + sb;
		}

	}

}
