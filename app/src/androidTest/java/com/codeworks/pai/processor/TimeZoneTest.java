package com.codeworks.pai.processor;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneTest extends TestCase {
	
	public void testUtcConvert() {
		Calendar startTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy kk:mm Z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.println("UTC formated "+sdf.format(startTime.getTime()));//, ));
	
		sdf.setTimeZone(TimeZone.getTimeZone("EST"));
		System.out.println("EST formated "+sdf.format(startTime.getTime()));//, TimeZone.getTimeZone("UTC")));
		startTime.set(Calendar.ZONE_OFFSET, TimeZone.getTimeZone("ESTERN").getRawOffset());

		startTime.get(Calendar.ZONE_OFFSET);
		TimeZone tz = TimeZone.getDefault();
		System.out.println("TimeZone   "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID());
	}

	public void testEstConvert() {
		Calendar startTime = GregorianCalendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mma Z", Locale.US);
	    System.out.println("ms before settimezone(est) " +startTime.getTimeInMillis());
		sdf.setTimeZone(TimeZone.getTimeZone("EST"));
	    System.out.println("ms after settimezone(est) " +startTime.getTimeInMillis());
		System.out.println("EST formated "+sdf.format(startTime.getTime()));//, TimeZone.getTimeZone("UTC")));

		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.println("UTC formated "+sdf.format(startTime.getTime()));//, ));
		
	}
	public void testSetTime() {
		//Calendar startTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("US/Eastern"));
		Calendar startTime = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));		
		startTime.set(Calendar.HOUR_OF_DAY, 9);
		startTime.set(Calendar.MINUTE, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm aa Z", Locale.US);
	    System.out.println("ms before settimezone(est) " +startTime.getTimeInMillis());
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		System.out.println("EST formated "+sdf.format(startTime.getTime()));//, TimeZone.getTimeZone("UTC")));
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		System.out.println("UTC formated "+sdf.format(startTime.getTime()));//, ));
		}
}
