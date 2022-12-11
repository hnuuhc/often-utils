package org.haic.often.parser.xml;

import org.haic.often.annotations.NotNull;

/**
 * 这是一个html和xml解析器,使用方法为 Document doc = Document.parse(String)
 *
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Document extends Element {

	private final String doctype;

	private Document(@NotNull String doctype, @NotNull String body, @NotNull String name, boolean isHtml) {
		super(body, name, isHtml);
		this.doctype = doctype.isEmpty() ? doctype : doctype + "\n";
	}

	public static Document parse(@NotNull String body) {
		String doctype = "", name;
		boolean isHtml = false;
		body = body.strip();
		if (body.startsWith("<!") && body.charAt(2) != '-') {
			doctype = body.substring(0, body.indexOf(">") + 1);
			isHtml = true;
			body = body.substring(body.indexOf("<html"));
			if (!body.endsWith("</html>")) {
				body = body.substring(0, body.lastIndexOf("</html>") + 7);
			}
			name = "html";
		} else if (body.startsWith("<html")) {
			isHtml = true;
			if (!body.endsWith("</html>")) {
				body = body.substring(0, body.lastIndexOf("</html>") + 7);
			}
			name = "html";
		} else if (body.startsWith("<?")) {
			doctype = body.substring(0, body.indexOf(">") + 1);
			body = body.substring(doctype.length());
			body = body.substring(body.indexOf("<"));
			name = body.substring(body.lastIndexOf("</") + 2, body.length() - 1);
		} else {
			body = "<html><head></head><body>" + body + "</body></html>";
			name = "html";
		}
		return new Document(doctype, body, name, isHtml);
	}

	@Override
	public String toString() {
		return doctype + super.toString();
	}

}
