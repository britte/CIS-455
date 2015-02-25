package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Class for Consumer thread.
 */

public class RequestThread extends PoolThread {

	static final Logger logger = Logger.getLogger(RequestThread.class);	
	
	private final Vector<HttpRequest> q;
	private final int capacity;
	private ServerSocket server;
	private Socket client;
	
	public RequestThread(Vector<HttpRequest> q, int capacity, ServerSocket server) {
		this.q = q;
		this.capacity = capacity;
		this.server = server;
	}
	
	/**
	 * Method which pushes a parsed request onto the shared queue.
	 * @param i - item to be added
	 * @throws InterruptedException
	 */
	private void addToQueue(HttpRequest req) throws InterruptedException {
		logger.info("Adding request to queue");
		
		// Wait if the queue is full
		while (q.size() == capacity) {
			// Synchronizing on the sharedQueue to make sure no more than one
			// thread is accessing the queue same time.
			synchronized (q) {
				logger.info("Queue is full!");
				q.wait();
			}
		}

		// Adding element to queue and notifying all waiting consumers
		synchronized (q) {
			q.add(req);
			q.notifyAll();
		}
	}

	public void run() {
		while(true) {
			try {
				client = server.accept();
				logger.info("Connection established");
				HttpRequest req = new HttpRequest(client);
				addToQueue(req);
			} catch (InterruptedException ex) {
				logger.error("Interrupt Exception in Request thread");
			} catch (IOException ex) {
				logger.error("Error reading from client");
			}
		}
	}
}
