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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.SettingsHelper;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;



    private SettingsHelper mSettingsHelper;
    private PreferenceCategory mServerCategory;
    private Activity mActivity;
    private AmuleRemoteApplication mApp;

    private PreferenceGroup mPrefGroup;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        mApp = (AmuleRemoteApplication) activity.getApplication();


        mSettingsHelper = new SettingsHelper(mApp.mSettings);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        PreferenceManager.setDefaultValues(mApp, R.xml.settings, false);
        mPrefGroup = getPreferenceScreen();

        setPreferenceSummary(AmuleRemoteApplication.AC_SETTING_AUTOREFRESH_INTERVAL, mApp.mSettings.getString(AmuleRemoteApplication.AC_SETTING_AUTOREFRESH_INTERVAL, ""));
        setPreferenceSummary(AmuleRemoteApplication.AC_SETTING_CONNECT_TIMEOUT, mApp.mSettings.getString(AmuleRemoteApplication.AC_SETTING_CONNECT_TIMEOUT, ""));
        setPreferenceSummary(AmuleRemoteApplication.AC_SETTING_READ_TIMEOUT, mApp.mSettings.getString(AmuleRemoteApplication.AC_SETTING_READ_TIMEOUT, ""));
    }

    @Override
    public void onResume() {
        super.onResume();
        mSettingsHelper.refreshServerList();
        refreshServerCategory();
        mPrefGroup.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPrefGroup.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void refreshServerCategory() {
        PreferenceScreen screen = getPreferenceScreen();
        mServerCategory = (PreferenceCategory) screen.findPreference("amule_server");
        mServerCategory.removeAll();

        for (int i = 0; i < mSettingsHelper.getServerCount(); i++) {
            PreferenceScreen serverPreference = getPreferenceManager().createPreferenceScreen(mActivity);
            SettingsHelper.ServerSettings serverSettings = mSettingsHelper.getServerSettings(i);
            serverPreference.setKey("server_edit_" + i);
            serverPreference.setTitle(serverSettings.name);
            serverPreference.setSummary(serverSettings.host + ":" + serverSettings.port);
            Intent serverSettingsIntent = new Intent(getActivity(), ServerSettingsActivity.class);
            serverSettingsIntent.putExtra(ServerSettingsActivity.BUNDLE_PARAM_SERVER_INDEX, i);
            serverPreference.setIntent(serverSettingsIntent);
            mServerCategory.addPreference(serverPreference);
        }

        PreferenceScreen addServer = getPreferenceManager().createPreferenceScreen(mActivity);
        addServer.setKey("server_add");
        addServer.setTitle(R.string.settings_label_server_add);
        Intent serverSettingsIntent = new Intent(getActivity(), ServerSettingsActivity.class);
        addServer.setIntent(serverSettingsIntent);
        mServerCategory.addPreference(addServer);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (! AmuleRemoteApplication.AC_SETTING_AUTOREFRESH.equals(key)) {
            setPreferenceSummary(key, sharedPreferences.getString(key, ""));
        }
    }

    private void setPreferenceSummary(String key, String value) {
        if (DEBUG) Log.d(TAG, "SettingsFragment.setPreferenceSummary(): Setting summary for " + key + " and value " + value);
        String summary = value;

        if (key.equals(AmuleRemoteApplication.AC_SETTING_AUTOREFRESH_INTERVAL)) {
            summary = getString(R.string.settings_summary_client_autorefresh_interval, Integer.parseInt(value));

        } else if (key.equals(AmuleRemoteApplication.AC_SETTING_CONNECT_TIMEOUT)) {
            summary = getString(R.string.settings_summary_client_connect_timeout, Integer.parseInt(value));

        } else if (key.equals(AmuleRemoteApplication.AC_SETTING_READ_TIMEOUT)) {
            summary = getString(R.string.settings_summary_client_read_timeout, Integer.parseInt(value));
        }
        Preference p = mPrefGroup.findPreference(key);
        if (p != null) {
            // null in cas of change of a preference not displayed here
            p.setSummary(summary);
        }
    }
}
