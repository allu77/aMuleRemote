/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.settings;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.SettingsHelper;

public class SettingsFragment extends PreferenceFragment {

    private SettingsHelper mSettingsHelper;
    private PreferenceCategory mServerCategory;
    private Activity mActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        mSettingsHelper = new SettingsHelper();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSettingsHelper.refresh();
        refreshServerCategory();
    }

    private void refreshServerCategory() {
        PreferenceScreen screen = this.getPreferenceScreen();
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
}
