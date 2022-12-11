package org.haic.often.compress;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.archivers.jar.JarArchiveInputStream;
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.haic.often.exception.ZipException;
import org.haic.often.util.FileUtil;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author haicdust
 * @version 1.0
 * @since 2021/3/31 11:59
 */
public class JarUtil extends ZipUtil {

	private JarUtil() {
	}

	/**
	 * 将列表的bytes添加进压缩包
	 *
	 * @param origin 文件信息
	 * @return 添加压缩包中的文件列表
	 */
	@Override
	@Contract(pure = true)
	public List<String> compress(@NotNull Map<String, byte[]> origin) {
		List<String> result = new ArrayList<>();
		try (JarArchiveOutputStream outputStream = new JarArchiveOutputStream(new FileOutputStream(archive))) {
			result = compress(origin, outputStream);
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
	@Contract(pure = true)
	protected List<String> compress(@NotNull Map<String, byte[]> origin, JarArchiveOutputStream outputStream) {
		try {
			for (Map.Entry<String, byte[]> info : origin.entrySet()) {
				JarArchiveEntry entry = new JarArchiveEntry(info.getKey());
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
	 * 解压ZIP压缩包
	 *
	 * @param out 输出文件夹
	 * @return 解压的文件列表
	 */
	@Override
	@Contract(pure = true)
	public List<String> deCompress(@NotNull File out) {
		if (!archive.isFile()) {
			throw new ZipException("Not found or not file " + archive);
		}
		List<String> result = new ArrayList<>();
		try (JarArchiveInputStream inputStream = new JarArchiveInputStream(new BufferedInputStream(new FileInputStream(archive)), charsetName)) {
			JarArchiveEntry archiveEntry;
			while ((archiveEntry = inputStream.getNextJarEntry()) != null) {
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
	 * 往压缩包中添加列表中的bytes
	 *
	 * @param origin bytes列表
	 * @return 添加压缩包中的文件列表
	 */
	@Override
	@Contract(pure = true)
	public List<String> addBytes(@NotNull Map<String, byte[]> origin) {
		if (!archive.isFile()) {
			throw new ZipException("Not found or not file " + archive);
		}
		List<String> result = new ArrayList<>();
		if (out == null) {
			Zip4jUtil.origin(archive).remove(new ArrayList<>(origin.keySet()));
			result = Zip4jUtil.origin(archive).addStream(origin); // 添加文件
		} else {
			FileUtil.deteleFile(out);
			try (JarArchiveOutputStream outputStream = new JarArchiveOutputStream(new FileOutputStream(out)); JarArchiveInputStream inputStream = new JarArchiveInputStream(new FileInputStream(archive))) {
				JarArchiveEntry entry;
				while ((entry = inputStream.getNextJarEntry()) != null) {
					File archiveEntry = new File(out, entry.getName());
					archiveEntry.getParentFile().mkdirs();
					if (origin.containsKey(archiveEntry.getName())) {
						continue;
					}
					IOUtils.copy(inputStream, outputStream);
				}
				result = compress(origin, outputStream); // 添加文件
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result; // 添加文件
	}

}
