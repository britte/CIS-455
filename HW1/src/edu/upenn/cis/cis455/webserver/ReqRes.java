package edu.upenn.cis.cis455.webserver;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

public class ReqRes {

	// Date Time Helpers
	
	final static String DateFormat = "EEE, MMM d, yyyy hh:mm:ss z";
	
	final static Date parseDate(String date) throws ParseException {
		SimpleDateFormat formattedTime = new SimpleDateFormat(DateFormat);
		formattedTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return formattedTime.parse(date);
	}
	
	final static String formatDate(long date) {
		SimpleDateFormat formattedTime = new SimpleDateFormat(DateFormat);
		formattedTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return formattedTime.format(date);
	}
	
	final static String formatDate(Date date) {
		SimpleDateFormat formattedTime = new SimpleDateFormat(DateFormat);
		formattedTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return formattedTime.format(date);
	}
	
	final static Date getLastModified() {
		File f = new File(HttpServer.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		return new Date(f.lastModified());
	}
	
	// Response Status Helpers
	
	final static String statusMsg(int statusCode) {
		switch (statusCode) {
			case 100: return "Continue";
			case 200: return "OK"; 
			case 304: return "Not Modified";
			case 400: return "Bad Request";
			case 401: return "Unauthorized";
			case 404: return "Not Found";
			case 408: return "Request Timeout";
			case 412: return "Precondition Failed";
			case 501: return "Not Implemented";
			default: return null;
		}
	}
	
	final static String htmlStart = "<!DOCTYPE html><html><body>";
	final static String htmlEnd = "</body></html>";
	
	final static String generateStatus(int statusCode) {
		// TODO get version 
		String message = ReqRes.statusMsg(statusCode);
		return String.format("HTTP/1.1 %d %s\r\n", statusCode, message);
	}
	
	final static String generateHeader(String header, ArrayList<String> values) {
		String strValues = values.toString();
		strValues = strValues.substring(1, strValues.length() - 1);
		return String.format("%s: %s\r\n", header, strValues);
	}
	
	final static String generateHeader(String header, String value) {
		return String.format("%s: %s\r\n", header, value);
	}
		
	final static String generateCookieHeader(Cookie c) {
		// TODO expires, httponly ? 
		// Set-Cookie: name=value(; "attr"=value)*
		StringBuilder s = new StringBuilder();
		s.append(c.getName() + "=" + c.getValue() + ";");
		s.append("Max-Age=" + Integer.toString(c.getMaxAge()) + ";");
		s.append("Domain=" + c.getDomain() + ";");
		s.append("Path=" + c.getPath() + ";");
		if (c.getSecure()) s.append("Secure");
		return s.toString();
	}
	
	// Request Parsing Helpers
	final static Cookie parseCookieHeader(String cookie) {
		String[] c = cookie.split("=");
		return new Cookie(c[0], c[1]);
	}
	
	final static boolean modifiedSince(String modString, File f) {
		String formats[] = {"EEE, d MMM yyyy hh:mm:ss z","EEEE, d-MMM-yy hh:mm:ss z","EEE MMM d hh:mm:ss yyyy"};
		for (int i = 0; i < formats.length; i++) {
			SimpleDateFormat formatted = new SimpleDateFormat(formats[i]);
			try {
				Date d = formatted.parse(modString);
				if (new Date(f.lastModified()).after(d)) return true;
			} catch (ParseException e) {
				continue;
			}
		}	
		return false;
	}	
}
