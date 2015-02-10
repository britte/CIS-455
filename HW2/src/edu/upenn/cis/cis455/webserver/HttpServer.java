package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
//					root = args[1];
					root = "/home/cis455/Documents";
					server = new ServerSocket(port);
					logger.info(String.format("Server running on port %d", port));
					
					// Create a request queue 
					Vector<HttpRequest> reqQ = new Vector<HttpRequest>();
					int capacity = 5;
					
					// Create a RequestThread to listen for server requests
					RequestThread reqThread = new RequestThread(reqQ, capacity, server);
					// Create a thread pool of ResponseThreads to respond to requests
					ResponseThread resThread = new ResponseThread(reqQ, root);
					
					reqThread.start();
					resThread.start();
					
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
	
	private String cleanPath(String path){
		return "";
	}
}
