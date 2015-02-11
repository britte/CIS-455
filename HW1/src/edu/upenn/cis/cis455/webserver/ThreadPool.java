package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.Vector;

import java.net.ServerSocket;

public class ThreadPool {
	
	private Vector<HttpRequest> q = new Vector<HttpRequest>();
	private ArrayList<Thread> threads = new ArrayList<Thread>();
	private boolean running = true;

	public ThreadPool() {};
	
	public void addThread(Thread t) {
		threads.add(t);
	};
	
	public void start() {
		for (Thread thread : threads) {
			thread.start();
		}
	}
	
	public void stop() {
		this.running = false;
		for(Thread thread : threads) {
			thread.stop();
		}
	}
	
}
