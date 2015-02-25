package edu.upenn.cis.cis455.webserver;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class myServletContext implements ServletContext {

	private String root;
	private String name;
	private HashMap<String, Object> attributes = new HashMap<String, Object>();;
	private HashMap<String, String> initParams;
	
	public myServletContext(String root, String name, HashMap<String,String> initParams) {
		this.root = root;
		this.name = name;
		this.initParams = initParams;
	}
	
	
	@Override
	public Object getAttribute(String attr) {
		return attributes.get(attr);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new IterEnumeration<String>(this.attributes.keySet().iterator());
	}

	@Override
	public ServletContext getContext(String path) {
		// Since we are only supporting a single context, 
		// the only valid requested path is that of the current context
		return (path.equals("/")) ? this : null;
	}

	@Override
	public String getInitParameter(String param) {
		return this.initParams.get(param);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return new IterEnumeration<String>(this.initParams.keySet().iterator());
	}

	@Override
	public int getMajorVersion() {
		return 2; // Version 2.4 compliant
	}

	@Override
	public int getMinorVersion() {
		return 4; // Version 2.4 compliant
	}

	@Override
	public String getRealPath(String path) {
		return this.root + path;
	}

	@Override
	public String getServerInfo() {
		return "HttpServer/1.0";
	}
	
	@Override
	public String getServletContextName() {
		return this.name;
	}
	
	@Override
	public void removeAttribute(String attr) {
		this.attributes.remove(attr);
	}

	@Override
	public void setAttribute(String attrName, Object attrVal) {
		this.attributes.put(attrName, attrVal);
	}

	
	//
	// Deprecated or Do Not Implement Methods
	//
	@Override
	public String getMimeType(String arg0) {
		return null; // DO NOT IMPLEMENT
	}

	@Override
	public RequestDispatcher getNamedDispatcher(String arg0) {
		return null; // DO NOT IMPLEMENT
	}
	
	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null; // DO NOT IMPLEMENT
	}

	@Override
	public URL getResource(String arg0) throws MalformedURLException {
		return null; // DO NOT IMPLEMENT
	}

	@Override
	public InputStream getResourceAsStream(String arg0) {
		return null; // DO NOT IMPLEMENT
	}

	@Override
	public Set getResourcePaths(String arg0) {
		return null; // DO NOT IMPLEMENT
	}
	
	@Override
	public Servlet getServlet(String arg0) throws ServletException {
		return null; // DEPRECATED
	}

	@Override
	public Enumeration getServletNames() {
		return null; // DEPRECATED
	}

	@Override
	public Enumeration getServlets() {
		return null; // DEPRECATED
	}

	@Override
	public void log(String arg0) {
		// DO NOT IMPLEMENT		
	}

	@Override
	public void log(Exception arg0, String arg1) {
		// DEPRECATED
	}

	@Override
	public void log(String arg0, Throwable arg1) {
		// DO NOT IMPLEMENT
	}
}
