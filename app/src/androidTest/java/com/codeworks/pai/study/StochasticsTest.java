package com.codeworks.pai.study;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class StochasticsTest {
	SimpleDateFormat	sdf	= new SimpleDateFormat("MM/dd/yyyy", Locale.US);

	@Test
	public void testStochasticsFast() throws ParseException {
		List<Price> weekly = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Collections.sort(weekly);
		Date maxDate = sdf.parse("04/03/2013");
		while (weekly.get(weekly.size() - 1).getDate().after(maxDate)) {
			weekly.remove(weekly.size() - 1);
		}
		Stochastics stochastics = new Stochastics();
		stochastics.calculateFast(weekly, 9, 3);
		double fast = stochastics.getK();
		fast = PaiUtils.round(fast,4);
		System.out.println("Last History Date" + weekly.get(weekly.size() - 1).getDate());
		System.out.println("Stochastics Fast K =" + fast);
		System.out.println("Stochastics Fast D =" + PaiUtils.round(stochastics.getD(),4));
		// April 3 Think or Swim 36.3344, 65.9635 , 79.9461 
		assertEquals(36.3344d, fast);
		assertEquals(65.9635d, PaiUtils.round(stochastics.getD(),4));
	}
	@Test
	public void testStochasticsSlow() throws ParseException {
		List<Price> daily = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Collections.sort(daily);
		Date maxDate = sdf.parse("04/03/2013");
		while (daily.get(daily.size() - 1).getDate().after(maxDate)) {
			daily.remove(daily.size() - 1);
		}
		Stochastics stochastics = new Stochastics();
		stochastics.calculateSlow(daily, 9, 3, 3);
		double slowK = stochastics.getK();
		slowK = PaiUtils.round(slowK,4);
		System.out.println("Last History Date" + daily.get(daily.size() - 1).getDate());
		System.out.println("Stochastics Slow K =" + slowK);
		System.out.println("Stochastics Slow D =" + PaiUtils.round(stochastics.getD(),4));
		// April 3 Think or Swim 65.9635 , 79.9461 
		assertEquals(65.9635d, slowK);
		assertEquals(79.9461d, PaiUtils.round(stochastics.getD(),4));
	}
	
	/**
	 * Fast K is not matching Sink or Swim for 12/26 and 12/27
	 * 
	 * @throws IOException
	 * @throws ParseException
	 */
	@Test
	public void testStochasticsFast2() {
		List<Price> daily = TestDataLoader.getTestHistory(TestDataLoader.SPY3);
		Collections.sort(daily);

		Stochastics stochastics = new Stochastics();
		stochastics.calculateFast(daily, 9, 3);
		double K = stochastics.getK();
		K = PaiUtils.round(K,4);
		System.out.println("Last History Date" + daily.get(daily.size() - 1).getDate());
		System.out.println("Stochastics Fast K =" + K);
		System.out.println("Stochastics Fast D =" + PaiUtils.round(stochastics.getD(),4));
		// 12/26/2013 Think or Swim 98.4187 , 98.315 
		// 12/27/2013 Think or Swim 95.1166 , 97.3764 
		assertEquals(95.1895d, K);
		assertEquals(97.4258d, PaiUtils.round(stochastics.getD(),4));
	}	
	
}
