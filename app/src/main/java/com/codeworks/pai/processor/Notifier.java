package com.codeworks.pai.processor;

import android.app.PendingIntent;
import android.content.Context;

import com.codeworks.pai.db.model.Study;

import java.util.List;

import androidx.core.app.NotificationCompat;

public interface Notifier {

    void updateNotification(List<Study> studies);

    void sendNotice(long securityId, String title, String text);

    void sendServiceNotice(int notifyId, String title, String text, int numMessages);

    void notifyUserWhenErrors(List<Study> studies);

    void addChannel(NotificationCompat.Builder builder);

    PendingIntent createBackStackIntent(Context context);

}