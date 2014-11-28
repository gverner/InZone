package com.codeworks.pai.study;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import android.test.AndroidTestCase;

import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;

public class SMATest extends AndroidTestCase {

	public void testSmaWeekly() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);

		Grouper grouper = new Grouper();
		List<Price> weekly = grouper.periodList(history, Period.Week);
		SMA sma = new SMA(20);
		double smaValue = 0;
		for (Price price : weekly) {
			smaValue = sma.compute(price.getClose());
		}
		BigDecimal decimal = new BigDecimal(smaValue).setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Weekly SMA = "+decimal.toPlainString());
		// One cent off sink or swim has 149.04
		assertEquals(149.03d, decimal.doubleValue());
	}

	public void testSmaMonthy() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Grouper grouper = new Grouper();
		List<Price> monthly = grouper.periodList(history, Period.Month);
		SMA sma = new SMA(20);
		double smaValue = 0;
		for (Price price : monthly) {
			smaValue = sma.compute(price.getClose());
		}
		BigDecimal decimal = new BigDecimal(smaValue);
		decimal = decimal.setScale(2,BigDecimal.ROUND_HALF_UP);
		System.out.println("Monthly SMA ="+  decimal.toPlainString());
		// 1 cent off Sink or swim has 136.74
		assertEquals(136.73d, decimal.doubleValue());
	}
	
}
