package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Element数组,用于存储多个标签节点
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Elements extends ArrayList<Element> {

	public Elements() {
		super();
	}

	public Elements(Elements el) {
		super(el);
	}

	/**
	 * 获取第一个标签节点
	 *
	 * @return 第一个标签节点
	 */
	public Element first() {
		return this.isEmpty() ? null : super.get(0);
	}

	/**
	 * 获取当前标签下所有文本,以空格分割(警告: 文本本身也可能存在空格)
	 * <p>
	 * 默认排除文本类标签 例: script,textarea,style
	 *
	 * @return 所有文本内容
	 */
	@NotNull
	public String text() {
		return this.stream().map(XmlTree::text).filter(e -> !e.isEmpty()).collect(Collectors.joining(" "));
	}

	/**
	 * 按照指定规则查询标签第一个,查询规则参照{@link #select(String)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	public Element selectFirst(String cssQuery) {
		var result = select(cssQuery);
		return result.isEmpty() ? null : result.get(0);
	}

	/**
	 * 按照指定规则查询标签,支持使用空格分割,以确保更精确的查询
	 * <p>
	 * 查询规则查看 {@link XmlPath#select(String)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	@NotNull
	public Elements select(String cssQuery) {
		return new XmlPath(this).select(cssQuery);
	}

	/**
	 * 按照指定条件筛选元素
	 *
	 * @param predicate 一个 非干扰的 、无状态 的谓词，应用于每个元素以确定是否应该包含它
	 * @return 筛选结果
	 */
	public Elements select(@NotNull Predicate<Element> predicate) {
		return this.stream().map(e -> e.select(predicate)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
	}

	/**
	 * 添加元素并返回自身
	 *
	 * @param e 待添加的元素
	 * @return 自身
	 */
	public Elements fluentAdd(@NotNull Element e) {
		super.add(e);
		return this;
	}

	/**
	 * 添加数组所有元素并返回自身
	 *
	 * @param e 数组
	 * @return 自身
	 */
	public Elements fluentAddAll(@NotNull Elements e) {
		super.addAll(e);
		return this;
	}

	@Override
	public String toString() {
		return this.stream().map(e -> e.toString(0)).collect(Collectors.joining("\n"));
	}

}
