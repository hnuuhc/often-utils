package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * xml解析器父类,仅能解析子节点
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:37
 */
public class Element {

	private final Tag tag;
	private final XmlChilds childs = new XmlChilds();

	/**
	 * 构造器,用于创建一个新的节点,如果未增加子节点,则默认为当前节点状态为非自闭合(即拥有闭合标签)
	 *
	 * @param name 标签名称
	 */
	public Element(@NotNull String name) {
		this.tag = new Tag(name, new TagAttrs(), false);
	}

	/**
	 * 内部私有构造,用于解析html
	 *
	 * @param node   html剩余内容
	 * @param tag    标签文本
	 * @param isHtml 是否为html格式
	 */
	protected Element(@NotNull ParserStringBuilder node, @NotNull Tag tag, boolean isHtml) {
		this.tag = tag;
		if (tag.isClose()) return;

		var name = tag.name();

		if (isHtml) { // html特殊标签处理后返回
			switch (name) {
				// 自闭合标签
				case "hr", "br", "input", "meta", "link", "img", "area", "base", "col", "command", "embed", "keygen", "param", "source", "track", "wbr", "feflood", "feblend", "feoffset", "fegaussianblur", "fecomposite", "fecolormatrix", "lineargradient", "radialgradient" -> {
					return;
				}
				// 文本标签
				case "textarea", "script", "style", "noscript" -> {
					int index = node.indexOf("</" + name + ">");
					var text = node.substring(node.pos(), index).strip();
					if (!text.isEmpty()) childs.add(text);
					node.pos(index + name.length() + 3);
					return;
				}
			}
		}

		while (node.pos() < node.length()) {
			int tagHeadIndex = node.indexOf("<"); // 获取标签初始位置
			if (node.charAt(tagHeadIndex + 1) == '!' && node.charAt(tagHeadIndex + 2) == '-' && node.charAt(tagHeadIndex + 3) == '-') { // 标签为注释
				var text = node.substring(node.pos(), tagHeadIndex).strip();
				if (!text.isEmpty()) childs.add(Document.unescape(text)); // 合法标签之前数据识别为文本
				node.pos(node.indexOf("-->", tagHeadIndex + 4) + 3); // 去除注释
				continue;
			}
			int tagtailIndex = node.indexOf(">", tagHeadIndex + 1);
			var thisChild = node.substring(tagHeadIndex, tagtailIndex + 1); // 获取当前子标签

			int error = thisChild.indexOf("<", 1); // 检查标签合法性
			if (error != -1) { // 标签错误,存在多个'<'符号
				var text = node.substring(node.pos(), tagHeadIndex + error).strip();
				if (!text.isEmpty()) childs.add(Document.unescape(text)); // 合法标签之前数据识别为文本
				node.pos(tagHeadIndex + error);
				continue;
			}
			var text = node.substring(node.pos(), tagHeadIndex).strip();
			if (!text.isEmpty()) childs.add(Document.unescape(text)); // 提前写入文本,防止结束返回
			if (thisChild.charAt(1) == '/') {
				node.pos(tagtailIndex + 1);
				if (thisChild.substring(2, thisChild.length() - 1).equalsIgnoreCase(name)) return; // 结束标签返回上级
				else continue; // 错误标签,跳过
			}

			var childTag = new Tag(thisChild);
			if (isHtml) {  // 可能不规范的链接标签,需要排序处理
				if (name.equals("a") && name.equals(childTag.name())) return;
				if (name.equals("p") && name.equals(childTag.name())) return;
			}
			childs.add(new Element(node.pos(tagtailIndex + 1), childTag, isHtml));
		}
	}

	/**
	 * 设置当前节点是否为自闭合(警告: 如果设置为自闭合,在格式化时将不会输出子节点以及文本,查询不受影响)
	 *
	 * @param close true为是自闭合标签,反之同理
	 * @return 当前节点
	 */
	public Element close(boolean close) {
		tag.isClose(close);
		return this;
	}

	/**
	 * 为当前节点标签增加属性
	 *
	 * @param key   属性名
	 * @param value 属性值
	 * @return 当前节点
	 */
	public Element attr(@NotNull String key, @NotNull String value) {
		tag.attr(key, value);
		return this;
	}

	/**
	 * 为当前节点标签增加属性
	 *
	 * @param attrs 属性参数数组
	 * @return 当前节点
	 */
	public Element attrs(@NotNull Map<String, String> attrs) {
		tag.attrs(attrs);
		return this;
	}

