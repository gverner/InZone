package com.codeworks.pai.processor;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import static com.codeworks.pai.processor.AlarmSetup.DAILY_START_INTENT_ID;

public class AlarmReceiver extends BroadcastReceiver {
	String TAG = "AlarmReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received Start Service Intent");
		intent = new Intent(context, UpdateService.class);
		intent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_SCHEDULE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}

}
