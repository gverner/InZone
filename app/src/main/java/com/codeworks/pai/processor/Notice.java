package com.codeworks.pai.processor;

import com.codeworks.pai.R;

public enum Notice {
	// making it obvious that we are using index instead of using ordinal.
	NONE(0,0,0,0), 
	POSSIBLE_WEEKLY_UPTREND_TEMINATION(1,R.string.notice_possible_weekly_uptrend_termination, R.string.notice_possible_weekly_uptrend_termination_text, R.string.alert_possible_weekly_uptrend_termination), 
	POSSIBLE_WEEKLY_DOWNTREND_TERMINATION(2,R.string.notice_possible_weekly_downtrend_termination, R.string.notice_possible_weekly_downtrend_termination_text, R.string.alert_possible_weekly_downtrend_termination),
	IN_SELL_ZONE(3, R.string.notice_in_sell_zone, R.string.notice_in_sell_zone_text, R.string.alert_in_sell_zone),
	IN_BUY_ZONE(4, R.string.notice_in_buy_zone, R.string.notice_in_buy_zone_text, R.string.alert_in_buy_zone);
	
	int index;
	int subject;
	int message;
	int rule;
	
	Notice ( int index, int subject, int message, int rule) {
		this.index = index;
		this.subject = subject;
		this.message = message;
		this.rule = rule;
	}
	
	public int getIndex() {
		return index;
	}
	public int getSubject() {
		return subject;
	}
	public int getMessage() {
		return message;
	}
	public int getRule() {
		return rule;
	}
	
	public static Notice fromIndex(int index) {
		for (Notice notice : values()) {
			if (index == notice.getIndex()) {
				return notice;
			}
		}
		return null;
	}
	
}
