package edu.upenn.cis.cis455.webserver;

import java.util.Enumeration;
import java.util.Iterator;

public class IterEnumeration<T> implements Enumeration<T> {
	
	Iterator<T> i;

	public IterEnumeration(Iterator<T> i) { this.i = i; }
	
	@Override
	public boolean hasMoreElements() { return i.hasNext(); }

	@Override
	public T nextElement() { return i.next(); }
	
}