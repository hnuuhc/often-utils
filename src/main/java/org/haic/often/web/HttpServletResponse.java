package org.haic.often.web;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

public class HttpServletResponse {

	private final RequestHeader headers = new RequestHeader();
	private final BufferedWriter writer;
	private String status = "HTTP/1.1 200 OK";
	private String body;

	public HttpServletResponse(BufferedWriter writer) {
		this.writer = writer;
	}

	public void status(String status) {
		this.status = status;
	}

	public void headers(Map<String, String> headers) {
		this.headers.putAll(headers);
	}

	public void header(String name, String value) {
		headers.put(name, value);
	}

	public void write(String body) {
		this.body = body;
	}

	public void flush() throws IOException {
		writer.write(status + "\r\n");
		for (var header : headers.entrySet()) {
			writer.write(header.getKey() + ": " + header.getValue() + "\r\n");
		}
		writer.write("\r\n");
		writer.write(body);
		writer.flush();
	}

}
