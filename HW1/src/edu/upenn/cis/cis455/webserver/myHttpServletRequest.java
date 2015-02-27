package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.security.Principal;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class myHttpServletRequest implements HttpServletRequest {

	private Socket client;
	private BufferedReader in;
	private myServletContext context;
	private Logger logger = Logger.getLogger(HttpRequest.class);
	
	private String method; 
	
	private String reqPath; 
	private String scheme = "http";
	private String serverName = ""; 
	private int serverPort = 80; 
	private String contextPath = ""; // Single app => no context path 
	private String servletPath = "";
	private String pathInfo = ""; 
	private String queryString = ""; 
	
	private String version; 
	
	private HashMap<String,ArrayList<String>> headers = new HashMap<String,ArrayList<String>>(); 
	private ArrayList<Cookie> cookies = new ArrayList<Cookie>();

	private String characterEncoding = "ISO-8859-1";
	private int contentLength; 
	private String contentType; 
	private Locale locale = null;
	
	private String body;
	
	private HashMap<String,Object> attributes = new HashMap<String,Object>();
	private HashMap<String,String[]> params = new HashMap<String,String[]>();
	
	private String reqSessionId;
	private myHttpSession session;
	private HashMap<String, myHttpSession> sessions = new HashMap<String,myHttpSession>();
	private myHttpServletResponse res;
	
	public myHttpServletRequest(HttpRequest req, myHttpServletResponse res, myServletContext context, String servletPath) throws IOException {
		this.client = req.getClient();
		this.in = req.getReader();
//		this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.context = context;
		this.res = res;
		this.sessions = (HashMap<String, myHttpSession>) context.getAttribute("Sessions");
		this.servletPath = (servletPath.startsWith("/")) ? servletPath : "/" + servletPath;
		
		this.method = req.getMethod();
		this.reqPath = req.getPath();
		this.version = req.getVersion();
		this.headers = req.getHeaders();
		this.body = req.getBody();
		
		parsePath(reqPath);
		parseCookies();
		parseParams(this.queryString);
		if (this.method.equals("POST") && this.getHeader("Content-Type").equals("application/x-www-form-urlencoded")) {
			parseParams(this.body.toString());
		}
		
	}
		
	protected void parseCookies() {
		ArrayList<String> values = this.headers.get("Cookie");
		if (values != null && values.size() > 0) {
			for (String v : values) {
				String[] cookies = v.split(";");
				if (cookies.length == 0) return;
				for (String cStr : cookies) {
					Cookie c = ReqRes.parseCookieHeader(cStr.trim());
					if (c == null) continue;
					if (c.getName().equalsIgnoreCase("jsessionid")) {
						this.setSession(c.getValue());
					}
					this.cookies.add(c);
				}	
			}	
		}
	}
	
	protected void parseParams(String source) {
		if (!source.isEmpty()) {
			String[] paramPairs = source.split("&");
			for (int i = 0; i < paramPairs.length; i ++) {
				String p = paramPairs[i];
				String[] nameValue = p.split("=");
				if (nameValue.length == 2) {
					this.params.put(nameValue[0].trim(), new String[] {nameValue[1].trim()});
				}
			}
		}
	}
			
	protected void parsePath(String path) {
		// If the request path is NOT absolute, append the host header to create absolute path
		if (path.indexOf("http://") == -1) {
			String host = this.getHeader("Host");
			path = (host != null) ? host + path : path;
		}
		String url = path;
		// format: scheme://domain[:port]/path[?query][#fragment_id]
		if (url.indexOf("#") != -1) {
			url = url.split("#")[0];
		} 
		// format: scheme://domain[:port]/path[?query]
		if (url.indexOf("?") != -1) {
			String[] urlQuery = url.split("\\?");
			url = urlQuery[0];
			int i = urlQuery[1].indexOf("#");
			this.queryString = (i > -1) ? urlQuery[1].substring(0, i): urlQuery[1];
		}
		// format: scheme://domain[:port]/path
		if (url.indexOf("://") != -1) {
			// We return a scheme of http by default
			url = url.split("\\?")[1];
		}
		// format: domain[:port]/path
		if (url.indexOf("/") != -1) {
			String[] domainPath = url.split("/", 2);
			url = domainPath[0];
			this.pathInfo = "/" + domainPath[0].replaceFirst(this.servletPath, "");
		}
		// format: domain[:port]
		if (url.indexOf(":") != -1) {
			String[] domainPort = url.split(":");
			this.serverName = domainPort[0];
			try {
				this.serverPort = Integer.parseInt(domainPort[1]);
			} catch (NumberFormatException e) {
				// give an ill formated request response
			}
		}
	}
	
	//
	// ServletResponse Methods
	//
	
	@Override
	public Object getAttribute(String attr) {
		return this.attributes.get(attr);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return new IterEnumeration<String>(this.attributes.keySet().iterator());
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
		return this.client.getLocalAddress().getHostAddress();
	}

	@Override
	public String getLocalName() {
		return this.client.getLocalAddress().getHostName();
	}

	@Override
	public int getLocalPort() {
		return this.client.getLocalPort();
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
		return new IterEnumeration<String>(this.params.keySet().iterator());
	}

	@Override
	public String[] getParameterValues(String param) {
		return this.params.get(param);
	}

	@Override
	public String getProtocol() {
		return this.version; // HTTP/1.1
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return this.in;
	}

	@Override
	public String getRemoteAddr() {
		return this.client.getInetAddress().getHostAddress();
	}

	@Override
	public String getRemoteHost() {
		return this.client.getInetAddress().getHostName();
	}

	@Override
	public int getRemotePort() {
		return this.client.getPort();
	}

	@Override
	public String getScheme() {
		return this.scheme;
	}

	@Override
	public String getServerName() {
		return this.serverName;
	}

	@Override
	public int getServerPort() {
		return this.serverPort;
	}

	@Override
	public boolean isSecure() {
		// We are only dealing with http requests
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
		// "For servlets in the default (root) context, this method returns ''"
		return this.contextPath;
	}

	@Override
	public Cookie[] getCookies() {
		return this.cookies.toArray(new Cookie[this.cookies.size()]);
	}

	@Override
	public long getDateHeader(String header) {
		ArrayList<String> values = this.headers.get(header);
		if (values == null) throw new IllegalArgumentException();
		try {
			Date d = ReqRes.parseDate(values.get(0));
			return d.getTime();
		} catch (ParseException e) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String getHeader(String header) {
		ArrayList<String> matchedHeaders = this.headers.get(header);
		return (matchedHeaders == null) ? null : matchedHeaders.get(0);
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		return new IterEnumeration<String> (this.headers.keySet().iterator());
	}

	@Override
	public Enumeration<String> getHeaders(String header) {
		ArrayList<String> matchedHeaders = this.headers.get(header);
		return (matchedHeaders == null) ? null : new IterEnumeration<String>(matchedHeaders.iterator());
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
		try {
			return URLDecoder.decode(this.pathInfo, this.characterEncoding);
		} catch (UnsupportedEncodingException e) {
			// TODO
			return null;
		}
	}

	@Override
	public String getQueryString() {
		try {
			return URLDecoder.decode(this.queryString, this.characterEncoding);
		} catch (UnsupportedEncodingException e) {
			// TODO
			return null;
		}
	}

	@Override
	public String getRemoteUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRequestURI() {
		// from the protocol name up to the query string
		String reqPath = this.reqPath;
		if (reqPath.indexOf("http://") != -1) {
			reqPath = reqPath.replace("http://", "");
		}
		if (reqPath.indexOf("?") != -1) {
			reqPath = reqPath.split("\\?")[0];
		}
		return reqPath;
	}

	@Override
	public StringBuffer getRequestURL() {
		// TODO Auto-generated method stub
		// INCLUDES protocol, server name, port number, and server path
		// DOES NOT INCLUDE query string
		return new StringBuffer("http://" + this.serverName + ":" + this.serverPort + this.servletPath + this.pathInfo);
	}

	@Override
	public String getRequestedSessionId() {
		return this.reqSessionId;
	}

	@Override
	public String getServletPath() {
		return this.servletPath;
	}

	@Override
	public HttpSession getSession() {
		return this.session;
	}

	@Override
	public HttpSession getSession(boolean create) {
		if (create && this.session == null) {
			this.session = new myHttpSession(context);
			// Set session cookie
			Cookie c = new Cookie("jsessionid", this.session.getId());
			c.setDomain("");
			c.setPath("/test"); //TODO
			res.addCookie(c);
			// TODO: include expiration date??
		}
		return this.session;
	}
	
	private void setSession(String id) {
		this.reqSessionId = id;
		// Set session to requestedSession, or new session if 
		// the requested session does not exist in Sessions list
		this.session = this.sessions.get(id);
		if (this.session == null || !this.session.isValid()) getSession(true);
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		// We are handling all sessions with cookies 
		// so if a reqSessionId exists it is from a cookie
		return this.reqSessionId != null; 
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		// We are handling all sessions with cookies
		return false;
	}

	@Override
	public boolean isRequestedSessionIdValid() {
		if (this.reqSessionId == null) return false;
		myHttpSession s = this.sessions.get(this.reqSessionId);
		return s.isValid();
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
	public String getRealPath(String arg0) {
		return null; // DEPRECATED
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
