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


import com.drx2.bootmanager.services.BackupRestoreService;
import com.drx2.bootmanager.utilities.CustomDialog;
import com.drx2.bootmanager.utilities.ShellCommand;
import com.drx2.bootmanager.utilities.Utilities;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Restore extends ListActivity {
	 
	ShellCommand s = new ShellCommand();
	Utilities u = new Utilities();
	private List<String> item = null;
	private List<String> path = null;
	private String root = u.getExternalDirectory() + "/BootManager/Backup";
	private String current = null;
	private String slot;
	private String whattodo;
	private String backup;
	private String backupshort;
	Button installbutton;
	String systemsize;
	String datasize;
	String cachesize;
	String ext = "ext2";
	File file;
	Context context;
	SharedPreferences colors;
	SharedPreferences shared;
	private static final String PREFS_DEVICE = "DeviceInfo";
		
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
        whattodo = extras.getString("whattodo");
        LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
		mainLayout.startAnimation(slideDown);
        mainLayout.setVisibility(LinearLayout.GONE);
        int startcolor = colors.getInt("buttonStart", context.getResources().getColor(R.color.buttonStart));
		int endcolor = colors.getInt("buttonEnd", context.getResources().getColor(R.color.buttonEnd));
		int[] color = {startcolor, endcolor};
		GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, color);
		mainLayout.setBackgroundDrawable(d);
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
        	Toast.makeText(Restore.this, R.string.nMount, Toast.LENGTH_LONG).show();
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
	    installbutton.setOnClickListener(new OnClickListener() {
			public void onClick(View v){
				Intent i = new Intent(Restore.this, BackupRestoreService.class);
				i.putExtra("backup", backup);
				i.putExtra("backupshort", backupshort);
				i.putExtra("whattodo", whattodo);
				i.putExtra("slot", slot);
				startService(i);
				finish();
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
	        		Restore.this.finish();
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
    
    private void itemclicked(){
    	LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
		Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
		mainLayout.startAnimation(slideUp);
		mainLayout.setVisibility(LinearLayout.VISIBLE);
		backup = file.getAbsolutePath();
		backupshort = file.getName();
		ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle("Restore " + backupshort + "to "+slot+"?");
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		file = new File(path.get(position));
		if (!file.isDirectory()){
			if(file.toString().endsWith("zip") && file.getName().startsWith("s"+slot.substring(3, 4))){
				LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
				Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
	  			mainLayout.startAnimation(slideUp);
				mainLayout.setVisibility(LinearLayout.VISIBLE);
				backup = file.getAbsolutePath();
				backupshort = file.getName();
				ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	  		  	actionBar.setTitle("Restore " + backupshort + "to "+slot+"?");
			} else if(file.toString().endsWith("zip")){
				CustomDialog.Builder builder = new CustomDialog.Builder(Restore.this);
				builder.setTitle("Restore to "+slot)
				   	   .setMessage("Restore "+file.getName()+" to "+slot+"?\n"+Restore.this.getString(R.string.restoreBackup) + " " + slot + "?")
				       .setCancelable(true)
				       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				        	   itemclicked();
				           }
				       })
				       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				           public void onClick(DialogInterface dialog, int id) {
				                dialog.cancel();
				           }
				       }).show();
			}else{
				Toast.makeText(Restore.this, file.getName() + " isn't a ROM", Toast.LENGTH_LONG).show();
			}
		}else{
			if(file.canRead()){
				getDir(path.get(position));
		  		if (findViewById(R.id.bottombar).isShown()){
		  			LinearLayout mainLayout=(LinearLayout)this.findViewById(R.id.bottombar);
		  			Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
		  			mainLayout.startAnimation(slideDown);
		  			mainLayout.setVisibility(LinearLayout.GONE);
		  			ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
		  			actionBar.setTitle(current);
		  		}
			}else{
				   Toast.makeText(Restore.this, file.getName() + " can't be read!", Toast.LENGTH_LONG).show();
			}
		}
	}
	

}
