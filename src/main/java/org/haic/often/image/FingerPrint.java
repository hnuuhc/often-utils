package org.haic.often.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.ToIntFunction;

/**
 * 均值哈希实现图像指纹比较
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/9/9 15:27
 */
public final class FingerPrint {

	/**
	 * 图像指纹的尺寸,将图像resize到指定的尺寸，来计算哈希数组
	 */
	private static final int HASH_SIZE = 16;

	/**
	 * 保存图像指纹的二值化矩阵
	 */
	private final byte[] binaryzationMatrix;

	private FingerPrint(byte[] hashValue) {
		if (hashValue.length != HASH_SIZE * HASH_SIZE) {
			throw new IllegalArgumentException(String.format("length of hashValue must be %d", HASH_SIZE * HASH_SIZE));
		}
		this.binaryzationMatrix = hashValue;
	}

	public FingerPrint(String hashValue) {
		this(toBytes(hashValue));
	}

	public FingerPrint(BufferedImage src) {
		this(hashValue(src));
	}

	/**
	 * 获取图片指纹
	 *
	 * @param imagePath 图片路径
	 * @return FingerPrint
	 */
	public static FingerPrint getFingerPrint(String imagePath) {
		return getFingerPrint(new File(imagePath));
	}

	/**
	 * 获取图片指纹
	 *
	 * @param file 文件
	 * @return FingerPrint
	 */
	public static FingerPrint getFingerPrint(File file) {
		FingerPrint FingerPrint = null;
		try {
			FingerPrint = new FingerPrint(ImageIO.read(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return FingerPrint;
	}

	/**
	 * 从压缩格式指纹创建{@link FingerPrint}对象
	 *
	 * @param compactValue 压缩格式bytes
	 * @return FingerPrint对象
	 */
	public static FingerPrint createCompact(byte[] compactValue) {
		return new FingerPrint(uncompact(compactValue));
	}

	/**
	 * 比较两个指纹的相识度
	 *
	 * @param frist  第一个指纹
	 * @param second 第二个指纹
	 * @return 相似度
	 */
	public static float compare(BufferedImage frist, BufferedImage second) {
		return new FingerPrint(frist).compare(new FingerPrint(second));
	}

	/**
	 * 判断两个数组相似度，数组长度必须一致否则抛出异常
	 *
	 * @param frist  byte数组
	 * @param second byte数组
	 * @return 返回相似度(0.0 ~ 1.0)
	 */
	private static float compare(byte[] frist, byte[] second) {
		if (frist.length != second.length) {
			throw new IllegalArgumentException("mismatch FingerPrint length");
		}
		int sameCount = 0;
		for (int i = 0; i < frist.length; i++) {
			if (frist[i] == second[i]) {
				sameCount++;
			}
		}
		return (float) sameCount / frist.length;
	}

	/**
	 * 比较两个压缩格式指纹的相识度
	 *
	 * @param frist  第一个指纹
	 * @param second 第二个指纹
	 * @return 相似度
	 */
	public static float compareCompact(byte[] frist, byte[] second) {
		return compare(uncompact(frist), uncompact(second));
	}

	/**
	 * 获取图片的哈希值
	 *
	 * @param src 图片对象
	 * @return 哈希值
	 */
	public static byte[] hashValue(BufferedImage src) {
		BufferedImage hashImage = ImageUtil.origin(src).resize(HASH_SIZE, HASH_SIZE);
		byte[] matrixGray = (byte[]) ImageUtil.origin(hashImage).toGray().getData().getDataElements(0, 0, HASH_SIZE, HASH_SIZE, null);
		return binaryzation(matrixGray);
	}

	/**
	 * 判断哈希值是否是有效的
	 *
	 * @param hashValue 哈希值
	 * @return 判断结果
	 */
	public static boolean isValidHashValue(byte[] hashValue) {
		if (hashValue.length != HASH_SIZE) return false;
		for (byte b : hashValue) {
			if (0 != b && 1 != b) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断哈希值是否是有效的
	 *
	 * @param hashValue 哈希值
	 * @return 判断结果
	 */
	public static boolean isValidHashValue(String hashValue) {
		if (hashValue.length() != HASH_SIZE) {
			return false;
		}
		for (int i = 0; i < hashValue.length(); i++) {
			if (hashValue.charAt(i) != '0' && hashValue.charAt(i) != '1') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 指纹数据按位压缩
	 *
	 * @param hashValue byte数组
	 * @return 压缩后的byte数组
	 */
	private static byte[] compact(byte[] hashValue) {
		byte[] result = new byte[(hashValue.length + 7) >> 3];
		byte b = 0;
		for (int i = 0; i < hashValue.length; i++) {
			if (0 == (i & 7)) {
				b = 0;
			}
			if (1 == hashValue[i]) {
				b |= 1 << (i & 7);
			} else if (hashValue[i] != 0) {
				throw new IllegalArgumentException("invalid hashValue,every element must be 0 or 1");
			}
			if (7 == (i & 7) || i == hashValue.length - 1) {
				result[i >> 3] = b;
			}
		}
		return result;
	}

	/**
	 * 压缩格式的指纹解压缩
	 *
	 * @param compactValue 压缩格式的byte数组
	 * @return 解压缩的byte数组
	 */
	private static byte[] uncompact(byte[] compactValue) {
		byte[] result = new byte[compactValue.length << 3];
		for (int i = 0; i < result.length; i++) {
			result[i] = (compactValue[i >> 3] & (1 << (i & 7))) == 0 ? 0 : (byte) 1;
		}
		return result;
	}

	/**
	 * 字符串类型的指纹数据转为字节数组
	 *
	 * @param hashValue 字符串类型的指纹数据
	 * @return 字节数组
	 */
	private static byte[] toBytes(String hashValue) {
		hashValue = hashValue.replaceAll("\\s", "");
		byte[] result = new byte[hashValue.length()];
		for (int i = 0; i < result.length; i++) {
			char c = hashValue.charAt(i);
			if ('0' == c) {
				result[i] = 0;
			} else if ('1' == c) {
				result[i] = 1;
			} else {
				throw new IllegalArgumentException("invalid hashValue String");
			}
		}
		return result;
	}

	/**
	 * 二值化处理
	 *
	 * @param src 链接
	 * @return 二值化字节数组
	 */
	private static byte[] binaryzation(byte[] src) {
		ToIntFunction<byte[]> average = l -> { // 计算均值
			long sum = 0;
			for (byte b : src) { // 将数组元素转为无符号整数
				sum += (long) b & 0xff;
			}
			return Math.round((float) sum / src.length);
		};
		byte[] dst = src.clone();
		int mean = average.applyAsInt(src);
		for (int i = 0; i < dst.length; i++) { // 将数组元素转为无符号整数再比较
			dst[i] = (byte) (((int) dst[i] & 0xff) >= mean ? 1 : 0);
		}
		return dst;
	}

	/**
	 * 比较指纹相似度
	 *
	 * @param src 链接
	 * @return 相似度
	 * @see #compare(byte[], byte[])
	 */
	public float compare(FingerPrint src) {
		if (src.binaryzationMatrix.length != this.binaryzationMatrix.length) {
			throw new IllegalArgumentException("length of hashValue is mismatch");
		}
		return compare(binaryzationMatrix, src.binaryzationMatrix);
	}

	/**
	 * 与指定的指纹比较相似度
	 *
	 * @param hashValue 字符串格式指纹数据
	 * @return 相似度
	 * @see #compare(FingerPrint)
	 */
	public float compare(String hashValue) {
		return compare(new FingerPrint(hashValue));
	}

	/**
	 * 与指定图像比较相似度
	 *
	 * @param image 图片
	 * @return 相似度
	 * @see #compare(FingerPrint)
	 */
	public float compare(BufferedImage image) {
		return compare(new FingerPrint(image));
	}

	/**
	 * 与指定的指纹比较相似度
	 *
	 * @param hashValue 指纹数据
	 * @return 相似度
	 * @see #compare(FingerPrint)
	 */
	public float compare(byte[] hashValue) {
		return compare(new FingerPrint(hashValue));
	}

	/**
	 * 与指定的压缩格式指纹比较相似度
	 *
	 * @param compactValue 压缩格式指纹数据
	 * @return 相似度
	 * @see #compare(FingerPrint)
	 */
	public float compareCompact(byte[] compactValue) {
		return compare(createCompact(compactValue));
	}

	/**
	 * 指纹数据按位压缩
	 *
	 * @return 压缩后的byte数组
	 */
	public byte[] compact() {
		return compact(binaryzationMatrix);
	}

	@Override
	public String toString() {
		return toString(true);
	}

	/**
	 * 获取当前对象的指纹
	 *
	 * @param multiLine 是否分行
	 * @return 字符串
	 */
	public String toString(boolean multiLine) {
		StringBuilder buffer = new StringBuilder();
		int count = 0;
		for (byte b : this.binaryzationMatrix) {
			buffer.append(0 == b ? '0' : '1');
			if (multiLine && ++count % HASH_SIZE == 0) {
				buffer.append('\n');
			}
		}
		return String.valueOf(buffer);
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof FingerPrint ? Arrays.equals(this.binaryzationMatrix, ((FingerPrint) obj).binaryzationMatrix) : super.equals(obj);
	}

}
