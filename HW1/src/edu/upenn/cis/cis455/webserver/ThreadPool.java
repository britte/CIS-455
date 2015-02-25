package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.Vector;

public class ThreadPool {
	
	private Vector<HttpRequest> q = new Vector<HttpRequest>();
	private ArrayList<PoolThread> threads = new ArrayList<PoolThread>();
	private ServerContext context;
	protected boolean running = true;
	
	public ThreadPool(ServerContext context) {
		this.context = context;
	};
		
	public void addThread(PoolThread t) {
		this.threads.add(t);
		t.setPool(this);
	};
	
	public ArrayList<PoolThread> getThreads() {
		return this.threads;
	};
	
	public ServerContext getContext() {
		return this.context;
	}
	
	public void start() {
		for (PoolThread thread : threads) {
			thread.start();
		}
	};
	
	public void shutdown() {
		this.running = false;
		this.context.servlet.destroy();
	};
}
