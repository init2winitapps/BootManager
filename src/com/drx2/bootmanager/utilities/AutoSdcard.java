package com.drx2.bootmanager.utilities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.drx2.bootmanager.R;

public class AutoSdcard extends BroadcastReceiver {
	
	Utilities u = new Utilities();
	private static final String PREFS_DEVICE = "DeviceInfo";
	public NotificationManager myNotificationManager;
	public static final int NOTIFICATION_ID = 5;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences settings = context.getSharedPreferences(PREFS_DEVICE, 0);
		String device = settings.getString("device", "");
		if(!device.equalsIgnoreCase("tuna")){
			if(prefs.contains("sdnotifyPref")){
				if(!(prefs.getBoolean("sdnotifyPref", false))) {
					myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					CharSequence NotificationTicket = context.getString(R.string.usb_connected);
					CharSequence NotificationTitle = context.getString(R.string.usb_connected);
					CharSequence NotificationContent = context.getString(R.string.useSdMount);
					Notification notification = new Notification(R.drawable.usbicon, NotificationTicket, 0);
					Intent notificationIntent = new Intent(context, SdcardOff.class);
					PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
					notification.setLatestEventInfo(context, NotificationTitle, NotificationContent, contentIntent);
					notification.flags |= Notification.FLAG_ONGOING_EVENT;
					myNotificationManager.notify(NOTIFICATION_ID, notification);
				}
			}else{
				myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				CharSequence NotificationTicket = context.getString(R.string.usb_connected);
				CharSequence NotificationTitle = context.getString(R.string.usb_connected);
				CharSequence NotificationContent = context.getString(R.string.useSdMount);
				Notification notification = new Notification(R.drawable.usbicon, NotificationTicket, 0);
				Intent notificationIntent = new Intent(context, SdcardOff.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				notification.setLatestEventInfo(context, NotificationTitle, NotificationContent, contentIntent);
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				myNotificationManager.notify(NOTIFICATION_ID, notification);
			}
		}
		String sdcardblock = settings.getString("sdcard", "");
		String emmc;
	    if(device.contains("vigor"))
	    	emmc = settings.getString("ext_sd", "");
	    else 
	    	emmc = settings.getString("emmc", "");
	    if(prefs.getBoolean("automountPref", false)) {
	    	u.execCommand("echo " + sdcardblock + " > "+ settings.getString("lunfile", "/sys/devices/platform/usb_mass_storage/lun0/file"));
	        if(device.equalsIgnoreCase("inc")||device.equalsIgnoreCase("vigor")){
	        	u.execCommand("echo " + emmc + " > "+ settings.getString("lunfile1", "/sys/devices/platform/usb_mass_storage/lun1/file"));
	        } 
	        Intent startActivity = new Intent();
	        startActivity.setClass(context, SdcardOff.class);
	        startActivity.setAction(SdcardOff.class.getName());
	        startActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
	        context.startActivity(startActivity);
	    }
	}
}