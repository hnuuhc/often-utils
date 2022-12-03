package org.haic.often.parser.xml;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Document extends Element {

	private String doctype;

	public Document(String body) {
		this(body = body.strip(), body.startsWith("<!") && body.charAt(2) != '-');
	}

	private Document(String body, boolean isHtml) {
		this(body, body.startsWith("<!") && body.charAt(2) != '-' ? "html" : body.substring(body.lastIndexOf("</") + 2, body.length() - 1), isHtml);
	}

	private Document(String body, String name, boolean isHtml) {
		super(body.substring(body.indexOf("<" + name)), name, isHtml);
		if (body.startsWith("<!") && body.charAt(2) != '-') {
			doctype = body.substring(0, body.indexOf(">") + 1);
		} else if (body.startsWith("<?")) {
			doctype = body.substring(0, body.indexOf(">") + 1);
		}
	}

	@Override
	public String toString() {
		String body = "";
		if (doctype != null) {
			body += doctype + "\n";
		}
		return body + super.toString();
	}

}
