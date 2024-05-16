package org.haic.often.net.http;

import java.util.HashMap;
import java.util.Map;

public class CookieStore extends HashMap<String, Map<String, String>> {

	public Map<String, String> cookies() {
		var cookies = new HashMap<String, String>();
		this.values().forEach(cookies::putAll);
		return cookies;
	}

}
