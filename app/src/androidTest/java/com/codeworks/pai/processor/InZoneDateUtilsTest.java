package com.codeworks.pai.processor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import android.test.AndroidTestCase;

import com.codeworks.pai.study.Period;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class InZoneDateUtilsTest extends AndroidTestCase {

	public void testFridayAt4pm() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("06/28/2013 16:00");
		assertTrue(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Week));
	}
	
	public void testFridayAt359pm() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("06/28/2013 15:59");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Week));
	}
	
	public void testMondayAT120pm() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("08/19/2013 13:20");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Week));
	}

	public void testMondayAT140pm() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("08/19/2013 13:40");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Week));
	}
	
	public void testEndOfMonthAfter() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("06/28/2013 16:01");
		assertTrue(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Month));
	}
	
	public void testEndOfMonthBefore() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		// TODO Test Breaks every month - This should be fixed
		Date date = sdf.parse("06/26/2013 16:00");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Month));
	}	
	public void testEndOfMonthNext() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("07/01/2013 9:29");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Month));
	}	

	public void testEndOfMonthNextOpen() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("07/01/2013 9:30");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Month));
	}

	public void testFridayMarketGoodFridayClosed() throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		Date date = sdf.parse("03/30/2018 00:01");
		Calendar cal = GregorianCalendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		cal.setTime(date);
		System.out.println("hour "+cal.get(Calendar.HOUR_OF_DAY));
		System.out.println("minute "+cal.get(Calendar.MINUTE));
		assertFalse(InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(date, Period.Week));
	}

    public void testMonthlyExpiration() {
        DateTime dt = DateTime.parse("20141101", ISODateTimeFormat.basicDate());
        assertEquals(DateTime.parse("20141018", ISODateTimeFormat.basicDate()), InZoneDateUtils.calcMonthlyExpiration(dt.minusMonths(1)));
        assertEquals(DateTime.parse("20141122", ISODateTimeFormat.basicDate()), InZoneDateUtils.calcMonthlyExpiration(dt.plusMonths(0)));
        assertEquals(DateTime.parse("20141220", ISODateTimeFormat.basicDate()), InZoneDateUtils.calcMonthlyExpiration(dt.plusMonths(1)));
        assertEquals(DateTime.parse("20150117", ISODateTimeFormat.basicDate()), InZoneDateUtils.calcMonthlyExpiration(dt.plus(org.joda.time.Period.months(2))));
    }

    public void testCalcFrontMonthSecondMonth() {
        DateTime[] dts = InZoneDateUtils.calcFrontAndSecondMonth(DateTime.parse("20141018", ISODateTimeFormat.basicDate()));
        assertEquals(DateTime.parse("20141122", ISODateTimeFormat.basicDate()), dts[0]);
        assertEquals(DateTime.parse("20141220", ISODateTimeFormat.basicDate()), dts[1]);
    }

    public void testCalcFrontMonthSecondMonthLessThen2Week() {
        DateTime[] dts = InZoneDateUtils.calcFrontAndSecondMonth(DateTime.parse("20141209", ISODateTimeFormat.basicDate()));
        assertEquals(DateTime.parse("20150117", ISODateTimeFormat.basicDate()), dts[0]);
        assertEquals(DateTime.parse("20150221", ISODateTimeFormat.basicDate()), dts[1]);
    }

    public void testCalcFrontMonthSecondMonthAcrossYear() {
        DateTime[] dts = InZoneDateUtils.calcFrontAndSecondMonth(DateTime.parse("20141204", ISODateTimeFormat.basicDate()));
        assertEquals(DateTime.parse("20141220", ISODateTimeFormat.basicDate()), dts[0]);
        assertEquals(DateTime.parse("20150117", ISODateTimeFormat.basicDate()), dts[1]);
    }
}
