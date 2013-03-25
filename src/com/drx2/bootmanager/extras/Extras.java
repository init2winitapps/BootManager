package com.drx2.bootmanager.extras;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment; 
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.drx2.bootmanager.Preferences;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.CustomProgressDialog;
import com.drx2.bootmanager.utilities.Utilities;

public class Extras extends ListFragment { 

	Utilities u = new Utilities();
	CustomProgressDialog extraDialog = null;
	GradientDrawable d;
	Context context;
	private Preferences vmPrefs;
	private Preferences sdBoostPrefs;
	private static final String PREFS_DEVICE = "DeviceInfo";
	String board;
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.getListView().setTextFilterEnabled(true);
        context = getActivity().getApplicationContext();
        SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
        int startcolor = colors.getInt("buttonStart", context.getResources().getColor(R.color.buttonStart));
		int endcolor = colors.getInt("buttonEnd", context.getResources().getColor(R.color.buttonEnd));
		int[] color = {startcolor, endcolor};
		d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
        this.getListView().setSelector(d);
        if(colors.getString("device", "").equals("tuna")){
        	this.setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.row, tunaitems));
        } else if(colors.getString("device", "").equals("spyder")||colors.getString("device", "").contains("maserati")||colors.getString("device", "").contains("targa")||colors.getString("device", "").contains("solana") && Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory() + "/BootManager/DoNotDelete").equals("internal")){
        	this.setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.row, tunaitems));
        }else {
        	this.setListAdapter(new ArrayAdapter<String>(getActivity(), R.layout.row, items));
        }
        this.setEmptyText(getString(R.string.loading));
        vmPrefs = new Preferences(getActivity().getApplicationContext());
        sdBoostPrefs = new Preferences(getActivity().getApplicationContext());
    }
    final String[] items = new String[] {"Clean Cache", "Empty Trash", "Fix Permissions", "SD Booster", "System Mount", "Reboot Options", "VM Heap Size", "Wipe Dalvik Cache", "Zipalign Applications"};
    final String[] tunaitems = new String[] {"Clean Cache", "Empty Trash", "Fix Permissions", "System Mount", "Reboot Options", "VM Heap Size", "Wipe Dalvik Cache", "Zipalign Applications"};
    @Override
	public void onListItemClick(ListView l, View v, int position, long id) {
     super.onListItemClick(l, v, position, id);
     	if(l.getItemAtPosition(position).toString().equals("Clean Cache")){
     		//Clean cache
			final CharSequence[] items = {getActivity().getString(R.string.app_cache), getActivity().getString(R.string.sd_cache), getActivity().getString(R.string.market_history)};
	        final boolean[] states = {false, false, false};
	        CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
	        builder.setTitle("Clear Cache");
	        builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener(){
	            public void onClick(DialogInterface dialogInterface, int item, boolean state) {
	            }
	        });
	        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	            	SparseBooleanArray CheCked =((CustomDialog)dialog).getListView().getCheckedItemPositions();
	            	if(CheCked.get(0)){
						u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox rm -r /data/data/*/cache/*");
						u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox rm /cache/*.apk");
	            	}
	            	if(CheCked.get(1)){
	            		u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox rm -r /sdcard/Android/data/*/cache/*");
	            	}
	            	if(CheCked.get(2)){
	            		u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox rm /data/data/com.android.vending/databases/suggestions.db");
	            	}
            		Toast.makeText(getActivity(), "Cache Cleared Successfully", Toast.LENGTH_SHORT).show();
	            }
	        });
	        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog, int id) {
	                dialog.cancel();
	            }
	        }).show();
     	}
     	if(l.getItemAtPosition(position).toString().equals("Empty Trash")){
     		//empty trash 
			CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
			builder.setTitle(R.string.button_trash)
				   .setMessage(R.string.trash)
			       .setCancelable(false)
			       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   extraDialog = CustomProgressDialog.show(getActivity(), "Cleaning Trash", "Please wait ...", true,true);
				           superThread threadActivity = new superThread("trash");
						   threadActivity.start();
			           }
			       })
			       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       }).show();
     	}
     	if(l.getItemAtPosition(position).toString().equals("Fix Permissions")){
     		//fix perms 
			CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
			builder.setTitle(R.string.fixPerm)
				   .setMessage("Are you sure you want to fix permissions?")
			       .setCancelable(false)
			       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
							extraDialog = CustomProgressDialog.show(getActivity(), "Fixing Permissions", "Please wait ...", true,true);
				        	superThread threadActivity = new superThread("fixperm");
							threadActivity.start();
			           }
			       })
			       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       }).show();
     	}
     	if(l.getItemAtPosition(position).toString().equals("SD Booster")){
     		//sd booster 
			SharedPreferences settings = getActivity().getSharedPreferences(PREFS_DEVICE, 0);
	    	if(settings.contains("sdboostIndex")){
	    		sdBoost();
	    	}else{
	    		SDwarning();
	    	}
     	}
     	if(l.getItemAtPosition(position).toString().equals("System Mount")){
     		//system mount
     		systemMounter();
     	}
     	if(l.getItemAtPosition(position).toString().equals("Reboot Options")){
     		//reboot options
     		final CharSequence[] items = {"Reboot", "Reboot Recovery", "Hot Reboot"};
			CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
			builder.setTitle("Reboot Options");
			builder.setItems(items, new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog, int item) {
			    	if(items[item] == "Reboot"){
			    		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
			    		builder.setTitle(R.string.button_reboot)
			    			   .setMessage(R.string.reboot_message)
			    		       .setCancelable(false)
			    		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			    		           public void onClick(DialogInterface dialog, int id) {
			    		        	   u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/reboot");
			    		           }
			    		       })
			    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			    		           public void onClick(DialogInterface dialog, int id) {
			    		                dialog.cancel();
			    		           }
			    		       }).show();
			    	}
			    	if(items[item] == "Reboot Recovery"){
			    		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
			    		builder.setTitle(R.string.button_reboot)
			    			   .setMessage(R.string.reboot_message)
			    		       .setCancelable(false)
			    		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			    		           public void onClick(DialogInterface dialog, int id) {
			    		        		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_DEVICE, 0);
						       			board=settings.getString("device", "");
						       			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
						       				u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox cp /sdcard/BootManager/.zips/recovery_mode /data/.recovery_mode");
						       				u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/reboot");
						       			} else {
						       				u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/reboot recovery");
						       			}
			    		           }
			    		       })
			    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			    		           public void onClick(DialogInterface dialog, int id) {
			    		                dialog.cancel();
			    		           }
			    		       }).show();
			    	}
			    	if(items[item] == "Hot Reboot"){
			    		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
			    		builder.setTitle(R.string.button_reboot)
			    			   .setMessage(R.string.reboot_message)
			    		       .setCancelable(false)
			    		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			    		           public void onClick(DialogInterface dialog, int id) {
			    		        	   u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox killall system_server");
			    		           }
			    		       })
			    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			    		           public void onClick(DialogInterface dialog, int id) {
			    		                dialog.cancel();
			    		           }
			    		       }).show();
			    	}
			    }
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			}).show();
     	}
     	if(l.getItemAtPosition(position).toString().equals("VM Heap Size")){
     		//heap size
     		final CharSequence[] items = {"12", "16", "20", "24", "28", "32", "36", "40", "44", "48", "52"};
	    	CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
	    	builder.setTitle("Select a heap size");
	    	builder.setSingleChoiceItems(items, vmPrefs.getvmIndex(), new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int item) {
	    			vmPrefs.savevmIndex(item);
	    			dialog.cancel();
	    			u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox mount -o rw,remount /system");
	    			u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox sed -i \"s/dalvik.vm.heapsize=.*/dalvik.vm.heapsize=" + items[item] +"m"+ "/g\" /system/build.prop");
	    			u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox sed -i \'s/hw_dalvik.vm.heapsize=.*/hw_dalvik.vm.heapsize=" + items[item] +"m"+ "/g\' /system/build.prop");
	    			u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox mount -o ro,remount /system");
	    			restartdialog();
	    		}
	    	})
	    	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {
	    			dialog.cancel();
	    		}
	    	}).show();
     	}
     	if(l.getItemAtPosition(position).toString().equals("Wipe Dalvik Cache")){
     		//Wipe Dalvik Cache
     		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
     		builder.setTitle("Wipe Dalvik Cache")
     			   .setMessage(R.string.dalvikCache)
     		       .setCancelable(true)
     		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
     		           public void onClick(DialogInterface dialog, int id) {
     		        	   u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox rm -r /data/dalvik-cache/*");
     		        	   u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/reboot");
     		           }
     		       })
     		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
     		           public void onClick(DialogInterface dialog, int id) {
     		                dialog.cancel();
     		           }
     		       }).show();
     	}
     	if(l.getItemAtPosition(position).toString().equals("Zipalign Applications")){
     		//zipalign
			CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
			builder.setTitle("Zipalign")
			.setMessage(R.string.zipAlign)
			.setCancelable(true)
			.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					extraDialog = CustomProgressDialog.show(getActivity(), "Zipaligning Applications", "Please wait ...", true, true);
					superThread threadActivity = new superThread("ZipA");
					threadActivity.start();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			}).show();
     	}
    }
    
    @Override
	public void onPause() {
        super.onPause();
        File zip = new File(u.getExternalDirectory() + "/extraFinisHed");
    	if(zip.exists()){
    		zip.delete();
    	}
    }
    
	private class superThread extends Thread{
		String Commandone;
		private superThread(String command1){
			this.Commandone = command1;
		}
		@Override
		public void run() {
			try {
				if(Commandone.equals("fixperm")){
					u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/fix_permissions "+getActivity().getFilesDir().getAbsolutePath() + "/busybox");
					check();
				}
				if(Commandone.equals("ZipA")){
					u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/zipalign");
    				check();
				}
				if(Commandone.equals("trash")){
					if (new File("/sdcard/.Trash-1000").exists()){
						u.execCommand("rm -r /sdcard/.Trash-1000/expunged/*");
						u.execCommand("rm -r /sdcard/.Trash-1000/files/*");
						u.execCommand("rm -r /sdcard/.Trash-1000/info/*");
		        	}
		        	if(new File("/sdcard/LOST.DIR").exists()){
						u.execCommand("rm -r /sdcard/LOST.DIR/*");
		        	}
				}
			} finally {
				extraDialog.dismiss();
			}
		}
	}

	private void check(){
    	File zip = new File(u.getExternalDirectory() + "/extraFinisHed");
    	if(!zip.exists()){
    		u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox sleep 2s");
    		check();
    	}else{
    		zip.delete();
    	}
    }
	
	private void restartdialog(){
    	CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
    	builder.setMessage(R.string.success)
    	.setCancelable(true)
    	.setPositiveButton("Reboot", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/reboot");
    		}
    	})
    	.setNeutralButton("Hot Reboot", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			u.execCommand("killall system_server");
    		}
    	})
    	.setNegativeButton("No Reboot", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int id) {
    			dialog.cancel();
    		}
    	}).show();
    }
	
	private boolean systemMounter() {
		File mountFile = new File("/proc/mounts");
		StringBuilder procData = new StringBuilder();
		if(mountFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(mountFile.toString());
				DataInputStream dis = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				String data;
				while((data = br.readLine()) != null) {
					procData.append(data + "\n");				
				}
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
			if(procData.toString() != null) {
				String[] tmp = procData.toString().split("\n");
				for(int x = 0; x < tmp.length; x++) {
					if(tmp[x].contains("/dev/block") && tmp[x].contains("/system")) {
						if(tmp[x].contains("rw")) {
							CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
							builder.setTitle("System is rw")
								   .setMessage("Mount System ro?")
							       .setCancelable(true)
							       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox mount -o ro,remount /system");
							           }
							       })
							       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							                dialog.cancel();
							           }
							       }).show();
							return true;
						} else if(tmp[x].contains("ro")) {
							CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
							builder.setTitle("System is ro")
								   .setMessage("Mount System rw?")
							       .setCancelable(true)
							       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox mount -o rw,remount /system");
							           }
							       })
							       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							                dialog.cancel();
							           }
							       }).show();							
							return false;
						} else {
							return false;
						}
					}
				}
			}
		}
		return false;
	}
	
	private void SDwarning(){
		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
		builder.setTitle("SD Booster")
			   .setMessage(R.string.sdWarn)
		       .setCancelable(true)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   sdBoost();
		           }
		       })
		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       }).show();
	}
	 
	private void sdBoost(){
 	   final CharSequence[] items = {"128", "512", "1024", "2048", "3072", "4096", "5120", "6144", "7168", "8196"};
      	CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
      	//builder.setTitle(R.string.sdInfo);
      	builder.setSingleChoiceItems(items, sdBoostPrefs.getSdBoostIndex(), new DialogInterface.OnClickListener() {
      		public void onClick(DialogInterface dialog, int item) {
      			sdBoostPrefs.saveSdBoostIndex(item);
      			if(new File(u.getExternalDirectory() + "/BootManager/.zips/boost").exists()){
      				new File(u.getExternalDirectory() + "/BootManager/.zips/boost").delete();
      			}
      			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '" + items[item] + "' >> " + u.getExternalDirectory() + "/BootManager/.zips/boost");
      			u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox echo " + items[item] + " > /sys/devices/virtual/bdi/179:0/read_ahead_kb");
      			u.execCommand(getActivity().getFilesDir().getAbsolutePath() + "/busybox echo " + items[item] + " > /sys/devices/virtual/bdi/179:64/read_ahead_kb");
      			dialog.cancel();
      		}
      	})
      	.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      		public void onClick(DialogInterface dialog, int id) {
      			dialog.cancel();
      		}
      	}).show();
	}
}