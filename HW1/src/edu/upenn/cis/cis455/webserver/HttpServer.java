package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class HttpServer {
	
	static final Logger logger = Logger.getLogger(HttpServer.class);	
	
	private static ServerSocket server;
	private static int port;
	private static String root;
	private static String servletPath;
	private static ArrayList<String> options;

	public static void run() throws IOException {
		server = new ServerSocket(port);
		
		logger.info(String.format("Server running on port %d", port));
		
		// Create a request queue 
		Vector<HttpRequest> reqQ = new Vector<HttpRequest>();
		int capacity = 25;
		int requestThreads = 1;
		int responseThreads = 20;
		
		ThreadPool pool = new ThreadPool();
		
		for (int i = 0; i < requestThreads; i++) {
			// Create a RequestThread to listen for server requests
			pool.addThread(new RequestThread(reqQ, capacity, server, root));
			
		}
		
		for (int i = 0; i < responseThreads; i++) {
			// Create a thread pool of ResponseThreads to respond to requests
			pool.addThread(new ResponseThread(reqQ, root));
		}
		
		pool.start();
		
		while (pool.running){}
		
		System.exit(0);
	}
	
	public static void main(String args[]) throws Exception {
		switch (args.length) {
			case 0: 
				System.out.println("Elizabeth Britton: britte");
				break;
			case 1:
				throw new IllegalArgumentException("Usage: <port> <root directory> [<servlet path>]");
			case 2: 
				logger.info("Attempting to start server...");
				
				try {
					port = Integer.parseInt(args[0]);
					root = args[1];
					run();
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Port must be valid integer");
				}
				break;		
			case 3: 
				logger.info("Attempting to start servlet...");
				
				try {
					port = Integer.parseInt(args[0]);
					root = args[1];
					servletPath = args[2];
					
					logger.info("Looking for servlet " + servletPath);
					XmlParser parser = new XmlParser(servletPath);
					parser.readFile();
					
					myServletConfig config = parser.getServletConfig();
					
//					run();
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Port must be valid integer");
				}
				break;		
			default:
				try {
					port = Integer.parseInt(args[0]);
					root = args[1];
					servletPath = args[2];
					options = new ArrayList<String>();
					for (int i = 3; i < args.length; i++) {
						options.add(args[i]);
					}
					run();
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Port must be valid integer");
				}
				break;
		}
	}
}
