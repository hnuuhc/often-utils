package org.haic.often.web;

import java.io.*;
import java.net.Socket;

public class Redirect extends Thread {
	public void run(Socket sock, String domain) {
		try (var input = sock.getInputStream(); var output = sock.getOutputStream()) {
			var reader = new BufferedReader(new InputStreamReader(input));
			var writer = new BufferedWriter(new OutputStreamWriter(output));
			var path = reader.readLine().split(" ")[1];
			writer.write("HTTP/1.1 301 Bad Request\r\n");
			writer.write("Location: " + domain + path + "\r\n");
			writer.flush();
		} catch (IOException ignored) {
		}
	}

}
