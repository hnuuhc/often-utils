package org.haic.often.compress;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.ZipException;
import org.haic.often.util.FileUtil;
import org.haic.often.util.ReadWriteUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author haicdust
 * @version 1.1
 * @since 2021/8/29 16:10
 */
public class ZipUtil {

	protected File archive; // 压缩包
	protected File out; // 输出文件
	protected String charsetName = "UTF8"; // 字符集编码格式
	protected boolean includeRoot; // 压缩时包含根目录
	protected boolean archiveName; // 解压使用压缩包名称文件夹

	protected ZipUtil() {
	}

	/**
	 * 获取新的ZipUtils对象并设置压缩包文件
	 *
	 * @param archive 压缩包路径
	 * @return new ZipUtils
	 */
	public static ZipUtil origin(String archive) {
		return origin(new File(archive));
	}

	/**
	 * 获取新的ZipUtils对象并设置压缩包文件
	 *
	 * @param archive 压缩包
	 * @return new ZipUtils
	 */
	public static ZipUtil origin(File archive) {
		return config().archive(archive);

	}

	/**
	 * 获取新的 Zip4jUtils
	 *
	 * @return new Zip4jUtils
	 */
	protected static ZipUtil config() {
		return new ZipUtil();
	}

	/**
	 * 设置 压缩包文件
	 *
	 * @param archive 压缩包文件
	 * @return this
	 */
	protected ZipUtil archive(File archive) {
		this.archive = archive;
		return this;
	}

	/**
	 * 在解压压缩包时使用，解压至压缩包名称的文件夹
	 *
	 * @param archiveName 启用 解压使用压缩包名称文件夹
	 * @return this
	 */
	public ZipUtil archiveName(boolean archiveName) {
		this.archiveName = archiveName;
		return this;
	}

	/**
	 * 设置 字符集编码格式名称
	 *
	 * @param charsetName 字符集编码格式名称
	 * @return this
	 */
	public ZipUtil charset(String charsetName) {
		this.charsetName = charsetName;
		return this;
	}

	/**
	 * 设置添加文件时输出文件路径
	 *
	 * @param out 输出文件路径
	 * @return this
	 */
	public ZipUtil out(String out) {
		return out(new File(out));
	}

	/**
	 * 设置添加文件时输出文件
	 *
	 * @param out 输出文件
	 * @return this
	 */
	public ZipUtil out(File out) {
		this.out = out;
		return this;
	}

	/**
	 * 压缩文件夹时，包含根目录
	 *
	 * @param includeRoot 启用 包含根目录
	 * @return this
	 */
	public ZipUtil includeRoot(boolean includeRoot) {
		this.includeRoot = includeRoot;
		return this;
	}

	/**
	 * 解压ZIP压缩包
	 *
	 * @param out 输出文件夹路径
	 * @return 解压的文件列表
	 */
	public List<String> deCompress(@NotNull String out) {
		return deCompress(new File(out));
	}

