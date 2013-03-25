package com.drx2.bootmanager.extras;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.SparseBooleanArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.drx2.bootmanager.MainActivity;
import com.drx2.bootmanager.Manual;
import com.drx2.bootmanager.R;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.Utilities;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Settings extends PreferenceActivity {

	Intent i;
	Utilities u = new Utilities();
	Context context;
	private static final String PREFS_DEVICE = "DeviceInfo";
	SharedPreferences shared;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    context = getApplicationContext();
		shared = PreferenceManager.getDefaultSharedPreferences(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	    if(shared.getBoolean("themePref", false) == true){
	    	setTheme(android.R.style.Theme_Black_NoTitleBar);
	    }else{
	    	setTheme(android.R.style.Theme_Light_NoTitleBar);
	    }
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		setContentView(R.layout.settings);
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    actionBar.setHomeAction(new Home());
	    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    actionBar.setHomeColor(colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
		mesThread mesActivity = new mesThread();
		mesActivity.start();
	    
		Preference AboutPref = (Preference) findPreference("aboutPref");
		AboutPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Settings.this, About.class);
		    	startActivity(i);
		    	overridePendingTransition(R.anim.slide_up_in, R.anim.no_anim);
		    	Settings.this.finish();
				return true;
			}
		});
		Preference tipsPref = (Preference) findPreference("quicktipsPref");
		tipsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				i = new Intent(Settings.this, Tips.class);
				startActivity(i);
				return true;
			}
		});
		
		EditTextPreference prefEditText1 = (EditTextPreference) findPreference("slotNum");
		prefEditText1.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		
		
		Preference manualPref = (Preference) findPreference("manualPref");
		manualPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				i = new Intent(Settings.this, Manual.class);
				startActivity(i);
				overridePendingTransition(R.anim.slide_up_in, R.anim.no_anim);
				Settings.this.finish();
				return true;
			}
		});
		
		
		
		CheckBoxPreference hboot = (CheckBoxPreference)findPreference("hboot");
		ListPreference installlocation = (ListPreference)findPreference("sdselector");
		PreferenceCategory extra = (PreferenceCategory) findPreference("Extras");
		if(Utilities.device().contains("vigor")){
			installlocation.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference arg0, Object arg1) {
					String useExternal = Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete";
					if(writeToSDSelectorFile(useExternal, arg1.toString())){
						Toast.makeText(context, "Restart BootManager for changes to take effect.", Toast.LENGTH_LONG).show();
						return true;
					}
					return false;
				}
			});
			hboot.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference preference, Object newValue) {
					if(Utilities.device().equals("vigor")){
						SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
						SharedPreferences.Editor editor = settings.edit();
						if(newValue.toString().equals("true"))
							editor.putString("sdcard", "/dev/block/mmcblk0p38");
						else
							editor.putString("sdcard", "/dev/block/mmcblk0p37");
						if(editor.commit())
							return true;
						else
							return false;
					}
					return false;
				}
			});
		} else if(Utilities.device().contains("spyder")||Utilities.device().contains("maserati")||Utilities.device().contains("targa")||Utilities.device().contains("solana")||Utilities.device().contains("holiday")||Utilities.device().contains("ruby")){
			installlocation.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference arg0, Object arg1) {
					String useExternal = Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete";
					if(writeToSDSelectorFile(useExternal, arg1.toString())){
						Toast.makeText(context, "Restart BootManager for changes to take effect.", Toast.LENGTH_LONG).show();
						return true;
					}
					return false;
				}
			});
			extra.removePreference(hboot);
		}else{
			extra.removePreference(installlocation);
			extra.removePreference(hboot);
		}
		Preference uitweakPref = (Preference) findPreference("uiTweakPref");
		uitweakPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Settings.this, UITweaks.class);
		    	startActivity(i);
		    	overridePendingTransition(R.anim.slide_up_in, R.anim.no_anim);
		    	Settings.this.finish();
				return true;
			}
		});
		
		if(u.appInstalledOrNot(context, "com.noshufou.android.su.elite") == false){
			CheckBoxPreference mCheckBoxPref = (CheckBoxPreference) findPreference("superuserPref");
			PreferenceCategory mCategory = (PreferenceCategory) findPreference("Extras");
			mCategory.removePreference(mCheckBoxPref);
		}
		
		Preference clearPref = (Preference) findPreference("clearPref");
		clearPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/BootManager/log.txt");
				Toast.makeText(Settings.this, "Log Cleared Successfully!", Toast.LENGTH_LONG).show();
				return true;
			}
		});
		Preference sLogPref = (Preference) findPreference("sLogPref");
		sLogPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("plain/text");
				i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@init2winitapps.com"});
				i.putExtra(Intent.EXTRA_SUBJECT, "Boot Manager Log");
				i.putExtra(Intent.EXTRA_TEXT   , "");
				i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(u.getExternalDirectory() + "/BootManager/log.txt"))); 
				try {
				    startActivity(Intent.createChooser(i, "Send mail..."));
				} catch (android.content.ActivityNotFoundException ex) {
				    Toast.makeText(Settings.this, "No Email Client Found!", Toast.LENGTH_SHORT).show();
				}
				return true;
			}
		});
		Preference changesPref = (Preference) findPreference("changesPref");
		changesPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				mesDialog();
				return true;
			}
		});
		Preference forumPref = (Preference) findPreference("forumPref");
		forumPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.init2winitapps.com"));
				startActivity(browserIntent);
				return true;
			}
		});
		Preference sitePref = (Preference) findPreference("sitePref");
		sitePref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://init2winitapps.com"));
				startActivity(browserIntent);
				return true;
			}
		});
		Preference donatePref = (Preference) findPreference("donatePref");
		donatePref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=H4VWDD29ZC5P6"));
				startActivity(browserIntent);
				return true;
			}
		});
		Preference twitterPref = (Preference) findPreference("twitterPref");
		twitterPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://twitter.com/#!/init2winitapps"));
				startActivity(browserIntent);
				return true;
			}
		});
		Preference smallBoot = (Preference) findPreference("forcelargeboot");
		PreferenceCategory mCategory2 = (PreferenceCategory) findPreference("Extras");
		SharedPreferences settings2 = getSharedPreferences("DeviceInfo", 0);
		String board2 = settings2.getString("device", "");
		if(board2.equalsIgnoreCase("ruby") || board2.equalsIgnoreCase("holiday") || board2.equalsIgnoreCase("vigor") || board2.equalsIgnoreCase("tuna") || board2.equalsIgnoreCase("sholes") || board2.equalsIgnoreCase("shadow") || board2.equalsIgnoreCase("droid2") || board2.equalsIgnoreCase("droid2we") || board2.equalsIgnoreCase("herring")||board2.contains("spyder")||board2.contains("maserati")||board2.contains("targa")||board2.contains("solana")){
			mCategory2.removePreference(smallBoot);
		}
		
		//Remove sd prefs from the Galaxy nexus since it has no sd
		Preference autosd = (Preference) findPreference("automountPref");
		Preference autosd2 = (Preference) findPreference("sdnotifyPref");
		if(board2.equalsIgnoreCase("tuna")||board2.equalsIgnoreCase("herring")){
			mCategory2.removePreference(autosd);
			mCategory2.removePreference(autosd2);
		}
		
		Preference calllogPref = (Preference) findPreference("calllogPref");
			calllogPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
				public boolean onPreferenceClick(Preference preference) {
					CharSequence[] items = new CharSequence[0];
					ArrayList<CharSequence> items2 = new ArrayList<CharSequence>();
					items2.add("Phone Rom");
					for (int i = 1; i < MainActivity.header.size()-1; i++) {
			        	items2.add("Rom"+i);
			        }
					items = items2.toArray(items);
					//final CharSequence[] items = {"Phone Rom", "Rom1", "Rom2", "Rom3", "Rom4"};
					final boolean[] states = getStates("calls");
					CustomDialog.Builder builder = new CustomDialog.Builder(Settings.this);
			        builder.setTitle("Sync call logs");
			        builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener(){
			            public void onClick(DialogInterface dialogInterface, int item, boolean state) {
			            }
			        });
			        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int id) {
			            	SparseBooleanArray CheCked =((CustomDialog)dialog).getListView().getCheckedItemPositions();
			            	if(CheCked.get(0)){
			            		setStates("phoneRom", true, "calls");
			            	} else {
			            		setStates("phoneRom", false, "calls");
			            	}
			            	for (int i = 1; i < CheCked.size(); i++) {
			            		if(CheCked.get(i)){
			            			setStates("rom"+i, true, "calls");
				            	} else {
				            		setStates("rom"+i, false, "calls");
				            	}
					        }
			            }
			        });
			        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			            }
			        }).show();
			        return true;
				}
			});
		
			Preference smsPref = (Preference) findPreference("smsPref");
			smsPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
				public boolean onPreferenceClick(Preference preference) {
					CharSequence[] items = new CharSequence[0];
					ArrayList<CharSequence> items2 = new ArrayList<CharSequence>();
					items2.add("Phone Rom");
					for (int i = 1; i < MainActivity.header.size()-1; i++) {
			        	items2.add("rom"+i);
			        }
					items = items2.toArray(items);
					final boolean[] states = getStates("sms");
					CustomDialog.Builder builder = new CustomDialog.Builder(Settings.this);
			        builder.setTitle("Sync sms messages");
			        builder.setMultiChoiceItems(items, states, new DialogInterface.OnMultiChoiceClickListener(){
			            public void onClick(DialogInterface dialogInterface, int item, boolean state) {
			            }
			        });
			        builder.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int id) {
			            	SparseBooleanArray CheCked =((CustomDialog)dialog).getListView().getCheckedItemPositions();
			            	if(CheCked.get(0)){
			            		setStates("phoneRom", true, "sms");
			            	} else {
			            		setStates("phoneRom", false, "sms");
			            	}
			            	for (int i = 1; i < CheCked.size(); i++) {
			            		if(CheCked.get(i)){
			            			setStates("rom"+i, true, "sms");
				            	} else {
				            		setStates("rom"+i, false, "sms");
				            	}
					        }
		            	}
			        });
			        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			            public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			            }
			        }).show();
			        return true;
				}
			});	
			
			
		Preference emmc = (CheckBoxPreference) findPreference("useemmc");
		PreferenceCategory mCategory = (PreferenceCategory) findPreference("Extras");
		SharedPreferences settings = getSharedPreferences("DeviceInfo", 0);
		String board = settings.getString("device", "");
		if(board.equalsIgnoreCase("inc") || board.equalsIgnoreCase("herring")){
			emmc.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
				public boolean onPreferenceClick(Preference preference) {
					SharedPreferences settings = getSharedPreferences("DeviceInfo", 0);
					SharedPreferences.Editor editor = settings.edit();
					SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					boolean useemmc = shared.getBoolean("useemmc", false);
					editor.putBoolean("useemmc", useemmc);
					editor.commit();
					if(useemmc){
						File folder2 = new File("/emmc/BootManager");
						File rom1 = new File(folder2+"/rom1");
						File rom2 = new File(folder2 + "/rom2");
						File rom3 = new File(folder2 + "/rom3");
						File rom4 = new File(folder2 + "/rom4");
						if(!(folder2.exists())){
				        	folder2.mkdir();
				        }
				        if(!(rom1.exists())){
				        	rom1.mkdir();
				        }
				        if(!(rom2.exists())){
				        	rom2.mkdir();
				        }
				        if(!(rom3.exists())){
				        	rom3.mkdir();
				        }
				        if(!(rom4.exists())){
				        	rom4.mkdir();
				        }
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom1");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom2");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom3");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom4");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o bind /emmc/BootManager/rom1 /sdcard/BootManager/rom1");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o bind /emmc/BootManager/rom2 /sdcard/BootManager/rom2");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o bind /emmc/BootManager/rom3 /sdcard/BootManager/rom3");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox mount -o bind /emmc/BootManager/rom4 /sdcard/BootManager/rom4");
			        } else {
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom1");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom2");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom3");
			        	u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /sdcard/BootManager/rom4");
			        }
					return true;
				}
			});
		} else {
			mCategory.removePreference(emmc);
		}
	}
	
	
	private void setStates(String slot, Boolean checked, String type){
		if(checked)
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox echo sync > "+u.getExternalDirectory()+"/BootManager/"+slot+"/sync"+type);
		else
			u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/BootManager/"+slot+"/sync"+type);
	}
	
	private boolean[] getStates(String type){
		ArrayList<Boolean> synclist = new ArrayList<Boolean>();
		synclist.add(new File(u.getExternalDirectory()+"/BootManager/phoneRom/sync"+type).exists());
		for (int i = 1; i < MainActivity.header.size()-1; i++) {
    		synclist.add(new File(u.getExternalDirectory()+"/BootManager/rom"+i+"/sync"+type).exists());
        }
		boolean[] states = toPrimitiveArray(synclist);
		return states;
		
	}
	 
	private boolean[] toPrimitiveArray(final ArrayList<Boolean> booleanList) {
	    final boolean[] primitives = new boolean[booleanList.size()];
	    int index = 0;
	    for (Boolean object : booleanList) {
	        primitives[index++] = object;
	    }
	    return primitives;
	}

	
	private void mesDialog() {
		CustomDialog.Builder builder = new CustomDialog.Builder(Settings.this);
		try {
			builder.setMessage(readFile(context.getFilesDir().getAbsolutePath() + "/changelog.txt"))
			       .setCancelable(true)
			       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               dialog.cancel();
			           }
			       }).show();
		} catch (IOException e) {
			Toast.makeText(Settings.this, "Error Getting Change Log!", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
	}
	
	private class mesThread extends Thread{
		@Override
		public void run() {
			try {
				u.downloadUtil(context, "http://bootmanager.gflam.com/changelogs/bootmanager/changelog.txt", context.getFilesDir().getAbsolutePath() + "/changelog.txt");				
				}finally{
 
			}
		}
	}

	@Override
	public void onPause(){
		super.onPause();
		if(shared.contains("slotNum")){
			try {
				Writer output = null;
				String text = shared.getString("slotNum", "4");
				File file = new File(u.getExternalDirectory() + "/BootManager/.zips/slots");
				output = new BufferedWriter(new FileWriter(file));
				output.write(text);
				output.close();
			}catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	}
	
	private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
	    	Settings.this.finish();
	    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	    }
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if(!(new File(context.getFilesDir().getAbsolutePath() + "/files/sqlite3").exists())){
        		try{
    				InputStream in = getAssets().open("sqlite3");
    				OutputStream out = new FileOutputStream(context.getFilesDir().getAbsolutePath()+"/sqlite3");
    				byte[] buf = new byte[1024];
    		    	int len;
    		    	while ((len = in.read(buf)) > 0) {
    		    		out.write(buf, 0, len);
    		    	}
    		    	in.close();
    		    	out.close();
    				u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox chmod 755 " + context.getFilesDir().getAbsolutePath() + "/sqlite3");
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
        	}
    		if(shared.getBoolean("superuserPref", false) == true){
    			u.execCommand(context.getFilesDir().getAbsolutePath() + "/sqlite3 /data/data/com.noshufou.android.su/databases/su.db \"UPDATE apps SET notifications='0' WHERE package='" + context.getPackageName() + "'\"");
    		}else{
    			u.execCommand(context.getFilesDir().getAbsolutePath() + "/sqlite3 /data/data/com.noshufou.android.su/databases/su.db \"UPDATE apps SET notifications='1' WHERE package='" + context.getPackageName() + "'\"");
    		}
        	Settings.this.finish();
        }
        return super.onKeyDown(keyCode, event);
	}
	
	private boolean writeToSDSelectorFile(String filename, String value) {
	     ShellCommand s = new ShellCommand();  
		 @SuppressWarnings("unused")
		 CommandResult wtf = s.su.runWaitFor(getApplicationContext().getFilesDir().getAbsolutePath()+"/busybox echo '"+value+"' > "+filename);
	     if(Utilities.readFirstLineOfFile(filename).contains(value)){
	    	 return true;
	     }
		 return false;
	 }
	
}