package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * 这是一个html和xml解析器,使用方法为 Document doc = Document.parse(String)
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Document extends Element {

	private final String doctype;

	public static final List<String> titleTag = Arrays.asList("h1", "h2", "h3", "h4", "h5");

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
			var start = sb.pos();
			var tail = sb.indexOf(">", start + 5) + 1;
			return new Document(type, sb.pos(tail), sb.substring(start, tail), true);
		} else if (sb.startsWith("<?")) {
			var typeTail = sb.indexOf(">", sb.pos() + 2) + 1;
			var type = sb.substring(sb.pos(), typeTail);
			sb.pos(typeTail); // 更新位置
			while (sb.stripLeading().startsWith("<!--")) sb.pos(sb.indexOf("-->", sb.pos() + 4) + 3); // 去除注释
			var name = sb.substring(sb.lastIndexOf("<") + 2, sb.lastIndexOf(">"));
			var start = sb.indexOf("<" + name);
			var tail = sb.indexOf(">", start + name.length() + 1) + 1;
			return new Document(type, sb.pos(tail), sb.substring(start, tail), false);
		} else if (sb.startsWith("<html")) {
			var start = sb.pos();
			var tail = sb.indexOf(">", 1) + 1;
			return new Document("", sb.pos(tail), sb.substring(start, tail), true);
		} else if (sb.startsWith("<body")) {
			return new Document("", new ParserStringBuilder("<html><head></head>" + (sb.pos() == 0 ? sb : sb.substring(sb.pos())) + "</html>").pos(6), "<html>", true);
		} else {
			return new Document("", new ParserStringBuilder("<html><head></head><body>" + (sb.pos() == 0 ? sb : sb.substring(sb.pos())) + "</body></html>").pos(6), "<html>", true);
		}
	}

	private Document(@NotNull String doctype, @NotNull ParserStringBuilder node, @NotNull String tag, boolean isHtml) {
		super(tag);
		this.doctype = doctype.isEmpty() ? doctype : doctype + "\n";
		var e = (XmlTree) this;
		while (node.stripLeading().pos() < node.length() && e != null) {
			if (e.isClose()) e = e.parent();
			var name = e.name();
			if (isHtml) { // html特殊标签处理后返回
				switch (name) {
					// 自闭合标签
					case "hr", "br", "input", "meta", "link", "img", "area", "base", "col", "command", "embed", "keygen", "param", "source", "track", "wbr", "feflood", "feblend", "feoffset", "fegaussianblur", "fecomposite", "fecolormatrix", "lineargradient", "radialgradient" -> {
						e = e.parent();
						continue;
					}
					// 文本标签
					case "textarea", "script", "style", "noscript" -> {
						int index = node.stripLeading().indexOf("</" + name + ">");
						if (index == -1) index = node.indexOf("</" + name.toUpperCase() + ">");
						var text = node.substring(node.pos(), index).strip();
						if (!text.isEmpty()) e.addChild(text);
						node.pos(index + name.length() + 3);
						e = e.parent();
						continue;
					}
				}
			}

			int tagHeadIndex = node.stripLeading().indexOf("<"); // 获取标签初始位置
			if (node.startsWith("!--", tagHeadIndex + 1)) {  // 去除注释
				var text = Document.unescape(node.substring(node.pos(), tagHeadIndex).stripTrailing()).strip();
				if (!text.isEmpty()) e.addChild(text); // 合法标签之前数据识别为文本
				node.pos(node.indexOf("-->", tagHeadIndex + 4) + 3);
				continue;
			}
			int tagtailIndex = node.indexOf(">", tagHeadIndex + 1);
			var thisChild = node.substring(tagHeadIndex, tagtailIndex + 1); // 获取当前子标签
			int error = thisChild.indexOf("<", 1); // 检查标签合法性
			if (error != -1) { // 标签错误,存在多个'<'符号
				var text = Document.unescape(node.substring(node.pos(), tagHeadIndex + error).stripTrailing()).strip();
				if (!text.isEmpty()) e.addChild(text); // 合法标签之前数据识别为文本
				node.pos(tagHeadIndex + error);
				continue;
			}
			var text = Document.unescape(node.substring(node.pos(), tagHeadIndex).stripTrailing()).strip();
			if (!text.isEmpty()) e.addChild(text); // 提前写入文本,防止结束返回
			if (thisChild.charAt(1) == '/') {
				node.pos(tagtailIndex + 1);
				for (var p = e; p != null; p = p.parent()) {
					if (thisChild.substring(2, thisChild.length() - 1).equalsIgnoreCase(p.name())) {
						e = p.parent();
						break;
					}
				}
				continue;
			}

			var child = new XmlTree(e, thisChild);
			if (isHtml) {  // 可能不规范的标签,需要排序处理
				if (name.equals("a") && name.equals(child.name())) {
					e = e.parent();
					continue;
				}
				if (name.equals("p") && name.equals(child.name())) {
					e = e.parent();
					continue;
				}
			}
			e.addChild(child);
			e = child;
			node.pos(tagtailIndex + 1);
		}

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
		return doctype + super.toString(0);
	}

}
