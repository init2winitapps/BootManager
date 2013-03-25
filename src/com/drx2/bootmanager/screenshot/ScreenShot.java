package com.drx2.bootmanager.screenshot;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.Utilities;



public class ScreenShot extends Activity{
	Utilities u = new Utilities();
	Context context;
	Handler ssHandler = new Handler();
	File input = new File(u.getExternalDirectory()+"/BootManager/screenshot.bmp");
	private String ssDir = u.getExternalDirectory()+"/DCIM/screens";

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.blank);
        context = getApplicationContext();
        File dcim = new File(u.getExternalDirectory()+"/DCIM");
        if(!dcim.exists())
        	dcim.mkdir();
        File screens = new File(dcim+"/screens");
        if(!screens.exists())
        	screens.mkdir();
        u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox chmod 777 /dev/graphics/fb0");
        takeScreenshot(3);
        finish();
        
    }
	
	void takeScreenshot(final int delay)
    {
        ssHandler.postDelayed(new Runnable()
        {
            public void run()
            {
                takescreenshot();
            }
        }, delay * 1000);
    }
	
    private void takescreenshot(){
    	
        if(input.exists())
        	input.delete();
        u.execCommand(context.getFilesDir().getAbsolutePath()+"/bootmanagerSS tryScreenshotClient");
        /*if(!input.exists()){
        	@SuppressWarnings("unused")
			int i = ssl.takeScreenShot("/dev/graphics/fb0");
        }
        */
        converttoPNG();
        Toast.makeText(ScreenShot.this, R.string.screenshot_saved, Toast.LENGTH_LONG).show();
        
    }
    
    private void converttoPNG() {
    	Calendar c = Calendar.getInstance();
    	int month = c.get(Calendar.MONTH) + 1;
    	int minute = c.get(Calendar.MINUTE);
    	String minutes = String.valueOf(minute);
    	    if(minutes.length() < 2){
    		    minutes = "0"+minutes;
    		  }
    	String sDate = month + "-" + c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.YEAR) + "-" + c.get(Calendar.HOUR_OF_DAY) + "." + minutes;
    	try {
			Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(input));
			FileOutputStream fout = new FileOutputStream(ssDir+"/"+sDate+".png");
            bmp.compress(CompressFormat.PNG, 100, fout);
            fout.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		input.delete();
		startMediaScanner(ssDir+"/"+sDate+".png");
    	
	}
    
    private void startMediaScanner(String addedPicture)
    {
         sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://"+ addedPicture)));
    }



	@Override
    public void onDestroy() {
    	super.onDestroy();
    }
	
    
}