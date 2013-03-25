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

import com.drx2.bootmanager.services.BackupRestoreService;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.PhoneRomSetup;
import com.drx2.bootmanager.utilities.PhotoPicker;
import com.drx2.bootmanager.utilities.Romswitcher;
import com.drx2.bootmanager.utilities.Utilities;

public class fragment1 extends Fragment{

	Context context;
	Utilities u = new Utilities();
	LayoutInflater InflateMe;
	static String slot = null;
	protected static final int PHOTO_PICKED = 0;
	private static final String PREFS_DEVICE = "DeviceInfo";
	private Button boot;
	private Button install;
	private Button editROM;
	private Button edit;
	static String name2;
	static String kernelName;
	static String desc;
	View V;
	static PhotoPicker p = new PhotoPicker();
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	V = inflater.inflate(R.layout.rom1, null, false);
    	context = getActivity().getApplicationContext();
    	slot = "rom" + String.valueOf(MainActivity.f1);
        getnames();
        PhotoPicker p = new PhotoPicker();
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
        romText.setText(name2);
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
    	install = (Button)V.findViewById(R.id.installButton);
    	editROM = (Button)V.findViewById(R.id.editROMButton);
    	edit = (Button)V.findViewById(R.id.editButton);
    	boot.setBackgroundDrawable(MainActivity.buttonState(context));
    	install.setBackgroundDrawable(MainActivity.buttonState(context));
    	editROM.setBackgroundDrawable(MainActivity.buttonState(context));
    	edit.setBackgroundDrawable(MainActivity.buttonState(context));
    	SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
    	boot.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	install.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	editROM.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	edit.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
    	
