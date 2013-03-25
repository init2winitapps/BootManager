package com.drx2.bootmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.PhoneRomSetup;
import com.drx2.bootmanager.utilities.PhotoPicker;
import com.drx2.bootmanager.utilities.Romswitcher;
import com.drx2.bootmanager.utilities.Utilities;

public class PhoneROM extends Fragment {
	protected static final int PHOTO_PICKED = 0;
	private Button boot;
	private Button recovery;
	private Button editROM;
	private Button edit;
	String slot = "phoneRom";
	String name1;
	String kernelName;
	String desc;
	String board;
	Utilities u = new Utilities();
	Context context;
	View V;
	PhotoPicker p = new PhotoPicker();
	private static final String PREFS_DEVICE = "DeviceInfo";
	MainActivity ma = new MainActivity();
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	V = inflater.inflate(R.layout.phone_rom, null, false);
    	context = getActivity().getApplicationContext();
        getnames();
        if(MainActivity.SDmount.equals("mounted")){
        	if(new File(u.getExternalDirectory()+"/BootManager/"+slot+"/screenshot.png").exists()){
        		ImageView imgV = (ImageView) V.findViewById(R.id.imageView1);
        		//imgV.setImageURI(p.getTempUri(slot));
        		imgV.setImageBitmap(p.getTempBitmap(slot));
        	}else{
            	ImageView imgV = (ImageView) V.findViewById(R.id.imageView1);
            	imgV.setImageResource(R.drawable.screen_shot);
        	}
        }else{
        	ImageView imgV = (ImageView) V.findViewById(R.id.imageView1);
        	imgV.setImageResource(R.drawable.screen_shot);
        	Toast.makeText(getActivity(), R.string.sdNotMounted, Toast.LENGTH_LONG).show();
        }
        TextView rom = new TextView(getActivity()); 
        rom=(TextView)V.findViewById(R.id.romName);
        rom.setText(getActivity().getString(R.string.romName));
        rom.setTypeface(null, Typeface.BOLD);
        rom.setTextSize(20);
        TextView romText = new TextView(getActivity());
        romText=(TextView)V.findViewById(R.id.romText);
        romText.setText(name1);
        romText.setTextSize(20);
        
        TextView kernel = new TextView(getActivity());
        kernel=(TextView)V.findViewById(R.id.kernelName);
        kernel.setTypeface(null, Typeface.BOLD);
        kernel.setTextSize(20);
        kernel.setText(getActivity().getString(R.string.kernelName));
        TextView kernelText = new TextView(getActivity());
        kernelText=(TextView)V.findViewById(R.id.kernelText);
        kernelText.setText(kernelName);
        kernelText.setTextSize(20);
        
        TextView description = new TextView(getActivity()); 
        description=(TextView)V.findViewById(R.id.description);
        description.setText(getActivity().getString(R.string.romDescription));
        description.setTypeface(null, Typeface.BOLD);
        description.setTextSize(20);
        TextView descText = new TextView(getActivity());
        descText=(TextView)V.findViewById(R.id.descriptionText);
        descText.setText(desc);
        descText.setTextSize(20);
        
