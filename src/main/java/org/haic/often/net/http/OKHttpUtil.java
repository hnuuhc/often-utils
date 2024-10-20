package org.haic.often.net.http;

import okhttp3.*;
import okio.BufferedSink;
import org.brotli.dec.BrotliInputStream;
import org.haic.often.Judge;
import org.haic.often.exception.HttpException;
import org.haic.often.net.*;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.util.IOUtil;
import org.haic.often.util.StringUtil;
import org.haic.often.util.ThreadUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * HttpClient工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/2/20 9:51
 */
public class OKHttpUtil {

    private OKHttpUtil() {
    }

    /**
     * 公共静态连接连接（ 字符串 网址）<br/> 使用定义的请求 URL 创建一个新的Connection （会话），用于获取和解析 HTML 页面
     *
     * @param url 要连接的 URL
     * @return 此连接，用于链接
     */

    public static Connection connect(@NotNull String url) {
        return new HttpConnection(url);
    }

    /**
     * 公共静态连接newSession ()
     * <p>
     * 创建一个新Connection以用作会话。将为会话维护连接设置（用户代理、超时、URL 等）和 cookie
     *
     * @return 此连接，用于链接
     */

    public static Connection newSession() {
        return new HttpConnection("");
    }

    private static class HttpConnection extends Connection {

        private String params = ""; // 请求参数

        private final OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).readTimeout(0, TimeUnit.SECONDS).sslSocketFactory(IgnoreSSLSocket.ignoreSSLContext().getSocketFactory(), new MyX509TrustManager()).hostnameVerifier((arg0, arg1) -> true);

        private MultipartBody.Builder file;

        private HttpConnection(@NotNull String url) {
            initialization();
            url(url);
        }

        private void initialization() {
            header("accept", "application/json, text/html;q=0.9, application/xhtml+xml;q=0.8, */*;q=0.7");
            header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
            header("accept-encoding", "gzip, deflate, br"); // 允许压缩gzip,br-Brotli
            header("user-agent", UserAgent.chrome()); // 设置随机请求头;
        }

        public Connection url(@NotNull String url) {
            if (!(url = url.strip()).isEmpty()) {
                if (!url.startsWith("http")) {
                    throw new HttpException("Only http & https protocols supported : " + url);
                }
                if ((url = url.contains("#") ? url.substring(0, url.indexOf("#")) : url).contains("?")) {
                    if (url.endsWith("?")) {
                        url = url.substring(0, url.length() - 1);
                    } else {
                        int index = url.indexOf("?");
                        url = url.substring(0, index + 1) + StringUtil.lines(url.substring(index + 1), "&").map(key -> {
                            int keyIndex = key.indexOf("=");
                            return key.substring(0, keyIndex + 1) + URIUtil.encodeValue(key.substring(keyIndex + 1));
                        }).collect(Collectors.joining("&"));
                    }
                }
                host = URIUtil.getHost(this.url = url);
                this.referrer(URIUtil.getDomain(url));
                if (sni != null) { // 域前置
                    header("host", host);
                    this.url = url.replace(host, sni);
                }
            }
            params = "";
            return this;
        }

        public Connection newRequest() {
            params = "";
            file = null;
            headers = new HashMap<>();
            method = Method.GET;
            initialization();
            return Judge.isEmpty(auth) ? this : auth(auth);
        }

        public Connection sslSocketFactory(SSLContext sslSocket) {
            okHttpClientBuilder.sslSocketFactory(sslSocket.getSocketFactory(), new MyX509TrustManager());
            return this;
        }

        public Connection followRedirects(boolean followRedirects) {
            okHttpClientBuilder.followRedirects(followRedirects);
            return this;
        }

        public Connection timeout(int millis) {
            okHttpClientBuilder.readTimeout(Duration.ofMillis(millis));
            return this;
        }

        public Connection removeHeader(@NotNull String key) {
            this.headers.remove(key);
            return this;
        }

        public Connection data(@NotNull String key, @NotNull String value) {
            params += (Judge.isEmpty(params) ? "" : "&") + key + "=" + URIUtil.encodeValue(value);
            return this;
        }

        public Connection data(@NotNull Map<String, String> params) {
            this.params = params.entrySet().stream().filter(l -> l.getValue() != null).map(l -> l.getKey() + "=" + URIUtil.encodeValue(l.getValue())).collect(Collectors.joining("&"));
            return this;
        }

        public Connection data(@NotNull String key, @NotNull String name, @NotNull InputStream in) {
            file = new MultipartBody.Builder().setType(MultipartBody.FORM);
            file.addFormDataPart(key, name, new FileRequestBody(in, MediaType.Companion.parse("multipart/form-data")));
            return this;
        }

        public Connection requestBody(@NotNull Object body) {
            if (body instanceof JSONObject json) {
                this.params = json.toJSONString();
                return contentType("application/json;charset=UTF-8");
            }
            return requestBody(String.valueOf(body));
        }

        public Connection requestBody(@NotNull String body) {
            this.params = body;
            return StringUtil.isJson(body) ? contentType("application/json;charset=UTF-8") : contentType("multipart/form-data;charset=UTF-8");
        }

