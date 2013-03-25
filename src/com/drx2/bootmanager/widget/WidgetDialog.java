package com.drx2.bootmanager.widget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import com.drx2.bootmanager.Loader;
import com.drx2.bootmanager.utilities.Romswitcher;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.Utilities;

public class WidgetDialog extends Activity {
	String slot="";
	SharedPreferences shared;
	Context context;
	
	private static final String PREFS_DEVICE = "DeviceInfo";
	Utilities u = new Utilities();
	ShellCommand s = new ShellCommand();
	Loader l = new Loader();
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        shared = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences settings2 = context.getSharedPreferences(PREFS_DEVICE, 0);
		Boolean useemmc=settings2.getBoolean("useemmc", false);
        l.setupemmc(context, useemmc);
	    try{
			Scanner scan = new Scanner(new File(u.getExternalDirectory() + "/BootManager/.zips/slots"));
			slot = scan.nextLine();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}
		if(u.checkIfNumber(slot) == false){
			slot = shared.getString("slotNum", "4");
		}
		System.out.println(slot);
		int numberofslots = Integer.parseInt(slot)+1;
		System.out.println(numberofslots);
		ArrayList<CharSequence> slots = new ArrayList<CharSequence>();
		slots.add(getnames("phone"));
		for(int i = 1; i < numberofslots; i++){
			slots.add(getnames("rom"+i));
		}
		final CharSequence[] items = new CharSequence[slots.size()];
		slots.toArray(items);
		AlertDialog.Builder builder = new AlertDialog.Builder(WidgetDialog.this);
		builder.setTitle("What ROM would you like to boot?");
		builder.setOnCancelListener(new DialogInterface.OnCancelListener(){
			@Override
			public void onCancel(DialogInterface dialog) {
				WidgetDialog.this.finish();
			}
		});
		builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
		    public void onClick(DialogInterface dialog, int item) {
		    }
		    });
		    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					SparseBooleanArray CheCked =((AlertDialog)dialog).getListView().getCheckedItemPositions();
					if(CheCked.get(0)){
						ConfirmDialog(context, "phoneRom");
					} else {
						for(int i = 0; i < items.length; i++){
							if(CheCked.get(i)){
								ConfirmDialog(context, "rom"+i);
								break;
							}
						}
					}
				}
		    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					WidgetDialog.this.finish();
				}
		}).show();
    }
	
	private void ConfirmDialog(final Context context, final String slot){
		AlertDialog.Builder b = new AlertDialog.Builder(WidgetDialog.this);
		b.setTitle("Reboot?");
		b.setMessage("Are you sure you want to boot to "+getnames(slot)+"?");
		b.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					Intent i = new Intent(WidgetDialog.this, Romswitcher.class);
			    	i.putExtra("slot", slot);
			    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	startActivity(i);
				}
		    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					WidgetDialog.this.finish();
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