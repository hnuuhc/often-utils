package org.haic.often;

/**
 * 判断是否为空
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:40
 */
public class Judge {

	/**
	 * 字符串 String
	 *
	 * @param str 字符串
	 * @return 判断结果
	 */
	public static boolean isEmpty(String str) {
		return str == null || str.equals("");
	}

	/**
	 * 判断char array数组是否为空
	 *
	 * @param c char array数组
	 * @return 判断结果
	 */
	public static boolean isEmpty(char... c) {
		return c == null || c.length == 0;
	}

	/**
	 * 判断泛型array数组是否为空
	 *
	 * @param T   泛型array数组
	 * @param <T> 泛型
	 * @return 判断结果
	 */
	public static <T> boolean isEmpty(T[] T) {
		return T == null || T.length == 0;
	}

}
