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

	private HashMap<String, Object> attributes;
	private HashMap<String, String> initParams;
	
	public myServletContext(HashMap<String,String> initParams) {
		this.attributes = new HashMap<String, Object>();
		this.initParams = initParams;
	}
	
	
	@Override
	public Object getAttribute(String attr) {
		return attributes.get(attr);
	}

	@Override
	public Enumeration getAttributeNames() {
		return new IterEnumeration<String>(this.attributes.keySet().iterator());
	}

	@Override
	public ServletContext getContext(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getInitParameter(String param) {
		return this.initParams.get(param);
	}

	@Override
	public Enumeration getInitParameterNames() {
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
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerInfo() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getServletContextName() {
		// TODO Auto-generated method stub
		return null;
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
