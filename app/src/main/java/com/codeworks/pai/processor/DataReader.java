package com.codeworks.pai.processor;

import java.util.Date;
import java.util.List;

import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.OptionType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Price;

import org.joda.time.DateTime;

public interface DataReader {

	public abstract boolean readCurrentPrice(Study security, List<String> errors);

	public abstract List<Price> readHistory(String symbol, List<String> errors);

	public abstract boolean readRTPrice(Study security, List<String> errors);
	
	public abstract Date latestHistoryDate(String symbol, List<String> errors);

    public abstract List<DateTime> readOptionDates(final String symbol, List<String> errors);

    public abstract Option readOption(Option option);

}