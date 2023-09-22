package org.haic.often.net;

import org.apache.http.HttpStatus;
import org.haic.often.Terminal;
import org.haic.often.annotations.NotNull;
import org.haic.often.net.http.HttpsUtil;
import org.haic.often.util.Base64Util;
import org.haic.often.util.ListUtil;
import org.haic.often.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * URI工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:13
 */
@SuppressWarnings("DuplicatedCode")
public class URIUtil {

	private static final Predicate<Character> specialSafetyChar = c -> "!#$&'()*+,/:;=?@-._~".contains(String.valueOf(c));
	private static final Predicate<Character> safetyChar = c -> (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
	private static final Predicate<Character> isDigit16Char = c -> Character.isDigit(c) || Character.isLetter(c);

	/**
	 * 提取链接中的参数
	 *
	 * @param url 链接
	 * @return 键值参数
	 */
	public static Map<String, String> getParams(String url) {
		var index = url.indexOf("?");
		return index == -1 ? new HashMap<>() : StringUtil.toMap(url.substring(index + 1), "&");
	}

	/**
	 * 获取相对网址的绝对网址链接
	 *
	 * @param url      源网址(绝对地址)
	 * @param relative 相对网址
	 * @return 绝对网址
	 */
	public static String toAbsoluteUrl(@NotNull String url, @NotNull String relative) {
		return relative.isEmpty() ? "" : (relative.startsWith("http") ? relative : relative.startsWith("/") ? relative.charAt(1) == '/' ? getProtocol(url) + relative : getDomain(url) + relative : url.substring(0, url.lastIndexOf("/") + 1) + relative).replaceAll("&amp;", "&");
	}

	/**
	 * 测试网络是IPv4还是IPv6访问优先(访问IPv4/IPv6双栈站点，如果返回IPv6地址，则IPv6访问优先)
	 *
	 * @return IP地址
	 */
	public static String getPublicIPAddress() {
		return HttpsUtil.connect("https://test.ipw.cn").execute().body();
	}

	/**
	 * 查询本机外网IPv4地址
	 *
	 * @return IPv4地址
	 */
	public static String getPublicIPv4Address() {
		return HttpsUtil.connect("https://4.ipw.cn").execute().body();
	}

	/**
	 * 查询本机外网IPv6地址
	 *
	 * @return IPv6地址
	 */
	public static String getPublicIPv6Address() {
		return HttpsUtil.connect("https://6.ipw.cn").execute().body();
	}

	/**
	 * 获取本机IPv4地址
	 *
	 * @return IPv4地址, 可能为 NULL
	 */
	public static String getLocalIPv4Address() {
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		InetAddress inetAddress = null;
		outer:
		while (networkInterfaces.hasMoreElements()) {
			Enumeration<InetAddress> inetAds = networkInterfaces.nextElement().getInetAddresses();
			while (inetAds.hasMoreElements()) {
				inetAddress = inetAds.nextElement();
				//检查此地址是否是IPv6地址以及是否是保留地址
				if (inetAddress instanceof Inet4Address && !isReservedAddr(inetAddress)) {
					break outer;
				}
			}
		}
		String ipAddr;
		return inetAddress == null ? null : (ipAddr = inetAddress.getHostAddress()).contains("%") ? ipAddr.substring(0, ipAddr.indexOf('%')) : ipAddr;
	}

	/**
	 * 获取本机IPv6地址
	 *
	 * @return IPv6地址, 可能为 NULL
	 */
	public static String getLocalIPv6Address() {
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
		InetAddress inetAddress = null;
		outer:
		while (networkInterfaces.hasMoreElements()) {
			Enumeration<InetAddress> inetAds = networkInterfaces.nextElement().getInetAddresses();
			while (inetAds.hasMoreElements()) {
				inetAddress = inetAds.nextElement();
				//检查此地址是否是IPv6地址以及是否是保留地址
				if (inetAddress instanceof Inet6Address && !isReservedAddr(inetAddress)) {
					break outer;
				}
			}
		}
		String ipAddr;
		return inetAddress == null ? null : (ipAddr = inetAddress.getHostAddress()).contains("%") ? ipAddr.substring(0, ipAddr.indexOf('%')) : ipAddr;
	}

	/**
	 * 判断I nternet 协议 (IP) 地址是否为保留地址
	 *
	 * @param inetAddress Internet 协议 (IP) 地址
	 * @return 判断结果
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	private static boolean isReservedAddr(InetAddress inetAddress) {
		return inetAddress.isAnyLocalAddress() || inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress();
	}

	/**
	 * 判断URL字符串是否合法
	 *
	 * @param url URL字符串
	 * @return 判断结果
	 */
	public static boolean isURL(@NotNull String url) {
		return url.matches("(ftp|https?)://.*\\..*") && url.lastIndexOf("://") < 6;
	}

	/**
	 * 获取URL的网络协议
	 *
	 * @param url URL
	 * @return 网络协议名称 例: http
	 */
	public static String getProtocol(@NotNull String url) {
		return url.split("/")[0];
	}

	/**
	 * 获取URL的域名网址
	 *
	 * @param url URL
	 * @return 域名网址
	 */
	public static String getDomain(@NotNull String url) {
		String[] info = url.split("/");
		return info[0] + "//" + info[2];
	}

	/**
	 * 提取文本内容中的所有URL链接
	 *
	 * @param body 文本内容
	 * @return URL列表
	 */
	public static List<String> extractURL(@NotNull String body) {
		return ListUtil.streamSet(StringUtil.extractList(body, "(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]"));
	}

	/**
	 * 常用网页字符集编码格式为GB2312,GBK,UTF-8
	 * <p>
	 * GBK编码兼容GB2312编码
	 * <p>
	 * 通过判断编码是否为UTF-8确定网页的字符集编码
	 *
	 * @param bytes 网页数据
	 * @return UTF-8 or GBK
	 */
	public static Charset encoding(byte[] bytes) {
		return StringUtil.isUTF8(bytes) ? StandardCharsets.UTF_8 : Charset.forName("GBK");
	}

	/**
	 * 创建一个URI对象
	 *
	 * @param url URL
	 * @return URI对象
	 */
	public static URI createURI(@NotNull String url) {
		URI uri = null;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

	/**
	 * 获取URL
	 *
	 * @param url URL
	 * @return URL对象
	 */
	public static URL createURL(@NotNull String url) {
		URL uri = null;
		try {
			uri = new URI(url).toURL();
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		return uri;
	}

	/**
	 * 获取域名
	 *
	 * @param url URL
	 * @return 字符串
	 */
	@NotNull
	public static String getHost(@NotNull String url) {
		return createURI(url).getHost();
	}

	/**
	 * 判断连接是否正常,状态码200+或300+
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	public static boolean statusIsNormal(int statusCode) {
		return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_BAD_REQUEST;
	}

	/**
	 * 判断连接是否成功,状态码200+
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	public static boolean statusIsOK(int statusCode) {
		return statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES;
	}

	/**
	 * 判断连接是否超时,或中断,状态码0或408
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	public static boolean statusIsTimeout(int statusCode) {
		return statusCode == HttpStatus.SC_REQUEST_TIMEOUT || statusCode == 0;
	}

	/**
	 * 判断连接是否重定向,状态码300+
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	public static boolean statusIsRedirect(int statusCode) {
		return statusCode >= HttpStatus.SC_MULTIPLE_CHOICES && statusCode < HttpStatus.SC_BAD_REQUEST;
	}

	/**
	 * 判断连接是否请求错误,状态码400+(排除408)
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	public static boolean statusIsError(int statusCode) {
		return statusCode >= HttpStatus.SC_BAD_REQUEST && statusCode < HttpStatus.SC_INTERNAL_SERVER_ERROR && statusCode != HttpStatus.SC_REQUEST_TIMEOUT;
	}

	/**
	 * 判断连接是否请求错误,状态码500+
	 *
	 * @param statusCode 状态码
	 * @return 连接状态 boolean
	 */
	public static boolean statusIsServerError(int statusCode) {
		return statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR;
	}

	/**
	 * 判断IP地址是否是私有地址
	 *
	 * @param ipAddr IPV4地址
	 * @return 判断结果
	 */
	public static boolean isPrivateAddress(@NotNull String ipAddr) {
		return ipAddr.matches("^1(((0|27)(.(([1-9]?|1[0-9])[0-9]|2([0-4][0-9]|5[0-5])))|(72.(1[6-9]|2[0-9]|3[01])|92.168))(.(([1-9]?|1[0-9])[0-9]|2([0-4][0-9]|5[0-5]))){2})$");
	}

	/**
	 * 判断IP地址是否是子网掩码
	 *
	 * @param ipAddr IPV4地址
	 * @return 判断结果
	 */
	public static boolean isSubnetMask(@NotNull String ipAddr) {
		return ipAddr.matches("^((128|192)|2(24|4[08]|5[245]))(\\.(0|(128|192)|2((24)|(4[08])|(5[245])))){3}$");
	}

	/**
	 * 判断IPV4地址格式是否正确
	 *
	 * @param ipAddr IPV4地址
	 * @return 判断结果
	 */
	public static boolean isIPv4Address(@NotNull String ipAddr) {
		return ipAddr.matches("^(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|[1-9])(\\.(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]\\d|\\d)){3}(:\\d{1,5})?$");
	}

	/**
	 * 判断IPV6地址格式是否正确
	 *
	 * @param ipAddr IPV6地址
	 * @return 判断结果
	 */
	public static boolean isIPv6Address(@NotNull String ipAddr) {
		Predicate<String> valid = host -> host.length() < 40 && host.matches("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4})*)?)::((([0-9A-Fa-f]{1,4}:)" + "*[0-9A-Fa-f]{1,4})?)$");
		return ipAddr.matches("\\[.*]:\\d{1,5}") ? valid.test(ipAddr.substring(1, ipAddr.indexOf(']'))) : valid.test(ipAddr);
	}

	/**
	 * 判断IP地址格式是否正确
	 *
	 * @param ipAddr IP地址
	 * @return 判断结果
	 */
	public static boolean isIPAddress(@NotNull String ipAddr) {
		return isIPv4Address(ipAddr) || isIPv6Address(ipAddr);
	}

	/**
	 * CMD命令获取IP连接状态
	 *
	 * @param host 域名或IP
	 * @return 连接状态
	 */
	public static boolean pingIp(@NotNull String host) {
		return Terminal.command("ping", host, "-n", "1", "-w", "5000").execute() == 0;
	}

	/**
	 * 获取HOST连接状态
	 *
	 * @param host 域名或IP
	 * @return 连接状态
	 */
	public static boolean pingHost(@NotNull String host) {
		return pingHost(host, 80);
	}

	/**
	 * 获取HOST连接状态
	 *
	 * @param host 域名或IP
	 * @param port 端口
	 * @return 连接状态
	 */
	public static boolean pingHost(@NotNull String host, int port) {
		boolean isReachable;
		try (Socket socket = new Socket()) {
			InetSocketAddress endpointSocketAddr = new InetSocketAddress(host, port);
			socket.connect(endpointSocketAddr, 5000);
			isReachable = socket.isConnected();
		} catch (IOException e) {
			isReachable = false;
		}
		return isReachable;
	}

	/**
	 * 获取当前网址的IP，如果不存在则返回null
	 *
	 * @param host 网址
	 * @return IP地址
	 */
	public static String hostIP(@NotNull String host) {
		try {
			return InetAddress.getByName(URIUtil.getDomain(host)).getHostAddress();
		} catch (UnknownHostException e) {
			return null;
		}
	}

	/**
	 * 获取 URL请求头Content-Disposition文件名属性
	 *
	 * @param disposition ontent-Disposition
	 * @return 文件名
	 */
	public static String getFileNameForDisposition(@NotNull String disposition) {
		String fileName = disposition.substring(disposition.lastIndexOf("filename"));
		fileName = fileName.substring(fileName.indexOf("=") + 1).replaceAll("\"", "");
		return decode(fileName.contains("'") ? fileName.substring(fileName.lastIndexOf("'") + 1) : fileName);
	}

	/**
	 * 由于不同网站请求头中的hash存放位置不一致或者不规范,将从可能的的键中获取hash值,注意返回值可能为null
	 *
	 * @param headers 请求头
	 * @return hash值, 自行根据长度判断hash类型
	 */
	public static String getHash(@NotNull Map<String, String> headers) {
		String hash;
		return (hash = headers.get("x-cos-meta-md5")) != null || (hash = headers.get("x-oss-hash-value")) != null || ((hash = headers.get("content-md5")) != null && hash.length() == 32) ? hash : null;
	}

	/**
	 * 迅雷磁链转换直链
	 *
	 * @param thunder 迅雷磁力链接
	 * @return URL直链
	 */
	@NotNull
	public static String thunderToURL(@NotNull String thunder) {
		String thunderUrl = Base64Util.decode(StringUtil.stripEnd(thunder, "=").replaceFirst("thunder://", ""), "GBK");
		return thunderUrl.substring(2, thunderUrl.length() - 2);
	}

	/**
	 * 判断字符串是否经过了UrlEncode
	 *
	 * @param s 字符串
	 * @return 判断结果
	 */
	public static boolean hasEnCode(@NotNull String s) {
		if (s.length() != 0) {
			for (int i = 0; i < s.length(); i++) {
				char c = s.charAt(i);
				if (c == '%' && i + 2 < s.length() && isDigit16Char.test(s.charAt(i + 1)) && isDigit16Char.test(s.charAt(i + 2))) {
					i += 2;
					continue;
				}
				if (!safetyChar.test(c) || !specialSafetyChar.test(c)) return false;
			}
		}
		return true;
	}

	/**
	 * UrlEncode解码,可解密被多次加密的字符串,直至获得最终解密的字符串
	 *
	 * @param s 待解密的字符串
	 * @return String
	 */
	@NotNull
	public static String decode(@NotNull String s) {
		return decode(s, StandardCharsets.UTF_8);
	}

	/**
	 * UrlEncode解码,可解密被多次加密的字符串,直至获得最终解密的字符串
	 *
	 * @param s       待解密的字符串
	 * @param charset 字符集编码名称
	 * @return String
	 */
	@NotNull
	public static String decode(@NotNull String s, @NotNull String charset) {
		return decode(s, Charset.forName(charset));
	}

	/**
	 * UrlEncode解码,可解密被多次加密的字符串,直至获得最终解密的字符串
	 *
	 * @param s       待解密的字符串
	 * @param charset 字符集编码
	 * @return String
	 */
	@NotNull
	public static String decode(@NotNull String s, @NotNull Charset charset) {
		var bytes = new ByteArrayOutputStream();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			boolean isByte = false;
			while (c == '%' && i + 2 < s.length() && isDigit16Char.test(s.charAt(i + 1)) && isDigit16Char.test(s.charAt(i + 2))) {
				c = (char) Integer.parseInt(s, i + 1, i + 3, 16);
				isByte = true;
				i += 2;
			}
			if (isByte) {
				bytes.write(c);
			} else {
				bytes.writeBytes(String.valueOf(c).getBytes());
			}
		}
		return bytes.toString(charset);
	}

	/**
	 * UrlEncode编码,如果已经加密,则返回原字符串
	 *
	 * @param s 待加密的字符串
	 * @return 加密的字符串
	 */
	@NotNull
	public static String encode(@NotNull String s) {
		var sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '%' && i + 2 < s.length() && isDigit16Char.test(s.charAt(i + 1)) && isDigit16Char.test(s.charAt(i + 2))) {
				sb.append(s, i, i + 3);
				i += 2;
			} else if (safetyChar.test(c) || specialSafetyChar.test(c)) {
				sb.append(c);
			} else {
				var bytes = String.valueOf(c).getBytes();
				if (bytes.length == 1) {
					sb.append("%");
					var hex = Integer.toHexString(bytes[0]).toUpperCase();
					if (hex.length() == 1) sb.append("0");
					sb.append(hex);
				} else {
					for (var b : bytes) {
						sb.append("%").append(Integer.toHexString((char) b).substring(2).toUpperCase());
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * UrlEncode编码,如果已经加密,则返回原字符串
	 * <p>
	 * 不跳过特殊安全字符 -> !#$&'()*+,/:;=?@-._~
	 *
	 * @param s 待加密的字符串
	 * @return 加密的字符串
	 */
	@NotNull
	public static String encodeValue(@NotNull String s) {
		var sb = new StringBuilder(s.length());
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (c == '%' && i + 2 < s.length() && isDigit16Char.test(s.charAt(i + 1)) && isDigit16Char.test(s.charAt(i + 2))) {
				sb.append(s, i, i + 3);
				i += 2;
			} else if (safetyChar.test(c)) {
				sb.append(c);
			} else {
				var bytes = String.valueOf(c).getBytes();
				if (bytes.length == 1) {
					sb.append("%");
					var hex = Integer.toHexString(bytes[0]).toUpperCase();
					if (hex.length() == 1) sb.append("0");
					sb.append(hex);
				} else {
					for (var b : bytes) {
						sb.append("%").append(Integer.toHexString((char) b).substring(2).toUpperCase());
					}
				}
			}
		}
		return sb.toString();
	}

}
