package com.codeworks.pai.processor;

import java.util.Date;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.codeworks.pai.PaiUtils;
import com.codeworks.pai.R;
import com.codeworks.pai.StudyActivity;
import com.codeworks.pai.contentprovider.PaiContentProvider;
import com.codeworks.pai.db.StudyTable;
import com.codeworks.pai.db.model.EmaRules;
import com.codeworks.pai.db.model.MaType;
import com.codeworks.pai.db.model.Study;
import com.codeworks.pai.db.model.Rules;
import com.codeworks.pai.db.model.SmaRules;

public class NotifierImpl implements Notifier {
	private static final String TAG = NotifierImpl.class.getSimpleName();
	public static int ONGOING_SERVICE_NOTIFICATION_ID=55102;
	public static int NETWORK_ERROR_NOTIFICATION_ID = 55101;

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
		String[] projection = new String[] { StudyTable.COLUMN_ID, StudyTable.COLUMN_NOTICE, StudyTable.COLUMN_NOTICE_DATE };
		Uri studyUri = Uri.parse(PaiContentProvider.PAI_STUDY_URI + "/" + study.getSecurityId());
		Cursor studyCursor = getContentResolver().query(studyUri, projection, null, null, null);
		try {
			ContentValues values = new ContentValues();
			values.put(StudyTable.COLUMN_NOTICE, study.getNotice().getIndex());
			values.put(StudyTable.COLUMN_NOTICE_DATE, StudyTable.noticeDateFormat.format(study.getNoticeDate()==null? new Date(): study.getNoticeDate()));
			if (studyCursor.moveToFirst()) {
				Notice lastNotice = Notice.fromIndex(studyCursor.getInt(1));
				String lastNoticeDate = studyCursor.getString(2);
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
				} else {
					changed = false;
				}
			} else {
				Log.d(TAG,"study not found "+study.toString() );
			}
		} finally {
			studyCursor.close();
		}
		return changed;
	}

	/**
	 * Create Notification from PaiStucyListActivity
	 * 
	 * @param securityId  Notification Id
	 * @param title		
	 * @param text
	 */
	public void sendNotice(long securityId, String title, String text) {
		Log.d(TAG, String.format("create notice title %1$s with text %2$s", title, text));

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		String ringtoneName = sharedPreferences.getString(PaiUtils.PREF_RINGTONE, "none");
		Uri ringtoneUri;
		if ("none".equals(ringtoneName) || ringtoneName == null) {
			ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		} else {
			ringtoneUri = Uri.parse(ringtoneName);
		}
		
		boolean vibrate = sharedPreferences.getBoolean(PaiUtils.PREF_VIBRATE_ON, false);
		
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle(title).setContentText(text);
		
		mBuilder.setAutoCancel(true);
		mBuilder.setOnlyAlertOnce(true);
		mBuilder.setSound(ringtoneUri);
		if (vibrate) {
			long[] pattern = { 500, 100, 100, 500 };
			mBuilder.setVibrate(pattern);
		}
		mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
		mBuilder.setContentIntent(createBackStackIntent());

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
        Log.d(TAG, "Network Error Count "+networkErrors);
		if (networkErrors > 0 ) {
			sendNotice(NETWORK_ERROR_NOTIFICATION_ID,"Network Error", "Prices Unavailable");
		} else {
			NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(NETWORK_ERROR_NOTIFICATION_ID);
		}
	}

	public void sendServiceNotice(int notifyId, String title, String text, int numMessages) {
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher).setContentTitle(title)
				.setContentText(text);
		mBuilder.setAutoCancel(true);
		mBuilder.setOnlyAlertOnce(true);
		mBuilder.setContentText(text).setNumber(numMessages);
		mBuilder.setContentIntent(createBackStackIntent());
		// Because the ID remains unchanged, the existing notification is
		// updated.
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(notifyId, mBuilder.build());
	}

	PendingIntent createBackStackIntent() {
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

		PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
		return resultPendingIntent;
	}
	
	ContentResolver getContentResolver() {
		return context.getContentResolver();
		
	}
}

