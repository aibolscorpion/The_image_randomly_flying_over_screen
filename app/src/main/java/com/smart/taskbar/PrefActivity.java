package com.smart.taskbar;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

/**
 * Created by aibol on 21.09.2015.
 */
public class PrefActivity extends PreferenceActivity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref);
        Log.i("aibol", "PrefActivity");
    }
}
