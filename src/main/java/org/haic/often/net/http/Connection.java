package org.haic.often.net.http;

import org.haic.often.Judge;
import org.haic.often.net.Method;
import org.haic.often.net.URIUtil;
import org.haic.often.net.UserAgent;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.parser.xml.Element;
import org.haic.often.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.Proxy;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Connection 接口是一个方便的 HTTP 客户端和会话对象，用于从 Web 获取内容，并将它们解析为 Documents。
 * <p>
 * 使用的“连接”并不意味着在连接对象的生命周期内与服务器保持长期连接。套接字连接仅在请求执行（ execute() 、 get()或post() ）时建立，并消耗服务器的响应。
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/3/16 10:35
 */
public abstract class Connection {

    protected String url; // URL
    protected String host;
    protected String auth;
    protected String sni; // URL
    protected Method method = Method.GET;
    protected Map<String, String> headers = new HashMap<>();
    protected Map<String, String> cookies = new HashMap<>();

    protected int retry; // 请求异常重试次数
    protected int MILLISECONDS_SLEEP; // 重试等待时间
    protected boolean unlimit;// 请求异常无限重试
    protected boolean failThrow; // 错误异常
    protected List<Integer> retryStatusCodes = new ArrayList<>();

    protected String params = ""; // 请求参数

    /**
     * 设置要获取的请求 URL，协议必须是 HTTP 或 HTTPS
     *
     * @param url 要连接的 URL
     * @return 此连接，用于链接
     */
    public abstract Connection url(@NotNull String url);

    /**
     * 连接newRequest ()
     * 创建一个新请求，使用此 Connection 作为会话状态并初始化连接设置（然后可以独立于返回的 Connection.Request 对象）。
     *
     * @return 一个新的 Connection 对象，具有共享的 Cookie 存储和来自此 Connection 和 Request 的初始化设置
     */
    public abstract Connection newRequest();

    /**
     * 连接sslSocketFactory （ SSLSocketFactory  sslSocketFactory）
     * <p>
     * 设置自定义 SSL 套接字工厂
     *
     * @param sslSocket 自定义 SSL 套接字工厂
     * @return 此连接，用于链接
     */
    public abstract Connection sslSocketFactory(SSLContext sslSocket);

    /**
     * 连接用户代理（ 字符串 用户代理）<br/>
     * 设置请求用户代理标头
     *
     * @param userAgent 要使用的用户代理
     * @return 此连接，用于链接
     */
    public Connection userAgent(@NotNull String userAgent) {
        return header("user-agent", userAgent);
    }

    /**
     * 添加请求头user-agent，以移动端方式访问页面
     *
     * @param isPhone true or false
     * @return 此连接，用于链接
     */
    public Connection isPhone(boolean isPhone) {
        return isPhone ? userAgent(UserAgent.chromeAsPhone()) : userAgent(UserAgent.chrome());
    }

    /**
     * 连接followRedirects （布尔followRedirects）<br/>
     * 将连接配置为（不）遵循服务器重定向，默认情况下这是true
     *
     * @param followRedirects 如果应该遵循服务器重定向，则为 true
     * @return 此连接，用于链接
     */
    public abstract Connection followRedirects(boolean followRedirects);

    /**
     * 连接引荐来源网址（ 字符串 引荐来源网址）<br/>
     * 设置请求引荐来源网址（又名“引荐来源网址”）标头
     *
     * @param referrer 要使用的来源网址
     * @return 此连接，用于链接
     */
    public Connection referrer(@NotNull String referrer) {
        return header("referer", referrer);
    }

    /**
     * 设置授权码或身份识别标识
     * <p>
     * 有些服务器不使用cookie验证身份,使用authorization进行验证
     * <p>
     * 一般信息在cookie或local Storage中存储
     * <p>
     * 如果没有协议类型,默认使用Bearer
     *
     * @param auth 授权码或身份识别标识
     * @return 此连接，用于链接
     */
    public Connection auth(@NotNull String auth) {
        return header("authorization", (this.auth = auth.contains(" ") ? auth : "Bearer " + auth));
    }

    /**
     * 设置读取超时时间，读取超时（ int millis）<br/>
     * 默认读取超时为 0 毫秒，超时为零被视为无限超时<br/>
     *
     * @param millis 读取超时之前的毫秒数（千分之一秒）
     * @return 此连接，用于链接
     */
    public abstract Connection timeout(int millis);

