package org.haic.often.ffmpeg;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * FFmpeg接口
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/15 11:35
 */
public abstract class FFmpeg {

	/**
	 * 获取视频工具类
	 *
	 * @param video 视频文件路径
	 * @return 视频工具类
	 */
	@Contract(pure = true)
	public abstract Video video(@NotNull String video);

	/**
	 * 获取音频工具类
	 *
	 * @param audio 音频文件路径
	 * @return 音频工具类
	 */
	@Contract(pure = true)
	public abstract Audio audio(@NotNull String audio);

}
