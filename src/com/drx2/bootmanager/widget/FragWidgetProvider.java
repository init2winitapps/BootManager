package com.drx2.bootmanager.widget;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.PhotoPicker;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.Utilities;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;

public class FragWidgetProvider extends AppWidgetProvider {
	ShellCommand s = new ShellCommand();
	Utilities u = new Utilities();
	private static final String PREFS_DEVICE = "DeviceInfo";
	String romnumber;
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	int current = currentROM(context);
    	
    	final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent rightintent = new Intent(context, com.drx2.bootmanager.widget.FragWidgetRight.class);
            rightintent.putExtra("slot", current);
            PendingIntent rpendingIntent = PendingIntent.getActivity(context, 0, rightintent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent leftintent = new Intent(context, com.drx2.bootmanager.widget.FragWidgetLeft.class);
            leftintent.putExtra("slot", current);
            PendingIntent lpendingIntent = PendingIntent.getActivity(context, 0, leftintent, PendingIntent.FLAG_UPDATE_CURRENT);
            Intent bootIntent = new Intent(context, com.drx2.bootmanager.widget.FragWidgetActivity.class);
            bootIntent.putExtra("slot", current);
            PendingIntent bootpendingIntent = PendingIntent.getActivity(context, 0, bootIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widgetfrag);
            Bitmap pic = getBitmap(current);
            if(pic!=null){
            	views.setImageViewBitmap(R.id.previewImage, pic);
            }else {
            	views.setImageViewResource(R.id.previewImage, R.drawable.screen_shot);
            }
            String rom;
    		if(current!=0){
    			rom = getnames("rom"+current);
    		} else {
    			rom = getnames("phoneRom");
    		}
            views.setTextViewText(R.id.romNameWidget, rom);
            views.setOnClickPendingIntent(R.id.rightbutton, rpendingIntent);
            views.setOnClickPendingIntent(R.id.leftbutton, lpendingIntent);
            views.setOnClickPendingIntent(R.id.previewImage, bootpendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    
    private int currentROM(Context context) {
    	String currentromname = null;
    	SharedPreferences settings = context.getSharedPreferences(PREFS_DEVICE, 0);
		String board=settings.getString("device", "");
	    if(board.equals("tuna")){
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
								currentromname = number;
							} else {
								currentromname = "0";
							}
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
			    if(currentrom.stdout.contains("rom")){
			    	int start = currentrom.stdout.indexOf("rom");
				    int end = start+4;
				    String newcurrentrom = currentrom.stdout.substring(start, end);
				    if(newcurrentrom!=null){
						if(newcurrentrom.contains("Rom/")){
							currentromname="0";
						} else {
							String number = newcurrentrom.substring(newcurrentrom.length()-1);
							if(u.checkIfNumber(number)){
								currentromname = number;
							} else {
								currentromname = "0";
							}
						}
					} else {
						currentromname="0";
					}
			    } else {
			    	currentromname="0";
				}
			} else {
		    	currentromname="0";
			}
	    }
		return Integer.parseInt(currentromname);
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
		Utilities u = new Utilities();
		File rompic = new File(u.getExternalDirectory()+"/BootManager/"+rom+"/screenshot.png");
		if(rompic.exists()){
			b = p.getTempBitmap(rom);
		}
		return b;
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