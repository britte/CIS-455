package edu.upenn.cis.cis455.webserver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ReqRes {

	final static String DateFormat = "EEE, MMM d, yyyy hh:mm:ss z";
	
	final static Date parseDate(String date) throws ParseException {
		SimpleDateFormat formattedTime = new SimpleDateFormat(DateFormat);
		formattedTime.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		return formattedTime.parse(date);
	}
	
}
