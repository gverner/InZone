package com.codeworks.pai.processor;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Study;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNotSame;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class YahooReaderTest {
	DataReaderYahoo reader;

	@Before
	public void setUp() throws Exception {
		reader = new DataReaderYahoo();
	}

	@Test
    public void testScanLine() {
        String line = "<token> ";
        String result = reader.scanLine("token",1,line,1);
    }

	@Test
	public void testFormatDate() {
		SimpleDateFormat ydf = new SimpleDateFormat("MMM dd, hh:mmaa zzz yyyy", Locale.US);
		ydf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
		String stringDate = "Jun 21, 4:30PM EDT 2013";//stringDate.substring(0, 5) + " " + cal.get(Calendar.YEAR) + " " + stringDate.substring(8,15);
		try {
			Date returnDate = ydf.parse(stringDate);
			System.out.println(ydf.format(returnDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

	@Test
	public void testReadRTPrice() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mmaa zzz", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
        Study security = new Study("XES");
        List<String> errors = new ArrayList<String>();
        assertTrue(reader.readRTPrice(security, errors));
        System.out.println(sdf.format(security.getPriceDate()));
        System.out.println(security.getName());
        System.out.println("ExtMarketPrice " + security.getExtMarketPrice());
        System.out.println("ExtMarketDate " + security.getExtMarketDate());
        System.out.println("reqular price Date " + security.getPriceDate());
        assertNotNull(security.getLastClose());
		/*
	    security = new PaiStudy("QQQ");
		assertTrue(reader.readRTPrice(security));
	    security = new PaiStudy("IWM");
		assertTrue(reader.readRTPrice(security));
	    security = new PaiStudy("EFA");
		assertTrue(reader.readRTPrice(security));
	    security = new PaiStudy("HYG");
		assertTrue(reader.readRTPrice(security));
	    security = new PaiStudy("XLE");
		assertTrue(reader.readRTPrice(security));
	    security = new PaiStudy("");
		assertFalse(reader.readRTPrice(security));
		*/
    }

    @Test
    public void testReadSecurityName() {
        Study security = new Study("SPY");
        List<String> errors = new ArrayList<String>();
        assertTrue(reader.readSecurityName(security, errors));
        assertNotNull(security.getName());
    }

    @Test
    public void testReadCurrentPrice() {
        Study security = new Study("SPY");
        List<String> errors = new ArrayList<String>();
        assertTrue(reader.readRTPrice(security, errors));
        assertNotSame(0d, security.getPrice());
    }

    @Test
	public void testReadBlankCurrentPrice() {
		Study security = new Study("");
        List<String> errors = new ArrayList<String>();
		assertFalse(reader.readRTPrice(security, errors));
		assertEquals(0d,security.getPrice());
	}


	@Test
	public void testReadNullCurrentPrice() {
		Study security = new Study(null);
        List<String> errors = new ArrayList<String>();
		assertFalse(reader.readRTPrice(security, errors));
		assertEquals(0d,security.getPrice());
	}

	@Test
	public void testParseDate() {
		String testDate = "2013-05-25";
		Date date = reader.parseDate(testDate, "TEST PARSE DATE");
		assertEquals(testDate, reader.dateFormat.format(date));
	}

	@Test
	public void testParseBadDate() {
		String testDate = "20130525";
		Date date = reader.parseDate(testDate, "TEST PARSE BAD DATE");
		assertEquals(null, date);
	}

	@Test
	public void testParseDateTime() {
		String testDate = "05/25/2013 09:30AM";
		Date date = reader.parseDateTime(testDate, "TEST PARSE DATE TIME");
		assertEquals(testDate, reader.dateTimeFormat.format(date));
	}

	@Test
	public void testParseBadDateTime() {
		String testDate = "";//"05/25/2013 09:30AM";
		Date date = reader.parseDateTime(testDate, "TEST PARSE BAD DATE TIME");
		assertEquals(null, date);
	}

	@Test
	public void testParseNullDateTime() {
		String testDate = null;
		Date date = reader.parseDateTime(testDate, "TEST PARSE BAD DATE TIME");
		assertEquals(null, date);
	}

	@Test
	public void testParseDouble() {
		String testDouble = "1.232";
		double result = reader.parseDouble(testDouble, "TEST PARSE DOUBLE");
		assertEquals(Double.parseDouble(testDouble), result);
	}

	@Test
	public void testParseBlankDouble() {
		String testDouble = "";
		double result = reader.parseDouble(testDouble, "TEST PARSE BLANK DOUBLE");
		assertEquals(0d,result);
	}

	@Test
	public void testParseNullDouble() {
		String testDouble = null;
		double result = reader.parseDouble(testDouble, "TEST PARSE NULL DOUBLE");
		assertEquals(0d,result);
	}

	@Test
	public void testBuildHistoryUrl() {
		String url = reader.buildHistoryUrl("SPY", 300, "crumb");
		System.out.println(url);
	}

	@Test
	public void testReadHistory() {
		long startTime = System.currentTimeMillis();
        List<String> errors = new ArrayList<String>();
		List<Price> history = reader.readHistory("SPY", errors);
		System.out.println("history size="+history.size() + " execution time in ms = " + (System.currentTimeMillis()- startTime));
		assertTrue(history.size() > 200);
		Date lastDate = history.get(history.size() -1).getDate();
		System.out.println("last History date "+ InZoneDateUtils.toDatabaseFormat(lastDate));
	}	
	/*
	public void testReadBlankHistory() {
		List<Price> history = reader.readHistory("");
		System.out.println("history size="+history.size());
		assertTrue(history.size() == 0);
	}	

	public void testReadNullHistory() {
		List<Price> history = reader.readHistory("");
		System.out.println("history size="+history.size());
		assertTrue(history.size() == 0);
	}	
	*/
	@Test
	public void testReadLatestDate() {
        List<String> errors = new ArrayList<String>();
		Date latestDate = reader.latestHistoryDate("SPY", errors);
		System.out.println("Latest History Date ="+latestDate);
		System.out.println("Last Probable Date = "+ InZoneDateUtils.lastProbableTradeDate());
		assertNotNull(latestDate);
		assertTrue(InZoneDateUtils.toDatabaseFormat(latestDate).compareTo(InZoneDateUtils.lastProbableTradeDate()) >= 0);
	}

	@Test
    public void testReadOptionDates() {
        List<String> errors = new ArrayList<String>();
        List<DateTime> optionDates = reader.readOptionExpirations("SPY", errors);
        for (DateTime dateTime : optionDates) {
            System.out.println(dateTime);
        }
    }

	@Test
	public void testReadOption() {
        DateTime[] dts = InZoneDateUtils.calcFrontAndSecondMonth(new DateTime());
        assertEquals(2, dts.length);
        assertNotNull(dts[0]);
        assertNotNull(dts[1]);
    }

	@Test
    public void testSelectingFrontAndSecondOptionDates() {
        DateTime[] dts = InZoneDateUtils.calcFrontAndSecondMonth(new DateTime());
        List<String> errors = new ArrayList<String>();
        List<DateTime> optionDates = reader.readOptionExpirations("SPY", errors);
        for (DateTime optionDate : optionDates) {
            Duration frontDuration = new Duration(optionDate, dts[0]);
            Duration secondDuration = new Duration(optionDate, dts[1]);
            if (frontDuration.getStandardDays() >= 0 && frontDuration.getStandardDays() <= 4) {
                System.out.println("front date "+ optionDate);
            }
            if (secondDuration.getStandardDays() >= 0 && secondDuration.getStandardDays() <= 4) {
                System.out.println("second date "+ optionDate);
            }
        }
    }
/*
	Tried to read yahoo to get crumb and then call history, but failed to find crumb in first request.s

    public void testReadYahooHistory() {
		List<String> errors = new ArrayList<String>();
		String symbol = "SPY";
		List<Price>  prices = reader.readHistoryOldYahooAndGoogle(symbol, errors);
		assertTrue(prices.size() > 0);
	}
	*/
}
