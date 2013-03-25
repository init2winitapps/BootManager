package com.drx2.bootmanager.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
import com.drx2.bootmanager.Installed;
import com.drx2.bootmanager.Loader;
import com.drx2.bootmanager.MainActivity;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.Script;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.drx2.bootmanager.utilities.Utilities;

public class BootManagerService extends Service {
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
	String rom;
	String romshort;
	String emmc;
	String busybox;
	String bbpath;
	String ext;
	String slot;
	String error;
	String currentratio;
	String currentlowmem;
	String chosenboot;
	Boolean useemmc;
	Boolean threadStop;
	Boolean wipedata = false;
	Boolean wipesystem = false;
	Boolean wipecache = false;
	//Boolean gappsinstalled;
	Boolean kernel;
	int key = 1;
	int exit = 0;
	Boolean largeboot;
	Boolean continueInstall=true;
	Notification notification;
	PendingIntent contentIntent;
	Intent notificationIntent;
	int queuePos = 0;
	Bundle extras;
	static ArrayList<File> dirs;
	static ArrayList<File> filePerms;

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context=getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		if(continueInstall){
			if(prefs.contains("screeninstallPref")){
				if(prefs.getBoolean("screeninstallPref", false)) {
					final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
					mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "");    
					mWakeLock.acquire();
				}
			}
			SharedPreferences LiveServ = getSharedPreferences(PREFS_DEVICE, 0);
			SharedPreferences.Editor editor = LiveServ.edit();
			editor.putString("Service", "Live");
			editor.commit();
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
			extras = intent.getExtras();
			ext=extras.getString("ext");
			queuePos = 0;
			slot=extras.getString("slot");
			File romfolder = new File(u.getExternalDirectory()+"/BootManager/"+slot);
			if(!romfolder.exists()) romfolder.mkdirs();
			File asec = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/.android_secure");
			if(!asec.exists())asec.mkdirs();
			//gappsinstalled=false;
			notify("Installing "+romshort, "Please wait...");
			queuer();
		} 
		return START_REDELIVER_INTENT;
	}

	private void queuer(){
		rom=Installed.queue.get(queuePos);
		if(Installed.bootimg.size() >= queuePos+1){
			chosenboot=Installed.bootimg.get(queuePos);
			if(chosenboot.equals("none")){
				//Reset to null so it doesn't search for a file called
				//none in the zip
				chosenboot=null;
			}
		} else {
			chosenboot=null;
		}
		romshort=(rom.substring(rom.lastIndexOf("/")+1));
		if(queuePos == 0){
			wipesystem=extras.getBoolean("wipesystem", false);
			wipedata=extras.getBoolean("wipedata", false);
			wipecache=extras.getBoolean("wipecache", false);
		}else{
			wipesystem = false;
			wipedata = false;
			wipecache = false;
		}
		if(Installed.kernel.get(queuePos)){
			kernelThread threadActivity = new kernelThread(rom, slot);
			threadActivity.start();
		} else {
			installThread threadActivity = new installThread(rom, board, slot, sdcard);
			//threadActivity.setPriority(Thread.MAX_PRIORITY);
			threadActivity.start();
		}
		queuePos++;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		threadStop=true;
	}
	private void notify(String Title, String message){
		notification.setLatestEventInfo(this, Title, message, contentIntent);
		startForeground(NOTI_ID, notification);
	}

	private void finalnotify(){
		if(!threadStop){
			u.log("final notify");
			myNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			CharSequence NotificationTicket = "Boot Manager Install Complete";
			CharSequence NotificationTitle = "Install Complete!";
			CharSequence NotificationContent = romshort + " Successfully Installed!";
			Notification notification = new Notification(R.drawable.icon, NotificationTicket, 0);
			Intent notificationIntent = null;
			//if(gappsinstalled){
			notificationIntent = new Intent(context, MainActivity.class);
			//u.log("gapps installed");
			/*} else {
					if(kernel){
						notificationIntent = new Intent(context, MainActivity.class);
						u.log("Kernel Installed");
					} else {
						notificationIntent = new Intent(context, Installgapps.class);
						u.log("gapps not installed");
					}
				}*/
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
			editor.putString("Service", "Kill");
			editor.commit();
			if(mWakeLock!=null){
				mWakeLock.release();
			}
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
			editor.putString("Service", "Kill");
			editor.commit();
			if(mWakeLock!=null){
				mWakeLock.release();
			}
		}
	}

	private String owner(File file){
		String done;
		CommandResult cmd = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls -ln "+file);
		String string = cmd.stdout;
		if(string!=null){
			String s1 = string.substring(16, 20);
			String s2 = string.substring(25, 29);
			String s3 = s1.replace(" ", "");
			String s4 = s2.replace(" ", "");
			done=s3+"."+s4;
		} else {
			done=null;
		}
		return done;
	}
	private String permission(File file){
		String done;
		CommandResult cmd = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls -l "+file);
		String string = cmd.stdout;
		if(string!=null){
			CharSequence substring = string.subSequence(1, 10);
			CharSequence sub2 = substring.subSequence(0, 3);
			CharSequence sub3 = substring.subSequence(3, 6);
			CharSequence sub4 = substring.subSequence(6, 9);
			String one = getnumber(sub2.toString());
			String two = getnumber(sub3.toString()); 
			String three = getnumber(sub4.toString());
			done = one+two+three;
		} else {
			done=null;
		}
		return done;
	}
	private String getnumber(String rwx){
		String rwx21 = rwx.substring(0, 1);
		String rwx31 = rwx.substring(1, 2);
		String rwx41	= rwx.substring(2, 3);
		String n1;
		String n2;
		String n3;
		if(rwx21.contains("-")){
			n1="0";
		} else {
			n1="1";
		}
		if(rwx31.contains("-")){
			n2="0";
		} else {
			n2="1";
		}
		if(rwx41.contains("-")){
			n3="0";
		} else {
			n3="1";
		}
		String number = n1+n2+n3;
		String result=null;
		if(number.contains("000")){
			result="0";
		}else if(number.contains("001")){
			result="1";
		}else if(number.contains("010")){
			result="2";
		}else if(number.contains("011")){
			result="3";
		}else if(number.contains("100")){
			result="4";	
		}else if(number.contains("101")){
			result="5";
		}else if(number.contains("110")){
			result="6";
		}else if(number.contains("111")){
			result="7";
		}	
		return result;

	}

	private class kernelThread extends Thread{
		String slot;
		String rom;
		private kernelThread(String ROM, String SLOT){
			this.slot = SLOT;
			this.rom = ROM;
		}
		@Override
		public void run() {
			try {
				u.log("Installing "+rom);
				installKernel(rom, slot);
				cleanUp(context);
			}finally{
				stopForeground(true);
				if(queuePos != Installed.queue.size()){
					queuer();
				}else{
					finalnotify();
				}
			}
		}
	}

	private class installThread extends Thread {
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
				Calendar c = Calendar.getInstance();
				u.log("Install started at "+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND));
				if(!board.contains("tuna")){
					getdirtyRatio();
					u.execCommand("echo 5 > /proc/sys/vm/dirty_ratio");
					u.execCommand("echo 1536,2048,4096,5120,5632,6144 > /sys/module/lowmemorykiller/parameters/minfree");
				}
				u.log("Installing "+vrom);
				installrom(vrom, vslot);
				BootManagerService.this.notify("Installing "+romshort, "Editing Boot.img");
				editboot(vrom, vboard, vslot, vsdcard);
				BootManagerService.this.notify("Installing "+romshort, "Cleaning up...");			
				cleanUp(context);
			}finally{
				if(!board.contains("tuna")){
					u.execCommand("echo "+currentratio+" > /proc/sys/vm/dirty_ratio");
					u.execCommand("echo "+ currentlowmem+" > /sys/module/lowmemorykiller/parameters/minfree");
				}
				Calendar c2 = Calendar.getInstance();
				u.log("Install ended at "+c2.get(Calendar.HOUR_OF_DAY)+":"+c2.get(Calendar.MINUTE)+":"+c2.get(Calendar.SECOND));
				stopForeground(true);
				if(queuePos != Installed.queue.size()){
					queuer();
				}else{
					finalnotify();
				}
			}
		}
	}


	private void editboot(String rom, String board, String slot, String sdcardblock ) {
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox chmod 777 /data/local/tmp");
		if(board.contains("shadow")||board.contains("droid2")||board.contains("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
			editboot2ndInit(rom, board, slot, sdcardblock);
		} else {
			editbootNormal(rom, board, slot, sdcardblock);
		}
	}

	private void editbootNormal(String rom2, String board2, String slot2, String sdcardblock) {
		if(chosenboot!=null){
			u.unzip(rom, "/data/local/tmp/", chosenboot, context);
			if(board.contains("vigor")){
				u.log(chosenboot);
				if(chosenboot.contains("PH98IMG")){
					u.unzip("/data/local/tmp/"+chosenboot, "/data/local/tmp/", "boot.img", context);
					u.log("unzipping /data/local/tmp/"+chosenboot);
				} else {
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp /data/local/tmp/"+chosenboot+" /data/local/tmp/boot.img");
				}
			} else {
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp /data/local/tmp/"+chosenboot+" /data/local/tmp/boot.img");
			}
			u.log("getting Boot.img from zip");
		}
		if((new File("/data/local/tmp/boot.img")).exists()){
			u.log("Boot.img found");
			//If there's a boot.img to edit we delete the old one on sdcard
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img");
			u.log("deleting old boot.img");
		}
		int count = 1;
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/busybox");
		if(largeboot){
			if(board.equals("glacier") || board.equals("otter") || board.equalsIgnoreCase("shooteru") || board.equalsIgnoreCase("shooter") || board.equalsIgnoreCase("pyramid") || board.equalsIgnoreCase("vivow") || board.equalsIgnoreCase("vivo") || board.equalsIgnoreCase("mecha") || board.equalsIgnoreCase("spade") || board.equalsIgnoreCase("runnymede")){
				script.installScript3d(context);
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/morebinarys/busybox /data/local/tmp/busybox");
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
			} else if(board.equals("ruby") || board.equals("vigor") || board.equals("holiday") ) {
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
			u.log("using smaller boot");
			if(board.equals("otter") || board.equalsIgnoreCase("shooteru") || board.equalsIgnoreCase("doubleshot") || board.equalsIgnoreCase("shooter") || board.equalsIgnoreCase("pyramid") || board.equalsIgnoreCase("vivow") || board.equalsIgnoreCase("vivo") || board.equalsIgnoreCase("mecha") || board.equalsIgnoreCase("spade")){
				script.smallInstallScript3d(context);
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/morebinarys/busybox "+u.getExternalDirectory()+"/BootManager/"+slot+"/busybox");
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
			} else if(board.equalsIgnoreCase("tuna")){
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
								u.log("Copying update.zip");//Only 4 rom's will fit on emmc so no reason to make extras
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
							} else if(board.contains("ruby") || board.contains("vigor") || board.contains("holiday") ){
								String storage = "/mnt/sdcard/ext_sd";
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
							if(board.equals("vigor")){
								addBootimgtoUpdateRezound(slot);
							} else {
								getUpdateZip(slot);
							}
						}
					}
					if(wipesystem && wipedata && wipecache){
						u.log("Setting rom name");
						u.execCommand("echo "+romshort+" > "+u.getExternalDirectory()+"/BootManager/"+slot+"/name");
					}
				} 
			}
		}
		if(!((new File(u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img")).exists())){
			threadStop=true;
			error="Error Building new boot.img";
			notify("Error", "Error building new boot.img. Please try again. If error continues please send log.txt off sdcard/BootManager to support@init2winitapps.com");
		}
	}

	private void editboot2ndInit(String rom2, String board2, String slot2, String sdcardblock) {
		notify("Preparing boot files", "Please wait......");
		String boardname ="mapphone_cdma";
		String sd;
		if(board.equals("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
			if(installInternal()){
				sd="/mnt/sdcard";
			} else {
				SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
				sdcardblock=settings.getString("ext_sd", "");
				sd="/mnt/sdcard-ext";
			}
		} else {
			sd = "/mnt/sdcard";
		}
		File hijack = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/hijack-boot.zip");
		if(wipesystem || !(hijack.exists())){
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
				if(wipesystem && wipedata && wipecache){
					u.log("Setting rom name");
					u.execCommand("echo "+romshort+" > "+u.getExternalDirectory()+"/BootManager/"+slot+"/name");
				}
				if(!new File(u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip").exists()) {
					u.log("Moving update.zip");
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
		}
		if(wipesystem && wipedata && wipecache){
			u.log("Setting rom name");
			u.execCommand("echo "+romshort+" > "+u.getExternalDirectory()+"/BootManager/"+slot+"/name");
		}
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
						//TODO internal/external should work make sure to pull right zip above
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

	}


	private boolean installInternal(){
		if(Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").contains("internal")){
			return true;
		} else {
			return false;
		}
	}
	private void cleanUp(Context context) {
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
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/hijack-boo*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/edit.sh");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/editkernel.sh");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/updatezip");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/update.zip");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/devices");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/kernel");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox losetup -d /dev/block/loop0");
	}


	private void installrom(String rom, String slot) {
		u.log("Installing files to imgs");
		context = getApplicationContext(); 
		String filesystem = "/mke2fs -F -b 4096 -m 0 ";
		if(board.equals("streak7")){//don't know if this will help
			filesystem="mke2fs -F -T ext4 -b 4096 -E stride=64,stripe-width=64 -O ^has_journal,extent,^huge_file ";
		} else {
			if (ext.equalsIgnoreCase("ext2")){
				filesystem="mke2fs -F -b 4096 -m 0 ";
			} else if(ext.equalsIgnoreCase("ext4")){
				filesystem="mke2fs -F -T ext4 -b 4096 -E stride=64,stripe-width=64 -O ^has_journal,extent,^huge_file -m 0 ";
			} else if(ext.equalsIgnoreCase("ext3")){
				//Droid X Droid 2 only
				filesystem="mke2fs -F -T ext3 -b 4096 -m 0 ";
			}
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
			if(board.equalsIgnoreCase("sholes") || board.equalsIgnoreCase("bravo") || board.equalsIgnoreCase("inc") || board.equalsIgnoreCase("mecha") || board.equalsIgnoreCase("supersonic") || board.equalsIgnoreCase("spade") || board.equalsIgnoreCase("vision") || board.equalsIgnoreCase("vivow") || board.equalsIgnoreCase("mahimahi") || board.equalsIgnoreCase("glacier") || board.equalsIgnoreCase("saga") || board.equalsIgnoreCase("aloha") || board.equalsIgnoreCase("buzz") || board.equalsIgnoreCase("shadow") || board.equalsIgnoreCase("spyder") || board.equalsIgnoreCase("droid2") || board.equalsIgnoreCase("droid2we")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
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
			}
		}
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
				notify("Installing "+romshort, "Making system.img");
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
					notify("Error", "Error Making system.img...Trying again.");
				}
				CommandResult systemfs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/"+filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img");
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
					CommandResult systemfs2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img");
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
						CommandResult systemfs3 = s.su.runWaitFor(filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img");
						if((systemfs3.exit_value)!=0){
							if(systemfs3.stdout!=null){
								u.log(systemfs3.stdout);
							}
							if(systemfs3.stderr!=null){
								u.log(systemfs3.stderr);
							}
							threadStop=true;
							error="Error Making Filesystem";
							notify("Error making filesystem", "Make sure your rom has "+ext+" support");
						}
					} else {}
				} else {}
			} else {
				u.log("system.img found"); 
			}}
		if(!threadStop){
			if (!new File(u.getExternalDirectory() + "/BootManager/"+slot+"/data.img").exists()){
				u.log("Making data.img");
				notify("Installing "+romshort, "Making data.img");
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
					threadStop=true;
					notify("Error", "Error Making data.img");
				}
				CommandResult datafs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/"+filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
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
					CommandResult datafs2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem+ u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
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
						CommandResult datafs3 = s.su.runWaitFor(filesystem+ u.getExternalDirectory() + "/BootManager/" + slot + "/data.img");
						if((datafs3.exit_value)!=0){
							if(datafs3.stdout!=null){
								u.log(datafs3.stdout);
							}
							if(datafs3.stderr!=null){
								u.log(datafs3.stderr);
							}
							threadStop=true;
							error="Error Making Filesystem";
							notify("Error making filesystem", "Make sure your rom has "+ext+" support");
						}
					} else {}
				} else {}
			} else {
				u.log("data.img found");
			}}
		if(!threadStop){
			if (!new File(u.getExternalDirectory() + "/BootManager/"+slot+"/cache.img").exists()){
				u.log("Making cache.img");
				notify("Installing "+romshort, "Making cache.img");
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
					notify("Error", "Error making cache.img");
				}
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
					CommandResult cachefs2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() +"/morebinarys/"+filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
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
						CommandResult cachefs3 = s.su.runWaitFor(filesystem + u.getExternalDirectory() + "/BootManager/" + slot + "/cache.img");
						if((cachefs3.exit_value)!=0){
							if(cachefs3.stdout!=null){
								u.log(cachefs3.stdout);
							}
							if(cachefs3.stderr!=null){
								u.log(cachefs3.stderr);
							}
							threadStop=true;
							error="Error Making Filesystem";
							notify("Error making filesystem", "Make sure your rom has "+ext+" support");
						}
					} else {}
				} else {}
			} else {
				u.log("cache.img found");
			}}
		if(!threadStop){
			if(wipesystem){
				u.log("Wiping system.img");
				notify("Installing "+romshort, "Wiping system.img");
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
							error="Error Making Filesystem";
							notify("Error making filesystem", "Make sure your rom has "+ext+" support");
							threadStop=true;
						}
					} else {}
				} else {}
			} else {

			}}
		if(!threadStop){
			if(wipedata){
				u.log("Wiping data.img");
				notify("Installing "+romshort, "Wiping data.img");
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
							error="Error Making Filesystem";
							notify("Error making filesystem", "Make sure your rom has "+ext+" support");
							threadStop=true;
						}
					} else {}
				} else {}
			} else {

			}}
		if(!threadStop){
			if(wipecache){
				u.log("Wiping cache.img");
				notify("Installing "+romshort, "Wiping cache.img");
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
							error="Error Making Filesystem";
							notify("Error making filesystem", "Make sure your rom has "+ext+" support");
							threadStop=true;
						}
					} else {}
				} else {}
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount " + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img /data/local/tmp/data"); 
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/data/dalvik-cache/*");
			}}
		if(!threadStop){
			u.log("Making system folder");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/system");
			u.log("Making data folder");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data");
			notify("Installing "+romshort, "Mounting img's");
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
			if(isMounted("/data/local/tmp/system")) {
				u.log("Mounting data.img");
				CommandResult mntDa = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount " + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img /data/local/tmp/data"); 
				if(mntDa.stderr!=null){
					u.log(mntDa.stderr);
				}
				if(mntDa.stdout!=null){
					u.log(mntDa.stdout);
				}
				u.log("Img's Mounted");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox ln -s "+u.getExternalDirectory()+" /data/local/tmp/sdcard");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/system");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data");
			} else {
				threadStop=true;
				error="Error mounting img's";
				u.log("System.img not Mounted");
				notify("Error", "Error Mounting img's");
			}
			if(!threadStop){
				u.unzip(rom, "/data/local/tmp/", "META-INF", context);
				u.log("Parsing updater-script");
				if(new File("/data/local/tmp/META-INF/com/google/android/updater-script").exists()){
					parseUS(false);
				}
				u.log("chmod'ing system and data");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/system");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/system/lost+found");
				u.log("extracting Rom");
			}
			if(!threadStop){
				unzip(rom, "/data/local/tmp/", "", context);
				u.log("rom extracted");
				notify("Installing "+romshort, "Extracting rom please wait...");
			}
			if(!threadStop){
				File app = new File("/data/local/tmp/data/app");
				if(!app.exists()){
					app.mkdir();
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/app");
				}
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/BootManager*.apk /data/local/tmp/data/app/BootManager.apk");
				if(board.equalsIgnoreCase("sholes")){
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/lib/modules/*");
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/modules.zip /data/local/tmp/modules.zip");
					u.unzip("/data/local/tmp/modules.zip", "/data/local/tmp/system/lib/modules/", "", context);
					u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+u.getExternalDirectory()+"/BootManager/.zips/etcwifi.zip /data/local/tmp/etcwifi.zip");
					u.unzip("/data/local/tmp/etcwifi.zip", "/data/local/tmp/system/etc/wifi/", "", context);
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox wget " + u.BASE_URL + "devices/sholes/fw_wlan1271.bin -O /data/local/tmp/system/etc/wifi/fw_wlan1271.bin");
				}
				//Create /data/.noa2sd
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo > /data/local/tmp/data/.noa2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/40a2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/04apps2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/10apps2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/04a2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/*a2sd");	
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/etc/init.d/*apps2sd");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/bin/*a2sd*");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/xbin/*a2sd*");
				setPermissionsAfterInstall();
				fixPermsonimgs();
				parseUS(true);
				reparseUS();
				/*gappsinstalled=(new File("/data/local/tmp/system/app/Vending.apk").exists());
				if(!gappsinstalled){
					findMarketAPK(new File("/data/local/tmp/data"));

				}
				if(!gappsinstalled){
					findMarketAPK(new File("/data/local/tmp/system/app"));
				}*/
			}
		}
	}

	private void installKernel(String rom, String slot) {
		context = getApplicationContext();
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox chmod 777 /data/local/tmp");
		script.kernelScript(context);
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/mkbootimg /data/local/tmp/mkbootimg");
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp "+context.getFilesDir().getAbsolutePath()+"/unpackbootimg /data/local/tmp/unpackbootimg");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/mkbootimg");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/unpackbootimg"); 
		u.log("Making system folder");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/system");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/data");
		u.log("Mounting system.img");
		CommandResult mntSys = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount " + u.getExternalDirectory() + "/BootManager/" + slot + "/system.img /data/local/tmp/system"); 
		if(mntSys.stderr!=null){
			u.log(mntSys.stderr);
		}
		if(mntSys.stdout!=null){
			u.log(mntSys.stdout);
		}
		CommandResult mntDa = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount " + u.getExternalDirectory() + "/BootManager/" + slot + "/data.img /data/local/tmp/data"); 
		if(mntDa.stderr!=null){
			u.log(mntDa.stderr);
		}
		if(mntDa.stdout!=null){
			u.log(mntDa.stdout);
		}
		if(isMounted("data/local/tmp/system")) {
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img /data/local/tmp/boot.img");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/system");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp/system/lib/modules/*");
		} else {
			threadStop=true;
			error="Error Making Filesystem";
			u.log("System.img not Mounted");
			notify("Error", "System.img not mounted");
			cleanUp(context);
		}
		if(!threadStop){
			u.log("extracting Kernel");
			notify("Installing "+romshort, "Extracting Kernel");
			u.unzip(rom, "/data/local/tmp/", "", context);
			parseUS(true);
			if(new File("/data/local/tmp/boot.img").exists()){
				u.log("Unpacking boot.img");
				CommandResult unboot = s.su.runWaitFor("/data/local/tmp/unpackbootimg -i /data/local/tmp/boot.img -o /data/local/tmp/");
				if(unboot.stderr!=null){
					u.log(unboot.stderr);
				}
				if(unboot.stdout!=null){
					u.log(unboot.stdout);
				}
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp /data/local/tmp/kernel/zImage /data/local/tmp/zImage");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/editkernel.sh");
				CommandResult ks = s.su.runWaitFor("/data/local/tmp/editkernel.sh "+slot+" "+busybox+" "+ext);
				if(ks.stderr!=null){
					u.log(ks.stderr);
				}
				if(ks.stdout!=null){
					u.log(ks.stdout);
				}
			} else {
				threadStop=true;
				error="Boot.img not found";
				u.log("Boot.img not found");
				notify("Error", "Boot.img not found");
			}
		}
	}

	private void parseUS(boolean romunzipped) {
		File updaterscript = new File("/data/local/tmp/META-INF/com/google/android/updater-script");
		if(updaterscript.exists()){
			u.log("found updater-script");
			StringBuilder commands = new StringBuilder(30000); 
			String bashcommand = null;
			String file2 = null;
			String line1;
			String line2;
			String line3;
			String line4;
			String line5;
			String newline;
			String newline2;
			try {
				u.log("Reading updater-script");
				FileInputStream fis = new FileInputStream(updaterscript.toString());
				u.log("convert updater-script to string");
				DataInputStream dis = new DataInputStream(fis);
				u.log("data input stream");
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				u.log("buffered reader");
				String data;
				while((data = br.readLine()) != null) {
					commands.append(data + "\n");	
				}
				fis.close();
				dis.close();
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
				u.log(e.toString());
				//return false;
			}
			if(commands.toString() != null) {
				String[] tmp = commands.toString().split("\n");
				for(int x = 0; x < tmp.length; x++) {

					if(romunzipped){

						if(tmp[x].contains("set_perm(")) {

							newline = tmp[x];

							if(newline.contains("set_perm(")) {
								line1=newline.replace("set_perm(", " ");
								line2=line1.replace(",", " ");
								line3=line2.replace(")", " ");
								line4=line3.replace("\"", " ");
								line5=line4.replace(";", "");

								StringTokenizer st = new StringTokenizer(line5, " ");

								String user = st.nextToken();
								String group = st.nextToken();
								@SuppressWarnings("unused")
								String rwx = st.nextToken();
								String file = "/data/local/tmp"+(st.nextToken());
								u.log(context.getFilesDir().getAbsolutePath() + "/busybox chown " + user + "." + group + " " + file);
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown " + user + "." + group + " " + file);
							}
						}

						if(tmp[x].contains("set_perm_recursive(")) { 

							newline2 = tmp[x];   
							if(newline2.contains("set_perm_recursive(")) {
								line1=newline2.replace("set_perm_recursive(", " ");
								line2=line1.replace(",", " ");
								line3=line2.replace(")", " ");
								line4=line3.replace("\"", " ");
								line5=line4.replace(";", "");

								StringTokenizer st2 = new StringTokenizer(line5, " ");

								String user = st2.nextToken();
								String group = st2.nextToken();
								String rwxfolder = st2.nextToken();
								String rwx = st2.nextToken();
								String file = "/data/local/tmp"+(st2.nextToken());
								bashcommand="chmod -R ";
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown -R " + user + "." + group + " " + file);
								u.log("chown -R " + user + "." + group + " " + file);
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod -R " + rwxfolder + " " + file);
								u.log("chmod -R " + rwxfolder + " " + file);
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox find " + file+"/. "+"-type f -exec "+context.getFilesDir().getAbsolutePath() +"/busybox chmod "+rwx+" {} \\;");
							}
						}

						if(tmp[x].contains("symlink(")) {

							newline2 = tmp[x];   
							if(newline2.contains("symlink(")) {
								line1=newline2.replace("symlink(", " ");
								line2=line1.replace(",", " ");
								line3=line2.replace(")", " ");
								line4=line3.replace("\"", " ");
								line5=line4.replace(";", "");
								bashcommand="ln -s ";
								StringTokenizer st2 = new StringTokenizer(line5, " ");
								String file =st2.nextToken();

								if(new File("/data/local/tmp/system/bin/" + file).exists()){
									file2=("/system/bin/" + file);
								} else if(new File("/data/local/tmp/system/xbin/" + file).exists()){
									file2=("/system/xbin/" + file);
								} else {
									file2=file;
								}
								while(st2.hasMoreTokens()) {
									String symlink = st2.nextToken();

									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox ln -sf " + file2 + " /data/local/tmp" + symlink);
									u.log("ln -s " + file2 + " /data/local/tmp" + symlink);
								}
							}
						}

						if(tmp[x].startsWith(" ")|| tmp[x].startsWith("\t")) {

							newline2 = tmp[x];   
							if(newline2.contains("\"")) {
								line1=newline2.replace("symlink(", " ");
								line2=line1.replace(",", " ");
								line3=line2.replace(")", " ");
								line4=line3.replace("\"", " ");
								line5=line4.replace(";", "");
								StringTokenizer st2 = new StringTokenizer(line5, " ");
								while(st2.hasMoreTokens()) {
									String symlink = st2.nextToken();
									if(bashcommand != null){
										if(bashcommand.equalsIgnoreCase("ln -s ")){
											if(symlink.contains("&")){
												//Skip it or install will hang
											} else {
												u.log(bashcommand + file2 + " /data/local/tmp" + symlink);
												u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox "+ bashcommand + file2 + " /data/local/tmp" + symlink);
											}
										} else {
											//I think this was the issue with rom's hanging
											//u.log(bashcommand+" "+symlink);
											//u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox " + bashcommand + " /data/local/tmp" + symlink); 
										}
									}
								}
							}
						}
					}

					if(!romunzipped){
						if(tmp[x].contains("delete(")) {

							newline2 = tmp[x];   
							if(newline2.contains("delete(")) {
								line1=newline2.replace("delete(", " ");
								line2=line1.replace(",", " ");
								line3=line2.replace(")", " ");
								line4=line3.replace("\"", " ");
								line5=line4.replace(";", "");

								StringTokenizer st2 = new StringTokenizer(line5, " ");

								while(st2.hasMoreTokens()) {
									String file = st2.nextToken();
									bashcommand="rm ";
									u.log("rm /data/local/tmp"+file);
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /data/local/tmp" + file);

								}
							}
						}

						if(tmp[x].contains("delete_recursive(")) {

							newline2 = tmp[x];   
							if(newline2.contains("delete_recursive(")) {
								line1=newline2.replace("delete_recursive(", " ");
								line2=line1.replace(",", " ");
								line3=line2.replace(")", " ");
								line4=line3.replace("\"", " ");
								line5=line4.replace(";", "");

								StringTokenizer st2 = new StringTokenizer(line5, " ");
								bashcommand="rm -r";
								while(st2.hasMoreTokens()) {
									String file = st2.nextToken();
									if(file.startsWith("/")){
										u.log("rm -r /data/local/tmp"+file);
										u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp" + file);
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void reparseUS(){
		String line1;
		String line2;
		String line3;
		String line4;
		String line5;
		String newline;
		File updaterscript = new File("/data/local/tmp/META-INF/com/google/android/updater-script");
		if(updaterscript.exists()){
			StringBuilder commands = new StringBuilder(30000); 

			try {
				u.log("Re-parsing updater-script");
				FileInputStream fis = new FileInputStream(updaterscript.toString());
				DataInputStream dis = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				String data;
				while((data = br.readLine()) != null) {
					commands.append(data + "\n");				
				}

				if(commands.toString() != null) {
					String[] tmp = commands.toString().split("\n");
					for(int x = 0; x < tmp.length; x++) {

						if(tmp[x].contains("set_perm(")) {

							newline = tmp[x];

							if(newline.contains("set_perm(")) {
								line1=newline.replace("set_perm(", " ");
								line2=line1.replace(",", " ");
								line3=line2.replace(")", " ");
								line4=line3.replace("\"", " ");
								line5=line4.replace(";", "");

								StringTokenizer st = new StringTokenizer(line5, " ");

								@SuppressWarnings("unused")
								String user = st.nextToken();
								@SuppressWarnings("unused")
								String group = st.nextToken();
								String rwx = st.nextToken();
								String file = "/data/local/tmp"+(st.nextToken());
								u.log(context.getFilesDir().getAbsolutePath() + "/busybox chmod " + rwx + " "+file);
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod " + rwx + " "+file);
							}
						}
					}	
					fis.close();
					br.close();
					dis.close();
				} 
			} catch (Exception e) {
				e.printStackTrace();
				u.log(e.toString());
				//return false;
			}	
		} 

		//return true;
	}

	private boolean isMounted(String path){
		try {
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
		} catch(Exception e){
			u.log("isMounted java error:"+e.toString());
			return false;
		}

	}

	private void fixPermsonimgs() {
		//This is a hack to fix permissions for rom's that push app's to data/app...now less dirty since I'm being more specific...
		//Hopefully this will fix all permissions if installing with no wipe.
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.9998 /data/local/tmp/data/misc");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1014.1014 /data/local/tmp/data/misc/dhcp");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1002.1002 /data/local/tmp/data/misc/bluetoothd");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1017.1017 /data/local/tmp/data/misc/keystore");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/app");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/misc/bluetooth");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/app-private");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/app/*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/app-private/*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/misc/vpn");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/data");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/misc/systemkeys");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 1000.1000 /data/local/tmp/data/misc/vpn/profiles");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 2000.2000 /data/local/tmp/data/local");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 2000.2000 /data/local/tmp/data/local/tmp");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 2000.2000 /data/local/tmp/data/local/rights");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /data/local/tmp/data/property");

		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 770 /data/local/tmp/data/misc/dhcp");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 01771 /data/local/tmp/data/misc");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 770 /data/local/tmp/data/misc/bluetoothd");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 770 /data/local/tmp/data/misc/bluetooth");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 770 /data/local/tmp/data/misc/vpn");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 770 /data/local/tmp/data/misc/vpn/profiles");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 770 /data/local/tmp/data/property");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 700 /data/local/tmp/data/misc/keystore");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 700 /data/local/tmp/data/misc/systemkeys");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/data/*/shared_prefs");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/data/*/databases");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/data/*/cache");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/local");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/local/tmp");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/data");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/app");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/data/*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 771 /data/local/tmp/data/app-private");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/local/rights");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/data/data/*/*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /data/local/tmp/data/app/*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 644 /data/local/tmp/data/app-private/*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 775 /data/local/tmp/data/data/*/lib");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 775 /data/local/tmp/data/data/cache");

	}

	private boolean permissionsset(File dir){
		for(int i = 0; i < dirs.size(); i++){
			if(dirs.contains(dir)){
				return true;
			}
		}
		return false;
	}

	private void setPermissionsAfterInstall(){
		u.log("setting perms after install");
		if(wipesystem){
			u.log("wipe system true for permissions");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod -R 777 /data/local/tmp/system");
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown -R 0.0 /data/local/tmp/system");
			if(wipedata){
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod -R 777 /data/local/tmp/data");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown -R 0.0 /data/local/tmp/data");
			} else {
				for(int i = 0; i < filePerms.size(); i++){
					File file = filePerms.get(i);
					if(file.toString().startsWith("/data/local/tmp/data/")){
						u.log("Setting permissions on "+file);
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+file);
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+file);
					}
				}
			}
		} else {
			System.out.println("was not wiped");
			for(int i = 0; i < filePerms.size(); i++){
				File file = filePerms.get(i);
				if(file.toString().startsWith("/data/local/tmp/data/") || file.toString().startsWith("/data/local/tmp/system/")){
					u.log("Setting permissions on "+file);
					u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+file);
				}
			}
		}
	}

	private void unzip(String src, String dest, String filestoextract, Context context){
		final int BUFFER_SIZE = 4096;
		dirs = new ArrayList<File>();
		filePerms = new ArrayList<File>();
		//File dir = new File("/data/local/tmp/system");
		//File dirpar = new File("/data/local/tmp");
		ArrayList<ZipEntry> ziplist = new ArrayList<ZipEntry>();
		BufferedOutputStream bufferedOutputStream = null;
		FileInputStream fileInputStream;
		try {
			fileInputStream = new FileInputStream(src); 
			ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));
			ZipEntry zipEntry;

			while ((zipEntry = zipInputStream.getNextEntry()) != null){
				String zipEntryName = zipEntry.getName();
				File file = new File(dest + zipEntryName);

				if(zipEntry.getName().contains("devices/"+board+"/")){
					ziplist.add(zipEntry);
				}

				if((zipEntry.getName()).contains(filestoextract)) {
					if(((zipEntry.getName()).contains("boot")) || ((zipEntry.getName()).contains("data")) || ((zipEntry.getName()).contains("system")) || ((zipEntry.getName()).contains("sdcard")) || ((zipEntry.getName()).contains("META-INF"))){

						if(!file.getParentFile().exists()) {
							file.getParentFile().mkdirs();
							File directory = file.getParentFile(); 
							if(!directory.equals("/data/local/tmp") || !directory.equals("/data/local")){
								if(!permissionsset(directory)){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory);
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory);
									dirs.add(directory);
									//System.out.println("setting permissions on "+directory+" folder");
								}
							}
							if(!directory.getParentFile().equals("/data/local/tmp") || !directory.getParentFile().equals("/data/local")){
								if(!permissionsset(directory.getParentFile())){	
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory.getParentFile());
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory.getParentFile());
									dirs.add(directory.getParentFile());
									//System.out.println("setting permissions on "+directory.getParentFile()+" folder");
								}
							}
							if(!directory.getParentFile().getParentFile().equals("/data/local/tmp") || !directory.getParentFile().getParentFile().equals("/data/local")){
								if(!permissionsset(directory.getParentFile())){	
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory.getParentFile().getParentFile());
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory.getParentFile().getParentFile());
									//System.out.println("setting permissions on "+directory.getParentFile().getParentFile()+" folder");
									dirs.add(directory.getParentFile().getParentFile());
								}
							}
							if(directory.getParentFile().exists()){
								if(!permissionsset(directory.getParentFile())){	
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory.getParentFile());
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+directory.getParentFile());
									//System.out.println("setting permissions on "+directory.getParentFile()+" folder");
									dirs.add(directory.getParentFile());
								}
							}
						} else {
							File directory = file.getParentFile();
							if(!permissionsset(directory)){
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+directory);
								dirs.add(directory);
								//System.out.println("setting permissions on "+directory+" folder");
							}
						}
						if (file.exists()){
							if(zipEntry.isDirectory()){ 
								if(!permissionsset(file)){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/"+zipEntry);
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /data/local/tmp/"+zipEntry);
									dirs.add(file);
								}
							}else{	
								String fileperm = permission(file);
								String fileown = owner(file);
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+file);
								byte buffer[] = new byte[BUFFER_SIZE];
								FileOutputStream fileOutputStream = new FileOutputStream(file);
								bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
								int count;

								while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
									bufferedOutputStream.write(buffer, 0, count);
								}
								//System.out.println("unizipping "+file);
								u.log("already exists but overwriting "+file+" "+fileperm+" "+fileown);
								notify("Installing "+romshort, "Unzipping "+file.getName());
								if(fileown!=null){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown "+fileown+" "+file);
								}
								if(fileperm!=null){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod "+fileperm+" "+file);
								}
								bufferedOutputStream.flush();
								bufferedOutputStream.close(); 
								zipInputStream.closeEntry();
							}
						} else {
							if(zipEntry.isDirectory()){ 
								file.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/"+zipEntry);
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 /data/local/tmp/"+zipEntry);
								dirs.add(file);
							}else{	
								byte buffer[] = new byte[BUFFER_SIZE];
								FileOutputStream fileOutputStream = new FileOutputStream(file);
								bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
								int count;

								while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
									bufferedOutputStream.write(buffer, 0, count);
								}
								//System.out.println("unizipping "+file);
								u.log("unzipping "+file);
								notify("Installing "+romshort, "Unzipping "+file.getName());
								filePerms.add(file);
								//u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+file);
								//u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 755 "+file);
								bufferedOutputStream.flush();
								bufferedOutputStream.close(); 
								zipInputStream.closeEntry();
							}
						}
					} else {
						File appdir = new File("/data/local/tmp/system/app");
						File framedir = new File("/data/local/tmp/system/framework");
						File fontdir = new File("/data/local/tmp/system/fonts");
						File langdir = new File("/data/local/tmp/system/tts/lang_pico");
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+appdir);
						u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+framedir);
						int slash = zipEntryName.lastIndexOf("/");
						String name;
						if(slash > -1){
							name = zipEntryName.substring(slash);
						} else {
							name = zipEntryName;
						}
						File file2 = null;

						if(zipEntryName.endsWith("apk")){ 
							if(!appdir.exists()){
								appdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+appdir);
							}
							if(!framedir.exists()){
								framedir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+framedir);
							}
							if(zipEntryName.contains("com.htc.resources.apk")||zipEntryName.contains("framework-res.apk")){
								file2=new File("/data/local/tmp/system/framework"+name);
							} else {
								file2=new File("/data/local/tmp/system/app"+name);
							}
						}
						if(zipEntryName.endsWith("jar")){
							if(!framedir.exists()){
								//framedir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/system/framework");
								u.log("framework folder made");
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+framedir);
							}
							file2=new File("/data/local/tmp/system/framework"+name);

						}
						if(zipEntryName.endsWith("ttf")){
							if(!fontdir.exists()){
								fontdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+fontdir);
							}
							file2=new File("/data/local/tmp/system/fonts"+name);

						}
						if(zipEntryName.endsWith("bin")){
							if(!langdir.exists()){
								langdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+langdir);
							}
							file2=new File("/data/local/tmp/system/tts/lang_pico"+name);

						}
						if(zipEntryName.endsWith("bmp")){
							File mediadir = new File("/data/local/tmp/system/media/GPU");
							if(!mediadir.exists()){
								mediadir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+mediadir);
							}
							file2=new File("/data/local/tmp/system/media/GPU"+name);

						}

						if(zipEntryName.endsWith("ogg")){
							File mediadir = new File("/data/local/tmp/system/media/audio/ui");
							File mediadir2 = new File("/data/local/tmp/system/usr/keychars");
							if(!mediadir.exists()){
								mediadir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+mediadir);
							}
							if(!mediadir2.exists()){
								mediadir2.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+mediadir2);
							}
							if(zipEntryName.contains("ui")){
								file2=new File("/data/local/tmp/system/media/audio/ui"+name);
							}
							if(zipEntryName.contains("keychars")){
								file2=new File("/data/local/tmp/system/usr/keychars"+name);
							}
						}

						if(zipEntryName.endsWith("mp3")){
							File mediadir = new File("/data/local/tmp/system/media/audio/alarms");
							File mediadir2 = new File("/data/local/tmp/system/media/audio/ringtones");
							File mediadir3 = new File("/data/local/tmp/system/media/audio/notifications");
							File mediadir4 = new File("/data/local/tmp/system/media/audio/ui");
							if(!mediadir.exists()){
								mediadir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+mediadir);
							}
							if(!mediadir2.exists()){
								mediadir2.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+mediadir2);
							}
							if(!mediadir3.exists()){
								mediadir3.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+mediadir3);
							}
							if(!mediadir4.exists()){
								mediadir4.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+mediadir4);
							}
							if(zipEntryName.contains("alarms")){
								file2=new File("/data/local/tmp/system/media/audio/alarms"+name);
							}
							if(zipEntryName.contains("ringtones")){
								file2=new File("/data/local/tmp/system/media/audio/ringtones"+name);
							}
							if(zipEntryName.contains("notifications")){
								file2=new File("/data/local/tmp/system/media/audio/notifications"+name);
							}
							if(zipEntryName.contains("ui")){
								file2=new File("/data/local/tmp/system/media/audio/ui"+name);
							}

						}

						if(zipEntryName.endsWith("so")){
							File libdir = new File("/data/local/tmp/system/lib");
							if(!libdir.exists()){
								libdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir);
							}
							file2=new File("/data/local/tmp/system/lib"+name);

						}

						if(zipEntryName.endsWith("xml")){
							File libdir = new File("/data/local/tmp/system/customize");
							File libdir2 = new File("/data/local/tmp/system/customize/CID");
							File libdir3 = new File("/data/local/tmp/system/customize/MNS");
							if(!libdir.exists()){
								libdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir);
							}
							if(!libdir2.exists()){
								libdir2.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir2);
							}
							if(!libdir3.exists()){
								libdir3.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir3);
							}
							if(zipEntryName.contains("customize")){
								file2=new File("/data/local/tmp/system/customize"+name);
							}
							if(zipEntryName.contains("CID")){
								file2=new File("/data/local/tmp/system/customize/CID"+name);
							}
							if(zipEntryName.contains("MNS")){
								file2=new File("/data/local/tmp/system/customize/MNS"+name);
							}

						}

						if(zipEntryName.endsWith("txt")){
							File libdir = new File("/data/local/tmp/system/customize");
							if(!libdir.exists()){
								libdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir);
							}
							File libdir2 = new File("/data/local/tmp/system/customize/resource");
							if(!libdir2.exists()){
								libdir2.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir2);
							}
							File libdir3 = new File("/data/local/tmp/system/etc/soundimage");
							if(!libdir3.exists()){
								libdir3.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir3);
							}
							if(zipEntryName.contains("customize")){
								file2=new File("/data/local/tmp/system/customize"+name);
							}
							if(zipEntryName.contains("resource")){
								file2=new File("/data/local/tmp/system/customize/resource"+name);
							}
							if(zipEntryName.contains("soundimage")){
								file2=new File("/data/local/tmp/system/etc/soundimage"+name);
							}

						}

						if(zipEntryName.endsWith("jpg")){
							File libdir = new File("/data/local/tmp/system/customize/resource");
							if(!libdir.exists()){
								libdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir);
							}
							File libdir2 = new File("/data/local/tmp/system/etc/slideshow");
							if(!libdir2.exists()){
								libdir2.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir2);
							}
							if(zipEntryName.contains("resource")){
								file2=new File("/data/local/tmp/system/customize/resource"+name);
							}
							if(zipEntryName.contains("slideshow")){
								file2=new File("/data/local/tmp/system/etc/slideshow"+name);
							}

						}

						if(zipEntryName.endsWith("png")){
							File libdir = new File("/data/local/tmp/system/customize/resource");
							if(!libdir.exists()){
								libdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir);
							}
							if(zipEntryName.contains("resource")){
								file2=new File("/data/local/tmp/system/customize/resource"+name);
							}

						}

						if(zipEntryName.endsWith("zip")){
							File libdir = new File("/data/local/tmp/system/customize/resource");
							if(!libdir.exists()){
								libdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir);
							}
							if(zipEntryName.contains("resource")){
								file2=new File("/data/local/tmp/system/customize/resource"+name);
							}

						}

						if(zipEntryName.endsWith("mp4")){
							File libdir = new File("/data/local/tmp/system/media/weather");
							if(!libdir.exists()){
								libdir.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+libdir);
							}
							if(zipEntryName.contains("weather")){
								file2=new File("/data/local/tmp/system/media/weather"+name);
							}
						}

						if(file2!=null){
							String fileperm = null;
							String fileown = null;
							if(file2.getParentFile().exists()){
								if(!permissionsset(file2.getParentFile())){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+file2.getParentFile());
									dirs.add(file2.getParentFile());
								}
							}
							if(file2.exists()){
								//System.out.println(file2+" exists");
								fileperm = permission(file2);
								fileown = owner(file2);
								u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 "+file2);
							}
							if(zipEntryName.contains("noneon")){
								CommandResult neon = s.su.runWaitFor("cat /proc/cpuinfo");
								if(neon.stdout!=null){
									if(!neon.stdout.contains("neon")){
										System.out.println("neon not detected");
										byte buffer[] = new byte[BUFFER_SIZE];
										FileOutputStream fileOutputStream = new FileOutputStream(file2);
										bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
										int count;

										while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
											bufferedOutputStream.write(buffer, 0, count);
										}
										bufferedOutputStream.flush();
										bufferedOutputStream.close(); 
										zipInputStream.closeEntry();
										notify("Installing "+romshort, "Unzipping "+file.getName());
										if(fileown!=null){
											u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown "+fileown+" "+file2);
										} else {
											if(!filePerms.contains(file2))filePerms.add(file2);
											//u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+file);
										}
										if(fileperm!=null){
											u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod "+fileperm+" "+file2);
										} else {
											if(!filePerms.contains(file2))filePerms.add(file2);
											//u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 755 "+file);
										}
									}
								} 
								fileown=null;
								fileperm=null;
							} else {
								byte buffer[] = new byte[BUFFER_SIZE];
								FileOutputStream fileOutputStream = new FileOutputStream(file2);
								bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
								int count;

								while ((count = zipInputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
									bufferedOutputStream.write(buffer, 0, count);
								}
								//System.out.println("unizipping "+file2);
								notify("Installing "+romshort, "Unzipping "+file2.getName());
								if(fileown!=null){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown "+fileown+" "+file2);
								} else {
									if(!filePerms.contains(file2))filePerms.add(file2);
									//u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chown 0.0 "+file);
								}
								if(fileperm!=null){
									u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod "+fileperm+" "+file2);
								} else {
									if(!filePerms.contains(file2))filePerms.add(file2);
									//u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 755 "+file);
								}
								fileown=null;
								fileperm=null;
								bufferedOutputStream.flush();
								bufferedOutputStream.close(); 
								zipInputStream.closeEntry();
							}
						}
					} 
				}
			}
			if(ziplist.size() > 0){ 
				for(int z = 0; z < ziplist.size(); z++){
					ZipFile zip = new ZipFile(new File(src));
					InputStream zipI = zip.getInputStream(ziplist.get(z));
					byte buffer[] = new byte[BUFFER_SIZE];
					String zipentryname = ziplist.get(z).getName();
					int add = board.length()+8;
					if(zipentryname.contains("devices/"+board+"/system/")){
						File zipfile = new File("/data/local/tmp/"+zipentryname.substring(zipentryname.lastIndexOf("devices/"+board+"/system/")+add));
						if(ziplist.get(z).isDirectory()){
							if(!zipfile.exists()){
								zipfile.mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox chmod 777 "+zipfile);
							}

						} else {
							if(zipfile.exists()){
								u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox rm "+zipfile);
							}
							if(!zipfile.getParentFile().exists()){
								zipfile.getParentFile().mkdirs();
								u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox chmod 777 "+zipfile.getParentFile());
							}
							notify("Installing "+romshort, "Unzipping "+zipfile.getName());
							FileOutputStream fileOutputStream = new FileOutputStream(zipfile);
							bufferedOutputStream = new BufferedOutputStream(fileOutputStream, BUFFER_SIZE);
							int count;
							while ((count = zipI.read(buffer, 0, BUFFER_SIZE)) != -1) {
								bufferedOutputStream.write(buffer, 0, count);
							}
							u.log("Extracting "+zipentryname+" to "+zipfile);
							bufferedOutputStream.flush();
							bufferedOutputStream.close(); 
						}
					}
					zipI.close();
				}
			}
			zipInputStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			u.log(e.toString());
		}catch (IOException e) {
			e.printStackTrace();
			u.log(e.toString());
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

	private void addBootimgtoUpdateRezound(String slot){
		File updatezip = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/update.zip");
		if(updatezip.exists())updatezip.delete();
		try{
			byte[] buffer = new byte[4096];
			ZipInputStream zin = new ZipInputStream(new FileInputStream(new File(u.getExternalDirectory()+"/BootManager/.zips/BootImageFlasherRezound.zip")));
			FileInputStream bootis = new FileInputStream(new File(u.getExternalDirectory()+"/BootManager/"+slot+"/boot.img"));
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(updatezip));
			ZipEntry ze;
			while ((ze = zin.getNextEntry()) != null){
				out.putNextEntry(new ZipEntry(ze));
				for(int read = zin.read(buffer); read > -1; read = zin.read(buffer)){
					out.write(buffer, 0, read);
				}
				out.closeEntry();
			}
			ZipEntry bootentry = new ZipEntry("kernel/boot.img");
			out.putNextEntry(bootentry);
			for(int read = bootis.read(buffer); read > -1; read = bootis.read(buffer)){
				out.write(buffer, 0, read);
			}
			out.closeEntry();
			bootis.close();

			out.close();
			zin.close();
		}catch(Exception e){
			u.log(e.toString());
			e.printStackTrace();
		}

	}

	private void getUpdateZip(String slot){
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