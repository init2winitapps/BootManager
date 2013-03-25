package com.drx2.bootmanager.utilities;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.drx2.bootmanager.MainActivity;
import com.drx2.bootmanager.R;

public class SdcardOff extends Activity {
	
	private Button SDcardoff;
	Context context;
	Utilities u = new Utilities();
	private String PREFS_DEVICE = "DeviceInfo";
		
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.sdcardoff);
	    context=getApplicationContext();
	    SDcardoff = (Button)findViewById(R.id.sdcardoffButton);
	    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(SdcardOff.this);
	    SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
	    String isItOn = settings.getString("USBstate", "");
	    if(pref.getBoolean("automountPref", false) || isItOn.contains("ON")) {
        	SDcardoff.setText(R.string.unmount);
        	//SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
        	SharedPreferences.Editor editor = settings.edit();
        	editor.putString("USBstate", "ON");
        	editor.commit();
        }else {
        	SDcardoff.setText(R.string.mount);
        	//SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
    		SharedPreferences.Editor editor = settings.edit();
    		editor.putString("USBstate", "OFF");
    		editor.commit();
        }
        
	    SDcardoff.setBackgroundDrawable(MainActivity.buttonState(context));
	    SDcardoff.setTextColor(settings.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
        SDcardoff.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
			    String sdcardblock = new String(settings.getString("sdcard", ""));
			    String device = new String(settings.getString("device", ""));
	    		String emmc;
	    		if(device.contains("vigor"))
	    			emmc = new String(settings.getString("ext_sd", ""));
	    		else 
	    			emmc = new String(settings.getString("emmc", ""));
				ImageView sdIMG = (ImageView)findViewById(R.id.sdcardIMG);
				if (settings.getString("USBstate", "").equals("ON")) {
					u.execCommand("echo > "+ settings.getString("lunfile", "/sys/devices/platform/usb_mass_storage/lun0/file"));
					u.execCommand("echo > "+ settings.getString("lunfile1", "/sys/devices/platform/usb_mass_storage/lun1/file"));
					SDcardoff.setText(R.string.mount);
			    	sdIMG.setImageResource(R.drawable.sd_logo);
		        	SharedPreferences.Editor editor = settings.edit();
		        	editor.putString("USBstate", "OFF");
		        	editor.commit();
				}else{
					u.execCommand("echo " + sdcardblock + " > "+ settings.getString("lunfile", "/sys/devices/platform/usb_mass_storage/lun0/file"));
					if(device.equalsIgnoreCase("inc")||device.equalsIgnoreCase("vigor")){
	        			u.execCommand("echo " + emmc + " > "+ settings.getString("lunfile1", "/sys/devices/platform/usb_mass_storage/lun1/file"));
	        		}
					SDcardoff.setText(R.string.unmount);
					sdIMG.setImageResource(R.drawable.sd_logo_on);
		        	SharedPreferences.Editor editor = settings.edit();
		        	editor.putString("USBstate", "ON");
		        	editor.commit();
				}
			}
        });
	}
}