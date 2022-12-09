package org.haic.often.parser.xml;

import org.apache.commons.text.StringEscapeUtils;
import org.haic.often.util.StringUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
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

	private final String name; // 标签名
	private final Map<String, String> attrs;
	private final Elements childs = new Elements();
	private String text = "";
	private String tail = ""; // 标签结束符
	private boolean close;

	/**
	 * 构造器,用于创建一个新的节点,如果未增加子节点,则默认为当前节点状态为非自闭合(即拥有闭合标签)
	 *
	 * @param name 标签名称
	 */
	public Element(@NotNull String name) {
		this.name = name;
		this.attrs = new HashMap<>();
		this.close = false;
		this.tail = "</" + name + ">";
	}

	public Element(@NotNull String node, @NotNull String name, boolean isHtml) {
		this(new StringBuilder(node), node.substring(0, node.indexOf(">") + 1), name, false, isHtml);
	}

	/**
	 * 内部私有构造,用于解析html
	 *
	 * @param node   html剩余内容
	 * @param tag    标签文本
	 * @param name   标签名称
	 * @param close  是否为自闭合标签
	 * @param isHtml 是否为html格式
	 */
	private Element(@NotNull StringBuilder node, @NotNull String tag, @NotNull String name, boolean close, boolean isHtml) {
		this.name = name; // 标签名称
		this.attrs = StringUtil.htmlAttributes(tag); // 获取标签属性
		this.close = close;

		node.delete(0, tag.length());  // 更新进度
		if (close) return; // 自闭合标签直接返回

		if (isHtml) {
			switch (name) { // 特殊文本类标签处理后返回
				// 自闭合标签
				case "br", "input", "meta", "link", "img", "area", "base", "col", "command", "embed", "keygen", "param", "source", "track", "wbr" -> {
					return;
				}
				case "textarea", "script", "style" -> {
					tail = "</" + name + ">";
					text = node.substring(0, node.indexOf("</" + name + ">"));
					node.delete(0, text.length() + name.length() + 3);
					text = text.strip();
					return;
				}
			}
		}

		tail = "</" + name + ">";

		while (node.length() > 0) {
			if (node.charAt(0) == '<') { // 判断是否为文字
				if (node.charAt(1) == '!' && node.charAt(2) == '-' && node.charAt(3) == '-') {
					node.delete(0, node.indexOf("-->") + 3);
					continue;
				}
				if (node.charAt(1) == '/') { // 遇到结束标签返回上级
					int index = node.indexOf(">");
					if (node.substring(2, index).equals(name)) {
						node.delete(0, index + 1);
						return;
					} else {
						node.delete(0, index + 1);
						continue;
					}
				}

				String childTag = node.substring(0, node.indexOf(">") + 1); // 获取当前子标签
				String childTagName = childTag.contains(" ") ? childTag.substring(1, childTag.indexOf(" ")) : node.charAt(childTag.length() - 2) == '/' ? childTag.substring(1, childTag.length() - 2) : childTag.substring(1, childTag.length() - 1);
				childTagName = childTagName.strip().toLowerCase(); // 由于html不区分大小写,统一以小写处理

				if (node.charAt(childTag.length() - 2) == '/') {
					childs.add(new Element(node, childTag, childTagName, true, isHtml));
				} else {
					childs.add(new Element(node, childTag, childTagName, false, isHtml));
				}
			} else { // 例如em标签是格式化,会造成上级标签多个位置存在文字
				String text = node.substring(0, node.indexOf("<"));
				node.delete(0, text.length());
				//noinspection StringConcatenationInLoop
				this.text += StringEscapeUtils.unescapeHtml4(text).strip(); // 反转义特殊字符,耗时较长等待修复
			}
		}
	}

	/**
	 * 设置当前节点是否为自闭合(警告: 如果设置为自闭合,在格式化时将不会输出子节点以及文本,查询不受影响)
	 *
	 * @param close true为是自闭合标签,反之同理
	 * @return 当前节点
	 */
	public Element close(boolean close) {
		this.close = close;
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
		this.attrs.put(key, value);
		return this;
	}

	/**
	 * 为当前节点标签增加属性
	 *
	 * @param attrs 属性参数数组
	 * @return 当前节点
	 */
	public Element attrs(@NotNull Map<String, String> attrs) {
		this.attrs.putAll(attrs);
		return this;
	}

	/**
	 * 删除当前节点的一个属性
	 *
	 * @param key 属性名
	 * @return 当前节点
	 */
	public Element removeAttr(@NotNull String key) {
		this.attrs.remove(key);
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
	 * 设置当前节点文本
	 *
	 * @param text 文本
	 * @return 当前节点
	 */
	public Element text(@NotNull String text) {
		this.text = text;
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
		return name;
	}

	/**
	 * 判断当前标签是否包含某个属性
	 *
	 * @param key 属性名
	 * @return 判断结果
	 */
	@Contract(pure = true)
	public boolean containsAttr(@NotNull String key) {
		return attrs.containsKey(key);
	}

	/**
	 * 获取当前标签所有属性(可能为空)
	 *
	 * @return 当前标签所有属性
	 */
	@NotNull
	@Contract(pure = true)
	public Map<String, String> attrs() {
		return attrs;
	}

	/**
	 * 获取当前标签指定属性值
	 *
	 * @param key 属性名称
	 * @return 属性值
	 */
	@Contract(pure = true)
	public String attr(@NotNull String key) {
		return attrs.get(key);
	}

	/**
	 * 获取当前标签的所有下级子标签
	 *
	 * @return 所有下级子标签
	 */
	@NotNull
	@Contract(pure = true)
	public Elements childs() {
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
		StringBuilder text = new StringBuilder(this.text);
		for (var child : childs) {
			switch (child.name()) {
				case "script", "textarea", "style" -> {} // 特殊文本标签
				default -> {
					String childText = child.text();
					if (!childText.isEmpty()) {
						text.append(" ").append(childText);
					}
				}
			}
		}
		return text.toString().strip();
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
		return new Elements(childs).select(cssQuery);
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
		for (Element child : childs) {
			result.addAll(child.selectById(id));
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
		for (Element child : childs) {
			result.addAll(child.selectByName(name));
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
		for (Element child : childs) {
			result.addAll(child.selectByNameAndAttrKey(name, key));
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
		for (Element child : childs) {
			result.addAll(child.selectByNameAndAttr(name, key, value));
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
		for (Element child : childs) {
			result.addAll(child.selectByAttr(key));
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
		for (Element child : childs) {
			result.addAll(child.selectByAttr(key, value));
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
		StringBuilder sb = new StringBuilder();
		sb.append("    ".repeat(depth)).append("<").append(name).append(attrs.entrySet().stream().map(attr -> " " + attr.getKey() + "=\"" + attr.getValue() + "\"").collect(Collectors.joining()));
		if (close) {
			return sb.append("/>").toString();
		} else {
			sb.append(">");
		}
		if (childs.isEmpty()) {
			if (!text.isEmpty()) {
				switch (name) {
					case "script", "textarea", "style" -> // 特殊文本标签
							sb.append("\n").append("    ".repeat(depth)).append(text).append("\n").append("    ".repeat(depth));
					default -> sb.append(text);
				}
			}
			sb.append(tail);
			return sb.toString();
		} else {
			for (Element child : childs) {
				sb.append("\n").append(child.toString(depth + 1));
			}
			sb.append(text).append("\n").append("    ".repeat(depth)).append(tail);
		}
		return sb.toString();
	}

}
