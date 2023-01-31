package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

import java.util.Map;

/**
 * xml解析器
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:37
 */
public class Element extends XmlTree {

	public Element(@NotNull String tag) {
		super(tag);
	}

	public Element(@NotNull XmlTree e, @NotNull String tag) {
		super(e, tag);
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
	 * 例:
	 * <blockquote>
	 * <pre>    #stop - 查询属性名id值为stop的标签节点</pre>
	 * <pre>    .stop - 查询属性名class值为stop的标签节点</pre>
	 * <pre>    a[class=stop] - 查询标签名为a属性名class值为stop的标签节点</pre>
	 * </blockquote>
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
		if (id.equals(attr("id"))) {
			result.add(this);
			return result;
		}
		for (var child : childs()) {
			if (child instanceof Element e) result.addAll(e.selectById(id));
		}
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
		if (name().equals(name)) {
			result.add(this);
			return result;
		}
		for (var child : childs()) {
			if (child instanceof Element e) result.addAll(e.selectByName(name));
		}
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
	public Elements selectByNameAndAttrKey(@NotNull String name, @NotNull String key) {
		var result = new Elements();
		if (name().equals(name) && containsAttr(key)) {
			result.add(this);
			return result;
		}
		for (var child : childs()) {
			if (child instanceof Element e) result.addAll(e.selectByNameAndAttrKey(name, key));
		}
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
		if (name().equals(name) && containsAttr(key) && value.equals(attr(key))) {
			result.add(this);
			return result;
		}
		for (var child : childs()) {
			if (child instanceof Element e) result.addAll(e.selectByNameAndAttr(name, key, value));
		}
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
		if (containsAttr(key)) {
			result.add(this);
			return result;
		}
		for (var child : childs()) {
			if (child instanceof Element e) result.addAll(e.selectByAttr(key));
		}
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
		if (containsAttr(key) && value.equals(attr(key))) {
			result.add(this);
			return result;
		}
		for (var child : childs()) {
			if (child instanceof Element e) result.addAll(e.selectByAttr(key, value));
		}
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
	 * 获取当前节点的父节点
	 *
	 * @return 当前节点的父节点
	 */
	public Element parent() {
		return (Element) super.parent();
	}

}
