package com.drx2.bootmanager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {
    private static final String APP_SHARED_PREFS = "DeviceInfo";
    private SharedPreferences appSharedPrefs;
    private Editor prefsEditor;

    public Preferences(Context context)
    {
        this.appSharedPrefs = context.getSharedPreferences(APP_SHARED_PREFS, Activity.MODE_PRIVATE);
        this.prefsEditor = appSharedPrefs.edit();
    }

    public int getvmIndex() {
        return appSharedPrefs.getInt("vmIndex", -1);
    }

    public void savevmIndex(int i) {
        prefsEditor.putInt("vmIndex", i);
        prefsEditor.commit();
    }
    
    public int getSdBoostIndex() {
        return appSharedPrefs.getInt("sdboostIndex", -1);
    }

    public void saveSdBoostIndex(int i) {
        prefsEditor.putInt("sdboostIndex", i);
        prefsEditor.commit();
    }
}