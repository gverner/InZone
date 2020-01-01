package com.codeworks.pai.processor;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.PriceHistoryTable;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.mock.MockDataReader;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.Period;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.List;

import androidx.test.rule.provider.ProviderTestRule;

import static androidx.test.InstrumentationRegistry.getContext;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.when;

// ROBO import org.robolectric.RuntimeEnvironment;


//@RunWith(AndroidJUnit4.class)
@RunWith(MockitoJUnitRunner.class)
public class ProcessorTest {

	@Rule
	public ProviderTestRule mProviderRule =
			new ProviderTestRule.Builder(PaiContentProvider.class, PaiContentProvider.AUTHORITY).build();

	ProcessorImpl processor;
	List<Study> studies;

	ContentResolver getMockContentResolver() {
		return mProviderRule.getResolver();
	}

	@Mock
	Context mMockContext;
	
	@Before
	public void setUp() {
		// ROBO mMockContext = RuntimeEnvironment.systemContext;
		//createSecurities();
		//studies = processor.process();
//        when(mMockContext.getContentResolver()).thenReturn(null);
        when(mMockContext.getPackageName()).thenReturn("com.codeworks.pai");
        processor = new ProcessorImpl(getMockContentResolver(), new MockDataReader(), mMockContext);

	}

	public Uri insertSecurity(String symbol) {
		ContentValues values = new ContentValues();
		values.put(StudyTable.COLUMN_SYMBOL, symbol);
		values.put(StudyTable.COLUMN_PORTFOLIO_ID, 1L);
		return getMockContentResolver().insert(PaiContentProvider.PAI_STUDY_URI, values);
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

    @Test
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
		assertFalse("DT Monthly", rules.isDownTrendMonthly());
		assertFalse("DT Weekly", rules.isDownTrendWeekly());
		assertFalse("TT", rules.isPossibleTrendTerminationWeekly());
		assertFalse("TT", rules.isPossibleUptrendTermination(Period.Week));
		assertFalse("TT", rules.isPossibleDowntrendTermination(Period.Week));
		assertFalse("Buy", rules.isPriceInBuyZone());
		assertFalse("Sell", rules.isPriceInSellZone());

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
		assertTrue("DT Monthly", rules.isDownTrendMonthly());
		assertFalse("UT Monthly", rules.isUpTrendMonthly());

		assertTrue("DT Weekly", rules.isDownTrendWeekly());
		assertFalse("UT Weekly", rules.isUpTrendWeekly());
		assertFalse("TT", rules.isPossibleTrendTerminationWeekly());
		assertFalse("TT", rules.isPossibleUptrendTermination(Period.Week));
		assertFalse("TT", rules.isPossibleDowntrendTermination(Period.Week));
		assertFalse("Buy", rules.isPriceInBuyZone());
		assertFalse("Sell", rules.isPriceInSellZone());

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
		assertFalse("DT Monthly", rules.isDownTrendMonthly());
		assertFalse("DT Weekly", rules.isDownTrendWeekly());
		assertEquals("StdDev Week", 5.56d, round(study.getEmaStddevWeek()));
		assertEquals("StdDev Month", 10.94d, round(study.getEmaStddevMonth()));
		assertFalse("TT", rules.isPossibleTrendTerminationWeekly());
		assertFalse("TT", rules.isPossibleUptrendTermination(Period.Week));
		assertFalse("TT", rules.isPossibleDowntrendTermination(Period.Week));
		assertFalse("Buy", rules.isPriceInBuyZone());
		assertFalse("Sell", rules.isPriceInSellZone());

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
		assertFalse("DT Monthly", rules.isDownTrendMonthly());
		assertFalse("DT Weekly", rules.isDownTrendWeekly());
		assertEquals("StdDev Week", 5.56d, round(study.getSmaStddevWeek()));
		assertEquals("StdDev Month", 7.89d, round(study.getSmaStddevMonth()));
		assertFalse("TT", rules.isPossibleTrendTerminationWeekly());
		assertFalse("TT", rules.isPossibleUptrendTermination(Period.Week));
		assertFalse("TT", rules.isPossibleDowntrendTermination(Period.Week));
		assertFalse("Buy", rules.isPriceInBuyZone());
		assertFalse("Sell", rules.isPriceInSellZone());

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
		assertFalse("DT Monthly", rules.isDownTrendMonthly());
		assertFalse("DT Weekly", rules.isDownTrendWeekly());
		assertFalse("TT", rules.isPossibleTrendTerminationWeekly());
		assertFalse("TT", rules.isPossibleUptrendTermination(Period.Week));
		assertFalse("TT", rules.isPossibleDowntrendTermination(Period.Week));
		assertFalse("Buy", rules.isPriceInBuyZone());
		assertFalse("Sell", rules.isPriceInSellZone());
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
		assertFalse("DT Monthly", smaRules.isDownTrendMonthly());
		assertFalse("DT Weekly", smaRules.isDownTrendWeekly());
		assertFalse("TT", smaRules.isPossibleTrendTerminationWeekly());
		assertFalse("TT", smaRules.isPossibleUptrendTermination(Period.Week));
		assertFalse("TT", smaRules.isPossibleDowntrendTermination(Period.Week));
		assertFalse("Buy", smaRules.isPriceInBuyZone());
		assertFalse("Sell", smaRules.isPriceInSellZone());
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
		assert cursor != null;
		if (cursor.moveToFirst()) {
		   expectedDate = cursor.getString(0);
		}
		cursor.close();
		String lastHistoryDate = processor.getMaxDbHistoryDate("SPY");
		System.out.println("last history date for spy = "+lastHistoryDate);
		assertEquals(expectedDate,lastHistoryDate);
	}

/*
	public void testHistoryReload() {
		ProcessorImpl processor = new ProcessorImpl(getMockContentResolver(), new DataReaderYahoo(), mMockContext);
		Study study = new Study("SPY");
		List<String> errors = new ArrayList<>();
		//String lastTradeDate = InZoneDateUtils.lastProbableTradeDate();
		String lastTradeDate = processor.getLastestOnlineHistoryDbDate("SPY", errors);
		List<Price> history = processor.getPriceHistory(study, errors, false);
		assertTrue(history.size() > 0);

	}

 */
}
