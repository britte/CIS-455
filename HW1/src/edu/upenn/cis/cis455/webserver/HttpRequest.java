																																								package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;

public class HttpRequest {
	
	private Socket client;
	private BufferedReader in;
	
	// Status line components
	private String method;
	private String path;
	private String version;
	
	private HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	private String lastSeenHeader = "";
	
	// Special header values
	private boolean hasHost = false;
	private boolean ifMod = false;
	private boolean ifUnmod = false;
	
	private StringBuilder body = new StringBuilder();
	
	static final Logger logger = Logger.getLogger(HttpRequest.class);
	
	public HttpRequest(Socket client) throws IOException {
		
		this.client = client;
		in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		
		readStatusLine();
		readHeaders();
		readBody();
		logger.info("Request recieved");
	}
	
	//
	// Parser Methods
	//
	
	private void readStatusLine() throws IOException {
		logger.info("Recieving request...");
		String statusLine = this.in.readLine();
		String request[] = statusLine.split(" ");
		if (request.length != 3) {
			// TODO: throw error and send response
			logger.error("Invalid request: request line misformatted");
			return;
		}
		
		this.method = request[0];
		
		// Check that header is a valid method
		if (!(method.equals("GET") || method.equals("HEAD") || method.equals("POST"))) {
			// TODO: throw error and send response
			logger.error("Invalid request: unrecognized method");
			return;
		}
		
		this.path = request[1];
		this.version = request[2];
	}
	
	private void readHeaders() throws IOException {
		
		String headerLine = in.readLine();
		
		while(!headerLine.isEmpty()) {
			String components[] = headerLine.split(":", 2);
			if (components.length > 1) { // line format = Header: value 
				ArrayList<String> values = this.headers.get(components[0]);
				if (values == null) values = new ArrayList<String>();
				
				values.add(components[1].trim());
				this.headers.put(components[0], values);
				headerCheck(components[0]);
				this.lastSeenHeader = components[0];
			} else { // line format = value (continued from last line with header)
				ArrayList<String> values = headers.get(lastSeenHeader);
				values.add(components[0].trim());
				headers.put(lastSeenHeader, values);
			}
			headerLine = in.readLine();
		}
	}
	
	private void readBody() throws IOException {
		ArrayList<String> contentLengthVals = this.headers.get("Content-Length");
		String contentLength = (contentLengthVals == null) ? null : contentLengthVals.get(0);
		try {
			int len = Integer.parseInt(contentLength);
			for (int i = len; i > 0; i--) {
				char c = (char) this.in.read();
				this.body.append(c);
			}
		} catch (NumberFormatException e) {
			// TODO throw bad format
		}
	}
	
	
	/*
	 * Check for special headers which will require certain
	 * behavior in the response handling
	 */
	private void headerCheck(String name) {
		switch (name) {
			case "Host": this.hasHost = true; break;
			case "If-Modified-Since": this.ifMod  = true; break;
			case "If-Unmodified-Since": this.ifUnmod = true; break;
		}
	}
	
	public String getBody() { return body.toString(); }
	public Socket getClient() { return client; }
	public BufferedReader getReader() { return in; }
	public String getMethod() { return method; }
	public String getPath() { return path; }
	public String getVersion() { return version; }
	
	public HashMap<String, ArrayList<String>> getHeaders() { return this.headers; }
	
	public boolean hostComplient() { return (!version.equals("1.1") || hasHost); }
	public String ifMod() { return (ifMod ? headers.get("If-Modified-Since").get(0) : null); }
	public String ifUnmod() { return (ifUnmod ? headers.get("If-Unmodified-Since").get(0) : null); }
}
