package com.drx2.bootmanager.utilities;


import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class USBOff extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); 
		notificationManager.cancel(AutoSdcard.NOTIFICATION_ID);
	}
}