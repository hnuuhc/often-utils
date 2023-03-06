package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

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
		for (var sb = new ParserStringBuilder(cssQuery); sb.stripLeading().pos() < sb.length(); sb.offset(1)) {
			switch (sb.charAt()) {
				case '.' -> {
					var value = sb.interceptOrEof(' ');
					es = es.select(e -> e.containsAttr("class") && value.equals(e.attr("class")));
				}
				case '#' -> {
					var value = sb.interceptOrEof(' ');
					es = es.select(e -> e.containsAttr("id") && value.equals(e.attr("id")));
				}
				case '@' -> {
					var value = sb.interceptOrEof(' ');
					if (es.size() != 1) throw new IllegalStateException("参数 " + value + " 错误,查询对象不为Element类型");
					es = es.get(0).childElements().stream().filter(l -> l.name().equals(value)).collect(Collectors.toCollection(Elements::new));
				}
				default -> {
					var css = new StringBuilder();
					var attrs = "";
					while (sb.isNoOutBounds()) {
						var c = sb.charAt();
						if (c == '[') {
							attrs = sb.intercept(']');
							break;
						}
						css.append(c);
						sb.offset(1);
					}
					if (css.charAt(0) == '!') {
						var name = css.substring(1);
						if (attrs.isEmpty()) {
							es = es.select(e -> !e.name().equals(name));
						} else {
							for (var attr : attrs.split("\\|")) {
								int indexAttr = attr.indexOf("=");
								if (indexAttr == -1) { // 不存在等号
									if (attr.startsWith("!")) {
										var thisAttr = attr.substring(1);
										es = thisAttr.isEmpty() ? es.select(e -> !e.name().equals(name) && e.attrIsEmpty()) : es.select(e -> !e.name().equals(name) && !e.containsAttr(thisAttr));
									} else {
										es = es.select(e -> !e.name().equals(name) && e.containsAttr(attr));
									}
								} else {
									var key = attr.substring(0, indexAttr);
									var value = attr.charAt(attr.length() - 1) == '\'' ? attr.substring(indexAttr + 2, attr.length() - 1) : attr.substring(indexAttr + 1);
									if (key.endsWith("!")) {
										var thisKey = key.substring(0, key.length() - 1);
										es = es.select(e -> !e.name().equals(name) && e.containsAttr(thisKey) && !value.equals(e.attr(thisKey)));
									} else {
										es = es.select(e -> !e.name().equals(name) && e.containsAttr(key) && value.equals(e.attr(key)));
									}
								}
							}
						}
					} else {
						var name = css.toString();
						if (attrs.isEmpty()) {
							es = es.select(e -> e.name().equals(name));
						} else {
							for (var attr : attrs.split("\\|")) {
								int indexAttr = attr.indexOf("=");
								if (indexAttr == -1) { // 不存在等号
									if (attr.startsWith("!")) {
										var thisAttr = attr.substring(1);
										es = thisAttr.isEmpty() ? es.select(e -> e.name().equals(name) && e.attrIsEmpty()) : es.select(e -> e.name().equals(name) && !e.containsAttr(thisAttr));
									} else {
										es = es.select(e -> e.name().equals(name) && e.containsAttr(attr));
									}
								} else {
									var key = attr.substring(0, indexAttr);
									var value = attr.charAt(attr.length() - 1) == '\'' ? attr.substring(indexAttr + 2, attr.length() - 1) : attr.substring(indexAttr + 1);
									if (key.endsWith("!")) {
										var thisKey = key.substring(0, key.length() - 1);
										es = es.select(e -> e.name().equals(name) && e.containsAttr(thisKey) && !value.equals(e.attr(thisKey)));
									} else {
										es = es.select(e -> e.name().equals(name) && e.containsAttr(key) && value.equals(e.attr(key)));
									}
								}
							}
						}
					}
				}
			}
		}
		return es;
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
