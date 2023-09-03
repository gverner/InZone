package com.codeworks.pai.study;

import com.codeworks.pai.db.model.Price;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class Grouper {
	
	/**
	 * Adds currentPrice
	 * 
	 * In order to byProtfolioId the last week end or month end the current date's Price is required.
	 * Which would not be included in the history unless its a week end.
	 * 
	 * Also currentPrice is added to the returned period List as this is needed in calculations.
	 * 
	 * @param inHistory
	 * @param period
	 * @param currentPrice
	 * @return
	 */
	public List<Price> periodList(List<Price> inHistory, Period period, Price currentPrice) {
		Collections.sort(inHistory);
		if (inHistory.get(inHistory.size() -1).getDate().before(currentPrice.getDate())) {
			inHistory.add(currentPrice);
		}
		List<Price> groupList = periodList(inHistory, period);
		if (groupList.get(groupList.size() -1).getDate().before(currentPrice.getDate())) {
			groupList.add(currentPrice);
		}
		return groupList;
	}
	/**
	 * periodList Loop through daily history and create list containing period
	 * ending (weekly or monthly) days.
	 * 
	 * @param inHistory list of daily prices excluding holidays
	 * @param period Period.Week or Period.Month
	 * @return
	 */
	public List<Price> periodList(List<Price> inHistory, Period period) {
		List<Price> history = new ArrayList<Price>(inHistory);
		Collections.sort(history);

		List<Price> periodList = new ArrayList<Price>();
		int lastDay = -1;
		Price lastPrice = null;
		for (Price price : history) {
			if (price.valid()) {
				Calendar cal = GregorianCalendar.getInstance(Locale.US);
				cal.setTime(price.getDate());
				if (Period.Week.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_WEEK);
					if (day <= lastDay) { // week change add = because 5 year history is weekly
						periodList.add(lastPrice);
					}
					lastPrice = price;
					lastDay = day;
				}
				if (Period.Month.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_MONTH);
					if (day < lastDay) { // month change
						periodList.add(lastPrice);
					}
					lastPrice = price;
					lastDay = day;
				}
			}
		}
		/* removed so we would calc last weeks value before adding last price
		if (Period.Week.equals(period)) {
			Calendar cal = GregorianCalendar.getInstance(Locale.US);
			int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
			cal.setTime(lastPrice.getDate());
			if (currentWeek > cal.get(Calendar.WEEK_OF_YEAR)) {
				periodList.add(lastPrice);
			}
		}
		if (Period.Month.equals(period)) {
			Calendar cal = GregorianCalendar.getInstance(Locale.US);
			int currentMonth = cal.get(Calendar.MONTH);
			cal.setTime(lastPrice.getDate());
			if (currentMonth > cal.get(Calendar.MONTH)) {
				periodList.add(lastPrice);
			}
		}  */
		return periodList;
	}

	/**
	 * Loop through daily price history and creates a sublist containing the
	 * (weekly or monthly) price records. This version returns the beginning of
	 * the period.
	 * 
	 * @param inHistory
	 * @param period
	 * @return
	 */
	public List<Price> periodListBeginning(List<Price> inHistory, Period period) {
		List<Price> history = new ArrayList<Price>(inHistory);
		Collections.sort(history);
		List<Price> periodList = new ArrayList<Price>();
		int lastDay = -1;
		int lastMonth = -1;
		for (Price price : history) {
			if (price.valid()) {
				Calendar cal = GregorianCalendar.getInstance(Locale.US);
				cal.setTime(price.getDate());
				if (Period.Week.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_WEEK);
					if (day < lastDay) { // week change
						periodList.add(price);
					}
					lastDay = day;
				}
				if (Period.Month.equals(period)) {
					int day = cal.get(Calendar.DAY_OF_MONTH);
					int month = cal.get(Calendar.MONTH);
					if (month != lastMonth) { // week change
						periodList.add(price);
					}
					lastMonth = month;
					lastDay = day;
				}
			}
		}
		return periodList;
	}
}
