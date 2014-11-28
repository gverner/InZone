package com.codeworks.pai.processor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(UpdateService.class.getName());
		serviceIntent.putExtra(UpdateService.SERVICE_ACTION, UpdateService.ACTION_BOOT);        
        context.startService(serviceIntent); 
	}
}
