package com.codeworks.pai.processor;

import com.codeworks.pai.db.model.Option;
import com.codeworks.pai.db.model.Price;
import com.codeworks.pai.db.model.Study;

import org.joda.time.DateTime;

import java.util.Date;
import java.util.List;

public interface DataReader {

    //public abstract boolean readDelayedPrice(Study security, List<String> errors);

    public abstract List<Price> readHistory(String symbol, List<String> errors);

    public abstract boolean readRTPrice(Study security, List<String> errors);

    boolean readSecurityName(final Study security, final List<String> errors);

    public abstract Date latestHistoryDate(String symbol, List<String> errors);

    public abstract List<DateTime> readOptionExpirations(final String symbol, List<String> errors);

    public List<Option> readOptionPrice(final String symbol, final long expirationDate, List<String> errors);

}