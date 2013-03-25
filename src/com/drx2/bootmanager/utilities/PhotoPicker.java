package com.drx2.bootmanager.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.drx2.bootmanager.R;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

public class PhotoPicker extends Activity{
	Utilities u = new Utilities();
	@SuppressWarnings("unused")
	private static final String TEMP_PHOTO_FILE = "screenshot.png";
	private final static String TAG = "BootManager";
	String rom;
	Context context;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent whofrom = getIntent();
        Bundle extras = whofrom.getExtras();
        rom = extras.getString("rom");
        setContentView(R.layout.blank);
        context = getApplicationContext();
        try {
        	u.log("getting pic for "+ rom);
            // Launch picker to choose photo for selected contact
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            //intent.putExtra("return-data", true);
            startActivityForResult(intent, 0);
    	} catch (ActivityNotFoundException e) {
            //Toast.makeText(getActivity(), "No PhotoPicker found", Toast.LENGTH_LONG).show();
        }
	}

	/*public Uri getTempUri(String rom) {
		return Uri.parse(getTempFile(rom));
	}*/
	
	/*public String getTempFile(String rom) {
		if (isSDCARDMounted()) {
			
			String f = u.getExternalDirectory()+"/BootManager/"+rom+"/screenshot.png";
			
			@SuppressWarnings("unused")
			Bitmap b = decodeFile(new File(u.getExternalDirectory()+"/BootManager/"+rom+"/screenshot.png"));
			return f;
		} else {
			return null;
		}	
	}*/
	
	public Bitmap getTempBitmap(String rom) {
		if (isSDCARDMounted()) {
			Bitmap b = decodeFile(new File(u.getExternalDirectory()+"/BootManager/"+rom+"/screenshot.png"));
			return b;
		} else {
			return null;
		}	
	}
	
	private boolean isSDCARDMounted(){
        String status = Environment.getExternalStorageState();
       
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch (requestCode) {
		case 0:
			u.log("case 0");
			if (resultCode == RESULT_OK) {
				u.log("result_ok");
					if (data == null) {
						u.log("data is null");
						Log.w(TAG, "Null data, but RESULT_OK, from image picker!");
		                Toast t = Toast.makeText(this, "no photo picked",
		                                         Toast.LENGTH_SHORT);
		                t.show();
		                return;
					}
					if(data!=null){
						Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
						String selectedImagePath = null;
						if(cursor != null){
							Uri selectedImageUri = data.getData();
							if (selectedImageUri!=null)
								selectedImagePath = getPath(selectedImageUri);
						} else {
							//MIUI sucks so we need a different method...more reason to hate MIUI
							selectedImagePath = data.getData().getPath();
						}
						if(selectedImagePath!=null);{
							u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox rm "+u.getExternalDirectory()+"/BootManager/"+rom+"/screenshot.png");
							u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox cp '"+selectedImagePath+"' "+u.getExternalDirectory()+"/BootManager/"+rom+"/screenshot.png");
							}	
						}
					}
				}
    		setResult(0);
    		finish();
		}
					 	 
 	    public String getPath(Uri uri) {
 	        String[] projection = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(uri, projection, null, null, null);
 	        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 	        cursor.moveToFirst();
 	        return cursor.getString(column_index);
 	    }
					 	    
 	   private Bitmap decodeFile(File f){
 		    Bitmap b = null;
 		    int IMAGE_MAX_SIZE = 480;
 		    try {
 		        //Decode image size
 		        BitmapFactory.Options o = new BitmapFactory.Options();
 		        o.inJustDecodeBounds = true;
 		        FileInputStream fis = new FileInputStream(f);
 		        BitmapFactory.decodeStream(fis, null, o);
 		        fis.close();

 		        int scale = 2;
 		        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
 		            scale = (int)Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
 		        }
 		        //Decode with inSampleSize
 		        BitmapFactory.Options o2 = new BitmapFactory.Options();
 		        o2.inSampleSize = scale;
 		        fis = new FileInputStream(f);
 		        b = BitmapFactory.decodeStream(fis, null, o2);
 		        fis.close();
 		    } catch (IOException e) {
 		    }
 		    return b;
 		}


}
