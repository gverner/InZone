package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.Study;

public interface Processor {
	public void onClose();
	public abstract List<Study> process(String symbol) throws InterruptedException;
	public abstract List<Study> updatePrice(String symbol) throws InterruptedException;
}