    /**
     * 设置连接请求类型参数,用于服务器识别内容类型
     *
     * @param type 请求类型
     * @return 此连接，用于链接
     */
    public Connection contentType(@NotNull String type) {
        return header("content-type", type);
    }

    /**
     * 连接头（ 字符串 名称， 字符串 值）<br/>
     * 设置请求标头
     *
     * @param name  标题名称
     * @param value 标头值
     * @return 此连接，用于链接
     */
    public Connection header(@NotNull String name, @NotNull String value) {
        this.headers.put(name, value);
        return this;
    }

    /**
     * 连接头（ Map  < String  , String  > 头）
     * <p>
     * 将每个提供的标头添加到请求中
     *
     * @param headers 标头名称映射 -> 值对
     * @return 此连接，用于链接
     */
    public Connection headers(@NotNull Map<String, String> headers) {
        this.headers.putAll(headers);
        return this;
    }

    /**
     * 连接头（ Map  < String  , String  > 头）
     * <p>
     * 将为连接设置全新的请求标头
     *
     * @param headers 标头名称映射 -> 值对
     * @return 此连接，用于链接
     */
    public Connection setHeaders(@NotNull Map<String, String> headers) {
        this.headers = new HashMap<>();
        return headers(headers);
    }

    /**
     * 删除在此请求/响应中设置 header。
     *
     * @param key header的键
     * @return 此连接，用于链接
     */
    public Connection removeHeader(@NotNull String key) {
        this.headers.remove(key);
        return this;
    }

    /**
     * 设置要在请求中发送的 cookie
     *
     * @param name  cookie 的名称
     * @param value cookie 的值
     * @return 此连接，用于链接
     */
    public Connection cookie(@NotNull String name, @NotNull String value) {
        this.cookies.put(name, value);
        return this;
    }

    /**
     * 连接 cookies （ Map < String  , String  >cookies）
     * <p>
     * 将每个提供的 cookie 添加到请求中
     *
     * @param cookies 名称映射 -> 值对
     * @return 此连接，用于链接
     */
    public Connection cookies(@NotNull Map<String, String> cookies) {
        cookies.entrySet().removeIf(entry -> entry.getValue() == null);
        this.cookies.putAll(cookies);
        return this;
    }

    /**
     * 连接 cookies
     * <p>
     * 将每个提供的 cookie 添加到请求中
     *
     * @param cookies cookie字符串
     * @return 此连接，用于链接
     */
    public Connection cookies(@NotNull String cookies) {
        return cookies(StringUtil.toMap(cookies, ";"));
    }

    /**
     * 连接 cookies （ Map < String  , String  >cookies）
     * <p>
     * 将为连接设置全新的 cookie
     *
     * @param cookies 名称映射 -> 值对
     * @return 此连接，用于链接
     */
    public Connection setCookies(@NotNull Map<String, String> cookies) {
        this.cookies = cookies;
        return this;
    }

    /**
     * 删除在此请求/响应中设置 cookie。
     *
     * @param name cookie的名称
     * @return 此连接，用于链接
     */
    public Connection removeCookie(@NotNull String name) {
        this.cookies.remove(name);
        return this;
    }

    /**
     * 获取此 Connection 当前域名的 Cookie
     *
     * @return Map < String , String >
     */
    public Map<String, String> cookies() {
        return this.cookies;
    }

    /**
     * 连接数据（ 字符串 键、 字符串 值）<br/>
     * 添加请求数据参数。请求参数在 GET 的请求查询字符串中发送，在 POST 的请求正文中发送。一个请求可能有多个同名的值。
     *
     * @param key   数据键
     * @param value 数据值
     * @return 此连接，用于链接
     */
    public Connection data(@NotNull String key, @NotNull String value) {
        if (this.headers.get("content-type").equals("application/x-www-form-urlencoded;charset=UTF-8")) {
            params += (Judge.isEmpty(params) ? "" : "&") + URIUtil.encodeValue(key) + "=" + URIUtil.encodeValue(value);
            return this;
        } else {
            params = URIUtil.encodeValue(key) + "=" + URIUtil.encodeValue(value);
            return contentType("application/x-www-form-urlencoded;charset=UTF-8");
        }
    }

    /**
     * 根据所有提供的数据设置全新的请求数据参数
     *
     * @param params 数据参数
     * @return 此连接，用于链接
     */
    public Connection data(@NotNull Map<String, String> params) {
        this.params = params.entrySet().stream().filter(l -> l.getValue() != null).map(l -> URIUtil.encodeValue(l.getKey()) + "=" + URIUtil.encodeValue(l.getValue())).collect(Collectors.joining("&"));
        return contentType("application/x-www-form-urlencoded;charset=UTF-8");
    }

