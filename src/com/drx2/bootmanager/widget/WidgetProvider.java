package com.drx2.bootmanager.widget;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.Utilities;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;

public class WidgetProvider extends AppWidgetProvider {
	ShellCommand s = new ShellCommand();
	Utilities u = new Utilities();
	private static final String PREFS_DEVICE = "DeviceInfo";
	String romnumber;
	
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	String rom = currentROM(context);
    	
    	if(rom!=null){
    		if(rom.contains("rom")){
    			romnumber = rom.substring(rom.length()-1);
    		} else {
    			romnumber = "P";
    		}
    	} else {
    		romnumber="P";
    	}
    	
    	final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent = new Intent(context, com.drx2.bootmanager.widget.WidgetDialog.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget2);
            views.setTextViewText(R.id.widget, romnumber);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
    
    private String currentROM(Context context) {
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
								//Page = currentromname;
							} else {
								currentromname = "phone";
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
							currentromname="phone";
						} else {
							currentromname=newcurrentrom;
						}
					} else {
						currentromname="phone";
					}
			    } else {
			    	currentromname="phone";
				}
			} else {
		    	currentromname="phone";
			}
	    }
		return currentromname;
	}
    
}