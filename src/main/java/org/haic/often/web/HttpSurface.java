package org.haic.often.web;

import org.haic.often.web.path.Path400;
import org.haic.often.web.path.Path404;
import org.haic.often.web.path.PathHello;

import java.util.HashMap;
import java.util.Map;

public class HttpSurface {

	private static final Map<String, HttpServlet> surface = new HashMap<>();

	static {
		register("/", new PathHello());
		register("404", new Path404());
		register("400", new Path400());
	}

	public static void register(String path, HttpServlet httpServlet) {
		surface.put(path, httpServlet);
	}

	public static HttpServlet get(String path) {
		var httpServlet = surface.get(path);
		return httpServlet == null ? surface.get("404") : httpServlet;
	}

}
