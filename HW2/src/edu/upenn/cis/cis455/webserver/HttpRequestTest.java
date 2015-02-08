package edu.upenn.cis.cis455.webserver;

import static org.junit.Assert.*;

import org.junit.Test;

public class HttpRequestTest {

	@Test
	public void test() {
		String req = "GET test/path/here HTTP/1.0 \r\n"+
					 "Header1: value \r\n"+
					 "\r\n"+
					 "body"; // GET request
		HttpRequest hr = new HttpRequest(req);
	}

}
