package com.drx2.bootmanager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class Installgapps extends Activity {
	Button yes;
	Button no;
	private Intent i;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
    setContentView(R.layout.installgapps);
    
    yes = (Button)findViewById(R.id.yes);
	
	yes.setOnClickListener(new OnClickListener() {
		public void onClick(View v){
			i = new Intent(Installgapps.this, MainActivity.class);
			startActivity(i);
			finish();
		}
	});
	
    }
}
