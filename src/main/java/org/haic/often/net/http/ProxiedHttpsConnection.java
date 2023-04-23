package org.haic.often.net.http;

import org.haic.often.net.URIUtil;
import org.haic.often.util.Base64Util;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.*;

public class ProxiedHttpsConnection extends HttpURLConnection {

	private static final byte[] NEWLINE = "\r\n".getBytes();//should be "ASCII7"
	private final SocketAddress proxy;

	private Socket socket = new Socket();
	private final Map<String, List<String>> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final Map<String, List<String>> sendheaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private final Map<String, List<String>> proxyheaders = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	private int statusCode;
	private String responseMessage;

	public ProxiedHttpsConnection(URL url, SocketAddress proxy, String username, String password) {
		super(url);
		this.proxy = proxy;
		var encoded = Base64Util.encode(username + ":" + password);
		proxyheaders.put("Proxy-Authorization", new ArrayList<>(List.of("Basic " + encoded)));
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		afterRead();
		afterWrite();
		return new FilterOutputStream(socket.getOutputStream()) {
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				out.write(String.valueOf(len).getBytes());
				out.write(NEWLINE);
				out.write(b, off, len);
				out.write(NEWLINE);
			}

			@Override
			public void write(byte[] b) throws IOException {
				out.write(String.valueOf(b.length).getBytes());
				out.write(NEWLINE);
				out.write(b);
				out.write(NEWLINE);
			}

			@Override
			public void write(int b) throws IOException {
				out.write(String.valueOf(1).getBytes());
				out.write(NEWLINE);
				out.write(b);
				out.write(NEWLINE);
			}

			@Override
			public void close() throws IOException {
				socket.close();
			}

		};
	}

	private boolean afterwritten = false;

	@Override
	public InputStream getErrorStream() {
		try {
			return getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		afterRead();
		return new BufferedInputStream(new DataInputStream(socket.getInputStream()));
	}

	@Override
	public void setRequestMethod(String method) {
		this.method = method;
	}

	@Override
	public void setRequestProperty(String key, String value) {
		sendheaders.put(key, new ArrayList<>(Collections.singletonList(value)));
	}

	@Override
	public void addRequestProperty(String key, String value) {
		sendheaders.computeIfAbsent(key, l -> new ArrayList<>()).add(value);
	}

	@Override
	public Map<String, List<String>> getHeaderFields() {
		return headers;
	}

	@Override
	public int getResponseCode() throws IOException {
		afterRead();
		return statusCode;
	}

	@Override
	public String getResponseMessage() throws IOException {
		afterRead();
		return responseMessage;
	}

	@Override
	public String getContentType() {
		try {
			afterRead();
			var type = headers.get("Content-Type");
			return type == null ? null : type.get(0);
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public void connect() throws IOException {
		if (connected) return;
		connected = true;
		socket.setSoTimeout(getReadTimeout());
		socket.connect(proxy, getConnectTimeout());
		var msg = new StringBuilder();
		msg.append("CONNECT ");
		msg.append(url.getHost());
		msg.append(':');
		msg.append(url.getPort() == -1 ? url.getProtocol().equals("http") ? 80 : 443 : url.getPort());
		msg.append(" HTTP/1.0\r\n");
		for (var header : proxyheaders.entrySet()) {
			for (var l : header.getValue()) {
				msg.append(header.getKey()).append(": ").append(l);
				msg.append("\r\n");
			}
		}
		msg.append("Connection: close\r\n");
		msg.append("\r\n");
		byte[] bytes;
		try {
			bytes = msg.toString().getBytes("ASCII7");
		} catch (UnsupportedEncodingException ignored) {
			bytes = msg.toString().getBytes();
		}

		socket.getOutputStream().write(bytes);
		socket.getOutputStream().flush();

		readStatus();

		if (URIUtil.statusIsOK(statusCode)) {
			statusCode = 0; // 重置状态,否则代理连接可能超时,仍然返回200
		} else {
			socket.close();
			afterReader = true;
			throw new IOException("代理服务器连接失败");
		}

		SSLSocket s = (SSLSocket) ((SSLSocketFactory) SSLSocketFactory.getDefault()).createSocket(socket, url.getHost(), url.getPort(), true);
		s.startHandshake();
		socket = s;
		msg.setLength(0);
		msg.append(method);
		msg.append(" ");

		var urlSplit = url.toExternalForm().split("/");
		if (urlSplit.length < 4) {
			msg.append("/");
		} else {
			var path = new StringBuilder();
			for (int i = 3; i < urlSplit.length; i++) {
				path.append("/").append(urlSplit[i]);
			}
			msg.append(path);
		}

		msg.append(" HTTP/1.0\r\n");
		sendheaders.remove("accept-encoding");
		for (var h : sendheaders.entrySet()) {
			for (var l : h.getValue()) {
				msg.append(h.getKey()).append(": ").append(l);
				msg.append("\r\n");
			}
		}
		if (method.equals("POST") || method.equals("PUT")) {
			msg.append("Transfer-Encoding: Chunked\r\n");
		} else {
			msg.append("Content-Length: 0\r\n");
		}
		msg.append("Host: ").append(url.getHost()).append("\r\n");
		msg.append("Connection: close\r\n");
		msg.append("\r\n");
		try {
			bytes = msg.toString().getBytes("ASCII7");
		} catch (UnsupportedEncodingException ignored) {
			bytes = msg.toString().getBytes();
		}
		socket.getOutputStream().write(bytes);
		socket.getOutputStream().flush();
	}

	private void readStatus() throws IOException {
		var reply = new byte[200];
		var header = new byte[200];
		int replyLen = 0;
		int headerLen = 0;
		int newlinesSeen = 0;
		boolean headerDone = false;

		var in = socket.getInputStream();
		while (newlinesSeen < 2) {
			int i = in.read();
			if (i < 0) throw new IOException("Unexpected EOF from remote server");
			if (i == '\n') {
				if (headerDone) {
					var h = new String(header, 0, headerLen);
					var split = h.split(": ");
					if (split.length != 1) {
						headers.computeIfAbsent(split[0], l -> new ArrayList<>()).add(split[1]);
					}
				}
				headerDone = true;
				++newlinesSeen;
				headerLen = 0;
			} else if (i != '\r') {
				newlinesSeen = 0;
				if (!headerDone && replyLen < reply.length) {
					reply[replyLen++] = (byte) i;
				} else if (headerLen < header.length) {
					header[headerLen++] = (byte) i;
				}
			}
		}

		String[] replyStr;
		try {
			replyStr = new String(reply, 0, replyLen, "ASCII7").split(" ");
		} catch (UnsupportedEncodingException ignored) {
			replyStr = new String(reply, 0, replyLen).split(" ");
		}

		if (!replyStr[0].startsWith("HTTP/1.")) {
			throw new IOException("不支持的协议: " + replyStr[0]);
		}

		statusCode = Integer.parseInt(replyStr[1]);
		var sb = new StringBuilder();
		for (int i = 2; i < replyStr.length; i++) {
			sb.append(replyStr[i]);
		}
		responseMessage = sb.toString();
	}

	private boolean afterReader;

	private void afterRead() throws IOException {
		if (afterReader) return;
		afterReader = true;
		connect();
		readStatus();
	}

	private void afterWrite() throws IOException {
		if (afterwritten) return;
		afterwritten = true;
		socket.getOutputStream().write(String.valueOf(0).getBytes());
		socket.getOutputStream().write(NEWLINE);
		socket.getOutputStream().write(NEWLINE);
		socket.getOutputStream().flush();
	}

	@Override
	public void disconnect() {
		try {
			afterWrite();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean usingProxy() {
		return true;
	}
}
