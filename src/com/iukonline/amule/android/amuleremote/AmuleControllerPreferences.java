package com.iukonline.amule.android.amuleremote;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

// TODO: Convert to new Fragment Preference Activiy

public class AmuleControllerPreferences extends SherlockPreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_settings);
    }

   
}
