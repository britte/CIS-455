package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

public class ResponseThread extends PoolThread {
	
	private Vector<HttpRequest> q;
	private HttpRequest req;
	
	static final Logger logger = Logger.getLogger(RequestThread.class);	
	
	public ResponseThread(Vector<HttpRequest> q) {
		this.q = q;
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
		while(pool.running) {
			try {
				this.req = readFromQueue();
				logger.info(String.format("%s consumed %s from shared queue", this.getName(), req));

				// Determine if this request is standards
				// or if it is for a servlet
				String reqPath = req.getPath(); // TODO: handle absolute path
				if (reqPath.indexOf("/") == 0) reqPath = reqPath.replaceFirst("/", "");
				ServerContext c = this.pool.getContext();
				if (c.servletContext != null && isServletPath(reqPath, c.servletMappings)) {
					if (!c.isInit) c.servlet.init(c.servletConfig);
					myHttpServletRequest myReq = new myHttpServletRequest(req, c.servletContext, c.servlet.getServletName());
					myHttpServletResponse myRes = new myHttpServletResponse(req.getClient());
					try {
						c.servlet.service(myReq, myRes);
						if (!myRes.isCommitted()) myRes.flushBuffer();
						myRes.close();
					} catch (Exception e) {
						// TODO: handle expection response
					}
				} else {
					HttpResponse res = new HttpResponse(req, c.root, this.pool);
				}
				req = null;
			} catch (InterruptedException ex) {
				logger.error("Interrupt Exception in Response Thread");
			} catch (IOException e) {
				logger.error("Error responding to client");
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		interrupt();
		logger.info(String.format("%s shutting down", this.getName()));
	}
	
	public String getStatus() {
		return this.getState().toString() + " " + (this.req != null ? this.req.getPath() : "");
	}
	
	private boolean isServletPath(String path, HashMap<String,String> pathMappings) {
		for (String map : pathMappings.keySet()) {
			if (map.endsWith("/*") && map.startsWith(path)) { // Path mapping (special case)
				if (path.startsWith(map.substring(0, map.lastIndexOf("/*")))) return true;
			} else if (map.endsWith("*")) { // Path mapping (special case)
				if (path.startsWith(map.substring(0, map.lastIndexOf("*")))) return true;
			} else { // Exact mapping
				if (path.equals(map)) return true;
			}
		}
		return false;
	}
}
