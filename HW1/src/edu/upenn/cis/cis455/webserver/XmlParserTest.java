package edu.upenn.cis.cis455.webserver;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class XmlParserTest {

	@Test
	public void fileTest() {
		String path = "extra/Servlets/testservlet/WEB-INF/web.xml";
		File f = new File(path);
		assertTrue(f.exists());
	}
	
	@Test
	public void parserTest() throws Exception {
		String path = "extra/Servlets/testservlet/WEB-INF/web.xml";
		XmlParser p = new XmlParser("root", path);
		p.readFile();
		
		assertEquals("display name", p.displayName,"Test servlet for CIS455");
		assertEquals("servlets size", p.servlets.size(), 1);
		assertTrue("servlets contents", p.servlets.containsKey("TestServlet"));
		assertEquals("servlets contents", p.servlets.get("TestServlet"), "edu.upenn.cis455.TestServlet");
		assertEquals("servlet mappings size", p.servletMappings.size(), 2);
		assertTrue("servlet mappings 1", p.servletMappings.containsKey("TestServlet"));
		assertTrue("servlet mappings 2", p.servletMappings.containsKey("default"));
		assertEquals("servlets mappings 3", p.servletMappings.get("TestServlet"), "test/*");
		assertEquals("servlets mappings 3", p.servletMappings.get("default"), "*.html");
		assertEquals("no context params", p.contextParams.size(), 0);
	}

}
