package com.drx2.bootmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.drx2.bootmanager.extras.Settings;
import com.drx2.bootmanager.utilities.Utilities;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Manual extends Activity implements OnClickListener {
	Button b1;
	Button b2;
	Button b3;
	Button b4;
	Button b5;
	Button b6;
	TextView t1;
	TextView t2;
	TextView t3;
	TextView t4;
	TextView t5;
	TextView tv1;
	TextView tv2;
	TextView tv3;
	TextView tv4;
	TextView tv5;
	Utilities u = new Utilities();
	private Intent i;
	private static final String PREFS_DEVICE = "DeviceInfo";
	Context context;
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
	        setContentView(R.layout.manual);
	        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	        actionBar.setHomeAction(new Home());
	        final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
		    actionBar.setHomeColor(colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
		    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
		    final float scale = context.getResources().getDisplayMetrics().density;
		    int pixels = (int) (45 * scale + 0.5f);
		    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
		    actionBar.setBackgroundDrawable(mDrawable);
		    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	        b1=(Button)findViewById(R.id.variable1);
	    	b1.setOnClickListener(this);
	    	b2=(Button)findViewById(R.id.variable2);
	    	b2.setOnClickListener(this);
	    	b3=(Button)findViewById(R.id.variable3);
	    	b3.setOnClickListener(this);
	    	b4=(Button)findViewById(R.id.variable4);
	    	b4.setOnClickListener(this);
	    	b5=(Button)findViewById(R.id.variable5);
	    	b5.setOnClickListener(this);
	    	b6=(Button)findViewById(R.id.variableFin);
	    	b6.setOnClickListener(this);
	    	b1.setBackgroundDrawable(MainActivity.buttonState(context));
	    	b2.setBackgroundDrawable(MainActivity.buttonState(context));
	    	b3.setBackgroundDrawable(MainActivity.buttonState(context));
	    	b4.setBackgroundDrawable(MainActivity.buttonState(context));
	    	b5.setBackgroundDrawable(MainActivity.buttonState(context));
	    	b6.setBackgroundDrawable(MainActivity.buttonState(context));
	    	b1.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
	    	b2.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
	    	b3.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
	    	b4.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
	    	b5.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
	    	b6.setTextColor(colors.getInt("buttonText", context.getResources().getColor(R.color.buttonText)));
	    	t1 =(TextView)this.findViewById(R.id.vText1);
	    	t2 =(TextView)this.findViewById(R.id.vText2);
	    	t3 =(TextView)this.findViewById(R.id.vText3);
	    	t4 =(TextView)this.findViewById(R.id.vText4);
	    	t5 =(TextView)this.findViewById(R.id.vText5);
	    	tv1 =(TextView)this.findViewById(R.id.textView1);
	    	tv2 =(TextView)this.findViewById(R.id.textView2);
	    	tv3 =(TextView)this.findViewById(R.id.textView3);
	    	tv4 =(TextView)this.findViewById(R.id.textView4);
	    	tv5 =(TextView)this.findViewById(R.id.textView5);
	    	getVariable();
	 }
	@Override
	public void onClick(View arg0) {
		if (b1 == arg0){
			Toast.makeText(Manual.this, "Device set as " + t1.getEditableText().toString(), Toast.LENGTH_LONG).show();
			manVariable("device", t1.getEditableText().toString());
		}
		
		if (b2 == arg0){
			Toast.makeText(Manual.this, "Sdcard block set as " + t2.getEditableText().toString(), Toast.LENGTH_LONG).show();
			manVariable("sdcard", t2.getEditableText().toString());
		}
		
		if (b3 == arg0){
			String systemsize;
			if((t3.getEditableText().toString()).contains(".")){
			int dot = (t3.getEditableText().toString()).lastIndexOf('.');
				systemsize=(t3.getEditableText().toString()).substring(0, dot);
			} else {
				systemsize=t3.getEditableText().toString();
			}
			manVariable("systemsize", systemsize);
			Toast.makeText(Manual.this, "System size set as " + systemsize, Toast.LENGTH_LONG).show();
		}
		
		if (b4 == arg0){
			String datasize;
			if((t4.getEditableText().toString()).contains(".")){
			int dot = (t4.getEditableText().toString()).lastIndexOf('.');
				datasize=(t4.getEditableText().toString()).substring(0, dot);
			} else {
				datasize=t4.getEditableText().toString();
			}
			Toast.makeText(Manual.this, "Data size set as " + datasize, Toast.LENGTH_LONG).show();
			manVariable("datasize", datasize);
		}
		
		if (b5 == arg0){
			String cachesize;
			if((t5.getEditableText().toString()).contains(".")){
			int dot = (t5.getEditableText().toString()).lastIndexOf('.');
				cachesize=(t5.getEditableText().toString()).substring(0, dot);
			} else {
				cachesize=t5.getEditableText().toString();
			}
			Toast.makeText(Manual.this, "Cache size set as " + cachesize, Toast.LENGTH_LONG).show();
			manVariable("cachesize", cachesize);
		}
		
		if (b6 == arg0){
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Check your variables!")
				   .setMessage(R.string.manual)
			       .setCancelable(true)
			       .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			        	   SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
			       		   SharedPreferences.Editor editor = settings.edit();
			       		   editor.putBoolean("varset", true);
			       		   editor.commit();
			        	   i = new Intent(Manual.this, com.drx2.bootmanager.Loader.class);
						   startActivity(i);
						   finish();
			           }
			       })
			       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			                dialog.cancel();
			           }
			       }).show();
		}
	}

	private void manVariable(String variable, String NAME){
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(variable, NAME);
		editor.commit();
		getVariable();
	}
	
	private void getVariable() {
		SharedPreferences settings = getSharedPreferences(PREFS_DEVICE, 0);
		String device = settings.getString("device", "not set");
		String sdcardblock = settings.getString("sdcard", "not set");
		String systemsize = settings.getString("systemsize", "not set");
		String datasize = settings.getString("datasize", "not set");
		String cachesize = settings.getString("cachesize", "not set");
		if(device!=null){
			tv1.setText("What Device Do You Have?\nCurrent setting = " + device);
		}
		if(sdcardblock!=null){
			tv2.setText("What is your sdcardblock?\nCurrent setting = "+sdcardblock);
		}
		if(systemsize!=null){
			tv3.setText("What is your system's size in mb?\nCurrent setting = "+systemsize);
		}
		if(datasize!=null){
			tv4.setText("What is your data's size in mb?\nCurrent setting = "+datasize);
		}
		if(cachesize!=null){
			tv5.setText("What is your cache's size in mb?\nCurrent setting = "+cachesize);
		}
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    Intent i = new Intent(Manual.this, Settings.class);
    	startActivity(i);
    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
    	Manual.this.finish();
	}
	
	private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
	    	Manual.this.finish();
	    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	    }
	}
}