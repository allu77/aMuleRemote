/*
 * Copyright (c) 2015. Gianluca Vegetti
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iukonline.amule.android.amuleremote.settings;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.util.Log;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;

public class ServerSettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    private final static String BUNDLE_PARAM_SERVER_INDEX = "server_index";

    public interface ServerSettingsFragmentContainer {
        boolean addServer();
    }

    private AmuleRemoteApplication mApp;
    private int mServerIndex = -1;
    private PreferenceGroup mPrefGroup;

    private ServerSettingsFragmentContainer mContainer;

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
        mApp = (AmuleRemoteApplication) activity.getApplication();
        mContainer = (ServerSettingsFragmentContainer) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onCreate: BEGIN");

        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) mServerIndex = args.getInt(ServerSettingsActivity.BUNDLE_PARAM_SERVER_INDEX, -1);

        initializeGUI();

        if (DEBUG) Log.d(TAG, "ServerSettingsFragment.onCreate: END");
    }

    private void initializeGUI() {
        addPreferencesFromResource(R.xml.server_settings);

        mPrefGroup = getPreferenceScreen();
        if (mServerIndex >= 0) {
            mPrefGroup.removePreference(mPrefGroup.findPreference("server_create"));
        } else {
            mPrefGroup.findPreference("server_create").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (DEBUG) Log.d(TAG, "ServerSettingsFragment->onPreferenceClick: ADD SERVER");
                    return mContainer.addServer();
                }
            });
        }

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
