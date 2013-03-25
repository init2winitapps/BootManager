package com.drx2.bootmanager.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;

public class WideWidgetProvider extends AppWidgetProvider {
	ShellCommand s = new ShellCommand();
	int widgetlayout;
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	CommandResult currentrom = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox losetup /dev/block/loop0");
    	if(currentrom.stdout!=null){
    		if(currentrom.stdout.contains("rom1")){
    			widgetlayout=R.layout.widewidget1;
    		} else if(currentrom.stdout.contains("rom2")){
    			widgetlayout=R.layout.widewidget2;//Use widget for rom2
    		} else if(currentrom.stdout.contains("rom3")){
    			widgetlayout=R.layout.widewidget3;//Use widget for rom3
    		} else if(currentrom.stdout.contains("rom4")){
    			widgetlayout=R.layout.widewidget4;//Use widget for rom4
    		} else {
    			widgetlayout=R.layout.widewidget5;//Use widget for phonerom
    		}
    		} else {
    			widgetlayout=R.layout.widewidget5;//Use widget for phonerom
    		}
    	
    	final int N = appWidgetIds.length;

        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            Intent intent1 = new Intent(context, com.drx2.bootmanager.widget.WideWidgetDialog.class);
            intent1.setAction("rom1");
            intent1.putExtra("rom", "rom1");
            PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 0, intent1, 0);
            RemoteViews views1 = new RemoteViews(context.getPackageName(), widgetlayout);
            views1.setOnClickPendingIntent(R.id.widewidget1, pendingIntent1);

            Intent intent2 = new Intent(context, com.drx2.bootmanager.widget.WideWidgetDialog.class);
            intent2.setAction("rom2");
            intent2.putExtra("rom", "rom2");
            PendingIntent pendingIntent2 = PendingIntent.getActivity(context, 0, intent2, 0);
            //RemoteViews views2 = new RemoteViews(context.getPackageName(), widgetlayout);
            views1.setOnClickPendingIntent(R.id.widewidget2, pendingIntent2);
            
            Intent intent3 = new Intent(context, com.drx2.bootmanager.widget.WideWidgetDialog.class);
            intent3.setAction("rom3");
            intent3.putExtra("rom", "rom3");
            PendingIntent pendingIntent3 = PendingIntent.getActivity(context, 0, intent3, 0);
            //RemoteViews views3 = new RemoteViews(context.getPackageName(), widgetlayout);
            views1.setOnClickPendingIntent(R.id.widewidget3, pendingIntent3);
            
            Intent intent4 = new Intent(context, com.drx2.bootmanager.widget.WideWidgetDialog.class);
            intent4.setAction("rom4");
            intent4.putExtra("rom", "rom4");
            PendingIntent pendingIntent4 = PendingIntent.getActivity(context, 0, intent4, 0);
            //RemoteViews views4 = new RemoteViews(context.getPackageName(), widgetlayout);
            views1.setOnClickPendingIntent(R.id.widewidget4, pendingIntent4);
            
            Intent intent5 = new Intent(context, com.drx2.bootmanager.widget.WideWidgetDialog.class);
            intent5.setAction("phonerom");
            intent5.putExtra("rom", "phoneRom");
            PendingIntent pendingIntent5 = PendingIntent.getActivity(context, 0, intent5, 0);
            //RemoteViews views5 = new RemoteViews(context.getPackageName(), widgetlayout);
            views1.setOnClickPendingIntent(R.id.widewidget5, pendingIntent5);
            
            appWidgetManager.updateAppWidget(appWidgetId, views1);
            //appWidgetManager.updateAppWidget(appWidgetId, views2);
            //appWidgetManager.updateAppWidget(appWidgetId, views3);
            //appWidgetManager.updateAppWidget(appWidgetId, views4);
            //appWidgetManager.updateAppWidget(appWidgetId, views5);
        }
    }
}