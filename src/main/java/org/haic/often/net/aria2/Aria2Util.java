package org.haic.often.net.aria2;

import org.haic.often.Symbol;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.Base64Util;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aria2 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 18:43
 */
public class Aria2Util {
	private Aria2Util() {
	}

	/**
	 * aria2RpcUrl: <a href="http://localhost:6800/jsonrpc">http://localhost:6800/jsonrpc</a>
	 *
	 * @return this
	 */
	public static Aria2Connection connect() {
		return connect("localhost", 6800);
	}

	/**
	 * 设置 aria2RpcUrl
	 *
	 * @param host 域名
	 * @param port 端口
	 * @return this
	 */
	@SuppressWarnings("HttpUrlsUsage")
	public static Aria2Connection connect(@NotNull String host, int port) {
		return new AriaConnection("http://" + host + ":" + port + "/jsonrpc");
	}

	private static class AriaConnection extends Aria2Connection {

		private final String aria2RpcUrl;
		private String token = ""; // 密钥
		private Proxy proxy = Proxy.NO_PROXY;
		private Map<String, String> rpcParams = new HashMap<>();
		private Map<String, String> rpcHeaders = new HashMap<>();
		private Map<String, Map<String, String>> listUrl = new HashMap<>();
		private Map<Aria2Method, JSONArray> sessions = new HashMap<>();

		private AriaConnection(@NotNull String aria2RpcUrl) {
			this.aria2RpcUrl = aria2RpcUrl;
		}

		public Aria2Connection newRequest() {
			listUrl = new HashMap<>();
			sessions = new HashMap<>();
			return this;
		}

