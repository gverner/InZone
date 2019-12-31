package com.codeworks.pai.processor;

import android.app.PendingIntent;
import android.content.Context;
import androidx.core.app.NotificationCompat;

import java.util.List;

import com.codeworks.pai.db.model.Study;

public class MockNotifier implements Notifier {
	int numberOfCalls = 0;
	int numberOfStudies = 0;
	int numberOfSendNoticeCalls = 0;
	int numberNotifyUserWhenErrors = 0;
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

	@Override
	public void notifyUserWhenErrors(List<Study> studies) {
		numberNotifyUserWhenErrors++;
	}

	@Override
	public void addChannel(NotificationCompat.Builder builder) {

	}

	@Override
	public PendingIntent createBackStackIntent(Context context) {
		return null;
	}

}