        boot.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				if(MainActivity.SDmount.equals("mounted")){
					CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					builder.setTitle("Boot ROM " + MainActivity.f1 + "?")
					   	   .setMessage(getActivity().getString(R.string.bootROM) + " " + name2 + "?")
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
    	
    	install.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				if(MainActivity.SDmount.equals("mounted")){
					final CharSequence[] items = {getActivity().getString(R.string.install_zip), getActivity().getString(R.string.restore_nandroid)};
					CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					builder.setTitle(R.string.install_zip);
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
					    	if(item == 0){
					    		Intent i = new Intent(getActivity(), Installed.class);
					    		i.putExtra("slot", slot);
					    		i.putExtra("installtype", "rom");
					    		startActivity(i);
					    		getActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.no_anim);
					    	}
					    	/**if(item == 1){
					    		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_DEVICE, 0);
						    	String board=settings.getString("device", "");
					    		if(board.contains("shadow")||board.contains("droid2")||board.contains("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
					    			Toast.makeText(getActivity(), R.string.kernels_not_supported, Toast.LENGTH_LONG).show();
					    		} else {
					    			Intent i = new Intent(getActivity(), Installed.class);
					    			i.putExtra("slot", slot);
					    			i.putExtra("installtype", "kernel");
					    			startActivity(i);
					    			getActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.no_anim);
					    		}
					    	}*/
					    	if(item == 1){
					    		Intent i = new Intent(getActivity(), NandPicker.class);
					    		i.putExtra("slot", slot);
					    		startActivity(i);
					    		getActivity().overridePendingTransition(R.anim.slide_up_in, R.anim.no_anim);
					    	}
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
    	
    	editROM.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				if(MainActivity.SDmount.equals("mounted")){
					final CharSequence[] items = {"Resize Img's", getActivity().getString(R.string.backup_slot), getActivity().getString(R.string.restore_to_slot), getActivity().getString(R.string.deleteSlot), getActivity().getString(R.string.fix_corrupted_filesystem), getActivity().getString(R.string.wipe_data_factory_reset)};
					CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					builder.setTitle("Manage Slot");
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
					    	if(item == 0){
					    		//Resize imgs here
					    		Intent i = new Intent(getActivity(), com.drx2.bootmanager.manage.Manage.class);
					    		i.putExtra("slot", slot);
					    		startActivity(i);
					    	}
					    	if(item == 1){
					    		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					    		builder.setTitle("Backup Slot " + slot)
					    			   .setMessage(getActivity().getString(R.string.backup) + " " + slot + "?")
					    		       .setCancelable(true)
					    		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
									    		Intent service = new Intent(getActivity(), BackupRestoreService.class);
									    		service.putExtra("whattodo", "backup");
									    		service.putExtra("slot", slot);
									    		getActivity().startService(service);
					    		           }
					    		       })
					    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
					    		                dialog.cancel();
					    		           }
					    		       }).show();
					    	}
					    	if(item == 2){
					    		Intent service = new Intent(getActivity(), Restore.class);
					    		service.putExtra("whattodo", "restore");
					    		service.putExtra("slot", slot);
					    		startActivity(service);
					    	}
					    	if(item == 3){
					    		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					    		builder.setTitle("Delete " + slot)
					    			   .setMessage(getActivity().getString(R.string.deleteSlot) + " " + slot + "?")
					    		       .setCancelable(true)
					    		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
					    		        	   u.deleteDir(new File(u.getExternalDirectory() + "/BootManager/"+slot));
					    		        	   new File(u.getExternalDirectory() + "/BootManager/" + slot).mkdir();
					    		        	   new File(u.getExternalDirectory() + "/BootManager/" + slot + "/.android_secure").mkdir();
					    		        	   getnames();
					    		        	   TextView description = new TextView(getActivity()); 
							    		       description=(TextView)V.findViewById(R.id.romText);
							    		       description.setText(name2);
							    		       TextView description2 = new TextView(getActivity()); 
							    		       description2=(TextView)V.findViewById(R.id.kernelText);
							    		       description2.setText(kernelName);
							    		       TextView description3 = new TextView(getActivity()); 
							    		       description3=(TextView)V.findViewById(R.id.descriptionText);
							    		       description3.setText(desc);
					    		        	}
					    		       })
					    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
					    		                dialog.cancel();
					    		           }
					    		       }).show();
					    	}
					    	if(item == 4){
					    		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					    		builder.setTitle("Fix Corrupted File System")
					    			   .setMessage(getActivity().getString(R.string.fix) + " " + slot + "?")
					    		       .setCancelable(true)
					    		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
					    		        	   u.fixfilesystem(slot, getActivity());
					    		           }
					    		       })
					    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
					    		                dialog.cancel();
					    		           }
					    		       }).show();
					    	}
					    	if(item == 5){
					    		CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					    		builder.setTitle("Wipe Data/Factory Reset?")
					    			   .setMessage(R.string.factory_reset)
					    		       .setCancelable(true)
					    		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
									    		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_DEVICE, 0);
										    	String board=settings.getString("device", "");
									    		PhoneRomSetup prs = new PhoneRomSetup();
									    		prs.factorReset(board, getActivity(), slot);
					    		           }
					    		       })
					    		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					    		           public void onClick(DialogInterface dialog, int id) {
					    		                dialog.cancel();
					    		           }
					    		       }).show();
					    	}
					    }
					});
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int whichButton) {
		    				dialog.cancel();
		    			}
					}).show();
				}else{
					Toast.makeText(getActivity(), R.string.sdNotMounted, Toast.LENGTH_LONG).show();
				}
			}
    	});
    	
    	edit.setOnClickListener(new OnClickListener() { 
			public void onClick(View v){
				if(MainActivity.SDmount.equals("mounted")){
					final CharSequence[] items = {getActivity().getString(R.string.screen_shot), getActivity().getString(R.string.rom_name), getActivity().getString(R.string.kernel_name), getActivity().getString(R.string.description)};
					CustomDialog.Builder builder = new CustomDialog.Builder(getActivity());
					builder.setTitle("User Interface");
					builder.setItems(items, new DialogInterface.OnClickListener() {
					    public void onClick(DialogInterface dialog, int item) {
					    	if(!(new File(u.getExternalDirectory() + "/BootManager/" + slot + "/").exists())){
					    		new File(u.getExternalDirectory() + "/BootManager/" + slot + "/").mkdir();
					    	}
					    	if(item == 0){
					        	Intent i = new Intent(getActivity(), PhotoPicker.class);
					        	i.putExtra("rom", slot);
					        	startActivityForResult(i, 0);
					        
					        }else if(item == 1){
					        	final CustomDialog.Builder alert = new CustomDialog.Builder(getActivity());
					    		final EditText input = new EditText(getActivity());
					    		input.setText(name2);
					    		input.setSelection(input.getText().length());
					    		alert.setView(input, true);
					        	alert.setTitle("Enter ROM Name");
					    		alert.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					    			public void onClick(DialogInterface dialog, int whichButton) {
					    				String value = input.getText().toString().trim();
					    				TextView description = new TextView(getActivity()); 
					    		        description=(TextView)V.findViewById(R.id.romText);
					    		        description.setText(value);
					    		        u.writeFile(u.getExternalDirectory() + "/BootManager/" + slot + "/name", value);
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
					    		        u.writeFile(u.getExternalDirectory() + "/BootManager/" + slot + "/kernel", value);
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
					    		        u.writeFile(u.getExternalDirectory() + "/BootManager/" + slot + "/description", value);
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
					});
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		    			public void onClick(DialogInterface dialog, int whichButton) {
		    				dialog.cancel();
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
		name2=readRomName(u.getExternalDirectory() + "/BootManager/"+slot+"/name");
		if(!(name2 !=null )){
			name2="Slot Empty";
		}
		kernelName=readRomName(u.getExternalDirectory() + "/BootManager/"+slot+"/kernel");
		if(!(kernelName !=null )){
			kernelName="Not Set";
		}
		desc=readRomName(u.getExternalDirectory() + "/BootManager/"+slot+"/description");
		if(!(desc !=null )){
			desc="Add a description here!!";
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