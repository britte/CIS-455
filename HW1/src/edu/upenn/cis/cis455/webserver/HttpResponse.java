package edu.upenn.cis.cis455.webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

public class HttpResponse {
	
	static final Logger logger = Logger.getLogger(HttpResponse.class);	
	
	private ThreadPool pool;
	private Socket client;
	private DataOutputStream out;
	private String path;
	
	final static String htmlStart = "<!DOCTYPE html><html><body>";
	final static String htmlEnd = "</body></html>";
	
	final static String OK = "HTTP/1.1 200 OK\r\n";
	final static String FileNotFound = "HTTP/1.1 404 Not Found\r\n";
	
	final static String ServerHeader = "server: HttpServer/1.0";
	final static String LastModifiedHeader = "Last-Modified: Tue, 11 Feb 2015 2:37:00 GMT"; 
	final static String ConnCloseHeader = "Connection: close\r\n";
	
	public HttpResponse(HttpRequest req, String root, ThreadPool pool) throws IOException {
		logger.info("Generating response ...");
		
		this.pool = pool;
		
		// Generate output stream
		this.client = req.getClient();
		this.out = new DataOutputStream(client.getOutputStream());
		
		this.path = req.getPath();
		
		switch (path) {
			case "/control": 
				okResponseHead();
				controlResponse();
				break;
			case "/shutdown":
				okResponseHead();
				shutdownResponse();
				break;
			default:
				this.path = root + this.path;
				pathResponse();	
		}
		
		// Close all streams
		close();
	}	
	
	/*
	 * Search for file or directory and delegate parsing appropriately
	 */
	private void pathResponse() throws IOException {
		File f = new File(path);
		
		if (f.exists()) {
			okResponseHead();
			if (f.isFile()) sendFile();
			else if (f.isDirectory()) sendDirectory(f.list());
		} else {
			notFoundResponseHead();
		}
	}
	
	/*
	 * Generate file response
	 */
	private void sendFile() throws IOException {
		logger.info(String.format("Sending file at path %s", path));
		FileInputStream fins = new FileInputStream(path);
		byte[] buffer = new byte[1024];
		int read;
		
		// Read the file into the output in buffered chunks
		while ((read = fins.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}			
		
		fins.close();
	}
	
	/*
	 * Generate directory response
	 */
	private void sendDirectory(String contents[]) throws IOException {
		logger.info(String.format("Sending directory at path %s", path));
		
		out.writeBytes(htmlStart);
		out.writeBytes(String.format("Directory at: %s", path));
		out.writeBytes("<ul>");
		for (int i = 0; i < contents.length; i++) {
			out.writeBytes(String.format("<li><a href='%s'>%s</a></li>", contents[i], contents[i]));
		}
		out.writeBytes("</ul>");
		out.writeBytes(htmlEnd);
	}
	
	/*
	 * Generate shutdown response and shutdown pool
	 */
	private void shutdownResponse() throws IOException {
		logger.info("Begining shutdown...");
		
		out.writeBytes(htmlStart);
		out.writeBytes("Server successfully shut down.");
		out.writeBytes(htmlEnd);
		
		pool.shutdown();
	}
	
	private void controlResponse() throws IOException {
		out.writeBytes(htmlStart);
		
		// Page header
		out.writeBytes("<h1>Control Page</h1>");
		out.writeBytes("Elizabeth Britton: britte <br/>");
		
		// Thread pool status
		out.writeBytes("<ul>");
		for (PoolThread t : pool.getThreads()) {
			out.writeBytes(String.format("<li>%s: %s</li>", t.getName(), t.getStatus()));
		}
		out.writeBytes("</ul><br/>");
		
		// Shutdown button
		out.writeBytes("<a href='/shutdown'>Shutdown</a>");
		
		out.writeBytes(htmlEnd);
	}
	
	private void close() throws IOException {
		out.close();
		client.close();
	}
	
	/*
	 * Helpers to print status and header lines 
	 */
	
	private void okResponseHead() throws IOException {
		out.writeBytes(OK);
		out.writeBytes(ServerHeader);
		out.writeBytes(LastModifiedHeader);
		out.writeBytes(ConnCloseHeader);
		out.writeBytes("\r\n");
	}
	
	private void notFoundResponseHead() throws IOException {
		logger.info(String.format("File not found at path %s", path));
		
		out.writeBytes(FileNotFound);
		out.writeBytes(ServerHeader);
		out.writeBytes(LastModifiedHeader);
		out.writeBytes(ConnCloseHeader);
		out.writeBytes("\r\n");
	}
}
