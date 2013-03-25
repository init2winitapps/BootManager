package com.drx2.bootmanager.manage;

import java.io.File;

import com.drx2.bootmanager.MainActivity;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.CustomProgressDialog;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.Utilities;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Manage extends Activity {
	private static final String PREFS_DEVICE = "DeviceInfo";
	String slot;
	Context context;
	TextView systemsize;
	TextView datasize;
	TextView cachesize;
	File systemimg;
	File dataimg;
	File cacheimg;
	String romName;
	String size;
	String used;
	String ssize;
	String sused;
	String dsize;
	String dused;
	String csize;
	String cused;
	String board;
	Button resizesystem;
	Button resizedata;
	Button resizecache;
	ActionBar actionBar;
	int sdfreespace;
	ShellCommand s = new ShellCommand();
	Utilities u = new Utilities();
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        context=getApplicationContext();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		if(shared.getBoolean("themePref", false) == true){
	    	setTheme(android.R.style.Theme_Black_NoTitleBar);
	    }else{
	    	setTheme(android.R.style.Theme_Light_NoTitleBar);
	    }
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resizelayout);
		actionBar = (ActionBar) findViewById(R.id.actionbar);
	    actionBar.setHomeAction(new Home());
	    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
	    board=colors.getString("device", "");
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setHomeColor(colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
        Bundle extras = getIntent().getExtras();
        slot = extras.getString("slot");
        systemimg = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/system.img");
        dataimg = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/data.img");
        cacheimg = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/cache.img");
        systemsize = (TextView)findViewById(R.id.systemsizeresize);
        datasize = (TextView)findViewById(R.id.datasizeresize);
        cachesize = (TextView)findViewById(R.id.cachesizeresize);
        resizesystem = (Button)findViewById(R.id.button1);
        resizedata = (Button)findViewById(R.id.button2);
        resizecache = (Button)findViewById(R.id.button3);
        resizesystem.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
        resizesystem.setBackgroundDrawable(MainActivity.buttonState(context));
        resizedata.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
        resizedata.setBackgroundDrawable(MainActivity.buttonState(context));
        resizecache.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
        resizecache.setBackgroundDrawable(MainActivity.buttonState(context));
        loaderTask lT = new loaderTask(slot, context);
        lT.execute();
    }
	
	

	private void getFreeSpace(String image){
		//TODO check if mounted since ext4 won't mount on all roms
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox mkdir /data/local/tmp/imgs");
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox mount "+u.getExternalDirectory()+"/BootManager/"+slot+"/"+image+" /data/local/tmp/imgs");
		long si = new File(u.getExternalDirectory()+"/BootManager/"+slot+"/"+image).length();
		float siz = si / (1024.f * 1024.f);
		size=(int)siz+"MB";
		int usd = (int) usedSpace(new File("/data/local/tmp/imgs"));
		used=usd+"MB";
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox umount /data/local/tmp/imgs");
		u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox rmdir /data/local/tmp/imgs");
	}
	
	
	class loaderTask extends AsyncTask<Void, Void, Void> {
		Context context;
		String slot;
		CustomProgressDialog p;
        
		public loaderTask(String slot, Context context) {
			this.context=context;
			this.slot=slot;
			p = CustomProgressDialog.show(Manage.this, "Examining Img's", "Please wait ...", true, false);
			
			
        }

		@Override
		protected Void doInBackground(Void... arg0) {
			CommandResult romname = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/busybox cat "+u.getExternalDirectory()+"/BootManager/"+slot+"/name");
			romName = romname.stdout;
			getFreeSpace("system.img");
	        ssize=size;
	        sused=used;
	        getFreeSpace("data.img");
	        dsize=size;
	        dused=used;
	        getFreeSpace("cache.img");
	        csize=size;
	        cused=used;
	        sdfreespace = (int)sdFreeSpace(new File(u.getExternalDirectory()+"/BootManager/"+slot+"/system.img"));
			return null;
		}
		@Override 
		protected void onPostExecute(Void unused) {
			if(romName!=null){
				actionBar.setTitle(romName);
	        } else {
	        	actionBar.setTitle(slot.toUpperCase());
	        }
			systemsize.setText(ssize+" "+sused+" Used");
	        datasize.setText(dsize+" "+dused+" Used");
	        cachesize.setText(csize+" "+cused+" Used");
	        if(p!=null)p.dismiss();
	        resizesystem.setOnClickListener(systemclick);
	        resizedata.setOnClickListener(dataclick);
	        resizecache.setOnClickListener(cacheclick);
		}       
    }
	
	class resizeTask extends AsyncTask<Void, Void, Void> {
		Context context;
		String image;
		String value;
		CustomProgressDialog p;
        
		public resizeTask(String image, String value, Context context) {
			this.context=context;
			this.image=image;
			this.value=value;
			p = CustomProgressDialog.show(Manage.this, "Resizing to "+value, "Please wait ...", true, false);
			
			
        }

		@Override
		protected Void doInBackground(Void... arg0) {
			CommandResult e2 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/e2fsck -fy "+u.getExternalDirectory()+"/BootManager/"+slot+"/"+image);
			if(e2.stdout!=null)u.log(e2.stdout);
			if(e2.stderr!=null)u.log(e2.stderr);
			CommandResult cs = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/resize2fs "+u.getExternalDirectory()+"/BootManager/"+slot+"/"+image+" "+value+"m");
			if(cs.stdout!=null)u.log(cs.stdout);
			if(cs.stderr!=null)u.log(cs.stderr);
			CommandResult e3 = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/e2fsck -fy "+u.getExternalDirectory()+"/BootManager/"+slot+"/"+image);
			if(e3.stdout!=null)u.log(e3.stdout);
			if(e3.stderr!=null)u.log(e3.stderr);
			
			return null;
		}
		@Override 
		protected void onPostExecute(Void unused) {
			if(p!=null)p.dismiss();
			loaderTask lT = new loaderTask(slot, context);
	        lT.execute();
		}       
    }
	
	OnClickListener systemclick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(MainActivity.currentromname.contains(slot)){
				u.errorDialog(Manage.this, "Error", "You can't make these changes to the rom you are booted to. Please boot to a different rom to resize this rom.");
			} else {
				final CustomDialog.Builder alert = new CustomDialog.Builder(Manage.this);
	    		final EditText input = new EditText(Manage.this);
	    		final int usedI;
	    		if(sused.contains("MB")){
	    			usedI = Integer.parseInt(sused.substring(0, sused.length()-2).trim());
	    		} else {
	    			usedI = 0;
	    		}
	    		final int sizeI = Integer.parseInt(ssize.substring(0, ssize.length()-2).trim().replace(",", ""));
	    		input.setInputType(InputType.TYPE_CLASS_NUMBER);
	    		alert.setView(input, true);
	        	alert.setTitle("Current size is "+ssize+"\nDo no choose smaller then "+(usedI+11));
	        	alert.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				String value = input.getText().toString().trim();
	    				int input = Integer.parseInt(value);
	    				if(usedI+10 < input){
	    					if((input-sizeI) < (sdfreespace-50)){
	    						resizeTask rT = new resizeTask("system.img", value, context);
	    						rT.execute();
	    					} else {
	    						u.errorDialog(Manage.this, "Not enough space!!", "Not enough space on sdcard.");
	    					}
	    				} else {
	    					u.errorDialog(Manage.this, "Size to small!!", "Please choose a larger size");
	    				}
	    			}
	    		});
	    		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				dialog.cancel();
	    			}
	    		}).show();
			}
		}
		
	};
	
	OnClickListener dataclick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(MainActivity.currentromname.contains(slot)){
				u.errorDialog(Manage.this, "Error", "You can't make these changes to the rom you are booted to. Please boot to a different rom to resize this rom.");
			} else {
				final CustomDialog.Builder alert = new CustomDialog.Builder(Manage.this);
	    		final EditText input = new EditText(Manage.this);
	    		final int usedI;
	    		if(dused.contains("MB")){
	    			usedI = Integer.parseInt(dused.substring(0, dused.length()-2).trim());
	    		} else {
	    			usedI = 0;
	    		}
	    		final int sizeI = Integer.parseInt(dsize.substring(0, dsize.length()-2).trim().replace(",", ""));
	    		input.setInputType(InputType.TYPE_CLASS_NUMBER);
	    		alert.setView(input, true);
	        	alert.setTitle("Current size is "+dsize+"\nDo no choose smaller then "+(usedI+31));
	        	alert.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				String value = input.getText().toString().trim();
	    				int input = Integer.parseInt(value);
	    				if(usedI+10 < input){
	    					if((input-sizeI) < (sdfreespace-50)){
	    						resizeTask rT = new resizeTask("data.img", value, context);
	    						rT.execute();
	    					} else {
	    						u.errorDialog(Manage.this, "Not enough space!!", "Not enough space on sdcard.");
	    					}
	    				} else {
	    					u.errorDialog(Manage.this, "Size to small!!", "Please choose a larger size");
	    				}
	    			}
	    		});
	    		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				dialog.cancel();
	    			}
	    		}).show();
			}
		}
		
	};
	
	OnClickListener cacheclick = new OnClickListener(){
		@Override
		public void onClick(View v) {
			if(MainActivity.currentromname.contains(slot)){
				u.errorDialog(Manage.this, "Error", "You can't make these changes to the rom you are booted to. Please boot to a different rom to resize this rom.");
			} else {
				final CustomDialog.Builder alert = new CustomDialog.Builder(Manage.this);
	    		final EditText input = new EditText(Manage.this);
	    		final int usedI;
	    		if(cused.contains("MB")){
	    			usedI = Integer.parseInt(cused.substring(0, cused.length()-2).trim());
	    		} else {
	    			usedI = 0;
	    		}
	    		final int sizeI = Integer.parseInt(csize.substring(0, csize.length()-2).trim().replace(",", ""));
	    		input.setInputType(InputType.TYPE_CLASS_NUMBER);
	    		alert.setView(input, true);
	        	alert.setTitle("Current size is "+csize+"\nDo no choose smaller then "+(usedI+11));
	        	alert.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				String value = input.getText().toString().trim();
	    				int input = Integer.parseInt(value);
	    				if(usedI+10 < input){
	    					if((input-sizeI) < (sdfreespace-50)){
	    						resizeTask rT = new resizeTask("cache.img", value, context);
	    						rT.execute();
	    					} else {
	    						u.errorDialog(Manage.this, "Not enough space!!", "Not enough space on sdcard.");
	    					}
	    				} else {
	    					u.errorDialog(Manage.this, "Size to small!!", "Please choose a larger size");
	    				}
	    			}
	    		});
	    		alert.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
	    			public void onClick(DialogInterface dialog, int whichButton) {
	    				dialog.cancel();
	    			}
	    		}).show();
			}
		}
		
	};
	
	private static float sdFreeSpace(File f) {
	    StatFs stat = new StatFs(f.getPath());
	    long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
	    return bytesAvailable / (1024.f * 1024.f);
	}

	private static float usedSpace(File f) {
	    StatFs stat = new StatFs(f.getPath());
	    long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
	    long totalbytes = (long)stat.getBlockSize() * (long)stat.getBlockCount();
	    return (totalbytes-bytesAvailable) / (1024.f * 1024.f);
	}
	
	private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
	    	Manage.this.finish();
	    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	    }
	}
}
