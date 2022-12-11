package org.haic.often.ffmpeg;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

import java.util.List;

/**
 * 音频操作接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/15 12:39
 */
public abstract class Audio {

	/**
	 * 合并音频(混音)
	 *
	 * @param audio 音频文件路径
	 * @param out   输出文件路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean merge(@NotNull String audio, @NotNull String out);

	/**
	 * 拼接音频,注意音频格式需要一致
	 *
	 * @param audio 音频文件路径
	 * @param out   输出文件路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean addAudio(@NotNull String audio, @NotNull String out);

	/**
	 * 拼接音频,注意音频格式需要一致
	 *
	 * @param audio 音频文件路径列表
	 * @param out   输出文件路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean addAudio(@NotNull List<String> audio, @NotNull String out);

	/**
	 * 音频转码
	 *
	 * @param out 输出文件路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean transcoding(@NotNull String out);

}
