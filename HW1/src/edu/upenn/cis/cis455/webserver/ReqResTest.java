package edu.upenn.cis.cis455.webserver;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

public class ReqResTest {

	@Test
	public void generateHeaderTest() {
		String header = "Header";
		ArrayList<String> values = new ArrayList<String>();
		values.add("A");
		
		assertEquals("", "Header: A\r\n", ReqRes.generateHeader(header, values));

		values.add("B");
		assertEquals("", "Header: A, B\r\n", ReqRes.generateHeader(header, values));

	}
	
	@Test
	public void generateStatusTest() {
		
		assertEquals("", "HTTP/1.1 200 OK\r\n", ReqRes.generateStatus(myHttpServletResponse.SC_OK));
		assertEquals("", "HTTP/1.1 304 Not Modified\r\n", ReqRes.generateStatus(myHttpServletResponse.SC_NOT_MODIFIED));
		assertEquals("", "HTTP/1.1 400 Bad Request\r\n", ReqRes.generateStatus(myHttpServletResponse.SC_BAD_REQUEST));
		assertEquals("", "HTTP/1.1 404 Not Found\r\n", ReqRes.generateStatus(myHttpServletResponse.SC_NOT_FOUND));

	}

}
