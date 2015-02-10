package edu.upenn.cis.cis455.webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.log4j.Logger;

public class HttpResponse {
	
	static final Logger logger = Logger.getLogger(HttpResponse.class);	
	
	private Socket client;
	private DataOutputStream out;
	private String path;
	
	final static String htmlStart = "<!DOCTYPE html><html><body>";
	final static String htmlEnd = "</body></html>";
	
	final static String OK = "HTTP/1.1 200 OK" + "\r\n";
	final static String FileNotFound = "HTTP/1.1 404 Not Found" + "\r\n";
	
	public HttpResponse(HttpRequest req, String root) throws IOException {
		logger.info("Generating response ...");
		// Generate output stream
		this.client = req.getClient();
		this.out = new DataOutputStream(client.getOutputStream());
		
		// Search for file and handle appropriately
		this.path = root + req.getPath();
		File f = new File(path);
		
		if (f.exists()) {
			logger.info("File or directory found");
			out.writeBytes(OK);
			out.writeBytes("\r\n");
			if (f.isFile()) sendFile();
			else if (f.isDirectory()) sendDirectory(f.list());
		} else {
			logger.info("File or directory not found");
			out.writeBytes(FileNotFound);
		}
		
		// Close all streams
		close();
	}	
	
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
	
	private void sendDirectory(String contents[]) throws IOException {
		logger.info(String.format("Sending directory at path %s", path));
		
		out.writeBytes(htmlStart);
		out.writeBytes(String.format("Directory at: %s", path));
		out.writeBytes("<ul>");
		
		for (int i = 0; i < contents.length; i++) {
			out.writeBytes("<li>");
			out.writeBytes(contents[i] + "\r\n");
			out.writeBytes("</li>");
		}
		
		out.writeBytes("</ul>");
		out.writeBytes(htmlEnd);
	}
	
	private void close() throws IOException {
		out.close();
		client.close();
	}
}