	/**
	 * 解压ZIP压缩包
	 *
	 * @param out 输出文件夹
	 * @return 解压的文件列表
	 */
	@Contract(pure = true)
	public List<String> deCompress(@NotNull File out) {
		if (!archive.isFile()) {
			throw new ZipException("Not found or not file " + archive);
		}
		List<String> result = new ArrayList<>();
		try (ZipArchiveInputStream inputStream = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(archive)), charsetName)) {
			ZipArchiveEntry archiveEntry;
			while ((archiveEntry = inputStream.getNextZipEntry()) != null) {
				if (archiveEntry.isDirectory()) {
					continue;
				}
				File curfile = new File(archiveName ? new File(out, archive.getName().substring(0, archive.getName().lastIndexOf(46))) : out, archiveEntry.getName());
				FileUtil.createFolder(curfile.getParentFile());
				IOUtils.copy(inputStream, new FileOutputStream(curfile)); // 将文件写出到解压的目录
				result.add(archiveEntry.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 压缩文件或文件夹
	 *
	 * @param origin 文件或文件夹路径
	 * @return 添加压缩包中的文件列表
	 */
	public List<String> compress(@NotNull String origin) {
		return compress(new File(origin));
	}

	/**
	 * 压缩文件或文件夹
	 *
	 * @param origin 文件或文件夹
	 * @return 添加压缩包中的文件列表
	 */
	@Contract(pure = true)
	public List<String> compress(@NotNull File origin) {
		if (!origin.exists()) {
			throw new ZipException("Not found " + origin);
		}
		return compress(getFilesInfo(origin));
	}

	/**
	 * 将列表的bytes添加进压缩包
	 *
	 * @param origin 文件信息
	 * @return 添加压缩包中的文件列表
	 */
	@Contract(pure = true)
	public List<String> compress(@NotNull Map<String, byte[]> origin) {
		List<String> result = new ArrayList<>();
		try (ZipArchiveOutputStream archiveOutputStream = new ZipArchiveOutputStream(archive)) {
			result = compress(origin, archiveOutputStream);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 将列表的bytes添加到指定输出流
	 *
	 * @param outputStream 输出流
	 * @param origin       文件信息
	 * @return 添加压缩包中的文件列表
	 */
	protected List<String> compress(@NotNull Map<String, byte[]> origin, ZipArchiveOutputStream outputStream) {
		try {
			for (Map.Entry<String, byte[]> info : origin.entrySet()) {
				ZipArchiveEntry entry = new ZipArchiveEntry(info.getKey());
				outputStream.putArchiveEntry(entry);
				outputStream.write(info.getValue());
				outputStream.closeArchiveEntry();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>(origin.keySet());
	}

	/**
	 * 将bytes添加进压缩包
	 *
	 * @param bytes     文件Bytes
	 * @param entryName 在压缩包中显示的路径、名称
	 * @return 添加压缩包中的文件列表
	 */
	@Contract(pure = true)
	public List<String> compress(byte[] bytes, @NotNull String entryName) {
		return compress(Map.of(entryName, bytes));
	}

	/**
	 * 添加文件或文件夹
	 *
	 * @param origin 文件或文件夹路径
	 * @return 添加压缩包中的文件列表
	 */
	public List<String> addFiles(@NotNull String origin) {
		return addFiles(new File(origin));
	}

	/**
	 * 添加文件或文件夹
	 *
	 * @param origin 文件或文件夹
	 * @return 添加压缩包中的文件列表
	 */
	@Contract(pure = true)
	public List<String> addFiles(@NotNull File origin) {
		return addBytes(getFilesInfo(origin));
	}

	/**
	 * 将字符串添加到压缩包中
	 *
	 * @param str       字符串
	 * @param entryName 在压缩包中显示的路径、名称
	 * @return 添加压缩包中的文件列表
	 */
	@Contract(pure = true)

	public List<String> addStr(@NotNull String str, @NotNull String entryName) {
		return addByte(str.getBytes(StandardCharsets.UTF_8), entryName);
	}

	/**
	 * 往压缩包中添加bytes
	 *
	 * @param bytes     文件bytes
	 * @param entryName 在压缩包中显示的路径、名称
	 * @return 添加压缩包中的文件列表
	 */
	@Contract(pure = true)
	public List<String> addByte(byte[] bytes, @NotNull String entryName) {
		return addBytes(Map.of(entryName, bytes));
	}

	/**
	 * 往压缩包中添加列表中的bytes
	 *
	 * @param origin bytes列表
	 * @return 添加压缩包中的文件列表
	 */
	@Contract(pure = true)
	public List<String> addBytes(@NotNull Map<String, byte[]> origin) {
		if (!archive.isFile()) {
			throw new ZipException("Not found or not file " + archive);
		}
		List<String> result = new ArrayList<>();
		if (out == null) {
			if (archive.exists()) {
				Zip4jUtil.origin(archive).remove(new ArrayList<>(origin.keySet()));
				result = Zip4jUtil.origin(archive).addStream(origin); // 添加文件
			}
		} else {
			FileUtil.deteleFile(out);
			try (ZipFile zipFile = new ZipFile(archive); ZipArchiveOutputStream outputStream = new ZipArchiveOutputStream(out)) {
				Enumeration<ZipArchiveEntry> enumeration = zipFile.getEntries();
				while (enumeration.hasMoreElements()) { // 原样拷贝
					ZipArchiveEntry entry = enumeration.nextElement();
					if (origin.containsKey(entry.getName())) {
						continue;
					}
					outputStream.putArchiveEntry(entry);
					if (!entry.isDirectory()) {
						IOUtils.copy(zipFile.getInputStream(entry), outputStream);
					}
					outputStream.closeArchiveEntry();
				}
				result = compress(origin, outputStream); // 添加文件
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取文件信息
	 *
	 * @param origin 来源文件或文件夹
	 * @return 文件列表信息集合
	 */
	@NotNull
	@Contract(pure = true)
	protected Map<String, byte[]> getFilesInfo(@NotNull File origin) {
		if (!origin.exists()) {
			throw new ZipException("Not found " + origin);
		}
		return origin.isFile() ? Map.of(includeRoot ? origin.getParentFile().getName() + "/" : "" + origin.getName(), ReadWriteUtil.orgin(origin).readBytes()) : FileUtil.iterateFiles(origin).parallelStream().collect(Collectors.toMap(file -> includeRoot ? origin.getName() + "/" : "" + file.getAbsolutePath().substring(origin.getAbsolutePath().length() + 1).replaceAll("\\\\", "/"), file -> ReadWriteUtil.orgin(file).readBytes()));
	}

}
