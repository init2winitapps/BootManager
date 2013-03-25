package com.drx2.bootmanager;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Scanner;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.drx2.bootmanager.extras.ActionItem;
import com.drx2.bootmanager.extras.Extras;
import com.drx2.bootmanager.extras.QuickAction;
import com.drx2.bootmanager.extras.Settings;
import com.drx2.bootmanager.page.adapter.PageAdapter;
import com.drx2.bootmanager.page.adapter.PageChangeListener;
import com.drx2.bootmanager.page.adapter.PagerHeader;
import com.drx2.bootmanager.services.BackupRestoreService;
import com.drx2.bootmanager.services.BootManagerService;
import com.drx2.bootmanager.services.NandRestoreService;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.CustomProgressDialog;
import com.drx2.bootmanager.utilities.ReadServer;
import com.drx2.bootmanager.utilities.PhoneRomSetup;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.drx2.bootmanager.utilities.Utilities;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class MainActivity extends FragmentActivity implements PageChangeListener {

	Intent i;
	String board;
	String Page;
	public static String SDmount;
	CustomProgressDialog sp = null;
	Utilities u = new Utilities();
	public PagerHeader mPageHeader;
	public ViewPager mViewPager;
	public PageAdapter mAdapter;
	ShellCommand s = new ShellCommand();
	Context context;
	public static String currentromname;
	SharedPreferences colors;
	SharedPreferences shared;
	public static int current_page = 0;
	private static final String PREFS_DEVICE = "DeviceInfo";
	public static GradientDrawable button;
	public static GradientDrawable buttonpressed;
	public static int f1 = 1;
	public static int f2 = 2;
	public static int f3 = 3;
	public static ArrayList<String> header;
	 
	@Override
	public void onCreate(Bundle savedInstanceState){
	    context = getApplicationContext();
		shared = PreferenceManager.getDefaultSharedPreferences(context);
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
	    if(shared.getBoolean("themePref", false) == true){
	    	setTheme(R.style.custom_black);
	    }else{
	    	setTheme(R.style.custom_light);
	    }
	    super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		PageAdapter.setPageChangeListener(this);
		colors = getSharedPreferences(PREFS_DEVICE, 0);
		if (android.os.Environment.MEDIA_MOUNTED.equals("mounted")){
			SDmount = new String("mounted");
		}else{
			SDmount = new String("unmounted");
		}
		mViewPager = (ViewPager)findViewById(R.id.pager);
		mPageHeader = (PagerHeader)findViewById(R.id.pager_header);
		mAdapter = new PageAdapter(this, mViewPager, mPageHeader);
		header = new ArrayList<String>();
		if(shared.getBoolean("viewpagerTitlePref", false)){
			mAdapter.addPage(PhoneROM.class, null, truncate(getnames("phoneRom")));
			header.add(getnames("phoneRom"));
		}else{
			mAdapter.addPage(PhoneROM.class, null, "Phone ROM");
			header.add("Phone ROM");
		}
		String slot = "";
		try{
			Scanner scan = new Scanner(new File(u.getExternalDirectory() + "/BootManager/.zips/slots"));
			if(scan.hasNextLine())slot = scan.nextLine();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}
		if(u.checkIfNumber(slot) == false){
			slot = shared.getString("slotNum", "4");
		}
		int pageNum = Integer.parseInt(slot) + 1;
		int curr = 1;
		int coun = 0;
		while(curr < pageNum){
			if(shared.getBoolean("viewpagerTitlePref", false)){
				if(coun == 0){
					mAdapter.addPage(fragment1.class, null, truncate(getnames("rom"+curr)));
					coun = 1;
				}else if(coun == 1){
					mAdapter.addPage(fragment2.class, null, truncate(getnames("rom"+curr)));
					coun = 2;
				}else if(coun == 2){
					mAdapter.addPage(fragment3.class, null, truncate(getnames("rom"+curr)));
					coun = 0;
				}
				header.add(getnames("rom"+curr));
			} else {
				if(coun == 0){
					mAdapter.addPage(fragment1.class, null, "ROM " + curr);
					coun = 1;
				}else if(coun == 1){
					mAdapter.addPage(fragment2.class, null, "ROM " + curr);
					coun = 2;
				}else if(coun == 2){
					mAdapter.addPage(fragment3.class, null, "ROM " + curr);
					coun = 0;
				}
				header.add("ROM" + curr);
			}
			curr = curr + 1;
		}
		mAdapter.addPage(Extras.class, null, "Extras");
		header.add("Extras");
	    currentROM();
	    SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		board=settings.getString("device", "");
		checkhijack chj = new checkhijack();
		chj.start();
		checkfiles cft = new checkfiles(context);
		cft.start();
		cleanUpThread cU = new cleanUpThread(context);
		cU.start();
		CommandResult mount = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls -l /dev/block/system");
		if(Utilities.device().equals("spyder")||Utilities.device().equals("maserati") && mount.stdout.endsWith("/dev/block/mmcblk1p22")){
			safestrap();
		}/**else if(Utilities.device().equals("targa")||Utilities.device().equals("solana") && mount.stdout.endsWith("/dev/block/mmcblk1p23")){
			safestrap();
		}*/else if(Utilities.device().equals("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana") && !new File("/system/etc/hijack-boot.zip").exists() && currentromname.equals("phone")){
			safestrap();
		}
		if(new File(context.getFilesDir().getAbsolutePath() + "/message").exists()){
			mesDialog();
		}
		updateCheck();
		SharedPreferences serv = getSharedPreferences(PREFS_DEVICE, 0);
    	if (serv.getString("Service", "").equals("Kill")) {
    		Intent service = new Intent(this, BootManagerService.class);
    		stopService(service);
    	}
    	if (settings.getString("NandService", "").equals("Kill")) {
    		Intent service = new Intent(this, NandRestoreService.class);
	    	stopService(service);
	    }
		if (settings.getString("BackupRestoreService", "").equals("Kill")){
			Intent service = new Intent(this, BackupRestoreService.class);
	    	stopService(service);	
		}
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE); 
		notificationManager.cancel(BootManagerService.NOTIFICATION_ID);
		notificationManager.cancel(NandRestoreService.NOTIFICATION_ID);
		notificationManager.cancel(BackupRestoreService.NOTIFICATION_ID);
		if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
			File hijack = new File(u.getExternalDirectory()+"/BootManager/phoneRom/hijack-boot.zip");
			if(board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
				hijack = new File(Environment.getExternalStorageDirectory() + "-ext/BootManager/phoneRom/hijack-boot.zip");
			}
			if(!hijack.exists()) {
				CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
	    		builder.setTitle("Phone Setup")
	    		   	.setMessage(R.string.phoneSet)
	    	       	.setCancelable(true)
	    	       	.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
	    	    	   	public void onClick(DialogInterface dialog, int id) {
	    	        	    setupPR();
	    	           	}
	    	           	})
		    	       	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		    	    	   public void onClick(DialogInterface dialog, int id) {
		    	              
		    	           	}
		    	       	}).show();
			}
		} else {
			if(!new File(u.getExternalDirectory()+"/BootManager/phoneRom/boot.img").exists()) {
				CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
	    		builder.setTitle("Phone Setup")
	    		   	.setMessage(R.string.phoneSet)
	    	       	.setCancelable(true)
	    	       	.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
	    	    	   	public void onClick(DialogInterface dialog, int id) {
	    	    	   		setupPR();
	    	           		}
	    	           	})
		    	       	.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		    	    	   public void onClick(DialogInterface dialog, int id) {
		    	              
		    	           	}
		    	       	}).show();
			}
		}
	}

	private void safestrap(){
		CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
		builder.setTitle("Boot Strap & 2nd Init Required")
			   .setMessage("Boot Strap Recovery and a 2nd Init Phone ROM are required!")
		       .setCancelable(false)
		       .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                MainActivity.this.finish();
		           }
		       }).show();
	}
	
	private void fragNum(int page){
		int cur = 2;
		int scen = 0;
		f1 = 1;
		f2 = 2;
		f3 = 3;
		if(!(page == 0 || page == 1 || page == 2)){
			while(cur != page){
				if(scen == 0){
					f1 = f1 + 3;
					scen = 1;
				}else if(scen == 1){
					f2 = f2 + 3;
					scen = 2;
				}else if(scen == 2){
					f3 = f3 + 3;
					scen = 0;
				}
				cur = cur + 1;
			}
		}
	}
	
	private String truncate(String truncate){
		int length = truncate.length();
		if(length > 30){
			return truncate.substring(0, 30);
		}else{
			return truncate;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.settings:
	    	String slot = "";
			try{
				Scanner scan = new Scanner(new File(u.getExternalDirectory() + "/BootManager/.zips/slots"));
				if(scan.hasNextLine()) slot = scan.nextLine();
			}catch (FileNotFoundException e){
				e.printStackTrace();
			}
			if(u.checkIfNumber(slot) == true){
				if(!(shared.getString("slotNum", "4").equals(slot))){
					SharedPreferences.Editor editor = shared.edit();
					editor.putString("slotNum", slot);
					editor.commit();
				}
			}
	    	i = new Intent(MainActivity.this, Settings.class);
			startActivity(i);
			overridePendingTransition(R.anim.slide_up_in, R.anim.no_anim);
	        return true;
	    case R.id.help:
	    	Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://init2winitapps.com/stories/BMManual.html"));
	    	startActivity(viewIntent);
	        return true;
	    case R.id.other:
	    	startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=init2winit")));
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private class SetupPR implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_gear;
	    }
	    @Override
	    public void performAction(View view) {
	    	fragNum(header.size());
	    	mViewPager.setCurrentItem(header.size());
	    	Page = "extras";
	    	mPageHeader.forceLayout();
			ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
		    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
		    final float scale = context.getResources().getDisplayMetrics().density;
		    int pixels = (int) (45 * scale + 0.5f);
		    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
		    actionBar.setBackgroundDrawable(mDrawable);
		    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    }
	}
	
	private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
	    	currentROM();
	    	mPageHeader.forceLayout();
	    	//onConfigurationChanged(null);
	    	ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
		    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
		    final float scale = context.getResources().getDisplayMetrics().density;
		    int pixels = (int) (45 * scale + 0.5f);
		    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
		    actionBar.setBackgroundDrawable(mDrawable);
		    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    }
	}
	
	private void setupPR(){
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
    	String boot=settings.getString("boot", null);
    	String board=settings.getString("device", "");
    	PhoneRomSetup prs = new PhoneRomSetup();
    	prs.setupPR(board, MainActivity.this, boot);
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
	
	private String getnames(String slot) {
		String name;
		if(slot.contains("phone")){
			name = readRomName(u.getExternalDirectory() + "/BootManager/phoneRom/name");
		} else {
			name = readRomName(u.getExternalDirectory() + "/BootManager/"+slot+"/name");
		}
		if(!(name !=null )){
			if(slot.contains("phone")){
				name="Phone Rom";
			} else {
				name=slot.toUpperCase();
			}
		}
		return name;
	}
	
	public void onResume(){
	    super.onResume();
	    context=getApplicationContext();
	    if(Page==null)currentROM();
	    if(Page==null)Page="phone";
	    if(Page.equals("phone")){
	  		mViewPager.setCurrentItem(0);
			ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
	    	actionBar.addAction(new SetupPR(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    	setupActionbarclick();
	  	}else if(Page.equals("extras")){
	  		mViewPager.setCurrentItem(header.size());
			ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
			actionBar.addAction(new Home(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
			setupActionbarclick();
	  	}else{
	  		fragNum(current_page);
	  		mViewPager.setCurrentItem(current_page);
	  		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
	    	actionBar.addAction(new SetupPR(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    	setupActionbarclick();
	  	}
	    ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.removeAllActions();
	    setupActionbarclick();
	    if(!(current_page == header.size())){
	    	actionBar.addAction(new SetupPR(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    }else{
	    	actionBar.addAction(new Home(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    }
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    
	}
	

	
	private void currentROM() {
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		colors = getSharedPreferences(PREFS_DEVICE, 0);
		shared = PreferenceManager.getDefaultSharedPreferences(context);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		board=settings.getString("device", "");
	    if(board.equals("tuna")){
	    	currentromname=null;
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
							if(shared.contains("romPagePref")){
								if(shared.getBoolean("romPagePref", false)){
									String number = currentromname.substring(currentromname.length()-1);
									if(u.checkIfNumber(number)){
										int i = Integer.parseInt(number);
										fragNum(i);
										mViewPager.setCurrentItem(i);
										Page = currentromname;
									} else {
										mViewPager.setCurrentItem(0);
										Page = "phone";
									}
								}
			    			}else{
								mViewPager.setCurrentItem(0);
								Page = "phone";
							}
							break;
						}
					}
					fis.close();
					br.close();
					dis.close();
					} catch (Exception e) {
						e.printStackTrace();
						u.log(e.toString());
						//return false;
					}
					if(Page==null){
						Page="phone";
						currentromname="phone";
					}
				} 
	    } else {
	    	CommandResult currentrom = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox losetup /dev/block/loop0");
	    	if(currentrom.stdout!=null){
			    if(currentrom.stdout.contains("rom")){
			    	int start = currentrom.stdout.indexOf("rom");
				    int end = start+4;
				    String newcurrentrom = currentrom.stdout.substring(start, end);
				    SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
					if(newcurrentrom!=null){
						if(newcurrentrom.contains("Rom/")){
							currentromname="phone";
							if(shared.contains("romPagePref")){
								if(shared.getBoolean("romPagePref", false)){
									mViewPager.setCurrentItem(0);
									Page = "phone";
								}else{
									mViewPager.setCurrentItem(0);
									Page = "phone";
								}
							}else{
								mViewPager.setCurrentItem(0);
								Page = "phone";
							}
						} else {
							currentromname=newcurrentrom;
							if(shared.contains("romPagePref")){
								if(shared.getBoolean("romPagePref", false)){
									fragNum(Integer.parseInt(currentromname.substring(currentromname.length()-1, currentromname.length())));
									mViewPager.setCurrentItem(Integer.parseInt(currentromname.substring(currentromname.length()-1, currentromname.length())));
									Page = currentromname;
								}else{
									mViewPager.setCurrentItem(0);
									Page = "phone";
								}
							}else{
								mViewPager.setCurrentItem(0);
								Page = "phone";
							}
						}
					} else {
						currentromname="phone";
						if(shared.contains("romPagePref")){
							if(shared.getBoolean("romPagePref", false)){
								mViewPager.setCurrentItem(0);
								Page = "phone";
							}else{
								mViewPager.setCurrentItem(0);
								Page = "phone";
							}
						}else{
							mViewPager.setCurrentItem(0);
							Page = "phone";
						}
					}
			    } else {
			    	currentromname="phone";
					mViewPager.setCurrentItem(0);
					Page = "phone";
			    }
			} else {
		    	currentromname="phone";
				mViewPager.setCurrentItem(0);
				Page = "phone";
		    }
	    }
	}
	
	@Override
	public void onPageChange(int page) {
		current_page = page;
		fragNum(page);
		if(current_page==0){
			Page="phone";
		} else if(current_page+1==header.size()){
			Page="extras";
		} else {
			Page="rom"+current_page;
		}
		if(Page.equals("phone")){
	  		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
	    	actionBar.addAction(new SetupPR(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    	setupActionbarclick();
	  	}else if(Page.equals("extras")){
	  		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
			actionBar.addAction(new Home(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
			setupActionbarclick();
	  	}else {
	  		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
	    	actionBar.addAction(new SetupPR(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    	setupActionbarclick();
	  	}
		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  	setContentView(R.layout.main);
	  	PageAdapter.setPageChangeListener(this);
	  	mViewPager = (ViewPager)findViewById(R.id.pager);
	  	mPageHeader = (PagerHeader)findViewById(R.id.pager_header);
	  	mAdapter = new PageAdapter(this, mViewPager, mPageHeader);
	  	SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
	  	header = new ArrayList<String>();
	  	if(shared.getBoolean("viewpagerTitlePref", false)){
			mAdapter.addPage(PhoneROM.class, null, truncate(getnames("phoneRom")));
			header.add(getnames("phoneRom"));
		}else{
			mAdapter.addPage(PhoneROM.class, null, "Phone ROM");
			header.add("Phone ROM");
		}
	  	String slot = "";
		try{
			Scanner scan = new Scanner(new File(u.getExternalDirectory() + "/BootManager/.zips/slots"));
			slot = scan.nextLine();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}
		if(u.checkIfNumber(slot) == false){
			slot = shared.getString("slotNum", "4");
		}
		int pageNum = Integer.parseInt(slot) + 1;
		int curr = 1;
		int coun = 0;
		while(curr < pageNum){
			if(shared.getBoolean("viewpagerTitlePref", false)){
				if(coun == 0){
					mAdapter.addPage(fragment1.class, null, truncate(getnames("rom"+curr)));
					coun = 1;
				}else if(coun == 1){
					mAdapter.addPage(fragment2.class, null, truncate(getnames("rom"+curr)));
					coun = 2;
				}else if(coun == 2){
					mAdapter.addPage(fragment3.class, null, truncate(getnames("rom"+curr)));
					coun = 0;
				}
				header.add(getnames("rom"+curr));
			} else {
				if(coun == 0){
					mAdapter.addPage(fragment1.class, null, "ROM " + curr);
					coun = 1;
				}else if(coun == 1){
					mAdapter.addPage(fragment2.class, null, "ROM " + curr);
					coun = 2;
				}else if(coun == 2){
					mAdapter.addPage(fragment3.class, null, "ROM " + curr);
					coun = 0;
				}
				header.add("ROM" + curr);
			}
			curr = curr + 1;
		}
		mAdapter.addPage(Extras.class, null, "Extras");
		header.add("Extras");
		if(Page.equals("phone")){
	  		mViewPager.setCurrentItem(0);
			ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
	    	actionBar.addAction(new SetupPR(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    	setupActionbarclick();
	  	}else if(current_page+1 >= header.size()){
	  		mViewPager.setCurrentItem(header.size());
			ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
			actionBar.addAction(new Home(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
			setupActionbarclick();
	  	}else{
	  		fragNum(current_page);
	  		mViewPager.setCurrentItem(current_page);
	  		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			actionBar.removeAllActions();
	    	actionBar.addAction(new SetupPR(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    	setupActionbarclick();
	  	}
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	}
	
	private class checkhijack extends Thread {
    	private checkhijack(){
    		
    	}
    	public void run(){
    		super.run();
    		try{
    			File h1 = new File(u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-rom1.zip");
				File r1 = new File(u.getExternalDirectory()+"/BootManager/.zips/recovery_mode");
				File u1 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip");
				File u2 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2-signed.zip");
				File u3 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3-signed.zip");
				File u4 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4-signed.zip");
				File pr = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip");
				File mConf = new File(u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf");
				if(Utilities.device().equals("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
					if(mConf.exists() && h1.exists() && r1.exists() && u1.exists()){
						//Download good do nothing
					} else {
						runOnUiThread(new Runnable() {                
							public void run() {
								CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
								builder.setTitle("Error Downloading Files")
									.setMessage(R.string.downloadError)
									.setCancelable(false)
									.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											finish();
										}
									}).show();
								}
		       				});
					}
				}else if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")){
					if(mConf.exists() && h1.exists() && r1.exists() && u1.exists() && u2.exists() && u3.exists() && u4.exists() && pr.exists()){
						//Download good do nothing
					} else {
						runOnUiThread(new Runnable() {                
							public void run() {
								CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
								builder.setTitle("Error Downloading Files")
									.setMessage(R.string.downloadError)
									.setCancelable(false)
									.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
										public void onClick(DialogInterface dialog, int id) {
											finish();
										}
									}).show();
								}
		       				});
						}
					}
			}finally{
			}
    	}
	}
	
	private class checkfiles extends Thread {
    	Context context;
		
    	private checkfiles(Context context){
    		this.context=context;
    	}
    	public void run(){
    		super.run();
    		try{
    			File bb = new File(context.getFilesDir().getAbsolutePath() + "/busybox");
				File di = new File(context.getFilesDir().getAbsolutePath() + "/dump_image");
				File ei = new File(context.getFilesDir().getAbsolutePath() + "/erase_image");
				File fi = new File(context.getFilesDir().getAbsolutePath() + "/flash_image");
				File za = new File(context.getFilesDir().getAbsolutePath() + "/zipalign");
				File rb = new File(context.getFilesDir().getAbsolutePath() + "/reboot");
				File fp = new File(context.getFilesDir().getAbsolutePath() + "/fix_permissions");
				File m2 = new File(context.getFilesDir().getAbsolutePath() + "/mke2fs");
				File m22 = new File(context.getFilesDir().getAbsolutePath() + "/morebinarys/mke2fs");
				File mbb = new File(context.getFilesDir().getAbsolutePath() + "/morebinarys/busybox");
				File mfi = new File(context.getFilesDir().getAbsolutePath() + "/morebinarys/flash_image");
				File mdi = new File(context.getFilesDir().getAbsolutePath() + "/morebinarys/dump_image");
				File lw = new File(context.getFilesDir().getAbsolutePath() + "/zip");
				File uy = new File(context.getFilesDir().getAbsolutePath() + "/unyaffs");
				File e2 = new File(context.getFilesDir().getAbsolutePath() + "/e2fsck");
				File up = new File(context.getFilesDir().getAbsolutePath() + "/unpackbootimg");
				File mb = new File(context.getFilesDir().getAbsolutePath() + "/mkbootimg");
				if(mbb.exists() && mfi.exists() && mdi.exists() && up.exists() && mb.exists() && e2.exists() && m22.exists() && bb.exists() && di.exists() && ei.exists() && fi.exists() && za.exists() && rb.exists() && fp.exists() && m2.exists() && lw.exists() && uy.exists()){
					//Download good do nothing
				} else {
					runOnUiThread(new Runnable() {                
						public void run() {
					CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
		    		builder.setTitle("Error Downloading Files")
		    		   	.setMessage(R.string.downloadError)
		    	       	.setCancelable(false)
		    	       	.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		    	    	   	public void onClick(DialogInterface dialog, int id) {
		    	        	   finish();
		    	           	}
		    	       	}).show();
						}
	       			});
				}
				if(board.equals("vigor")){
					if(new File(u.getExternalDirectory()+"/BootManager/.zips/BootImageFlasherRezound.zip").exists()){
						//Download good do nothing
					} else {
						runOnUiThread(new Runnable() {                
							public void run() {
						CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
			    		builder.setTitle("Error Downloading Files")
			    		   	.setMessage(R.string.downloadError)
			    	       	.setCancelable(false)
			    	       	.setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			    	    	   	public void onClick(DialogInterface dialog, int id) {
			    	        	   finish();
			    	           	}
			    	       	}).show();
							}
		       			});
					}
				}
			}finally{
			
			}
    	}
	}

	
	public void cleanUp(Context context) {
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/system"); 
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/data"); 
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox umount /data/local/tmp/cache");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm " + context.getFilesDir().getAbsolutePath() + "/booteditor.zip");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/META-INF");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/busybox");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/mfgsrv");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/createnewboot.sh");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/mkbootimg");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-base");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/data");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/modules.zip");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/etcwifi.zip");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-cmdline");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/e2fsck");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/newboot.img");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-pagesize");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/editramdisk.sh");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/system");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-ramdisk");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/ext2.ko");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/unpackbootimg");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-ramdisk.gz");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/kernelswapper.sh");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/zImage");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/boot.img-zImage");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/kernel");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/cache");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/hijack-boo*");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/edit.sh");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/editkernel.sh");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/updatezip");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/update.zip");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/devices");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm -r /data/local/tmp/kernel");
		u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox losetup -d /dev/block/loop0");
	}
	
	private class cleanUpThread extends Thread {
    	Context context;
		
    	private cleanUpThread(Context context){
    		this.context=context;
    	}
    	public void run(){
    		super.run();
    		try{
    			cleanUp(context);
    			}finally{
    			
    			}
    	}
	}
	
	private void updateCheck(){
		try {
			String link = new String("http://bootmanager.gflam.com/AppVersions/bootmanager.txt");
			if(ReadServer.daerrevreSeliF(link) != null){
				PackageInfo pInfo;
				pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
				final String version = pInfo.versionName;
				final String currentVersion = new String(ReadServer.daerrevreSeliF(link));
				if(!(currentVersion.equals(version))){
					CustomDialog.Builder builder = new CustomDialog.Builder(this);
					builder.setTitle("Boot Manager V" + currentVersion)
					   		.setMessage(MainActivity.this.getString(R.string.updateAvailible) + currentVersion + " " + MainActivity.this.getString(R.string.updateNow))
					   		.setCancelable(true)
					   		.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
					   			public void onClick(DialogInterface dialog, int id) {
					   				if(colors.contains("key")){
					   					if(new File(u.getExternalDirectory() + "/BootManager" + version + ".apk").exists()){
					   						new File(u.getExternalDirectory() + "/BootManager" + version + ".apk").delete();
					   					}
					   					u.downloadUtil(MainActivity.this, "http://bootmanager.gflam.com/Download/BootManager.apk", u.getExternalDirectory() + "/BootManager/.zips/BootManager" + currentVersion + ".apk");
					   					Intent intent = new Intent(Intent.ACTION_VIEW);
					   					intent.setDataAndType(Uri.fromFile(new File(u.getExternalDirectory() + "/BootManager/.zips/BootManager" + currentVersion + ".apk")), "application/vnd.android.package-archive");
					   					startActivity(intent);
					   				}else{
					   					startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.drx2.bootmanager")));
					   				}
					   			}
					   		})
					   		.setNegativeButton(R.string.later, new DialogInterface.OnClickListener() {
					   			public void onClick(DialogInterface dialog, int id) {
					   				dialog.cancel();
					   			}
					   		}).show();
				}
					
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private void mesDialog() {
		CustomDialog.Builder builder = new CustomDialog.Builder(MainActivity.this);
		try {
			final SpannableString s = new SpannableString(readFile(context.getFilesDir().getAbsolutePath() + "/message"));
			final TextView tx1=new TextView(this);
            tx1.setText(s);
            tx1.setAutoLinkMask(RESULT_OK);
            tx1.setMovementMethod(LinkMovementMethod.getInstance());
			Linkify.addLinks(s, Linkify.ALL);
			builder.setIcon(R.drawable.message, true)
				   .setTitle("New Message!")
				   .setCancelable(true)
			       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   new File(context.getFilesDir().getAbsolutePath() + "/message").renameTo(new File(context.getFilesDir().getAbsolutePath() + "/tmpmessage"));
			               dialog.cancel();
			           }
			       }).setView(tx1, false).show();
		} catch (IOException e) {
			Toast.makeText(MainActivity.this, "Error Reading Message!", Toast.LENGTH_SHORT).show();
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
	
	public static StateListDrawable buttonState(Context context){
		int stateFocused = android.R.attr.state_focused;
		int statePressed = android.R.attr.state_pressed;
		button = buttonDrawable(context);
		buttonpressed = buttonDrawablePressed(context);
		StateListDrawable d = new StateListDrawable();
		d.addState(new int[]{-stateFocused, - statePressed}, button);
		d.addState(new int[]{stateFocused, statePressed}, buttonpressed);
		d.addState(new int[]{stateFocused}, buttonpressed);
		d.addState(new int[]{statePressed}, buttonpressed);
		return d;
		
	}
	/** Unpressed button **/
	public static GradientDrawable buttonDrawable(Context context){
		SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
		int startcolor = colors.getInt("buttonStart", context.getResources().getColor(R.color.buttonStart));
		int endcolor = colors.getInt("buttonEnd", context.getResources().getColor(R.color.buttonEnd));
		int stroke = context.getResources().getColor(R.color.stroke);
		int[] color = {startcolor, endcolor};
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
		d.setCornerRadius(5);
		d.setStroke(3, stroke);
		return d;
		
	}
	
	/** Pressed button **/
	public static GradientDrawable buttonDrawablePressed(Context context){
		SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
		int endcolor = colors.getInt("buttonStart", context.getResources().getColor(R.color.buttonStart));
		int[] color = {endcolor, endcolor};
		int stroke = context.getResources().getColor(R.color.stroke);
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
		d.setCornerRadius(5);
		d.setStroke(3, stroke);
		return d;
	}

	public int Icon(String name, int icon){
		if(name.toLowerCase().contains("cyan") || name.toLowerCase().startsWith("cm") || name.toLowerCase().contains(" cm") || name.toLowerCase().contains("cm ") || name.toLowerCase().contains("cm-") || name.toLowerCase().contains("-cm")){
			icon = R.drawable.cyanogenmod;
			return icon;
		}else if(name.toLowerCase().contains("miui")){
			icon = R.drawable.miui;
			return icon;
		}else if(name.toLowerCase().contains("synergy")){
			icon = R.drawable.synergy;
			return icon;
		}else if(name.toLowerCase().contains("virtuous")){
			icon = R.drawable.virtuous;
			return icon;
		}else if(name.toLowerCase().contains("infected")){
			icon = R.drawable.infected;
			return icon;
		}else if(name.toLowerCase().contains("leedroid")){
			icon = R.drawable.leedroid;
			return icon;
		}else if(name.toLowerCase().contains("viper")){
			icon = R.drawable.viper;
			return icon;
		}else if(name.toLowerCase().contains("zen")){
			icon = R.drawable.zen;
			return icon;
		}else if(name.toLowerCase().contains("salvage")){
			icon = R.drawable.salvage;
			return icon;
		}else if(name.toLowerCase().contains("green rom") || name.toLowerCase().contains("grp") || name.toLowerCase().contains("green")){
			icon = R.drawable.grp;
			return icon;
		}else if(name.toLowerCase().contains("skyraider")){
			icon = R.drawable.skyraider;
			return icon;
		}else if(name.toLowerCase().contains("omfgb")){
			icon = R.drawable.omfgb;
			return icon;
		}else if(name.toLowerCase().contains("liquid")){
			icon = R.drawable.lsr;
			return icon;
		}else if(name.toLowerCase().contains("liberty")){
			icon = R.drawable.liberty;
			return icon;
		}else if(name.toLowerCase().contains("bamf")){
			icon = R.drawable.bamf;
			return icon;
		}else if(name.toLowerCase().contains("sourcery")){
			icon = R.drawable.sourcery;
			return icon;
		}else{
			icon = R.drawable.btn_gear;
			return icon;
		}
	}
	
	private ActionItem ai(String title){
		ActionItem item = new ActionItem();
		item.setTitle(title);
		item.setIcon(context.getResources().getDrawable(Icon(title, R.drawable.icon)));
		return item;
	}
	public void setupActionbarclick(){
		final QuickAction mQuickAction  = new QuickAction(context);
        mQuickAction.addActionItem(ai(getnames("phoneRom")));
        for (int i = 1; i < header.size()-1; i++) {
        	mQuickAction.addActionItem(ai(getnames("rom"+i)));
        }
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
        	@Override
			public void onItemClick(QuickAction source, int pos, int actionId) {
				fragNum(pos);
				mViewPager.setCurrentItem(pos);
				mPageHeader.forceLayout();
			}
        });
        
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
        actionBar.setTitle("Booted To: " + getnames(currentromname));
        actionBar.setOnTitleClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	mQuickAction.show(v);
	        }
	    });
	}
		
}