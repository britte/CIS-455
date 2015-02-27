package edu.upenn.cis.cis455.webserver;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.Date;
import java.util.Stack;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

public class HttpResponse {
	
	static final Logger logger = Logger.getLogger(HttpResponse.class);	
	
	private ThreadPool pool;
	private Socket client;
	private HttpRequest req;
	private StringBuilder sb = new StringBuilder();
	private PrintWriter writer;
	private BufferedOutputStream out;
	private String path;
	private boolean head;
	private String contentType = "text/html";
	private long contentLength = -1;
	
	public HttpResponse(HttpRequest req, String root, ThreadPool pool) throws IOException {
		logger.info("Generating response ...");
		
		this.pool = pool;
		
		// Generate output stream
		this.client = req.getClient();
		this.out = new BufferedOutputStream(client.getOutputStream());
		this.writer = new PrintWriter(this.out);


		this.req = req;
		this.path = req.getPath();
		
		String method = req.getMethod();
		
		this.head = req.getMethod().equals("HEAD");
		
		if (!req.hostComplient()) {
			sendResponseHeader(HttpServletResponse.SC_BAD_REQUEST);
		} else {
			switch (path) {
				case "/control": 
					controlPage();
					break;
				case "/shutdown":
					shutdownPage();
					break;
				default:
					pathResponse(cleanPath(URLDecoder.decode(req.getPath()), root));	
			}
		}	
		
		// Close all streams
		close();
	}	
	
	private String cleanPath(String path, String root) throws IOException {
		String[] dirs = root.concat("/" + path).replaceAll("//", "/").split("/");
		Stack<String> s = new Stack<String>();
		StringBuilder cleanBuilder = new StringBuilder();
		
		// Prevent backtracking above the root folder
		for (String subdir: dirs) {
			if (subdir.isEmpty() || subdir.equals(".")) {
				
			} else if (subdir.equals("..")) {
				if (!s.isEmpty()) s.pop();
			} else {
				s.push(subdir);
			} 
		}
		
		while (!s.isEmpty()) {
			cleanBuilder.insert(0, "/" + s.pop());
		}
		
		// set trailing slash as it was in original path
		String cleanPath = cleanBuilder.toString();
		if (!cleanPath.startsWith(root)) {
			sendResponseHeader(HttpServletResponse.SC_FORBIDDEN);
			return null;
		} else {
			return (path.endsWith("/")) ? cleanPath + "/" : cleanPath; 
		}
	}
	
	/*
	 * Search for file or directory and delegate parsing appropriately
	 */
	private void pathResponse(String fPath) throws IOException {
		File f = new File(fPath);
		
		if (f.exists()) {
			String ifMod = req.ifMod();
			String ifUnmod = req.ifUnmod();
			if (ifMod != null && ReqRes.modifiedSince(ifMod, f)) {
				sendResponseHeader(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			} else if (ifUnmod != null && !ReqRes.modifiedSince(ifUnmod, f)) {
				sendResponseHeader(HttpServletResponse.SC_PRECONDITION_FAILED);
				return;
			} else {
				if (f.isFile()) sendFile(f);
				else if (f.isDirectory()) sendDirectory(f.listFiles());	
			}
		} else {
			sendResponseHeader(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	/*
	 * Generate file response
	 */
	private void sendFile(File f) throws IOException {
		logger.info(String.format("Sending file at path %s", path));
		
		String MIME = Files.probeContentType(f.toPath());
		this.contentType = MIME;
		this.contentLength = f.length();
		
		sendResponseHeader(HttpServletResponse.SC_OK);
		
		if (!head) {
			FileInputStream fins = new FileInputStream(path);
			byte[] buffer = new byte[1024];
			int read;
			
			// Read the file into the output in buffered chunks
			while ((read = fins.read(buffer)) != -1){
				out.write(buffer, 0, read);
			}			
			
			fins.close();
		}
	}
	
	/*
	 * Generate directory response
	 */
	private void sendDirectory(File fs[]) throws IOException {
		logger.info(String.format("Sending directory at path %s", path));
		
		sb.append(ReqRes.htmlStart);
		sb.append(String.format("Directory at: %s", path));
		sb.append("<ul>");
		for (int i = 0; i < fs.length; i++) {
			File f = fs[i];
			String displayName = f.getName() + (f.isDirectory() ? "/" : "");
			if (!path.endsWith("/")) path = path.concat("/");
			sb.append(String.format("<li><a href='%s%s'>%s</a></li>", path, f.getName(), displayName));
		}
		sb.append("</ul>");
		sb.append(ReqRes.htmlEnd);
		
		sendResponse(HttpServletResponse.SC_OK);
	}
	
	/*
	 * Generate shutdown response and shutdown pool
	 */
	private void shutdownPage() throws IOException {
		logger.info("Begining shutdown...");
		
		sb.append(ReqRes.htmlStart);
		sb.append("Server successfully shut down.");
		sb.append(ReqRes.htmlEnd);
		
		sendResponse(HttpServletResponse.SC_OK);
		
		pool.shutdown();
	}
	
	private void controlPage() throws IOException {
		
		sb.append(ReqRes.htmlStart);
		
		// Page header
		sb.append("<h1>Control Page</h1>");
		sb.append("Elizabeth Britton: britte <br/>");
		
		// Thread pool status
		sb.append("<ul>");
		for (PoolThread t : pool.getThreads()) {
			sb.append(String.format("<li>%s: %s</li>", t.getName(), t.getStatus()));
		}
		sb.append("</ul><br/>");
		
		// Shutdown button
		sb.append("<a href='/shutdown'>Shutdown</a>");
		
		sb.append(ReqRes.htmlEnd);
		
		sendResponse(HttpServletResponse.SC_OK);
	}
	
	/*
	 * Generate response based on status code.
	 */
	private void sendResponseHeader(int statusCode) throws IOException {
		writer.print(ReqRes.generateStatus(statusCode));
		writer.print(ReqRes.generateHeader("Server", "HttpServer/1.0"));
		writer.print(ReqRes.generateHeader("Date", ReqRes.formatDate(new Date())));
		writer.print(ReqRes.generateHeader("Last-Modified", ReqRes.formatDate(ReqRes.getLastModified())));
		writer.print(ReqRes.generateHeader("Connection", "close"));
		writer.print(ReqRes.generateHeader("Content-Type", this.contentType));
		writer.print(ReqRes.generateHeader("Content-Length", Long.toString(this.contentLength)));
		writer.print("\r\n");
		this.writer.flush();
	}
	
	private void sendResponse(int statusCode) throws IOException {
		this.contentLength = sb.length();
		sendResponseHeader(statusCode);
		if (!head) {
			writer.print(sb.toString());
			this.writer.flush();
		}
	}
		
	private void close() throws IOException {
		out.close();
		client.close();
	}
}
