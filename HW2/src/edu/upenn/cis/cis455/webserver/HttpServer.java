package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
					
					while (true) {
						Socket client = server.accept();
						logger.info("Connection established ");
						
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						DataOutputStream out = new DataOutputStream(client.getOutputStream());
						
						String line;
						HttpRequest req = new HttpRequest();
						HttpResponse res = new HttpResponse(out);
						
						while ((line = in.readLine()) != null) {
							// Build Request
							if (!req.statusDigested()) {
								req.parseStatusLine(line);
							} else if (!line.isEmpty()){
								req.parseHeader(line);
							} else {
								res.createResponse(root + req.getPath());
								break;
							}
						}
						
						logger.info("Closing client socket.");
						client.close();
					}
					
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Port must be valid integer");
				} catch (IllegalArgumentException e) {
					throw new IllegalArgumentException("Port must be in bounds (0-65535 inclusive)");
				}
			default:
				throw new IllegalArgumentException("Expected arguments: [port] [root directory]");
		}
	}
	
	private String cleanPath(String path){
		return "";
	}
}
