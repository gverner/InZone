package com.codeworks.pai.processor;

import java.util.Date;
import java.util.List;

import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Price;

public interface DataReader {

	public abstract boolean readCurrentPrice(Study security);

	public abstract List<Price> readHistory(String symbol);

	public abstract boolean readRTPrice(Study security);
	
	public abstract Date latestHistoryDate(String symbol);

}