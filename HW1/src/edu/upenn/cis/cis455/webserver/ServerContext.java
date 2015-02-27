package edu.upenn.cis.cis455.webserver;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;

public class ServerContext {
	
	// Server items
	protected int port;
	protected String root;
	
	// Servlet items
	protected String servletPath;
	protected myServletContext servletContext;
	protected HashMap<String,myServletWrapper> servlets;
	
	public ServerContext() {}
}
