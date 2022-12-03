package org.haic.often.parser.xml;

/**
 * @author haicdust
 * @version 1.0
 * @since 2022/11/30 9:36
 */
public class Document extends Element {

	private final String doctype;

	public Document(String body) {
		this(new HtmlCleaner(body));
	}

	private Document(HtmlCleaner htmlClear) {
		super(htmlClear.body(), htmlClear.head(), htmlClear.isHtml());
		this.doctype = htmlClear.doctype();
	}

	private static class HtmlCleaner {

		private String doctype;
		private final String body;
		private boolean isHtml;
		private final String head;

		public HtmlCleaner(String body) {
			body = body.strip();
			if (body.startsWith("<!") && body.charAt(2) != '-') {
				doctype = body.substring(0, body.indexOf(">") + 1);
				isHtml = true;
				body = body.substring(body.indexOf("<html"));
				if (!body.endsWith("</html>")) {
					body = body.substring(0, body.lastIndexOf("</html>") + 7);
				}
				head = "html";
			} else if (body.startsWith("<html")) {
				isHtml = true;
				if (!body.endsWith("</html>")) {
					body = body.substring(0, body.lastIndexOf("</html>") + 7);
				}
				head = "html";
			} else if (body.startsWith("<?")) {
				doctype = body.substring(0, body.indexOf(">") + 1);
				body = body.substring(doctype.length());
				body = body.substring(body.indexOf("<"));
				head = body.substring(body.lastIndexOf("</") + 2, body.length() - 1);
			} else {
				body = "<html><head></head><body>" + body + "</body></html>";
				head = "html";
			}
			this.body = body;
		}

		public String head() {
			return head;
		}

		public String doctype() {
			return doctype;
		}

		public boolean isHtml() {
			return isHtml;
		}

		public String body() {
			return body;
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