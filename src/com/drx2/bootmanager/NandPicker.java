package com.drx2.bootmanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.drx2.bootmanager.services.NandRestoreService;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.ShellCommand.CommandResult;
import com.drx2.bootmanager.utilities.Utilities;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class NandPicker extends ListActivity {
	 
	ShellCommand s = new ShellCommand();
	Utilities u = new Utilities();
	private List<String> item = null;
	private List<String> path = null;
	private String root = Environment.getExternalStorageDirectory().getAbsoluteFile() + "/";
	private String current = null;
	private String slot;
	private String romshort;
	@SuppressWarnings("unused")
	private String installtype;
	Button installbutton;
	private static final String PREFS_DEVICE = "DeviceInfo";
	String systemsize;
	String datasize;
	String cachesize;
	String ext = "ext2";
	private String nandroid;
	Context context;
	SharedPreferences colors;
	SharedPreferences shared;
		
    /** Called when the activity is first created. */
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
        setContentView(R.layout.restore);
        colors = getSharedPreferences(PREFS_DEVICE, 0);
        installbutton=(Button)findViewById(R.id.installbutton);
        Bundle extras = getIntent().getExtras();
        slot = extras.getString("slot");
        LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
		mainLayout.startAnimation(slideDown);
        mainLayout.setVisibility(LinearLayout.GONE);
        try{
        	getDir(root);
        	File directory = new File(root);
        	File[] contents = directory.listFiles();
        	if (contents.length == 0) {
        		TextView t = new TextView(this); 
        		t=(TextView)findViewById(R.id.empty); 
        		t.setText(R.string.emptyFol);
        	}
        }catch (Exception e) {
        	Toast.makeText(NandPicker.this, R.string.nMount, Toast.LENGTH_LONG).show();
        }
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    actionBar.setTitle(current);
	    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    LinearLayout l = (LinearLayout) findViewById(R.id.bottombar);
        int startcolor = colors.getInt("buttonStart", context.getResources().getColor(R.color.buttonStart));
		int endcolor = colors.getInt("buttonEnd", context.getResources().getColor(R.color.buttonEnd));
		int[] color = {startcolor, endcolor};
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
		l.setBackgroundDrawable(d);
	    installbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				if(new File(u.getExternalDirectory() + "/BootManager/" + slot+ "/system.img").exists()){
							OWDialog(slot);
						}else{
							spaceCheck(slot);	
				}
			}
    	});
    }
	    
    private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
		        getDir(root);
	    }
	}
	    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (findViewById(R.id.bottombar).isShown()){
        		LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
        		Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
	  			mainLayout.startAnimation(slideDown);
  		      	mainLayout.setVisibility(LinearLayout.GONE);
  		      	ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	  			actionBar.setTitle(current);
			}else{
				if(!(current.equals("/mnt/sdcard") || current.equals(root))){
	        		File cur = new File(current);
	        		getDir(cur.getParentFile().toString());
	        	}else{
	        		NandPicker.this.finish();
	        		overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	        	}	  
			}
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	    
    private void getDir(String dirPath){
    	item = new ArrayList<String>();
    	path = new ArrayList<String>();
     
    	File f = new File(dirPath);
    	File[] files = f.listFiles();
     
    	for(int i=0; i < files.length; i++){
    		File file = files[i];
    		//Problem was adding the path without the /
    		//path.add(file.getPath());
    		if(file.isDirectory()){
    			path.add(file.getPath()+"/");
    			item.add(file.getName() + "/");
    		}else if(file.getName().endsWith("zip")){
    			path.add(file.getPath());
    			item.add(file.getName());
    		}else{
    			//do nothing
    		}
    	}
	     
	    current = dirPath;
	     
	    ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(current);
		if(!(current.equals("/mnt/sdcard") || current.equals(root))){
			actionBar.removeAllActions();
			actionBar.addAction(new Home(), colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));	
     	}else{
     		actionBar.removeAllActions();
     	}
		
		ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
		setListAdapter(fileList);
		class IgnoreCaseComparator implements Comparator<String> {
			public int compare(String strA, String strB) {
				return strA.compareToIgnoreCase(strB);
			}
		}
		IgnoreCaseComparator icc = new IgnoreCaseComparator();
		java.util.Collections.sort(path,icc);
		java.util.Collections.sort(item,icc);
	}

	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {
		final File file = new File(path.get(position));
		if (!file.isDirectory())
		  {
			  Toast.makeText(NandPicker.this, "Error: Not A Nandroid!", Toast.LENGTH_LONG).show();
		  } else {
			  if(file.canRead()){
				  if(new File(file + "/recovery.img").exists() || new File(file + "/system.ext3.win").exists() || new File(file + "/system.ext4.win").exists() || new File(file + "/system.ext2.win").exists() || new File(file + "/system.win").exists() || new File(file + "/nandroid.md5").exists()){
					  	LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
						Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
			  			mainLayout.startAnimation(slideUp);
						mainLayout.setVisibility(LinearLayout.VISIBLE);
						nandroid = file.getAbsolutePath();
						romshort = file.getName();
						ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
			  		  	actionBar.setTitle("Restore " + romshort + "to "+slot+"?");
				  }else{
			  			getDir(path.get(position));
				  }
					  /*CustomDialog.Builder builder = new CustomDialog.Builder(this);
					   builder.setMessage("Restore " + " Nandroid?")
					          .setCancelable(false)
					          .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
					              public void onClick(DialogInterface dialog, int id) {
					            	  	nandroid=file.getAbsolutePath();
					            	  	if(new File(u.getExternalDirectory() + "/BootManager/" + slot+ "/system.img").exists()){
					      					OWDialog(slot); 
					      				}else{
					      					spaceCheck(slot);	
					      				}
					              }
					          })
					          .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					              public void onClick(DialogInterface dialog, int id) {
					            	  getDir(path.get(position));
					              }
					          }).show();
				  		}else{
				  			getDir(path.get(position));
				  		}*/
			  		}else{
					   Toast.makeText(NandPicker.this, file.getName() + " can't be read!", Toast.LENGTH_LONG).show();
				   }
		  		}
	}
	
	private void spaceCheck(String slot){
		u.log("checking free space");
        SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
    	systemsize=settings.getString("systemsize", "");
    	datasize=settings.getString("datasize", "");
    	cachesize=settings.getString("cachesize", ""); 
        int a = Integer.parseInt(systemsize);
        int b = Integer.parseInt(datasize);
        int c = Integer.parseInt(cachesize); 
        int sum = a + b + c;
        StatFs stat = new StatFs((u.getExternalDirectory()+"/BootManager/rom1").toString());
        double sdAvailSize = (double)stat.getAvailableBlocks() *(double)stat.getBlockSize();
        double gigaAvailable = sdAvailSize / 1048576;
        if(gigaAvailable < sum){
        	u.log("Only "+gigaAvailable+" space free");
        	CustomDialog.Builder builder = new CustomDialog.Builder(NandPicker.this);
        	builder.setTitle("Low On Space!")
        		   .setMessage(R.string.lowspace)
        	       .setCancelable(true)
        	       .setNegativeButton(R.string.okay, new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   NandPicker.this.finish();
        	           }
        	       }).show();
        }else{
        	Dialog(slot);
        }
	}
	
	private void OWDialog(final String slot){
		romshort=(nandroid.substring(nandroid.lastIndexOf("/")+1));
		CustomDialog.Builder builder = new CustomDialog.Builder(NandPicker.this);
		builder.setTitle("Overwrite?")
			   .setMessage(getString(R.string.overwriteROM) + " " + slot + " " + getString(R.string.overwriteCurrent))
		       .setCancelable(true)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
						extDialog(slot);
		           }
		       })
		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       }).show();
	}
	
	private void Dialog(final String slot){
		romshort=(nandroid.substring(nandroid.lastIndexOf("/")+1));
		CustomDialog.Builder builder = new CustomDialog.Builder(NandPicker.this);
		builder.setTitle("Install to" + slot + "?")
			   .setMessage(getString(R.string.installConfirm) + " " + romshort + " " + getString(R.string.to) + " " + slot)
		       .setCancelable(true)
		       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	    extDialog(slot);
		           }
		       })
		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		           }
		       }).show();
	}
	
	private void extDialog(final String slot){
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		String board=settings.getString("device", "");
		if(board.contains("shadow")||board.contains("droid2")||board.contains("droid2we")||board.contains("spyder")||board.contains("maserati")||board.contains("targa")||board.contains("solana")){
			ext="ext3";
			startinstallservice(slot);
    	    Intent i = new Intent(NandPicker.this, Install.class);
			startActivity(i);
		} else if(board.contains("tuna")){
			ext="ext4";
			startinstallservice(slot);
    	    Intent i = new Intent(NandPicker.this, Install.class);
			startActivity(i);
			NandPicker.this.finish();
		} else {
		String fsmessage = getString(R.string.filesystem);
		CustomDialog.Builder builder = new CustomDialog.Builder(NandPicker.this);
		builder.setTitle("Which Filesystem for "+slot)
			   .setMessage(fsmessage+checkfilesystems())
		       .setCancelable(true)
		       .setPositiveButton("Ext4", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	    u.execCommand(context.getFilesDir().getAbsolutePath() + "/busybox rm "+u.getExternalDirectory()+"/log.txt");
		        	    ext="ext4";
		        	    u.log("Chose ext4 format");
		        	    startinstallservice(slot);
		        	    Intent i = new Intent(NandPicker.this, Install.class);
						startActivity(i);
		           }
		       })
		       .setNegativeButton("Ext2", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                ext="ext2";
		                u.log("chose ext2 format");
		                startinstallservice(slot);
		                Intent i = new Intent(NandPicker.this, Install.class);
						startActivity(i);
		           }
		       }).show();
		}
	}
	
	private String checkfilesystems(){
		String string;
		Boolean ext2=false;
		Boolean ext4=false;
		CommandResult cf = s.su.runWaitFor(context.getFilesDir().getAbsolutePath()+"/busybox cat /proc/filesystems");
		if(cf.stdout!=null){
			if(cf.stdout.contains("ext2")){
				ext2=true;
			}
			if(cf.stdout.contains("ext4")){
				ext4=true;
			}
		}
		if(ext2 && ext4){
			string=getString(R.string.e2_e4_support);
		} else if(ext2 && !ext4){
			string=getString(R.string.e2_support);
		} else if(!ext2 && ext4){
			string=getString(R.string.e4_support);
		} else {
			string=getString(R.string.no_fs_support); 
		}
		return string;
	}
	
	private void startinstallservice(String slot){
		Intent service = new Intent(this, NandRestoreService.class);
		service.putExtra("ext", ext);
		service.putExtra("nandroid", nandroid);
		service.putExtra("slot", slot);
		startService(service);
	}	
}