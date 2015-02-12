																																								package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

public class HttpRequest {
	
	private Socket client;
	
	// Parse method components
	private String method;
	private String path;
	private String version;
	
	private HashMap<String, String> headers = new HashMap<String, String>();
	private String lastHeader = "";
	private boolean hasHost = false;
	private boolean ifMod = false;
	private boolean ifUnmod = false;
	
	private StringBuilder body;
	
	static final Logger logger = Logger.getLogger(HttpRequest.class);
	
	public HttpRequest(Socket client) throws IOException {
		
		this.client = client;
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		String line;
		
		// Build Request
		boolean statusDigested = false;
		boolean startBody = false;
		
		while ((line = in.readLine()) != null) {
			if (!statusDigested) {
				parseStatusLine(line);
				statusDigested = true;
			} else if (!line.isEmpty()){
//				if (startBody) parseBody(line);
				parseHeader(line);
			} else if (line.isEmpty()){
//				if (!startBody) startBody = true;
				break;
			}
		}
	}
	
	private void parseStatusLine(String statusLine) {
		logger.info("Recieving request...");
//		logger.info(statusLine);
		
		String request[] = statusLine.split(" ");
		if (request.length != 3) {
			logger.error("Invalid request: request line misformatted");
			return;
		}
		method = request[0];
		
		// Check that header is a valid method
		if (!(method.equals("GET") || method.equals("HEAD"))) {
			logger.error("Invalid request: unrecognized method");
			return;
		}
		
		path = request[1];
		version = request[2];
	}
	
	private void parseHeader(String header) {
//		logger.info(header);
		String components[] = header.split(":");
		
		if (components.length > 1) {
			headers.put(components[0], components[1].trim());
			lastHeader = components[0];
			headerCheck(components[0]);
		} else {
			String values = headers.get(lastHeader);
			headers.put(components[0], values + "," + components[1].trim());
		}
	}
	
	private void headerCheck(String h) {
		switch (h) {
			case "Host": this.hasHost = true;
			case "If-Modified-Since": this.ifMod  = true;
			case "If-Unmodified-Since": this.ifUnmod = true;
		}
	}
	
	private void parseBody(String body) {
		this.body.append(body);
	}
	
	public Socket getClient() { return client; }
	public String getPath() { return path; }
	public String getMethod() { return method; }
	public String getVersion() { return version; }
	
	public boolean hostComplient() { return (!version.equals("1.1") || hasHost); }
	public String ifMod() { return (ifMod ? headers.get("If-Modified-Since") : null); }
	public String ifUnmod() { return (ifUnmod ? headers.get("If-Unmodified-Since") : null); }
}
