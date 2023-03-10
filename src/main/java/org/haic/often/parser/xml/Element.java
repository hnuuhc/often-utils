package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * xml解析器
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:37
 */
public class Element extends XmlTree {

	public Element(@NotNull String name) {
		super(name);
	}

	public Element(Element parent, @NotNull String name) {
		super(parent, name);
	}

	protected Element(XmlTree parent, @NotNull ParserStringBuilder node) {
		super(parent, node);
	}

	/**
	 * 按照指定规则查询标签第一个,查询规则参照{@link #select(String)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	@Contract(pure = true)
	public Element selectFirst(@NotNull String cssQuery) {
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
	@Contract(pure = true)
	public Elements select(@NotNull String cssQuery) {
		return new XmlPath(this).select(cssQuery);
	}

	/**
	 * 按照指定条件筛选元素
	 *
	 * @param predicate 一个 非干扰的 、无状态 的谓词，应用于每个元素以确定是否应该包含它
	 * @return 筛选结果
	 */
	public Elements select(@NotNull Predicate<Element> predicate) {
		var result = new Elements();
		if (predicate.test(this)) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.select(predicate));
		return result;
	}

	/**
	 * 添加属性
	 *
	 * @param key   属性名称
	 * @param value 属性值
	 * @return 当前标签
	 */
	public Element attr(@NotNull String key, @NotNull String value) {
		super.attr(key, value);
		return this;
	}

	/**
	 * 为当前标签添加所有属性
	 *
	 * @param attrs 属性数组
	 * @return 当前标签
	 */
	public Element addAttrs(@NotNull Map<String, String> attrs) {
		super.addAttrs(attrs);
		return this;
	}

	/**
	 * 删除当前标签的指定属性
	 *
	 * @param key 属性名称
	 * @return 当前标签
	 */
	public Element removeAttr(@NotNull String key) {
		super.removeAttr(key);
		return this;
	}

	/**
	 * 设置当前节点是否为自闭合(警告: 如果设置为自闭合,在格式化时将不会输出子节点以及文本,查询不受影响)
	 *
	 * @param isClose true为是自闭合标签,反之同理
	 * @return 当前节点
	 */
	public Element close(boolean isClose) {
		super.close(isClose);
		return this;
	}

	/**
	 * 添加一个子节点
	 *
	 * @param child 子节点
	 * @return 当前节点
	 */
	public Element addChild(@NotNull XmlTree child) {
		super.addChild(child);
		return this;
	}

	/**
	 * 添加一个子节点
	 *
	 * @param child 子节点
	 * @return 当前节点
	 */
	public Element addChild(@NotNull String child) {
		super.addChild(child);
		return this;
	}

	/**
	 * 添加多个子节点
	 *
	 * @param childs 子节点数组
	 * @return 当前节点
	 */
	public Element addChilds(@NotNull XmlChilds childs) {
		super.addChilds(childs);
		return this;
	}

	/**
	 * 删除指定索引的子节点
	 *
	 * @param i 指定索引参数
	 * @return 当前节点
	 */
	public Element removeChild(int i) {
		super.removeChild(i);
		return this;
	}

	/**
	 * 删除满足条件的子节点
	 *
	 * @param filter 条件函数
	 * @return 当前节点
	 */
	public Element removeIf(Predicate<Object> filter) {
		super.removeIf(filter);
		return this;
	}

	/**
	 * 获取当前节点的父节点
	 *
	 * @return 当前节点的父节点
	 */
	public Element parent() {
		return (Element) super.parent();
	}

	/**
	 * 获取当前节点的所有子元素
	 *
	 * @return 所有子元素
	 */
	public Elements childElements() {
		return super.childs().stream().filter(e -> e instanceof XmlTree).map(e -> (Element) e).collect(Collectors.toCollection(Elements::new));
	}

}