        public Connection socks(@NotNull String host, int port) {
            return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)));
        }

        public Connection socks(@NotNull String host, int port, @NotNull String user, @NotNull String password) {
            return proxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(host, port)), user, password);
        }

        public Connection proxy(@NotNull String host, int port) {
            return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
        }

        public Connection proxy(@NotNull String host, int port, @NotNull String user, @NotNull String password) {
            return proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)), user, password);
        }

        public Connection proxy(@NotNull Proxy proxy) {
            okHttpClientBuilder.proxy(proxy);
            return this;
        }

        public Connection proxy(@NotNull Proxy proxy, @NotNull String user, @NotNull String password) {
            okHttpClientBuilder.proxyAuthenticator((route, response) -> response.request().newBuilder().header("Proxy-Authorization", Credentials.basic(user, password)).build());
            return proxy(proxy);
        }

        public Response get() {
            return method(Method.GET).execute();
        }

        public Response post() {
            return method(Method.POST).execute();
        }

        public Response execute() {
            var client = okHttpClientBuilder.build();

            var requestBuilder = new Request.Builder();
            // 设置cookies
            requestBuilder.header("cookie", cookies.entrySet().stream().map(l -> l.getKey() + "=" + l.getValue()).collect(Collectors.joining("; ")));
            // 设置headers
            headers.forEach(requestBuilder::header);

            if (method == Method.GET) {
                requestBuilder.url(Judge.isEmpty(params) ? url : url + (url.contains("?") ? "&" : "?") + params);
            } else {
                if (file == null) {
                    requestBuilder.url(url).method(method.name(), RequestBody.Companion.create(params, MediaType.Companion.parse(headers.get("content-type"))));
                } else {
                    StringUtil.toMap(params, "&").forEach((key, value) -> file.addFormDataPart(key, value));
                }
            }

            var response = executeProgram(client, requestBuilder.build());
            int statusCode = response.statusCode();
            for (int i = 0; (URIUtil.statusIsTimeout(statusCode) || retryStatusCodes.contains(statusCode)) && (i < retry || unlimit); i++) {
                ThreadUtil.waitThread(MILLISECONDS_SLEEP); // 程序等待
                response = executeProgram(client, requestBuilder.build());
                statusCode = response.statusCode();
            }

            if (failThrow && !URIUtil.statusIsNormal(statusCode)) {
                throw new HttpException("连接URL失败，状态码: " + statusCode + " URL: " + url);
            }
            return response;
        }

        private Response executeProgram(OkHttpClient client, Request request) {
            try {
                return new HttpResponse(client.newCall(request).execute(), cookies);
            } catch (Exception e) {
                return new HttpResponse(null, cookies);
            }
        }
    }

    private static class FileRequestBody extends RequestBody {

        private final InputStream in;
        private final MediaType mediaType;

        public FileRequestBody(InputStream in, @Nullable MediaType mediaType) {
            this.in = in;
            this.mediaType = mediaType;
        }

        @Override
        public void writeTo(@org.jetbrains.annotations.NotNull BufferedSink sink) throws IOException {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer, 0, 8192)) >= 0) {
                sink.write(buffer, 0, read);
            }
        }

        @Override
        public MediaType contentType() {
            return mediaType;
        }
    }

    /**
     * 响应接口
     *
     * @author haicdust
     * @version 1.0
     * @since 2022/3/16 10:33
     */
    private static class HttpResponse extends Response {

        private final okhttp3.Response res;

        private HttpResponse(okhttp3.Response res, Map<String, String> cookies) {
            this.res = res;
            this.cookies = cookies;

            if (statusCode() != HttpStatus.SC_REQUEST_TIMEOUT) {
                for (var header : res.headers()) {
                    var name = header.getFirst().toLowerCase();
                    var value = header.getSecond();
                    if (!value.equals("-")) {
                        if (name.equals("set-cookie")) {
                            var cookie = headers.get(name);
                            value = value.substring(0, value.indexOf(";"));
                            headers.put(name, cookie == null ? value : cookie + "; " + value);
                            var ck = value.split("=");
                            cookies.put(ck[0], ck[1]);
                        } else {
                            headers.put(name, value);
                        }
                    }
                }
            }
        }

        public String url() {
            return res.request().url().toString();
        }

        public int statusCode() {
            return res == null ? HttpStatus.SC_REQUEST_TIMEOUT : res.code();
        }

        public String statusMessage() {
            return res.message();
        }

        public String contentType() {
            return headers().get("content-type");
        }

        public Map<String, String> headers() {
            return headers;
        }

        public Map<String, String> cookies() {
            return cookies;
        }

        public InputStream bodyStream() {
            return res.body().byteStream();
        }

        protected ByteArrayOutputStream bodyAsByteArray() {
            if (this.body != null) return this.body;
            try (var in = bodyStream()) {
                var encoding = header("content-encoding");
                var body = "gzip".equals(encoding) ? new GZIPInputStream(in) : "deflate".equals(encoding) ? new InflaterInputStream(in, new Inflater(true)) : "br".equals(encoding) ? new BrotliInputStream(in) : in;
                this.body = IOUtil.stream(body).toByteArrayOutputStream();
                res.close();
                return this.body;
            } catch (Exception e) {
                return null;
            }
        }

        public void close() {
            res.close();
        }

    }

}
