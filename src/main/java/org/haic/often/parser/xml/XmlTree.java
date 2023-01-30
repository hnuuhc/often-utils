package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;

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

	public XmlTree(XmlTree pid, String tag, XmlChilds childs) {
		this(pid, tag);
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

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int depth) {
		var sb = new StringBuilder().append("    ".repeat(depth)).append(super.toString());
		if (!isClose() && !childs.isEmpty()) {
			var name = this.name();
			switch (name) {
				case "script", "style" -> sb.append("\n").append("    ".repeat(depth)).append(childs.get(0)).append("\n").append("    ".repeat(depth));
				case "textarea", "noscript" -> sb.append("\n").append("    ".repeat(depth + 1)).append('"').append(childs.get(0)).append('"').append("\n").append("    ".repeat(depth));
				default -> {
					if (childs.size() == 1 && childs.get(0) instanceof String s) return sb.append(s).append("</").append(name).append(">").toString();
					sb.append(childs.toString(depth + 1)).append("\n").append("    ".repeat(depth));
				}
			}
			return sb.append("</").append(name).append(">").toString();
		}
		return sb.toString();
	}

}
