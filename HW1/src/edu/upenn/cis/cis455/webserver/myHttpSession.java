package edu.upenn.cis.cis455.webserver;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class myHttpSession implements HttpSession {
	
	HashMap<String,Object> attributes = new HashMap<String,Object>();
	myServletContext context;
	Date creationTime;
	Date lastAccessedTime;
	String id; 
	int maxInactiveInterval = 0;
	boolean isNew = true;
	boolean valid = true;
	
	public myHttpSession(myServletContext context) {
		this.creationTime = new Date();
		this.id = UUID.randomUUID().toString();
		this.context = context;
		HashMap<String, myHttpSession> sessions = (HashMap<String, myHttpSession>) this.context.getAttribute("Sessions");
		sessions.put(id, this);
	}
	
	@Override
	public Object getAttribute(String attr) {
		if (!valid || !isValid()) throw new IllegalStateException();
		return this.attributes.get(attr);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		if (!valid || !isValid()) throw new IllegalStateException();
		return new IterEnumeration<String>(this.attributes.keySet().iterator());
	}

	@Override
	public long getCreationTime() {
		if (!valid || !isValid()) throw new IllegalStateException();
		return this.creationTime.getTime();
	}

	@Override
	public String getId() {
		if (!valid || !isValid()) throw new IllegalStateException();
		return this.id;
	}

	@Override
	public long getLastAccessedTime() {
		if (!valid || !isValid()) throw new IllegalStateException();
		return this.lastAccessedTime.getTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		if (!valid || !isValid()) throw new IllegalStateException();
		return this.maxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		if (!valid || !isValid()) throw new IllegalStateException();
		return this.context;
	}

	@Override
	public void invalidate() {
		// TODO Clarify "unbinds any objects associated"
		this.valid = false;
		// Remove session from context sessions list
		HashMap<String, myHttpSession> sessions = (HashMap<String, myHttpSession>) this.context.getAttribute("Sessions");
		this.context.setAttribute("Sessions", sessions.remove(this.id));
	}

	@Override
	public boolean isNew() {
		if (!valid || !isValid()) throw new IllegalStateException();
		return this.isNew;
	}
	
	public boolean isValid() {
		long now = new Date().getTime() / 1000;
		long lastAccessed = this.lastAccessedTime.getTime() / 1000;
		// Check if the session has expired (if it can expire)
		if (this.maxInactiveInterval > 0 && now - lastAccessed > this.maxInactiveInterval) {
			this.invalidate();
			return false;
		} else {
			return true;
		}
	}

	@Override
	public void removeAttribute(String attr) {
		if (!valid || !isValid()) throw new IllegalStateException();
		this.attributes.remove(attr);
	}

	@Override
	public void setAttribute(String attrName, Object attrVal) {
		if (!valid || !isValid()) throw new IllegalStateException();
		this.attributes.put(attrName, attrVal);
	}

	public void setAccessed() {
		this.isNew = false;
		this.lastAccessedTime = new Date();
	}
	
	@Override
	public void setMaxInactiveInterval(int i) {
		if (!valid) throw new IllegalStateException();
		this.maxInactiveInterval = i;
	}
	
	//
	// Deprecated or Do Not Implement
	//
	
	
	@SuppressWarnings("deprecation")
	@Override
	public HttpSessionContext getSessionContext() {
		return null; // DEPRECATED
	}

	@Override
	public Object getValue(String arg0) {
		return null; // DEPRECATED
	}

	@Override
	public String[] getValueNames() {
		return null; // DEPRECATED
	}
	
	@Override
	public void putValue(String arg0, Object arg1) {
		// DEPRECATED
	}
	
	@Override
	public void removeValue(String arg0) {
		// DEPRECATED
	}
}
