package com.codeworks.pai.processor;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;

import com.codeworks.pai.db.model.Study;

import java.util.List;

public interface Notifier {

	public abstract void updateNotification(List<Study> studies);
	public abstract void sendNotice(long securityId, String title, String text);
	public abstract void sendServiceNotice(int notifyId, String title, String text, int numMessages);
	public void notifyUserWhenErrors(List<Study> studies);
	public void addChannel(NotificationCompat.Builder builder);
	public PendingIntent createBackStackIntent(Context context);

}