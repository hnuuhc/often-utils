package org.haic.often.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.FileOutputStream;

/**
 * 视频工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/26 13:02
 */
public class MovieUtil {

	/**
	 * 合并音频和视频文件
	 *
	 * @param audio 音频文件路径
	 * @param video 视频文件路径
	 * @param out   输出路径
	 * @return 操作是否成功
	 */
	@Contract(pure = true)
	public static boolean audioVideoMerge(@NotNull String audio, @NotNull String video, @NotNull String out) {
		boolean success = false;
		try (FileOutputStream fos = new FileOutputStream(out)) {
			Movie movie = MovieCreator.build(audio);
			MovieCreator.build(video).getTracks().forEach(movie::addTrack);
			new FragmentedMp4Builder().build(movie).writeContainer(fos.getChannel());
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.gc(); // 必须手动释放资源
		return success;
	}

}
