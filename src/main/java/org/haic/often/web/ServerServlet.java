package org.haic.often.web;

import javax.net.ServerSocketFactory;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.SecureRandom;

public class ServerServlet {

	private ServerSocketFactory socketFactory;

	public ServerServlet() {
		socketFactory = ServerSocketFactory.getDefault();
	}

	public ServerServlet(String keyPath, char[] password) {
		try {
			var keyFile = new FileInputStream(keyPath);
			var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyFile, password);
			var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, password);
			var keyManagers = keyManagerFactory.getKeyManagers();
			var sslContext = SSLContext.getInstance("TLS");
			sslContext.init(keyManagers, null, new SecureRandom());
			socketFactory = sslContext.getServerSocketFactory();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void redirectSSL(String domain) {
		new Thread(() -> {
			try (var socket = ServerSocketFactory.getDefault().createServerSocket(80)) {
				while (true) {
					try (var ss = socket.accept()) {
						new Redirect().run(ss, domain);
					} catch (IOException ignored) {
					}
				}
			} catch (IOException ignored) {
			}
		}).start();
	}

	public void listen(int arg) throws IOException {
		try (var socket = socketFactory.createServerSocket(arg)) {
			//noinspection InfiniteLoopStatement
			while (true) {
				try (var ss = socket.accept()) {
					new Handler().run(ss);
				}
			}
		}

	}

}
