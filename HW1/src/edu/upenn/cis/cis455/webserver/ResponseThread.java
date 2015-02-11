package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.util.Vector;

import org.apache.log4j.Logger;

public class ResponseThread extends PoolThread {
	
	private ThreadPool pool;
	
	private Vector<HttpRequest> q;
	private String root;
	
	static final Logger logger = Logger.getLogger(RequestThread.class);	
	
	public ResponseThread(Vector<HttpRequest> q, String root) {
		this.q = q;
		this.root = root;
	}
	
	/**
	 * Method to read from the queue.
	 * @return - element read from queue
	 * @throws InterruptedException
	 */
	private HttpRequest readFromQueue() throws InterruptedException {
		while (q.isEmpty()) {
			// If the queue is empty, we push the current thread to waiting state. Way to avoid polling.
			synchronized (q) {
				logger.info(String.format("Queue is currently empty. %s waiting...", this.getName()));
				q.wait();
			}
		}

		// Otherwise consume element and notify waiting producer
		synchronized (q) {
			q.notifyAll();
			return q.remove(0);
		}
	}
	
	public void run() {
		while(!stopped) {
			try {
				HttpRequest req = readFromQueue();
				logger.info(String.format("%s consumed %s from shared queue", this.getName(), req));
				
				HttpResponse res = new HttpResponse(req, root, pool);
				
				Thread.sleep(100); // why?
			} catch (InterruptedException ex) {
				logger.error("Interrupt Exception in Response Thread");
			} catch (IOException e) {
				logger.error("Error responding to client");
			}
		}
	}
}
