package com.drx2.bootmanager.widget;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.PhotoPicker;
import com.drx2.bootmanager.utilities.Utilities;

public class FragWidgetRight extends Activity {
	Context context;
	int slot;
	Utilities u;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        Bundle extras = getIntent().getExtras();
        slot = extras.getInt("slot");
        u = new Utilities();
        //TODO account for running out of slots to go to
        int showing;
        if(slot < getMaxSlots()){//Get number of slots and make sure we don't go over here
        	showing = slot+1;//Subtract for left
        } else {
        	showing = slot;
        }
        //Toast.makeText(context, "right "+slot, Toast.LENGTH_LONG).show();
        ComponentName thisWidget = new ComponentName(FragWidgetRight.this, FragWidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(FragWidgetRight.this);
        Intent rightintent = new Intent(context, com.drx2.bootmanager.widget.FragWidgetRight.class);
        rightintent.putExtra("slot", showing);
        PendingIntent rpendingIntent = PendingIntent.getActivity(context, 0, rightintent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent leftintent = new Intent(context, com.drx2.bootmanager.widget.FragWidgetLeft.class);
        leftintent.putExtra("slot", showing);
        PendingIntent lpendingIntent = PendingIntent.getActivity(context, 0, leftintent, PendingIntent.FLAG_UPDATE_CURRENT);
        Intent bootIntent = new Intent(context, com.drx2.bootmanager.widget.FragWidgetActivity.class);
        bootIntent.putExtra("slot", showing);
        PendingIntent bootpendingIntent = PendingIntent.getActivity(context, 0, bootIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widgetfrag);
        Bitmap pic = getBitmap(showing);
        if(pic!=null){
        	views.setImageViewBitmap(R.id.previewImage, pic);
        }else {
        	views.setImageViewResource(R.id.previewImage, R.drawable.screen_shot);
        }
        String rom;
        if(showing!=0){
			rom = getnames("rom"+showing);
		} else {
			rom = getnames("phoneRom");
		}
        views.setTextViewText(R.id.romNameWidget, rom);
        views.setOnClickPendingIntent(R.id.rightbutton, rpendingIntent);
        views.setOnClickPendingIntent(R.id.leftbutton, lpendingIntent);
        views.setOnClickPendingIntent(R.id.previewImage, bootpendingIntent);
        manager.updateAppWidget(thisWidget, views);
        this.finish();
        
	}
	
	private Bitmap getBitmap(int showing){
		Bitmap b = null;
		String rom;
		if(showing!=0){
			rom = "rom"+showing;
		} else {
			rom = "phoneRom";
		}
		PhotoPicker p = new PhotoPicker();
		File rompic = new File(u.getExternalDirectory()+"/BootManager/"+rom+"/screenshot.png");
		if(rompic.exists()){
			b = p.getTempBitmap(rom);
		}
		return b;
	}
	
	private int getMaxSlots(){
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		String slot = "";
		try{
			Scanner scan = new Scanner(new File(u.getExternalDirectory() + "/BootManager/.zips/slots"));
			if(scan.hasNextLine())slot = scan.nextLine();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}
		if(u.checkIfNumber(slot) == false){
			slot = shared.getString("slotNum", "4");
		}
		return Integer.parseInt(slot);
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
	
}
