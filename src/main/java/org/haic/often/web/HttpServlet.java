package org.haic.often.web;

import java.io.IOException;

public abstract class HttpServlet {

	public abstract void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException;

	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doGet(req, res);
	}

	public void doPut(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doGet(req, res);
	}

	public void doDelete(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doGet(req, res);
	}

	public void doHead(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doGet(req, res);
	}

	public void doTrace(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doGet(req, res);
	}

	public void doConnect(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doGet(req, res);
	}

	public void doOptions(HttpServletRequest req, HttpServletResponse res) throws IOException {
		doGet(req, res);
	}

}
