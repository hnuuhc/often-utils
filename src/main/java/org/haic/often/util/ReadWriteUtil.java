package org.haic.often.util;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.json.JSON;
import org.haic.often.parser.json.JSONArray;
import org.haic.often.parser.json.JSONObject;
import org.haic.often.parser.xml.Document;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 读写工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/4/12 11:04
 */
public class ReadWriteUtil {

	private File source; // 目标文件或文件夹
	private int DEFAULT_BUFFER_SIZE = 8192; // 缓冲区大小
	private Charset charset = StandardCharsets.UTF_8; // 字符集编码格式
	private boolean append = true; // 默认追加写入

	private ReadWriteUtil() {
	}

	/**
	 * 设置目标文件或文件夹并获取 new ReadWriteUtils
	 *
	 * @param source 文件或文件夹路径
	 * @return this
	 */
	public static ReadWriteUtil orgin(String source) {
		return orgin(new File(source));
	}

	/**
	 * 设置目标文件或文件夹并获取 new ReadWriteUtils
	 *
	 * @param source 文件或文件夹
	 * @return this
	 */
	public static ReadWriteUtil orgin(File source) {
		return config().file(source);
	}

	/**
	 * 获取 new ReadWriteUtils
	 *
	 * @return this
	 */
	protected static ReadWriteUtil config() {
		return new ReadWriteUtil();
	}

	/**
	 * 设置 文件或文件夹
	 *
	 * @param source 文件或文件夹
	 * @return this
	 */
	protected ReadWriteUtil file(File source) {
		this.source = source;
		return this;
	}

	/**
	 * 设置 缓冲区大小,用于写入数据时使用
	 *
	 * @param bufferSize 缓冲区大小
	 * @return this
	 */
	public ReadWriteUtil bufferSize(int bufferSize) {
		this.DEFAULT_BUFFER_SIZE = bufferSize;
		return this;
	}

	/**
	 * 设置 字符集编码格式
	 *
	 * @param charset 字符集编码格式
	 * @return this
	 */
	public ReadWriteUtil charset(Charset charset) {
		this.charset = charset;
		return this;
	}

	/**
	 * 设置 字符集格式
	 *
	 * @param charsetName 字符集格式
	 * @return this
	 */
	public ReadWriteUtil charset(String charsetName) {
		return charset(Charset.forName(charsetName));
	}

	/**
	 * 设置 追加写入
	 *
	 * @param append 启用追加写入,默认true
	 * @return this
	 */
	public ReadWriteUtil append(boolean append) {
		this.append = append;
		return this;
	}

	// ================================================== WriteUtils ==================================================

