package com.drx2.bootmanager.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.StatFs;
import android.preference.PreferenceManager;

import com.drx2.bootmanager.Install;
import com.drx2.bootmanager.Loader;
import com.drx2.bootmanager.MainActivity;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.Script;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.drx2.bootmanager.utilities.Utilities;

public class NandRestoreService extends Service {
	public NotificationManager myNotificationManager;
	public static final int NOTIFICATION_ID = 2;
	public static final int NOTI_ID = 1;
	private PowerManager.WakeLock mWakeLock;
	Utilities u = new Utilities();
	ShellCommand s = new ShellCommand();
	Script script = new Script();
	Loader l = new Loader();
	private static final String PREFS_DEVICE = "DeviceInfo";
	String appKey = "bmk1";
	Context context;
	String board; 
	String sdcard;
	String ext_sd;
	String systemsize;
	String datasize;
	String cachesize;
	String nandroid;
	String romshort;
	String emmc;
	String busybox;
	String bbpath;
	String ext;
	String currentratio;
	String currentlowmem;
	String slot;
	String error;
	Boolean wipe;
	Boolean useemmc;
	Boolean threadStop;
	Boolean largeboot;
	Boolean continueInstall=true;
	int key;
	int exit = 0;
	Notification notification;
	PendingIntent contentIntent;
	Intent notificationIntent;
	int StartId;

		@Override
		public IBinder onBind(Intent arg0) {

			return null;
		}
		
