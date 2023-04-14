package org.haic.often.web;

import org.haic.often.util.StringUtil;

import java.io.*;
import java.net.Socket;

/**
 * @author haicdust
 * @version 1.0
 * @since 2023/4/11 16:07
 */
public class Handler extends Thread {

	public void run(Socket sock) {
		try (var input = sock.getInputStream(); var output = sock.getOutputStream()) {
			handle(input, output);
		} catch (IOException ignored) {
		}
	}

	private void handle(InputStream input, OutputStream output) throws IOException {
		var reader = new BufferedReader(new InputStreamReader(input));
		var writer = new BufferedWriter(new OutputStreamWriter(output));
		// 读取HTTP请求:
		var first = reader.readLine().split(" ");
		var path = first[1];

		RequestParam params;
		var index = path.indexOf('?');
		if (index == -1) {
			params = new RequestParam();
		} else {
			path = path.substring(0, index);
			//noinspection DataFlowIssue
			params = new RequestParam(StringUtil.toMap(path.substring(index + 1), "&"));
		}

		var headers = new RequestHeader();
		while (true) {
			var header = reader.readLine();
			if (header.isEmpty()) { // 读取到空行时, HTTP Header读取完毕
				break;
			}
			int headerIndex = header.indexOf(": ");
			headers.put(header.substring(0, headerIndex).toLowerCase(), header.substring(headerIndex + 2));
		}

		var request = new HttpServletRequest(path, params, headers, reader);
		var response = new HttpServletResponse(writer);
		switch (first[0]) {
			case "GET" -> HttpSurface.get(path).doGet(request, response);
			case "POST" -> HttpSurface.get(path).doPost(request, response);
			case "PUT" -> HttpSurface.get(path).doPut(request, response);
			case "DELETE" -> HttpSurface.get(path).doDelete(request, response);
			case "HEAD" -> HttpSurface.get(path).doHead(request, response);
			case "TRACE" -> HttpSurface.get(path).doTrace(request, response);
			case "CONNECT" -> HttpSurface.get(path).doConnect(request, response);
			case "OPTIONS" -> HttpSurface.get(path).doOptions(request, response);
			default -> HttpSurface.get("400").doGet(request, response);
		}
	}
}
