package edu.upenn.cis.cis455.webserver;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.log4j.Logger;


public class myServletConfig implements ServletConfig {

	private Logger logger = Logger.getLogger(HttpServer.class);	
	
	private String servletName;
	private myServletContext servletContext;
	private HashMap<String, String> initParameters;
	
	public myServletConfig(String name, myServletContext context, HashMap<String, String> params) {
		
		this.servletName = name;
		this.servletContext = context;
		this.initParameters = params;

	}
	
	@Override
	public String getInitParameter(String paramName) {
		return this.initParameters.get(paramName);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return new IterEnumeration<String>(this.initParameters.keySet().iterator());
	}

	@Override
	public ServletContext getServletContext() {
		return this.servletContext;
	}

	@Override
	public String getServletName() {
		return this.servletName;
	}

}
