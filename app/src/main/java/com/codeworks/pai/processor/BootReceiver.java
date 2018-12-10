package com.codeworks.pai.processor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, UpdateService.class);
		serviceIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_BOOT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(intent);
		} else {
			context.startService(intent);
		}
	}
}
