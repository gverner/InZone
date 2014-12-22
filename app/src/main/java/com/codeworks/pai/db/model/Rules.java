package com.codeworks.pai.db.model;

import android.content.res.Resources;

import com.codeworks.pai.study.Period;

public interface Rules {

	public abstract double calcUpperSellZoneTop(Period period);

	public abstract double calcUpperSellZoneBottom(Period period);

	public abstract double calcUpperBuyZoneTop(Period period);

	public abstract double calcUpperBuyZoneBottom(Period period);

	public abstract double calcLowerSellZoneTop(Period period);

	public abstract double calcLowerSellZoneBottom(Period period);

	public abstract double calcLowerBuyZoneTop(Period period);

	public abstract double calcLowerBuyZoneBottom(Period period);
	
	public abstract boolean isWeeklyUpperSellZoneExpandedByMonthly();

	public abstract boolean isWeeklyLowerBuyZoneCompressedByMonthly();

	public abstract double calcBuyZoneBottom();

	public abstract double calcBuyZoneTop();

	public abstract double calcSellZoneBottom();

	public abstract double calcSellZoneTop();

    public abstract double AOACall();

    public abstract double AOBPut();

	public abstract boolean isPriceInBuyZone();

	public abstract boolean isPriceInSellZone();
	
	public abstract boolean hasTradedBelowMAToday();

	public abstract boolean isUpTrendWeekly();

	public abstract boolean isUpTrendMonthly();

	public abstract boolean isUpTrend(Period period);

	public abstract boolean isDownTrendWeekly();

	public abstract boolean isDownTrendMonthly();

	public abstract boolean isPossibleTrendTerminationWeekly();

	public abstract boolean isPossibleUptrendTermination(Period period);

	public abstract boolean isPossibleDowntrendTermination(Period period);

	public abstract String formatNet(double net);
	
	public abstract String inCash();
		
	public abstract String inCashAndPut();
	
	public abstract String inStock();
	
	public abstract String inStockAndCall();
	
	public abstract MaType getMaType();
	
	public abstract int getBuyZoneTextColor();
	
	public abstract int getBuyZoneBackgroundColor();
	
	public abstract int getSellZoneTextColor();
	
	public abstract int getSellZoneBackgroundColor();
	
	public abstract String getTrendText(Resources res);
	
	public abstract StringBuilder getAdditionalAlerts(Resources res);
	
	public abstract void updateNotice();
}