package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser {
	
	private Logger logger = Logger.getLogger(HttpServer.class);	
	
	private String path;
	private HashMap<String,String> servlets;
	private HashMap<String,String> contextParams;
	private HashMap<String, HashMap<String,String>> servletParams;
	
	public XmlParser(String path) {
		this.path = path;
	}
	
	
	/*
	 * Handler class for parsing out servlet xml files.Passed into a SAXParser.
	 */
	static class Handler extends DefaultHandler {
		private int m_state = 0; // state representing servlet element
		private String m_servletName;
		private String m_paramName;
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
//		HashMap<String,String> m_servletMappings = new HashMap
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				// check if you are within a context-param element
				// or a servlet's init-param element
				m_state = (m_state == 3) ? 30 : 40;
			} else if (qName.compareTo("param-value") == 0) {
				// check if you are within a context-param element
				// or a servlet's init-param element
				m_state = (m_state == 30) ? 31 : 41;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) { // servlet-name
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) { // servlet-class
				m_servlets.put(m_servletName, value);
				m_state = 0; 
			} else if (m_state == 30 || m_state == 40) { // init-param || context-param > param-name
				m_paramName = value;
			} else if (m_state == 31) { // context-param > param-value
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 41) { // init-param > param-value
				if (m_paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = m_servletParams.get(m_servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					m_servletParams.put(m_servletName, p);
				}
				p.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			}
		}
	}
	
	public void readFile() throws Exception {
		Handler h = new Handler();
		File file = new File(path);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		this.servlets = h.m_servlets;
		this.contextParams = h.m_contextParams;
		this.servletParams = h.m_servletParams;
	}
	
	public myServletContext getServletContext() {
		return new myServletContext(this.contextParams);
	}
	
	
	public myServletConfig getServletConfig(){
		if (this.servlets.isEmpty()) return null;
		else {
			String servletName = this.servlets.keySet().iterator().next();
			myServletContext context = getServletContext();
			HashMap<String,String> params = this.servletParams.get(servletName);
			return new myServletConfig(servletName, context, params);
		}
	}
	
	public myServletConfig getServletConfig(String servletName){
		String servlet = this.servlets.get(servletName);
		if (servlet == null) return null;
		else {
			myServletContext context = getServletContext();
			HashMap<String,String> params = this.servletParams.get(servletName);
			return new myServletConfig(servletName, context, params);
		}
	}
}


