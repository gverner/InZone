package com.codeworks.pai.processor;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.Period;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import static androidx.test.InstrumentationRegistry.getContext;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ProcessorFunctionalTest {
	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
	ProcessorImpl processor;

	@Before
	public void setUp() {
		processor = new ProcessorImpl(null, new MockDataReader(), getContext());
	}
	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}
	@Test
	public void testStudyGenDTSell() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		//logHiszory(history);

		MockDataReader.buildSecurity(study, "S&P 500", 9.10, "06/09/2013");
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 9.10d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("MA week", 12.49d, PaiUtils.round(study.getEmaWeek()));
		assertEquals("MA month", 20.19d, PaiUtils.round(study.getEmaMonth()));
		// why do these swap
//		assertEquals("MA last week", 12.85d, PaiUtils.round(study.getEmaLastWeek())); // value Sat Dec 20 2014
//		assertEquals("MA last week", 12.49d, PaiUtils.round(study.getEmaLastWeek()));
// because period close is based on when test is run
        if (InZoneDateUtils.isMarketClosedForThisDateTimeAndPeriod(study.getPriceDate(), Period.Week)) {
            assertEquals("MA last week", 12.49d, PaiUtils.round(study.getEmaLastWeek())); // value Sat Dec 20 2014
        } else {
            assertEquals("MA last week", 12.85d, PaiUtils.round(study.getEmaLastWeek())); // value Sat Dec 20 2014
        }

		assertEquals("MA last month", 21,36d, PaiUtils.round(study.getEmaLastMonth()));
		assertEquals("StdDev Week", 1.78d, PaiUtils.round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 7.52d, PaiUtils.round(study.getEmaStddevMonth()));
		assertTrue("DT Monthly", rules.isDownTrendMonthly());
		assertTrue("DT Weekly", rules.isDownTrendWeekly());
		assertFalse("TT", rules.isPossibleTrendTerminationWeekly());
		assertFalse("TT", rules.isPossibleUptrendTermination(Period.Week));
		assertFalse("TT", rules.isPossibleDowntrendTermination(Period.Week));
		assertFalse("Buy", rules.isPriceInBuyZone());
		assertFalse("Sell", rules.isPriceInSellZone());
	}

	@Test
	public void testStudyGenDTBelowSell() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(40.00, 10.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 8.00, MockDataReader.PRICE_CLOSE_DATE2);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 8.00d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("MA week", 13,15d, PaiUtils.round(study.getEmaWeek()));
		assertEquals("MA month", 21.36d, PaiUtils.round(study.getEmaMonth()));
		assertEquals("MA last week", 13.15d, PaiUtils.round(study.getEmaLastWeek()));
		assertEquals("MA last month", 21.36, PaiUtils.round(study.getEmaLastMonth()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getEmaStddevMonth()));
		assertEquals("DT Monthly", true, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", true, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}
	@Test
	public void testStudyGenSell() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 41.10, MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 41.10d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getEmaStddevMonth()));
		assertEquals("MA week", 36.85D, PaiUtils.round(study.getEmaWeek()));
		assertEquals("MA month", 28.64d, PaiUtils.round(study.getEmaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getEmaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getEmaLastMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", true, rules.isPriceInSellZone());
	}

	@Test
	public void testStudyGenBuy() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 37.50, MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 37.50d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getEmaStddevMonth()));
		assertEquals("MA week", 36.85d, PaiUtils.round(study.getEmaWeek()));
		assertEquals("MA month", 28.64d, PaiUtils.round(study.getEmaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getEmaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getEmaLastMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", true, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}

	@Test
	public void testStudyGenUTrendTT() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.generateHistory(10.00, 40.00, 500);
		Rules rules = new EmaRules(study);
		logHistory(history);
		MockDataReader.buildSecurity(study, "S&P 500", 36.50, MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", 36.50d, study.getPrice());
		assertEquals("ATR", 0.06d, round(study.getAverageTrueRange()));
		assertEquals("MA week", 36.85d, PaiUtils.round(study.getEmaWeek()));
		assertEquals("MA month", 28.64d, PaiUtils.round(study.getEmaMonth()));
		assertEquals("MA last week", 36.85d, PaiUtils.round(study.getEmaLastWeek()));
		assertEquals("MA last month", 28.64d, PaiUtils.round(study.getEmaLastMonth()));
		assertEquals("StdDev Week", 1.73d, PaiUtils.round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 7.53d, PaiUtils.round(study.getEmaStddevMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", true, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", true, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}
	
	void logHistory(List<Price> history) {
		for (Price price : history) {
			System.out.println("logHistory"+sdf.format(price.getDate())+  "  " + price.getClose());
		}
	}
	
	public static void logStudy (Study study) {
		Rules rules = new EmaRules(study);
		System.out.println(study.getSymbol());
		System.out.println("price         "+study.getPrice());
		System.out.println("BuyZone Top   "+rules.calcBuyZoneTop());
		System.out.println("BuyZone Botto "+rules.calcBuyZoneBottom());
		System.out.println("SelZone Top   "+rules.calcSellZoneTop());
		System.out.println("SelZone Botto "+rules.calcSellZoneBottom());
		System.out.println("ma week       "+study.getEmaWeek());
		System.out.println("ma month      "+study.getEmaMonth());
		System.out.println("pr last week  "+study.getPriceLastWeek());
		System.out.println("ma last week  "+study.getEmaLastWeek());
		System.out.println("pr last month "+study.getPriceLastMonth());
		System.out.println("ma last month "+study.getEmaLastMonth());
		System.out.println("stddev  week  "+study.getEmaStddevWeek());
		System.out.println("stddev  month "+study.getEmaStddevMonth());
		
	}

	@Test
	public void testStudy() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
//		history.add(buildPrice(MockDataReader.SPY_PRICE,"04/13/2013"));
		MockDataReader.buildSecurity(study, "S&P 500", MockDataReader.SPY_PRICE,MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		logStudy(study);
		assertEquals("Price", MockDataReader.SPY_PRICE, study.getPrice());
		assertEquals("ATR", 2.08d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 5.56d, PaiUtils.round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 10.94d, PaiUtils.round(study.getEmaStddevMonth()));
		assertEquals("MA week", 151.08d, PaiUtils.round(study.getEmaWeek()));
		assertEquals("MA month", 141.13d, PaiUtils.round(study.getEmaMonth()));
		assertEquals("MA last week", 150.27d, PaiUtils.round(study.getEmaLastWeek()));
		assertEquals("MA last month", 139.27d, PaiUtils.round(study.getEmaLastMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}

	@Test
	public void testStudyMultiple() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		Study study2 = new Study(TestDataLoader.SPY);
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		MockDataReader.buildSecurity(study, "S&P 500", MockDataReader.SPY_PRICE,MockDataReader.PRICE_CLOSE_DATE);
		MockDataReader.buildSecurity(study2, "S&P 500", MockDataReader.SPY_PRICE,MockDataReader.PRICE_CLOSE_DATE);
		processor.calculateStudy(study, history);
		processor.calculateStudy(study2, history);
		logStudy(study);
		logStudy(study2);
		assertEquals(study.getAverageTrueRange(), study2.getAverageTrueRange());
		assertEquals(study.getEmaWeek(), study2.getEmaWeek());
		assertEquals(study.getEmaMonth(), study2.getEmaMonth());
		assertEquals(study.getEmaLastWeek(), study2.getEmaLastWeek());
		assertEquals(study.getEmaLastMonth(), study2.getEmaLastMonth());
		
	}

	@Test
	public void testFridayCutoff() throws ParseException {
		Study study = new Study(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY_FRIDAY_CLOSE);
		// history.add(buildPrice(MockPaiStudyDataReader.SPY_PRICE,
		// "04/13/2013"));
		MockDataReader.buildSecurity(study, "S&P 500", 169.11D,"07/26/2013");
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mmaa Z", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("Eastern/US"));
		Date testDate = sdf.parse("07/26/2013 04:00pm EDT");
		study.setPriceDate(testDate);
		processor.calculateStudy(study, history);
		assertEquals("MA week", 161.43d, PaiUtils.round(study.getEmaWeek()));
		assertEquals("MA last week", 161.43d, PaiUtils.round(study.getEmaLastWeek()));
	}
	
	Price buildPrice(double price, String priceDate) throws ParseException {
		Price lastPrice = new Price();
		lastPrice.setClose(price);
		lastPrice.setDate(sdf.parse(priceDate));
		return lastPrice;
	}

}
