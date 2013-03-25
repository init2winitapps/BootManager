package com.drx2.bootmanager.widget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.drx2.bootmanager.Loader;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.CallLogBackupRestore;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.drx2.bootmanager.utilities.Utilities;

public class WideWidgetDialog extends Activity {
	private static final String PREFS_DEVICE = "DeviceInfo";
	String slot;
	String name1;
	String name2;
	String name3;
	String name4;
	String name5;
	String none = "No Rom Set!";
	Utilities u = new Utilities();
	Loader l = new Loader();
	ShellCommand s = new ShellCommand();
	Context context;
	String board;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		SharedPreferences settings2 = context.getSharedPreferences(PREFS_DEVICE, 0);
		Boolean useemmc=settings2.getBoolean("useemmc", false);
		l.setupemmc(context, useemmc);
		Bundle extras = getIntent().getExtras();
		slot=extras.getString("rom");
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
    	board=settings.getString("device", "");
    	getnames();
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setOnCancelListener(new DialogInterface.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				WideWidgetDialog.this.finish(); 
			}
		});
		//TODO check slot name dialog
		String sName = new String("rom"); 
		if(slot.equals("rom1")){
			sName = new String(name1); 
		}
		if(slot.equals("rom2")){
			sName = new String(name2); 
		}
		if(slot.equals("rom3")){
			sName = new String(name3); 
		}
		if(slot.equals("rom4")){
			sName = new String(name4); 
		}
		if(slot.equals("phoneRom")){
			sName = new String(name5); 
		}
		if(sName.equals(none)){
			builder.setMessage("No ROM found in this Slot!")
		       .setCancelable(true)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   dialog.cancel();
		           }
		       }).show();
		}else{
		builder.setMessage("Are you sure you want to reboot to "+sName)
		       .setCancelable(true)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   syncData(slot, currentRom());
		        	   if (new File(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img").exists()){
			        	   if(board.equals("sholes")){
			        		   recoveryInstall(slot);
			        	   } else {
			   				flashboot(u.getExternalDirectory() + "/BootManager/"+slot+"/boot.img");
			        	   }
		        	   } else if(new File(u.getExternalDirectory()+"/BootManager/"+slot+"/hijack-boot.zip").exists()){
		        		   if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")) {
		        			   u.log("Board detected as "+board);
		        			   secondinit(slot);
		        		   }
		        	   } else {
							Toast.makeText(WideWidgetDialog.this, "No Rom Installed to "+slot, Toast.LENGTH_LONG).show();
							finish();
		        	   }
		        	}
		       })
		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   WideWidgetDialog.this.finish();
		           }
		       }).show();
		}
    }
	
	private void recoveryInstall(String slot){
		
		if(new File("/system/lib/modules/mbcache.ko").exists()){
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox insmod /system/lib/modules/mbcache.ko");
		}
		if(new File("/system/lib/modules/ext2.ko").exists()){
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox insmod /system/lib/modules/ext2.ko");
		}
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/cache");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 777 /data/local/tmp/cache");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount /dev/block/mtdblock5 /data/local/tmp/cache");
		File recovery = new File("/data/local/tmp/cache/recovery");
		if(!(recovery.exists())){
		recovery.mkdir();
		}
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo \"install_zip SDCARD:\"`" + context.getFilesDir().getAbsolutePath()+ "/busybox echo " + "/sdcard/BootManager/" + slot + "/update.zip | " + context.getFilesDir().getAbsolutePath()+ "/busybox sed 's|/sdcard/||'`\"\" >> /data/local/tmp/cache/recovery/extendedcommand");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/cache");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/reboot recovery");
	}
	
	private void flashboot(String boot) {
		u.log("Flashing boot");
		if(board.equalsIgnoreCase("tegra")){
			SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
	    	String bootblock=settings.getString("boot", "");
			CommandResult result = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox dd if=/dev/zero of="+bootblock);
			if(result.stderr!=null){
				u.log(result.stderr);
			}
			if(result.stdout!=null){
				u.log(result.stdout);
			}
			CommandResult result2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox dd if="+boot+" of="+bootblock);
			if(result2.stderr!=null){
				u.log(result2.stderr);
			}
			if(result2.stdout!=null){
				u.log(result2.stdout);
			}
			reboot();
		} else if(board.equalsIgnoreCase("aloha") || board.equalsIgnoreCase("buzz")){
			CommandResult result = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/morebinarys/flash_image boot " +boot);
			int exit;
			if(result.exit_value!=null){
				exit=result.exit_value;
			} else {
				exit=0;
			}
			if(result.stderr!=null){
				u.log(result.stderr);
			}
			if(result.stdout!=null){
				u.log(result.stdout);
			}
			String theresult = result.stdout;
			if(theresult!=null){
				//Do nothing so result is same
			} else {
				theresult="ok";
			}
			if((theresult).contains("header is the same")){
				u.log("header is same...erasing boot");
				eraseboot();
				CommandResult result2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/morebinarys/flash_image boot " +boot);
				if(result2.stderr!=null){
					u.log(result2.stderr);
				}
				if(result2.stdout!=null){
					u.log(result2.stdout);
				}
				if((result2.exit_value)!=0){
					u.log("Error Flashing boot.img");
					runOnUiThread(new Runnable() {                
						public void run() {
						   	u.errorDialog(WideWidgetDialog.this, "Error", "Boot.img failed to flash. Not rebooting");
					   	}
					});
					} else {
					reboot();
				}
			} else if(exit!=0){
				u.log("Error Flashing boot.img");
				runOnUiThread(new Runnable() {                
				   	public void run() {
					   u.errorDialog(WideWidgetDialog.this, "Error", "Boot.img failed to flash. Not rebooting");
				   }
			});
			} else {
				reboot();
			}
		} else {
			CommandResult result = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/flash_image boot " +boot);
			int exit;
			if(result.exit_value!=null){
				exit=result.exit_value;
			} else {
				exit=0;
			}
			if(result.stderr!=null){
				u.log(result.stderr);
			}
			if(result.stdout!=null){
				u.log(result.stdout);
			}
			String theresult = result.stdout;
			if(theresult!=null){
				//Do nothing so result is same
			} else {
				theresult="ok";
			}
			if((theresult).contains("header is the same")){
				u.log("header is same...erasing boot");
				eraseboot();
				CommandResult result2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/flash_image boot " +boot);
				if(result2.stderr!=null){
					u.log(result2.stderr);
				}
				if(result2.stdout!=null){
					u.log(result2.stdout);
				}
				if((result2.exit_value)!=0){
					u.log("Error Flashing boot.img");
					runOnUiThread(new Runnable() {                
						public void run() {
						   	u.errorDialog(WideWidgetDialog.this, "Error", "Boot.img failed to flash. Not rebooting");
					   	}
					});
					} else {
					reboot();
				}
			} else if(exit!=0){
				u.log("Error Flashing boot.img");
				runOnUiThread(new Runnable() {                
				   	public void run() {
					   u.errorDialog(WideWidgetDialog.this, "Error", "Boot.img failed to flash. Not rebooting");
				   }
			});
			} else {
				reboot();
			}
		}
	}
	
	private void eraseboot() {
		CommandResult result2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/erase_image boot");
		if(result2.stderr!=null){
			u.log(result2.stderr);
		}
		if(result2.stdout!=null){
			u.log(result2.stdout);
		}
	}
	
	private void reboot() {
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/reboot"); 
	 }
	
	private void getnames() {
		name1=readRomName(u.getExternalDirectory() +"/BootManager/rom1/name");
		name2=readRomName(u.getExternalDirectory() +"/BootManager/rom2/name");
		name3=readRomName(u.getExternalDirectory() +"/BootManager/rom3/name");
		name4=readRomName(u.getExternalDirectory() +"/BootManager/rom4/name");
		name5=readRomName(u.getExternalDirectory() +"/BootManager/phoneRom/name");
		if(name1 !=null ){
		} else {
			name1=none;
		}
		if(name2 !=null ){
		} else {
			name2=none;
		}
		if(name3 !=null ){
		} else {
			name3=none;
		}
		if(name4 !=null ){
		} else {
			name4=none;
		}
		if(name5 !=null ){
		} else {
			name5=none;
		}
	}
	
	private static String readRomName(String fname) {
        BufferedReader br;
        String line = null;

        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
        	
        }
        return line;
    }
	
	private void secondinit(String boot){
		Boolean reboot=false;
		if(new File("/system/xbin/cat.jpg").exists()){
			u.execCommand("/system/xbin/cat.jpg");
		}
		u.log("Second init called");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/realsystem");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount /dev/block/mmcblk1p21 /data/local/tmp/realsystem");
		if(new File("/data/local/tmp/realsystem/etc/hijack-boot.zip").exists()){
			if(new File(u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip").exists()){
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip /data/local/tmp/realsystem/etc/hijack-boot.zip");
				reboot=true; 
			}else{
				Toast.makeText(WideWidgetDialog.this, "No ROM installed to " + boot, Toast.LENGTH_LONG).show();
			}
		} else {
			if(new File(u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip").exists()){
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o rw,remount /system");
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip /system/etc/hijack-boot.zip");
				if(new File("/system/etc/hijack-boot.zip").exists()){
					reboot=true;
				} else {
					Toast.makeText(WideWidgetDialog.this, "Error rebooting too " + boot, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(WideWidgetDialog.this, "No ROM installed to " + boot, Toast.LENGTH_LONG).show();
			}
		}
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/realsystem");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rmdir /data/local/tmp/realsystem");
		if(reboot){
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /internaldata/.recovery_mode");
			reboot();
		}
		
	}
	
	private void syncData(final String slot, final String currentrom){
	 	File f0 = new File(u.getExternalDirectory()+"/BootManager/"+currentrom+"/synccalls");
		File f1 = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/synccalls");
		File s0 = new File(u.getExternalDirectory()+"/BootManager/"+currentrom+"/syncsms");
		File s1 = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/syncsms");
		if(f0.exists() && f1.exists()){
			//Backup the phone log here
			CallLogBackupRestore c = new CallLogBackupRestore();
			c.getCallLog(context);
		}
		if(s0.exists() && s1.exists()){
			//Backup sms here
			CallLogBackupRestore c = new CallLogBackupRestore();
			c.getSMS(context);
		}
	}
	
	private String currentRom(){
	 	String current;
	 	CommandResult currentrom = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox losetup /dev/block/loop0");
    	if(currentrom.stdout!=null){
    		if(currentrom.stdout.contains("rom1")){
    			current="rom1";
    		} else if(currentrom.stdout.contains("rom2")){
    			current="rom2";
    		} else if(currentrom.stdout.contains("rom3")){
    			current="rom3";
    		} else if(currentrom.stdout.contains("rom4")){
    			current="rom4";
    		} else {
    			current="phoneRom";
    		}
    	} else {
    		current="phoneRom";
    	}
    	return current;
	}
}