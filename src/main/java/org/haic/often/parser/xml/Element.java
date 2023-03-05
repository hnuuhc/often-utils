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
	 * 查询规则查看 {@link Elements#select(String)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements select(@NotNull String cssQuery) {
		return new Elements().fluentAdd(this).select(cssQuery);
	}

	/**
	 * 按照ID查询标签
	 *
	 * @param id id值
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectById(@NotNull String id) {
		var result = new Elements();
		if (id.equals(attr("id"))) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectById(id));
		return result;
	}

	/**
	 * 按照标签名称查询标签
	 *
	 * @param name 标签名称
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByName(@NotNull String name) {
		var result = new Elements();
		if (name().equals(name)) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByName(name));
		return result;
	}

	/**
	 * 排除标签名称查询标签
	 *
	 * @param name 标签名称
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByNoName(@NotNull String name) {
		return this.childs().stream().filter(l -> l instanceof Element e && !e.name().equals(name)).map(l -> (Element) l).collect(Collectors.toCollection(Elements::new));
	}

	/**
	 * 按照标签名称且不存在属性查询标签
	 *
	 * @param name 标签名称
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByNameAndAttrs(@NotNull String name) {
		var result = new Elements();
		if (name().equals(name) && attrIsEmpty()) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByNameAndAttrs(name));
		return result;
	}

	/**
	 * 按照标签名称和属性名查询标签
	 *
	 * @param name 标签名称
	 * @param key  属性名
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByNameAndAttr(@NotNull String name, @NotNull String key) {
		var result = new Elements();
		if (name().equals(name) && containsAttr(key)) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByNameAndAttr(name, key));
		return result;
	}

	/**
	 * 按照标签名称和排除属性名查询标签
	 *
	 * @param name 标签名称
	 * @param key  属性名
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByNameAndNoAttr(@NotNull String name, @NotNull String key) {
		var result = new Elements();
		if (name().equals(name) && !containsAttr(key)) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByNameAndNoAttr(name, key));
		return result;
	}

	/**
	 * 按照标签名称和属性值查询标签
	 *
	 * @param name  标签名称
	 * @param key   属性名
	 * @param value 属性值
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByNameAndAttr(@NotNull String name, @NotNull String key, @NotNull String value) {
		var result = new Elements();
		if (name().equals(name) && containsAttr(key) && value.equals(attr(key))) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByNameAndAttr(name, key, value));
		return result;
	}

	/**
	 * 按照标签名称和排除属性值查询标签
	 *
	 * @param name  标签名称
	 * @param key   属性名
	 * @param value 属性值
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByNameAndNoAttr(@NotNull String name, @NotNull String key, @NotNull String value) {
		var result = new Elements();
		if (name().equals(name) && containsAttr(key) && !value.equals(attr(key))) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByNameAndNoAttr(name, key, value));
		return result;
	}

	/**
	 * 按照属性名称查询标签
	 *
	 * @param key 属性名称
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByAttr(@NotNull String key) {
		var result = new Elements();
		if (containsAttr(key)) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByAttr(key));
		return result;
	}

	/**
	 * 按照属性名称和属性值查询标签
	 *
	 * @param key   属性名称
	 * @param value 属性值
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByAttr(@NotNull String key, @NotNull String value) {
		var result = new Elements();
		if (containsAttr(key) && value.equals(attr(key))) return result.fluentAdd(this);
		for (var child : childs()) if (child instanceof Element e) result.addAll(e.selectByAttr(key, value));
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
	 * 获取当前节点的属性列表状态
	 *
	 * @return 当前节点的属性是否为空
	 */
	public boolean attrIsEmpty() {
		return attrs().isEmpty();
	}

	/**
	 * 获取当前节点的子元素列表状态
	 *
	 * @return 当前节点的子元素是否为空
	 */
	public boolean isEmpty() {
		return childs().isEmpty();
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
