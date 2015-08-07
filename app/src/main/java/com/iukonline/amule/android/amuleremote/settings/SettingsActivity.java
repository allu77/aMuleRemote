/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.iukonline.amule.android.amuleremote.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
