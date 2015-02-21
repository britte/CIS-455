package edu.upenn.cis.cis455.webserver;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class HttpResponse {
	
	static final Logger logger = Logger.getLogger(HttpResponse.class);	
	
	private ThreadPool pool;
	private Socket client;
	private HttpRequest req;
	private DataOutputStream out;
	private String path;
	private boolean get;
	
	final static String htmlStart = "<!DOCTYPE html><html><body>";
	final static String htmlEnd = "</body></html>";
	
	final static String Continue = "HTTP/1.1 100 Continue\r\n";
	final static String OK = "HTTP/1.1 200 OK\r\n";
	final static String NotModified = "HTTP/1.1 304 Not Modified\r\n";
	final static String BadRequest = "HTTP/1.1 400 Bad Request\r\n";
	final static String FileNotFound = "HTTP/1.1 404 Not Found\r\n";
	final static String PreconditionFail = "HTTP/1.1 412 Precondition Failed\r\n";
	final static String NotImplemented = "HTTP/1.1 501 Not Implemented\r\n";
	
	final static String DateFormat = "EEE, MMM d, yyyy hh:mm:ss z";
	final static String ServerHeader = generateHeader("Server","HttpServer/1.0");
	final static String LastModifiedHeader = generateHeader("Last-Modified", generateTimestamp(getLastModified())); 
	final static String DateHeader = generateHeader("Date", generateTimestamp(new Date()));
	final static String ConnCloseHeader = generateHeader("Connection","close");
	
	public HttpResponse(HttpRequest req, String root, ThreadPool pool) throws IOException {
		logger.info("Generating response ...");
		
		this.pool = pool;
		
		// Generate output stream
		this.client = req.getClient();
		this.out = new DataOutputStream(client.getOutputStream());
		
		this.req = req;
		this.path = req.getPath();
		
		String method = req.getMethod();
		
		this.get = req.getMethod().equals("GET");
		
		if (!req.hostComplient()) {
			noHostResponseHead(); 
		} else {
			switch (path) {
				case "/control": 
					okResponseHead();
					out.writeBytes(generateHeader("Content-Type", "text/html"));
					out.writeBytes("\r\n");
					if (get) controlResponse();
					break;
				case "/shutdown":
					okResponseHead();
					out.writeBytes(generateHeader("Conent-Type", "text/html"));
					out.writeBytes("\r\n");
					if (get) shutdownResponse();
					break;
				default:
					this.path = root + this.path;
					pathResponse();	
			}
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
			String ifMod = req.ifMod();
			String ifUnmod = req.ifUnmod();
			if (ifMod != null && modifiedSince(ifMod, f)) {
				modifiedSinceResponse();
				return;
			} else if (ifUnmod != null && !modifiedSince(ifUnmod, f)) {
				unmodifiedSinceResponse();
				return;
			} else {
				okResponseHead();
				if (get) {
					if (f.isFile()) sendFile(f);
					else if (f.isDirectory()) sendDirectory(f.listFiles());	
				}
			}
		} else {
			notFoundResponseHead();
		}
	}
	
	/*
	 * Generate file response
	 */
	private void sendFile(File f) throws IOException {
		logger.info(String.format("Sending file at path %s", path));
		
		String MIME = Files.probeContentType(f.toPath());
		out.writeBytes(generateHeader("Content-Type", MIME));
		out.writeBytes("\r\n");
		
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
	private void sendDirectory(File fs[]) throws IOException {
		logger.info(String.format("Sending directory at path %s", path));
		
		out.writeBytes(generateHeader("Content-Type", "text/html"));
		out.writeBytes("\r\n");
		
		out.writeBytes(htmlStart);
		out.writeBytes(String.format("Directory at: %s", path));
		out.writeBytes("<ul>");
		for (int i = 0; i < fs.length; i++) {
			File f = fs[i];
			String displayName = f.getName() + (f.isDirectory() ? "/" : "");
			out.writeBytes(String.format("<li><a href='%s'>%s</a></li>", f.getName(), displayName));
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
		out.writeBytes(DateHeader);
		out.writeBytes(LastModifiedHeader);
		out.writeBytes(ConnCloseHeader);
	}
	
	private void notFoundResponseHead() throws IOException {
		logger.info(String.format("File not found at path %s", path));
		
		out.writeBytes(FileNotFound);
		out.writeBytes(ServerHeader);
		out.writeBytes(DateHeader);
		out.writeBytes(LastModifiedHeader);
		out.writeBytes(ConnCloseHeader);
		out.writeBytes("\r\n");
	}
	
	private void noHostResponseHead() throws IOException {
		logger.info(String.format("File not found at path %s", path));
		
		out.writeBytes(BadRequest);
		out.writeBytes(ServerHeader);
		out.writeBytes(DateHeader);
		out.writeBytes(LastModifiedHeader);
		out.writeBytes(ConnCloseHeader);
		out.writeBytes("\r\n");
	}
	
	private void modifiedSinceResponse() throws IOException {
		out.writeBytes(NotModified);
		out.writeBytes(DateHeader);
		out.writeBytes("\r\n");
	}
	
	private void unmodifiedSinceResponse() throws IOException {
		out.writeBytes(PreconditionFail);
		out.writeBytes("\r\n");
	}
	
	private void notImplementedResponse() throws IOException {
		out.writeBytes(NotImplemented);
		out.writeBytes("\r\n");
	}
	
	private static String generateHeader(String header, String value) {
		return String.format("%s: %s\r\n", header, value);
	}
	
	private static String generateTimestamp(Date date) {
		SimpleDateFormat formattedTime = new SimpleDateFormat(DateFormat);
		formattedTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return formattedTime.format(date);
	}
	
	private static Date getLastModified() {
		File f = new File(HttpServer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		return new Date(f.lastModified());
	}
	
	private static boolean modifiedSince(String modString, File f) {
		String formats[] = {"EEE, d MMM yyyy hh:mm:ss z","EEEE, d-MMM-yy hh:mm:ss z","EEE MMM d hh:mm:ss yyyy"};
		for (int i = 0; i < formats.length; i++) {
			SimpleDateFormat formatted = new SimpleDateFormat(formats[i]);
			try {
				Date d = formatted.parse(modString);
				if (new Date(f.lastModified()).after(d)) return true;
			} catch (ParseException e) {
				continue;
			}
		}	
		return false;
	}
}
