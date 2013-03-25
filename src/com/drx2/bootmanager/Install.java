package com.drx2.bootmanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.drx2.bootmanager.services.BackupRestoreService;
import com.drx2.bootmanager.services.BootManagerService;
import com.drx2.bootmanager.services.NandRestoreService;
import com.drx2.bootmanager.utilities.CustomDialog;

public class Install extends Activity {
	private Button stopInstall;
	private static final String PREFS_DEVICE = "DeviceInfo";
	Intent i;
	Context context;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.install);
        context = getApplicationContext();
        SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
        stopInstall = (Button)findViewById(R.id.stopButton);
        stopInstall.setBackgroundDrawable(MainActivity.buttonState(context));
        stopInstall.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
        stopInstall.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				CustomDialog.Builder builder = new CustomDialog.Builder(Install.this);
				builder.setTitle("Stop Install?")
					   .setMessage(R.string.cancelInstall)
				       .setCancelable(false)
				       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   SharedPreferences serv = getSharedPreferences(PREFS_DEVICE, 0);
				        	   SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
				        	   if (serv.getString("Service", "").equals("Live")) {
				        	   		Intent service = new Intent(Install.this, BootManagerService.class);
				        	   		stopService(service);
				        	   }
				        	   if (settings.getString("NandService", "").equals("Live")) {
				        		    Intent service = new Intent(Install.this, NandRestoreService.class);
				        	   		stopService(service);
				        	   }
				        	   if (settings.getString("BackupRestoreService", "").equals("Live")) {
				        		    Intent service = new Intent(Install.this, BackupRestoreService.class);
				        	   		stopService(service);
				        	   }
				        	   i = new Intent(Install.this, com.drx2.bootmanager.MainActivity.class);
								startActivity(i);
				           }
				       })
				       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       }).show();

				}
        });
    }
}