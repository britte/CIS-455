package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParser {
	
	private static Logger logger = Logger.getLogger(HttpServer.class);	
	
	private String root;
	private String appPath;
	private String displayName;
	private HashMap<String,String> servletClasses; 
	private ArrayList<String> loadOnStart;
	private HashMap<String,String> servletMappings;
	private HashMap<String,String> contextParams;
	private HashMap<String, HashMap<String,String>> servletParams;
	
	public XmlParser(String root, String path) {
		this.root = root;
		this.appPath = path;
	}
		
	/*
	 * Handler class for parsing out servlet xml files.Passed into a SAXParser.
	 */
	static class Handler extends DefaultHandler {
		private int m_state = 0;
		private String m_displayName;
		private String m_servletName;
		private String m_paramName;
		private String m_mappingName;
		HashMap<String,String> m_servlets = new HashMap<String,String>();
		ArrayList<String> m_load_on_start = new ArrayList<String>();
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
				m_servlets.put(m_servletName, value);
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
				m_servletMappings.put(value, m_mappingName);
				m_mappingName = null;
				m_state = 0;
			} else if (m_state == 6) {
				m_displayName = value;
				m_state = 0;
			} else if (m_state == 7) {
				// Since we only support one servlet, load order is moot
				m_load_on_start.add(m_servletName);
				m_state = 0;
			}
		}
	}
	
	public void readFile() throws Exception {
		Handler h = new Handler();
		File file = new File(this.appPath); 
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		this.servletClasses = h.m_servlets;
		this.loadOnStart = h.m_load_on_start;
		this.displayName = h.m_displayName;
		this.contextParams = h.m_contextParams;
		this.servletParams = h.m_servletParams;
		this.servletMappings = h.m_servletMappings;
	}
		
	/*
	 * @return map of servlets paths => corresponding HttpServlets
	 * (if no Servlet exists, path maps to null)
	 */
	public HashMap<String,myServletWrapper> getServletMap() { 
		HashMap<String,myServletWrapper> servlets = this.getServlets();
		HashMap<String,myServletWrapper> mappedServlets = new HashMap<String,myServletWrapper>();
		
		// Mappings of form: path => servlet name
		for (Map.Entry<String, String> map: this.servletMappings.entrySet()) {
			mappedServlets.put(map.getKey(), servlets.get(map.getValue()));
		}
		
		return mappedServlets; 
	}
	
	/*
	 * Convert all servlet classes in the app to HttpServelts; initialize when appropriate
	 * @return map of servlets names => corresponding HttpServlets
	 */
	private HashMap<String,myServletWrapper> getServlets() { 
		HashMap<String,myServletWrapper> servlets = new HashMap<String,myServletWrapper>();
		
		for (Map.Entry<String, String> servlet : this.servletClasses.entrySet()) {
			String sName = servlet.getKey();
			String sClass = servlet.getValue();
			try {
				Class c = Class.forName(sClass);
				HttpServlet rawServlet = (HttpServlet) c.newInstance();
				myServletWrapper s = new myServletWrapper (rawServlet, getServletConfig(sName));
				if (this.loadOnStart.contains(sName)) s.configInit();
				servlets.put(servlet.getKey(), s);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
			} catch (ServletException e) {
				// TODO Auto-generated catch block
			}
		}
		
		return servlets; 
	}
	
	public myServletContext getServletContext() {
		return new myServletContext(this.root, this.displayName, this.contextParams);
	}
		
	private myServletConfig getServletConfig(String servletName){
		myServletContext context = getServletContext();
		HashMap<String,String> params = this.servletParams.get(servletName);
		return new myServletConfig(servletName, context, params);
	}
}


