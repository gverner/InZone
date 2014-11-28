package com.codeworks.pai.study;

import java.math.BigDecimal;

public class EMA3 {
	/*
	 * EMA = Price(t) * k + EMA(y) * (1 - k) t = today, y = yesterday, N =
	 * number of days in EMA, k = 2/(N+1)
	 */
	public static double ema(int numberOfPeriods, double[] prices) {
		int startindex = numberOfPeriods;

		// Multiplier: (2 / (Time periods + 1) ) = (2 / (10 + 1) ) = 0.1818
		// (18.18%)
		double multiplier = (2d / (numberOfPeriods + 1d));
		System.out.println("Multiplier=" + multiplier);

		// simple moving average
		double average = 0.0;
		for (int i = 0; i < startindex; i++) {
			average += prices[i];
		}
		average = average / startindex;
		System.out.println("SMA = " + new BigDecimal(average).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString());

		// exponential moving average
		double ema = average; // start with sma
		// EMA: {Close - EMA(previous day)} x multiplier + EMA(previous day).
		for (int i = startindex; i < prices.length; i++) {
			ema = (prices[i] - ema) * multiplier + ema; 
		}
		
		return ema;
	}
	public static String format(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
	}
}
