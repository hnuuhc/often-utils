package org.haic.often.website;

import org.haic.often.Symbol;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.StringUtil;
import org.haic.often.website.api.Github;
import org.haic.often.website.api.Weibo;
import org.haic.often.website.api.Youtube;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
		if (ipAddr.startsWith("[")) {
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
			return JSONObject.parseObject(HttpsUtil.connect(latestUrl).execute().body()).getList("assets", JSONObject.class).stream().collect(Collectors.toMap(l -> l.getString("name"), l -> l.getString("browser_download_url")));
		}

	}

	private static class YoutubeBuilder extends Youtube {

		private static final String playerBaseUrl = "https://www.youtube.com/s/player/4eb6b35d/player_ias.vflset/zh_CN/base.js";

		private Function<String, String> function;

		public Youtube updateFunction() {
			String body = HttpsUtil.connect(playerBaseUrl).proxy(foreignProxy).execute().body();
			// 截取解密代码段
			String methodCode = body.substring(body.indexOf("a=a.split(\"\")"));
			methodCode = methodCode.substring(0, methodCode.indexOf("}"));
			// 分割代码段
			List<String> methodCodeList = methodCode.replaceAll(";", "\n").lines().toList();
			// 截取解密代码段调用函数
			String commandString = methodCodeList.get(1);
			String functionName = commandString.substring(0, commandString.indexOf("."));
			String functionString = body.substring(body.indexOf("var " + functionName + "={"));
			functionString = functionString.substring(0, functionString.indexOf("};") + 1);
			// 去除已提取的函数名
			methodCodeList = methodCodeList.stream().map(l -> l.replaceAll(functionName + ".", "")).toList();
			// 开始构建解密函数
			List<Function<StringBuilder, StringBuilder>> functionList = new ArrayList<>();
			for (int i = 1; i < methodCodeList.size() - 1; i++) {
				String command = methodCodeList.get(i);
				String f = command.substring(0, command.indexOf("("));
				if (functionString.contains(f + ":function(a){a.reverse()}")) {
					functionList.add(StringBuilder::reverse);
				} else {
					int index = Integer.parseInt(command.substring(command.indexOf(",") + 1, command.length() - 1));
					if (functionString.contains(f + ":function(a,b){a.splice(0,b)}")) {
						functionList.add(sb -> sb.delete(0, index));
					} else if (functionString.contains(f + "function(a,b){var c=a[0];")) {
						functionList.add(sb -> {
							int sbIndex = index % sb.length();
							char c = sb.charAt(0);
							sb.setCharAt(0, sb.charAt(sbIndex));
							sb.setCharAt(sbIndex, c);
							return sb;
						});
					} else {
						throw new RuntimeException("构建解密函数失败");
					}
				}
			}
			this.function = signatureCipher -> {
				Map<String, String> signatureCipherSplit = StringUtil.toMap(signatureCipher, "&");
				StringBuilder sb = new StringBuilder(URIUtil.decode(signatureCipherSplit.get("s")));
				for (var list : functionList) sb = list.apply(sb);
				return URIUtil.decode(signatureCipherSplit.get("url")) + "&" + signatureCipherSplit.get("sp") + "=" + sb;
			};
			return this;
		}

		@Contract(pure = true)
		public String signatureCipherDecrypt(@NotNull String signatureCipher) {
			return function == null ? updateFunction().signatureCipherDecrypt(signatureCipher) : function.apply(signatureCipher);
		}

	}

}
