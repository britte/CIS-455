package edu.upenn.cis.cis455.webserver;

import javax.servlet.http.HttpServlet;
import javax.servlet.ServletConfig;

public class myHttpServlet extends HttpServlet {
	
	private ServletConfig config;
	
	public myHttpServlet() {}
	
	public void init(ServletConfig config) {
		this.config = config;
	}
}
