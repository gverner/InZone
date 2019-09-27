package com.codeworks.pai.processor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
	String TAG = "AlarmReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received Start Service Intent");
		Intent newIntent = new Intent(context, UpdateService.class);
		newIntent.putExtras(intent.getExtras());
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(newIntent);
		} else {
			context.startService(newIntent);
		}
	}

}
