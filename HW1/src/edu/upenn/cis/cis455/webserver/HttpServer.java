package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

public class HttpServer {
	
	static final Logger logger = Logger.getLogger(HttpServer.class);	
	
	private static ServerContext context = new ServerContext();

	public static void run() throws IOException {
		ServerSocket server = new ServerSocket(context.port);
		
		logger.info(String.format("Server running on port %d", context.port));
		
		// Create a request queue 
		Vector<HttpRequest> reqQ = new Vector<HttpRequest>();
		int capacity = 25;
		int requestThreads = 1;
		int responseThreads = 20;
		
		ThreadPool pool = new ThreadPool(context);
		
		RequestThread req = new RequestThread(reqQ, capacity, server);
		
		for (int i = 0; i < responseThreads; i++) {
			// Create a thread pool of ResponseThreads to respond to requests
			pool.addThread(new ResponseThread(reqQ));
		}
		
		pool.start();
		req.start();
		
		while (pool.running){}
		System.exit(0);
	}
	
	public static void createServlet(String className) {
		try {
			Class servletClass = Class.forName(className);
			context.servlet = (HttpServlet) servletClass.newInstance();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
		}
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
					context.port = Integer.parseInt(args[0]);
					context.root = args[1];
					run();
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Port must be valid integer");
				}
				break;		
			case 3: 
				logger.info("Attempting to start servlet...");
				
				try {
					context.port = Integer.parseInt(args[0]);
					context.root = args[1];
					context.servletPath = args[2];
					
					logger.info("Parsing servlet " + context.servletPath);
					XmlParser parser = new XmlParser(context.root, context.servletPath);
					parser.readFile();
					
					context.servletContext = parser.getServletContext();
					context.servletContext.setAttribute("Sessions", new HashMap<String, myHttpSession>());
					context.servletConfig = parser.getServletConfig();
					context.servletMappings = parser.servletMappings;
					createServlet(parser.servletClass);
					if (parser.loadOnStart) {
						logger.info("Starting servlet " + parser.servletName);
						context.servlet.init(context.servletConfig);
						context.isInit = true;
					}		
					logger.info(context.servletPath + " parsed");
					run();
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Port must be valid integer");
				}
				break;		
			default:
				throw new IllegalArgumentException("Usage: <port> <root directory> [<servlet path>]");
		}
	}
}
