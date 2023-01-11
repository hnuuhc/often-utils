package org.haic.often.parser.xml;

import org.haic.often.annotations.Contract;
import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

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

	/**
	 * 内部私有构造,用于解析html
	 *
	 * @param node   html剩余内容
	 * @param tag    标签文本
	 * @param isHtml 是否为html格式
	 */
	protected Element(@NotNull ParserStringBuilder node, @NotNull String tag, boolean isHtml) {
		this.name = (tag.contains(" ") ? tag.substring(1, tag.indexOf(" ")) : node.charAt(tag.length() - 2) == '/' ? tag.substring(1, tag.length() - 2) : tag.substring(1, tag.length() - 1)).strip().toLowerCase();
		this.attrs = htmlAttributes(tag); // 获取标签属性
		this.close = tag.charAt(tag.length() - 2) == '/';

		node.offset(tag.length()); // 更新进度
		if (close) return; // 自闭合标签直接返回

		if (isHtml) { // html特殊标签处理后返回
			switch (name) {
				// 自闭合标签
				case "br", "input", "meta", "link", "img", "area", "base", "col", "command", "embed", "keygen", "param", "source", "track", "wbr" -> {
					return;
				}
				// 文本标签
				case "textarea", "script", "style" -> {
					int index = node.indexOf("</" + name + ">");
					text = node.substring(node.pos(), index);
					node.pos(index + name.length() + 3);
					text = Document.unescape(text.strip());
					return;
				}
			}
		}

		tail = "</" + name + ">";

		StringBuilder text = new StringBuilder();
		while (node.pos() < node.length()) {
			int tagHeadIndex = node.indexOf("<");
			if (node.charAt(tagHeadIndex + 1) == '!' && node.charAt(tagHeadIndex + 2) == '-' && node.charAt(tagHeadIndex + 3) == '-') {
				text.append(node.substring(node.pos(), tagHeadIndex));
				node.pos(node.indexOf("-->", tagHeadIndex + 4) + 3); // 去除注释
				continue;
			}
			int tagtailIndex = node.indexOf(">", tagHeadIndex + 1);
			String childTag = node.substring(tagHeadIndex, tagtailIndex + 1); // 获取当前子标签
			int error = childTag.indexOf("<", 1);
			if (error != -1) { // 错误标签
				text.append(node.substring(node.pos(), tagHeadIndex + error)); // 写入文本
				node.pos(tagHeadIndex + error);
				continue;
			}
			text.append(node.substring(node.pos(), tagHeadIndex)); // 提前写入文本,防止返回
			if (node.charAt(tagHeadIndex + 1) == '/') { // 遇到结束标签返回上级
				if (node.substring(tagHeadIndex + 2, tagtailIndex).equals(name)) this.text = Document.unescape(text.toString().strip()); // 反转义文本
				node.pos(tagtailIndex + 1);
				return;
			}
			childs.add(new Element(node.pos(tagHeadIndex), childTag, isHtml));
		}
	}

	/**
	 * 提取html标签内属性值
	 *
	 * @param tag html标签
	 * @return 列表: 属性名称 - 属性值
	 */
	@Contract(pure = true)
	private static Map<String, String> htmlAttributes(@NotNull String tag) {
		Map<String, String> attrs = new HashMap<>();
		char[] tagChars = tag.toCharArray(); // 存在标签属性
		for (int i = 2; i < tagChars.length; i++) {
			if (tagChars[i] == ' ' && tagChars[i + 1] != ' ' && tagChars[++i] != '/' && tagChars[i] != '>') {
				StringBuilder key = new StringBuilder();
				do {
					key.append(tagChars[i++]);
					if (tagChars[i] == ' ') {
						attrs.put(key.toString(), "");
						break;
					} else if (tagChars[i] == '/' || tagChars[i] == '>') {
						attrs.put(key.toString(), "");
						return attrs;
					}
				} while (tagChars[i] != '=');
				StringBuilder value = new StringBuilder();
				if (tagChars[++i] == '"') {
					while (tagChars[++i] != '"') value.append(tagChars[i]);
					if (Character.isLetter(tagChars[i + 1])) {
						tagChars[i] = ' ';
						i--;
					}
				} else if (tagChars[i] == '\'') {
					while (tagChars[++i] != '\'') value.append(tagChars[i]);
				} else if (tagChars[i] == '&' && tagChars[i + 1] == 'q' && tagChars[i + 2] == 'u' && tagChars[i + 3] == 'o' && tagChars[i + 4] == 't' && tagChars[i + 5] == ';') {
					i = i + 6;
					do {
						value.append(tagChars[i++]);
					} while (tagChars[i] == '&' && tagChars[i + 1] == 'q' && tagChars[i + 2] == 'u' && tagChars[i + 3] == 'o' && tagChars[i + 4] == 't' && tagChars[i + 5] == ';');
					i += 5;
				} else {
					do {
						value.append(tagChars[i++]);
					} while (tagChars[i] != ' ' && tagChars[i] != '>' && !(tagChars[i] == '/' && tagChars[i + 1] == '>'));
					if (tagChars[i] == ' ') i--;
				}
				attrs.put(key.toString(), Document.unescape(value.toString()));
			}
		}
		return attrs;
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
		if (close) return sb.append("/>").toString();
		else sb.append(">");
		if (childs.isEmpty()) {
			if (!text.isEmpty()) {
				switch (name) {
					// 特殊文本标签
					case "script", "textarea", "style" -> sb.append("\n").append("    ".repeat(depth)).append(text).append("\n").append("    ".repeat(depth));
					default -> sb.append(text);
				}
			}
			return sb.append(tail).toString();
		} else {
			for (Element child : childs) sb.append("\n").append(child.toString(depth + 1));
			sb.append(text).append("\n").append("    ".repeat(depth)).append(tail);
		}
		return sb.toString();
	}

}
