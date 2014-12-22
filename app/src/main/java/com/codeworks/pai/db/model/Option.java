package com.codeworks.pai.db.model;

import org.joda.time.DateTime;

/**
 * Created by Glenn Verner on 11/29/14.
 */
public class Option implements Cloneable {

    String symbol;
    OptionType type;
    DateTime expires;
    double strike;
    double bid = 0;
    double ask = 0;

    double price = 0;

    public Option(String symbol, OptionType putCall, double strike, DateTime expires) {
        this.symbol = symbol;
        this.type = putCall;
        this.strike = strike;
        this.expires = expires;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public OptionType getType() {
        return type;
    }

    public void setType(OptionType type) {
        this.type = type;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public double getStrike() {
        return strike;
    }

    public DateTime getExpires() {
        return expires;
    }

    public void setExpires(DateTime expires) { this.expires = expires; }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
