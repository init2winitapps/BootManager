package com.drx2.bootmanager.extras;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.drx2.bootmanager.R;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class Help extends Activity {
	
	Intent i;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        ActionBar actionBar = (ActionBar) findViewById(R.id.actionbar);
	    actionBar.setHomeAction(new Home());
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
	    	Help.this.finish();
	    	overridePendingTransition(R.anim.no_anim, R.anim.slide_down_out);
	    }
	}
}