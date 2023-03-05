package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;

import java.util.ArrayList;
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
	@Contract(pure = true)
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
	@Contract(pure = true)
	public String text() {
		return this.stream().map(XmlTree::text).filter(e -> !e.isEmpty()).collect(Collectors.joining("\n"));
	}

	/**
	 * 按照指定规则查询标签第一个,查询规则参照{@link #select(String)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	@Contract(pure = true)
	public Element selectFirst(String cssQuery) {
		var result = select(cssQuery);
		return result.isEmpty() ? null : result.get(0);
	}

	/**
	 * 按照指定规则查询标签,支持使用空格分割,以确保更精确的查询
	 * <p>
	 * 例:
	 * <blockquote>
	 * <pre>    @head - 查询当前节点名称为head的子节点</pre>
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
	public Elements select(String cssQuery) {
		var result = new Elements(this);
		var querys = cssQuery.split(" ");
		for (int i = 0; i < querys.length; i++) {
			switch (querys[i].charAt(0)) {
				case '.' -> result = selectByAttr("class", querys[i].substring(1));
				case '#' -> result = selectByAttr("id", querys[i].substring(1));
				case '@' -> {
					var value = querys[i].substring(1);
					if (result.size() != 1) throw new IllegalStateException("在参数 " + querys[i] + " 查询对象不为Element类型");
					var e = result.get(0);
					result = e.childElements().stream().filter(l -> l.name().equals(value)).collect(Collectors.toCollection(Elements::new));
				}
				default -> {
					int index = querys[i].indexOf("[");
					if (index == -1) {
						result = querys[i].startsWith("!") ? result.selectByNoName(querys[i].substring(1)) : result.selectByName(querys[i]);
					} else {
						var attr = querys[i];
						var name = attr.substring(0, index);
						if (!attr.endsWith("]")) { // 属性值中存在空格,重新拼接
							do { // 可能存在多个空格
								//noinspection StringConcatenationInLoop
								attr += " " + querys[++i];
							} while (!attr.endsWith("]"));
						}
						attr = attr.substring(index + 1, attr.length() - 1);
						int indexAttr = attr.indexOf("=");
						if (indexAttr == -1) { // 不存在等号
							result = attr.startsWith("!") ? result.selectByNameAndNoAttr(name, attr.substring(1)) : result.selectByNameAndAttr(name, attr);
						} else {
							var key = attr.substring(0, indexAttr);
							var value = attr.charAt(attr.length() - 1) == '\'' ? attr.substring(indexAttr + 2, attr.length() - 1) : attr.substring(indexAttr + 1);
							result = key.endsWith("!") ? result.selectByNameAndNoAttr(name, key.substring(0, key.length() - 1), value) : result.selectByNameAndAttr(name, key, value);
						}
					}
				}
			}
		}
		return result;
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
		this.forEach(child -> result.addAll(child.selectById(id)));
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
		this.forEach(child -> result.addAll(child.selectByName(name)));
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
		return this.stream().filter(l -> !l.name().equals(name)).collect(Collectors.toCollection(Elements::new));
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
		this.forEach(child -> result.addAll(child.selectByNameAndAttr(name, key)));
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
		this.forEach(child -> result.addAll(child.selectByNameAndNoAttr(name, key)));
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
		this.forEach(child -> result.addAll(child.selectByNameAndAttr(name, key, value)));
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
		this.forEach(child -> result.addAll(child.selectByNameAndNoAttr(name, key, value)));
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
		this.forEach(child -> result.addAll(child.selectByAttr(key)));
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
		this.forEach(child -> result.addAll(child.selectByAttr(key, value)));
		return result;
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
		var sb = new StringBuilder();
		for (var child : this) sb.append(child.toString(0)).append("\n");
		return sb.toString();
	}

}