    /**
     * 添加输入流作为请求数据参数，对于 GET 没有效果，但对于 POST 这将上传输入流
     * <p>
     * 该方法将默认的文本格式上传:
     * <blockquote>
     * <pre>	--UUID--</pre>
     * <pre>	content-disposition: form-data; name="key"; filename="name"</pre>
     * <pre>	content-type: application/octet-stream; charset=utf-8</pre>
     * <pre>	</pre>
     * <pre>	文件流</pre>
     * <pre>	--UUID--</pre>
     * </blockquote>
     *
     * @param key  数据键（表单项名称）
     * @param name 要呈现给删除服务器的文件的名称。通常只是名称，而不是路径，组件
     * @param in   要上传的输入流，您可能从FileInputStream获得。您必须在finally块中关闭 InputStream
     * @return 此连接，用于链接
     */
    public abstract Connection data(@NotNull String key, @NotNull String name, @NotNull InputStream in);

    /**
     * 设置 POST（或 PUT）请求正文<br/>
     *
     * @param body 请求正文
     * @return 此连接，用于链接
     */
    public Connection requestBody(@NotNull Object body) {
        switch (body) {
            case JSONObject json -> {
                this.params = json.toJSONString();
                return contentType("application/json;charset=UTF-8");
            }
            case Map<?, ?> map -> {
                this.params = map.entrySet().stream().filter(l -> l.getValue() != null).map(l -> URIUtil.encodeValue(l.getKey().toString()) + "=" + URIUtil.encodeValue(l.getValue().toString())).collect(Collectors.joining("&"));
                return contentType("application/x-www-form-urlencoded;charset=UTF-8");
            }
            case Element element -> {
                this.params = element.toString();
                return contentType("application/xml;charset=UTF-8");
            }
            default -> {
                this.params = body.toString();
                removeHeader("content-type"); // RAW
                return this;
            }
        }
    }

