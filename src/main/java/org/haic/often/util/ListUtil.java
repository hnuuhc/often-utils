package org.haic.often.util;

import org.haic.often.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * List数组工具类
 *
 * @author haicdust
 * @version 1.0
 * @since 2020/2/18 17:20
 */
public class ListUtil {

	/**
	 * 去重无排序,按照输入顺序返回
	 *
	 * @param list 动态数组
	 * @return 无排序的数组
	 */
	public static <E> List<E> linkedHashSet(@NotNull Collection<E> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	/**
	 * 去重排序
	 *
	 * @param list 动态数组
	 * @return 排序后的数组
	 */
	public static <E> List<E> treeSet(@NotNull Collection<E> list) {
		return new ArrayList<>(new TreeSet<>(list));
	}

	/**
	 * 去重排序
	 *
	 * @param list 字符串数组
	 * @return 排序后的数组
	 */
	public static <E> List<E> hashSet(@NotNull Collection<E> list) {
		return new ArrayList<>(new HashSet<>(list));
	}

	/**
	 * 去重无排序，隐式处理
	 *
	 * @param list 动态数组
	 * @param <E>  泛型
	 * @return 无排序的数组
	 */
	public static <E> List<E> streamSet(@NotNull Collection<E> list) {
		return streamSet(list.stream());
	}

	/**
	 * 去重无排序，隐式处理
	 *
	 * @param list 动态数组流
	 * @return 无排序的数组
	 */
	public static <E> List<E> streamSet(@NotNull Stream<E> list) {
		return list.distinct().collect(Collectors.toList());
	}

	/**
	 * 合并多个数组
	 *
	 * @param list 数组
	 * @return 数组
	 */
	@SafeVarargs
	public static <E> List<E> merge(Collection<E>... list) {
		return Arrays.stream(list).flatMap(Collection::stream).collect(Collectors.toList());
	}

	/**
	 * 合并多个数组并去重
	 *
	 * @param list 数组
	 * @return 无重复的数组
	 */
	@SafeVarargs
	public static <T> List<T> mergeSet(Collection<T>... list) {
		return Arrays.stream(list).flatMap(Collection::stream).distinct().collect(Collectors.toList());
	}

	/**
	 * 流排序，隐式处理
	 *
	 * @param list 动态数组
	 * @return 无排序的数组
	 */
	public static <E> List<E> sort(@NotNull Collection<E> list) {
		return list.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * 流排序，隐式处理
	 *
	 * @param list       动态数组
	 * @param comparator 排序参数
	 * @return 无排序的数组
	 */
	public static <E> List<E> sort(@NotNull Collection<E> list, @NotNull Comparator<E> comparator) {
		return list.stream().sorted(comparator).collect(Collectors.toList());
	}

}
