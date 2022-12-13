package org.haic.often.compress;

import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * Zip工具接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/12/13 19:51
 */
public abstract class Zip {

	/**
	 * 设置压缩包文件
	 *
	 * @param archive 压缩包文件路径
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip origin(@NotNull String archive);

	/**
	 * 设置压缩包文件
	 *
	 * @param archive 压缩包文件路径
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip origin(@NotNull File archive);

	/**
	 * 在解压压缩包时使用，解压至压缩包名称的文件夹
	 *
	 * @param archiveName 启用 解压使用压缩包名称文件夹
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip archiveName(boolean archiveName);

	/**
	 * 设置 压缩包密码
	 *
	 * @param passwd 压缩包密码
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip passwd(@NotNull String passwd);

	/**
	 * 压缩文件夹时，包含根目录
	 *
	 * @param includeRoot 启用 包含根目录
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip includeRoot(boolean includeRoot);

	/**
	 * 设置 压缩方式
	 *
	 * @param method 压缩方式
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip method(@NotNull CompressionMethod method);

	/**
	 * 设置 压缩级别
	 *
	 * @param level 压缩级别
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip level(@NotNull CompressionLevel level);

	/**
	 * 设置 字符集编码格式
	 *
	 * @param charset 集编码格式
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip charset(@NotNull Charset charset);

	/**
	 * 设置 字符集编码格式
	 *
	 * @param charsetName 集编码格式名称
	 * @return this
	 */
	@Contract(pure = true)
	public abstract Zip charset(@NotNull String charsetName);

	/**
	 * 解压压缩包到指定目录
	 *
	 * @param out 输出文件夹路径
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean deCompress(@NotNull String out);

	/**
	 * 解压压缩包到指定目录
	 *
	 * @param out 输出文件夹
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean deCompress(@NotNull File out);

	/**
	 * 压缩文件或文件夹到指定压缩包(如果文件存在则替换)
	 *
	 * @param origin 文件或文件夹路径
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean compress(@NotNull String origin);

	/**
	 * 压缩文件或文件夹到指定压缩包(如果文件存在则替换)
	 *
	 * @param origin 文件或文件夹
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean compress(@NotNull File origin);

	/**
	 * 添加流至压缩包
	 *
	 * @param inputStream 流
	 * @param entryName   文件名或路径
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean addStream(@NotNull InputStream inputStream, String entryName);

	/**
	 * 添加流至压缩包
	 *
	 * @param origin 集合 -> 文件名或路径 - 流
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean addStream(@NotNull Map<String, InputStream> origin);

	/**
	 * 删除压缩包中的文件,注意路径以 "/"分割
	 *
	 * @param origin 压缩包中的文件路径
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean remove(@NotNull String origin);

	/**
	 * 批量删除压缩包中的文件,注意路径以 "/"分割
	 *
	 * @param origin 压缩包中的文件路径列表
	 * @return 操作结果
	 */
	@Contract(pure = true)
	public abstract boolean remove(@NotNull List<String> origin);

}
