package org.haic.often.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * IO工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/13 12:01
 */
public class IOUtil {

	/**
	 * InputStream 流工具
	 *
	 * @param inputStream InputStream
	 * @return new InputStreamUtil
	 */
	@Contract(pure = true)
	public static InputStreamUtil stream(@NotNull InputStream inputStream) {
		return new InputStreamUtil(inputStream);
	}

	/**
	 * BufferedInputStream 流工具
	 *
	 * @param inputStream BufferedInputStream
	 * @return new InputStreamReaderUtil
	 */
	@Contract(pure = true)
	public static BufferedInputStreamUtil stream(@NotNull BufferedInputStream inputStream) {
		return new BufferedInputStreamUtil(inputStream);
	}

	/**
	 * InputStreamReader 流工具
	 *
	 * @param inputStream InputStreamReader
	 * @return new InputStreamReaderUtil
	 */
	@Contract(pure = true)
	public static InputStreamReaderUtil stream(@NotNull InputStreamReader inputStream) {
		return new InputStreamReaderUtil(inputStream);
	}

	/**
	 * 流工具类
	 */
	protected abstract static class InputStreamBuilder {

		protected Charset charset = StandardCharsets.UTF_8;

		protected InputStreamBuilder() {
		}

		/**
		 * 设置 字符集编码(默认UTF8)
		 *
		 * @param charsetName 字符集编码名称
		 * @return this
		 */
		@Contract(pure = true)
		public abstract InputStreamBuilder charset(@NotNull String charsetName);

		/**
		 * 设置 字符集编码(默认UTF8)
		 *
		 * @param charset 字符集编码
		 * @return this
		 */
		@Contract(pure = true)
		public abstract InputStreamBuilder charset(@NotNull Charset charset);

		/**
		 * 获取 Stream 中字符串信息
		 *
		 * @return 字符串文本
		 */
		@NotNull
		@Contract(pure = true)
		public abstract String read() throws IOException;

		/**
		 * 获取 Stream 中字符串信息
		 *
		 * @return 按行分割的字符串列表
		 */
		@NotNull
		@Contract(pure = true)
		public abstract List<String> readAsLine() throws IOException;

		/**
		 * 获取 Stream 中字符信息,转为 ByteArrayOutputStream
		 *
		 * @return ByteArrayOutputStream
		 */
		@NotNull
		@Contract(pure = true)
		public abstract ByteArrayOutputStream toByteArrayOutputStream() throws IOException;

		/**
		 * 获取 Stream 中字符信息
		 *
		 * @return byte数组
		 */
		@Contract(pure = true)
		public abstract byte[] toByteArray() throws IOException;

	}

	/**
	 * InputStreamUtil 工具类
	 */
	public static class InputStreamUtil extends InputStreamBuilder {
		private final InputStream stream;

		private InputStreamUtil(@NotNull InputStream in) {
			this.stream = in;
		}

		@Contract(pure = true)
		public InputStreamUtil charset(@NotNull String charsetName) {
			this.charset = Charset.forName(charsetName);
			return this;
		}

		@Contract(pure = true)
		public InputStreamUtil charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
		}

		@NotNull
		@Contract(pure = true)
		public String read() throws IOException {
			return toByteArrayOutputStream().toString(charset);
		}

		@NotNull
		@Contract(pure = true)
		public List<String> readAsLine() throws IOException {
			return stream(new InputStreamReader(stream, charset)).readAsLine();
		}

		@Contract(pure = true)
		public byte[] toByteArray() throws IOException {
			return toByteArrayOutputStream().toByteArray();
		}

		@NotNull
		@Contract(pure = true)
		public ByteArrayOutputStream toByteArrayOutputStream() throws IOException {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			stream.transferTo(result);
			return result;
		}

	}

	/**
	 * BufferedInputStreamUtil 工具类
	 */
	public static class BufferedInputStreamUtil extends InputStreamBuilder {
		private final BufferedInputStream stream;

		private BufferedInputStreamUtil(BufferedInputStream in) {
			this.stream = in;
		}

		@Contract(pure = true)
		public BufferedInputStreamUtil charset(@NotNull String charsetName) {
			this.charset = Charset.forName(charsetName);
			return this;
		}

		@Contract(pure = true)
		public BufferedInputStreamUtil charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
		}

		@NotNull
		@Contract(pure = true)
		public String read() throws IOException {
			return toByteArrayOutputStream().toString(charset);
		}

		@NotNull
		@Contract(pure = true)
		public List<String> readAsLine() throws IOException {
			return stream(new InputStreamReader(stream, charset)).readAsLine();
		}

		@NotNull
		@Contract(pure = true)
		public ByteArrayOutputStream toByteArrayOutputStream() throws IOException {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			stream.transferTo(result);
			return result;
		}

		@Contract(pure = true)
		public byte[] toByteArray() throws IOException {
			return toByteArrayOutputStream().toByteArray();
		}

	}

	/**
	 * InputStreamReader 工具类
	 */
	public static class InputStreamReaderUtil {
		private final InputStreamReader stream;

		private InputStreamReaderUtil(@NotNull InputStreamReader in) {
			this.stream = in;
		}

		/**
		 * 获取 Stream 中字符串信息
		 *
		 * @return 字符串文本
		 */
		@NotNull
		@Contract(pure = true)
		public String read() throws IOException {
			StringWriter result = new StringWriter();
			stream.transferTo(result);
			return String.valueOf(result);
		}

		/**
		 * 获取 Stream 中字符串信息
		 *
		 * @return 按行分割的字符串列表
		 */
		@NotNull
		@Contract(pure = true)
		public List<String> readAsLine() throws IOException {
			List<String> result = new ArrayList<>();
			BufferedReader bufferedReader = new BufferedReader(stream);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				result.add(line);
			}
			return result;
		}

	}

	protected abstract static class OutputStreamBuilder {

		protected Charset charset = StandardCharsets.UTF_8;

		protected OutputStreamBuilder() {
		}

		/**
		 * 设置 字符集编码(默认UTF8)
		 *
		 * @param charsetName 字符集编码名称
		 * @return this
		 */
		@Contract(pure = true)
		public abstract OutputStreamBuilder charset(@NotNull String charsetName);

		/**
		 * 设置 字符集编码(默认UTF8)
		 *
		 * @param charset 字符集编码
		 * @return this
		 */
		@Contract(pure = true)
		public abstract OutputStreamBuilder charset(@NotNull Charset charset);

	}

	public static class OutputStreamUtil extends OutputStreamBuilder {

		private final FileOutputStream stream;

		private OutputStreamUtil(@NotNull FileOutputStream stream) {
			this.stream = stream;
		}

		@Contract(pure = true)
		public OutputStreamUtil charset(@NotNull String charsetName) {
			this.charset = Charset.forName(charsetName);
			return this;
		}

		@Contract(pure = true)
		public OutputStreamUtil charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
		}

		/**
		 * 使用BufferedWriter方式向输出流内写入字符数据
		 *
		 * @param s 字符数据
		 */
		@Contract(pure = true)
		public void write(String s) throws IOException {
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(stream, charset), 8192);
			out.write(s); // 文件输出流用于将数据写入文件
			out.flush();
		}

	}

}
