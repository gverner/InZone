package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.Study;

public interface Notifier {

	public abstract void updateNotification(List<Study> studies);
	public abstract void sendNotice(long securityId, String title, String text);
	public abstract void sendServiceNotice(int notifyId, String title, String text, int numMessages);
	public void notifyUserWhenErrors(List<Study> studies);


	}