    /**
     * 设置用于此请求的 SOCKS 代理
     * <p>
     * 如果参数为空,则不使用代理,等同于 proxy(Proxy.NO_PROXY)
     *
     * @param ipAddr 代理地址 格式 - host:port
     * @return 此连接，用于链接
     */
    public Connection socks(@NotNull String ipAddr) {
        if (ipAddr.isEmpty()) return proxy(Proxy.NO_PROXY);
        if (ipAddr.startsWith("[")) {
            return socks(ipAddr.substring(1, ipAddr.indexOf(']')), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
        } else {
            var spilt = ipAddr.split("@");
            var split2 = spilt[0].split(":");
            if (spilt.length == 1) {
                return socks(split2[0], Integer.parseInt(split2[1]));
            } else {
                var split3 = spilt[1].split(":");
                return socks(split2[0], Integer.parseInt(split2[1]), split3[0], split3[1]);
            }
        }
    }

    /**
     * 设置用于此请求的 SOCKS 代理
     *
     * @param host 代理主机名
     * @param port 代理端口
     * @return 此连接，用于链接
     */
    public abstract Connection socks(@NotNull String host, int port);

    /**
     * 设置用于此请求的 SOCKS 代理
     *
     * @param host     代理地址
     * @param port     代理端口
     * @param user     用户
     * @param password 密码
     * @return 此连接，用于链接
     */
    public abstract Connection socks(@NotNull String host, int port, @NotNull String user, @NotNull String password);

    /**
     * 连接代理（ @NotNull  Proxy 代理）
     * <p>
     * 设置用于此请求的代理
     * <p>
     * 如果参数为空,则不使用代理,等同于 proxy(Proxy.NO_PROXY)
     *
     * @param ipAddr 代理地址 格式 - host:port
     * @return 此连接，用于链接
     */
    public Connection proxy(@NotNull String ipAddr) {
        if (ipAddr.isEmpty()) return proxy(Proxy.NO_PROXY);
        if (ipAddr.startsWith("[")) {
            return proxy(ipAddr.substring(1, ipAddr.indexOf(']')), Integer.parseInt(ipAddr.substring(ipAddr.lastIndexOf(":") + 1)));
        } else {
            var spilt = ipAddr.split("@");
            var split2 = spilt[0].split(":");
            if (spilt.length == 1) {
                return proxy(split2[0], Integer.parseInt(split2[1]));
            } else {
                var split3 = spilt[1].split(":");
                return proxy(split2[0], Integer.parseInt(split2[1]), split3[0], split3[1]);
            }
        }
    }

    /**
     * 连接代理（ @NotNull  Proxy 代理）<br/>
     * 设置用于此请求的代理
     *
     * @param host 代理地址
     * @param port 代理端口
     * @return 此连接，用于链接
     */
    public abstract Connection proxy(@NotNull String host, int port);

    /**
     * 连接代理（ @NotNull  Proxy 代理）<br/>
     * 设置用于此请求的代理
     *
     * @param host     代理地址
     * @param port     代理端口
     * @param user     用户
     * @param password 密码
     * @return 此连接，用于链接
     */
    public abstract Connection proxy(@NotNull String host, int port, @NotNull String user, @NotNull String password);

    /**
     * 连接代理（ @NotNull  Proxy 代理）<br/>
     * 设置用于此请求的代理
     *
     * @param proxy 要使用的代理
     * @return 此连接，用于链接
     */
    public abstract Connection proxy(@NotNull Proxy proxy);

    /**
     * 连接方法（ Connection.Method方法）
     * 设置要使用的请求方法，GET 或 POST。默认为 GET。
     *
     * @param method HTTP 请求方法
     * @return 此连接，用于链接
     */
    public Connection method(@NotNull Method method) {
        this.method = method;
        return this;
    }

    /**
     * 对于某些国家和地区对网站封锁,采用域前置绕过
     *
     * @param sni 前置域名(host)
     * @return 此连接，用于链接
     */
    public Connection sni(String sni) {
        this.sni = sni;
        if (url != null) url(url);
        return this;
    }

    /**
     * 在请求超时或者指定状态码发生时，进行重试，重试超过次数或者状态码正常返回
     *
     * @param retry 重试次数
     * @return 此连接，用于链接
     */
    public Connection retry(int retry) {
        this.retry = retry;
        return this;
    }

    /**
     * 在请求超时或者指定状态码发生时，进行重试，重试超过次数或者状态码正常返回
     *
     * @param retry  重试次数
     * @param millis 重试等待时间(毫秒)
     * @return this
     */
    public Connection retry(int retry, int millis) {
        this.retry = retry;
        this.MILLISECONDS_SLEEP = millis;
        return this;
    }

    /**
     * 在请求超时或者指定状态码发生时，无限进行重试，直至状态码正常返回
     *
     * @param unlimit 启用无限重试, 默认false
     * @return 此连接，用于链接
     */
    public Connection retry(boolean unlimit) {
        this.unlimit = unlimit;
        return this;
    }

    /**
     * 在请求超时或者指定状态码发生时，无限进行重试，直至状态码正常返回
     *
     * @param unlimit 启用无限重试, 默认false
     * @param millis  重试等待时间(毫秒)
     * @return 此连接，用于链接
     */
    public Connection retry(boolean unlimit, int millis) {
        this.unlimit = unlimit;
        this.MILLISECONDS_SLEEP = millis;
        return this;
    }

    /**
     * 额外指定错误状态码码，在指定状态发生时，也进行重试，可指定多个
     *
     * @param statusCode 状态码
     * @return 此连接，用于链接
     */
    public Connection retryStatusCodes(int... statusCode) {
        retryStatusCodes = Arrays.stream(statusCode).boxed().toList();
        return this;
    }

    /**
     * 额外指定错误状态码码，在指定状态发生时，也进行重试，可指定多个
     *
     * @param retryStatusCodes 状态码列表
     * @return 此连接，用于链接
     */
    public Connection retryStatusCodes(List<Integer> retryStatusCodes) {
        this.retryStatusCodes = retryStatusCodes;
        return this;
    }

    /**
     * 在状态码不为200+或300+时，抛出执行异常，并获取一些参数，一般用于调试<br/>
     * 默认情况下为false
     *
     * @param exit 启用错误退出
     * @return 此连接，用于链接
     */
    public Connection failThrow(boolean exit) {
        this.failThrow = exit;
        return this;
    }

    /**
     * 将请求作为 GET 执行，并解析结果
     *
     * @return HTML文档
     */
    public Response get() {
        return method(Method.GET).execute();
    }

    /**
     * 将请求作为 POST 执行，并解析结果
     *
     * @return HTML文档
     */
    public Response post() {
        return method(Method.POST).execute();
    }

    /**
     * 运行程序，获取 响应结果
     *
     * @return Response
     */
    public abstract Response execute();

}