		public Aria2Connection socks(@NotNull String ipAddr) {
			if (ipAddr.startsWith("[")) {
				return socks(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
			} else {
				int index = ipAddr.lastIndexOf(":");
				return socks(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		public Aria2Connection socks(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
		}

		public Aria2Connection proxy(@NotNull String ipAddr) {
			if (ipAddr.startsWith("[")) {
				return proxy(ipAddr.substring(1, ipAddr.indexOf(Symbol.CLOSE_BRACKET)), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
			} else {
				int index = ipAddr.lastIndexOf(":");
				return proxy(ipAddr.substring(0, index), Integer.parseInt(ipAddr.substring(index + 1)));
			}
		}

		public Aria2Connection proxy(@NotNull String host, int port) {
			return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
		}

		public Aria2Connection proxy(@NotNull Proxy proxy) {
			this.proxy = proxy;
			return this;
		}

		public Aria2Connection folder(@NotNull String folderPath) {
			return rpcParams("dir", folderPath);
		}

		public Aria2Connection folder(@NotNull File folder) {
			return folder(folder.getPath());
		}

		public Aria2Connection rpcToken(@NotNull String token) {
			this.token = token;
			return this;
		}

		public Aria2Connection rpcParams(@NotNull String name, @NotNull String value) {
			rpcParams.put(name, value);
			return this;
		}

		public Aria2Connection rpcParams(@NotNull Map<String, String> params) {
			rpcParams.putAll(params);
			return this;
		}

		public Aria2Connection setRpcParams(@NotNull Map<String, String> params) {
			rpcParams = params;
			return this;
		}

		public Aria2Connection rpcProxy(@NotNull String host, int post) {
			return rpcProxy(host + ":" + post);
		}

		public Aria2Connection rpcProxy(@NotNull String ipAddr) {
			rpcParams.put("all-proxy", ipAddr);
			return this;
		}

		public Aria2Connection rpcHeader(@NotNull String name, @NotNull String value) {
			rpcHeaders.put(name, value);
			return this;
		}

		public Aria2Connection rpcHeaders(@NotNull Map<String, String> headers) {
			rpcHeaders.putAll(headers);
			return this;
		}

		public Aria2Connection setRpcHeaders(@NotNull Map<String, String> headers) {
			rpcHeaders = headers;
			return this;
		}

		public Aria2Connection setRpcUserAgent(@NotNull String userAgent) {
			rpcHeaders.put("user-agent", userAgent);
			return this;
		}

		public Aria2Connection addUrl(@NotNull String url) {
			return addUrl(url, new HashMap<>());
		}

		public Aria2Connection addUrl(@NotNull String url, @NotNull Map<String, String> headers) {
			listUrl.put(url, headers);
			return this;
		}

		public Aria2Connection addUrl(@NotNull List<String> listUrl) {
			return addUrl(listUrl, new HashMap<>());
		}

		public Aria2Connection addUrl(@NotNull List<String> listUrl, @NotNull Map<String, String> headers) {
			listUrl.forEach(l -> addUrl(l, headers));
			return this;
		}

		public Aria2Connection session(@NotNull Aria2Method method) {
			return session(method, new JSONArray());
		}

		public Aria2Connection session(@NotNull Aria2Method method, @NotNull JSONArray params) {
			sessions.put(method, params);
			return this;
		}

		public Aria2Connection session(@NotNull Aria2Method method, @NotNull String gid) {
			return session(method, new JSONArray().fluentAdd(List.of(gid)));
		}

		public Aria2Connection remove(@NotNull String gid) {
			return session(Aria2Method.REMOVE, gid);
		}

		public Aria2Connection pause(@NotNull String gid) {
			return session(Aria2Method.PAUSE, gid);
		}

		public Aria2Connection pauseAll() {
			return session(Aria2Method.PAUSEALL);
		}

		public Aria2Connection unpause(@NotNull String gid) {
			return session(Aria2Method.UNPAUSE, gid);
		}

		public Aria2Connection unpauseAll() {
			return session(Aria2Method.UNPAUSEALL);
		}

		public Aria2Connection tellStatus(@NotNull String gid) {
			return session(Aria2Method.TELLSTATUS, gid);
		}

		public Aria2Connection changeOption(@NotNull String gid, @NotNull Map<String, String> option) {
			return session(Aria2Method.CHANGEOPTION, new JSONArray().fluentAdd(gid).fluentAdd(option));
		}

		public String get() {
			return HttpsUtil.connect(aria2RpcUrl).data("params", Base64Util.encode(rpcSessionBody())).proxy(proxy).execute().body();
		}

		public String post() {
			return HttpsUtil.connect(aria2RpcUrl).requestBody(rpcSessionBody()).proxy(proxy).post().body();
		}

		private String rpcSessionBody() {
			var sessionsJson = JSONArray.parseArray(Stream.concat(rpcUrlSession().stream(), rpcSession().stream()).toList());
			return sessionsJson.size() == 1 ? sessionsJson.getJSONObject(0).toString() : sessionsJson.toString();
		}

		private JSONArray rpcSession() {
			return JSONArray.parseArray(sessions.entrySet().stream().map(l -> rpcSessionHead(l.getKey()).fluentPut("params", l.getValue().fluentAdd("token:" + token))).toList());
		}

		private JSONArray rpcUrlSession() {
			var sessionsJson = new JSONArray();
			for (var entry : listUrl.entrySet()) {
				var url = entry.getKey();
				var method = url.endsWith(".torrent") || Base64Util.isBase64(url) ? Aria2Method.ADDTORRENT : url.endsWith(".xml") ? Aria2Method.ADDMETALINK : Aria2Method.ADDURI;
				sessionsJson.add(rpcSessionHead(method).fluentPut("params", new JSONArray().fluentAdd("token:" + token).fluentAdd(List.of(url)).fluentAdd(new HashMap<>() {{
					putAll(rpcParams);
					put("header", Stream.concat(rpcHeaders.entrySet().stream(), entry.getValue().entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2)).entrySet().stream().map(l -> l.getKey() + ":" + l.getValue()).toList());
				}})));
			}
			return sessionsJson;
		}

		private JSONObject rpcSessionHead(@NotNull Aria2Method method) {
			return new JSONObject().fluentPut("id", UUID.randomUUID().toString()).fluentPut("jsonrpc", "2.0").fluentPut("method", method.getValue());
		}

	}

}
