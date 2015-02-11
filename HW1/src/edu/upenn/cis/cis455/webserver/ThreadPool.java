package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.Vector;

import java.net.ServerSocket;

public class ThreadPool {
	
	private Vector<HttpRequest> q = new Vector<HttpRequest>();
	private ArrayList<PoolThread> threads = new ArrayList<PoolThread>();
	private boolean running = true;

	public ThreadPool() {};
	
	public void addThread(PoolThread t) {
		threads.add(t);
		t.setPool(this);
	};
	
	public ArrayList<PoolThread> getThreads() {
		return threads;
	};
	
	public void start() {
		for (PoolThread thread : threads) {
			thread.start();
		}
	};
	
	public void shutdown() {
		this.running = false;
		for(PoolThread thread : threads) {
			thread.shutdown();
		}
	};
}
