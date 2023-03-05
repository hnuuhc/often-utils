package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
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
	 * <pre> 	@ - @head - 查询当前节点名称为head的子节点</pre>
	 * <pre> 	# - #stop - 查询属性名id值为stop的标签节点</pre>
	 * <pre> 	. - .stop - 查询属性名class值为stop的标签节点</pre>
	 * <pre>	! - !name - 查询标签名不匹配相等的标签节点</pre>
	 * <pre>	name - 查询标签名匹配相等的标签节点</pre>
	 * <pre>	name[key] - 查询标签名并且属性名都匹配相等的标签节点</pre>
	 * <pre>	name[key=value] - 查询标签名并且属性名和值都匹配相等的标签节点</pre>
	 * <pre>	name[key!=value] - 查询标签名并且属性名和值都匹配且不包含匹配属性值的标签节点</pre>
	 * <pre>	name[!key] - 查询标签名匹配且不包含匹配属性名标签节点</pre>
	 * <pre>	name[!class|!src] - 管道符'|'连接多个属性筛选</pre>
	 * </blockquote>
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements select(String cssQuery) {
		var es = new Elements(this);
		var querys = cssQuery.split(" ");
		for (int i = 0; i < querys.length; i++) {
			switch (querys[i].charAt(0)) {
				case '.' -> es = selectByAttr("class", querys[i].substring(1));
				case '#' -> es = selectByAttr("id", querys[i].substring(1));
				case '@' -> {
					var value = querys[i].substring(1);
					if (es.size() != 1) throw new IllegalStateException("在参数 " + querys[i] + " 查询对象不为Element类型");
					var e = es.get(0);
					es = e.childElements().stream().filter(l -> l.name().equals(value)).collect(Collectors.toCollection(Elements::new));
				}
				default -> {
					int index = querys[i].indexOf("[");
					if (index == -1) {
						es = querys[i].startsWith("!") ? es.selectByNoName(querys[i].substring(1)) : es.selectByName(querys[i]);
					} else {
						var attrs = querys[i];
						var name = attrs.substring(0, index);
						if (!attrs.endsWith("]")) { // 属性值中存在空格,重新拼接
							do { // 可能存在多个空格
								//noinspection StringConcatenationInLoop
								attrs += " " + querys[++i];
							} while (!attrs.endsWith("]"));
						}
						attrs = attrs.substring(index + 1, attrs.length() - 1);
						for (var attr : attrs.split("\\|")) {
							int indexAttr = attr.indexOf("=");
							if (indexAttr == -1) { // 不存在等号
								es = attr.startsWith("!") ? attr.length() == 1 ? es.selectByNameAndAttrs(name) : es.selectByNameAndNoAttr(name, attr.substring(1)) : es.selectByNameAndAttr(name, attr);
							} else {
								var key = attr.substring(0, indexAttr);
								var value = attr.charAt(attr.length() - 1) == '\'' ? attr.substring(indexAttr + 2, attr.length() - 1) : attr.substring(indexAttr + 1);
								es = key.endsWith("!") ? es.selectByNameAndNoAttr(name, key.substring(0, key.length() - 1), value) : es.selectByNameAndAttr(name, key, value);
							}
						}
					}
				}
			}
		}
		return es;
	}

	public Elements select(@NotNull Predicate<Element> predicate) {
		return this.stream().map(e -> e.select(predicate)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectById(id)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectByName(name)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
	 * 按照标签名称且不存在属性查询标签
	 *
	 * @param name 标签名称
	 * @return 查询结果
	 */
	@NotNull
	@Contract(pure = true)
	public Elements selectByNameAndAttrs(@NotNull String name) {
		return this.stream().map(e -> e.selectByNameAndAttrs(name)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectByNameAndAttr(name, key)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectByNameAndNoAttr(name, key)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectByNameAndAttr(name, key, value)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectByNameAndNoAttr(name, key, value)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectByAttr(key)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
		return this.stream().map(e -> e.selectByAttr(key, value)).flatMap(Elements::stream).collect(Collectors.toCollection(Elements::new));
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
