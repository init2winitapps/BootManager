package com.drx2.bootmanager.extras;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.View;
import android.view.Window;

import com.drx2.bootmanager.R;
import com.drx2.bootmanager.extras.AmbilWarnaDialog.OnAmbilWarnaListener;
import com.drx2.bootmanager.utilities.Utilities;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class UITweaks extends PreferenceActivity {

	Context context;
    Utilities u = new Utilities();
    ActionBar actionBar;
    SharedPreferences shared;
    private static final String PREFS_DEVICE = "DeviceInfo";
    
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
		addPreferencesFromResource(R.xml.uitweaks);
		setContentView(R.layout.settings);
		final SharedPreferences colors = getSharedPreferences(PREFS_DEVICE, 0);
		final SharedPreferences.Editor editor = colors.edit();
		actionBar = (ActionBar) findViewById(R.id.actionbar);
		actionBar.setTitle(R.string.uiTweaksTitle);
	    actionBar.setHomeAction(new Home());
	    final SharedPreferences colord = getSharedPreferences(PREFS_DEVICE, 0);
	    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
	    final float scale = context.getResources().getDisplayMetrics().density;
	    int pixels = (int) (45 * scale + 0.5f);
	    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colord.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colord.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
	    actionBar.setBackgroundDrawable(mDrawable);
	    actionBar.setHomeColor(colord.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
	    actionBar.setTitleColor(colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)));	    
		
		Preference topbarStartPref = (Preference) findPreference("uitopBarPref");
		topbarStartPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("actionbarStart", color);
						editor.commit();
					    ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
					    final float scale = context.getResources().getDisplayMetrics().density;
					    int pixels = (int) (45 * scale + 0.5f);
					    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colord.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colord.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
					    actionBar.setBackgroundDrawable(mDrawable);
					    actionBar.setHomeColor(colord.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
					}
			                
					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();
				return true;
			}
		});
		
		Preference topbottomStartPref = (Preference) findPreference("uibottomBarPref");
		topbottomStartPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("actionbarEnd", color);
						editor.commit();
						ShapeDrawable mDrawable = new ShapeDrawable(new RectShape());
					    final float scale = context.getResources().getDisplayMetrics().density;
					    int pixels = (int) (45 * scale + 0.5f);
					    mDrawable.getPaint().setShader(new LinearGradient(0, 0, 0, pixels, colord.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colord.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)), Shader.TileMode.REPEAT));
					    actionBar.setBackgroundDrawable(mDrawable);
					    actionBar.setHomeColor(colord.getInt("actionbarStart", getResources().getColor(R.color.actionbar_background_start)), colors.getInt("actionbarEnd", getResources().getColor(R.color.actionbar_background_end)));
					}
			                
					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();
				return true;
			}
		});

		Preference topTextPref = (Preference) findPreference("uiTopTextPref");
		topTextPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("actionbarText", getResources().getColor(R.color.actionbar_title)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("actionbarText", color);
						editor.commit();
						actionBar.setTitleColor(color);
					}

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();				
				return true;
			}
		});
		
		Preference titleTextPref = (Preference) findPreference("pageTitlePref");
		titleTextPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("pageTitleText", getResources().getColor(R.color.vpTitle)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("pageTitleText", color);
						editor.commit();
					}

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();				
				return true;
			}
		});
		
		Preference titleUnusedTextPref = (Preference) findPreference("pageUnusedTitlePref");
		titleUnusedTextPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("pageUnusedTitleText", getResources().getColor(R.color.vpTitleUnused)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("pageUnusedTitleText", color);
						editor.commit();
					}

					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();				
				return true;
			}
		});
		
		Preference buttonStartPref = (Preference) findPreference("buttonStartPref");
		buttonStartPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("buttonStart", getResources().getColor(R.color.buttonStart)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("buttonStart", color);
						editor.commit();
					}
			                
					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();
				return true;
			}
		});
		
		Preference buttonEndPref = (Preference) findPreference("buttonEndPref");
		buttonEndPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("buttonEnd", getResources().getColor(R.color.buttonEnd)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("buttonEnd", color);
						editor.commit();
					}
			                
					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();
				return true;
			}
		});
		
		Preference buttonTextPref = (Preference) findPreference("buttonTextPref");
		buttonTextPref.setOnPreferenceClickListener(new OnPreferenceClickListener() { 
			public boolean onPreferenceClick(Preference preference) {
				AmbilWarnaDialog dialog = new AmbilWarnaDialog(UITweaks.this, colors.getInt("buttonText", getResources().getColor(R.color.white)), new OnAmbilWarnaListener() {
					@Override
					public void onOk(AmbilWarnaDialog dialog, int color) {
						editor.putInt("buttonText", color);
						editor.commit();
					}
			                
					@Override
					public void onCancel(AmbilWarnaDialog dialog) {

					}
				});
				dialog.show();
				return true;
			}
		});
	}
	
	@Override
	public void onBackPressed() {
	    super.onBackPressed();
	    Intent i = new Intent(UITweaks.this, Settings.class);
    	startActivity(i);
    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
    	UITweaks.this.finish();
	}
	
	private class Home implements Action {
	    @Override
	    public int getDrawable() {
	        return R.drawable.btn_actionbar_home;
	    }
	    @Override
	    public void performAction(View view) {
	    	UITweaks.this.finish();
	    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	    }
	}
}