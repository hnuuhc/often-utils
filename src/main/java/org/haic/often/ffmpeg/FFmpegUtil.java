package org.haic.often.ffmpeg;

import org.haic.often.Terminal;
import org.haic.often.annotations.NotNull;
import org.haic.often.util.FileUtil;
import org.haic.often.util.ReadWriteUtil;

import java.io.File;
import java.util.List;

/**
 * FFmpeg 工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/15 11:08
 */
public class FFmpegUtil {

	/**
	 * 指定FFmpeg可执行文件路径构建FFmpeg类
	 *
	 * @param ffmpeg FFmpeg可执行文件路径
	 * @return FFmpeg
	 */
	public static FFmpeg ffmpeg(@NotNull String ffmpeg) {
		return new FFmpegBuilder(ffmpeg);
	}

	/**
	 * 使用系统环境下FFmpeg文件路径,如果文件不存在则抛出执行异常
	 *
	 * @return FFmpeg
	 */
	public static FFmpeg ffmpeg() {
		if (Terminal.command("ffmpeg", "-version").execute() != 0) {
			throw new RuntimeException("系统ffmpeg可执行文件不存在");
		}
		return new FFmpegBuilder("ffmpeg");
	}

	private static class FFmpegBuilder extends FFmpeg {

		private final String ffmpeg;

		private FFmpegBuilder(@NotNull String ffmpeg) {
			this.ffmpeg = ffmpeg;
		}

		public Video video(@NotNull String video) {
			return new VideoBuilder(ffmpeg, video);
		}

		public Audio audio(@NotNull String audio) {
			return new AudioBuilder(ffmpeg, audio);
		}

	}

	private static class VideoBuilder extends Video {

		private final String ffmpeg;
		private final String video;

		private VideoBuilder(@NotNull String ffmpeg, @NotNull String video) {
			this.ffmpeg = ffmpeg;
			this.video = video;
		}

		public boolean setAudio(@NotNull String audio, @NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", Terminal.doubleQuote(video), "-i", Terminal.doubleQuote(audio), "-c", "copy", Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean addAudio(@NotNull String audio, @NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", Terminal.doubleQuote(video), "-i", Terminal.doubleQuote(audio), "-c", "copy", "-map", "0", "-map", "1", Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean removeAudio(@NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", Terminal.doubleQuote(video), "-an", Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean capture(@NotNull String start, int time, @NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", Terminal.doubleQuote(video), "-t", String.valueOf(time), "-ss", start, Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean speed(float multiple, @NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", Terminal.doubleQuote(video), "-filter:v", "setpts=PTS/" + multiple, Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean extractAudio(@NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", Terminal.doubleQuote(video), "-f", out.substring(out.lastIndexOf(".") + 1), Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean addVideo(@NotNull String video, @NotNull String out) {
			return addVideo(List.of(video), out);
		}

		public boolean addVideo(@NotNull List<String> video, @NotNull String out) {
			FileUtil.deteleFile(out);
			File fileList = new File(out + ".txt");
			ReadWriteUtil.orgin(fileList).append(false).write("file " + Terminal.quote(this.video) + "\n" + String.join("\n", video.stream().map(l -> "file " + Terminal.quote(l)).toList()));
			return Terminal.command(ffmpeg, "-f", "concat", "-safe", "0", "-i", fileList.getPath(), "-c", "copy", Terminal.doubleQuote(out)).execute() == 0 && fileList.delete();
		}

	}

	private static class AudioBuilder extends Audio {

		private final String ffmpeg;
		private final String audio;

		private AudioBuilder(@NotNull String ffmpeg, @NotNull String audio) {
			this.ffmpeg = ffmpeg;
			this.audio = audio;
		}

		public boolean merge(@NotNull String audio, @NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", this.audio, "-i", audio, "-f", out.substring(out.lastIndexOf(".") + 1), Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean addAudio(@NotNull String audio, @NotNull String out) {
			return addAudio(List.of(audio), out);
		}

		public boolean addAudio(@NotNull List<String> audio, @NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", Terminal.doubleQuote("concat:" + this.audio + "|" + String.join("|", audio)), "-acodec", "copy", Terminal.doubleQuote(out)).execute() == 0;
		}

		public boolean transcoding(@NotNull String out) {
			FileUtil.deteleFile(out);
			return Terminal.command(ffmpeg, "-i", audio, "-acodec", "libmp3lame", Terminal.doubleQuote(out)).execute() == 0;
		}

	}

}
