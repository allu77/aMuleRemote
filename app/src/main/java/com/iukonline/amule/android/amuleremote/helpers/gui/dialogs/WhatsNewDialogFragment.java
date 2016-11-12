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

package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;

public class WhatsNewDialogFragment extends AlertDialogFragment {
    
    private final static String BUNDLE_WELCOME = "welcome";
    private final static String BUNDLE_FEATURES = "features";
    private String mWelcome;
    private String mFeatures;
    
    public WhatsNewDialogFragment() {}
    
    public static WhatsNewDialogFragment newInstance(String welcome, String features) {

        Bundle args = new Bundle();
        args.putString(BUNDLE_WELCOME, welcome);
        args.putString(BUNDLE_FEATURES, features);
        args.putBoolean(BUNDLE_SHOW_CANCEL, false);

        WhatsNewDialogFragment fragment = new WhatsNewDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_WELCOME, mWelcome);
        outState.putString(BUNDLE_FEATURES, mFeatures);
        super.onSaveInstanceState(outState);
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);

        Bundle b = (savedInstanceState == null ? getArguments() : savedInstanceState);
        if (b != null) {
            mWelcome = b.getString(BUNDLE_WELCOME);
            mFeatures = b.getString(BUNDLE_FEATURES);
        }
        
        View whatsNewView = getActivity().getLayoutInflater().inflate(R.layout.dialog_whats_new, null);
        ((TextView) whatsNewView.findViewById(R.id.dialog_whats_new_title)).setText(mWelcome);
        ((TextView) whatsNewView.findViewById(R.id.dialog_whats_new_features)).setText(mFeatures);
        builder.setView(whatsNewView);

        return builder.create();
    }
}
