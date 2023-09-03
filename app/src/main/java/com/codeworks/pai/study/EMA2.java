package com.codeworks.pai.study;

import com.codeworks.pai.db.model.Price;

import java.math.BigDecimal;
import java.util.List;

public class EMA2 {
	//private static final String TAG = EMA2.class.getSimpleName();
	private double alpha;
	private boolean isFirst = true;
	private double avg;

	public double getAvg() {
		return avg;
	}

	public EMA2(int length) {
		alpha = 2d / (length + 1d);
		//Log.d(TAG, "(1d - alpha) " + (1d - alpha));
		//Log.d(TAG, "alpha = 2 / (periods + 1) = " + alpha);
	}

	public double compute(double value) {
		if (isFirst) {
			avg = value;
			isFirst = false;
		} else {
			avg = (value * alpha) + (avg * (1d - alpha));
			// avg = (value - avg) * alpha + avg;
		}
		return avg;
	}

	public static double compute(List<Price> priceList, int noPeriods) {
		EMA2 ema = new EMA2(noPeriods);
		for (Price price : priceList) {
			ema.compute(price.getClose());
		}
		return ema.getAvg();
	}
	
	public static String format(double value) {
		return new BigDecimal(value).setScale(2, BigDecimal.ROUND_HALF_UP).toPlainString();
	}
}
