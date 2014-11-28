package com.codeworks.pai.db.model;

import java.io.Serializable;

import com.codeworks.pai.processor.Notice;

public class PaiNotification implements Serializable {

	private static final long serialVersionUID = -7560173976396606668L;

	long securityId;
	String symbol;
	String title;
	String text;
	Notice notice;
	
	public long getSecurityId() {
		return securityId;
	}
	public void setSecurityId(long securityId) {
		this.securityId = securityId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Notice getNotice() {
		return notice;
	}
	public void setNotice(Notice notice) {
		this.notice = notice;
	}
	public String getSymbol() {
		return symbol;
	}
	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}
}
