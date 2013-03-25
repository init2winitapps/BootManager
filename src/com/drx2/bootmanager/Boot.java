package com.drx2.bootmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.drx2.bootmanager.services.BootService;
import com.drx2.bootmanager.utilities.Utilities;

public class Boot extends BroadcastReceiver {
	MainActivity ri = new MainActivity();
	private Preferences sdBoostPrefs;
	private static final String PREFS_DEVICE = "DeviceInfo"; 
	Utilities u = new Utilities();
	
	@Override
	public void onReceive(Context context, Intent intent) {
		sdBoostPrefs = new Preferences(context);
		ri.cleanUp(context);
		final CharSequence[] items = {"128", "512", "1024", "2048", "3072", "4096", "5120", "6144", "7168", "8196"};
    	int item = sdBoostPrefs.getSdBoostIndex();
    	SharedPreferences settings = context.getSharedPreferences(PREFS_DEVICE, 0);
    	if(settings.contains("sdboostIndex")){
       		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo " + items[item] + " > /sys/devices/virtual/bdi/179:0/read_ahead_kb");
    		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo " + items[item] + " > /sys/devices/virtual/bdi/179:64/read_ahead_kb");
    	}else if(new File(u.getExternalDirectory() + "/BootManager/.zips/boost").exists()){
    		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo " + readBoost(u.getExternalDirectory() + "/BootManager/.zips/boost") + " > /sys/devices/virtual/bdi/179:0/read_ahead_kb");
    		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo " + readBoost(u.getExternalDirectory() + "/BootManager/.zips/boost") + " > /sys/devices/virtual/bdi/179:64/read_ahead_kb");
    	}
    	//Running the restore of call log/ sms from a service so we don't anr here
    	//Might be worth moving some of the other stuff to the service
    	//Not sure if this is getting called on a fresh install
    	//u.log("Boot receiver called");
    	Intent i = new Intent(context, BootService.class);
    	context.startService(i);
    	
	}
	
	private static String readBoost(String fname) {
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
	
	
}