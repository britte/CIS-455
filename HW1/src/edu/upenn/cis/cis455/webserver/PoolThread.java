package edu.upenn.cis.cis455.webserver;

public class PoolThread extends Thread {

	private ThreadPool pool;
	protected boolean stopped = false;
	
	public void setPool(ThreadPool pool) {
		this.pool = pool;
	}
	
	public void shutdown() {
		this.stopped = true;
	}
}
