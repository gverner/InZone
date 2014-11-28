package com.codeworks.pai.db.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Price implements Comparable<Price>, Serializable {
	private static final long serialVersionUID = 501625055770734658L;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd E", Locale.US);
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	Date date;
	double open;
	double high;
	double low;
	double close;
	double adjustedClose;
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}
	public double getAdjustedClose() {
		return adjustedClose;
	}
	public void setAdjustedClose(double adjustedClose) {
		this.adjustedClose = adjustedClose;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Date=");
		sb.append(sdf.format(date));
		sb.append(" close=");
		sb.append(close);
		sb.append(" adjustedClose=");
		sb.append(adjustedClose);
		return sb.toString();
	}
	public boolean valid() {
		return (date != null && close != 0);
	}

	@Override
	public int compareTo(Price another) {
		if (date != null && another.getDate() != null) {
			return date.compareTo((Date) another.getDate());
		} else {
			return 0;
		}
	}
}
