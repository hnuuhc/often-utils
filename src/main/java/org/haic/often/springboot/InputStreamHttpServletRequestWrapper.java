package org.haic.often.springboot;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author haicdust
 * @version 1.0
 * @since 2023/7/4 21:22
 */
public class InputStreamHttpServletRequestWrapper extends HttpServletRequestWrapper {

	private final byte[] streamBody;
	private static final int BUFFER_SIZE = 8192;

	public InputStreamHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		byte[] bytes = inputStream2Byte(request.getInputStream());
		if (bytes.length == 0 && RequestMethod.POST.name().equals(request.getMethod())) {
			//从ParameterMap获取参数，并保存以便多次获取
			bytes = request.getParameterMap().entrySet().stream().map(entry -> {
				String result;
				String[] value = entry.getValue();
				if (value != null && value.length > 1) {
					result = Arrays.stream(value).map(s -> entry.getKey() + "=" + s).collect(Collectors.joining("&"));
				} else {
					assert value != null;
					result = entry.getKey() + "=" + value[0];
				}

				return result;
			}).collect(Collectors.joining("&")).getBytes();
		}

		streamBody = bytes;
	}

	private byte[] inputStream2Byte(InputStream inputStream) throws IOException {
		var outputStream = new ByteArrayOutputStream();
		byte[] bytes = new byte[BUFFER_SIZE];
		int length;
		while ((length = inputStream.read(bytes, 0, BUFFER_SIZE)) != -1) {
			outputStream.write(bytes, 0, length);
		}

		return outputStream.toByteArray();
	}

	@Override
	public ServletInputStream getInputStream() {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(streamBody);

		return new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener listener) {

			}

			@Override
			public int read() {
				return inputStream.read();
			}
		};
	}

	@Override
	public BufferedReader getReader() {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}
}
