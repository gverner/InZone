package com.codeworks.pai.study;

import com.codeworks.pai.db.model.Price;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Calculates Stochastics
 * 
 * @author glenn verner
 * 
 *         Fast Stochastics The 10-bar fast stochastic calculations are as
 *         follows: %K = 100[(C - L10)/(H10 - L10)] where C = last close and H10
 *         is the highest high in the last 10 bars and L10 is the lowest low in
 *         the last 10 bars.
 * 
 *         %D is a proxy for a three-period smoothed average of %K. %D =
 *         100(H3/L3) where H3 is the three-period sum of (C- L10) over the last
 *         three bars and L3 is the three-period sum of (H10-L10) for the last
 *         three bars.
 * 
 *         The settings are (10,3), which is a 10-bar lookback and a
 *         three-period smoothing.
 * 
 * 
 *         CQG traders have a choice of the number of bars in the lookback
 *         period, the number of bars for the smoothing, and the choice of
 *         smoothed, simple, exponential, weighted, and centered moving
 *         averages.
 * 
 *         Chart 1 is an example of the fast stochastic. The %K line is volatile
 *         as it tracks the closing price relative to the highest high and
 *         lowest low over the last 10 bars. The %D line smoothes out this
 *         volatile oscillator line.
 * 
 *         Slow Stochastics The slow stochastic calculates the fast stochastic
 *         %K and %D, but does not plot the original %K; instead, it uses the %D
 *         value from the fast stochastic and labels it as %K. The new %D line
 *         is a three-period average of the new %K. The settings would be
 *         (10,3,3), but only the (3,3) lines are plotted. See chart 2 for a
 *         comparison between the two versions.
 * 
 * 
 *         - See more at:
 *         http://www.cqg.com/Technical-Analysis/Studies/Standard-
 *         Studies/Stochastic.aspx#sthash.SWcwFTdJ.dpuf
 * 
 * 
 * 
 */
public class Stochastics {

	double	K;
	double	D;

	class Data {
		double	close;
		double	high;
		double	low;
	}

	public double getK() {
		return K;
	}

	public double getD() {
		return D;
	}

	/**
	 * Assumes daily prices sorted in ascending order.
	 * 
	 * @param priceList
	 * @param periods
	 * @param smooth1
	 */
	public void calculateFast(List<Price> priceList, int periods, int smooth1) {
		if (periods <= priceList.size()) {
			Data[] data = new Data[smooth1];
			for (int ndx = 0; ndx < data.length; ndx++) {
				data[ndx] = scanHighLowInPeriod(priceList, periods, ndx);
				data[ndx].close = priceList.get(priceList.size() - (1 + ndx)).getClose();
				// System.out.println("high="+data[ndx].high+" Low="+data[ndx].low+" Close="+data[ndx].close);
			}
			K = calculateK(data[0]);
			D = average(data);
		} else {
			K = 0;
			D = 0;
		}
	}

	/**
	 * Assumes daily priceList sorted in ascending order.
	 * 
	 * @param priceList
	 * @param periods
	 * @param smooth1
	 * @param smooth2
	 */
	public void calculateSlow(List<Price> priceList, int periods, int smooth1, int smooth2) {
		if (periods <= priceList.size()) {
			double[] DV = new double[smooth2];
			for (int ndx1 = 0; ndx1 < DV.length; ndx1++) {
				Data[] data = new Data[smooth1];
				for (int ndx = 0; ndx < data.length; ndx++) {
					data[ndx] = scanHighLowInPeriod(priceList, periods, ndx + ndx1);
					data[ndx].close = priceList.get(priceList.size() - (1 + ndx + ndx1)).getClose();
					// System.out.println("Close="+data[ndx].close);
				}
				DV[ndx1] = average(data);
			}
			K = DV[0];
			D = average(DV);
		} else {
			K = 0;
			D = 0;
		}
	}

	double average(Data[] data) {
		double kSum = 0;
		for (int s1 = 0; s1 < data.length; s1++) {
			kSum = kSum + calculateK(data[s1]);
		}
		return kSum / data.length;
	}

	double average(double[] data) {
		double kSum = 0;
		for (int s1 = 0; s1 < data.length; s1++) {
			kSum = kSum + data[s1];
		}
		return kSum / data.length;
	}

	double calculateK(Data data) {
		double K = (data.close - data.low) / (data.high - data.low) * 100;
		return K;
	}

	SimpleDateFormat	sdf	= new SimpleDateFormat("MM/dd/yyyy", Locale.US);

	Data scanHighLowInPeriod(List<Price> priceList, int periods, int offset) {
		Data data = new Data();
		data.high = priceList.get(priceList.size() - 1 - offset).getHigh();
		data.low = priceList.get(priceList.size() - 1 - offset).getLow();
		for (int ndx = priceList.size() - periods - offset; ndx < priceList.size() - offset; ndx++) {
			if (data.high < priceList.get(ndx).getHigh()) {
				data.high = priceList.get(ndx).getHigh();

			}
			if (data.low > priceList.get(ndx).getLow()) {
				data.low = priceList.get(ndx).getLow();
			}
			// System.out.println(" Date "+sdf.format(priceList.get(ndx).getDate())+" high="+priceList.get(ndx).getHigh()+" Low="+priceList.get(ndx).getLow()+" Close="+priceList.get(ndx).getClose());
		}
		return data;
	}
}
