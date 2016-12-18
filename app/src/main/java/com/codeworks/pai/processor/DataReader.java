package com.codeworks.pai.processor;

import java.util.Date;
import java.util.List;

import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.OptionType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Price;

import org.joda.time.DateTime;

public interface DataReader {

	public abstract boolean readDelayedPrice(Study security, List<String> errors);

	public abstract List<Price> readHistory(String symbol, List<String> errors);

	public abstract boolean readRTPrice(Study security, List<String> errors);
	
	public abstract Date latestHistoryDate(String symbol, List<String> errors);

    public abstract List<DateTime> readOptionExpirations(final String symbol, List<String> errors);

	public List<Option> readOptionPrice(final String symbol, final long expirationDate, List<String> errors);

}