	/**
	 * 将数组合按行行写入文件,以"\n"结尾
	 *
	 * @param lists 字符串数组
	 * @return 写入是否成功
	 */
	public boolean writeAsLine(@NotNull List<String> lists) {
		File parent = source.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source, append), charset), DEFAULT_BUFFER_SIZE)) {
			outStream.write(String.join(StringUtil.LF, lists) + StringUtil.LF); // 文件输出流用于将数据写入文件
			outStream.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 将字符串写入文件
	 *
	 * @param s 字符串
	 * @return 写入是否成功
	 */
	public boolean write(@NotNull String s) {
		File parent = source.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source, append), charset), DEFAULT_BUFFER_SIZE)) {
			output.write(s); // 文件输出流用于将数据写入文件
			output.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 将byte数组写入文件
	 *
	 * @param b byte数组
	 * @return 写入是否成功
	 */
	public boolean write(byte[] b) {
		return write(b, 0, b.length);
	}

	/**
	 * 将byte数组写入文件
	 *
	 * @param b   byte数组
	 * @param off 起始位
	 * @param len 长度
	 * @return 写入是否成功
	 */
	public boolean write(byte[] b, int off, int len) {
		File parent = source.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (FileOutputStream output = new FileOutputStream(source, append)) {
			output.write(b, off, len); // 文件输出流用于将数据写入文件
			output.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 将数组按照分隔符写入文件,默认为空格
	 *
	 * @param lists 字符串数组
	 * @return 写入是否成功
	 */
	public boolean write(@NotNull List<String> lists) {
		return write(lists, StringUtil.SPACE);
	}

	/**
	 * 将数组按照分隔符写入文件
	 *
	 * @param lists 字符串数组
	 * @return 写入是否成功
	 */
	public boolean write(@NotNull List<String> lists, @NotNull String regex) {
		File parent = source.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (BufferedWriter outStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(source, append), charset), DEFAULT_BUFFER_SIZE)) {
			outStream.write(String.join(regex, lists)); // 文件输出流用于将数据写入文件
			outStream.flush();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * FileChannel方式写入文件文本
	 *
	 * @param s 字符串
	 * @return 写入是否成功
	 */
	public boolean channelWrite(@NotNull String s) {
		File parent = source.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (FileOutputStream out = new FileOutputStream(source, append); FileChannel channel = out.getChannel()) {
			channel.write(charset.encode(s));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * RandomAccessFile方式写入文本
	 *
	 * @param s 字符串
	 * @return 写入是否成功
	 */
	public boolean randomWrite(@NotNull String s) {
		File parent = source.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (RandomAccessFile randomAccess = new RandomAccessFile(source, "rw")) {
			if (append) {
				randomAccess.seek(source.length());
			}
			randomAccess.write(s.getBytes(charset));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * MappedByteBuffer内存映射方法写入文件
	 *
	 * @param s 字符串
	 * @return 写入是否成功
	 */
	public boolean mappedWrite(String s) {
		File parent = source.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		byte[] params = s.getBytes(charset);
		try (FileChannel fileChannel = append ? FileChannel.open(source.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE) : FileChannel.open(source.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING)) {
			fileChannel.map(FileChannel.MapMode.READ_WRITE, append ? source.length() : 0, params.length).put(params);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 文件复制
	 *
	 * @param out 指定输出文件路径
	 * @return 文件复制状态
	 */
	public boolean copy(String out) {
		return copy(new File(out));
	}

	/**
	 * 文件复制
	 *
	 * @param out 指定输出文件
	 * @return 文件复制状态
	 */
	public boolean copy(File out) {
		File parent = out.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (InputStream input = new FileInputStream(source); OutputStream output = new FileOutputStream(out)) {
			input.transferTo(output);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * RandomAccessFile 文件复制
	 *
	 * @param out 指定输出文件
	 * @return 文件复制
	 */
	public boolean randomCopy(@NotNull String out) {
		return randomCopy(new File(out));
	}

	/**
	 * RandomAccessFile 文件复制
	 *
	 * @param out 指定输出文件
	 * @return 文件复制
	 */
	public boolean randomCopy(@NotNull File out) {
		File parent = out.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (RandomAccessFile input = new RandomAccessFile(source, "r"); RandomAccessFile output = new RandomAccessFile(out, "rw")) {
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length;
			while ((length = input.read(buffer)) != -1) {
				output.write(buffer, 0, length);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * FileChannel 文件复制
	 *
	 * @param out 指定输出文件
	 * @return 文件复制状态
	 */
	public boolean channelCopy(String out) {
		return channelCopy(new File(out));
	}

	/**
	 * FileChannel 文件复制
	 *
	 * @param out 指定输出文件
	 * @return 文件复制状态
	 */
	public boolean channelCopy(File out) {
		File parent = out.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (FileInputStream inStream = new FileInputStream(source); FileChannel input = inStream.getChannel(); FileOutputStream outStream = new FileOutputStream(out); FileChannel output = outStream.getChannel()) {
			int count = 0;
			long size = input.size();
			while (count < size) { // 循环支持2G以上文件
				int position = count;
				count += output.transferFrom(input, position, size - position);
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean mappedCopy(String out) {
		return mappedCopy(new File(out));
	}

	/**
	 * MappedByteBuffer 文件复制
	 *
	 * @param out 指定输出文件路径
	 * @return 文件复制状态
	 */
	public boolean mappedCopy(File out) {
		File parent = out.getParentFile();
		if (parent != null) FileUtil.createFolder(parent);
		try (FileInputStream inStream = new FileInputStream(source); FileChannel input = inStream.getChannel(); RandomAccessFile outStream = new RandomAccessFile(out, "rw"); FileChannel output = outStream.getChannel()) {
			long size = input.size();
			for (long i = 0; i < size; i += Integer.MAX_VALUE) {
				long position = Integer.MAX_VALUE * i;
				output.map(FileChannel.MapMode.READ_WRITE, position, size - position).put(input.map(FileChannel.MapMode.READ_ONLY, position, size - position));
			}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	// ================================================== ReadUtils ==================================================

	/**
	 * 遍历文件或文件夹,按行读取内容
	 *
	 * @return 文本信息列表
	 */
	public List<String> readAsLine() {
		Function<File, List<String>> read = file -> {
			List<String> result = null;
			try (InputStream in = new FileInputStream(file)) {
				result = IOUtil.stream(in).charset(charset).readAsLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		};
		return source.isDirectory() ? FileUtil.iterateFiles(source).stream().map(read).collect(ArrayList::new, ArrayList::addAll, ArrayList::addAll) : read.apply(source);
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return 文本信息
	 */
	public String read() {
		String result = null;
		try (InputStream in = new FileInputStream(source)) {
			result = IOUtil.stream(in).charset(charset).read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return json
	 */
	public JSONObject readJSON() {
		var str = read();
		return str.isEmpty() ? null : JSON.parseObject(str);
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return json
	 */
	public JSONArray readJSONArray() {
		var str = read();
		return str.isEmpty() ? null : JSON.parseArray(str);
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return Document
	 */
	public Document readXML() {
		var str = read();
		return str.isEmpty() ? null : Document.parse(str);
	}

	/**
	 * 读取指定文件的内容
	 *
	 * @return byte数组
	 */
	public byte[] readBytes() {
		byte[] result = null;
		try (InputStream in = new FileInputStream(source)) {
			result = IOUtil.stream(in).charset(charset).toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * FileChannel 读取文件文本
	 *
	 * @return 文本字符串
	 */
	public String channelRead() {
		CharBuffer result = null;
		try (FileInputStream in = new FileInputStream(source); FileChannel channel = in.getChannel()) {
			ByteBuffer buffer = ByteBuffer.allocate(Math.toIntExact(source.length()));
			channel.read(buffer);
			buffer.flip();
			result = charset.decode(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return String.valueOf(result);
	}

	/**
	 * RandomAccessFile 随机存储读取文件
	 *
	 * @return 文本
	 */
	public String randomRead() {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		try (RandomAccessFile randomAccess = new RandomAccessFile(source, "r")) {
			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length;
			while ((length = randomAccess.read(buffer)) != -1) {
				result.write(buffer, 0, length);
			}
			result.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result.toString(charset);
	}

	/**
	 * MappedByteBuffer 内存映射方法读取文件文本
	 *
	 * @return 文本
	 */
	public String mappedRead() {
		CharBuffer result = null;
		try (FileInputStream in = new FileInputStream(source); FileChannel channel = in.getChannel()) {
			result = charset.decode(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()).asReadOnlyBuffer());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return String.valueOf(result);
	}

}
