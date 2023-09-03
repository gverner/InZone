package com.codeworks.pai.processor;

import com.codeworks.pai.db.model.Study;

import java.util.List;

public interface Processor {
    public void onClose();

    public abstract List<Study> process(String symbol, boolean updateHistory) throws InterruptedException;

    public abstract List<Study> updatePrice(String symbol) throws InterruptedException;
}