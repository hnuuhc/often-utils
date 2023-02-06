package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * XML树状结构
 *
 * @author haicdust
 * @version 1.0
 * @since 2023/1/22 17:36
 */
public class XmlTree extends Tag {

	private final XmlTree parent; // 父节点

	private final XmlChilds childs = new XmlChilds();

	public XmlTree(String tag) {
		this(null, tag);
	}

	public XmlTree(XmlTree parent, String tag) {
		super(tag);
		this.parent = parent;
	}

	public XmlTree(XmlTree parent, String tag, XmlChilds childs) {
		this(parent, tag);
		this.childs.addAll(childs);
	}

	/**
	 * 添加一个子节点
	 *
	 * @param child 子节点
	 * @return 当前节点
	 */
	public XmlTree addChild(@NotNull XmlTree child) {
		this.childs.add(child);
		return this;
	}

	/**
	 * 添加一个子节点
	 *
	 * @param child 子节点
	 * @return 当前节点
	 */
	public XmlTree addChild(@NotNull String child) {
		this.childs.add(child);
		return this;
	}

	/**
	 * 删除指定索引的子节点
	 *
	 * @param i 指定索引参数
	 * @return 当前节点
	 */
	public XmlTree removeChild(int i) {
		this.childs.remove(i);
		return this;
	}

	/**
	 * 删除满足条件的子节点
	 *
	 * @param filter 条件函数
	 * @return 当前节点
	 */
	public XmlTree removeIf(Predicate<Object> filter) {
		this.childs.removeIf(filter);
		return this;
	}

	/**
	 * 获取当前节点的父节点
	 *
	 * @return 当前节点的父节点
	 */
	public XmlTree parent() {
		return parent;
	}

	/**
	 * 获取当前节点的所有子节点
	 *
	 * @return 所有子节点
	 */
	public XmlChilds childs() {
		return childs;
	}

	/**
	 * 获取指定索引的子节点
	 *
	 * @param i 指定索引参数
	 * @return 子节点
	 */
	public Object child(int i) {
		return childs.get(i);
	}

	/**
	 * 获取当前标签下所有文本,以空格分割(警告: 文本本身也可能存在空格)
	 *
	 * @return 所有文本内容
	 */
	public String text() {
		return childs().stream().map(l -> l instanceof XmlTree e ? e.text() : l.toString()).filter(l -> !l.isEmpty()).collect(Collectors.joining(" "));
	}

	/**
	 * 添加属性
	 *
	 * @param key   属性名称
	 * @param value 属性值
	 * @return 当前标签
	 */
	public XmlTree attr(@NotNull String key, @NotNull String value) {
		super.attr(key, value);
		return this;
	}

	/**
	 * 为当前标签添加所有属性
	 *
	 * @param attrs 属性数组
	 * @return 当前标签
	 */
	public XmlTree addAttrs(@NotNull Map<String, String> attrs) {
		super.addAttrs(attrs);
		return this;
	}

	/**
	 * 删除当前标签的指定属性
	 *
	 * @param key 属性名称
	 * @return 当前标签
	 */
	public XmlTree removeAttr(@NotNull String key) {
		super.removeAttr(key);
		return this;
	}

	/**
	 * 设置当前节点是否为自闭合(警告: 如果设置为自闭合,在格式化时将不会输出子节点以及文本,查询不受影响)
	 *
	 * @param isClose true为是自闭合标签,反之同理
	 * @return 当前节点
	 */
	public XmlTree close(boolean isClose) {
		super.close(isClose);
		return this;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int depth) {
		var sb = new StringBuilder().append("    ".repeat(depth)).append(super.toString());
		switch (this.name()) {
			case "script", "style" -> {
				if (!childs.isEmpty()) sb.append("\n").append("    ".repeat(depth)).append(childs.get(0)).append("\n").append("    ".repeat(depth));
			}
			case "textarea", "noscript" -> {
				if (!childs.isEmpty()) {
					var text = (String) childs.get(0);
					sb.append("\n").append("    ".repeat(depth + 1));
					if (text.startsWith("\"")) sb.append(text);
					else sb.append('"').append(text).append('"');
					sb.append("\n").append("    ".repeat(depth));
				}
			}
			default -> {
				if (isClose()) return sb.toString();
				if (!childs.isEmpty()) {
					if (childs.size() == 1 && childs.get(0) instanceof String s) sb.append(s);
					else sb.append(childs.toString(depth + 1)).append("\n").append("    ".repeat(depth));
				}
			}
		}
		return sb.append("</").append(this.name()).append(">").toString();
	}

}
