package edu.upenn.cis.cis455.webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class HttpResponse {
	
	static final Logger logger = Logger.getLogger(HttpResponse.class);	
	
	private DataOutputStream out;
	
	final static String htmlStart = "<!DOCTYPE html><html><body>";
	final static String htmlEnd = "</body></html>";
	
	final static String OK = "HTTP/1.1 200 OK" + "\r\n";
	final static String FileNotFound = "HTTP/1.1 404 Not Found" + "\r\n";
	
	public HttpResponse(DataOutputStream out) {
		this.out = out;
	}	
	
	public void createResponse(String path){
		logger.info("Generating response ...");
		logger.info(path);
		File f = new File(path);
		if (f.exists()) {
			logger.info("File exists");
			try {
				out.writeBytes(OK);
				out.writeBytes("\r\n");
				if (f.isFile()) sendFile(path);
				else if (f.isDirectory()) sendDirectory(f.list());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			logger.info("File does not exist");
			try {
				out.writeBytes(FileNotFound);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void sendFile(String path) throws IOException {
		logger.info(String.format("Sending file at path %s", path));
		FileInputStream fins = new FileInputStream(path);
		byte[] buffer = new byte[1024];
		int read;
		
		// Read the file into the output in buffered chunks
		while ((read = fins.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}			
		
		fins.close();
		out.close();
	}
	
	public void sendDirectory(String contents[]) throws IOException {
		for (int i = 0; i < contents.length; i++) {
			out.writeBytes(contents[i] + "\r\n");
		}
		out.close();
	}
}
