package org.haic.often.compress;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.*;
import org.haic.often.Judge;
import org.haic.often.annotations.NotNull;
import org.haic.often.exception.ZipException;
import org.haic.often.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Zip工具类
 *
 * @author haicdust
 * @version 1.1
 * @since 2021/8/29 16:14
 */
public class ZipUtil {

	private ZipUtil() {}

	/**
	 * 获取新的ZipUtil对象并设置压缩包文件
	 *
	 * @param archive 压缩包文件路径
	 * @return new Zip4jUtils
	 */
	public static Zip origin(@NotNull String archive) {
		return origin(new File(archive));
	}

	/**
	 * 获取新的ZipUtil对象并设置压缩包文件
	 *
	 * @param archive 压缩包文件
	 * @return new Zip4jUtils
	 */
	public static Zip origin(@NotNull File archive) {
		return new ZipBuilder().origin(archive);

	}

	private static class ZipBuilder extends Zip {

		private final ZipParameters params = new ZipParameters(); // 压缩参数
		private File archive; // 压缩包
		private char[] passwd; // 压缩包密码
		private CompressionMethod method = CompressionMethod.DEFLATE;// 压缩方式
		private CompressionLevel level = CompressionLevel.FASTEST;// 压缩级别
		private Charset charset = StandardCharsets.UTF_8;// 字符集编码格式
		private boolean archiveName; // 解压使用压缩包名称文件夹

		private ZipBuilder() {
			params.setCompressionMethod(method); // 压缩方式
			params.setCompressionLevel(level); // 压缩级别
			params.setIncludeRootFolder(false); // 包含根文件夹
		}

		public Zip origin(@NotNull String archive) {
			return origin(new File(archive));
		}

		public Zip origin(@NotNull File archive) {
			this.archive = archive;
			return this;
		}

		public Zip archiveName(boolean archiveName) {
			this.archiveName = archiveName;
			return this;
		}

		public Zip passwd(@NotNull String passwd) {
			this.passwd = passwd.toCharArray();
			params.setEncryptFiles(this.passwd.length != 0); // 设置文件加密
			params.setEncryptionMethod(EncryptionMethod.AES); // 加密方式
			params.setAesVersion(AesVersion.TWO); // 用于加密的AES格式版本
			params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256); // AES加密密钥的密钥强度
			return this;
		}

		public Zip includeRoot(boolean includeRoot) {
			params.setIncludeRootFolder(includeRoot);
			return this;
		}

		public Zip method(@NotNull CompressionMethod method) {
			this.method = method;
			return this;
		}

		public Zip level(@NotNull CompressionLevel level) {
			this.level = level;
			return this;
		}

		public Zip charset(@NotNull Charset charset) {
			this.charset = charset;
			return this;
		}

		public Zip charset(@NotNull String charsetName) {
			this.charset = Charset.forName(charsetName);
			return this;
		}

		public boolean deCompress(@NotNull String out) {
			return deCompress(new File(out));
		}

		public boolean deCompress(@NotNull File out) {
			if (!archive.isFile()) throw new ZipException("Not found or not file " + archive);
			if (out.isFile()) throw new ZipException("解压路径是一个文件");
			File parent = out.getParentFile();
			if (parent != null) FileUtil.createFolder(parent);
			try (ZipFile zipFile = new ZipFile(archive)) {
				zipFile.setCharset(charset);
				if (!zipFile.isValidZipFile()) {
					throw new ZipException("压缩文件不合法,可能被损坏");
				}
				if (zipFile.isEncrypted()) { // 3.判断是否已加密
					zipFile.setPassword(passwd);
				}
				zipFile.extractAll((archiveName ? new File(out, archive.getName().substring(0, archive.getName().lastIndexOf(46))) : out).getPath()); // 4.解压所有文件
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		public boolean compress(@NotNull String origin) {
			return compress(new File(origin));
		}

		public boolean compress(@NotNull File origin) {
			if (!origin.exists()) {
				throw new ZipException("Not found " + origin);
			}
			try (ZipFile zipFile = new ZipFile(archive)) {
				zipFile.setCharset(charset);
				if (!Judge.isEmpty(passwd)) {
					zipFile.setPassword(passwd);
				}
				if (origin.isFile()) { // 添加文件,优先删除,否则存在文件会照成效率极其低下
					zipFile.removeFile(origin.getName());
					zipFile.addFile(origin, params);
				} else {
					zipFile.removeFiles(FileUtil.iterateFiles(origin).stream().map(file -> file.getAbsolutePath().substring(origin.getAbsolutePath().length() + 1).replaceAll("\\\\", "/")).collect(Collectors.toList()));
					zipFile.addFolder(origin, params);
				}
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		public boolean addStream(@NotNull InputStream inputStream, String entryName) {
			try (ZipFile zipFile = new ZipFile(archive)) {
				zipFile.setCharset(charset);
				if (!Judge.isEmpty(passwd)) {
					zipFile.setPassword(passwd);
				}
				params.setFileNameInZip(entryName);
				zipFile.removeFile(entryName);
				zipFile.addStream(inputStream, params);
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		public boolean addStream(@NotNull Map<String, InputStream> origin) {
			try (ZipFile zipFile = new ZipFile(archive)) {
				for (var entry : origin.entrySet()) {
					zipFile.setCharset(charset);
					if (!Judge.isEmpty(passwd)) {
						zipFile.setPassword(passwd);
					}
					var entryName = entry.getKey();
					params.setFileNameInZip(entryName);
					zipFile.removeFile(entryName);
					zipFile.addStream(entry.getValue(), params);
				}
				return true;
			} catch (IOException e) {
				return false;
			}
		}

		public boolean remove(@NotNull String origin) {
			if (!archive.isFile()) {
				throw new ZipException("Not found or not file " + archive);
			}
			try (ZipFile zipFile = new ZipFile(archive)) {
				zipFile.removeFile(origin);
			} catch (IOException e) {
				return false;
			}
			return true;
		}

		public boolean remove(@NotNull List<String> origin) {
			if (!archive.isFile()) {
				throw new ZipException("Not found or not file " + archive);
			}
			try (ZipFile zipFile = new ZipFile(archive)) {
				zipFile.removeFiles(origin);
			} catch (IOException e) {
				return false;
			}
			return true;
		}
	}

}
