package com.codeworks.pai.study;

import java.util.List;

import com.codeworks.pai.db.model.Price;

public class ATR {
	private boolean			isFirst	= true;
	private double			emaAtr;
	private double          smaAtr;
	private int				periods;
	private int 			cnt;
	private double alpha;
	/**
	 * Construct an <tt>ATR</tt> instance.
	 * we are using SMA
	 * <blockquote><code><pre>  
	 * alpha = 2 / (numberPeriods + 1)  
	 * </pre></code></blockquote>
	 * 
	 * @param numberPeriods
	 */
	public ATR(int numberPeriods) {
		alpha = 2d / (numberPeriods + 1d); // EMA
		//alpha = 2d / (3 + 1d); // EMA
		periods = numberPeriods;
		cnt = 0;
		System.out.println("alpha="+alpha);
	}

	public double getEmaAtr() {
		return emaAtr;
	}
	public double getSmaAtr() {
		return smaAtr;
	}

	public double compute(double high, double low, double prevClose) {
		double tr = Math.max(Math.max(Math.abs(high - low), Math.abs(high - prevClose)), Math.abs(low - prevClose));
		//System.out.println("hi="+format(high)+" lo="+format(low)+" pCls="+format(prevClose)+" tr="+format(tr)+" hl="+format(Math.abs(high - low))+" hp="+format(Math.abs(high - prevClose))+" lp="+format(Math.abs(low - prevClose)));
		if (isFirst) {
			emaAtr = high - low; // on first use high - low
			smaAtr = emaAtr;
			isFirst = false;
			cnt++;
		} else {
			/* NOT NECESSARY TO start average at period */
			
			cnt++;
			
			if (cnt < (periods)) {
				emaAtr = (emaAtr + tr);
				smaAtr = emaAtr;
			} else if (cnt == (periods)) {
				emaAtr = (emaAtr + tr) / (periods);
				smaAtr = emaAtr;
			} else  {
				smaAtr = ((smaAtr * (periods - 1)) + tr) / periods;
				emaAtr = (tr * alpha) + (emaAtr * (1d - alpha));
			}

			//atr = ((atr * (periods - 1)) + tr) / periods;

			// -- Multiply the previous 14-day ATR by 13.
			// -- Add the most recent day's TR value.
			// -- Divide the total by 14
			//atr = (tr * alpha) + (atr * (1d - alpha));
		}
		//System.out.println("cnt="+cnt+"satr="+satr+ " eatr="+atr);
		return emaAtr;
	}

	public static double compute(List<Price> priceList, int noPeriods) {
		ATR atr = new ATR(noPeriods);
		int end = priceList.size();
		List<Price> subList = priceList.subList(end - Math.min(end, (noPeriods * 2)), end);
		Price prev = new Price();
		prev = subList.get(0);
		for (Price price : subList) {
			atr.compute(price.getHigh(), price.getLow(), prev.getClose());
			prev = price;
		}
		return atr.getEmaAtr();
	}
	/*
	static String format(double value) {
		return new BigDecimal(value).setScale(4, BigDecimal.ROUND_HALF_UP).toPlainString();
	}*/

}
