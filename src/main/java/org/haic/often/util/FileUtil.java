package org.haic.often.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.haic.often.Judge;
import org.haic.often.Symbol;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.swing.filechooser.FileSystemView;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2021/3/7 17:29
 */
public class FileUtil {

	/**
	 * 获取文件名的字符长度
	 *
	 * @param fileName 文件名
	 * @return 文件名的字符长度
	 */
	@Contract(pure = true)
	public static int nameLength(@NotNull String fileName) {
		return new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1).length();
	}

	/**
	 * 获取文件名的字符长度
	 *
	 * @param file 文件对象
	 * @return 文件名的字符长度
	 */
	@Contract(pure = true)
	public static int nameLength(@NotNull File file) {
		return nameLength(file.getName());
	}

	/**
	 * 文件hash效验, 支持 MD5, SHA1, SHA256, SHA384, SHA512
	 *
	 * @param filePath 文件路径
	 * @param hash     待效验的值
	 * @return 判断是否匹配
	 */
	@Contract(pure = true)
	public static boolean hashValidity(@NotNull String filePath, @NotNull String hash) {
		return hashValidity(new File(filePath), hash);
	}

	/**
	 * 文件效验, 支持 MD5, SHA1, SHA256, SHA384, SHA512
	 *
	 * @param file 文件
	 * @param hash 待效验的值
	 * @return 判断是否匹配
	 */
	@Contract(pure = true)
	public static boolean hashValidity(@NotNull File file, @NotNull String hash) {
		return hashGet(file, hash).equals(hash.toLowerCase());
	}

	/**
	 * 根据所给hash位数,获取相应类型的文件hash值
	 *
	 * @param filePath 文件路径
	 * @param hash     hash值
	 * @return 文件hash值
	 */
	@Contract(pure = true)
	public static String hashGet(@NotNull String filePath, @NotNull String hash) {
		return hashGet(new File(filePath), hash);
	}

	/**
	 * 根据所给hash位数,获取相应类型的文件hash值
	 *
	 * @param file 文件
	 * @param hash hash值
	 * @return 文件hash值
	 */
	@Contract(pure = true)
	public static String hashGet(@NotNull File file, @NotNull String hash) {
		String result;
		if (hash.length() == 32) {
			result = FileUtil.getMD5(file);
		} else if (hash.length() == 40) {
			result = FileUtil.getSHA1(file);
		} else if (hash.length() == 64) {
			result = FileUtil.getSHA256(file);
		} else if (hash.length() == 96) {
			result = FileUtil.getSHA384(file);
		} else if (hash.length() == 128) {
			result = FileUtil.getSHA512(file);
		} else {
			throw new RuntimeException("hash值位数不正确");
		}
		return result.toLowerCase();
	}

	/**
	 * 受于系统限制,对文件名长度进行效验,防止致命错误
	 *
	 * @param fileName 文件名
	 */
	@Contract(pure = true)
	public static void fileNameValidity(@NotNull String fileName) {
		if (FileUtil.nameLength(fileName) > 240) {
			throw new RuntimeException("Error: File name length is greater than 240 FileName: " + fileName);
		}
	}

	/**
	 * 如果文件存在，删除文件
	 *
	 * @param filepath 文件路径
	 * @return 删除是否成功
	 */
	@Contract(pure = true)
	public static boolean deteleFile(String filepath) {
		return deteleFile(new File(filepath));
	}

	/**
	 * 如果文件存在，删除文件
	 *
	 * @param file 文件
	 * @return 删除是否成功
	 */
	@Contract(pure = true)
	public static boolean deteleFile(File file) {
		return file.exists() && file.delete();
	}

	/**
	 * 删除列表文件
	 *
	 * @param files 文件列表
	 * @return 删除的文件列表
	 */
	@Contract(pure = true)
	public static List<String> deteleFiles(List<File> files) {
		return files.parallelStream().filter(FileUtil::deteleFile).map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 删除空文件夹
	 *
	 * @param filesPath 文件夹路径
	 * @return 删除的空文件夹路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> deleteBlankDirectory(@NotNull String filesPath) {
		return deleteBlankDirectory(new File(filesPath));
	}

	/**
	 * 删除空文件夹
	 *
	 * @param files 文件夹
	 * @return 删除的空文件夹路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> deleteBlankDirectory(@NotNull File files) {
		return files.exists() && !files.isFile() ? Arrays.stream(Objects.requireNonNull(files.listFiles())).parallel().flatMap(file -> isBlankDirectory(file) && file.delete() ? Stream.of(file.getPath()) : deleteBlankDirectory(file).stream()).collect(Collectors.toList()) : new ArrayList<>();
	}

	/**
	 * 判断是否为空文件夹
	 *
	 * @param folderPath 需要判断的文件夹路径
	 * @return 判断结果
	 */
	@Contract(pure = true)
	public static boolean isBlankDirectory(@NotNull String folderPath) {
		return isBlankDirectory(new File(folderPath));
	}

	/**
	 * 判断是否为空文件夹
	 *
	 * @param folder 需要判断的文件夹
	 * @return 判断结果
	 */
	@Contract(pure = true)
	public static boolean isBlankDirectory(@NotNull File folder) {
		return folder.isDirectory() && Judge.isEmpty(folder.list());
	}

	/**
	 * 删除文件夹
	 *
	 * @param folderPath 文件夹路径
	 * @return 删除文件夹是否成功
	 */
	@Contract(pure = true)
	public static boolean deleteDirectory(@NotNull String folderPath) {
		return deleteDirectory(new File(folderPath));
	}

	/**
	 * 删除文件夹
	 *
	 * @param folder 文件夹
	 * @return 删除文件夹是否成功
	 */
	@Contract(pure = true)
	public static boolean deleteDirectory(@NotNull File folder) {
		return folder.exists() && !Arrays.stream(Objects.requireNonNull(folder.listFiles())).parallel().map(file -> file.isDirectory() ? deleteDirectory(file) : file.delete()).toList().contains(false) && folder.delete();
	}

	/**
	 * 删除文件夹内指定后缀文件
	 *
	 * @param folderPath 文件夹路径
	 * @param suffix     后缀
	 * @return 删除的文件路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> deleteSuffixFiles(@NotNull String folderPath, @NotNull String suffix) {
		return deleteSuffixFiles(new File(folderPath), suffix);
	}

	/**
	 * 删除文件夹内指定后缀文件
	 *
	 * @param files  文件夹对象
	 * @param suffix 后缀
	 * @return 删除的文件路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> deleteSuffixFiles(@NotNull File files, @NotNull String suffix) {
		return iterateFilesAsSuffix(files, suffix).parallelStream().filter(File::delete).map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 判断是否为指定后缀
	 *
	 * @param filePath 文件路径
	 * @param suffix   后缀
	 * @return boolean
	 */
	@Contract(pure = true)
	public static boolean isSuffixFile(@NotNull String filePath, @NotNull String suffix) {
		return filePath.endsWith((char) 46 + suffix);
	}

	/**
	 * 判断是否为指定后缀
	 *
	 * @param file   文件对象
	 * @param suffix 后缀
	 * @return boolean
	 */
	@Contract(pure = true)
	public static boolean isSuffixFile(@NotNull File file, @NotNull String suffix) {
		return file.isFile() && isSuffixFile(file.getName(), suffix);
	}

	/**
	 * 获取文件后缀
	 *
	 * @param fileName 文件名
	 * @return 文件后缀
	 */
	@Contract(pure = true)
	public static String getFileSuffix(@NotNull String fileName) {
		return fileName.contains(Symbol.DOT) ? fileName.substring(fileName.lastIndexOf(46) + 1) : null;
	}

	/**
	 * 获取文件后缀
	 *
	 * @param file 文件对象
	 * @return 文件后缀
	 */
	@Contract(pure = true)
	public static String getFileSuffix(@NotNull File file) {
		return getFileSuffix(file.getName());
	}

	/**
	 * 修改文件后缀
	 *
	 * @param file   文件对象
	 * @param suffix 后缀
	 * @return 修改后缀是否成功
	 */
	@Contract(pure = true)
	public static boolean afterFileSuffix(@NotNull File file, @NotNull String suffix) {
		String fileName = file.getName();
		File newfile;
		if (fileName.contains(Symbol.DOT)) {
			newfile = new File(file.getParent(), fileName.substring(0, fileName.lastIndexOf(46) + 1) + suffix);
		} else {
			newfile = new File(file.getParent(), fileName + (char) 46 + suffix);
		}
		return !newfile.exists() && file.renameTo(newfile);
	}

	/**
	 * 修改文件后缀
	 *
	 * @param filePath 文件路径
	 * @param suffix   后缀
	 * @return 修改后缀是否成功
	 */
	@Contract(pure = true)
	public static boolean afterFileSuffix(@NotNull String filePath, @NotNull String suffix) {
		return afterFileSuffix(new File(filePath), suffix);
	}

	/**
	 * 重命名文件
	 *
	 * @param file     文件对象
	 * @param fileName 文件名
	 * @return 重命名是否成功
	 */
	@Contract(pure = true)
	public static boolean afterFileName(@NotNull File file, @NotNull String fileName) {
		File newfile = new File(file.getParent(), fileName);
		return !newfile.exists() && file.renameTo(newfile);
	}

	/**
	 * 重命名文件
	 *
	 * @param filePath 文件路径
	 * @param fileName 新的文件名
	 * @return 重命名是否成功
	 */
	@Contract(pure = true)
	public static boolean afterFileName(@NotNull String filePath, @NotNull String fileName) {
		return afterFileName(new File(filePath), fileName);
	}

	/**
	 * 获取文件夹所有文件对象列表
	 *
	 * @param filePath 文件夹或文件路径
	 * @return 文件对象列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<File> iterateFiles(@NotNull String filePath) {
		return iterateFiles(new File(filePath));
	}

	/**
	 * 获取文件夹所有文件对象列表
	 *
	 * @param file 文件夹或文件对象
	 * @return 文件对象列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<File> iterateFiles(@NotNull File file) {
		return file.exists() ? file.isFile() ? Collections.singletonList(file) : Arrays.stream(Objects.requireNonNull(file.listFiles())).parallel().flatMap(f -> iterateFiles(f).stream()).collect(Collectors.toList()) : new ArrayList<>();
	}

	/**
	 * 获取文件夹所有指定后缀的文件路径列表
	 *
	 * @param filePath 文件夹或文件路径
	 * @param suffix   文件后缀名
	 * @return 文件对象列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<File> iterateFilesAsSuffix(@NotNull String filePath, @NotNull String suffix) {
		return iterateFilesAsSuffix(new File(filePath), suffix);
	}

	/**
	 * 获取文件夹所有指定后缀的文件路径列表
	 *
	 * @param file   文件夹或文件对象
	 * @param suffix 文件后缀名
	 * @return 文件对象列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<File> iterateFilesAsSuffix(@NotNull File file, @NotNull String suffix) {
		return iterateFiles(file).parallelStream().filter(f -> f.getName().endsWith((char) 46 + suffix)).collect(Collectors.toList());
	}

	/**
	 * 获取文件夹所有文件路径列表
	 *
	 * @param filePath 文件夹或文件路径
	 * @return 文件路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> iterateFilesPath(@NotNull String filePath) {
		return iterateFilesPath(new File(filePath));
	}

	/**
	 * 获取文件夹所有文件路径列表
	 *
	 * @param file 文件夹或文件对象
	 * @return 文件路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> iterateFilesPath(@NotNull File file) {
		return iterateFiles(file).parallelStream().map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 获取文件夹所有指定后缀的文件路径列表
	 *
	 * @param filePath 文件夹或文件路径
	 * @param suffix   文件后缀名
	 * @return 文件路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> iterateFilesPathAsSuffix(@NotNull String filePath, @NotNull String suffix) {
		return iterateFilesPathAsSuffix(new File(filePath), suffix);
	}

	/**
	 * 获取文件夹所有指定后缀的文件路径列表
	 *
	 * @param file   文件夹或文件对象
	 * @param suffix 文件后缀名
	 * @return 文件路径列表
	 */
	@NotNull
	@Contract(pure = true)
	public static List<String> iterateFilesPathAsSuffix(@NotNull File file, @NotNull String suffix) {
		return iterateFilesAsSuffix(file, suffix).parallelStream().map(File::getPath).collect(Collectors.toList());
	}

	/**
	 * 获取桌面对象
	 *
	 * @return 文件对象
	 */
	@Contract(pure = true)
	public static File getDesktop() {
		return FileSystemView.getFileSystemView().getHomeDirectory();
	}

	/**
	 * 获取桌面路径
	 *
	 * @return 路径
	 */
	@NotNull
	@Contract(pure = true)
	public static String getDesktopPath() {
		return getDesktop().toString();
	}

	/**
	 * 创建文件
	 *
	 * @param filePath 文件路径
	 */
	@Contract(pure = true)
	public static void createFile(@NotNull String filePath) {
		createFile(new File(filePath));
	}

	/**
	 * 创建文件
	 *
	 * @param file 文件
	 */
	@Contract(pure = true)
	public static void createFile(@NotNull File file) {
		if (!file.exists()) { // 文件不存在则创建文件，先创建目录
			createFolder(file.getParent());
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 创建文件夹
	 *
	 * @param folderPath 文件夹路径
	 */
	@Contract(pure = true)
	public static boolean createFolder(@NotNull String folderPath) {
		return createFolder(new File(folderPath));
	}

	/**
	 * 创建文件夹
	 *
	 * @param folder 文件夹对象
	 * @return 创建文件是否成功
	 */
	@Contract(pure = true)
	public static boolean createFolder(@NotNull File folder) {
		return !folder.exists() && folder.mkdirs();
	}

	/**
	 * 修改非法的Windows文件名
	 *
	 * @param fileName 文件名
	 * @return 正常的Windows文件名
	 */
	@NotNull
	@Contract(pure = true)
	public static String illegalFileName(@NotNull String fileName) {
		return fileName.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("_{2,}", "_");
	}

	/**
	 * 传入路径，返回是否是绝对路径，是绝对路径返回true，反之
	 *
	 * @param src 文件路径
	 * @return 判断结果
	 */
	@Contract(pure = true)
	public static boolean isAbsolutePath(@NotNull String src) {
		return src.startsWith(Symbol.SLASH) || src.charAt(1) == 58;
	}

	/**
	 * 传入路径，返回绝对路径
	 *
	 * @param src 文件路径
	 * @return 绝对路径
	 */
	@NotNull
	@Contract(pure = true)
	public static String getAbsolutePath(@NotNull String src) {
		return new File(src).getAbsolutePath();
	}

	/**
	 * 获取文件MD5值
	 *
	 * @param filePath 文件路径
	 * @return MD5值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getMD5(@NotNull String filePath) {
		return getMD5(new File(filePath));
	}

	/**
	 * 获取文件 MD5 值
	 *
	 * @param file 文件
	 * @return MD5 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getMD5(@NotNull File file) {
		String result = "";
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			result = DigestUtils.md5Hex(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取文件 SHA1 值
	 *
	 * @param filePath 文件路径
	 * @return SHA1 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA1(@NotNull String filePath) {
		return getSHA1(new File(filePath));
	}

	/**
	 * 获取文件 SHA1 值
	 *
	 * @param file 文件
	 * @return SHA1 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA1(@NotNull File file) {
		String result = "";
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			result = DigestUtils.sha1Hex(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取文件 SHA256 值
	 *
	 * @param filePath 文件路径
	 * @return SHA256 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA256(@NotNull String filePath) {
		return getSHA256(new File(filePath));
	}

	/**
	 * 获取文件 SHA256 值
	 *
	 * @param file 文件
	 * @return SHA256 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA256(@NotNull File file) {
		String result = "";
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			result = DigestUtils.sha256Hex(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取文件 SHA384 值
	 *
	 * @param filePath 文件路径
	 * @return SHA384 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA384(@NotNull String filePath) {
		return getSHA384(new File(filePath));
	}

	/**
	 * 获取文件 SHA384 值
	 *
	 * @param file 文件
	 * @return SHA384 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA384(@NotNull File file) {
		String result = "";
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			result = DigestUtils.sha384Hex(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取文件 SHA512 值
	 *
	 * @param filePath 文件路径
	 * @return SHA512 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA512(@NotNull String filePath) {
		return getSHA512(new File(filePath));
	}

	/**
	 * 获取文件 SHA512 值
	 *
	 * @param file 文件
	 * @return SHA512 值
	 */
	@NotNull
	@Contract(pure = true)
	public static String getSHA512(@NotNull File file) {
		String result = "";
		try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
			result = DigestUtils.sha512Hex(inputStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 复制文件
	 *
	 * @param input  来源文件
	 * @param output 输出路径
	 * @return 复制是否成功
	 */
	@Contract(pure = true)
	public static boolean copyFile(@NotNull File input, @NotNull File output) {
		return ReadWriteUtil.orgin(input).channelCopy(output);
	}

	/**
	 * 复制文件夹
	 *
	 * @param input  来源文件夹
	 * @param output 输出目录
	 * @return 复制是否成功
	 */
	@Contract(pure = true)
	public static boolean copyDirectory(@NotNull File input, @NotNull File output) {
		if (output.equals(input.getParentFile())) {
			return false;
		}
		for (File file : iterateFiles(input)) {
			String filePath = file.getAbsolutePath();
			File newFile = new File(output + filePath.substring(input.getAbsolutePath().length()));
			if (!copyFile(file, newFile)) {
				return false;
			}
		}
		return true;
	}

}
