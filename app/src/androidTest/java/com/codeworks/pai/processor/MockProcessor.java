package com.codeworks.pai.processor;

import com.codeworks.pai.db.model.Study;

import java.util.ArrayList;
import java.util.List;

public class MockProcessor implements Processor {
	int numberOfProcessCalls = 0;
	int numberOfUpdatePriceCalls = 0;
	
	@Override
	public List<Study> process(String symbol, boolean updateHistory) throws InterruptedException {
		numberOfProcessCalls++;
		List<Study> studies = new ArrayList<Study>();
		Study study = new Study("SPY");
		studies.add(study);
		study.setPrice(150.00d);
		study.setEmaLastMonth(150.00d);
		study.setEmaLastWeek(150.00d);
		study.setAverageTrueRange(0);
		study.setNotice(Notice.NONE);
		study.setEmaMonth(150.00d);
		study.setEmaWeek(150.00d);
		study.setPriceLastMonth(150.00d);
		study.setPriceLastWeek(150.00d);
		study.setName("S&P");
		return studies;
	}

	@Override
	public List<Study> updatePrice(String symbol) throws InterruptedException {
		numberOfUpdatePriceCalls++;
		return new ArrayList<Study>();
	}

	@Override
	public void onClose() {
		// TODO Auto-generated method stub
		
	}

}
