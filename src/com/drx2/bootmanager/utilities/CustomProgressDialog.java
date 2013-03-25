package com.drx2.bootmanager.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drx2.bootmanager.R;

public class CustomProgressDialog extends Dialog {
	private static final String PREFS_DEVICE = "DeviceInfo";

		public static CustomProgressDialog show(Context context, CharSequence title,
		        CharSequence message) {
		    return show(context, title, message, false);
		}

		public static CustomProgressDialog show(Context context, CharSequence title,
		        CharSequence message, boolean indeterminate) {
		    return show(context, title, message, indeterminate, false, null);
		}

		public static CustomProgressDialog show(Context context, CharSequence title,
		        CharSequence message, boolean indeterminate, boolean cancelable) {
		    return show(context, title, message, indeterminate, cancelable, null);
		}

		public static CustomProgressDialog show(Context context, CharSequence title,
		        CharSequence message, boolean indeterminate,
		        boolean cancelable, OnCancelListener cancelListener) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    CustomProgressDialog dialog = new CustomProgressDialog(context);
		    SharedPreferences shared = PreferenceManager.getDefaultSharedPreferences(context);
    		View layout;
            if(shared.getBoolean("themePref", false) == true){
            	layout = inflater.inflate(R.layout.custom_progress_dialog_dark, null);
    	    }else{
    	    	layout = inflater.inflate(R.layout.custom_progress_dialog, null);
    	    }
		    SharedPreferences colors = context.getSharedPreferences(PREFS_DEVICE, 0);
        	final float[] roundedCorners = new float[] { 10, 10, 10, 10, 10, 10, 10, 10 };
   		 	ShapeDrawable boardershape = new ShapeDrawable(new RoundRectShape(roundedCorners, null, roundedCorners));
            boardershape.getPaint().setColor(colors.getInt("actionbarStart", context.getResources().getColor(R.color.actionbar_background_end)));
            ((LinearLayout) layout.findViewById(R.id.border)).setBackgroundDrawable(boardershape);
            ((LinearLayout) layout.findViewById(R.id.horizontalLine)).setBackgroundColor(colors.getInt("actionbarStart", context.getResources().getColor(R.color.actionbar_background_end)));
            if(title!=null){
            	((TextView) layout.findViewById(R.id.title)).setText(title);
            } else {
                layout.findViewById(R.id.title).setVisibility(View.GONE);
                layout.findViewById(R.id.horizontalLine).setVisibility(View.GONE);
            }
		    if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
		    } else {
                layout.findViewById(R.id.message).setVisibility(View.GONE);
            }    
		    dialog.setCancelable(cancelable);
		    dialog.setOnCancelListener(cancelListener);
		    /* The next line will add the ProgressBar to the dialog. */
		    dialog.setContentView(layout);
		    dialog.show();

		    return dialog;
		}

		public CustomProgressDialog(Context context) {
		    super(context, R.style.ProgressDialog);
		}

	
}
