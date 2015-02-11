package edu.upenn.cis.cis455.webserver;

public abstract class PoolThread extends Thread {

	protected ThreadPool pool;
	protected boolean stopped = false;
	
	public void setPool(ThreadPool pool) {
		this.pool = pool;
	};
	
	public String getStatus() {
		return this.getState().toString();
	};
}
