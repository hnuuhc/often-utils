package org.haic.often.web.path;

import org.haic.often.web.HttpServlet;
import org.haic.often.web.HttpServletRequest;
import org.haic.often.web.HttpServletResponse;

import java.io.IOException;

public class PathHello extends HttpServlet {

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
		res.header("content-type", "text/html; charset=utf-8");
		res.write("<html><head></head><body><h1 style=\"text-align:center;\">Hello World!</h1></body></html>");
		res.flush();
	}

}
