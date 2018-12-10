package com.codeworks.pai.study;

import android.support.test.runner.AndroidJUnit4;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.mock.TestDataLoader;
import com.codeworks.pai.processor.DataReaderYahoo;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

@RunWith(AndroidJUnit4.class)
public class ATRTest {

	public void testAtr() throws IOException {
		List<Price> history = TestDataLoader.getTestHistory(TestDataLoader.SPY);
		Collections.sort(history);
		double value = 0;
		value = ATR.compute(history, 20);
		System.out.println(" Ema ATR = "+format(value)+" for date "+history.get(history.size()-1).getDate());
		// Sink or Swim has 1.4918, has to be daily rounding or exponential moving average?
		// smaAtr = 1.46d
		// assertEquals(1.46d, PaiUtils.round(value));
		assertEquals(1.48d, PaiUtils.round(value));
	}

	@Test
	public void testAtrToday() throws IOException {
		DataReaderYahoo reader = new DataReaderYahoo();
        List<String> errors = new ArrayList<String>();

		List<Price> history = reader.readHistory("SPY", errors);
		Collections.sort(history);
		double value = 0;
		value = ATR.compute(history, 20);
		System.out.println("Daily ATR = "+format(value)+" for date "+history.get(history.size()-1).getDate());
		// sma assertEquals(1.46d, PaiUtils.round(value));
	}
	String format(double value) {
		return new BigDecimal(value).setScale(6,BigDecimal.ROUND_HALF_UP).toPlainString();
	}
}
