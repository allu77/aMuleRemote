package com.iukonline.amule.android.amuleremote;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class AmuleControllerPreferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_settings);
    }

    @Override
    public void onContentChanged() {
        // TODO Auto-generated method stub
        super.onContentChanged();
    }
    
    
}
