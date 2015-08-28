/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.SettingsHelper;

public class ServerSettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener, ServerSettingsFragment.ServerSettingsFragmentContainer {
    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    public final static String BUNDLE_PARAM_SERVER_INDEX = "server_index";
    private final String BUNDLE_INITIALIZED = "is_initialized";

    public final static String SETTINGS_SERVER_NAME = "placeholder_server_name";
    public final static String SETTINGS_SERVER_HOST = "placeholder_server_host";
    public final static String SETTINGS_SERVER_PORT = "placeholder_server_port";
    public final static String SETTINGS_SERVER_PASSWORD = "placeholder_server_password";
    public final static String SETTINGS_SERVER_VERSION = "placeholder_server_version";

    private int mServerIndex = -1;
    private AmuleRemoteApplication mApp;
    private SettingsHelper.ServerSettings mServerSettings;

    private boolean mInitialized = false;


    @Override
    public boolean onSupportNavigateUp() {
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApp = (AmuleRemoteApplication) getApplication();
        setContentView(R.layout.act_server_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            mServerIndex = savedInstanceState.getInt(BUNDLE_PARAM_SERVER_INDEX, -1);
            mInitialized = savedInstanceState.getBoolean(BUNDLE_INITIALIZED, false);
        } else {
            Intent i = getIntent();
            if (i == null) {
                finish(); // This should never happen...
                return;
            }
            mServerIndex = i.getIntExtra(BUNDLE_PARAM_SERVER_INDEX, -1);
        }

        mApp.mSettingsHelper = new SettingsHelper(mApp.mSettings);
        if (mServerIndex >= 0) {
            mServerSettings = mApp.mSettingsHelper.getServerSettings(mServerIndex);
        } else {
            mServerSettings = new SettingsHelper.ServerSettings("", "", 4712, "", "");
        }

        if (! mInitialized) {

            PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SETTINGS_SERVER_NAME, mServerSettings.name)
                .putString(SETTINGS_SERVER_HOST, mServerSettings.host)
                .putString(SETTINGS_SERVER_PORT, "" + mServerSettings.port)
                .putString(SETTINGS_SERVER_PASSWORD, mServerSettings.password)
                .putString(SETTINGS_SERVER_VERSION, mServerSettings.version)
                .commit();

            getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.server_settings_frag_settings, ServerSettingsFragment.newInstance(mServerIndex))
                .commit();

        }

    }

    @Override
    protected void onPause() {
        mApp.mOnTopActivity = null;
        mApp.mSettings.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        mApp.mOnTopActivity = this;
        mApp.mSettings.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_PARAM_SERVER_INDEX, mServerIndex);
        outState.putBoolean(BUNDLE_INITIALIZED, mInitialized);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) Log.d(TAG, "ServerSettingsActivity.onCreateOptionsMenu: BEGIN");
        getMenuInflater().inflate(R.menu.menu_server_settings, menu);
        menu.findItem(R.id.menu_server_settings_remove).setVisible(mServerIndex != -1);
        if (DEBUG) Log.d(TAG, "ServerSettingsActivity.onCreateOptionsMenu: END");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.menu_server_settings_remove:
                if (removeServer()) {
                    finish();
                    return true;
                } else {
                    return false;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean removeServer() {
        if (mApp.mSettingsHelper.removeServerSettings(mServerIndex)) {
            mApp.mSettingsHelper.mNeedNavigationRefresh = true;
            return mApp.mSettingsHelper.commitServerList();
        }
        return false;
    }

    public boolean addServer() {
        if (mApp.mSettingsHelper.addServerSettings(new SettingsHelper.ServerSettings(
                mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_NAME, ""),
                mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_HOST, ""),
                Integer.parseInt(mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_PORT, "")),
                mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_PASSWORD, ""),
                mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_VERSION, "")
        ))) {
            mApp.mSettingsHelper.mNeedNavigationRefresh = true;
            if (mApp.mSettingsHelper.commitServerList()) {
                finish();
                return true;
            }
        }

        return false;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (DEBUG) Log.d(TAG, "ServerSettingsActivity.onSharedPreferenceChanged: Setting " + s + " has changed. This is server index " + mServerIndex);
        if (mServerIndex >= 0) {
            if (SETTINGS_SERVER_HOST.equals(s) ||
                SETTINGS_SERVER_NAME.equals(s) ||
                SETTINGS_SERVER_PASSWORD.equals(s) ||
                SETTINGS_SERVER_PORT.equals(s) ||
                SETTINGS_SERVER_VERSION.equals(s)) {

                int port;
                try {
                    port = Integer.parseInt(mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_PORT, ""));
                } catch (NumberFormatException e) {
                    port = 4712;
                }
                if (mApp.mSettingsHelper.editServerSettings(
                        mServerIndex,
                        mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_NAME, ""),
                        mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_HOST, ""),
                        port,
                        mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_PASSWORD, ""),
                        mApp.mSettings.getString(ServerSettingsActivity.SETTINGS_SERVER_VERSION, "")
                ) != null) mApp.mSettingsHelper.commitServerList();

                mApp.mSettingsHelper.mNeedNavigationRefresh = true;
            }
        }
    }
}
