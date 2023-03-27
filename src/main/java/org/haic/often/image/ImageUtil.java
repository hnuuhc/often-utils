package org.haic.often.image;

import org.haic.often.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.DoubleFunction;

/**
 * 图片工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/9/8 21:35
 */
public class ImageUtil {

	private BufferedImage src;
	private int imageType = BufferedImage.TYPE_3BYTE_BGR;

	private ImageUtil() {
	}

	/**
	 * 创建图片的工具对象
	 *
	 * @param src 图片路径
	 * @return new ImageUtil
	 */
	public static ImageUtil origin(@NotNull String src) {
		return origin(new File(src));
	}

	/**
	 * 创建图片的工具对象
	 *
	 * @param file 图片文件对象
	 * @return new ImageUtil
	 */
	public static ImageUtil origin(@NotNull File file) {
		BufferedImage image;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return origin(image);
	}

	/**
	 * 创建图片的工具对象
	 *
	 * @param image 图片文件对象
	 * @return new ImageUtil
	 */
	public static ImageUtil origin(@NotNull BufferedImage image) {
		return new ImageUtil().image(image);
	}

	/**
	 * 将图片对象写入文件
	 *
	 * @param image 图片对象
	 * @param out   输出文件路径
	 * @return 写入状态
	 */
	public static boolean write(@NotNull BufferedImage image, @NotNull String out) {
		return write(image, new File(out));
	}

	/**
	 * 将图片对象写入文件
	 *
	 * @param image 图片对象
	 * @param out   输出文件
	 * @return 写入状态
	 */
	public static boolean write(@NotNull BufferedImage image, @NotNull File out) {
		try {
			String fileName = out.getName();
			ImageIO.write(image, fileName.substring(fileName.lastIndexOf(".") + 1), out);
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private ImageUtil image(@NotNull BufferedImage image) {
		this.src = image;
		int imageType = image.getType();
		this.imageType = imageType == 0 ? this.imageType : imageType;
		return this;
	}

	/**
	 * 转灰度图像
	 *
	 * @return 图片对象
	 */
	public BufferedImage toGray() {
		if (imageType == BufferedImage.TYPE_BYTE_GRAY) {
			return src;
		} else { // 图像转灰
			BufferedImage grayImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(src, grayImage);
			return grayImage;
		}
	}

	/**
	 * 放大图像至指定倍数
	 *
	 * @param multiple 放大倍数
	 * @return 图片对象
	 */
	public BufferedImage enlarge(int multiple) {
		return resize(src.getWidth() * multiple, src.getHeight() * multiple);
	}

	/**
	 * 缩小图像至指定倍数
	 *
	 * @param multiple 缩小倍数
	 * @return 图片对象
	 */
	public BufferedImage reduce(int multiple) {
		return resize(src.getWidth() / multiple, src.getHeight() / multiple);
	}

	/**
	 * 缩放图像到指定尺寸
	 *
	 * @param width  宽
	 * @param height 高
	 * @return 图片对象
	 */
	public BufferedImage resize(int width, int height) {
		BufferedImage resultImage = new BufferedImage(width, height, imageType);
		Graphics graphics = resultImage.createGraphics();
		graphics.drawImage(src.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH), 0, 0, null);
		graphics.dispose();
		return resultImage;
	}

	/**
	 * 对图形进行裁剪
	 *
	 * @param x      横向起始位置
	 * @param y      竖向起始位置
	 * @param width  裁剪的宽度
	 * @param height 裁剪的高度
	 * @return 裁剪后的图像
	 */
	public BufferedImage cutOff(int x, int y, int width, int height) {
		return src.getSubimage(x, y, width, height);
	}

	/**
	 * 对图形进行圆形裁剪
	 *
	 * @param x      横向起始位置
	 * @param y      竖向起始位置
	 * @param width  裁剪的宽度
	 * @param height 裁剪的高度
	 * @return 裁剪后的图像
	 */
	public BufferedImage cutOffRound(int x, int y, int width, int height) {
		BufferedImage resultImage = new BufferedImage(width, height, imageType);
		Ellipse2D.Double shape = new Ellipse2D.Double(x, y, width, height);
		Graphics2D graphics = resultImage.createGraphics();
		graphics.setClip(shape);
		graphics.drawImage(src, 0, 0, null);
		graphics.dispose();
		return resultImage;
	}

	/**
	 * 向右旋转图像
	 *
	 * @param theta 任意角度
	 * @return 旋转后的图像
	 */
	public BufferedImage rotate(double theta) {
		int width = src.getWidth();
		int height = src.getHeight();
		int centerX = width / 2;
		int centerY = height / 2;
		double radius = Math.sqrt(centerX * centerX + centerY * centerY);
		double angle = theta * Math.PI / 180; // 度转弧度
		DoubleFunction<Double> getX = angleX -> {
			double[] results = new double[4];
			results[0] = radius * Math.cos(angleX + angle);
			results[1] = radius * Math.cos(Math.PI - angleX + angle);
			results[2] = -results[0];
			results[3] = -results[1];
			Arrays.sort(results);
			return results[3] - results[0];
		};
		DoubleFunction<Double> getY = angleY -> {
			double[] results = new double[4];
			results[0] = radius * Math.sin(angleY + angle);
			results[1] = radius * Math.sin(Math.PI - angleY + angle);
			results[2] = -results[0];
			results[3] = -results[1];
			Arrays.sort(results);
			return results[3] - results[0];
		};
		int resultWidth = getX.apply(Math.acos(centerX / radius)).intValue();
		int resultHeight = getY.apply(Math.asin(centerY / radius)).intValue();
		BufferedImage resultImage = new BufferedImage(resultWidth, resultHeight, imageType);
		Graphics2D graphics = resultImage.createGraphics();
		graphics.rotate(Math.toRadians(theta), (double) resultWidth / 2, (double) resultHeight / 2);
		graphics.drawImage(src, (resultWidth - width) / 2, (resultHeight - height) / 2, null);
		graphics.dispose();
		return resultImage;

	}

	/**
	 * 获取图像的水平镜像
	 *
	 * @return 水平镜像
	 */
	public BufferedImage mirror() {
		int width = src.getWidth();
		int height = src.getHeight();
		BufferedImage resultImage = new BufferedImage(width, height, imageType);
		Graphics graphics = resultImage.createGraphics();
		graphics.drawImage(src, 0, 0, width, height, width, 0, 0, height, null);
		graphics.dispose();
		return resultImage;
	}

}
