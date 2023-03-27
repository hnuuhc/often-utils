package org.haic.often.net;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.cert.X509Certificate;

/**
 * HTTPS忽略证书验证,防止高版本jdk因证书算法不符合约束条件,使用继承X509ExtendedTrustManager的方式
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/3/27 12:26
 */
public class MyX509TrustManager extends X509ExtendedTrustManager {
	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1) {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1) {

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1, Socket arg2) {

	}

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1, Socket arg2) {

	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1, SSLEngine arg2) {

	}
}
