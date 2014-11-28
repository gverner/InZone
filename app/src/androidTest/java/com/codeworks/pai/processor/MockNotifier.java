package com.codeworks.pai.processor;

import java.util.List;

import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.processor.Notifier;

public class MockNotifier implements Notifier {
	int numberOfCalls = 0;
	int numberOfStudies = 0;
	int numberOfSendNoticeCalls = 0;
	@Override
	public void updateNotification(List<Study> studies) {
		numberOfCalls++;
		for (Study study : studies) {
			numberOfStudies++;
			System.out.println("Mock Notifier received study "+study.toString());
		}
	}
	@Override
	public void sendNotice(long securityId, String title, String text) {
		numberOfSendNoticeCalls++;
	}
	@Override
	public void sendServiceNotice(int notifyId, String title, String text, int numMessages) {
		numberOfSendNoticeCalls++;
		
	}

}
