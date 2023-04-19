package org.haic.often.net;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * SSL Socket 工厂
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/2/25 18:16
 */
public class IgnoreSSLSocket {

	/**
	 * 获得HTTPS忽略证书验证的SSLContext
	 *
	 * @return SSLContext
	 */
	public static SSLContext ignoreSSLContext() {
		TrustManager[] tm = { new MyX509TrustManager() };
		SSLContext ctx = null;
		try {
			ctx = SSLContext.getInstance("TLSv1.3");
			ctx.init(null, tm, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ctx;
	}

}
