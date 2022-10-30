package org.haic.often.net;

public enum Method {

	GET(false),
	POST(true),
	PUT(true),
	DELETE(false),
	PATCH(true),
	HEAD(false),
	OPTIONS(false),
	TRACE(false);

	private final boolean hasBody;

	Method(boolean hasBody) {
		this.hasBody = hasBody;
	}

	/**
	 * Check if this HTTP method has/needs a request body
	 *
	 * @return if body needed
	 */
	public final boolean hasBody() {
		return hasBody;
	}
}