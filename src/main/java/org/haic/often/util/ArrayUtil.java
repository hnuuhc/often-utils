package org.haic.often.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 静态数组工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/10/24 22:29
 */
public class ArrayUtil {

	/**
	 * int类型数组转换Integer类型动态数组
	 *
	 * @param nums int类型静态数组
	 * @return Integer类型动态数组
	 */
	public static List<Integer> intToInteger(int[] nums) {
		return Arrays.stream(nums).boxed().collect(Collectors.toList());
	}

	/**
	 * 合并数组
	 *
	 * @param a 第一个数组
	 * @param b 第二个数组
	 * @return 合并后的数组
	 */
	@SuppressWarnings({ "SuspiciousSystemArraycopy", "unchecked" })
	public static <T> T concatenate(T a, T b) {
		if (!a.getClass().isArray() || !b.getClass().isArray()) {
			throw new IllegalArgumentException();
		}
		Class<?> resCompType;
		var aCompType = a.getClass().getComponentType();
		var bCompType = b.getClass().getComponentType();
		if (aCompType.isAssignableFrom(bCompType)) {
			resCompType = aCompType;
		} else if (bCompType.isAssignableFrom(aCompType)) {
			resCompType = bCompType;
		} else {
			throw new IllegalArgumentException();
		}
		var aLen = Array.getLength(a);
		var bLen = Array.getLength(b);
		T result = (T) Array.newInstance(resCompType, aLen + bLen);
		System.arraycopy(a, 0, result, 0, aLen);
		System.arraycopy(b, 0, result, aLen, bLen);
		return result;
	}

}
