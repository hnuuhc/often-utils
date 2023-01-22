package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;
import org.haic.often.parser.ParserStringBuilder;

/**
 * 这是一个html和xml解析器,使用方法为 Document doc = Document.parse(String)
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Document extends Element {

	private final String doctype;

	public static Document parse(@NotNull String body) {
		var sb = new ParserStringBuilder(body.strip());
		if (sb.startsWith("<!") && sb.charAt(2) != '-') {
			var start = sb.indexOf("<html");
			var tail = sb.indexOf(">", start + 5);
			return new Document(sb.substring(0, sb.indexOf(">", 2) + 1), sb.pos(tail + 1), sb.substring(start, tail), true);
		} else if (sb.startsWith("<?")) {
			var name = sb.substring(sb.lastIndexOf("<") + 2, sb.lastIndexOf(">"));
			var start = sb.indexOf("<" + name);
			var tail = sb.indexOf(">", start + name.length() + 1);
			return new Document(sb.substring(0, sb.indexOf(">", 2) + 1), sb.pos(tail + 1), sb.substring(start, tail), false);
		} else if (sb.startsWith("<html")) {
			var index = sb.indexOf(">", 1) + 1;
			return new Document("", sb.pos(index), sb.substring(0, index), true);
		} else if (sb.startsWith("<body")) {
			return new Document("", new ParserStringBuilder("<html><head></head>" + sb + "</html>").pos(6), "<html>", true);
		} else {
			return new Document("", new ParserStringBuilder("<html><head></head><body>" + sb + "</body></html>").pos(6), "<html>", true);
		}
	}

	private Document(@NotNull String doctype, @NotNull ParserStringBuilder body, @NotNull String tag, boolean isHtml) {
		super(body, new Tag(tag), isHtml);
		this.doctype = doctype.isEmpty() ? doctype : doctype + "\n";
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
		return doctype + super.toString();
	}

}
