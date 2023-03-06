package org.haic.often.website;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.URIUtil;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Youtube
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/28 19:45
 */
public class Youtube {

	private static final String playerBaseUrl = "https://www.youtube.com/s/player/4eb6b35d/player_ias.vflset/zh_CN/base.js";

	private static Function<String, String> function;

	private static String proxy = "";

	/**
	 * 设置代理,用于链接
	 *
	 * @param proxy 代理
	 */
	public static void proxy(@NotNull String proxy) {
		Youtube.proxy = proxy;
	}

	/**
	 * 构建解密函数,由于会下载js代码,注意需要提前设置代理
	 */
	public static Function<String, String> updateFunction() {
		if (function != null) return function;
		var body = HttpsUtil.connect(playerBaseUrl).proxy(proxy).execute().body();
		// 截取解密代码段
		var methodCode = body.substring(body.indexOf("a=a.split(\"\")"));
		methodCode = methodCode.substring(0, methodCode.indexOf("}"));
		// 分割代码段
		var methodCodeList = methodCode.replaceAll(";", "\n").lines().toList();
		// 截取解密代码段调用函数
		var commandString = methodCodeList.get(1);
		var functionName = commandString.substring(0, commandString.indexOf("."));
		var functionString = body.substring(body.indexOf("var " + functionName + "={"));
		functionString = functionString.substring(0, functionString.indexOf("};") + 1);
		// 去除已提取的函数名
		methodCodeList = methodCodeList.stream().map(l -> l.replaceAll(functionName + ".", "")).toList();
		// 开始构建解密函数
		List<Function<StringBuilder, StringBuilder>> functionList = new ArrayList<>();
		for (int i = 1; i < methodCodeList.size() - 1; i++) {
			var command = methodCodeList.get(i);
			var f = command.substring(0, command.indexOf("("));
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
		return function = signatureCipher -> {
			var signatureCipherSplit = StringUtil.toMap(signatureCipher, "&");
			var sb = new StringBuilder(URIUtil.decode(signatureCipherSplit.get("s")));
			for (var list : functionList) sb = list.apply(sb);
			return URIUtil.decode(signatureCipherSplit.get("url")) + "&" + signatureCipherSplit.get("sp") + "=" + sb;
		};
	}

	/**
	 * signatureCipher解密,如果未构建解密函数,将会下载js代码,注意需要提前设置代理
	 *
	 * @param signatureCipher signatureCipher
	 * @return 已解密的signatureCipher
	 */
	@Contract(pure = true)
	public static String signatureCipherDecrypt(@NotNull String signatureCipher) {
		return updateFunction().apply(signatureCipher);
	}

}
