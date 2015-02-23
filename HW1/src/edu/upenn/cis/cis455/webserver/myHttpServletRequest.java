package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class myHttpServletRequest implements HttpServletRequest {

	private String method; // TODO
	private String path; // TODO
	private String version; // TODO
	
	private HashMap<String,ArrayList<String>> headers; // TODO
	
	private String characterEncoding = "ISO-8859-1";
	private int contentLength; // TODO
	private String contentType; // TODO
	private Locale locale = null;
	
	private HashMap<String,Object> attributes;
	private HashMap<String,String[]> params;
	private myHttpSession session;
	
	//
	// ServletResponse Methods
	//
	
	@Override
	public Object getAttribute(String attr) {
		return this.attributes.get(attr);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new HashEnum<String>(this.attributes.keySet().iterator());
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
	public int getContentLength() {
		return this.contentLength;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getLocalPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public String getParameter(String param) {
		String[] matchedParams = this.params.get(param);
		return (matchedParams == null) ? null : matchedParams[0];
	}

	@Override
	public Map getParameterMap() {
		return this.params;
	}

	@Override
	public Enumeration<String> getParameterNames() {
		return new HashEnum<String>(this.params.keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String param) {
		return this.params.get(param);
	}

	@Override
	public String getProtocol() {
		return "HTTP/" + this.version; // ex: HTTP/1.1
	}

	@Override
	public BufferedReader getReader() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getRemotePort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getServerPort() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeAttribute(String attr) {
		this.attributes.remove(attr);
	}

	@Override
	public void setAttribute(String attrName, Object attrVal) {
		this.attributes.put(attrName, attrVal);
	}

	@Override
	public void setCharacterEncoding(String encoding)
			throws UnsupportedEncodingException {
		// TODO check for supported encodings
		
		this.characterEncoding = encoding;
	}
	
	//
	// HttpServletResponse Methods
	//

	@Override
	public String getAuthType() {
		return BASIC_AUTH;
	}

	@Override
	public String getContextPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getHeader(String header) {
		ArrayList<String> matchedHeaders = this.headers.get(header);
		return (matchedHeaders == null) ? null : matchedHeaders.get(0);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return new HashEnum<String> (this.headers.keySet().iterator());
	}

	@Override
	public Enumeration<String> getHeaders(String header) {
		ArrayList<String> matchedHeaders = this.headers.get(header);
		return (matchedHeaders == null) ? null : new HashEnum<String>(matchedHeaders.iterator());
	}

	@Override
	public int getIntHeader(String header) {
		ArrayList<String> matchedHeaders = this.headers.get(header);
		return Integer.parseInt(matchedHeaders.get(0));
	}

	@Override
	public String getMethod() {
		return this.method;
	}

	@Override
	public String getPathInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestedSessionId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServletPath() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (create && this.session == null) {
			this.session = new myHttpSession();
		}
		return this.session;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		// TODO Auto-generated method stub
		return false;
	}
	
	//
	// Deprecated or Do Not Implement Methods
	//

	@Override
	public ServletInputStream getInputStream() throws IOException {
		return null; // DO NOT IMPLEMENT
	}
	
	@Override
	public Enumeration getLocales() {
		return null; // DO NOT IMPLEMENT
	}

	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {
		return null; // DO NOT IMPLEMENT
	}
	
	@Override
	public String getPathTranslated() {
		return null; // DO NOT IMPLEMENT
	}
	
	@Override
	public Principal getUserPrincipal() {
		return null; // DO NOT IMPLEMENT
	}
	
	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false; // DEPRECATED
	}
	
	@Override
	public boolean isUserInRole(String arg0) {
		return false; // DO NOT IMPLEMENT
	}

}
