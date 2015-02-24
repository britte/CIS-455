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
	private Logger logger = Logger.getLogger(HttpRequest.class);
	
	private String method; 
	
	private String reqPath; 
	private String scheme = "http";
	private String serverName; // TODO
	private int serverPort = 80; 
	private String contextPath = ""; 
	private String servletPath; // TODO
	private String pathInfo; // TODO
	private String queryString; 
	
	private String version; 
	
	private HashMap<String,ArrayList<String>> headers = new HashMap<String,ArrayList<String>>(); 
	private ArrayList<Cookie> cookies = new ArrayList<Cookie>();
	private String lastSeenHeader;
	private String characterEncoding = "ISO-8859-1";
	private int contentLength; // TODO
	private String contentType; // TODO
	private Locale locale = null;
	
	private StringBuilder body;
	
	private HashMap<String,Object> attributes;
	private HashMap<String,String[]> params;
	private myHttpSession session;
	
	public myHttpServletRequest(Socket client, myHttpSession session) throws IOException {
		this.client = client;
		this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.session = session;
		
		// Build request
		String line;
		boolean statusDigested = false;
		boolean startBody = false;
		
		while ((line = in.readLine()) != null) {
			if (!statusDigested) {
				parseStatusLine(line); // TODO throw 400 if bad
				statusDigested = true;
			} else if (!line.isEmpty()){
				if (startBody) parseBody(line);
				parseHeader(line); // TODO throw 400 if bad
			} else if (line.isEmpty()){
				if (!startBody) startBody = true;
				else break;
			}
		}
	}
	
	//
	// Parser Methods
	//
	
	private boolean parseStatusLine(String statusLine) {
		
		String request[] = statusLine.split(" ");
		if (request.length != 3) {
			logger.error("Invalid request: status line misformatted");
			return false;
		}
		
		this.method = request[0];
		
		// Check that header is a valid method
		if (!(method.equals("GET") || method.equals("HEAD"))) {
			logger.error("Invalid request: unsupported method");
			return false;
		}
		
		this.reqPath = request[1]; // TODO handle absolute path
		this.version = request[2];
		return true;
	}
	
	private void parseHeader(String header) {
		String components[] = header.split(":");
		
		if (components.length > 1) { // line format = Header: value 
			if (components[0] == "Cookie") {
				String[] cookies = components[1].split(";");
				for (String c : cookies) {
					this.cookies.add(ReqRes.parseCookieHeader(c.trim()));
				}
			} else {
				ArrayList<String> values = this.headers.get(components[0]);
				if (values == null) values = new ArrayList<String>();
				
				values.add(components[1].trim());
				this.headers.put(components[0], values);	
			}
			this.lastSeenHeader = components[0];
		} else { // line format = value (continued from last line with header)
			ArrayList<String> values = headers.get(lastSeenHeader);
			values.add(components[0].trim());
			headers.put(lastSeenHeader, values);
		}
	}
	
	private void parseBody(String body) {
		this.body.append(body);
	}
	
	private boolean isAbsoluteUrl(String path) {
		return path.indexOf("http://") != -1;
	}
	
	private void parsePath(String path) {
		String url = path;
		// format: scheme://domain[:port]/path[?query][#fragment_id]
		if (url.indexOf("#") != -1) {
			url = url.split("#")[0];
		} 
		// format: scheme://domain[:port]/path[?query]
		if (url.indexOf("?") != -1) {
			String[] urlQuery = url.split("\\?");
			url = urlQuery[0];
			this.queryString = urlQuery[1];
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
			this.pathInfo = domainPath[1]; // TODO: split out the servlet path
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
		// TODO Auto-generated method stub
		return null;
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
		return (create && this.session == null) ? new myHttpSession() : this.session;
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
