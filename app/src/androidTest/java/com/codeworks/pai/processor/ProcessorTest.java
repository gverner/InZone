package com.codeworks.pai.processor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.ProviderTestCase2;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.Period;

public class ProcessorTest extends ProviderTestCase2<PaiContentProvider> {

	public ProcessorTest() {
		super(PaiContentProvider.class, PaiContentProvider.AUTHORITY);
	}

	ProcessorImpl processor;
	List<Study> studies;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		processor = new ProcessorImpl(getMockContentResolver(), new MockDataReader(), getContext());
		//createSecurities();
		//studies = processor.process();
	}

	public Uri insertSecurity(String symbol) {
		ContentValues values = new ContentValues();
		values.put(StudyTable.COLUMN_SYMBOL, symbol);
		values.put(StudyTable.COLUMN_PORTFOLIO_ID, 1L);
		Uri uri = getMockContentResolver().insert(PaiContentProvider.PAI_STUDY_URI, values);
		return uri;
	}

	public void createSecurities() {
		insertSecurity(TestDataLoader.SPY);
		insertSecurity(TestDataLoader.QQQ);
		insertSecurity(TestDataLoader.GLD);
		insertSecurity(TestDataLoader.UNG);
	}

	public double round(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	public Study getStudy(String symbol) {
		for (Study study : studies) {
			if (symbol.equalsIgnoreCase(study.getSymbol())) {
				return study;
			}
		}
		return null;
	}
/*
	public void testHyg() throws InterruptedException {
		insertSecurity(TestDataLoader.HYG);
		studies = processor.process();
		PaiStudy study = getStudy(TestDataLoader.HYG);
		assertEquals("Price", MockDataReader.HYG_PRICE, study.getPrice());
		assertEquals("ATR", 0.0d, study.getAverageTrueRange());
		assertEquals("MA week", 94.10d, round(study.getMaWeek()));
		assertEquals("MA month", 92.23d, round(study.getMaMonth()));
		assertEquals("MA last week", 94.28d, round(study.getMaLastWeek()));
		assertEquals("MA last month", 92.21d, round(study.getMaLastMonth()));
		assertEquals("Price last week", 92.92d, round(study.getPriceLastWeek()));
		assertEquals("Price last month", 92.92d, round(study.getPriceLastMonth()));
		assertEquals("StdDev Week", .87d, round(study.getStddevWeek()));
		assertEquals("StdDev Month", 2.18d, round(study.getStddevMonth()));
		assertEquals("DT Monthly",false, study.isDownTrendMonthly());
		assertEquals("DT Weekly", true, study.isDownTrendWeekly());
		assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, study.isPossibleUptrendTermination());
		assertEquals("TT", false, study.isPossibleDowntrendTermination());
		assertEquals("Buy", false, study.isPriceInBuyZone());
		assertEquals("Sell", false, study.isPriceInSellZone());

	}
*/

	
	public void testUng() throws InterruptedException {
		insertSecurity(TestDataLoader.UNG);
		studies = processor.process(TestDataLoader.UNG, false);
		Study study = getStudy(TestDataLoader.UNG);
		Rules rules = new EmaRules(study);
		
		assertEquals("Price", MockDataReader.UNG_PRICE, study.getPrice());
		assertEquals("ATR", 0.61d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 1.46d, round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 5.62d, round(study.getEmaStddevMonth()));
		assertEquals("MA week", 20.46d, round(study.getEmaWeek()));
		assertEquals("MA month", 17.72d, round(study.getEmaMonth()));
		assertEquals("MA last week", 20.18d, round(study.getEmaLastWeek()));
		assertEquals("MA last month", 17.16d, round(study.getEmaLastMonth()));
		assertEquals("Price last week", 22.46d, round(study.getPriceLastWeek()));
		assertEquals("Price last month", 21.88d, round(study.getPriceLastMonth()));
		assertEquals("DT Monthly",false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());

	}

	public void testGld() throws InterruptedException {
		insertSecurity(TestDataLoader.GLD);
		studies = processor.process(null, false);
		Study study = getStudy(TestDataLoader.GLD);
		Rules rules = new EmaRules(study);
		assertEquals("Price", MockDataReader.GLD_PRICE, study.getPrice());
		assertEquals("ATR", 2.21d, round(study.getAverageTrueRange()));
		assertEquals("StdDev Week", 5.42d, round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 7.15d, round(study.getEmaStddevMonth()));
		assertEquals("MA week", 156.61d, round(study.getEmaWeek()));
		assertEquals("MA month", 155.75d, round(study.getEmaMonth()));
		assertEquals("MA last week", 157.94d, round(study.getEmaLastWeek()));
		assertEquals("MA last month", 156.99d, round(study.getEmaLastMonth()));
		assertEquals("DT Monthly",true, rules.isDownTrendMonthly());
		assertEquals("UT Monthly",false, rules.isUpTrendMonthly());
		
		assertEquals("DT Weekly", true, rules.isDownTrendWeekly());
		assertEquals("UT Weekly", false, rules.isUpTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());

	}

	public void testSpy() throws InterruptedException {
		insertSecurity(TestDataLoader.SPY);
		studies = processor.process(TestDataLoader.SPY, false);
		Study study = getStudy(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		assertEquals("Price", MockDataReader.SPY_PRICE, study.getPrice());
		assertEquals("ATR", 1.48d, round(study.getAverageTrueRange()));
		assertEquals("MA week", 151.08d, round(study.getEmaWeek()));
		assertEquals("MA month", 141.13d, round(study.getEmaMonth()));
		assertEquals("MA last week", 150.27d, round(study.getEmaLastWeek()));
		assertEquals("MA last month", 139.27d, round(study.getEmaLastMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("StdDev Week", 5.56d, round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 10.94d, round(study.getEmaStddevMonth()));
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());

	}
	
	public void testSmaSpy() throws InterruptedException {
		insertSecurity(TestDataLoader.SPY);
		studies = processor.process(TestDataLoader.SPY, false);
		Study study = getStudy(TestDataLoader.SPY);
		Rules rules = new EmaRules(study);
		assertEquals("Price", MockDataReader.SPY_PRICE, study.getPrice());
		assertEquals("ATR", 1.48d, round(study.getAverageTrueRange()));
		assertEquals("MA last week", 149.03d, round(study.getSmaLastWeek()));
		assertEquals("MA last month", 142.85d, round(study.getSmaLastMonth()));
		assertEquals("MA week", 149.91d, round(study.getSmaWeek()));
		assertEquals("MA month", 144.42d, round(study.getSmaMonth())); // as of 4/12
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("StdDev Week", 5.56d, round(study.getSmaStddevWeek()));
		assertEquals("StdDev Month", 7.89d, round(study.getSmaStddevMonth()));
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());

	}
	public void testQQQ() throws InterruptedException {
		insertSecurity(TestDataLoader.QQQ);
		studies = processor.process(null, false);
		
		Study study = getStudy(TestDataLoader.QQQ);
		Rules rules = new EmaRules(study);
		assertEquals("Price", MockDataReader.QQQ_PRICE, study.getPrice());
		assertEquals("ATR", 0.77, round(study.getAverageTrueRange()));
		assertEquals("MA week", 67.57d, round(study.getEmaWeek()));
		assertEquals("MA month", 63.99d, round(study.getEmaMonth()));
		assertEquals("MA last week", 67.32d, round(study.getEmaLastWeek()));
		assertEquals("MA last month", 63.36d, round(study.getEmaLastMonth()));
		assertEquals("StdDev Week", 1.55d, round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 4.75d, round(study.getEmaStddevMonth()));
		assertEquals("DT Monthly", false, rules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, rules.isDownTrendWeekly());
		assertEquals("TT", false, rules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, rules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, rules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, rules.isPriceInBuyZone());
		assertEquals("Sell", false, rules.isPriceInSellZone());
	}
	
	public void testSmaQQQ() throws InterruptedException {
		insertSecurity(TestDataLoader.QQQ);
		studies = processor.process(null, false);

		Study study = getStudy(TestDataLoader.QQQ);
	
		Rules smaRules = new SmaRules(study);
		assertEquals("Price", MockDataReader.QQQ_PRICE, study.getPrice());
		assertEquals("ATR", 0.77, round(study.getAverageTrueRange()));
		assertEquals("MA week", 67.15d, round(study.getSmaWeek()));
		assertEquals("MA month", 66.38d, round(study.getSmaMonth()));
		assertEquals("MA last week", 66.89d, round(study.getSmaLastWeek()));
		assertEquals("MA last month", 66.11d, round(study.getSmaLastMonth()));
		assertEquals("StdDev Week", 1.55d, round(study.getSmaStddevWeek()));
		assertEquals("StdDev Month", 2.20d, round(study.getSmaStddevMonth()));
		assertEquals("DT Monthly", false, smaRules.isDownTrendMonthly());
		assertEquals("DT Weekly", false, smaRules.isDownTrendWeekly());
		assertEquals("TT", false, smaRules.isPossibleTrendTerminationWeekly());
		assertEquals("TT", false, smaRules.isPossibleUptrendTermination(Period.Week));
		assertEquals("TT", false, smaRules.isPossibleDowntrendTermination(Period.Week));
		assertEquals("Buy", false, smaRules.isPriceInBuyZone());
		assertEquals("Sell", false, smaRules.isPriceInSellZone());		
	}

	/*
	public void testProcessor1() {
		insertSecurity(TestDataLoader.SPY);
		studies = processor.process();
		
		assertTrue(studies.size() == 2);
		for (PaiStudy study : studies) {
			if (TestDataLoader.SPY.equalsIgnoreCase(study.getSymbol())) {
				assertEquals("Price", MockSecurityDataReader.SPY_PRICE, study.getPrice());
				assertEquals("ATR", 0.0d, study.getAverageTrueRange());
				assertEquals("StdDev Week", 5.56d, round(study.getStddevWeek()));
				assertEquals("StdDev Month", 10.94d, round(study.getStddevMonth()));
				assertEquals("MA week", 151.08d, round(study.getMaWeek()));
				assertEquals("MA month", 141.13d, round(study.getMaMonth()));
				assertEquals("MA last week", 150.27d, round(study.getMaLastWeek()));
				assertEquals("MA last month", 139.27d, round(study.getMaLastMonth()));
				assertEquals("DT Monthly", false, study.isDownTrendMonthly());
				assertEquals("DT Weekly", false, study.isDownTrendWeekly());
				assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
				assertEquals("TT", false, study.isPriceInPossibleUptrendTermination());
				assertEquals("TT", false, study.isPriceInPossibleDowntrendTermination());
				assertEquals("Buy", false, study.isPriceInBuyZone());
				assertEquals("Sell", false, study.isPriceInSellZone());

			} else if (TestDataLoader.QQQ.equalsIgnoreCase(study.getSymbol())) {
				assertEquals("Price", MockSecurityDataReader.QQQ_PRICE, study.getPrice());
				assertEquals("ATR", 0.0, study.getAverageTrueRange());
				assertEquals("StdDev Week", 1.42d, round(study.getStddevWeek()));
				assertEquals("StdDev Month", 4.64d, round(study.getStddevMonth()));
				assertEquals("MA week", 67.37d, study.getMaWeek());
				assertEquals("MA month", 63.79d, study.getMaMonth());
				assertEquals("MA last week", 67.32d, round(study.getMaLastWeek()));
				assertEquals("MA last month", 63.36d, round(study.getMaLastMonth()));
				assertEquals("DT Monthly", false, study.isDownTrendMonthly());
				assertEquals("DT Weekly", false, study.isDownTrendWeekly());
				assertEquals("TT", false, study.isPossibleTrendTerminationWeekly());
				assertEquals("TT", false, study.isPriceInPossibleUptrendTermination());
				assertEquals("TT", false, study.isPriceInPossibleDowntrendTermination());
				assertEquals("Buy", true, study.isPriceInBuyZone());
				assertEquals("Sell", false, study.isPriceInSellZone());
			}
		}
	}
	*/
	public void testLastHistoryDate() {
		Cursor cursor = getContext().getContentResolver().query(PaiContentProvider.PRICE_HISTORY_URI, new String[]{PriceHistoryTable.COLUMN_DATE}, 
				PriceHistoryTable.COLUMN_SYMBOL + " = ? ", new String[]{"SPY"}, PriceHistoryTable.COLUMN_DATE+" desc");
		String expectedDate = "";
		if (cursor.moveToFirst()) {
		   expectedDate = cursor.getString(0);
		}
		cursor.close();
		String lastHistoryDate = processor.getMaxDbHistoryDate("SPY");
		System.out.println("last history date for spy = "+lastHistoryDate);
		assertEquals(expectedDate,lastHistoryDate);
	}


	public void testHistoryReload() {
		ProcessorImpl processor = new ProcessorImpl(getMockContentResolver(), new DataReaderYahoo(), getMockContext());
		Study study = new Study("SPY");
		List<String> errors = new ArrayList<>();
		//String lastTradeDate = InZoneDateUtils.lastProbableTradeDate();
		String lastTradeDate = processor.getLastestOnlineHistoryDbDate("SPY", errors);
		List<Price> history = processor.getPriceHistory(study, errors, false);
		assertTrue(history.size() > 0);

	}
}
