package com.iukonline.amule.android.amuleremote;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


// TODO: Convert to new Fragment Preference Activiy

public class AmuleControllerPreferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.main_settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.preferences_options, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_preferences_opt_help:
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_help)));
            startActivity(myIntent);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

   
}
