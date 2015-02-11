package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Vector;

import org.apache.log4j.Logger;

public class HttpServer {
	
	static final Logger logger = Logger.getLogger(HttpServer.class);	
	
	private static ServerSocket server;
	private static int port;
	private static String root;

	public static void main(String args[]) throws IOException {
		switch (args.length) {
			case 0: 
				System.out.println("Elizabeth Britton: britte");
				break;
			case 2: 
				logger.info("Attempting to start server...");
				
				try {
					port = Integer.parseInt(args[0]);
					root = args[1];
					server = new ServerSocket(port);
					logger.info(String.format("Server running on port %d", port));
					
					// Create a request queue 
					Vector<HttpRequest> reqQ = new Vector<HttpRequest>();
					int capacity = 5;
					int requestThreads = 1;
					int responseThreads = 5;
					
					ThreadPool pool = new ThreadPool();
					
					for (int i = 0; i < requestThreads; i++) {
						// Create a RequestThread to listen for server requests
						pool.addThread(new RequestThread(reqQ, capacity, server));
						
					}
					
					for (int i = 0; i < responseThreads; i++) {
						// Create a thread pool of ResponseThreads to respond to requests
						pool.addThread(new ResponseThread(reqQ, root));
					}
					
					pool.start();
					
					while (pool.running){}
					
					System.exit(0);
					
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Port must be valid integer");
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Port must be in bounds (0-65535 inclusive)");
				}
				break;
			default:
				throw new IllegalArgumentException("Expected arguments: [port] [root directory]");
		}
	}
}
