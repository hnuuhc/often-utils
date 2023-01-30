package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

/**
 * xml解析器
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:37
 */
public class Element extends XmlTree {

	protected Element(@NotNull String tag) {
		super(tag);
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

}
