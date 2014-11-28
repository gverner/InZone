package com.codeworks.pai.db.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import android.R.color;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.R;
import com.codeworks.pai.study.Period;


public abstract class RulesBase implements Rules {
	private String TAG = RulesBase.class.getName();
	protected Study	study;

	@Override
	public String formatNet(double net) {
		NumberFormat format = NumberFormat.getInstance(Locale.US);
		if (format instanceof DecimalFormat) {
			((DecimalFormat)format).applyPattern("+####.00;-####.00");
		}
		String result = format.format(net);
		return result;
	}
	
	@Override
	public boolean hasTradedBelowMAToday() {
		//Log.d(TAG, study.getSymbol()+" TradBelowMA="+(isUpTrendWeekly() && study.getLow() <  study.getMovingAverage(Period.Week))+" low="+study.getLow()+" ma="+PaiUtils.round(study.getMovingAverage(Period.Week)) + " maTyp="+study.getMaType() +" Id="+ study.getSecurityId());
		return isUpTrendWeekly() && study.getLow() > 0 && study.getLow() <  study.getMovingAverage(Period.Week);
	}
	
	@Override
	public int getBuyZoneTextColor() {
		if (isPriceInBuyZone()) {
			return Color.GREEN;
		} else if (isPossibleUptrendTermination(Period.Week)) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}
	}
	
	@Override
	public int getBuyZoneBackgroundColor() {
		if (isPriceInBuyZone()) {
			return Color.DKGRAY;
		} else if (isPossibleUptrendTermination(Period.Week)) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}
	}

	@Override
	public int getSellZoneTextColor() {
		if (isPriceInSellZone()) {
			return Color.GREEN;
		} else if (isPossibleDowntrendTermination(Period.Week)) {
			return Color.MAGENTA;
		} else {
			return Color.BLACK;
		}

	}

	@Override
	public int getSellZoneBackgroundColor() {
		if (isPriceInSellZone()) {
			return color.holo_green_dark;
		} else if (isPossibleDowntrendTermination(Period.Week)) {
			return color.holo_orange_light;
		} else {
			return color.background_light;
		}

	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPossibleUptrendTermination()
	 */
	@Override
	public boolean isPossibleUptrendTermination(Period period) {
		if (Period.Week.equals(period)) {
			return (isUpTrendWeekly() && study.getPrice() < study.getEmaWeek()); 
		} else {
			return (isUpTrendMonthly() && study.getPrice() < study.getEmaMonth()); 
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.codeworks.pai.db.model.Rules#isPossibleDowntrendTermination()
	 */
	@Override
	public boolean isPossibleDowntrendTermination(Period period) {
		if (Period.Week.equals(period)) {
			return (isDownTrendWeekly() && study.getPrice() > study.getEmaWeek());
		} else {
			return (isDownTrendMonthly() && study.getPrice() > study.getEmaMonth());
		}
	}

	@Override
	public boolean isUpTrendWeekly() {
		return isUpTrend(Period.Week);
	}

	@Override
	public boolean isUpTrendMonthly() {
		return isUpTrend(Period.Month);
	}

	@Override
	public boolean isDownTrendWeekly() {
		return !isUpTrendWeekly();
	}

	@Override
	public boolean isDownTrendMonthly() {
		return !isUpTrendMonthly();
	}


	@Override
	public String getTrendText(Resources res) {
		StringBuilder sb = new StringBuilder();
		if (isUpTrend(Period.Month)) {
			sb.append(res.getString(R.string.status_monthly_uptrend));
		} else {
			sb.append(res.getString(R.string.status_monthly_downtrend));
		}
		sb.append(" ");
		if (isUpTrend(Period.Week)) {
			sb.append(res.getString(R.string.status_weekly_uptrend));
		} else {
			sb.append(res.getString(R.string.status_weekly_downtrend));
		}
		return sb.toString();
	}
	/**
	 * Super Version to contain common alerts
	 */
	public StringBuilder getAdditionalAlerts(Resources res) {
		StringBuilder alert = new StringBuilder();

		if (study.hasDelayedPrice()) {
			alert.append(res.getString(R.string.alert_delayed_price));
			alert.append("\n");
		}
		if (study.hasInsufficientHistory()) {
			alert.append(res.getString(R.string.alert_insufficent_history));
			alert.append("\n");
		}
		if (study.hasNoPrice()) {
			alert.append(res.getString(R.string.alert_no_price));
			alert.append("\n");
		}
		return alert;
	}

}
