package org.haic.often.ffmpeg;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 视频操作接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/15 12:41
 */
public abstract class Video {

	/**
	 * 为视频设置音频
	 *
	 * @param audio 音频文件路径
	 * @param out   文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean setAudio(@NotNull String audio, @NotNull String out);

	/**
	 * 为视频添加一个新的音频轨道
	 *
	 * @param audio 音频文件路径
	 * @param out   文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean addAudio(@NotNull String audio, @NotNull String out);

	/**
	 * 删除所有音频(静音)
	 *
	 * @param out 文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean removeAudio(@NotNull String out);

	/**
	 * 视频截取
	 *
	 * @param start 开始时间(例: 00:00:10)
	 * @param time  截取时间(秒)
	 * @param out   文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean capture(@NotNull String start, int time, @NotNull String out);

	/**
	 * 倍速播放
	 *
	 * @param multiple 倍数
	 * @param out      文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean speed(float multiple, @NotNull String out);

	/**
	 * 提取音频
	 *
	 * @param out 文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean extractAudio(@NotNull String out);

	/**
	 * 拼接视频
	 *
	 * @param video 视频文件路径
	 * @param out   文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean addVideo(@NotNull String video, @NotNull String out);

	/**
	 * 拼接视频
	 *
	 * @param video 视频文件列表
	 * @param out   文件输出路径
	 * @return 执行结果
	 */
	@Contract(pure = true)
	public abstract boolean addVideo(@NotNull List<String> video, @NotNull String out);

}