		public void onStart(Intent intent, int startId) {
			context=getApplicationContext();
			SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
			if(continueInstall){
				if(prefs.contains("screeninstallPref")){
					if(prefs.getBoolean("screeninstallPref", false)) {
						final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
						mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "");    
						mWakeLock.acquire();
					}
				}
				StartId=startId;
				threadStop=false;
				myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				CharSequence NotificationTicket = "BootManager Running";
				CharSequence NotificationTitle = "Installing Zip";
				CharSequence NotificationContent = ""; 
				notification = new Notification(R.drawable.icon, NotificationTicket, 0);
				notificationIntent = new Intent(context, Install.class);
				contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				notification.setLatestEventInfo(context, NotificationTitle, NotificationContent, contentIntent);
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				startForeground(startId, notification);
		        wipe=true;
		    	board=settings.getString("device", "");
		    	sdcard=settings.getString("sdcard", "");
		    	ext_sd=settings.getString("ext_sd", "");
		    	emmc=settings.getString("emmc", "");
		    	largeboot=prefs.getBoolean("forcelargeboot", false);
		    	useemmc=settings.getBoolean("useemmc", false);
		    	systemsize=settings.getString("systemsize", "");
		    	datasize=settings.getString("datasize", "");
		    	cachesize=settings.getString("cachesize", "");
		    	busybox=(context.getFilesDir().getAbsolutePath() + "/busybox");
		    	Bundle extras = intent.getExtras();
		    	ext=extras.getString("ext");
		    	nandroid=extras.getString("nandroid");
		    	romshort=(nandroid.substring(nandroid.lastIndexOf("/")+1));
		    	slot=extras.getString("slot");
		    	File romfolder = new File(u.getExternalDirectory()+"/BootManager/"+slot);
		    	if(!romfolder.exists()) romfolder.mkdirs();
		    	File asec = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/.android_secure");
		    	if(!asec.exists())asec.mkdirs();
		    	SharedPreferences LiveServ = getSharedPreferences(PREFS_DEVICE, 0);
	    		SharedPreferences.Editor editor = LiveServ.edit();
	    		editor.putString("NandService", "Live");
	    		editor.commit();
		    	notify("Restoring "+romshort, "Please wait...");
		    	installThread threadActivity = new installThread(nandroid, board, slot, sdcard);
				//threadActivity.setPriority(Thread.MAX_PRIORITY);
				threadActivity.start();
		    	}
		}

		
		private void notify(String Title, String message){
			notification.setLatestEventInfo(this, Title, message, contentIntent);
			startForeground(NOTI_ID, notification);
		}

		private void finalnotify(){
			u.log("is this getting called?");
			if(!threadStop){
				u.log("final notify");
				myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				CharSequence NotificationTicket = "Boot Manager Install Complete";
				CharSequence NotificationTitle = "Install Complete!";
				CharSequence NotificationContent = romshort + " Successfully Installed!";
				Notification notification = new Notification(R.drawable.icon, NotificationTicket, 0);
				Intent notificationIntent = new Intent(context, MainActivity.class);
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				if(prefs.getBoolean("usesound", false)){
					if(prefs.contains("Audio")){
						String uriSt = prefs.getString("Audio", "");
						if(uriSt!=null) notification.sound = Uri.parse(uriSt);
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
	    		editor.putString("NandService", "Kill");
	    		editor.commit();
			} else {
				if(error!=null)
					u.log("final notify error "+error);
				else
					u.log("final notify error");
				myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
				CharSequence NotificationTicket = "Boot Manager Install Complete";
				CharSequence NotificationTitle = "Install Error";
				CharSequence NotificationContent;
				if(error!=null)
					NotificationContent = error;
				else
					NotificationContent = "There has been an error during install";
				Notification notification = new Notification(R.drawable.icon, NotificationTicket, 0);
				Intent notificationIntent;
				notificationIntent = new Intent(context, MainActivity.class);
				PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
				notification.setLatestEventInfo(context, NotificationTitle, NotificationContent, contentIntent);
				notification.flags |= Notification.FLAG_ONGOING_EVENT;
				myNotificationManager.notify(NOTIFICATION_ID, notification);
				SharedPreferences KillServ = getSharedPreferences(PREFS_DEVICE, 0);
				SharedPreferences.Editor editor = KillServ.edit();
				editor.putString("NandService", "Kill");
				editor.commit();
				if(mWakeLock!=null){
					mWakeLock.release();
				}
			}
		}

		private class installThread extends Thread{
	    	String vrom;
	    	String vboard;
	    	String vslot;
	    	String vsdcard;
	    	
	    	private installThread(String ROM, String BOARD, String SLOT, String SDCard){
	    		this.vrom = ROM;
	    		this.vboard = BOARD;
	    		this.vslot = SLOT;
	    		this.vsdcard = SDCard;
	    	}
	    	public void run(){
	    		super.run();
	    		try{
	    			if(!board.contains("tuna")){
						getdirtyRatio();
						u.execCommand("echo 5 > /proc/sys/vm/dirty_ratio");
						u.execCommand("echo 1536,2048,4096,5120,5632,6144 > /sys/module/lowmemorykiller/parameters/minfree");
					}
	    			u.log("Restoring Nandroid "+vrom);
	    			installrom(vrom, vslot);
	    			NandRestoreService.this.notify("Restoring "+romshort, "Editing Boot.img");
	    			editboot(vrom, vboard, vslot, vsdcard);
	    			NandRestoreService.this.notify("Restoring "+romshort, "Cleaning up...");			
	    			cleanUp(context);
	    			}finally{
	    				if(!board.contains("tuna")){
	    					u.execCommand("echo "+currentratio+" > /proc/sys/vm/dirty_ratio");
	    					u.execCommand("echo "+ currentlowmem+" > /sys/module/lowmemorykiller/parameters/minfree");
	    				}
	    				NandRestoreService.this.notify(romshort+" Installed", "Click here to open reboot options");
	    				stopForeground(true);
	    				finalnotify(); 
	    			}
	    	}
		}
		
		
		private void editboot(String rom, String board, String slot, String sdcardblock ) {
			u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox chmod 777 /data/local/tmp");
			if(board.contains("shadow")||board.contains("droid2")||board.contains("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
				String boardname ="mapphone_cdma";
				String sd;
				if(board.equals("spdyer")){
					if(Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").contains("internal")){
						sd="/mnt/sdcard";
					} else {
						SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
						sdcardblock=settings.getString("ext_sd", "");
						sd="/mnt/sdcard-ext";//TODO Gflam look to see if it's mounting on /mnt/sdcard-ext or straight to sdcard-ext
					}
				} else {
					sd = "/mnt/sdcard";
				}
				File hbzip = new File("/data/local/tmp/system/etc/hijack-boot.zip");
				if(hbzip.exists()){
					//Do something if it's already a 2ndInit rom
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/busybox");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/hijack-boot");
					u.unzip(hbzip.toString(), "/data/local/tmp/hijack-boot/", "", context);
					script.installScriptX(context);
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/edit.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+context.getFilesDir().getAbsolutePath() + "/morebinarys/busybox /data/local/tmp/busybox");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/recovery_mode /data/local/tmp/recovery_mode");
					SharedPreferences keycheck = getSharedPreferences(PREFS_DEVICE, 0);
					if(keycheck.contains("key"))key=0;
					CommandResult erd = s.su.runWaitFor("/data/local/tmp/edit.sh " + boardname + " " + slot + " " + sdcardblock+" "+busybox+" "+context.getFilesDir().getAbsolutePath()+"/zip"+" "+key+" "+ext+" "+sd);
					if(erd.stderr!=null){
						u.log(erd.stderr);
					}
					if(erd.stdout!=null){
						u.log(erd.stdout);
					}
					if((erd.exit_value).toString()!=null){
						u.log("Exit Value of script is "+(erd.exit_value).toString());
					}
					CommandResult cp =s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox cp /data/local/tmp/hijack-boot.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/hijack-boot.zip");
					if(cp.stderr!=null){
						u.log(cp.stderr);
					}
					if(cp.stdout!=null){
						u.log(cp.stdout);
					}
					u.log("Setting rom name");
					u.execCommand("echo "+romshort+" > "+u.getExternalDirectory()+"/BootManager/"+slot+"/name");
					if(!new File(u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip").exists()) {
						u.log("Moving update.zip");
						//TODO If you download the update.zip for internal to internal sdcard/bootmanager/.zips and external update to sdcard-ext/bootmanager/.zips
						//and name them BootManager-rom1-signed.zip then all the update.zip code should work as it is now
						File update = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-"+slot+"-signed.zip");
						if(update.exists()){
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager-"+slot+"-signed.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip");
						} else {
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/updatezip");
							u.unzip(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip", "/data/local/tmp/updatezip/", "", context);
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/updatezip/movehijack.sh");
							try{
					    		FileWriter fstream = new FileWriter("/data/local/tmp/updatezip/movehijack.sh");
					    		BufferedWriter out = new BufferedWriter(fstream);
					    		if(board.equals("spyder")||board.equals("maserati"))
					    			//TODO internal/external This should edit them correctly...make sure we are pulling from the right zip in todo right above
					    			if(installInternal()){
					    				out.write("#!/sbin/sh\n#\n\n#Do not modify this file!\n\nmount /dev/block/mmcblk1p20 system\ncp /sdcard/BootManager/"+slot+"/hijack-boot.zip /system/etc/hijack-boot.zip\nsync");
					    			} else {
					    				out.write("#!/sbin/sh\n#\n\n#Do not modify this file!\n\nmount /dev/block/mmcblk1p20 system\ncp /sdcard-ext/BootManager/"+slot+"/hijack-boot.zip /system/etc/hijack-boot.zip\nsync");
					    			}
					    		else
					    			out.write("#!/sbin/sh\n#\n\n#Do not modify this file!\n\nmount /dev/block/mmcblk1p21 system\ncp /sdcard/BootManager/"+slot+"/hijack-boot.zip /system/etc/hijack-boot.zip\nsync");
					    		out.close();
					    	}catch (Exception e){
					    		u.log(e.toString());
					    	}
					    	try{
					    		FileWriter fstream = new FileWriter("/data/local/tmp/editupdatezip.sh");
					    		BufferedWriter out = new BufferedWriter(fstream);
					    		out.write("#!/data/data/com.drx2.bootmanager/files/busybox sh\n#\n\n#Do not modify this file!\n\ncd /data/local/tmp/updatezip\n"+context.getFilesDir().getAbsolutePath() + "/zip -r /data/local/tmp/update *");
					    		out.close();
					    	}catch (Exception e){
					    		u.log(e.toString());
					    	}
					    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/editupdatezip.sh");
					    	u.execCommand("/data/local/tmp/editupdatezip.sh");
					    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp /data/local/tmp/update.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip");
					    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/editupdate.zip.sh");
						}
					}
				}else{
					u.log("getting boot.zip");
					File hijack = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/hijack-boot.zip");//TODO depending on how you store internal/external hijacks you might have to put an if statement here to get the right one...do the same thing in nandrestoreservice
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/BootManager/"+slot+"/hijack-boot.zip");
					if(!(hijack.exists())){
						//TODO get internal/external somehow here
						//Same theory as update.zips if you download the right base file for internal/external to corresponding sdcard .zips folder i think this will work
						File premadehijack = new File(u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-"+slot+".zip");
						if(premadehijack.exists()){
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-"+slot+".zip "+ u.getExternalDirectory()+"/BootManager/"+slot+"/hijack-boot.zip");
						} else {
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/busybox");
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/hijack-boot");
							//TODO get internal/external somehow here same - should work if we download right file to right sdcard
							u.unzip(u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-rom1.zip", "/data/local/tmp/hijack-boot/", "", context);
							if(board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
								script.editStockHijackRazrScript(context);
							} else {
								script.editStockHijackXScript(context);
							}
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/edit.sh");
							SharedPreferences keycheck = getSharedPreferences(PREFS_DEVICE, 0);
							if(keycheck.contains("key"))key=0;
							CommandResult erd = s.su.runWaitFor("/data/local/tmp/edit.sh " +  slot +" "+busybox+" "+context.getFilesDir().getAbsolutePath()+"/zip"+" "+key+" "+ext+" "+sd);
							if(erd.stderr!=null){
								u.log(erd.stderr);
							}
							if(erd.stdout!=null){
								u.log(erd.stdout);
							}
							if((erd.exit_value).toString()!=null){
								u.log("Exit Value of script is "+(erd.exit_value).toString());
							}
							CommandResult cp =s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox cp /data/local/tmp/hijack-boot.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/hijack-boot.zip");
							if(cp.stderr!=null){
								u.log(cp.stderr);
							}
							if(cp.stdout!=null){
								u.log(cp.stdout);
							}
						}
					}
				}
				u.log("Setting rom name");
				u.execCommand("echo "+romshort+" > "+u.getExternalDirectory()+"/BootManager/"+slot+"/name");
				if(!new File(u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip").exists()) {
					u.log("Copying update.zip");
					//TODO If you download the update.zip for internal to internal sdcard/bootmanager/.zips and external update to sdcard-ext/bootmanager/.zips
					//and name them BootManager-rom1-signed.zip then all the update.zip code should work as it is now
					File update = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-"+slot+"-signed.zip");
					if(update.exists()){
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager-"+slot+"-signed.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip");
					} else {
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/updatezip");
						u.unzip(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip", "/data/local/tmp/updatezip/", "", context);
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/updatezip/movehijack.sh");
						try{
				    		FileWriter fstream = new FileWriter("/data/local/tmp/updatezip/movehijack.sh");
				    		BufferedWriter out = new BufferedWriter(fstream);
				    		if(board.equals("spyder")||board.equals("maserati"))
				    			//TODO internal/external This should edit them correctly...make sure we are pulling from the right zip in todo right above
				    			if(installInternal()){
				    				out.write("#!/sbin/sh\n#\n\n#Do not modify this file!\n\nmount /dev/block/mmcblk1p20 system\ncp /sdcard/BootManager/"+slot+"/hijack-boot.zip /system/etc/hijack-boot.zip\nsync");
				    			} else {
				    				out.write("#!/sbin/sh\n#\n\n#Do not modify this file!\n\nmount /dev/block/mmcblk1p20 system\ncp /sdcard-ext/BootManager/"+slot+"/hijack-boot.zip /system/etc/hijack-boot.zip\nsync");
				    			}
				    		else 
				    			out.write("#!/sbin/sh\n#\n\n#Do not modify this file!\n\nmount /dev/block/mmcblk1p21 system\ncp /sdcard/BootManager/"+slot+"/hijack-boot.zip /system/etc/hijack-boot.zip\nsync");
				    		out.close();
				    	}catch (Exception e){
				    		u.log(e.toString());
				    	}
				    	try{
				    		FileWriter fstream = new FileWriter("/data/local/tmp/editupdatezip.sh");
				    		BufferedWriter out = new BufferedWriter(fstream);
				    		out.write("#!/data/data/com.drx2.bootmanager/files/busybox sh\n#\n\n#Do not modify this file!\n\ncd /data/local/tmp/updatezip\n"+context.getFilesDir().getAbsolutePath() + "/zip -r /data/local/tmp/update *");
				    		out.close();
				    	}catch (Exception e){
				    		u.log(e.toString());
				    	}
				    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/editupdatezip.sh");
				    	u.execCommand("/data/local/tmp/editupdatezip.sh");
				    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp /data/local/tmp/update.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip");
				    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/editupdate.zip.sh");
					}
				}
			} else {
			if((new File(nandroid+"/boot.img")).exists()){
				//If there's a boot.img to edit we delete the old on on sdcard
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+nandroid+"/boot.img /data/local/tmp/boot.img");
				u.log("copying boot.img from nandroid");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img");
			}
			//TWRP backups for Thunderbolt
			if((new File(nandroid+"/boot.win")).exists()){
				//If there's a boot.img to edit we delete the old on on sdcard
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+nandroid+"/boot.win /data/local/tmp/boot.img");
				u.log("copying boot.img from nandroid");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img");
			}
			if((new File(nandroid+"/boot.emmc.win")).exists()){
				//If there's a boot.img to edit we delete the old on on sdcard
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+nandroid+"/boot.win /data/local/tmp/boot.img");
				u.log("copying boot.img from nandroid");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img");
			}
			int count = 1;
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/busybox");
			if(largeboot){
				if(board.equals("otter") || board.equalsIgnoreCase("shooteru") || board.equalsIgnoreCase("shooter") || board.equalsIgnoreCase("pyramid") || board.equalsIgnoreCase("vivow") || board.equalsIgnoreCase("vivo") || board.equalsIgnoreCase("mecha") || board.equalsIgnoreCase("spade") || board.equalsIgnoreCase("runnymede")){
					script.installScript3d(context);
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/morebinarys/busybox /data/local/tmp/busybox");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
				} else if(board.equals("ruby") || board.equals("vigor") || board.equals("holiday")) {
					if(Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").contains("internal")){
						script.installScript3d(context);
					} else {
						script.vigorScript(context);
					}
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/morebinarys/busybox /data/local/tmp/busybox");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
				} else {
					script.installScript(context);
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/morebinarys/busybox /data/local/tmp/busybox");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/e2fsck /data/local/tmp/e2fsck");
				}
			} else {
				if(board.equals("otter") || board.equalsIgnoreCase("shooteru") || board.equalsIgnoreCase("doubleshot") || board.equalsIgnoreCase("shooter") || board.equalsIgnoreCase("pyramid") || board.equalsIgnoreCase("vivow") || board.equalsIgnoreCase("vivo") || board.equalsIgnoreCase("mecha") || board.equalsIgnoreCase("spade")){
					script.smallInstallScript3d(context);
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/morebinarys/busybox "+u.getExternalDirectory()+"/BootManager/"+slot+"/busybox");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
				} else if (board.equalsIgnoreCase("tuna")){
					script.galaxynexusScript(context);
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
				} else {
					script.smallInstallScript(context);
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/morebinarys/busybox "+u.getExternalDirectory()+"/BootManager/"+slot+"/busybox");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
				}
			}
			while(!((new File(u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img")).exists())){
				count ++;
				if(count > 5){
					break;
				} else {
				u.log("EditBoot.img code");
				if(!threadStop){
					context = getApplicationContext();
					//unzipbooteditor();
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/mkbootimg");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/unpackbootimg"); 
					if(!new File("/data/local/tmp/boot.img").exists()){
						u.log("Boot.img missing attempting to reextract");
						u.unzip(rom, "/data/local/tmp/", "boot.img", context);
					}
					if(new File("/data/local/tmp/boot.img").exists()){
						u.log("Unpacking boot.img");
						CommandResult unpack = s.su.runWaitFor("/data/local/tmp/unpackbootimg -i /data/local/tmp/boot.img -o /data/local/tmp/");
						if(unpack.stdout!=null){
							u.log(unpack.stdout);
						}
						if(unpack.stderr!=null){
							u.log(unpack.stderr);
						}
						u.log("Editing boot.img");
						if(board.equals("sholes")){
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/boot.img-zImage");
							u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/boot.img-zImage /data/local/tmp/boot.img-zImage");
							u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/ext2.ko /data/local/tmp/ext2.ko");
							u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/mbcache.ko /data/local/tmp/mbcache.ko");
						}
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/edit.sh");
						if(new File("/data/local/tmp/system/xbin/busybox").exists()){
							bbpath="/system/xbin/busybox";
						}
						else if(new File("/data/local/tmp/system/bin/busybox").exists()){
							bbpath="/system/bin/busybox";
						}
						else {
							u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+context.getFilesDir().getAbsolutePath() + "/busybox /data/local/tmp/system/xbin");
							bbpath="/system/xbin/busybox";
						}
						SharedPreferences keycheck = getSharedPreferences(PREFS_DEVICE, 0);
						if(keycheck.contains("key"))key=0;
						//Some phones have internal sd like the inc so we want option to use that because it's faster then an sdcard
						if(useemmc){ 
							u.log("Using EMMC");
							CommandResult erd = s.su.runWaitFor("/data/local/tmp/edit.sh " + board + " " + slot + " " + emmc+" "+"emmc "+busybox+" "+bbpath+" "+sdcard+" "+ext+" "+key);
							if(erd.stderr!=null){
								u.log(erd.stderr);
							}
							if(erd.stdout!=null){
								u.log(erd.stdout);
							}
							if((erd.exit_value).toString()!=null){
								u.log("Exit Value of script is "+(erd.exit_value).toString());
							}
							if(!new File(u.getExternalDirectory()+"/BootManager/BootManager-"+slot+"EMMC-signed.zip").exists()) {
								u.log("Copying update.zip");
								u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager-"+slot+"EMMC-signed.zip "+ u.getExternalDirectory()+"/BootManager/BootManager-"+slot+"EMMC-signed.zip");
							}
						} else {
							if(board.contains("triumph")){
								board="qcom";
							}
							if(board.contains("olympus"))board="mapphone_cdma";
							if(board.contains("otter"))board="omap4430";
							if(board.contains("tuna")||board.contains("herring")){
								CommandResult erd = s.su.runWaitFor("/data/local/tmp/edit.sh " + board + " " + slot + " "+busybox+" "+key);
								if(erd.stderr!=null){
									u.log(erd.stderr);
								}
								if(erd.stdout!=null){
									u.log(erd.stdout);
								}
								if((erd.exit_value)!=null){
									u.log("Exit Value of script is "+(erd.exit_value).toString());
								}
							} else if(board.contains("ruby") || board.contains("vigor")|| board.contains("holiday") ){
								String storage ="/mnt/sdcard/ext_sd";
								if(Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").contains("internal")){
									CommandResult erd = s.su.runWaitFor("/data/local/tmp/edit.sh " + board + " " + slot + " " + sdcardblock+" "+"mnt/sdcard "+busybox+" "+bbpath+" "+sdcard+" "+ext+" "+key);
									if(erd.stderr!=null){
										u.log(erd.stderr);
									}
									if(erd.stdout!=null){
										u.log(erd.stdout);
									}
									if((erd.exit_value)!=null){
										u.log("Exit Value of script is "+(erd.exit_value).toString());
									}
								} else {
									CommandResult erd = s.su.runWaitFor("/data/local/tmp/edit.sh " + board + " " + slot + " " + sdcardblock+" "+storage+" "+busybox+" "+bbpath+" "+ext_sd+" "+ext+" "+key);
									if(erd.stderr!=null){
										u.log(erd.stderr);
									}
									if(erd.stdout!=null){
										u.log(erd.stdout);
									}
									if((erd.exit_value)!=null){
										u.log("Exit Value of script is "+(erd.exit_value).toString());
									}
								}
								u.log("Using Sdcard");
							} else {
								CommandResult erd = s.su.runWaitFor("/data/local/tmp/edit.sh " + board + " " + slot + " " + sdcardblock+" "+"mnt/sdcard "+busybox+" "+bbpath+" "+sdcard+" "+ext+" "+key);
								if(erd.stderr!=null){
									u.log(erd.stderr);
								}
								if(erd.stdout!=null){
									u.log(erd.stdout);
								}
								if((erd.exit_value)!=null){
									u.log("Exit Value of script is "+(erd.exit_value).toString());
								}
								u.log("Using Sdcard");
							}
							if(!new File(u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip").exists()) {
								u.log("Copying update.zip");
								File update = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-"+slot+"-signed.zip");
								if(update.exists()){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager-"+slot+"-signed.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip");
								} else {
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/updatezip");
									u.unzip(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip", "/data/local/tmp/updatezip/", "", context);
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/updatezip/moveboot.sh");
									try{
							    		FileWriter fstream = new FileWriter("/data/local/tmp/updatezip/moveboot.sh");
							    		BufferedWriter out = new BufferedWriter(fstream);
							    		out.write("#!/sbin/sh\n#\n\n#Do not modify this file!\n\nmount sdcard\ncp /sdcard/BootManager/"+slot+"/boot.img /tmp/boot.img");
							    		out.close();
							    	}catch (Exception e){
							    		u.log(e.toString());
							    	}
							    	try{
							    		FileWriter fstream = new FileWriter("/data/local/tmp/editupdatezip.sh");
							    		BufferedWriter out = new BufferedWriter(fstream);
							    		out.write("#!/data/data/com.drx2.bootmanager/files/busybox sh\n#\n\n#Do not modify this file!\n\ncd /data/local/tmp/updatezip\n"+context.getFilesDir().getAbsolutePath() + "/zip -r /data/local/tmp/update *");
							    		out.close();
							    	}catch (Exception e){
							    		u.log(e.toString());
							    	}
							    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/editupdatezip.sh");
							    	u.execCommand("/data/local/tmp/editupdatezip.sh");
							    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp /data/local/tmp/update.zip "+u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip");
							    	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/editupdate.zip.sh");
								}
							}
						}
					}
					u.log("Setting rom name");
					u.execCommand("echo "+romshort+" > /sdcard/BootManager/"+slot+"/name");
					} 
				}
			}
			if(!((new File(u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img")).exists())){
				threadStop=true;
				error="Error building new boot.img.";
				notify("Error", "Error building new boot.img. Please try again. If error continues please send log.txt off sdcard/BootManager to support@init2winitapps.com");
			}
			}
		}

		private boolean installInternal(){
			if(Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").contains("internal")){
				return true;
			} else {
				return false;
			}
		}
		
		
		private void cleanUp(Context context) {
			u.log("Cleaning up");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/system"); 
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/data"); 
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/cache");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm " + context.getFilesDir().getAbsolutePath() + "/booteditor.zip");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/META-INF");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/busybox");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/mfgsrv");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/createnewboot.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/mkbootimg");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-base");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/data");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/modules.zip");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/etcwifi.zip");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-cmdline");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/e2fsck");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/newboot.img");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-pagesize");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/editramdisk.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/system");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-ramdisk");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/ext2.ko");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/unpackbootimg");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-ramdisk.gz");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/kernelswapper.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/zImage");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-zImage");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/kernel");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/cache");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/extractsystem.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/extractdata.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/extractdatadata.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/cache");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/hijack-boo*");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/edit.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/editkernel.sh");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/updatezip");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/update.zip");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox losetup -d /dev/block/loop0");
		}
		
		
		
		
		
		private void installrom(String rom, String slot) {
			u.log("Restoring files to imgs");
			context = getApplicationContext(); 
			String filesystem = "/mke2fs -F -b 4096 -m 0 ";
			if (ext.equalsIgnoreCase("ext2")){
				filesystem="mke2fs -F -b 4096 -m 0 ";
			} else if(ext.equalsIgnoreCase("ext4")){
				filesystem="mke2fs -F -T ext4 -b 4096 -E stride=64,stripe-width=64 -O ^has_journal,extent,^huge_file -m 0 ";
			} else if(ext.equalsIgnoreCase("ext3")){
				//Droid X only
				filesystem="mke2fs -F -T ext3 -b 4096 -m 0 ";
			}
			if(useemmc){
				l.setupemmc(context, useemmc);
			}
			if(board.equalsIgnoreCase("aloha")){
				File mke2fs = new File("/system/bin/mke2fs");
				if(!mke2fs.exists()){
					u.log("mke2fs not found downloading now");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o rw,remount /system");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/mke2fs /system/bin/mke2fs");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 755 /system/bin/mke2fs");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/bin/mke2fs");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o ro,remount /system");
				}
			}
			if(!threadStop){
				File mConf = new File("/system/etc/mke2fs.conf");
				if(!mConf.exists()){
					u.log("mke2fs.conf does not exist...adding now...");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o rw,remount /system");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf /system/etc/mke2fs.conf");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /system/etc/mke2fs.conf");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/etc/mke2fs.conf");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o ro,remount /system");
				}
				if(board.equalsIgnoreCase("sholes") || board.equalsIgnoreCase("bravo") || board.equalsIgnoreCase("inc") || board.equalsIgnoreCase("mecha") || board.equalsIgnoreCase("supersonic") || board.equalsIgnoreCase("spade") || board.equalsIgnoreCase("vision") || board.equalsIgnoreCase("vivow") || board.equalsIgnoreCase("mahimahi") || board.equalsIgnoreCase("glacier") || board.equalsIgnoreCase("saga") || board.equalsIgnoreCase("aloha") || board.equalsIgnoreCase("buzz")){
					u.log("Board detected as "+board+". Checking libs...");
					File lib1 = new File("/system/lib/libext2fs.so");
					File lib2 = new File("/system/lib/libext2_blkid.so");
					File lib3 = new File("/system/lib/libext2_com_err.so");
					File lib4 = new File("/system/lib/libext2_e2p.so");
					File lib5 = new File("/system/lib/libext2_profile.so");
					File lib6 = new File("/system/lib/libext2_uuid.so");
					if(!lib1.exists() || !lib2.exists() || !lib3.exists() || !lib4.exists() || !lib5.exists() || !lib6.exists()){
						u.log("Libs do not exist downloading now....");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o rw,remount /system");
						u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/libext2fs.so /system/lib/libext2fs.so");
						u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/libext2_blkid.so /system/lib/libext2_blkid.so");
						u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/libext2_com_err.so /system/lib/libext2_com_err.so");
						u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/libext2_e2p.so /system/lib/libext2_e2p.so");
						u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/libext2_profile.so /system/lib/libext2_profile.so");
						u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/libext2_uuid.so /system/lib/libext2_uuid.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /system/lib/libext2fs.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /system/lib/libext2_blkid.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /system/lib/libext2_com_err.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /system/lib/libext2_e2p.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /system/lib/libext2_profile.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /system/lib/libext2_uuid.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/lib/libext2fs.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/lib/libext2_blkid.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/lib/libext2_com_err.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/lib/libext2_e2p.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/lib/libext2_profile.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /system/lib/libext2_uuid.so");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o ro,remount /system");
					} else {
						u.log("Libs do exist");
					}
				}}
			if(!threadStop){
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp");
				if(new File("/system/lib/modules/mbcache.ko").exists()){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox insmod /system/lib/modules/mbcache.ko");
				}
				if(new File("/system/lib/modules/ext2.ko").exists()){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox insmod /system/lib/modules/ext2.ko");
				}
			}
			if(!threadStop){
			if (!new File(u.getExternalDirectory() + "/BootManager/" + slot + "/system.img").exists()){
				u.log("Making system.img");
				notify("Restoring "+romshort, "Making system.img");
				CommandResult systemImg = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/zero of=" + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img bs=1M count=" + systemsize);
				if(systemImg.stdout!=null){
					u.log(systemImg.stdout);
				}
				if(systemImg.stderr!=null){
					u.log(systemImg.stderr);
				}
				int ssize = Integer.parseInt(systemsize);
				if(((new File(u.getExternalDirectory() + "/BootManager/" + slot + "/system.img").length())/ 1048576) < (ssize-10)){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory() + "/BootManager/" + slot + "/system.img");
					u.log("Error Making system.img");
					error="Error making system.img";
					notify("Error", "Error Making system.img");
					threadStop=true;
				}
			} else {
				u.log("system.img found"); 
			}}
			if(!threadStop){
			if (!new File(u.getExternalDirectory() + "/BootManager/"+slot+"/data.img").exists()){
				u.log("Making data.img");
				notify("Restoring "+romshort, "Making data.img");
				CommandResult dataImg = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/zero of=" + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img bs=1M count=" + datasize);
				if(dataImg.stdout!=null){
					u.log(dataImg.stdout);
				}
				if(dataImg.stderr!=null){
					u.log(dataImg.stderr);
				}
				int dsize = Integer.parseInt(datasize);
				if(((new File(u.getExternalDirectory() + "/BootManager/" + slot + "/data.img").length())/ 1048576) < (dsize-10)){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
					u.log("Error Making data.img");
					error="Error making data.img";
					notify("Error", "Error Making data.img");
					threadStop=true;
				}
			} else {
				u.log("data.img found");
			}}
			if(!threadStop){
			if (!new File(u.getExternalDirectory() + "/BootManager/"+slot+"/cache.img").exists()){
				u.log("Making cache.img");
				notify("Restoring "+romshort, "Making cache.img");
				CommandResult cacheImg = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/zero of=" + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img bs=1M count=" + cachesize);
				if(cacheImg.stdout!=null){
					u.log(cacheImg.stdout);
				}
				if(cacheImg.stderr!=null){
					u.log(cacheImg.stderr);
				}
				int csize = Integer.parseInt(cachesize);
				if(((new File(u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img").length())/ 1048576) < (csize-10)){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
					u.log("Error Making cache.img");
					error="Error making cache.img";
					notify("Error", "Error Making cache.img");
					threadStop=true;
				}
			} else {
				u.log("cache.img found");
			}}
			if(!threadStop){
			if(wipe){
				u.log("Wiping system.img");
				notify("Restoring "+romshort, "Wiping system.img");
				CommandResult systemfs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/"+filesystem  + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img");
				if(systemfs.stdout!=null){
					u.log(systemfs.stdout);
				}
				if(systemfs.stderr!=null){
					u.log(systemfs.stderr);
				}
				if(systemfs.exit_value!=null){
					exit=systemfs.exit_value;
				} 
				if(exit!=0){
					CommandResult systemfs2 = s.su.runWaitFor(filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img");
					if(systemfs2.stdout!=null){
						u.log(systemfs2.stdout);
					}
					if(systemfs2.stderr!=null){
						u.log(systemfs2.stderr);
					}
					if(systemfs2.exit_value!=null){
						exit=systemfs2.exit_value;
					} 
					if(exit!=0){
						CommandResult systemfs3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img");
						if((systemfs3.exit_value)!=0){
						if(systemfs3.stdout!=null){
							u.log(systemfs3.stdout);
						}
						if(systemfs3.stderr!=null){
							u.log(systemfs3.stderr);
						}
						error="Error making filesystem";
						notify("Error making filesystem", "Make sure your rom has "+ext+" support");
						threadStop=true;
						}
					} else {}
				} else {}
			} else {
				
			}}
			if(!threadStop){
			if(wipe){
				u.log("Wiping data.img");
				notify("Restoring "+romshort, "Wiping data.img");
				CommandResult datafs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/"+filesystem  + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
				if(datafs.stdout!=null){
					u.log(datafs.stdout);
				}
				if(datafs.stderr!=null){
					u.log(datafs.stderr);
				}
				if(datafs.exit_value!=null){
					exit=datafs.exit_value;
				} 
				if(exit!=0){
					CommandResult datafs2 = s.su.runWaitFor(filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
					if(datafs2.stdout!=null){
						u.log(datafs2.stdout);
					}
					if(datafs2.stderr!=null){
						u.log(datafs2.stderr);
					}
					if(datafs2.exit_value!=null){
						exit=datafs2.exit_value;
					} 
					if(exit!=0){
						CommandResult datafs3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem+ u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
						if((datafs3.exit_value)!=0){
						if(datafs3.stdout!=null){
							u.log(datafs3.stdout);
						}
						if(datafs3.stderr!=null){
							u.log(datafs3.stderr);
						}
						error="Error making filesystem";
						notify("Error making filesystem", "Make sure your rom has "+ext+" support");
						threadStop=true;
						}
					} else {}
				} else {}
			} else {
				
			}}
			if(!threadStop){
			if(wipe){
				u.log("Wiping cache.img");
				notify("Restoring "+romshort, "Wiping cache.img");
				u.log("Wiping data/dalvik-cache");
				CommandResult cachefs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/"+filesystem  + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
				if(cachefs.stdout!=null){
					u.log(cachefs.stdout);
				}
				if(cachefs.stderr!=null){
					u.log(cachefs.stderr);
				}
				if(cachefs.exit_value!=null){
					exit=cachefs.exit_value;
				} 
				if(exit!=0){
					CommandResult cachefs2 = s.su.runWaitFor(filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
					if(cachefs2.stdout!=null){
						u.log(cachefs2.stdout);
					}
					if(cachefs2.stderr!=null){
						u.log(cachefs2.stderr);
					}
					if(cachefs2.exit_value!=null){
						exit=cachefs2.exit_value;
					} 
					if(exit!=0){
						CommandResult cachefs3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
						if((cachefs3.exit_value)!=0){
						if(cachefs3.stdout!=null){
							u.log(cachefs3.stdout);
						}
						if(cachefs3.stderr!=null){
							u.log(cachefs3.stderr);
						}
						error="Error making filesystem";
						notify("Error making filesystem", "Make sure your rom has "+ext+" support");
						threadStop=true;
						}
					} else {}
				} else {}
				}
			}
			if(!threadStop){
			u.log("Making system folder");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/system");
			u.log("Making data folder");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data");
			u.log("Making cache folder");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/cache");
			notify("Restoring "+romshort, "Mounting img's");
			}
			if(!threadStop){
			u.log("Mounting system.img");
			CommandResult mntSys = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount " + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img /data/local/tmp/system"); 
			if(mntSys.stderr!=null){
				u.log(mntSys.stderr);
			}
			if(mntSys.stdout!=null){
				u.log(mntSys.stdout);
			}
			if(isMounted("data/local/tmp/system")) {
				u.log("Mounting data.img");
				CommandResult mntDa = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount " + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img /data/local/tmp/data"); 
				if(mntDa.stderr!=null){
					u.log(mntDa.stderr);
				}
				if(mntDa.stdout!=null){
					u.log(mntDa.stdout);
				}
				u.log("Mounting cache.img");
				CommandResult mntCha = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount " + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img /data/local/tmp/cache"); 
				if(mntCha.stderr!=null){
					u.log(mntCha.stderr);
				}
				if(mntDa.stdout!=null){
					u.log(mntCha.stdout);
				}
				u.log("Img's Mounted");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox ln -s "+u.getExternalDirectory()+" /data/local/tmp/sdcard");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/system");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/cache");
			} else {
				threadStop=true;
				u.log("System.img not Mounted");
				notify("Error", "Img's not mounted");
			}
			if(!threadStop){
				if(new File(nandroid+"/system.img").exists()){
					notify("Restoring "+romshort, "Restoring system.img");
					u.log("Restoring system");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/system >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/system.img >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsystem.sh");
					CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractsystem.sh");
					if(ressys.stdout!=null){
						u.log(ressys.stdout);
					}
					if(ressys.stderr!=null){
						u.log(ressys.stderr);
					}
				}
				if(new File(nandroid+"/system.yaffs2.img").exists()){
					notify("Restoring "+romshort, "Restoring system.img");
					u.log("Restoring system");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/system >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/system.yaffs2.img >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsystem.sh");
					CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractsystem.sh");
					if(ressys.stdout!=null){
						u.log(ressys.stdout);
					}
					if(ressys.stderr!=null){
						u.log(ressys.stderr);
					}
				}
				//Tar files in new recovery nandroids
				CommandResult system = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/system.*.tar");
				String systemimg = null;
				if(system.stdout!=null){
					u.log(system.stdout+" exists");
					systemimg = system.stdout;
					if(new File(systemimg).exists()){
						notify("Restoring "+romshort, "Restoring system.img");
						u.log("Creating script");
						CommandResult ressys1 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsystem.sh");
						if(ressys1.stdout!=null){
							u.log(ressys1.stdout);
						}
						if(ressys1.stderr!=null){
							u.log(ressys1.stderr);
						}
						CommandResult ressys2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp >> data/local/tmp/extractsystem.sh");
						if(ressys2.stdout!=null){
							u.log(ressys2.stdout);
						}
						if(ressys2.stderr!=null){
							u.log(ressys2.stderr);
						}
						CommandResult ressys3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+systemimg+" >> data/local/tmp/extractsystem.sh");
						if(ressys3.stdout!=null){
							u.log(ressys3.stdout);
						}
						if(ressys3.stderr!=null){
							u.log(ressys3.stderr);
						}
						CommandResult ressys4 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsystem.sh");
						if(ressys4.stdout!=null){
							u.log(ressys4.stdout);
						}
						if(ressys4.stderr!=null){
							u.log(ressys4.stderr);
						}
						CommandResult ressys5 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsystem.sh");
						if(ressys5.stdout!=null){
							u.log(ressys5.stdout);
						}
						if(ressys5.stderr!=null){
							u.log(ressys5.stderr);
						}
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractsystem.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				if(system.stderr!=null){
					u.log(system.stderr);
				}
				//TeamWin recovery on Thunderbolt
				if(new File(nandroid+"/system.win").exists()){
					notify("Restoring "+romshort, "Restoring system.img");
					u.log("Restoring system");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/system >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/system.win >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsystem.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsystem.sh");
					CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractsystem.sh");
					if(ressys.stdout!=null){
						u.log(ressys.stdout);
					}
					if(ressys.stderr!=null){
						u.log(ressys.stderr);
					}
				}
				//Tar files in new twrp nandroids
				CommandResult system2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/system.*.win");
				String systemimg2 = null;
				if(system2.stdout!=null){
					u.log(system2.stdout+" exists");
					systemimg2 = system2.stdout;
					if(new File(systemimg2).exists()){
						notify("Restoring "+romshort, "Restoring system.img");
						u.log("Creating script");
						CommandResult ressys1 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsystem.sh");
						if(ressys1.stdout!=null){
							u.log(ressys1.stdout);
						}
						if(ressys1.stderr!=null){
							u.log(ressys1.stderr);
						}
						CommandResult ressys2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp >> data/local/tmp/extractsystem.sh");
						if(ressys2.stdout!=null){
							u.log(ressys2.stdout);
						}
						if(ressys2.stderr!=null){
							u.log(ressys2.stderr);
						}
						CommandResult ressys3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+systemimg2+" >> data/local/tmp/extractsystem.sh");
						if(ressys3.stdout!=null){
							u.log(ressys3.stdout);
						}
						if(ressys3.stderr!=null){
							u.log(ressys3.stderr);
						}
						CommandResult ressys4 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsystem.sh");
						if(ressys4.stdout!=null){
							u.log(ressys4.stdout);
						}
						if(ressys4.stderr!=null){
							u.log(ressys4.stderr);
						}
						CommandResult ressys5 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsystem.sh");
						if(ressys5.stdout!=null){
							u.log(ressys5.stdout);
						}
						if(ressys5.stderr!=null){
							u.log(ressys5.stderr);
						}
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractsystem.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				if(system.stderr!=null){
					u.log(system.stderr);
				}
				if(new File(nandroid+"/data.img").exists()){
					notify("Restoring "+romshort, "Restoring data.img");
					u.log("Restoring data");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/data >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/data.img >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdata.sh");
					CommandResult resdata = s.su.runWaitFor("/data/local/tmp/extractdata.sh");
					if(resdata.stdout!=null){
						u.log(resdata.stdout);
					}
					if(resdata.stderr!=null){
						u.log(resdata.stderr);
					}
				}
				if(new File(nandroid+"/data.yaffs2.img").exists()){
					notify("Restoring "+romshort, "Restoring data.img");
					u.log("Restoring data");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/data >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/data.yaffs2.img >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdata.sh");
					CommandResult resdata = s.su.runWaitFor("/data/local/tmp/extractdata.sh");
					if(resdata.stdout!=null){
						u.log(resdata.stdout);
					}
					if(resdata.stderr!=null){
						u.log(resdata.stderr);
					}
				}
				CommandResult data = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/data.*.tar");
				String dataimg = null;
				if(data.stdout!=null){
					u.log(data.stdout+" exists");
					dataimg = data.stdout;
					if(new File(dataimg).exists()){
						notify("Restoring "+romshort, "Restoring data.img");
						u.log("Restoring data");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp >> data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+dataimg+" >> data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdata.sh");
						u.log("running script");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractdata.sh");
						u.log("script complete");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				if(data.stderr!=null){
					u.log(data.stderr);
				}
				//TeamWin recovery on Thunderbolt
				if(new File(nandroid+"/data.win").exists()){
					notify("Restoring "+romshort, "Restoring data.img");
					u.log("Restoring data");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/data >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/data.win >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdata.sh");
					CommandResult resdata = s.su.runWaitFor("/data/local/tmp/extractdata.sh");
					if(resdata.stdout!=null){
						u.log(resdata.stdout);
					}
					if(resdata.stderr!=null){
						u.log(resdata.stderr);
					}
				}
				//Team win new recovery nandroids -teamwinsucks:)
				CommandResult data2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/data.*.win");
				String dataimg2 = null;
				if(data2.stdout!=null){
					u.log(data2.stdout+" exists");
					dataimg2 = data2.stdout;
					if(new File(dataimg2).exists()){
						notify("Restoring "+romshort, "Restoring data.img");
						u.log("Restoring data");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp >> data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+dataimg2+" >> data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdata.sh");
						u.log("running script");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractdata.sh");
						u.log("script complete");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				if(data.stderr!=null){
					u.log(data.stderr);
				}
				if(new File(nandroid+"/datadata.img").exists()){
					notify("Restoring "+romshort, "Restoring datadata.img");
					if(!(new File("/data/local/tmp/data/data").exists())){
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data/data");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/data");
					}
					u.log("Restoring datadata");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/data/data >> data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/datadata.img >> data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdatadata.sh");
					CommandResult resdatadata = s.su.runWaitFor("/data/local/tmp/extractdatadata.sh");
					if(resdatadata.stdout!=null){
						u.log(resdatadata.stdout);
					}
					if(resdatadata.stderr!=null){
						u.log(resdatadata.stderr);
					}
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/data");
				}
				if(new File(nandroid+"/datadata.yaffs2.img").exists()){
					notify("Restoring "+romshort, "Restoring datadata.img");
					if(!(new File("/data/local/tmp/data/data").exists())){
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data/data");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/data");
					}
					u.log("Restoring datadata");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/data/data >> data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/datadata.yaffs2.img >> data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdatadata.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdatadata.sh");
					CommandResult resdatadata = s.su.runWaitFor("/data/local/tmp/extractdatadata.sh");
					if(resdatadata.stdout!=null){
						u.log(resdatadata.stdout);
					}
					if(resdatadata.stderr!=null){
						u.log(resdatadata.stderr);
					}
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/data");
				}
				CommandResult datadata = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/datadata.*.tar");
				String datadataimg = null;
				if(!(new File("/data/local/tmp/data/data").exists())){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data/data");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/data");
				}
				if(datadata.stdout!=null){
					u.log(datadata.stdout+" exists");
					datadataimg = datadata.stdout;
					if(new File(datadataimg).exists()){
						notify("Restoring "+romshort, "Restoring datadata.img");
						u.log("Restoring datadata");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/data >> data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+datadataimg+" >> data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdatadata.sh");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractdatadata.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/data");
				}
				if(datadata.stderr!=null){
					u.log(datadata.stderr);
				}
				CommandResult datadata2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/datadata.*.win");
				String datadataimg2 = null;
				if(!(new File("/data/local/tmp/data/data").exists())){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data/data");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/data");
				}
				if(datadata2.stdout!=null){
					u.log(datadata2.stdout+" exists");
					datadataimg2 = datadata2.stdout;
					if(new File(datadataimg2).exists()){
						notify("Restoring "+romshort, "Restoring datadata.img");
						u.log("Restoring datadata");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/data >> data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+datadataimg2+" >> data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractdatadata.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractdatadata.sh");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractdatadata.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/data");
				}
				if(datadata.stderr!=null){
					u.log(datadata.stderr);
				}
				if(new File(nandroid+"/cache.img").exists()){
					notify("Restoring "+romshort, "Restoring cache.img");
					u.log("Restoring cache");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/cache >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/cache.img >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractcache.sh");
					CommandResult rescache = s.su.runWaitFor("/data/local/tmp/extractcache.sh");
					if(rescache.stderr!=null){
						u.log(rescache.stderr);
					}
					if(rescache.stdout!=null){
						u.log(rescache.stdout);
					}
				}
				if(new File(nandroid+"/cache.yaffs2.img").exists()){
					notify("Restoring "+romshort, "Restoring cache.img");
					u.log("Restoring cache");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/cache >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/cache.yaffs2.img >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractcache.sh");
					CommandResult rescache = s.su.runWaitFor("/data/local/tmp/extractcache.sh");
					if(rescache.stderr!=null){
						u.log(rescache.stderr);
					}
					if(rescache.stdout!=null){
						u.log(rescache.stdout);
					}
				}
				CommandResult cache = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/cache.*.tar");
				String cacheimg = null;
				if(cache.stdout!=null){
					u.log(cache.stdout+" exists");
					cacheimg = cache.stdout;
					if(new File(cacheimg).exists()){
						notify("Restoring "+romshort, "Restoring cache.img");
						u.log("Restoring cache");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp >> data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+cacheimg+" >> data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractcache.sh");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractcache.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				if(cache.stderr!=null){
					u.log(cache.stderr);
				}
				//TeamWin recovery on Thunderbolt
				if(new File(nandroid+"/cache.win").exists()){
					notify("Restoring "+romshort, "Restoring cache.img");
					u.log("Restoring cache");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp/cache >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/cache.win >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractcache.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractcache.sh");
					CommandResult rescache = s.su.runWaitFor("/data/local/tmp/extractcache.sh");
					if(rescache.stderr!=null){
						u.log(rescache.stderr);
					}
					if(rescache.stdout!=null){
						u.log(rescache.stdout);
					}
				}
				CommandResult cache2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/cache.*.win");
				String cacheimg2 = null;
				if(cache2.stdout!=null){
					u.log(cache2.stdout+" exists");
					cacheimg2 = cache2.stdout;
					if(new File(cacheimg2).exists()){
						notify("Restoring "+romshort, "Restoring cache.img");
						u.log("Restoring cache");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /data/local/tmp >> data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+cacheimg2+" >> data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractcache.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractcache.sh");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractcache.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				if(cache.stderr!=null){
					u.log(cache.stderr);
				}
				//Restore android_secure
				if(new File(nandroid+"/.android_secure.img").exists()){
					notify("Restoring "+romshort, "Restoring .android_secure");
					u.log("Restoring android_secure");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /sdcard/BootManager/"+slot+"/.android_secure/*");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /sdcard/BootManager/"+slot+"/.android_secure");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /sdcard/BootManager/"+slot+"/.android_secure >> data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/.android_secure.img >> data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsd.sh");
					CommandResult rescache = s.su.runWaitFor("/data/local/tmp/extractsd.sh");
					if(rescache.stderr!=null){
						u.log(rescache.stderr);
					}
					if(rescache.stdout!=null){
						u.log(rescache.stdout);
					}
				}
				//TeamWin recovery on Thunderbolt
				if(new File(nandroid+"/and-sec.win").exists()){
					notify("Restoring "+romshort, "Restoring android_secure");
					u.log("Restoring android_secure");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /sdcard/BootManager/"+slot+"/.android_secure/*");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /sdcard/BootManager/"+slot+"/.android_secure");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /sdcard/BootManager/"+slot+"/.android_secure >> data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/unyaffs "+nandroid+"/and-sec.win >> data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsd.sh");
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsd.sh");
					CommandResult rescache = s.su.runWaitFor("/data/local/tmp/extractsd.sh");
					if(rescache.stderr!=null){
						u.log(rescache.stderr);
					}
					if(rescache.stdout!=null){
						u.log(rescache.stdout);
					}
				}
				CommandResult sd = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/.android_secure.*.tar");
				String sdimg = null;
				if(sd.stdout!=null){
					u.log(sd.stdout+" exists");
					sdimg = sd.stdout;
					if(new File(sdimg).exists()){
						notify("Restoring "+romshort, "Restoring .android_secure.img");
						u.log("Restoring .android_secure");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /sdcard/BootManager/"+slot+"/.android_secure/*");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /sdcard/BootManager/"+slot+" >> data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+sdimg+" >> data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsd.sh");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractsd.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				//More twrp crap
				CommandResult sd2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls "+nandroid+"/.and-sec.*.win");
				String sdimg2 = null;
				if(sd2.stdout!=null){
					u.log(sd2.stdout+" exists");
					sdimg2 = sd2.stdout;
					if(new File(sdimg2).exists()){
						notify("Restoring "+romshort, "Restoring .android_secure.img");
						u.log("Restoring .android_secure");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /sdcard/BootManager/"+slot+"/.android_secure/*");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '#!/data/data/com.drx2.bootmanager/files/busybox sh' > data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo cd /sdcard/BootManager/"+slot+" >> data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo "+context.getFilesDir().getAbsolutePath() + "/busybox tar xf "+sdimg2+" >> data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo exit >> data/local/tmp/extractsd.sh");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/extractsd.sh");
						CommandResult ressys = s.su.runWaitFor("/data/local/tmp/extractsd.sh");
						if(ressys.stdout!=null){
							u.log(ressys.stdout);
						}
						if(ressys.stderr!=null){
							u.log(ressys.stderr);
						}
					}
				}
				u.log("chmod'ing system and data");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/system");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/system/lost+found");
				}
				if(!threadStop){
				File app = new File("/data/local/tmp/data/app");
				if(!app.exists()){
					app.mkdir();
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/app");
				}
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManagerLite*.apk /data/local/tmp/data/app/BootManagerLite.apk");
				if(board.equalsIgnoreCase("sholes")){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/lib/modules/*");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/modules.zip /data/local/tmp/modules.zip");
					u.unzip("/data/local/tmp/modules.zip", "/data/local/tmp/system/lib/modules/", "", context);
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/etcwifi.zip /data/local/tmp/etcwifi.zip");
					u.unzip("/data/local/tmp/etcwifi.zip", "/data/local/tmp/system/etc/wifi/", "", context);
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox wget " + u.BASE_URL + "devices/sholes/fw_wlan1271.bin -O /data/local/tmp/system/etc/wifi/fw_wlan1271.bin");
				}
				//create /data/.noa2sd
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo > /data/.noa2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/40a2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/04apps2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/10apps2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/04a2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/*a2sd");	
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/*apps2sd");
				}
			}
		}
		
		private boolean isMounted(String path){
			StatFs stat = new StatFs(path);
			StatFs stat2 = new StatFs("/data/local/tmp");
	        int imgsize = (stat.getBlockSize()*stat.getBlockCount()) / 1048576;
	        int tmpsize = (stat2.getBlockSize()*stat2.getBlockCount()) / 1048576;
	        if(imgsize > tmpsize){
				return true;
			} else if(imgsize < tmpsize){
				return true;
			} else {
				return false;
			}
			
		}
		
		private void getdirtyRatio(){
			CommandResult ratio = s.su.runWaitFor("cat /proc/sys/vm/dirty_ratio");
			if(ratio.stdout!=null){
				currentratio = ratio.stdout;
			}
			CommandResult lowmem = s.su.runWaitFor("cat /sys/module/lowmemorykiller/parameters/minfree");
			if(lowmem.stdout!=null){
				currentlowmem = lowmem.stdout;
			}
		}
		
}