package com.drx2.bootmanager.extras;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;

import com.drx2.bootmanager.R;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class About extends Activity {

	Intent i;
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
        setContentView(R.layout.about);
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    actionBar.setHomeAction(new Home());
	    final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setHomeColor(colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));
	    
        String Version = getString(R.string.bootmanager_version);
        String version = null;
	    try {
	    	PackageInfo pInfo;
	    	pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	    	version = pInfo.versionName;
		}catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		String about = getString(R.string.aboutTitle);
     	actionBar.setTitle(about + " " + Version + version);
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    Intent i = new Intent(About.this, Settings.class);
    	startActivity(i);
    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
    	About.this.finish();
	}
	
	private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
	    	About.this.finish();
	    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	    }
	}
}