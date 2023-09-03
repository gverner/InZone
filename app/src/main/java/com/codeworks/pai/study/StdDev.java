package com.codeworks.pai.study;

import com.codeworks.pai.db.model.Price;

import java.util.List;

public class StdDev {
    /**
     * Calculate Standard Deviation.
     *
     * <li>1.Calculate the mean (simple average of the numbers).
     * <li>2.For each number: subtract the mean. Square the result.
     * <li>3.Calculate the mean of those squared differences. This is the variance.
     * <li>4.Take the square root of that to obtain the <b>population standard deviation.</b>
     * <li>Or
	 * <li>4.Divide this sum by one less than the number of data points (N - 1). This gives you <b>the sample variance.</b>
	 * 
	 * @param prices Array of prices must be at least noPeriods long
	 * if longer only last noPeriods is used.
	 * @param noPeriods
	 * @return
	 */
	public double calculate(double[] prices, int noPeriods) {
		double stddev;
		int offset = prices.length - noPeriods;
		if (offset < 0) {
			throw new RuntimeException("History to short to calculate std deviation");
		}
		// 1.Calculate the mean (simple average of the numbers). 
		double average = 0d;//deviate = new double[noPeriods];
		for (int ndx = 0; ndx < noPeriods; ndx++) {
			average += prices[ndx+offset];
		}
		average = average / noPeriods;
		
		// 2.For each number: subtract the mean. Square the result.
		double[] deviate = new double[noPeriods];
		double squared[] = new double[noPeriods];
		double sumSquared = 0d;
		for (int ndx = 0; ndx < noPeriods; ndx++) {
			deviate[ndx] = prices[ndx+offset] - average;
			squared[ndx] = Math.pow(deviate[ndx],2);
			sumSquared += squared[ndx];
		}
		// 3.Calculate the mean of those squared differences. sumSquared / (noPeriods) This is the variance.
		// 4.Take the square root of that to obtain the population standard deviation.
		//stddev = Math.sqrt(sumSquared / (noPeriods -1 ));
		stddev = Math.sqrt(sumSquared / (noPeriods));
		return stddev;
	}
	
	/**
	 * Calculate Standard Deviation.
	 * converts Prices.close to array and calls calculate
	 * @param List<Price> must have at least noPeriods values.
	 * @param noPeriods
	 * @return
	 */	
	public static double calculate(List<Price> priceList, int noPeriods) {
		StdDev stddev = new StdDev();
		double[] prices = new double[priceList.size()];
		for (int ndx = 0; ndx < priceList.size(); ndx++) {
			prices[ndx] = priceList.get(ndx).getClose();
		}
		if (noPeriods <= prices.length) {
			return stddev.calculate(prices, noPeriods);
		} else {
			return 0;
		}
	}
}
