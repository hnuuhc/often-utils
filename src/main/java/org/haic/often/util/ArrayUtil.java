package org.haic.often.util;

import org.jetbrains.annotations.Contract;

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
	@Contract(pure = true)
	public static List<Integer> intToInteger(int[] nums) {
		return Arrays.stream(nums).boxed().collect(Collectors.toList());
	}

}
