package org.haic.often.parser.csv;

import org.jetbrains.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;
import org.haic.often.util.TypeReference;
import org.haic.often.util.TypeUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * CSV解析工具
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/3/17 14:40
 */
public class CSV extends ArrayList<CSVNode> {

	public CSV() {
		super();
	}

	public CSV(@NotNull String body) {
		var node = new ParserStringBuilder(body).strip();
		while (node.isNoOutBounds()) {
			this.add(new CSVNode(node));
			node.offset(1);
		}
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param i      要返回的元素的索引
	 * @param mapper 函数式接口,用于指定转换类型
	 * @param <T>    返回泛型
	 * @return 值
	 */
	public <T> List<T> get(int i, @NotNull Function<Object, ? extends T> mapper) {
		return super.get(i).stream().map(mapper).collect(Collectors.toList());
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param i         要返回的元素的索引
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 值
	 */
	public <T> List<T> get(int i, @NotNull Class<T> itemClass) {
		return TypeUtil.convertList(super.get(i), itemClass);
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param y      要返回的元素的索引(行)
	 * @param x      要返回的元素的索引(列)
	 * @param mapper 函数式接口,用于指定转换类型
	 * @param <T>    返回泛型
	 * @return 值
	 */
	public <T> T get(@NotNull int y, int x, @NotNull Function<Object, ? extends T> mapper) {
		return this.get(y).get(x, mapper);
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param y    要返回的元素的索引(行)
	 * @param x    要返回的元素的索引(列)
	 * @param type TypeReference接口类型
	 * @param <T>  返回泛型
	 * @return 值
	 */
	public <T> T get(int y, int x, @NotNull TypeReference<T> type) {
		return this.get(y).get(x, type);
	}

	/**
	 * 获取索引位置元素
	 *
	 * @param y         要返回的元素的索引(行)
	 * @param x         要返回的元素的索引(列)
	 * @param itemClass 指定类型
	 * @param <T>       返回泛型
	 * @return 值
	 */
	public <T> T get(int y, int x, @NotNull Class<T> itemClass) {
		return this.get(y).get(x, itemClass);
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param obj 待添加的元素
	 * @return 自身
	 */
	public CSV fluentAdd(CSVNode obj) {
		super.add(obj);
		return this;
	}

	/**
	 * 添加数组所有元素并返回自身
	 *
	 * @param c 数组
	 * @return 自身
	 */
	public CSV fluentAddAll(Collection<CSVNode> c) {
		super.addAll(c);
		return this;
	}

	@Override
	public String toString() {
		return this.stream().map(CSVNode::toString).collect(Collectors.joining("\n"));
	}

}
