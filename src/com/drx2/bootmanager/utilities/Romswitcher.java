package com.drx2.bootmanager.utilities;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;

public class Romswitcher extends Activity {
	Boolean useemmc;
	Utilities u = new Utilities();
	ShellCommand s = new ShellCommand();
	Context context;
	String slot;
	private static final String PREFS_DEVICE = "DeviceInfo";
	
	 protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        //setContentView(R.layout.blank);
	        context = getApplicationContext();
	        Bundle extras = getIntent().getExtras();
	        slot=extras.getString("slot");
	        confirmTask cft = new confirmTask(slot, currentRom());
	        cft.execute();
	        //Confirm(slot, currentRom());
	        //finish();
	
	 
	 }
	 
	 private String currentRom(){
		 	String current=null;
		 	String currentromname;
		 	String board;
		 	SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
			board=settings.getString("device", "");
		    if(board.equals("tuna")){
		    	currentromname=null;
		    	File init = new File("/init.tuna.rc");
				if(init.exists()){
					try {
						FileInputStream fis = Utilities.rootFileInputStream(init.toString());
						DataInputStream dis = new DataInputStream(fis);
						BufferedReader br = new BufferedReader(new InputStreamReader(dis));
						String data;
						while((data = br.readLine()) != null) {
							if(data.contains("/BootManager/rom")){
								int start = data.indexOf("/BootManager/rom")+13;
								int end = start + 4;
								currentromname=data.substring(start, end);
								String number = currentromname.substring(currentromname.length()-1);
								if(u.checkIfNumber(number)){
									current = currentromname;
								} else {
									current = "phoneRom";
								}
								break;
							}
						}
						fis.close();
						br.close();
						dis.close();
						} catch (Exception e) {
							e.printStackTrace();
							u.log(e.toString());
						}	
					} 
		    } else {
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
		    }
		    if(current==null)current = "phoneRom";
		return current;
		 
	 }
	 	 
	 private void reboot() {
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/reboot"); 
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
		//TODO add variable for cache partition so other phones can use recovery install
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
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
    	String board=settings.getString("device", "");
		if(board.equalsIgnoreCase("tegra")){
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
						   	u.errorDialog(Romswitcher.this, "Error", "Boot.img failed to flash. Not rebooting");
					   	}
					});
					} else {
					reboot();
				}
			} else if(exit!=0){
				u.log("Error Flashing boot.img");
				runOnUiThread(new Runnable() {                
				   	public void run() {
					   u.errorDialog(Romswitcher.this, "Error", "Boot.img failed to flash. Not rebooting");
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
						   	u.errorDialog(Romswitcher.this, "Error", "Boot.img failed to flash. Not rebooting");
					   	}
					});
					} else {
					reboot();
				}
			} else if(exit!=0){
				u.log("Error Flashing boot.img");
				runOnUiThread(new Runnable() {                
				   	public void run() {
					   u.errorDialog(Romswitcher.this, "Error", "Boot.img failed to flash. Not rebooting");
				   }
			});
			} else {
				reboot();
			}
		}
	}
	
	private void flashbootSpecifyBootPartition(String boot, String bootpartition) {
		u.log("Flashing boot");
		CommandResult result = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/flash_image "+bootpartition+" " +boot);
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
			//eraseboot();//TODO Don't know if we should do this here
			CommandResult result2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/flash_image "+bootpartition+" " +boot);
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
					   	u.errorDialog(Romswitcher.this, "Error", "Boot.img failed to flash. Not rebooting");
				   	}
				});
				} else {
				reboot();
			}
		} else if(exit!=0){
			u.log("Error Flashing boot.img");
			runOnUiThread(new Runnable() {                
			   	public void run() {
				   u.errorDialog(Romswitcher.this, "Error", "Boot.img failed to flash. Not rebooting");
			   }
		});
		} else {
			reboot();
		}
	}
	
	private void eraseboot() {
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/erase_image boot");
	}
	
	
	private void secondinit(String boot){
		SharedPreferences settings = context.getSharedPreferences(PREFS_DEVICE, 0);
		String device = settings.getString("device", "");
		Boolean reboot=false;
		if(new File("/system/xbin/cat.jpg").exists()){
			u.execCommand("/system/xbin/cat.jpg");
		}
		CommandResult result = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mkdir /data/local/tmp/realsystem");
		if(result.stderr!=null){
			u.log(result.stderr);
		}
		if(result.stdout!=null){
			u.log(result.stdout);
		}
		CommandResult result1 = null;
		if(device.contains("spyder")||device.contains("maserati")){
			result1 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount /dev/block/mmcblk1p20 /data/local/tmp/realsystem");
		}else if(device.contains("shadow")||device.equals("droid2")||device.equals("droid2we")||device.equals("targa")||device.equals("solana")){
			result1 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount /dev/block/mmcblk1p21 /data/local/tmp/realsystem");
		}
		if(result1.stderr!=null){
			u.log(result1.stderr);
		}
		if(result1.stdout!=null){
			u.log(result1.stdout);
		}
		if(new File("/data/local/tmp/realsystem/etc/hijack-boot.zip").exists()){
			if(new File(u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip").exists()){
				CommandResult result2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip /data/local/tmp/realsystem/etc/hijack-boot.zip");
				if(result2.stderr!=null){
					u.log(result2.stderr);
				}
				if(result2.stdout!=null){
					u.log(result2.stdout);
				}
				reboot=true; 
			}else if(Utilities.device().equals("spyder")||Utilities.device().equals("solana")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")){
				CommandResult result2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory() + "-ext/BootManager/" + boot + "/hijack-boot.zip /data/local/tmp/realsystem/etc/hijack-boot.zip");
				if(result2.stderr!=null){
					u.log(result2.stderr);
				}
				if(result2.stdout!=null){
					u.log(result2.stdout);
				}
				reboot=true; 
			}else{
				Toast.makeText(Romswitcher.this, "No ROM installed to " + boot, Toast.LENGTH_LONG).show();
			}
		} else {
			if(new File(u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip").exists()){
				CommandResult result3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox mount -o rw,remount /system");
				if(result3.stderr!=null){
					u.log(result3.stderr);
				}
				if(result3.stdout!=null){
					u.log(result3.stdout);
				}
				CommandResult result4 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox cp "+u.getExternalDirectory() + "/BootManager/" + boot + "/hijack-boot.zip /system/etc/hijack-boot.zip");
				if(result4.stderr!=null){
					u.log(result4.stderr);
				}
				if(result4.stdout!=null){
					u.log(result4.stdout);
				}
				if(new File("/system/etc/hijack-boot.zip").exists()){
					reboot=true;
				} else {
					Toast.makeText(Romswitcher.this, "Error rebooting too " + boot, Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(Romswitcher.this, "No ROM installed to " + boot, Toast.LENGTH_LONG).show();
			}
		}
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/realsystem");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rmdir /data/local/tmp/realsystem");
		if(reboot){
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm /internaldata/.recovery_mode");
			reboot();
		}
		
	}
	
	class confirmTask extends AsyncTask<Void, Void, Void> {
		File f0;
		File f1;
		File s0;
		File s1;
        CustomProgressDialog p;
		
		public confirmTask(String slot, String currentrom) {
			this.f0 = new File(u.getExternalDirectory()+"/BootManager/"+currentrom+"/synccalls");
			this.f1 = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/synccalls");
			this.s0 = new File(u.getExternalDirectory()+"/BootManager/"+currentrom+"/syncsms");
			this.s1 = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/syncsms");
			p = CustomProgressDialog.show(Romswitcher.this, "Rebooting", "Please wait ...", true,true);
        }

		@Override
		protected Void doInBackground(Void... arg0) {
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
			return null;
		}
		@Override 
		protected void onPostExecute(Void unused) {
			SharedPreferences settings = context.getSharedPreferences(PREFS_DEVICE, 0);
			String device = settings.getString("device", "");
			if(device.equals("sholes")){
				if (new File(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img").exists()){
					recoveryInstall(slot);
				} else {
					Toast.makeText(Romswitcher.this, "No ROM installed to " + slot, Toast.LENGTH_LONG).show();
				}
			} else if(device.equals("tuna")){
				if (new File(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img").exists()){
					flashbootSpecifyBootPartition(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img", "/dev/block/mmcblk0p7");
				} else {
					Toast.makeText(Romswitcher.this, "No ROM installed to " + slot, Toast.LENGTH_LONG).show();
				}
			} else if(device.equals("tegra")||device.equals("herring")||device.equals("otter")){
				if (new File(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img").exists()){
					String boot = settings.getString("boot", "");
					flashbootSpecifyBootPartition(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img", boot);
				} else {
					Toast.makeText(Romswitcher.this, "No ROM installed to " + slot, Toast.LENGTH_LONG).show();
				}
			} else if(device.equals("shadow")||device.equals("droid2")||device.equals("droid2we")||device.equals("spyder")||device.contains("maserati")||device.contains("targa")||device.contains("solana")) {
				secondinit(slot);
			}else{
				if (new File(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img").exists()){
					flashboot(u.getExternalDirectory() + "/BootManager/" + slot + "/boot.img");
				} else {
					Toast.makeText(Romswitcher.this, "No ROM installed to " + slot, Toast.LENGTH_LONG).show();
				}
			}
			if(p!=null)p.dismiss();
			Romswitcher.this.finish();
		}       
    }
	
}