	/**
	 * 删除当前节点的一个属性
	 *
	 * @param key 属性名
	 * @return 当前节点
	 */
	public Element removeAttr(@NotNull String key) {
		tag.removeAttr(key);
		return this;
	}

	/**
	 * 为当前节点增加子节点
	 *
	 * @param child 子节点
	 * @return 当前节点
	 */
	public Element child(@NotNull Element child) {
		this.childs.add(child);
		return this;
	}

	/**
	 * 为当前节点增加子节点
	 *
	 * @param childs 子节点数组
	 * @return 当前节点
	 */
	public Element childs(@NotNull Elements childs) {
		this.childs.addAll(childs);
		return this;
	}

	/**
	 * 删除一个子节点
	 *
	 * @param index 子节点位置
	 * @return 当前节点
	 */
	public Element removeChild(int index) {
		this.childs.remove(index);
		return this;
	}

	/**
	 * 获取当前标签名称
	 *
	 * @return 标签名称
	 */
	@NotNull
	@Contract(pure = true)
	public String name() {
		return tag.name();
	}

	/**
	 * 判断当前标签是否包含某个属性
	 *
	 * @param key 属性名
	 * @return 判断结果
	 */
	@Contract(pure = true)
	public boolean containsAttr(@NotNull String key) {
		return tag.containsKey(key);
	}

	/**
	 * 获取当前标签所有属性(可能为空)
	 *
	 * @return 当前标签所有属性
	 */
	@NotNull
	@Contract(pure = true)
	public Map<String, String> attrs() {
		return tag.attrs();
	}

	/**
	 * 获取当前标签指定属性值
	 *
	 * @param key 属性名称
	 * @return 属性值
	 */
	@Contract(pure = true)
	public String attr(@NotNull String key) {
		return tag.attr(key);
	}

	/**
	 * 获取当前标签的下级子标签
	 *
	 * @param i 子标签的索引
	 * @return 下级子标签
	 */
	@NotNull
	@Contract(pure = true)
	public Object child(int i) {
		return childs.get(i);
	}

	/**
	 * 获取当前标签的所有下级子标签
	 *
	 * @return 所有下级子标签
	 */
	@NotNull
	@Contract(pure = true)
	public XmlChilds childs() {
		return childs;
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
		return childs.stream().map(l -> l instanceof Element e ? e.text() : l.toString()).filter(l -> !l.isEmpty()).collect(Collectors.joining(" "));
	}

	/**
	 * 按照指定规则查询标签第一个,查询规则参照{@link #select(String)}
	 *
	 * @param cssQuery 查询规则
	 * @return 查询结果
	 */
	@Contract(pure = true)
	public Element selectFirst(@NotNull String cssQuery) {
		Elements result = select(cssQuery);
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
		Elements result = new Elements();
		if (id.equals(attr("id"))) {
			result.add(this);
			return result;
		}
		for (var child : childs) {
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
		Elements result = new Elements();
		if (name().equals(name)) {
			result.add(this);
			return result;
		}
		for (var child : childs) {
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
		Elements result = new Elements();
		if (name().equals(name) && containsAttr(key)) {
			result.add(this);
			return result;
		}
		for (var child : childs) {
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
		Elements result = new Elements();
		if (name().equals(name) && containsAttr(key) && value.equals(attr(key))) {
			result.add(this);
			return result;
		}
		for (var child : childs) {
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
		Elements result = new Elements();
		if (containsAttr(key)) {
			result.add(this);
			return result;
		}
		for (var child : childs) {
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
		Elements result = new Elements();
		if (containsAttr(key) && value.equals(attr(key))) {
			result.add(this);
			return result;
		}
		for (var child : childs) {
			if (child instanceof Element e) result.addAll(e.selectByAttr(key, value));
		}
		return result;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	/**
	 * 以指定的深度格式化当前标签
	 *
	 * @param depth 深度
	 * @return 格式化的标签
	 */
	@NotNull
	@Contract(pure = true)
	public String toString(int depth) {
		var sb = new StringBuilder().append("    ".repeat(depth)).append(tag);
		if (!tag.isClose() && !childs.isEmpty()) {
			var name = this.name();
			switch (name) {
				case "script", "style" -> sb.append("\n").append("    ".repeat(depth)).append(childs.get(0)).append("\n").append("    ".repeat(depth));
				case "textarea", "noscript" -> sb.append("\n").append("    ".repeat(depth)).append('"').append(childs.get(0)).append('"').append("\n").append("    ".repeat(depth));
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
