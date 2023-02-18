package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

/**
 * 这是一个html和xml解析器,使用方法为 Document doc = Document.parse(String)
 * <p>
 * 重要警告: 由于格式化时自动完成转义,已经格式化的文本不可再次被解析
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Document extends Element {

	private final String type;

	public static Document parse(String body) {
		if (body == null) return null;
		var sb = new ParserStringBuilder(body).strip();
		if (sb.startsWith("\uFEFF")) sb.offset(1); // 去除特殊符号
		while (sb.startsWith("<!--")) sb.pos(sb.indexOf("-->", sb.pos() + 4) + 3); // 去除注释
		if (sb.stripLeading().startsWith("<!")) {
			var typeTail = sb.indexOf(">", sb.pos() + 2) + 1;
			var type = sb.substring(sb.pos(), typeTail);
			sb.pos(typeTail); // 更新位置
			while (sb.stripLeading().startsWith("<!--")) sb.pos(sb.indexOf("-->", sb.pos() + 4) + 3); // 去除注释
			return new Document(type, sb, true);
		} else if (sb.startsWith("<?")) {
			var typeTail = sb.indexOf(">", sb.pos() + 2) + 1;
			var type = sb.substring(sb.pos(), typeTail);
			sb.pos(typeTail); // 更新位置
			while (sb.stripLeading().startsWith("<!--")) sb.pos(sb.indexOf("-->", sb.pos() + 4) + 3); // 去除注释
			var name = sb.substring(sb.lastIndexOf("<") + 2, sb.lastIndexOf(">"));
			return new Document(type, sb.pos(sb.indexOf("<" + name)), false);
		} else if (sb.startsWith("<html")) {
			return new Document("", sb, true);
		} else if (sb.startsWith("<body")) {
			return new Document("", new ParserStringBuilder("<html><head></head>" + sb + "</html>"), true);
		} else {
			return new Document("", new ParserStringBuilder("<html><head></head><body>" + sb + "</body></html>"), true);
		}
	}

	private Document(@NotNull String type, @NotNull ParserStringBuilder node, boolean isHtml) {
		super(null, node);
		this.type = type;
		node.offset(1);
		for (Element tree = this; node.stripLeading().pos() < node.length() && tree != null; node.offset(1)) {
			int start = node.stripLeading().pos(); // 记录初始位置
			int tagHeadIndex = node.indexOf("<"); // 获取标签初始位置

			if (node.startsWith("!--", tagHeadIndex + 1)) {  // 去除注释
				var text = Document.unescape(node.substring(node.pos(), tagHeadIndex).stripTrailing()).strip();
				if (!text.isEmpty()) tree.addChild(text); // 合法标签之前数据识别为文本
				node.pos(node.indexOf("-->", tagHeadIndex + 4) + 3);
				continue;
			}

			var child = new Element(tree, node.pos(tagHeadIndex));

			while (node.charAt() == '<') { // 修正错误标签
				tagHeadIndex = node.pos();
				child = new Element(tree, node);
			}

			if (start + 1 < tagHeadIndex) { // 提前写入文本,防止结束返回
				var text = Document.unescape(node.substring(start, tagHeadIndex).stripTrailing()).strip();
				if (!text.isEmpty()) tree.addChild(text);
			}

			if (node.charAt() == '/') { // 结束标签返回,允许多级返回
				var name = node.offset(1).substring(node.pos(), node.pos(node.indexOf(">")).pos());
				for (var e = tree; e != null; e = e.parent()) {
					if (name.equalsIgnoreCase(e.name())) {
						tree = e.parent();
						break;
					}
				}
				continue;
			}

			if (isHtml) {  // 可能不规范的标签,需要处理
				switch (child.name()) { // html特殊标签处理后返回
					case "a", "p" -> {   // 可能不规范的标签,需要排序处理
						if (child.name().equals(tree.name())) {
							tree = tree.parent();
							tree.addChild(child);
							continue;
						}
					}
					// 自闭合标签
					case "hr", "br", "input", "meta", "link", "img", "area", "base", "col", "command", "embed", "keygen", "param", "source", "track", "wbr", "feflood", "feblend", "feoffset", "fegaussianblur", "fecomposite", "fecolormatrix", "lineargradient", "radialgradient" -> {
						tree.addChild(child.close(true));
						continue;
					}
					// 文本标签
					case "textarea", "script", "style", "noscript" -> {
						if (tree.name().equals("div")) tree = tree.parent(); // 异常位置
						int index = node.offset(1).indexOf("</" + child.name() + ">");
						if (index == -1) index = node.indexOf("</" + child.name().toUpperCase() + ">");
						var s = node.substring(node.pos(), index).strip();
						if (!s.isEmpty()) child.addChild(s);
						tree.addChild(child);
						node.pos(index + child.name().length() + 2);
						continue;
					}
				}
			}

			tree.addChild(child);
			if (!child.isClose()) tree = child; // 非自闭合标签,进入下级
		}

	}

	/**
	 * 返回html文档的html.head元素,如果不存在body,则会发生异常
	 *
	 * @return BODY元素
	 */
	public Element head() {
		return (Element) this.childs().stream().filter(l -> l instanceof XmlTree e && e.name().equals("head")).findFirst().orElseThrow();
	}

	/**
	 * 返回html文档的html.body元素,如果不存在body,则会发生异常
	 *
	 * @return BODY元素
	 */
	public Element body() {
		return (Element) this.childs().stream().filter(l -> l instanceof XmlTree e && e.name().equals("body")).findFirst().orElseThrow();
	}

	/**
	 * 反转义当前字符串,如果存在未知转义符,则不做转义处理
	 *
	 * @param s 待反转义字符串
	 * @return 反转义后的字符串
	 */
	public static String unescape(@NotNull String s) {
		var sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '&') {
				int index = s.indexOf(";", i + 1);
				if (index == -1) return sb.append(s.substring(i + 1)).toString();
				var escape = s.substring(i, (i = index) + 1);
				try {
					sb.append(HtmlEscape.unescape(escape));
				} catch (IllegalArgumentException e) {
					sb.append(escape);
				}
			} else {
				sb.append(s.charAt(i));
			}
		}
		return sb.toString();
	}

	/**
	 * 转义当前字符串
	 *
	 * @param s 待转义的字符串
	 * @return 转义后的字符串
	 */
	public static String escape(@NotNull String s) {
		var sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) sb.append(Character.isLetterOrDigit(s.charAt(i)) ? s.charAt(i) : HtmlEscape.escape(s.charAt(i)));
		return sb.toString();
	}

	@Override
	public String toString() {
		return type.isEmpty() ? super.toString() : type + "\n" + super.toString();
	}

}
