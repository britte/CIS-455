package edu.upenn.cis.cis455.webserver;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.net.Socket;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

public class myHttpServletResponse implements HttpServletResponse {

	private Socket client;
	private int bufferSize = 512; // default for BufferedOutputStream 
	private PrintWriter writer;
	
	private HashMap<String,ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	private int statusCode = this.SC_OK;
	private ArrayList<Cookie> cookies = new ArrayList<Cookie>();
	
	private String contentType = "text/html";
	private int contentLength = 0;
	private String characterEncoding = "ISO-8859-1";
	private Locale locale;
	
	public myHttpServletResponse(Socket client) throws IOException {
		this.writer = new PrintWriter(new BufferedOutputStream(client.getOutputStream(), this.bufferSize));
	}
	
	
	private class myPrintWriter extends PrintWriter {
		
		// TODO: idk more mods probs
		
		private boolean headersSent = false;
		
		public myPrintWriter(Writer writer) { super(writer); }
		
		@Override
		public void write(int c) {
			if (!headersSent) {
				generateHeaders();
				headersSent = true;
			}
			super.write(c);
		}
		
	}
	
	//
	// ServletResponse Methods
	//
	
	@Override
	public void flushBuffer() throws IOException {
		this.writer.flush(); // confirm
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
		return new myPrintWriter(this.writer); // TODO https://piazza.com/class/i4vn9s3ilfg2hs?cid=408
	}

	@Override
	public boolean isCommitted() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		try {
			this.headers = new HashMap<String, ArrayList<String>>();
			this.statusCode = this.SC_OK;
			this.writer = new PrintWriter(new BufferedOutputStream(this.client.getOutputStream(), this.bufferSize));
		} catch (IOException e) {
			// TODO 
		}
	}

	@Override
	public void resetBuffer() {
		try {
			this.writer = new PrintWriter(new BufferedOutputStream(this.client.getOutputStream(), this.bufferSize));
		} catch (IOException e) {
			// TODO 
		}
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
	public String encodeRedirectURL(String arg0) {
		// TODO "includes the logic to determine whether the session ID needs to be encoded in the URL.
		return null;
	}

	@Override
	public String encodeURL(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sendRedirect(String arg0) throws IOException {
		// TODO Auto-generated method stub
		// Should be run through encodeRedirectURL

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
	
	private void generateHeaders() {
		writer.print(ReqRes.generateStatus(this.statusCode)); // Status header
		// Server header
		if (this.headers.containsKey("Server")) {
			writer.print(ReqRes.generateHeader("Server", this.headers.get("Server")));
		} else {
			writer.print(ReqRes.generateHeader("Server", "HttpServer/1.0"));
		}
		
		// Date header
		if (this.headers.containsKey("Date")) {
			writer.print(ReqRes.generateHeader("Date", this.headers.get("Date")));
		} else {
			writer.print(ReqRes.generateHeader("Date", ReqRes.formatDate(new Date())));
		}
		
		// Last Modified header
		if (this.headers.containsKey("Last-Modified")) {
			writer.print(ReqRes.generateHeader("Last-Modified", this.headers.get("Last-Modified")));
		} else {
			writer.print(ReqRes.generateHeader("Server", ReqRes.formatDate(ReqRes.getLastModified())));
		}
		// TODO Connection Close header
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
