package edu.upenn.cis.cis455.webserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

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

				// Determine if this request is standards or if it is for a servlet
				String reqPath = req.getPath(); // TODO: handle absolute path
				if (reqPath.indexOf("/") == 0) reqPath = reqPath.replaceFirst("/", "");
				ServerContext c = this.pool.getContext();
				String[] matchPath = isServletPath(reqPath, c.servlets); // [mapPath, servletPath]
				if (matchPath != null) {
					myServletWrapper s = c.servlets.get(matchPath[0]); 
					if (!s.isInit()) s.configInit();
					
					myHttpServletResponse myRes = new myHttpServletResponse(req.getClient());
					myHttpServletRequest myReq = new myHttpServletRequest(req, myRes, c.servletContext, matchPath[1]);
					try {
						s.servlet.service(myReq, myRes);
						if (!myRes.isCommitted()) myRes.flushBuffer();
						myRes.close();
					} catch (Exception e) {
						myRes.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
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
	
	private String[] isServletPath(String reqPath, HashMap<String,myServletWrapper> pathMappings) {
		for (String map : pathMappings.keySet()) {
			//TODO ???
			// ex: /foo/* matches /foo?param=1
			// ex: /foo/bop* matches /foo/bop/boom
			// ex: /foo matches /foo
			if (map.endsWith("/*")) {
				String prefix = map.substring(0, map.lastIndexOf("/*"));
				if (reqPath.startsWith(prefix)) return new String[]{map, prefix};
			} else if (map.endsWith("*")) {
				String prefix = map.substring(0, map.lastIndexOf("*"));
				if (reqPath.startsWith(prefix)) return new String[]{map, prefix};
			} else if (reqPath.equals(map)) {
				return new String[]{map, map};
			}
		}
		return null;
	}
}
