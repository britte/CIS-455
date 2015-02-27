package edu.upenn.cis.cis455.webserver;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class myServletWrapper {
	
	protected HttpServlet servlet;
	private myServletConfig config;
	private boolean isInit = false;
	
	public myServletWrapper(HttpServlet servlet, myServletConfig config) {
		this.servlet = servlet;
		this.config = config;
	}
	
	public void configInit() throws ServletException {
		this.servlet.init(this.config);
	}
	
	public boolean isInit() {
		return this.isInit;
	}
	
}
