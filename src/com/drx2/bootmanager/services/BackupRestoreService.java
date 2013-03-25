package com.drx2.bootmanager.services;

import java.io.File;
import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;

import com.drx2.bootmanager.Install;
import com.drx2.bootmanager.MainActivity;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.Utilities;

public class BackupRestoreService extends Service {
	Context context;
	String whattodo;
	String backup;
	String backupshort;
	String slot;
	Boolean useemmc;
	Utilities u = new Utilities();
	public NotificationManager myNotificationManager;
	public static final int NOTIFICATION_ID = 2;
	public static final int NOTI_ID = 1;
	private PowerManager.WakeLock mWakeLock;
	Notification notification;
	PendingIntent contentIntent;
	Intent notificationIntent;
	CharSequence NotificationTitle;
	private static final String PREFS_DEVICE = "DeviceInfo";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// Auto-generated method stub
		return null;
	}
	//TODO Make an activity to stop backup or restores and make it the intent of notification
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context=getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		if(prefs.contains("screeninstallPref")){
			if(prefs.getBoolean("screeninstallPref", false)) {
				final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
				mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "");    
				mWakeLock.acquire();
			}
		}
		SharedPreferences LiveServ = getSharedPreferences(PREFS_DEVICE, 0);
		SharedPreferences.Editor editor = LiveServ.edit();
		editor.putString("BackupRestoreService", "Live");
		editor.commit();
		myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence NotificationTicket = "BootManager Running";
		CharSequence NotificationTitle = "Process Started";
		CharSequence NotificationContent = "";
		notification = new Notification(R.drawable.icon, NotificationTicket, 0);
		notificationIntent = new Intent(context, Install.class);
		contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, NotificationTitle, NotificationContent, contentIntent);
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(startId, notification);
		Bundle extras = intent.getExtras();
		whattodo = extras.getString("whattodo");
		backup = extras.getString("backup");
		backupshort = extras.getString("backupshort");
		slot = extras.getString("slot");
		File romfolder = new File(u.getExternalDirectory()+"/BootManager/"+slot);
    	if(!romfolder.exists()) romfolder.mkdirs();
		useemmc=prefs.getBoolean("useemmc", false);
        if(whattodo.contains("restore")){
        	BackupRestoreService.this.notify("Restoring Backup", "Restoring "+backupshort);
        	restoreThread threadActivity = new restoreThread(backup, backupshort);
			threadActivity.start();
        } else if(whattodo.contains("backup")){
        	Backup(slot);
        }
		return startId;
		
	}
	
	private void Backup(String slot){
		Calendar c = Calendar.getInstance();
	    	int month = c.get(Calendar.MONTH) + 1;
	    	int minute = c.get(Calendar.MINUTE);
	    	String minutes = String.valueOf(minute);
	    	    if(minutes.length() < 2){
	    		    minutes = "0"+minutes;
	    		  }
	    	String sDate = "-" + month + "-" + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.YEAR) + "-" + c.get(Calendar.HOUR_OF_DAY) + "." + minutes;
	    	BUThread threadbu = new BUThread(slot, "s"+slot.substring(3, 4), sDate.toString());
			threadbu.start();
	
	}
	
	private class BUThread extends Thread{
		String slot;
		String name;
		String date;
		private BUThread(String SLOT, String NAME, String DATE){
			this.slot = SLOT;
			this.name = NAME;
			this.date = DATE;
		}
		@Override
		public void run() {
			try {
				if(useemmc){
					BackupRestoreService.this.notify("Creating Backup", "Backing up "+slot);
   		    		u.zipper2(u.getExternalDirectory() + "/BootManager/" + slot, u.getExternalDirectory() + "/BootManager/Backup/"+name+date+"-EMMC.zip", u.getExternalDirectory() + "/BootManager/" + slot);
   		    	} else {
   		    		BackupRestoreService.this.notify("Creating Backup", "Backing up "+slot);
   		    		u.zipper2(u.getExternalDirectory() + "/BootManager/" + slot, u.getExternalDirectory() + "/BootManager/Backup/"+name+date+".zip", u.getExternalDirectory() + "/BootManager/" + slot);
   		    	}
   		    	}finally{
   		    		stopForeground(true);
    				finalnotify();
   		    }
		}
	}
	
	private void notify(String Title, String message){
		notification.setLatestEventInfo(this, Title, message, contentIntent);
    	startForeground(NOTI_ID, notification);
	}

	private void finalnotify(){
			u.log("final notify");
			myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			CharSequence NotificationTicket = "Boot Manager Complete";
			if(whattodo.contains("restore"))
				NotificationTitle = "Restore Complete!";
			else
				NotificationTitle = "Backup Complete!";
			CharSequence NotificationContent = "Process successful!";
			Notification notification = new Notification(R.drawable.icon, NotificationTicket, 0);
			Intent notificationIntent = null;
			notificationIntent = new Intent(context, MainActivity.class);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if(prefs.getBoolean("usesound", false)){
				if(prefs.contains("Audio")){
					notification.sound = Uri.parse(prefs.getString("Audio", ""));
				} else {
					notification.defaults |= Notification.DEFAULT_SOUND;
				}
			}
			if(prefs.getBoolean("usevibrate", false)){
				notification.defaults |= Notification.DEFAULT_VIBRATE;
			}
			PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.setLatestEventInfo(context, NotificationTitle, NotificationContent, contentIntent);
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			myNotificationManager.notify(NOTIFICATION_ID, notification);
			SharedPreferences KillServ = getSharedPreferences(PREFS_DEVICE, 0);
			SharedPreferences.Editor editor = KillServ.edit();
			editor.putString("BackupRestoreService", "Kill");
			editor.commit();
			if(mWakeLock!=null){
				mWakeLock.release();
			}
		
		}
	
	private class restoreThread extends Thread{
		String file;
		@SuppressWarnings("unused")
		String fileName;
		private restoreThread(String File, String Filename){
			this.file=File;
			this.fileName=Filename;
		}
		@Override
		public void run() {
			try {
	        	deleteDir(new File(u.getExternalDirectory() + "/BootManager/"+slot));
	        	(new File(u.getExternalDirectory() + "/BootManager/"+slot)).mkdir();
	        	u.unzip(file, u.getExternalDirectory() + "/BootManager/"+slot+"/", "", BackupRestoreService.this);
	        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 075 " + u.getExternalDirectory() + "/BootManager/"+slot+"/*");
	        }finally{		
				stopForeground(true);
				finalnotify();
			}
		}
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i=0; i<children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
	
}