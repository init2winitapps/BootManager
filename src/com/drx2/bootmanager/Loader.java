package com.drx2.bootmanager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.drx2.bootmanager.utilities.Utilities;

public class Loader extends Activity{
	Intent i;
	ShapeDrawable pgDrawable;
	int progress = 0;
	ProgressBar pg;
	boolean customTitleSupported;
	Context context;
	Utilities u;
	ShellCommand s;
	String selection;
	private static final String PREFS_DEVICE = "DeviceInfo";
	File folder;
	File rom1;
	File rom2;
	File rom3;
	File rom4;
	File rom1as;
	File rom2as;
	File rom3as;
	File rom4as;
	File phoneRom;
	File backup;
	Boolean variablesset;
	static Boolean useemmc;
	Boolean resetanimation;
	Boolean gappsinstalled;
	Boolean firstuse=false;
	String appKey = "bmk1";
	SharedPreferences settings;
	
	@Override
	public void onCreate(Bundle icicle) {
	super.onCreate(icicle);
		context=getApplicationContext();
		//resetanimation=true;
		customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		//setContentView(R.layout.loader);
		//Set contentVeiw can't go above this line...need to request custom title bar first
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.loader, null);
		//if we set it normaly without inflating the progress bar doesn't seem to work
		setContentView(layout);
		settings = getSharedPreferences(PREFS_DEVICE, 0);
		ShapeDrawable d = new ShapeDrawable();
        d.getPaint().setColor(settings.getInt("actionbarStart", context.getResources().getColor(R.color.actionbar_background_end)));
		((LinearLayout) layout.findViewById(R.id.border)).setBackgroundDrawable(d);
		setContentView(layout);
		customTitleBar("Loading ...", "");
		u = new Utilities();
		//Start License Thread
		folder = new File(u.getExternalDirectory() + "/BootManager");
		rom1 = new File(folder + "/rom1");
		rom2 = new File(folder + "/rom2");
		rom3 = new File(folder + "/rom3");
		rom4 = new File(folder + "/rom4");
		rom1as = new File(folder + "/rom1/.android_secure");
		rom2as = new File(folder + "/rom2/.android_secure");
		rom3as = new File(folder + "/rom3/.android_secure");
		rom4as = new File(folder + "/rom4/.android_secure");
		phoneRom = new File(folder + "/phoneRom");
		backup = new File(folder + "/Backup");
		variablesset=settings.getBoolean("varset", false);
		setUp();
		//End License Thread
	}
	/**
	 * Method to list files of a root only directory
	 * @param dir
	 * @return Returns list of files in dir
	 */
	public File[] listFiles(File dir, Context context){
		File bb = new File(context.getFilesDir().getAbsolutePath() + "/busybox");
		CommandResult board = s.su.runWaitFor("getprop ro.product.board");
		if(board.stdout!=null){
			String board2= board.stdout;
			if(!bb.exists()){
				if(board2.contains("tuna")){
					if(!bb.exists()) u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
					if(!bb.exists()) u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
					if(!u.checkbinarys(bb.toString(), "92cd913a325866a8d7a03044a19ed1f0"))u.execCommand("rm "+bb.toString());else progress(progress+1);
				} else {
					if(!bb.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
					if(!bb.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
					if(!u.checkbinarys(bb.toString(), "94e5efab5f0115baab91376ebfb3ad98"))u.execCommand("rm "+bb.toString());else progress(progress+1);
				}
			}
			u.execCommand("chmod 777 "+context.getFilesDir().getAbsolutePath()+"/busybox");
		}
		ArrayList<File> files = new ArrayList<File>();
		File[] filelist;
		if(dir.canRead()){
			filelist = dir.listFiles();
		} else {
			CommandResult cmd = s.su.runWaitFor(context.getFilesDir().getAbsolutePath() + "/busybox ls -A '" + dir + "'");
			if(cmd.stdout != null){
				Scanner scanner = new Scanner(cmd.stdout);
				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					File file = new File(dir+"/"+line);
					files.add(file);
				}
			} else {
				files.add(new File("/dev/zero"));
			}
			filelist = new File[0];
			filelist = files.toArray(filelist);
		}
		return filelist;
	}
	
	@Override
	public void onWindowFocusChanged(boolean bool) { 
		super.onWindowFocusChanged(bool);
		//Animation a = AnimationUtils.loadAnimation(this, R.anim.scale);
		ImageView img = (ImageView)findViewById(R.id.simple_anim);
		img.setBackgroundResource(R.drawable.loader);
		//if(resetanimation) img.startAnimation(a);
		//resetanimation=false;
		MyAnimationRoutine mar = new MyAnimationRoutine();
		MyAnimationRoutine2 mar2 = new MyAnimationRoutine2();
		Timer t = new Timer(false);
		t.schedule(mar, 100);
		Timer t2 = new Timer(false);
		t2.schedule(mar2, 5000);
		
	}
	
	class MyAnimationRoutine extends TimerTask{
		MyAnimationRoutine(){
		}
		
	public void run(){
		ImageView img = (ImageView)findViewById(R.id.simple_anim);
		AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
		frameAnimation.start();
		}
	}

	class MyAnimationRoutine2 extends TimerTask{
		MyAnimationRoutine2(){
		}

	public void run(){
		ImageView img = (ImageView)findViewById(R.id.simple_anim);
		@SuppressWarnings("unused")
		AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
		}
	}
	
	public void customTitleBar(String left, String right) {
        if (right.length() > 20) right = right.substring(0, 20);
        // set up custom title
        if (customTitleSupported) {
        	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.my_titlebar);
        	SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
            ((RelativeLayout) findViewById(R.id.titlebackground)).setBackgroundColor(colors.getInt("actionbarStart", context.getResources().getColor(R.color.actionbar_background_end)));
        	TextView titleTvLeft = (TextView) findViewById(R.id.titleTvLeft);
        	titleTvLeft.setText(left);
        	pg = (ProgressBar) findViewById(R.id.leadProgressBar);
        	final float[] roundedCorners = new float[] { 10, 10, 10, 10, 10, 10, 10, 10 };
        	pgDrawable = new ShapeDrawable(new RoundRectShape(roundedCorners, null,null));
        	String MyColor = "#FFFFFFFF";
        	pgDrawable.getPaint().setColor(Color.parseColor(MyColor));
        	ClipDrawable progress = new ClipDrawable(pgDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
        	pg.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.progress_horizontal));
        	pg.setProgressDrawable(progress);
        	//hide the progress bar if it is not needed
        	//titleProgressBar.setVisibility(ProgressBar.GONE);
        	}
		}
	
	private void progress(final int p){
		progress=p;
		if(pg!=null)pg.setProgress(p);
	}

	private class superThread extends Thread{
    	
    	private superThread(){
    	}
    	public void run(){
    		super.run();
    		try{
    			String board=settings.getString("device", "");
				if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.equals("inc")||board.equals("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
					if(pg!=null)pg.setMax(33);
				}else if(board.equals("aloha")){
					if(pg!=null)pg.setMax(30); 
				}else if(board.equals("sholes")){
					if(pg!=null)pg.setMax(36);
				}else{
					if(pg!=null)pg.setMax(29);
				}
				Loader.this.runOnUiThread(new Runnable() {
				    public void run() {
				    	messageCheck();
				    }
				});
    			getassets();
    			getFiles();
    			getupdate();
    			gethijack();
    			if(!board.equals("vigor")){
    				getlibs();
    			}
    			getsholes();
    			if(pg!=null){
    				progress(pg.getMax());
    			}
    		} finally {
    			if(!settings.contains("lunfile")){ 
	    			String lunfile = findlunfile();
	    			if(lunfile!=null){
	    				SharedPreferences.Editor edit = settings.edit();
	    				edit.putString("lunfile", lunfile);
	    				edit.putString("lunfile1", lunfile.replace("lun0", "lun1"));
	    				edit.commit();
	    			}
    			}
    			setupemmc(context, useemmc);
    			Intent i = new Intent(Loader.this, MainActivity.class);
    		    startActivityForResult(i, 0);
    		    finish();
    		}
    	}
	};

	private boolean getDevice() {
		if(!variablesset){
		File mountFile = new File("/system/build.prop");
		StringBuilder procData = new StringBuilder();
		if(mountFile.exists()) {
			try {
				FileInputStream fis = new FileInputStream(mountFile.toString());
				DataInputStream dis = new DataInputStream(fis);
				BufferedReader br = new BufferedReader(new InputStreamReader(dis));
				String data;
				while((data = br.readLine()) != null) {
					procData.append(data + "\n");				
				}
			} catch (Exception e) {
				e.printStackTrace();
				u.log(e.toString());
				return false;
			}
			if(procData.toString() != null) {
				String[] tmp = procData.toString().split("\n");
				for(int x = 0; x < tmp.length; x++) {
					if(tmp[x].contains("ro.product.board")) {
						u.log("Board detected as "+tmp[x]);
						if(tmp[x].contains("sholes")) {
							//Motorala Droid -TESTED WORKING
							//Tester-GFlam
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "sholes");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "140");
							editor.putString("datasize", "262");
							editor.putString("cachesize", "92");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if(tmp[x].contains("inc")) {
							if(!tmp[x].contains("incs")) {
							//HTC Incredible-TESTED WORKING
							//Tester-Conap, tiny4579@XDA, blcklab@XDA
							SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor editor = settings.edit();
							SharedPreferences.Editor share = shared.edit();
							editor.putString("device", "inc");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "290");
							editor.putString("datasize", "950");
							editor.putString("cachesize", "192");
							editor.putString("emmc", "/dev/block/mmcblk0p3");
							boolean firstruncomplete = settings.getBoolean("firstruncomplete", false);
							if(!firstruncomplete){
							useemmc=true;
							editor.putBoolean("useemmc", true);
							share.putBoolean("useemmc", true).commit();
							editor.putBoolean("firstruncomplete", true);
							}
							editor.putBoolean("varset", true);
							editor.commit();
							setupemmc(context, useemmc);
					        superThread threadActivity = new superThread();
			    			threadActivity.start();
							}
							return true;
						} else if (tmp[x].contains("mecha"))  {
							//HTC Thunderbolt - TESTED-WORKING
							//Tester-Yankindasouth @ df
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "mecha");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "849");
							editor.putString("datasize", "2600");
							editor.putString("cachesize", "423");
							editor.putBoolean("varset", true);
							editor.commit();
							//Make force large boot default
							SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor sharededitor = shared.edit();
							sharededitor.putBoolean("forcelargeboot", true);
							sharededitor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("supersonic"))  {
							//HTC Evo 4G -Tested Working
							//tazman92863@gmail.com (Tim Stevens)
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "supersonic");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "370");
							editor.putString("datasize", "430");
							editor.putString("cachesize", "160");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("spade")|| tmp[x].contains("ace")||tmp[x].contains("Ace"))  {
							//HTC Ace/Desire HD-
							//Tester lukersik@gmail.com blackthesoul@gmail.com, mohit408@gmail.com
							//HTC Inspire4G
							//Tester  mikegoss010@gmail.com, kylanfabila@gmail.com
							//Tested working
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "spade");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "550");
							editor.putString("datasize", "1150");
							editor.putString("cachesize", "296");
							editor.putBoolean("varset", true);
							editor.commit();
							//Make force large boot default
							SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor sharededitor = shared.edit();
							sharededitor.putBoolean("forcelargeboot", true);
							sharededitor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("vision"))  {
							//HTC Vision aka G2 maybe also named Desire Z? -In testing waiting for feedback. 
							//Tester-misfit61871 @ xda
							//Tested working
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "vision");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "480");
							editor.putString("datasize", "1300");
							editor.putString("cachesize", "199");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("mahimahi"))  {
							//Nexus One aka Passion-Tester: xsafire@gmail.com  Stuart.upton.su@googlemail.com Turge08@gmail.com jay@jayuk.org
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "mahimahi");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "150");
							editor.putString("datasize", "200");
							editor.putString("cachesize", "97");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("bravo"))  {
							//HTC Desire -Tested Working
							//Tester madkai@xda,allfourthree@gmail.com,c.michael.murphy@gmail.com,morten.mhp@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "bravo");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "250");
							editor.putString("datasize", "147");
							editor.putString("cachesize", "40");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
							//HTC Inspire4G  Not needed..this phone is same as DHD
							//Tester  mikegoss010@gmail.com, kylanfabila@gmail.com
						} else if (tmp[x].contains("latte")) {
							//HTC Tmobile MyTouch Slide
							//Tester jshealton@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "latte");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "240");
							editor.putString("datasize", "145");
							editor.putString("cachesize", "80");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("shooteru")) {      
							//HTC Evo 3D GSM-Tested Working
							//colthekid
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "shooteru");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "787");
							editor.putString("datasize", "1176");
							editor.putString("cachesize", "118");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("shooter")||tmp[x].contains("PG86100")) {      
							//HTC Evo 3D
							//Tester GFlam
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "shooter");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "787");
							editor.putString("datasize", "1126");
							editor.putString("cachesize", "109");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("vivow")) {
							//HTC Incredible 2
							//Tester ruvort1@gmail.com, ForougAS@gmail.com, volumeindrivecistooloud@gmail.com (good tester)
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "vivow");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "549");
							editor.putString("datasize", "1126");
							editor.putString("cachesize", "289");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("glacier")) {
							//HTC myTouch 4G
							//Tester davidhooper4@gmail.com gazaian13@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "glacier");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "550");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "290");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("saga")) {
							//HTC HTC Desire S - working
							//Tester markus.egger.www@gmail.com shromekhanna@gmail.com
							//Good Tester zacthespack@gmail.com (from twitter)  acdwebpro@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "saga");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "545");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "290");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("p999")||(tmp[x].contains("tegra"))) {
							//LG-P999 aka T-Mobile G2x
							//Tester rafase282@gmail.com kgill7@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "tegra");
							editor.putString("sdcard", "/dev/block/mmcblk0p8");//This is actually /emmc but the are mounting it as /sdcard i think?
							editor.putString("systemsize", "414");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "62");
							editor.putString("boot", "/dev/block/mmcblk0p5");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("herring")) {
								//Nexus S
								//Tester jshealton@gmail.com
								SharedPreferences.Editor editor = settings.edit();
								editor.putString("device", "herring");
								editor.putString("sdcard", "/dev/block/platform/s3c-sdhci.0/by-name/media");
								editor.putString("systemsize", "504");
								editor.putString("datasize", "1008");
								editor.putString("cachesize", "470");
								editor.putString("boot", "/dev/block/mmcblk0p5");//needs confirming also saw /dev/mtd/mtd2
								editor.putBoolean("varset", true);
								editor.commit();
								superThread threadActivity = new superThread();
				    			threadActivity.start();
								return true;
						} else if (tmp[x].contains("pyramid")) {
							//HTC Pyramid - Sensation -working
							//Tester pauloveeidem@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "pyramid");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "788");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "118");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("olympus")) {
							//Motorola Atrix -not working
							//Tester batmania.kc@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "olympus");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "309");
							editor.putString("datasize", "2000");
							editor.putString("cachesize", "629");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("vivo")) {
							if(!tmp[x].contains("vivow")) {
							//HTC Incredible S -working
							//Tester toots5555.mk@gmail.com -have not emailed him yet
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "vivo");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "545");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "290");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
							}
						} else if (tmp[x].contains("aloha")){
							//LG Ally
							//Tested 
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "aloha");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "179");
							editor.putString("datasize", "153");
							editor.putString("cachesize", "128");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("blade")) {
							//ZTE Blade
							//Tester m.vaculciak.jr@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "blade");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "136");
							editor.putString("datasize", "303");
							editor.putString("cachesize", "10");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						}  else if (tmp[x].contains("buzz")) {
							//HTC WildFire
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "buzz");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "250");
							editor.putString("datasize", "200");
							editor.putString("cachesize", "40");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						}  else if (tmp[x].contains("shadow")) {
							//DroidX ready to go :)
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "shadow");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "270");
							editor.putString("datasize", "1000");
							editor.putString("cachesize", "184");
							editor.commit();
							warning2ndInitdialog("shadow");
							return true;
						}  else if (tmp[x].contains("spyder")) {
							//Droid RAZR
							//Tester GFlam
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "spyder");
							editor.putString("sdcard", "/dev/block/mmcblk1p25");
							editor.putString("ext_sd", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "637");
							editor.putString("datasize", "2000");
							editor.putString("cachesize", "1008");
							editor.commit();
							if(u.appInstalledOrNot(Loader.this, "com.hashcode.razrsafestrap") == true){
								CustomDialog.Builder builder = new CustomDialog.Builder(this);
								builder.setTitle("Safestrap")
									   .setMessage("Boot Manager is not compatible with safestrap! Please uninstall safestrap and install boot strap in order to use Boot Manager")
								       .setCancelable(false)
								       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
								           public void onClick(DialogInterface dialog, int id) {
								                Loader.this.finish();
								           }
								       }).show();
							}else{
								warning2ndInitdialog("spyder");
							}
							return true;
						}  else if (tmp[x].contains("solana")) {
							//Droid3
							//Tester Hash Code
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "solana");
							editor.putString("sdcard", "/dev/block/mmcblk1p25");
							editor.putString("ext_sd", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "426");
							editor.putString("datasize", "2000");
							editor.putString("cachesize", "535");
							editor.commit();
							if(u.appInstalledOrNot(Loader.this, "com.hashcode.razrsafestrap") == true){
								CustomDialog.Builder builder = new CustomDialog.Builder(this);
								builder.setTitle("Safestrap")
									   .setMessage("Boot Manager is not compatible with safestrap! Please uninstall safestrap and install boot strap in order to use Boot Manager")
								       .setCancelable(false)
								       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
								           public void onClick(DialogInterface dialog, int id) {
								                Loader.this.finish();
								           }
								       }).show();
							}else{
								warning2ndInitdialog("solana");
							}
							return true;
						}  else if (tmp[x].contains("maserati")) {
							//Droid 4
							//Tester Hash Code
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "maserati");
							editor.putString("sdcard", "/dev/block/mmcblk1p25");
							editor.putString("ext_sd", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "637");
							editor.putString("datasize", "2000");
							editor.putString("cachesize", "1008");
							editor.commit();
							if(u.appInstalledOrNot(Loader.this, "com.hashcode.razrsafestrap") == true){
								CustomDialog.Builder builder = new CustomDialog.Builder(this);
								builder.setTitle("Safestrap")
									   .setMessage("Boot Manager is not compatible with safestrap! Please uninstall safestrap and install boot strap in order to use Boot Manager")
								       .setCancelable(false)
								       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
								           public void onClick(DialogInterface dialog, int id) {
								                Loader.this.finish();
								           }
								       }).show();
							}else{
								warning2ndInitdialog("maserati");
							}
							return true;
						}  else if (tmp[x].contains("targa")) {
							//Droid Bionic
							//Tester Hash Code
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "targa");
							editor.putString("sdcard", "/dev/block/mmcblk1p26");
							editor.putString("ext_sd", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "478");
							editor.putString("datasize", "2000");
							editor.putString("cachesize", "709");
							editor.commit();
							/**if(u.appInstalledOrNot(Loader.this, "com.hashcode.razrsafestrap") == true){
								CustomDialog.Builder builder = new CustomDialog.Builder(this);
								builder.setTitle("Safestrap")
									   .setMessage("Boot Manager is not compatible with safestrap! Please uninstall safestrap and install boot strap in order to use Boot Manager")
								       .setCancelable(false)
								       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
								           public void onClick(DialogInterface dialog, int id) {
								                Loader.this.finish();
								           }
								       }).show();
							}else{*/
								warning2ndInitdialog("targa");
							//}
							return true;
						}  else if (tmp[x].contains("droid2")) {
							//MOTO D2 tester...know's adb :) foxdox66@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "droid2");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "270");
							editor.putString("datasize", "1000");
							editor.putString("cachesize", "184");
							editor.commit();
							warning2ndInitdialog("droid2");
							return true;
						}  else if (tmp[x].contains("droid2we")) {
							//MOTO D2we jvec31@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "droid2we");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "270");
							editor.putString("datasize", "1000");
							editor.putString("cachesize", "184");
							editor.commit();
							warning2ndInitdialog("droid2we");
							return true;
						}  else if (tmp[x].contains("triumph")) {
							//MOTO Triumph jvec31@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "triumph");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "250");
							editor.putString("datasize", "1120");
							editor.putString("cachesize", "193");
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						}  else if (tmp[x].contains("HTC_X515E")) {
							//Htc Evo4G+ 
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "HTC_X515E");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "788");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "109");
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("doubleshot")) {
							//MyTouch 4g Slide
							//Tester 
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "doubleshot");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "787");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "118");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("tuna")) {
							//Nexus Galaxy
							//Tester 
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "tuna");
							editor.putString("sdcard", "/dev/block/platform/omap/omap_hsmmc.0/by-name/userdata");//This will mount data and they will need to open media folder so we might need to exclude this...won't work on windows i don't think cause ext4 probably won't mount on windows
							editor.putString("systemsize", "644");
							editor.putString("datasize", "1100");
							editor.putString("cachesize", "400");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("icong")) {
							//HTC phone...need to look up the name :)
							//Tester ferretsievo@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "icong");
							editor.putString("sdcard", "/dev/block/mmcblk0p1");
							editor.putString("systemsize", "260");
							editor.putString("datasize", "999");
							editor.putString("cachesize", "30");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("otter")||tmp[x].contains("omap4sdp")||tmp[x].contains("blaze")) {
							//Kindle fire
							//Tester nathan hendrix
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "otter");
							editor.putString("sdcard", "/dev/block/platform/mmci-omap-hs.1/by-name/media");
							editor.putString("systemsize", "505");
							editor.putString("datasize", "600");
							editor.putString("cachesize", "150");
							editor.putString("boot", "/dev/block/mmcblk0p7");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("vigor")) {
							//HTC Rezound - vigor
							//Tester me!!!!!
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "vigor");
							editor.putString("ext_sd", "/dev/block/mmcblk1p1");
							//editor.putString("sdcard", "/dev/block/mmcblk0p37");
							editor.putString("systemsize", "1200");
							editor.putString("datasize", "1200");
							editor.putString("cachesize", "250");
							editor.putBoolean("varset", true);
							editor.commit();
							SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor sharededitor = shared.edit();
							sharededitor.putBoolean("forcelargeboot", true);
							sharededitor.commit();
							if(!shared.contains("hboot")){
								askHboot();
							} else {
								if(new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").exists()){
									superThread threadActivity = new superThread();
					    			threadActivity.start();
								} else {
									sdSelectorDialog();
								}
							}
							return true;
						} else if (tmp[x].contains("runnymede")) {
							//Htc Sensation XL
							//Tester Faisal Shami shami.fa@gmail.com
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "runnymede");
							editor.putString("sdcard", "/dev/block/mmcblk0p33");
							editor.putString("systemsize", "1008");
							editor.putString("datasize", "1024");
							editor.putString("cachesize", "567");
							editor.putBoolean("varset", true);
							editor.commit();
							superThread threadActivity = new superThread();
			    			threadActivity.start();
							return true;
						} else if (tmp[x].contains("holiday")) {
							//Htc Vivid
							//Tester 
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "holiday");
							editor.putString("sdcard", "/dev/block/mmcblk0p36");
							editor.putString("ext_sd", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "1008");
							editor.putString("datasize", "1024");
							editor.putString("cachesize", "283");
							editor.putBoolean("varset", true);
							editor.commit();
							SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor sharededitor = shared.edit();
							sharededitor.putBoolean("forcelargeboot", true);
							sharededitor.commit();
							if(new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").exists()){
								superThread threadActivity = new superThread();
				    			threadActivity.start();
							} else {
								sdSelectorDialog();
							}
							return true;
						} else if (tmp[x].contains("ruby")) {
							//Htc Vivid
							//Tester 
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "ruby");
							editor.putString("sdcard", "/dev/block/mmcblk0p36");
							editor.putString("ext_sd", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "1008");
							editor.putString("datasize", "1024");
							editor.putString("cachesize", "315");
							editor.putBoolean("varset", true);
							editor.commit();
							SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor sharededitor = shared.edit();
							sharededitor.putBoolean("forcelargeboot", true);
							sharededitor.commit();
							if(new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").exists()){
								superThread threadActivity = new superThread();
				    			threadActivity.start();
							} else {
								sdSelectorDialog();
							}
							return true;
						} else if (tmp[x].contains("MSM8960")||tmp[x].contains("d2vzw")) {
							//Samsung SGS3
							//Tester 
							SharedPreferences.Editor editor = settings.edit();
							editor.putString("device", "d2vzw");
							editor.putString("sdcard", "/dev/block/mmcblk1p1");
							editor.putString("systemsize", "1024");
							editor.putString("datasize", "1024");
							editor.putString("cachesize", "512");
							editor.putBoolean("varset", true);
							editor.commit();
							SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
							SharedPreferences.Editor sharededitor = shared.edit();
							sharededitor.putBoolean("forcelargeboot", true);
							sharededitor.commit();
							return true;
							//d3 tester"Lee Wilson" <kain4475@gmail.com>
						//DX2 tester paulbwells@gmail.com
						//Bionic	collins.drums@gmail.com
							//wchrisyu@gmail.com nexus s says he won't bail :)
						} else {
							CustomDialog.Builder builder = new CustomDialog.Builder(this);
							builder.setTitle("Unsupported Device!")
								   .setMessage(R.string.unsupported)
							       .setCancelable(false)
							       .setPositiveButton("Email", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   Intent i = new Intent(Intent.ACTION_SEND);
							        	   i.setType("text/html");
							        	   i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"support@init2winitapps.com"});
							        	   i.putExtra(Intent.EXTRA_SUBJECT, "Boot Manager Unsupported Device");
							        	   try {
							        	       startActivity(Intent.createChooser(i, "Send Email to Us"));
							        	   } catch (android.content.ActivityNotFoundException ex) {
							        	       Toast.makeText(Loader.this, "There are no email clients installed", Toast.LENGTH_LONG).show();
							        	   }
							        	   Loader.this.finish();
							           }
							       })
							       .setNegativeButton("Manually", new DialogInterface.OnClickListener() {
							           public void onClick(DialogInterface dialog, int id) {
							        	   Intent i = new Intent(Loader.this, Manual.class);
							    	       startActivityForResult(i, 0);
							    	       finish();
							           }
							       }).show();
							return false;
						}
					}
				}
			}
		}
		} else {
			//We don't need to set variables every time so this is if they are already set.
			setupemmc(context, useemmc);
			superThread threadActivity = new superThread();
			threadActivity.start();
			return true;
			
		}
		return false;
		
	}
	
	public void setupemmc(Context context, Boolean useemmc) {
        if(useemmc){
        u = new Utilities();
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
    	}
    }
			
	private void getlibs(){
		String board=settings.getString("device", "");
		File hidden = new File(u.getExternalDirectory()+"/BootManager/.zips");
		File l1 = new File(u.getExternalDirectory()+"/BootManager/.zips/libext2_blkid.so");
		File l2 = new File(u.getExternalDirectory()+"/BootManager/.zips/libext2_com_err.so");
		File l3 = new File(u.getExternalDirectory()+"/BootManager/.zips/libext2_e2p.so");
		File l4 = new File(u.getExternalDirectory()+"/BootManager/.zips/libext2_profile.so");
		File l5 = new File(u.getExternalDirectory()+"/BootManager/.zips/libext2_uuid.so");
		File l6 = new File(u.getExternalDirectory()+"/BootManager/.zips/libext2fs.so");
		try {
			PackageInfo pInfo;
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			File bm = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager" + version + ".apk");
			if(!bm.exists()){
				u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox rm "+u.getExternalDirectory()+"/BootManager/.zips/BootManager*.apk");
				u.downloadUtil(context, u.BASE_URL + "files/BootManager.apk", u.getExternalDirectory()+"/BootManager/.zips/BootManager" + version + ".apk");
				if(!bm.exists()){
					u.downloadUtil2(context, u.BASE_URL + "files/BootManager.apk", u.getExternalDirectory()+"/BootManager/.zips/BootManager" + version + ".apk");
				}
				progress(progress+1);
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if(!hidden.exists()){
			hidden.mkdirs();
		}
		if(!l1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/libs/libext2_blkid.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_blkid.so");
		if(!l1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/libs/libext2_blkid.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_blkid.so");
		progress(progress+1);
		if(!l2.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/libs/libext2_com_err.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_com_err.so");
		if(!l2.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/libs/libext2_com_err.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_com_err.so");
		progress(progress+1);
		if(!l3.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/libs/libext2_e2p.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_e2p.so");
		if(!l3.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/libs/libext2_e2p.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_e2p.so");
		progress(progress+1);
		if(!l4.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/libs/libext2_profile.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_profile.so");
		if(!l4.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/libs/libext2_profile.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_profile.so");
		progress(progress+1);
		if(!l5.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/libs/libext2_uuid.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_uuid.so");
		if(!l5.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/libs/libext2_uuid.so", u.getExternalDirectory()+"/BootManager/.zips/libext2_uuid.so");
		progress(progress+1);
		if(!l6.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/libs/libext2fs.so", u.getExternalDirectory()+"/BootManager/.zips/libext2fs.so");
		if(!l6.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/libs/libext2fs.so", u.getExternalDirectory()+"/BootManager/.zips/libext2fs.so");
		progress(progress+1);
		if(board.equals("aloha")){
			File mke2fs = new File(u.getExternalDirectory()+"/BootManager/.zips/mke2fs");
			if(!mke2fs.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/libs/mke2fs", u.getExternalDirectory()+"/BootManager/.zips/mke2fs");
			if(!mke2fs.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/libs/mke2fs", u.getExternalDirectory()+"/BootManager/.zips/mke2fs");
			progress(progress+1);
		}
	}
	
	private void getsholes(){
		String board=settings.getString("device", "");
		if(board.equals("sholes")){
			File hidden = new File(u.getExternalDirectory()+"/BootManager/.zips");
			File s1 = new File(u.getExternalDirectory()+"/BootManager/.zips/boot.img-zImage");
			File s2 = new File(u.getExternalDirectory()+"/BootManager/.zips/etcwifi.zip");
			File s3 = new File(u.getExternalDirectory()+"/BootManager/.zips/ext2.ko");
			File s4 = new File(u.getExternalDirectory()+"/BootManager/.zips/fw_wlan1271.bin");
			File s5 = new File(u.getExternalDirectory()+"/BootManager/.zips/mbcache.ko");
			File s6 = new File(u.getExternalDirectory()+"/BootManager/.zips/modules.zip");
			File s7 = new File(u.getExternalDirectory()+"/BootManager/.zips/tiwlan_drv.ko");
			if(!hidden.exists()){
				hidden.mkdirs();
			}
			if(!s1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/boot.img-zImage", u.getExternalDirectory()+"/BootManager/.zips/boot.img-zImage");
			if(!s1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/boot.img-zImage", u.getExternalDirectory()+"/BootManager/.zips/boot.img-zImage");
			progress(progress+1);
			if(!s2.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/etcwifi.zip", u.getExternalDirectory()+"/BootManager/.zips/etcwifi.zip");
			if(!s2.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/etcwifi.zip", u.getExternalDirectory()+"/BootManager/.zips/etcwifi.zip");
			progress(progress+1);
			if(!s3.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/ext2.ko", u.getExternalDirectory()+"/BootManager/.zips/ext2.ko");
			if(!s3.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/ext2.ko", u.getExternalDirectory()+"/BootManager/.zips/ext2.ko");
			progress(progress+1);
			if(!s4.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/fw_wlan1271.bin", u.getExternalDirectory()+"/BootManager/.zips/fw_wlan1271.bin");
			if(!s4.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/fw_wlan1271.bin", u.getExternalDirectory()+"/BootManager/.zips/fw_wlan1271.bin");
			progress(progress+1);
			if(!s5.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/mbcache.ko", u.getExternalDirectory()+"/BootManager/.zips/mbcache.ko");
			if(!s5.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/mbcache.ko", u.getExternalDirectory()+"/BootManager/.zips/mbcache.ko");
			progress(progress+1);
			if(!s6.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/modules.zip", u.getExternalDirectory()+"/BootManager/.zips/modules.zip");
			if(!s6.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/modules.zip", u.getExternalDirectory()+"/BootManager/.zips/modules.zip");
			progress(progress+1);
			if(!s7.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/tiwlan_drv.ko", u.getExternalDirectory()+"/BootManager/.zips/tiwlan_drv.ko");
			if(!s7.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/tiwlan_drv.ko", u.getExternalDirectory()+"/BootManager/.zips/tiwlan_drv.ko");
			progress(progress+1);
		}
	}
	
	private void getupdate(){
		String board=settings.getString("device", "");
		File hidden = new File(u.getExternalDirectory()+"/BootManager/.zips");
		File u1 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip");
		File u2 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2-signed.zip");
		File u3 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3-signed.zip");
		File u4 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4-signed.zip");
		File pr = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip");
		if(!hidden.exists()){
			hidden.mkdirs();
		}
		//TODO add hash codes devices note the check binarys line
		if(board.equalsIgnoreCase("spyder")||board.equalsIgnoreCase("solana")||board.equalsIgnoreCase("targa")||board.equalsIgnoreCase("maserati")){
			if(!pr.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-EXTphoneRom-signed.zip", pr.toString());
			if(!pr.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-EXTphoneRom-signed.zip", pr.toString());
			//if(!u.checkbinarys(pr.toString(), "ea333ded6eeeb4a547b99c19a3fa2e5e"))u.execCommand("rm "+pr.toString());
			progress(progress+1);
			if(Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").contains("external")){
				if(!u1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-EXTrom1-signed.zip", u1.toString());
				if(!u1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-EXTrom1-signed.zip", u1.toString());
				//if(!u.checkbinarys(u1.toString(), "6f05fba9efa6484e4625ccc5ed045150"))u.execCommand("rm "+u1.toString());
				progress(progress+1);
			}else{
				if(!u1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom1-signed.zip", u1.toString());
				if(!u1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom1-signed.zip", u1.toString());
				//if(!u.checkbinarys(u1.toString(), "d0c4e84a4ab3317180b48efe98c7c4b2"))u.execCommand("rm "+u1.toString());
				progress(progress+1);
			}
		}else if(board.equalsIgnoreCase("inc")){
			File e1 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1EMMC-signed.zip");
			File e2 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2EMMC-signed.zip");
			File e3 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3EMMC-signed.zip");
			File e4 = new File(u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4EMMC-signed.zip");
			if(!e1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom1EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1EMMC-signed.zip");
			if(!e1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom1EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1EMMC-signed.zip");
			progress(progress+1);
			if(!e2.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom2EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2EMMC-signed.zip");
			if(!e2.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom2EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2EMMC-signed.zip");
			progress(progress+1);
			if(!e3.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom3EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3EMMC-signed.zip");
			if(!e3.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom3EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3EMMC-signed.zip");
			progress(progress+1);
			if(!e4.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom4EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4EMMC-signed.zip");
			if(!e4.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom4EMMC-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4EMMC-signed.zip");
			progress(progress+1);
		}
		if(board.equalsIgnoreCase("aloha")||board.equalsIgnoreCase("buzz")||board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.equals("olympus")||board.equals("triumph")||board.equals("tuna")){
			if(!u1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom1-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip");
			if(!u1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom1-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip");
			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")){
				if(!u.checkbinarys(u1.toString(), "aa21ae224b3a85ae7a2ffd08fa295121"))u.execCommand("rm "+u1.toString());else progress(progress+1);
			}else{
				progress(progress+1);
			}
			if(!u2.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom2-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2-signed.zip");
			if(!u2.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom2-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2-signed.zip");
			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")){
				if(!u.checkbinarys(u2.toString(), "fe70e4a6be45dbd5187389e2532a2b4e"))u.execCommand("rm "+u2.toString());else progress(progress+1);
			} else {
				progress(progress+1);
			}
			if(!u3.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom3-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3-signed.zip");
			if(!u3.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom3-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3-signed.zip");
			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")){
				if(!u.checkbinarys(u3.toString(), "7cc79274c03c00401d0f470ca1c73db3"))u.execCommand("rm "+u3.toString());else progress(progress+1);
			} else {
				progress(progress+1);
			}
			if(!u4.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-rom4-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4-signed.zip");
			if(!u4.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-rom4-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4-signed.zip");
			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")){
				if(!u.checkbinarys(u4.toString(), "63a840fbdd789693bd404e6e9c270094"))u.execCommand("rm "+u4.toString());else progress(progress+1);
			} else {
				progress(progress+1);
			}
			if(!pr.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/BootManager-phoneRom-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip");
			if(!pr.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/BootManager-phoneRom-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip");
			if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")){
				if(!u.checkbinarys(pr.toString(), "eb45107f1dd2ce8a5fa02e32ee37bad1"))u.execCommand("rm "+pr.toString());else progress(progress+1);
			}else{
				progress(progress+1);
			}
		} else if(board.equals("vigor")){
			File bootflasher = new File(u.getExternalDirectory()+"/BootManager/.zips/BootImageFlasherRezound.zip");
			if(!bootflasher.exists())u.downloadUtil(context, u.BASE_URL+"devices/"+board+"/BootImageFlasherRezound.zip", u.getExternalDirectory()+"/BootManager/.zips/BootImageFlasherRezound.zip");
			if(!bootflasher.exists())u.downloadUtil2(context, u.BASE_URL+"devices/"+board+"/BootImageFlasherRezound.zip", u.getExternalDirectory()+"/BootManager/.zips/BootImageFlasherRezound.zip");
			progress(progress+1);
		} else if(!board.equals("spyder")||board.equals("maserati")||board.equals("targa")||board.equals("solana")){
			if(!u1.exists())u.downloadUtil(context, u.BASE_URL + "files/BootManager-rom1-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip");
			if(!u1.exists())u.downloadUtil2(context, u.BASE_URL + "files/BootManager-rom1-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom1-signed.zip");
			progress(progress+1);
			if(!u2.exists())u.downloadUtil(context, u.BASE_URL + "files/BootManager-rom2-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2-signed.zip");
			if(!u2.exists())u.downloadUtil2(context, u.BASE_URL + "files/BootManager-rom2-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom2-signed.zip");
			progress(progress+1);
			if(!u3.exists())u.downloadUtil(context, u.BASE_URL + "files/BootManager-rom3-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3-signed.zip");
			if(!u3.exists())u.downloadUtil2(context, u.BASE_URL + "files/BootManager-rom3-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom3-signed.zip");
			progress(progress+1);
			if(!u4.exists())u.downloadUtil(context, u.BASE_URL + "files/BootManager-rom4-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4-signed.zip");
			if(!u4.exists())u.downloadUtil2(context, u.BASE_URL + "files/BootManager-rom4-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-rom4-signed.zip");
			progress(progress+1);
			if(!pr.exists())u.downloadUtil(context, u.BASE_URL + "files/BootManager-phoneRom-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip");
			if(!pr.exists())u.downloadUtil2(context, u.BASE_URL + "files/BootManager-phoneRom-signed.zip", u.getExternalDirectory()+"/BootManager/.zips/BootManager-phoneRom-signed.zip");
			progress(progress+1);
		}	
	}
	
	private void gethijack(){
		String board=settings.getString("device", "");
		if(board.equals("shadow")||board.equals("droid2")||board.equals("droid2we")||board.equals("spyder")||board.equals("maserati")||board.equals("targa")||board.equals("solana")){			
			File hidden = new File(u.getExternalDirectory()+"/BootManager/.zips");
			File h1 = new File(u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-rom1.zip");
			File h2 = new File(u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-rom2.zip");
			File h3 = new File(u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-rom3.zip");
			File h4 = new File(u.getExternalDirectory()+"/BootManager/.zips/hijack-boot-rom4.zip");
			File r1 = new File(u.getExternalDirectory()+"/BootManager/.zips/recovery_mode");
			File mConf = new File(u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf");
			if(!hidden.exists()){
				hidden.mkdirs();
			}
			if(board.equals("spyder")||board.equalsIgnoreCase("solana")||board.equalsIgnoreCase("targa")||board.equalsIgnoreCase("maserati")){
				//TODO add hash codes devices
				//TODO after adding mount points to hijacks uncomment the check binaries and add md5sum to them
				if(Utilities.readFirstLineOfFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").contains("external")){
					if(!h1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/hijack-boot-EXTrom1.zip", h1.toString());
					if(!h1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/hijack-boot-EXTrom1.zip", h1.toString());
					/**if(!u.checkbinarys(h1.toString(), "ec6f459a00cacc460210010fa7fe95fc"))
						u.execCommand("rm "+h1.toString());
					else*/
						progress(progress+1);
				}else{
					if(!h1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/hijack-boot-rom1.zip", h1.toString());
					if(!h1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/hijack-boot-rom1.zip", h1.toString());
					/**if(!u.checkbinarys(h1.toString(), "0aed2b9c4356ab7f79de14f541789553"))
						u.execCommand("rm "+h1.toString());
					else*/ 
						progress(progress+1);
				}
			}else{
				if(!h1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/hijack-boot-rom1.zip", h1.toString());
				if(!h1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/hijack-boot-rom1.zip", h1.toString());
			}
			if(board.equals("shadow")){
				if(!u.checkbinarys(h1.toString(), "cff6fcd4296b578ead536e223e8135e1"))u.execCommand("rm "+h1.toString());else progress(progress+1);
			}
			if(board.equals("droid2")){
				if(!u.checkbinarys(h1.toString(), "655f25343032de271a39cd42a84fc48e"))u.execCommand("rm "+h1.toString());else progress(progress+1);
			}
			if(board.equals("droid2we")){
				if(!u.checkbinarys(h1.toString(), "207abfc29c526f541a9d9d7cb5a462d2"))u.execCommand("rm "+h1.toString());else progress(progress+1);
			}
			if(board.equals("shadow")){
				if(!u.checkbinarys(h2.toString(), "54641a4cb522994d4a42d50ae139e269"))u.execCommand("rm "+h2.toString());else progress(progress+1);
			}
			if(board.equals("droid2")){
				if(!u.checkbinarys(h2.toString(), "ffafe4fbdd428945945543b008f6aa7a"))u.execCommand("rm "+h2.toString());else progress(progress+1);
			}
			if(board.equals("droid2we")){
				if(!u.checkbinarys(h2.toString(), "c404bbd3ddfaedf70a306c3472e8f8ae"))u.execCommand("rm "+h2.toString());else progress(progress+1);
			}
			if(board.equals("shadow")){
				if(!u.checkbinarys(h3.toString(), "d073a87768358a197b2259702bf0cfa8"))u.execCommand("rm "+h3.toString());else progress(progress+1);
			}
			if(board.equals("droid2")){
				if(!u.checkbinarys(h3.toString(), "e3f48324111f6b6cc528025dd1bac0c3"))u.execCommand("rm "+h3.toString());else progress(progress+1);
			}
			if(board.equals("droid2we")){
				if(!u.checkbinarys(h3.toString(), "0f53280362e83a9090b546fc2b3b9653"))u.execCommand("rm "+h3.toString());else progress(progress+1);
			}
			if(board.equals("shadow")){
				if(!u.checkbinarys(h4.toString(), "1d86c9b58ce2752b77505941fe17f5dc"))u.execCommand("rm "+h4.toString());else progress(progress+1);
			}
			if(board.equals("droid2")){
				if(!u.checkbinarys(h4.toString(), "0569fa3e5a637364ea8b314f5dc3f2e7"))u.execCommand("rm "+h4.toString());else progress(progress+1);
			}
			if(board.equals("droid2we")){
				if(!u.checkbinarys(h4.toString(), "74011cdea5824cbb996a125cf7f23883"))u.execCommand("rm "+h4.toString());else progress(progress+1);
			}
			if(!r1.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/recovery_mode", u.getExternalDirectory()+"/BootManager/.zips/recovery_mode");
			if(!r1.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/recovery_mode", u.getExternalDirectory()+"/BootManager/.zips/recovery_mode");
			if(!mConf.exists())u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/mke2fs.conf", u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf");
			if(!mConf.exists())u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/mke2fs.conf", u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf");
			if(!u.checkbinarys(mConf.toString(), "33c2a4bda46c7cee498bb660084492dd"))u.execCommand("rm "+mConf.toString());
		} 
	}
	
	private void getFiles() {
		u.log("getting files");
		int count = 1;
		//File files = new File(context.getFilesDir().getAbsolutePath() + "/files.zip");
		File mbs = new File(context.getFilesDir().getAbsoluteFile() + "/morebinarys");
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
		File mConf = new File(u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf");
		File rz = new File(context.getFilesDir().getAbsolutePath() + "/resize2fs");
		if(!mbs.exists()){
			mbs.mkdirs();
		}
		while(!rz.exists() || !mConf.exists() || !mbb.exists() || !mfi.exists() || !mdi.exists() || !up.exists() || !mb.exists() || !e2.exists() || !m22.exists() || !bb.exists() || !di.exists() || !ei.exists() || !fi.exists() || !za.exists() || !rb.exists() || !fp.exists() || !m2.exists() || !lw.exists() || !uy.exists()){
			count ++;
			if(count > 5){
				u.log("Binary files did not get downloaded");
				break;
			} else {
				String board=settings.getString("device", "");
				if(!bb.exists()){
					if(board.equalsIgnoreCase("tuna")){
						if(!bb.exists()) u.downloadUtil(context, u.BASE_URL + "devices/"+board+"/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
						if(!bb.exists()) u.downloadUtil2(context, u.BASE_URL + "devices/"+board+"/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
						if(!u.checkbinarys(bb.toString(), "92cd913a325866a8d7a03044a19ed1f0"))u.execCommand("rm "+bb.toString());else progress(progress+1);
					} else {
						if(!bb.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
						if(!bb.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/busybox", context.getFilesDir().getAbsolutePath() + "/busybox");
						if(!u.checkbinarys(bb.toString(), "94e5efab5f0115baab91376ebfb3ad98"))u.execCommand("rm "+bb.toString());else progress(progress+1);
					}
				}
				if(!di.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/dump_image", context.getFilesDir().getAbsolutePath() + "/dump_image");
				if(!di.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/dump_image", context.getFilesDir().getAbsolutePath() + "/dump_image");
				if(!u.checkbinarys(di.toString(), "34bab4b698354ae4254e14e3b27494de"))u.execCommand("rm "+di.toString());else progress(progress+1);
				if(!ei.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/erase_image", context.getFilesDir().getAbsolutePath() + "/erase_image");
				if(!ei.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/erase_image", context.getFilesDir().getAbsolutePath() + "/erase_image");
				if(!u.checkbinarys(ei.toString(), "37d986fb974d0ac20b1167e85b1f5198"))u.execCommand("rm "+ei.toString());else progress(progress+1);
				if(!fi.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/flash_image", context.getFilesDir().getAbsolutePath() + "/flash_image");
				if(!fi.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/flash_image", context.getFilesDir().getAbsolutePath() + "/flash_image");
				if(!u.checkbinarys(fi.toString(), "88b8bd756baf5553e8bc93bb41e4854c"))u.execCommand("rm "+fi.toString());else progress(progress+1);
				if(!za.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/zipalign", context.getFilesDir().getAbsolutePath() + "/zipalign");
				if(!za.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/zipalign", context.getFilesDir().getAbsolutePath() + "/zipalign");
				if(!u.checkbinarys(za.toString(), "fb0afb44d706849fe68da1e042ff9b12"))u.execCommand("rm "+za.toString());else progress(progress+1);
				if(!rb.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/reboot", context.getFilesDir().getAbsolutePath() + "/reboot");
				if(!rb.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/reboot", context.getFilesDir().getAbsolutePath() + "/reboot");
				if(!u.checkbinarys(rb.toString(), "ace70a2ce9288da6a96ec38b7d295903"))u.execCommand("rm "+rb.toString());else progress(progress+1);
				if(!fp.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/fix_permissions", context.getFilesDir().getAbsolutePath() + "/fix_permissions");
				if(!fp.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/fix_permissions", context.getFilesDir().getAbsolutePath() + "/fix_permissions");
				if(!u.checkbinarys(fp.toString(), "82c12aa915a3fd427daccd57e815f0fd"))u.execCommand("rm "+fp.toString());else progress(progress+1);
				if(!m2.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/mke2fs", context.getFilesDir().getAbsolutePath() + "/mke2fs");
				if(!m2.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/mke2fs", context.getFilesDir().getAbsolutePath() + "/mke2fs");
				if(!u.checkbinarys(m2.toString(), "e0c2e5c684120fdbce43a4bfff4f75cd"))u.execCommand("rm "+m2.toString());else progress(progress+1);
				if(!m22.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/morebinarys/mke2fs", context.getFilesDir().getAbsolutePath() + "/morebinarys/mke2fs");
				if(!m22.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/morebinarys/mke2fs", context.getFilesDir().getAbsolutePath() + "/morebinarys/mke2fs");
				if(!u.checkbinarys(m22.toString(), "1a79311e730a905ae01058e702264e21"))u.execCommand("rm "+m22.toString());else progress(progress+1);
				if(!mbb.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/morebinarys/busybox", context.getFilesDir().getAbsolutePath() + "/morebinarys/busybox");
				if(!mbb.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/morebinarys/busybox", context.getFilesDir().getAbsolutePath() + "/morebinarys/busybox");
				if(!u.checkbinarys(mbb.toString(), "a8e20a66939a26dfb524dc7844b1fde9"))u.execCommand("rm "+mbb.toString());else progress(progress+1);
				if(!mfi.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/morebinarys/flash_image", context.getFilesDir().getAbsolutePath() + "/morebinarys/flash_image");
				if(!mfi.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/morebinarys/flash_image", context.getFilesDir().getAbsolutePath() + "/morebinarys/flash_image");
				if(!u.checkbinarys(mfi.toString(), "8385b77affc8ab4da04a3a0542952c99"))u.execCommand("rm "+mfi.toString());else progress(progress+1);
				if(!mdi.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/morebinarys/dump_image", context.getFilesDir().getAbsolutePath() + "/morebinarys/dump_image");
				if(!mdi.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/morebinarys/dump_image", context.getFilesDir().getAbsolutePath() + "/morebinarys/dump_image");
				if(!u.checkbinarys(mdi.toString(), "d0dd8557992390c809b50e1108c99b8f"))u.execCommand("rm "+mdi.toString());else progress(progress+1);
				if(!lw.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/zip", context.getFilesDir().getAbsolutePath() + "/zip");
				if(!lw.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/zip", context.getFilesDir().getAbsolutePath() + "/zip");
				if(!u.checkbinarys(lw.toString(), "520b956745931bf071ed53538563a58c"))u.execCommand("rm "+lw.toString());else progress(progress+1);
				if(!uy.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/unyaffs", context.getFilesDir().getAbsolutePath() + "/unyaffs");
				if(!uy.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/unyaffs", context.getFilesDir().getAbsolutePath() + "/unyaffs");
				if(!u.checkbinarys(uy.toString(), "f67c3fe0bea02896a46286f0e45861fe"))u.execCommand("rm "+uy.toString());else progress(progress+1);
				if(!e2.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/e2fsck", context.getFilesDir().getAbsolutePath() + "/e2fsck");
				if(!e2.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/e2fsck", context.getFilesDir().getAbsolutePath() + "/e2fsck");
				if(!u.checkbinarys(e2.toString(), "89a4b7025dc3ba5a7bd0ee39ccf6a20f"))u.execCommand("rm "+e2.toString());else progress(progress+1);
				if(!up.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/unpackbootimg", context.getFilesDir().getAbsolutePath() + "/unpackbootimg");
				if(!up.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/unpackbootimg", context.getFilesDir().getAbsolutePath() + "/unpackbootimg");
				if(!u.checkbinarys(up.toString(), "c172ee99612b110c15cdca7afbfb2b9c"))u.execCommand("rm "+up.toString());else progress(progress+1);
				if(!mb.exists()) u.downloadUtil(context, u.BASE_URL + "files/binarys/mkbootimg", context.getFilesDir().getAbsolutePath() + "/mkbootimg");
				if(!mb.exists()) u.downloadUtil2(context, u.BASE_URL + "files/binarys/mkbootimg", context.getFilesDir().getAbsolutePath() + "/mkbootimg");
				if(!u.checkbinarys(mb.toString(), "b53b9ef4623db67a6da1f43de3aa0d63"))u.execCommand("rm "+mb.toString());else progress(progress+1);
				if(!mConf.exists())u.downloadUtil(context, u.BASE_URL + "files/binarys/mke2fs.conf", u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf");
				if(!mConf.exists())u.downloadUtil2(context, u.BASE_URL + "files/binarys/mke2fs.conf", u.getExternalDirectory()+"/BootManager/.zips/mke2fs.conf");
				if(!u.checkbinarys(mConf.toString(), "33c2a4bda46c7cee498bb660084492dd"))u.execCommand("rm "+mConf.toString());
				if(!rz.exists())u.downloadUtil(context, u.BASE_URL + "files/binarys/resize2fs", context.getFilesDir().getAbsolutePath() + "/resize2fs");
				if(!rz.exists())u.downloadUtil2(context, u.BASE_URL + "files/binarys/resize2fs", context.getFilesDir().getAbsolutePath() + "/resize2fs");
				if(!u.checkbinarys(rz.toString(), "0eea13b57dc4183240c696e34d894e65"))u.execCommand("rm "+rz.toString());
				if(!(mbb.exists() || mfi.exists() || mdi.exists() || up.exists() || mb.exists() || e2.exists() || m22.exists() || bb.exists() || di.exists() || ei.exists() || fi.exists() || za.exists() || rb.exists() || fp.exists() || m2.exists() || lw.exists() || uy.exists())){
					u.log("Binary files did not get downloaded Trying again....");
				}else {
					u.log("Binary files downloaded and checked");
					u.execCommand("chmod 755 " + context.getFilesDir().getAbsolutePath() + "/*");
					u.execCommand("chmod 755 " + context.getFilesDir().getAbsolutePath() + "/morebinarys/*");
					break;
					}
				}
			}
	}
	
	class tmpmessageTask extends AsyncTask<Void, Void, Void> {
		Context context;
        
		public tmpmessageTask(final Context context) {
			this.context=context;
			
        }

		@Override
		protected Void doInBackground(Void... arg0) {
			File tmpmes = new File(context.getFilesDir().getAbsolutePath() + "/tmpmes");
			File tmpmessage = new File(context.getFilesDir().getAbsolutePath() + "/tmpmessage");
			if(tmpmessage.exists()){
				u.downloadUtil(Loader.this, u.BASE_URL + "message", tmpmes.toString());
				File siz = new File(tmpmes.toString());
				long lengt = siz.length();
				if(lengt == 0){
					new File(context.getFilesDir().getAbsolutePath() + "/message").renameTo(new File(context.getFilesDir().getAbsolutePath() + "/tmpmessage"));
				}else{
					try {
						if(Utilities.getMD5(tmpmes.toString()).matches(Utilities.getMD5(tmpmessage.toString()))){
							tmpmes.delete();
						}else{
							tmpmessage.delete();
							tmpmes.renameTo(new File(context.getFilesDir().getAbsolutePath() + "/message"));
						}
					} catch (Exception e) {
						e.printStackTrace();
						u.log(e.toString());
					}
				}
			}else{	
				u.downloadUtil(Loader.this, u.BASE_URL + "message", context.getFilesDir().getAbsolutePath() + "/message");
				File size = new File(context.getFilesDir().getAbsolutePath() + "/message");
				long length = size.length();
				if(length == 0){
					new File(context.getFilesDir().getAbsolutePath() + "/message").renameTo(new File(context.getFilesDir().getAbsolutePath() + "/tmpmessage"));
				}
			}
			return null;
		}
		@Override 
		protected void onPostExecute(Void unused) {
		}       
    }
	
	private void messageCheck() {
		tmpmessageTask tmpT = new tmpmessageTask(context);
		tmpT.execute();
	}
	
	private void getassets(){
		u.log("extracting assets");
		if(!new File(context.getFilesDir().getAbsolutePath()+"/wget").exists()){
			try{
				InputStream in = getAssets().open("wget");
				OutputStream out = new FileOutputStream(context.getFilesDir().getAbsolutePath()+"/wget");
				byte[] buf = new byte[1024];
		    	int len;
		    	while ((len = in.read(buf)) > 0) {
		    		out.write(buf, 0, len);
		    	}
		    	in.close();
		    	out.close();
		    	ShellCommand cmd2 = new ShellCommand();
	        	@SuppressWarnings("unused")
				CommandResult r2 = cmd2.su.runWaitFor("chmod 777 "+context.getFilesDir().getAbsolutePath()+"/wget");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		if(!new File(context.getFilesDir().getAbsolutePath()+"/bootmanagerSS").exists()){
			try{
				InputStream in = getAssets().open("bootmanagerSS");
				OutputStream out = new FileOutputStream(context.getFilesDir().getAbsolutePath()+"/bootmanagerSS");
				byte[] buf = new byte[1024];
		    	int len;
		    	while ((len = in.read(buf)) > 0) {
		    		out.write(buf, 0, len);
		    	}
		    	in.close();
		    	out.close();
		    	ShellCommand cmd2 = new ShellCommand();
	        	@SuppressWarnings("unused")
				CommandResult r2 = cmd2.su.runWaitFor("chmod 777 "+context.getFilesDir().getAbsolutePath()+"/bootmanagerSS");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	
	private void sdSelectorDialog(){
		selection="internal";
		final CharSequence[] items = {"Internal Storage", "External Storage"};
      	CustomDialog.Builder builder = new CustomDialog.Builder(Loader.this);
      	builder.setTitle("Select Install Location");
      	builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
      		public void onClick(DialogInterface dialog, int item) {
      			if(item==0){
      				selection="internal";
				}
      			if(item==1){
      				selection="external";
      			}
      		}
      	})
      	.setPositiveButton("OK", new DialogInterface.OnClickListener() {
      		public void onClick(DialogInterface dialog, int id) {
      			SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor sharededitor = shared.edit();
				sharededitor.putString("sdselector", selection);
				sharededitor.commit();
				System.out.println(selection);
				if(!(new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager").exists())){
					new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager").mkdir();
				}
				if(!(writeToSDSelectorFile(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete", selection)))
					Toast.makeText(context, "Error saving storage selection. Please make sure sdcard is available.", Toast.LENGTH_LONG).show();
				if(Utilities.device().equals("spyder")||Utilities.device().contains("maserati")||Utilities.device().contains("targa")||Utilities.device().contains("solana") && selection.equals("external")){
					folder = new File(u.getExternalDirectory() + "/BootManager");
		    		rom1 = new File(folder + "/rom1");
		    		rom2 = new File(folder + "/rom2");
		    		rom3 = new File(folder + "/rom3");
		    		rom4 = new File(folder + "/rom4");
		    		rom1as = new File(folder + "/rom1/.android_secure");
		    		rom2as = new File(folder + "/rom2/.android_secure");
		    		rom3as = new File(folder + "/rom3/.android_secure");
		    		rom4as = new File(folder + "/rom4/.android_secure");
		    		phoneRom = new File(folder + "/phoneRom");
		    		backup = new File(folder + "/Backup");
		    		createFOLDS();
				}else if(Utilities.device().equals("spyder")||Utilities.device().contains("maserati")||Utilities.device().contains("targa")||Utilities.device().contains("solana")){
					createFOLDS();
				}
				dialog.cancel();
				superThread threadActivity = new superThread();
    			threadActivity.start();
      		}
      	}).show();
	}
	
	private String findlunfile(){
		try{
    		FileWriter fstream = new FileWriter(context.getFilesDir().getAbsolutePath() + "/command.sh");
    		BufferedWriter out = new BufferedWriter(fstream);
    		//make sure paths is write for busybox when adding to lite
    		out.write("#!/data/data/com.drx2.bootmanager/files/busybox sh\n#\n\n#Do not modify this file!\n\ncd /sys\n$1 find /sys/devices/platform `pwd` -name \"file\" | $1 grep \"usb\" | $1 grep \"lun0\" | $1 sed -n '1,0p'");
    		out.close();
    	}catch (Exception e){
    		u.log("Exception:"+e.toString());
    	}
    	u.execCommand(context.getFilesDir().getAbsolutePath()+"/busybox chmod 755 "+context.getFilesDir().getAbsolutePath()+"/command.sh");
    	u.execCommand("chmod 755 "+context.getFilesDir().getAbsolutePath()+"/command.sh");
    	CommandResult find = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/command.sh "+context.getFilesDir().getAbsolutePath()+"/busybox");
		if(find.stderr!=null){
			u.log(find.stderr); 
			new File(context.getFilesDir().getAbsolutePath() + "/command.sh").delete();
		}
    	if(find.stdout!=null)
			return find.stdout;
		else
			return null;
		
	}
	
	private void askHboot(){
		final SharedPreferences.Editor editor = settings.edit();
		SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor sharededitor = shared.edit();
		CustomDialog.Builder builder = new CustomDialog.Builder(Loader.this);
      	builder.setTitle("Hboot Version");
      	builder.setMessage("Do you have the new Hboot 2.21 or the Hboot 2.11 or older? If you choose wrong you can change your prefference in BootManager settings at any time. You can only install rom's made for your hboot version.");
      	builder.setPositiveButton("Older Hboot", new DialogInterface.OnClickListener() {
      		public void onClick(DialogInterface dialog, int id) {
      			editor.putString("sdcard", "/dev/block/mmcblk0p37");
      			sharededitor.putBoolean("hboot", false);
      			editor.commit();
      			sharededitor.commit();
      			if(new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").exists()){
					superThread threadActivity = new superThread();
	    			threadActivity.start();
				} else {
					sdSelectorDialog();
				}
      		}
      	})
      	.setNegativeButton("Hboot 2.21", new DialogInterface.OnClickListener() {
      		public void onClick(DialogInterface dialog, int id) {
      			editor.putString("sdcard", "/dev/block/mmcblk0p38");
      			sharededitor.putBoolean("hboot", true);
      			editor.commit();
      			sharededitor.commit();
      			if(new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").exists()){
					superThread threadActivity = new superThread();
	    			threadActivity.start();
				} else {
					sdSelectorDialog();
				}
      		}
      	}).show();
	}
	
	private boolean writeToSDSelectorFile(String filename, String value) {
	     try {
			FileWriter f = new FileWriter(new File(filename));
			f.write(value+"\n");
			f.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		 if(Utilities.readFirstLineOfFile(filename).contains(value)){
	    	 return true;
	     }
		 return false;
	 }
	
	private void warning2ndInitdialog(final String board) {
	final SpannableString s = new SpannableString("BootManager requires you to use a 2nd Init rom with GB kernel as your base phone rom. Non 2ndInit rom's that use the GB kernel can be run from the sdcard. If you don't know what 2ndInit is please don't use this app. Using a non 2ndInit rom as your phone rom with BootManager will most likely cause you to need to sbf your phone. Please visit http://forum.init2winitapps.com/viewtopic.php?f=7&t=116 for more information.");
    final TextView tx1=new TextView(this);
    tx1.setText(s);
    tx1.setAutoLinkMask(RESULT_OK);
    tx1.setMovementMethod(LinkMovementMethod.getInstance());
    Linkify.addLinks(s, Linkify.WEB_URLS);
	CustomDialog.Builder builder = new CustomDialog.Builder(Loader.this);
	builder.setTitle("Warning 2ndInit Devices!")
		   .setCancelable(false)
		   //.setIcon(R.drawable.icon, true)
	       .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean("varset", true);
					editor.commit();
					if(board.equals("spyder")||board.equals("maserati")||board.equals("targa")||board.equals("solana")){
						if(new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/BootManager/DoNotDelete").exists()){
							superThread threadActivity = new superThread();
			    			threadActivity.start();
						} else {
							sdSelectorDialog();
						}
					}else{
						superThread threadActivity = new superThread();
						threadActivity.start();
					}
	           }
	       })
	       .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	                Loader.this.finish();
	           }
	       }).setView(tx1, false).show();
	}
	
	private void createFOLDS(){
        if(!(folder.exists())){
        	folder.mkdir();
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
        if(!(rom1as.exists())){
        	rom1as.mkdir();
        }
        if(!(rom2as.exists())){
        	rom2as.mkdir();
        }
        if(!(rom3as.exists())){
        	rom3as.mkdir();
        }
        if(!(rom4as.exists())){
        	rom4as.mkdir();
        }
        if(Utilities.device().equals("spyder")||Utilities.device().contains("maserati")||Utilities.device().contains("targa")||Utilities.device().contains("solana")){
        	if(!(new File(Environment.getExternalStorageDirectory() + "-ext/BootManager/phoneRom").exists())){
        		new File(Environment.getExternalStorageDirectory() + "-ext/BootManager").mkdir();
        		new File(Environment.getExternalStorageDirectory() + "-ext/BootManager/phoneRom").mkdir();
        	}
        }else{
        	if(!(phoneRom.exists())){
        		phoneRom.mkdir();
        	}
        }
        if(!(backup.exists())){
        	backup.mkdir();
        }
	}
		
	private void setUp(){
		runOnUiThread(new Runnable() {
		    public void run() {
				variablesset=settings.getBoolean("varset", false);
				useemmc=settings.getBoolean("useemmc", false);
				String Version = new String(settings.getString("Version", ""));
				   try {
		   			   	PackageInfo pInfo;
		   			   	pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		   			   	String version = pInfo.versionName;
		   			   	u.log("Current Version Boot Manager V" + version);
		   		    if(!(Version.equals("V" + version))){
		   		    	firstuse=true;
		   	            final SpannableString s = new SpannableString("Warning this app has the potential to brick your device as does " +
		   		    		   		"any modification you make to your phone. We are not responsible for any " +
		   		    		   		"damage done to your device use this at your own risk just as any other " + 
		   		    		   		"mod/hack.\n\nPlease send any questions or problems to support@init2winitapps.com\n\n");
		   	            final TextView tx1=new TextView(Loader.this);
		   	            tx1.setText(s);
		   	            tx1.setAutoLinkMask(RESULT_OK);
		   	            tx1.setMovementMethod(LinkMovementMethod.getInstance());
		   	            Linkify.addLinks(s, Linkify.EMAIL_ADDRESSES);
		   		    	CustomDialog.Builder builder = new CustomDialog.Builder(Loader.this);
		   		    	builder.setTitle("Warning!")
		   		    	       .setCancelable(false)
		   		    	       .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
		   		    	           public void onClick(DialogInterface dialog, int id) {
		   		    	       		   SharedPreferences.Editor editor = settings.edit();
		   		    	       		   try {
		   		    	       			   PackageInfo pInfo;
		   		    	       			   pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		   		    	       			   String version = pInfo.versionName;
		   		    	       			   editor.putString("Version", "V" + version);
		   			    	    		   editor.commit();
		   		    	       		   }catch (NameNotFoundException e) {
		   		    	       			   e.printStackTrace();
		   		    	       		   }
		   		    	    		   getDevice();
		   		    	           }
		   		    	       })
		   		    	       .setNegativeButton("Decline", new DialogInterface.OnClickListener() {
		   		    	           public void onClick(DialogInterface dialog, int id) {
		   		    	                Loader.this.finish();
		   		    	           }
		   		    	       }).setView(tx1, false).show();
		   		    }else{
		   		    	getDevice();
		   				messageCheck();
		   		    }
				   }catch (NameNotFoundException e) {
		   			   e.printStackTrace();
		   		   }
				   SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
				   if(!(Utilities.device().equals("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana"))){
					   createFOLDS();
				   }else if(shared.getString("sdselector", "") != "" && Utilities.device().equals("spyder")||Utilities.device().equals("maserati")||Utilities.device().equals("targa")||Utilities.device().equals("solana")){
					   createFOLDS();
				   }
		    }
		});
	}
}