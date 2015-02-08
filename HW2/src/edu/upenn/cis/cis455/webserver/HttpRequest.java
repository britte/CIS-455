package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.omg.CORBA.Request;

public class HttpRequest {
	
	static final Logger logger = Logger.getLogger(HttpRequest.class);	
	
	private String method;
	private String path;
	private String version;
	private boolean statusDigested = false;
	
	private ArrayList<Header> headers = new ArrayList<Header>();
	
	private String body;
	
	public HttpRequest() {}
	
	private class Header {
		private String name;
		private String value;
		
		public Header(String name, String value) {
			this.name = name;
			this.value = value.trim();
		}
		
		public void addValue(String value) {
			this.value.concat(value);
		}
	}
	
	public void parseStatusLine(String statusLine) {
		logger.info("Recieving request...");
		logger.info(statusLine);
		
		String request[] = statusLine.split(" ");
		if (request.length != 3) {
			logger.error("Invalid request: request line misformatted");
		}
		method = request[0];
		
		// Check that header is a valid method
		if (!(method.equals("GET") || method.equals("HEAD"))) {
			logger.error("Invalid request: unrecognized method");
		}
		
		path = request[1];
		version = request[2];
		statusDigested = true;
	}
	
	public void parseHeader(String header){
		logger.info(header);
		String components[] = header.split(":");
		
		if (components.length > 1) {
			headers.add(new Header(components[0], components[1]));
		} else {
			headers.get(headers.size() - 1).addValue(components[0]);
		}
	}
	
	public boolean statusDigested() { return statusDigested; }
	
	public String getPath() { return path; }
	public String getMethod() { return method; }
	public String getVersion() { return version; }
	
}
