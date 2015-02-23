package edu.upenn.cis.cis455.webserver;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class myHttpSession implements HttpSession {
	
	HashMap<String,Object> attributes;
	ServletContext context;
	Date creationTime;
	int maxInactiveInterval = 0;
	boolean isNew = true;
	boolean valid = true;
	
	public myHttpSession() {
		attributes = new HashMap<String,Object>();
		creationTime = new Date();
	}

	@Override
	public Object getAttribute(String attr) {
		if (!valid) throw new IllegalStateException();
		return this.attributes.get(attr);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		if (!valid) throw new IllegalStateException();
		return new HashEnum<String>(this.attributes.keySet().iterator());
	}

	@Override
	public long getCreationTime() {
		if (!valid) throw new IllegalStateException();
		return this.creationTime.getTime();
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastAccessedTime() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMaxInactiveInterval() {
		if (!valid) throw new IllegalStateException();
		return this.maxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		if (!valid) throw new IllegalStateException();
		return this.context;
	}

	@Override
	public void invalidate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isNew() {
		if (!valid) throw new IllegalStateException();
		return this.isNew;
	}

	@Override
	public void removeAttribute(String attr) {
		if (!valid) throw new IllegalStateException();
		this.attributes.remove(attr);
	}

	@Override
	public void setAttribute(String attrName, Object attrVal) {
		if (!valid) throw new IllegalStateException();
		this.attributes.put(attrName, attrVal);
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
