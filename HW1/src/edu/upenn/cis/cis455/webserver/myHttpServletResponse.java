package edu.upenn.cis.cis455.webserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.net.Socket;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class myHttpServletResponse implements HttpServletResponse {

	private Socket client;
	private int bufferSize = 512; // default for BufferedOutputStream 
	private myPrintWriter writer;
	private boolean isCommitted = false;
	
	private HashMap<String,ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	private int statusCode = this.SC_OK;
	private ArrayList<Cookie> cookies = new ArrayList<Cookie>();
	
	private String contentType = "text/html";
	private int contentLength = 0;
	private String characterEncoding = "ISO-8859-1";
	private Locale locale;
	
	public myHttpServletResponse(Socket client) throws IOException {
		this.writer = new myPrintWriter(new BufferedOutputStream(client.getOutputStream(), this.bufferSize));
	}
	
	
	private class myPrintWriter extends PrintWriter {
		
		private StringBuilder sb = new StringBuilder();
		
		public myPrintWriter(BufferedOutputStream stream) { super(stream); }
		
		@Override
		public void println() {
			sb.append("");
		}
		
		@Override
		public void write(int c) {
			if (isCommitted) super.write(c);
			else this.sb.append(c);
		}
		
		@Override
		public void write(char[] buf) {
			if (isCommitted) super.write(buf);
			else this.sb.append(buf);
		}
		
		@Override
		//TODO
		public void write(char[] buf, int off, int len) {
			if (isCommitted) super.write(buf, off, len);
			else this.sb.append(buf);
		}
		
		@Override
		public void write(String s) {
			if (isCommitted) super.write(s);
			else this.sb.append(s);
		}
		
		@Override
		// TODO
		public void write(String s, int off, int len) {
			if (isCommitted) super.write(s, off, len);
			else this.sb.append(s);
		}
		
		@Override
		public void flush() {
			contentLength = sb.length();
			generateHeaders();
			writer.print("\n\r");
			String s = sb.toString();
			writer.print(sb.toString());
			super.flush();
		}
		
		public void clear() {
			this.sb = new StringBuilder();
		}
		
		private void generateHeaders() {
			isCommitted = true;
			this.print(ReqRes.generateStatus(statusCode)); // Status header
			
			// Set certain headers
			setHeader("Server", "HttpServer/1.0");
			setHeader("Date", ReqRes.formatDate(new Date()));
			setHeader("Last-Modified", ReqRes.formatDate(ReqRes.getLastModified()));
			setHeader("Connection", "close");
			setHeader("Content-Type", contentType);
			setHeader("Content-Length", Integer.toString(contentLength));
			
			// Add headers 
		    Iterator iter = headers.entrySet().iterator();
		    while (iter.hasNext()) {
		        Map.Entry<String,ArrayList<String>> pair = (Map.Entry) iter.next();
		        this.print(ReqRes.generateHeader(pair.getKey(), pair.getValue()));
		        iter.remove(); 
		    }
			    
			// Cookies 
			for (Cookie c : cookies) {
				this.print(ReqRes.generateCookieHeader(c));
			}
		}
	}
	
	public void close() throws IOException {
		writer.close();
		client.close();
	}
	
	//
	// ServletResponse Methods
	//
	
	@Override
	public void flushBuffer() throws IOException {
		this.writer.flush();
	}

	@Override
	public int getBufferSize() {
		return this.bufferSize; 
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding; 
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return this.writer;
	}

	@Override
	public boolean isCommitted() {
		return this.isCommitted;
	}

	@Override
	public void reset() {
		this.headers = new HashMap<String, ArrayList<String>>();
		this.statusCode = this.SC_OK;
		this.writer.clear();
	}

	@Override
	public void resetBuffer() {
		this.writer.clear();
	}
	
	@Override
	public void setBufferSize(int size) {
		this.bufferSize = size;
	}

	@Override
	public void setCharacterEncoding(String arg0) {
		// no action required; getCharacterEncoding returns constant value
	}

	@Override
	public void setContentLength(int length) {
		this.contentLength = length;
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	//
	// HttpServletResponse Methods
	//

	@Override
	public void addCookie(Cookie cookie) {
		this.cookies.add(cookie);
	}

	@Override
	public void addDateHeader(String header, long date) {
		this.addHeader(header, ReqRes.formatDate(date));
	}

	@Override
	public void addHeader(String header, String value) {
		ArrayList<String> values = this.headers.get(header);
		if (values == null) values = new ArrayList<String>();
		values.add(value);
		this.headers.put(header, values);
	}

	@Override
	public void addIntHeader(String header, int i) {
		this.addHeader(header, Integer.toString(i));
	}

	@Override
	public boolean containsHeader(String header) {
		return this.headers.containsKey(header);
	}

	@Override
	public String encodeRedirectURL(String url) {
		// Since we are implementing sessions through cookies, just send url
		return url;
	}

	@Override
	public String encodeURL(String url) {
		// Since we are implementing sessions through cookies, just send url
		return url;
	}

	@Override
	public void sendError(int statusCode) throws IOException {
		setStatus(statusCode);
		this.writer.clear();
		this.writer.flush();
	}

	@Override
	public void sendError(int statusCode, String msg) throws IOException {
		// TODO: If an error-page declaration has been made for the web application corresponding to the
		// status code passed in, it will be served back in preference to the suggested msg parameter.
		setStatus(statusCode);
		this.writer.clear();
		
		this.writer.print(ReqRes.htmlStart);
		this.writer.print(msg);
		this.writer.print(ReqRes.htmlEnd);
		
		this.writer.flush();
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO: Auto-generated method stub
	}

	@Override
	public void setDateHeader(String header, long date) {
		this.setHeader(header, ReqRes.formatDate(date));
	}

	@Override
	public void setHeader(String header, String value) {
		ArrayList<String> values = new ArrayList<String> ();
		values.add(value);
		this.headers.put(header, values);
	}

	@Override
	public void setIntHeader(String header, int i) {
		this.setHeader(header, Integer.toString(i));
	}

	@Override
	public void setStatus(int status) {
		this.statusCode = status;
	}
	
	//
	// Deprecated or Do Not Implement
	//
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		return null; // DO NOT IMPLEMENT
	}

	@Override
	public String encodeRedirectUrl(String arg0) {
		return null; // DEPRECATED
	}

	@Override
	public String encodeUrl(String arg0) {
		return null; // DEPRECATED
	}
	
	@Override
	public void setStatus(int arg0, String arg1) {
		// DEPRECATED
	}
	
}
