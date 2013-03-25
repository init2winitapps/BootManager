package com.drx2.bootmanager.services;

import java.io.File;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.CallLogBackupRestore;
import com.drx2.bootmanager.utilities.Utilities;

public class BootService extends Service {
	Context context;
	Utilities u = new Utilities();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public void onStart(Intent intent, int startId) {
			context=getApplicationContext();
			while(!android.os.Environment.MEDIA_MOUNTED.equals("mounted")){
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(Utilities.device().contains("tuna")){
				if(u.getExternalDirectory().equals("/bootmanager/media")){
					notifySDFail();
				}
			}
			loadCallLog(context);
	    	loadSMS(context);
	    	if(android.os.Build.DEVICE.equals("vigor")){
	    		File ph98 = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/ext_sd/PH98IMG.zip");
	    		File delete = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/ext_sd/DELETEPH98IMG");
	    		if(ph98.exists()){
	    			if(delete.exists()){
	    				ph98.delete();
	    				delete.delete();
	    			}
	    		}
	    	}
	    	stopSelf();

	}
	
	private void notifySDFail() {
		int icon = R.drawable.icon;
        long when = System.currentTimeMillis();
        String message = "Files saved to sdcard will not be saved.";
        Notification notification = new Notification(icon, message, when);
        notification.setLatestEventInfo(context, "Error Mounting sdcard", message, null);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(5, notification);
		
	}

	private void loadCallLog(Context context){
		if(new File(u.getExternalDirectory()+"/BootManager/.zips/phonelog").exists()){
			u.log("Syncing call log");
			CallLogBackupRestore c = new CallLogBackupRestore();
			c.addCall(context);
		}
	}
	
	private void loadSMS(Context context){
		if(new File(u.getExternalDirectory()+"/BootManager/.zips/smslog").exists()){
			u.log("syncing sms");
			CallLogBackupRestore c = new CallLogBackupRestore();
			c.restoreSMS(context);
		}
	}
	
	
}
