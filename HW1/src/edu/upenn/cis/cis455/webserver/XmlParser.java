package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser {
	
	private static Logger logger = Logger.getLogger(HttpServer.class);	
	
	protected String root;
	protected String path;
	protected String displayName;
	protected boolean loadOnStart = false;
//	protected HashMap<String,String> servlets; // multiple servlet model
	protected String servletName;
	protected String servletClass;
	protected HashMap<String,String> servletMappings;
	protected HashMap<String,String> contextParams;
	protected HashMap<String, HashMap<String,String>> servletParams;
	
	public XmlParser(String root, String path) {
		this.root = root;
		this.path = path;
	}
	
	
	/*
	 * Handler class for parsing out servlet xml files.Passed into a SAXParser.
	 */
	static class Handler extends DefaultHandler {
		private int m_state = 0;
		private String m_displayName;
		private String m_servletName;
		private String m_servletClass;
		private String m_paramName;
		private String m_mappingName;
		private boolean m_load_on_start = false;
//		HashMap<String,String> m_servlets = new HashMap<String,String>();
		HashMap<String,String> m_servletMappings = new HashMap<String,String>();
		HashMap<String,String> m_contextParams = new HashMap<String,String>();
		HashMap<String,HashMap<String,String>> m_servletParams = new HashMap<String,HashMap<String,String>>();
		
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				m_state = (m_state == 0) ? 1 : 50;
			} else if (qName.compareTo("servlet-class") == 0) {
				m_state = 2;
			} else if (qName.compareTo("context-param") == 0) {
				m_state = 3;
			} else if (qName.compareTo("init-param") == 0) {
				m_state = 4;
			} else if (qName.compareTo("param-name") == 0) {
				m_state = (m_state == 3) ? 30 : 40;
			} else if (qName.compareTo("param-value") == 0) {
				m_state = (m_state == 30) ? 31 : 41;
			} else if (qName.compareTo("servlet-mapping") == 0) {
				m_state = 5;
			} else if (qName.compareTo("url-pattern") == 0) {
				m_state = 51;
			} else if (qName.compareTo("display-name") == 0) {
				m_state = 6;
			} else if (qName.compareTo("load-on-startup") == 0) {
				m_state = 7;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (m_state == 1) {
				m_servletName = value;
				m_state = 0;
			} else if (m_state == 2) {
//				m_servlet.put(m_servletName, value);
				m_servletClass = value;
				m_state = 0;
			} else if (m_state == 30 || m_state == 40) {
				if (value.trim().length() > 0) {
					m_paramName = value;
				}
			} else if (m_state == 31) {
				if (m_paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				m_contextParams.put(m_paramName, value);
				m_paramName = null;
				m_state = 0;
			} else if (m_state == 41) {
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
			} else if (m_state == 50) {
				if (value.trim().length() > 0) {
					m_mappingName = value;
				}
			} else if (m_state == 51) {
				if (m_mappingName == null) {
					System.err.println("Servlet mapping value '" + value + "' without url");
					System.exit(-1);
				}
				m_servletMappings.put(m_mappingName, value);
				m_mappingName = null;
				m_state = 0;
			} else if (m_state == 6) {
				m_displayName = value;
				m_state = 0;
			} else if (m_state == 7) {
				// Since we only support one servlet, load order is moot
				m_load_on_start = true;
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
		
		this.servletName = h.m_servletName;
		this.servletClass = h.m_servletClass;
		this.displayName = h.m_displayName;
		this.contextParams = h.m_contextParams;
		this.servletParams = h.m_servletParams;
		this.servletMappings = h.m_servletMappings;
		this.loadOnStart = h.m_load_on_start;
	}
		
	public myServletContext getServletContext() {
		return new myServletContext(this.path, this.displayName, this.contextParams);
	}
	
	
	public myServletConfig getServletConfig(){
		if (this.servletName == null) return null;
		else {
			myServletContext context = getServletContext();
			HashMap<String,String> params = this.servletParams.get(this.servletName);
			return new myServletConfig(this.servletName, context, params);
		}
	}
	
	public myServletConfig getServletConfig(String servletName){
		if (this.servletName == null) return null;
		else {
			myServletContext context = getServletContext();
			HashMap<String,String> params = this.servletParams.get(this.servletName);
			return new myServletConfig(this.servletName, context, params);
		}
	}
}


