package org.haic.often.web;

import java.io.BufferedReader;
import java.io.IOException;

public class HttpServletRequest {

	private final String path;

	private final RequestParam params;
	private final RequestHeader headers;
	private final BufferedReader reader;

	public HttpServletRequest(String path, RequestParam params, RequestHeader headers, BufferedReader reader) {
		this.path = path;
		this.params = params;
		this.headers = headers;
		this.reader = reader;
	}

	public String path() {
		return path;
	}

	public RequestParam params() {
		return params;
	}

	public String param(String name) {
		return params.get(name);
	}

	public RequestHeader headers() {
		return headers;
	}

	public String header(String name) {
		return headers.get(name);
	}

	public String body() throws IOException {
		var length = headers.get("content-length");
		if (length == null) {
			if (header("transfer-encoding").equals("chunked")) {
				var sb = new StringBuilder();
				for (int len = Integer.parseInt(reader.readLine()); len > 0; len = Integer.parseInt(reader.readLine())) {
					var buff = new char[len];
					reader.read(buff);
					sb.append(buff);
				}
				return sb.toString();
			} else {
				return null;
			}
		} else {
			var buff = new char[Integer.parseInt(length)];
			reader.read(buff);
			return new String(buff);
		}
	}

}
