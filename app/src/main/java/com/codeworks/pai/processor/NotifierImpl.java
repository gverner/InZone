package com.codeworks.pai.processor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.codeworks.pai.R;
import com.codeworks.pai.StudyActivity;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;
import com.codeworks.pai.db.model.Study;

import java.util.Date;
import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

public class NotifierImpl implements Notifier {
    private static final String TAG = NotifierImpl.class.getSimpleName();
    static int ONGOING_SERVICE_NOTIFICATION_ID = 55102;
    static int NETWORK_ERROR_NOTIFICATION_ID = 55101;
    public static final String CHANNEL_ID = "com.codeworks.pai";

    Context context;

    public NotifierImpl(Context context) {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see com.codeworks.pai.processor.Notifier#updateNotification(java.util.List)
     */
    @Override
    public void updateNotification(List<Study> studies) {
        Resources res = context.getResources();

        for (Study study : studies) {
            if (study.isValid() && !study.hasInsufficientHistory()) {
                Rules rules;
                if (MaType.S.equals(study.getMaType())) {
                    rules = new SmaRules(study);
                } else {
                    rules = new EmaRules(study);
                }
                rules.updateNotice();
                String additionalMessage = "";
                if (rules.hasTradedBelowMAToday() && !Notice.POSSIBLE_WEEKLY_DOWNTREND_TERMINATION.equals(study.getNotice())) {
                    additionalMessage = String.format(res.getString(R.string.notice_has_traded_below_ma_text), study.getSymbol());
                }
                boolean sendNotice = saveStudyNoticeIfChanged(study) && !Notice.NONE.equals(study.getNotice());

                Log.d(TAG, "SendNotice = " + sendNotice + " for " + study.getSymbol() + " Prot=" + study.getPortfolioId());
                if (sendNotice) {

                    sendNotice(study.getSecurityId(), res.getString(study.getNotice().getSubject()),
                            study.getPortfolioId() + ") " + String.format(res.getString(study.getNotice().getMessage()), study.getSymbol()) + "\n"
                                    + additionalMessage);
                }
            }
        }
    }

    boolean saveStudyNoticeIfChanged(Study study) {
        boolean changed = false;
        String[] projection = new String[]{StudyTable.COLUMN_ID, StudyTable.COLUMN_NOTICE, StudyTable.COLUMN_NOTICE_DATE};
        Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
        Cursor studyCursor = getContentResolver().query(studyUri, projection, null, null, null);
        try {
            ContentValues values = new ContentValues();
            values.put(StudyTable.COLUMN_NOTICE, study.getNotice().getIndex());
            values.put(StudyTable.COLUMN_NOTICE_DATE, StudyTable.noticeDateFormat.format(study.getNoticeDate() == null ? new Date() : study.getNoticeDate()));
            assert studyCursor != null;
            if (studyCursor.moveToFirst()) {
                Notice lastNotice = Notice.fromIndex(studyCursor.getInt(1));
                String lastNoticeDate = studyCursor.getString(2);
                assert lastNotice != null;
                Log.d(TAG,
                        "Notice upd " + study.getSymbol() + " p=" + study.getPortfolioId() + " last=" + lastNotice.getIndex() + " new="
                                + values.getAsString(StudyTable.COLUMN_NOTICE) + " last=" + lastNoticeDate + " new="
                                + values.getAsString(StudyTable.COLUMN_NOTICE_DATE));
                if (study.getNotice() != null && !study.getNotice().equals(lastNotice)) {
                    studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
                    if (getContentResolver().update(studyUri, values, null, null) != 1) {
                        Log.d(TAG, "Notice update failed");
                    }
                    changed = true;
                }
            } else {
                Log.d(TAG, "study not found " + study.toString());
            }
        } finally {
            studyCursor.close();
        }
        return changed;
    }

    /**
     * Create Notification from PaiStucyListActivity
     *
     * @param securityId Notification Id
     * @param title
     * @param text
     */
    public void sendNotice(long securityId, String title, String text) {
        Log.d(TAG, String.format("create notice title %1$s with text %2$s", title, text));


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(title).setContentText(text);

        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        mBuilder.setContentIntent(createBackStackIntent(context));
        addChannel(mBuilder);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(Long.valueOf(securityId).intValue(), mBuilder.build());

    }

    public void notifyUserWhenErrors(List<Study> studies) {
        int networkErrors = 0;
        for (Study study : studies) {
            if (study.hasNetworkError()) {
                networkErrors++;
            }
        }
        Log.d(TAG, "Network Error Count " + networkErrors);
        if (networkErrors > 0) {
            sendNotice(NETWORK_ERROR_NOTIFICATION_ID, "Network Error", "Prices Unavailable");
        } else {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NETWORK_ERROR_NOTIFICATION_ID);
        }
    }

    public void sendServiceNotice(int notifyId, String title, String text, int numMessages) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
                .setContentText(text);
        mBuilder.setAutoCancel(true);
        mBuilder.setOnlyAlertOnce(true);
        mBuilder.setContentText(text).setNumber(numMessages);
        mBuilder.setContentIntent(createBackStackIntent(context));
        // Because the ID remains unchanged, the existing notification is
        // updated.
        addChannel(mBuilder);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notifyId, mBuilder.build());
    }

    public void addChannel(NotificationCompat.Builder builder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(createNotificationChannel());
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private String createNotificationChannel() {
        NotificationChannel chan = new NotificationChannel(NotifierImpl.CHANNEL_ID, "InZone Channel", NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        //chan.setDescription(description); // TODO are we missing the description
        NotificationManager service = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        service.createNotificationChannel(chan);
        return NotifierImpl.CHANNEL_ID;
    }

    public PendingIntent createBackStackIntent(Context context) {
        // the started Activity.
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, StudyActivity.class);

        // The stack builder object will contain an artificial back stack for
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context); //
        // Adds the back stack for the Intent (but not the Intent itself)
        // stackBuilder.addParentStack(PaiStudyListActivity.class); // Adds the
        // Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    ContentResolver getContentResolver() {
        return context.getContentResolver();

    }
}

