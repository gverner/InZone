package com.codeworks.pai.study;

import java.util.LinkedList;
import java.util.List;

import com.codeworks.pai.db.model.Price;

public class SMA {
	private LinkedList<Double> values = new LinkedList<Double>();
	private int length;
	private double sum = 0;

	public SMA(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException(
					"length must be greater than zero");
		}
		this.length = length;
	}

	public double getSum() {
		return sum;
	}
	
	public double getAvr() {
		return sum / length;
	}
	
	public double compute(double value) {
		if (values.size() == length) {
			sum -= ((Double) values.getFirst()).doubleValue();
			values.removeFirst();
		}
		sum += value;
		values.addLast(Double.valueOf(value));
		return sum / length;
	}
	
	public static double compute(List<Price> priceList, int noPeriods) {
		SMA ema = new SMA(noPeriods);
		for (Price price : priceList) {
			ema.compute(price.getClose());
		}
		return ema.getAvr();
	}

}
