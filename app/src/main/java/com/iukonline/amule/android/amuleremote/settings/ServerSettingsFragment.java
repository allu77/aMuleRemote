/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.settings;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.util.Log;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.SettingsHelper;

public class ServerSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = AmuleControllerApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
    private final static String BUNDLE_INITIALIZED = "is_initialized";
    private final static String BUNDLE_PARAM_SERVER_INDEX = "server_index";

    private AmuleControllerApplication mApp;
    private int mServerIndex = -1;
    private PreferenceGroup mPrefGroup;
    private SettingsHelper mSettingsHelper;
    private boolean mInitialized = false;

    public static ServerSettingsFragment newInstance(int serverIndex) {
        ServerSettingsFragment f = new ServerSettingsFragment();
        Bundle serverSettingsParams = new Bundle();
        serverSettingsParams.putInt(BUNDLE_PARAM_SERVER_INDEX, serverIndex);
        f.setArguments(serverSettingsParams);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mApp = (AmuleControllerApplication) activity.getApplication();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onCreate: BEGIN");

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) mServerIndex = args.getInt(ServerSettingsActivity.BUNDLE_PARAM_SERVER_INDEX, -1);
        if (savedInstanceState != null) mInitialized = savedInstanceState.getBoolean(BUNDLE_INITIALIZED, false);

        mSettingsHelper = new SettingsHelper(mApp.mSettings);
        initializeGUI();

        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onCreate: END");
    }

    private void initializeGUI() {
        addPreferencesFromResource(R.xml.server_settings);

        mPrefGroup = getPreferenceScreen();
        SharedPreferences pref = mApp.mSettings;
        setPreferenceSummary(ServerSettingsActivity.SETTINGS_SERVER_NAME, pref.getString(ServerSettingsActivity.SETTINGS_SERVER_NAME, ""));
        setPreferenceSummary(ServerSettingsActivity.SETTINGS_SERVER_HOST, pref.getString(ServerSettingsActivity.SETTINGS_SERVER_HOST, ""));
        setPreferenceSummary(ServerSettingsActivity.SETTINGS_SERVER_PORT, pref.getString(ServerSettingsActivity.SETTINGS_SERVER_PORT, ""));
        setPreferenceSummary(ServerSettingsActivity.SETTINGS_SERVER_PASSWORD, pref.getString(ServerSettingsActivity.SETTINGS_SERVER_PASSWORD, ""));
        setPreferenceSummary(ServerSettingsActivity.SETTINGS_SERVER_VERSION, pref.getString(ServerSettingsActivity.SETTINGS_SERVER_VERSION, ""));
    }

    @Override
    public void onResume() {
        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onResume: BEGIN");

        super.onResume();
        mPrefGroup.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onResume: END");
    }

    @Override
    public void onPause() {
        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onPause: BEGIN");

        super.onPause();
        mPrefGroup.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);

        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onPause: END");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onSaveInstanceState: BEGIN");

        outState.putBoolean(BUNDLE_INITIALIZED, mInitialized);
        super.onSaveInstanceState(outState);

        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onSaveInstanceState: END");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        setPreferenceSummary(key,sharedPreferences.getString(key, ""));

    }


    private void setPreferenceSummary(String key, String value) {
        if (DEBUG) Log.d(TAG, "SererSettingsFragment.setPreferenceSummary(): Setting summary for " + key + " and value " + value);
        String summary = value;

        if (key.equals(ServerSettingsActivity.SETTINGS_SERVER_NAME)) {
            summary = (value.length() > 0 ? value : getString(R.string.settings_summary_server_name));

        } else if (key.equals(ServerSettingsActivity.SETTINGS_SERVER_HOST)) {
            summary = (value.length() > 0 ? value : getString(R.string.settings_summary_server_host));

        } else if (key.equals(ServerSettingsActivity.SETTINGS_SERVER_PASSWORD)) {
            summary = (value.length() > 0 ? "******" : getResources().getString(R.string.settings_summary_server_password));

        } else if (key.equals(ServerSettingsActivity.SETTINGS_SERVER_VERSION)) {
            String[] versionDescription = getResources().getStringArray(R.array.settings_server_version_description);
            String[] versionValue = getResources().getStringArray(R.array.settings_server_version_value);

            summary = getResources().getString(R.string.settings_summary_server_version);
            for (int i = 0; i < versionValue.length; i++) {
                if (value.equals(versionValue[i])) {
                    summary = versionDescription[i];
                    break;
                }
            }
        }
        Preference p = mPrefGroup.findPreference(key);
        if (p != null) {
            // null in cas of change of a preference not displayed here
            p.setSummary(summary);
        }
    }
}
