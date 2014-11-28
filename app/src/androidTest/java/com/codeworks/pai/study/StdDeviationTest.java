package com.codeworks.pai.study;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import junit.framework.TestCase;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.study.EMA2;
import com.codeworks.pai.study.Grouper;
import com.codeworks.pai.study.Period;
import com.codeworks.pai.study.StdDev;

public class StdDeviationTest extends TestCase {
	
	public void testStdDeviation() throws IOException {
		int noPeriods = 20;
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> weekly = grouper.periodList(history, Period.Week);
		StdDev stdDeviation = new StdDev();
		double[] prices = new double[weekly.size()];
		for (int ndx = 0; ndx < weekly.size(); ndx++) {
			prices[ndx] = weekly.get(ndx).getClose();
		}
		double stddev = stdDeviation.calculate(prices, noPeriods);
		System.out.println("Std Deviation * 2 "+EMA2.format(stddev * 2)+ " half="+EMA2.format(stddev * 0.5));
		// 161.20 - 150.27 range from thinkorswim devide by 2 for week of april 1
		assertEquals(5.46d, new BigDecimal(stddev).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
	}
	
	public void testStdDeviationMonthly() throws IOException {
		int noPeriods = 20;
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> monthly = grouper.periodList(history, Period.Month);
		StdDev stdDeviation = new StdDev();
		double[] prices = new double[monthly.size()];
		for (int ndx = 0; ndx < monthly.size(); ndx++) {
			prices[ndx] = monthly.get(ndx).getClose();
		}
		double stddev = stdDeviation.calculate(prices, noPeriods);
		System.out.println("Std Deviation "+EMA2.format(stddev));
		//160.17d-139.27d range from thinkorswim for march
		assertEquals( 10.45d, new BigDecimal(stddev).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
	}	

}
