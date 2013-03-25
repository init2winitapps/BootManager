package com.drx2.bootmanager.widget;

import java.io.BufferedReader;
import java.io.FileReader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.drx2.bootmanager.Loader;
import com.drx2.bootmanager.utilities.Romswitcher;
import com.drx2.bootmanager.utilities.Utilities;

public class FragWidgetActivity extends Activity {
	Context context;
	int slot;
	SharedPreferences shared;
	private static final String PREFS_DEVICE = "DeviceInfo";
	Utilities u = new Utilities();
	Loader l = new Loader();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        context = getApplicationContext();
        shared = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences settings2 = context.getSharedPreferences(PREFS_DEVICE, 0);
		Boolean useemmc=settings2.getBoolean("useemmc", false);
        l.setupemmc(context, useemmc);
        slot = extras.getInt("slot");
        String rom;
        if(slot!=0){
			rom = "rom"+slot;
		} else {
			rom = "phoneRom";
		}
        ConfirmDialog(context, rom);
        
        
        
	}
	
	private void ConfirmDialog(final Context context, final String slot){
		AlertDialog.Builder b = new AlertDialog.Builder(FragWidgetActivity.this);
		b.setTitle("Reboot?");
		b.setMessage("Are you sure you want to boot to "+getnames(slot)+"?");
		b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent i = new Intent(FragWidgetActivity.this, Romswitcher.class);
			    	i.putExtra("slot", slot);
			    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	startActivity(i);
				}
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					FragWidgetActivity.this.finish();
				}
		}).show();
	}
	
	
	private String getnames(String slot) {
		String name;
		if(slot.contains("phone")){
			name = readRomName(u.getExternalDirectory() + "/BootManager/phoneRom/name");
		} else {
			name = readRomName(u.getExternalDirectory() + "/BootManager/"+slot+"/name");
		}
		if(!(name !=null )){
			if(slot.contains("phone")){
				name="Phone Rom";
			} else {
				name=slot.toUpperCase();
			}
		}
		return name;
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
	
}