    	boot = (Button)V.findViewById(R.id.bootButton);
    	recovery = (Button)V.findViewById(R.id.recoveryButton);
    	editROM = (Button)V.findViewById(R.id.editROMButton);
    	edit = (Button)V.findViewById(R.id.editButton);
    	boot.setBackgroundDrawable(MainActivity.buttonState(context));
    	recovery.setBackgroundDrawable(MainActivity.buttonState(context));
    	editROM.setBackgroundDrawable(MainActivity.buttonState(context));
    	edit.setBackgroundDrawable(MainActivity.buttonState(context));
    	SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
    	boot.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	recovery.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	editROM.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	edit.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	
    	boot.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
		        if(MainActivity.SDmount.equals("mounted")){
					CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					builder.setTitle("Boot Phone ROM?")
						   .setMessage(getActivity().getString(R.string.bootROM) + " " + name1 + "?")
					       .setCancelable(true)
					       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					        	   Intent i = new Intent(getActivity(), Romswitcher.class);
					        	   i.putExtra("slot", slot);
					        	   startActivity(i);
					           }
					       })
					       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					                dialog.cancel();
					           }
					       }).show();
		        }else{
		        	Toast.makeText(getActivity(), R.string.sdNotMounted, Toast.LENGTH_LONG).show();
		        }
			}
    	});
    	
    	recovery.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				//CallLogBackupRestore crb = new CallLogBackupRestore();
				//crb.getSMS(context);
				//crb.restoreSMS(context);
				CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
				builder.setTitle("Reboot Recovery")
					   .setMessage(R.string.rebootRecovery)
				       .setCancelable(true)
				       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   	SharedPreferences settings = getActivity().getSharedPreferences(PREFS_DEVICE, 0);
				       			board=settings.getString("device", "");
				       			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
				       				if(new File("/system/xbin/cat.jpg").exists()){
				       					u.execCommand("/system/xbin/cat.jpg");
				       				}
				       				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox cp /sdcard/BootManager/.zips/recovery_mode /data/.recovery_mode");
				       				u.execCommand(context.getFilesDir().getAbsolutePath() + "/reboot");
				       			} else {
				       				u.execCommand(context.getFilesDir().getAbsolutePath() + "/reboot recovery");
				       			}
				           	}
				       })
				       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       }).show();
			}
    	});
    	
    	editROM.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				SharedPreferences settings = getActivity().getSharedPreferences(PREFS_DEVICE, 0);
		    	String boot=settings.getString("boot", null);
		    	String board=settings.getString("device", "");
		    	PhoneRomSetup prs = new PhoneRomSetup();
		    	prs.setupPR(board, getActivity(), boot);
			}
    	});
    	
    	edit.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				if(MainActivity.SDmount.equals("mounted")){
					final CharSequence[] items = {getActivity().getString(R.string.screen_shot), getActivity().getString(R.string.rom_name), getActivity().getString(R.string.kernel_name), getActivity().getString(R.string.description)};
					CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					builder.setTitle("User Interface");
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int whichButton) {
		    				dialog.cancel();
		    			}
					});
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
					    	if(item == 0){
					        	Intent i = new Intent(getActivity(), PhotoPicker.class);
					        	i.putExtra("rom", slot);
					        	startActivityForResult(i, 0);
					        }else if(item == 1){
					        	final CustomDialog.Builder alert = new CustomDialog.Builder(getActivity());
					    		final EditText input = new EditText(getActivity());
					    		input.setText(name1);
					    		input.setSelection(input.getText().length());
					    		alert.setView(input, true);
					    		alert.setTitle("Enter ROM Name");
					    		alert.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    			public void onClick(DialogInterface dialog, int whichButton) {
					    				String value = input.getText().toString().trim();
					    				TextView description = new TextView(getActivity()); 
					    		        description=(TextView)V.findViewById(R.id.romText);
					    		        description.setText(value);
					    		        u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '" + value + "' > " + u.getExternalDirectory() + "/BootManager/phoneRom/name");
					    		        getnames();
					    			}
					    		});
					    		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					    			public void onClick(DialogInterface dialog, int whichButton) {
					    				dialog.cancel();
					    			}
					    		}).show();
					        }else if(item == 2){
					        	final CustomDialog.Builder alert = new CustomDialog.Builder(getActivity());
					    		final EditText input = new EditText(getActivity());
					    		input.setText(kernelName);
					    		input.setSelection(input.getText().length());
					    		alert.setView(input, true);
					    		alert.setTitle("Enter Kernel Name");
					    		alert.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    			public void onClick(DialogInterface dialog, int whichButton) {
					    				String value = input.getText().toString().trim();
					    				TextView description = new TextView(getActivity()); 
					    		        description=(TextView)V.findViewById(R.id.kernelText);
					    		        description.setText(value);
					    		        u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '" + value + "' > " + u.getExternalDirectory() + "/BootManager/phoneRom/kernel");
					    		        getnames();
					    			}
					    		});
					    		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					    			public void onClick(DialogInterface dialog, int whichButton) {
					    				dialog.cancel();
					    			}
					    		}).show();
					        }else if(item == 3){
					    		final CustomDialog.Builder alert = new CustomDialog.Builder(getActivity());
					    		final EditText input = new EditText(getActivity());
					    		input.setText(desc);
					    		input.setSelection(input.getText().length());
					    		alert.setView(input, true);
					    		alert.setTitle("Enter ROM Description");
					    		alert.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    			public void onClick(DialogInterface dialog, int whichButton) {
					    				String value = input.getText().toString().trim();
					    				TextView description = new TextView(getActivity()); 
					    		        description=(TextView)V.findViewById(R.id.descriptionText);
					    		        description.setText(value);
					    		        u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo '" + value + "' > " + u.getExternalDirectory() + "/BootManager/phoneRom/description");
					    		        getnames();
					    			}
					    		});
					    		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					    			public void onClick(DialogInterface dialog, int whichButton) {
					    				dialog.cancel();
					    			}
					    		}).show();
					        }
					    }
					}).show();
				}else{
					Toast.makeText(getActivity(), R.string.sdNotMounted, Toast.LENGTH_LONG).show();
				}
			}
    	});
    	return V;
    }
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	switch (requestCode) {
		case 0:
			if (resultCode == 0) {
				ImageView imgV = (ImageView) V.findViewById(R.id.imageView1);
	        	Bitmap bmp = p.getTempBitmap(slot);
	        	if(bmp!=null)
	        		imgV.setImageBitmap(bmp);
			}
		}
	}
	
	private void getnames() {
		name1=readRomName(u.getExternalDirectory() + "/BootManager/phoneRom/name");
		if(!(name1 !=null )){
			name1="Phone ROM";
		}
		kernelName=readRomName(u.getExternalDirectory() + "/BootManager/phoneRom/kernel");
		if(!(kernelName !=null )){
			kernelName="Kernel not set";
		}
		desc=readRomName(u.getExternalDirectory() + "/BootManager/phoneRom/description");
		if(!(desc !=null )){
			desc="Phone ROM";
		}
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