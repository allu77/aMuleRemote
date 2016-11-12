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

public class NewVersionDialogFragment extends AlertDialogFragment {
    
    private final static String BUNDLE_NEW_RELEASE_URL = "new_release_url";
    private final static String BUNDLE_RELEASE_NOTES = "release_notes";
    
    private String mNewReleaseUrl;
    private String mReleaseNotes;
    
    public NewVersionDialogFragment() {}
    
    public static NewVersionDialogFragment newInstance(String newReleaseURL, String releaseNotes) {

        Bundle args = new Bundle();
        args.putString(BUNDLE_NEW_RELEASE_URL, newReleaseURL);
        args.putString(BUNDLE_RELEASE_NOTES, releaseNotes);
        args.putBoolean(BUNDLE_SHOW_CANCEL, false);

        NewVersionDialogFragment fragment = new NewVersionDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_NEW_RELEASE_URL, mNewReleaseUrl);
        outState.putString(BUNDLE_RELEASE_NOTES, mReleaseNotes);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mNewReleaseUrl = savedInstanceState.getString(BUNDLE_NEW_RELEASE_URL);
            mReleaseNotes = savedInstanceState.getString(BUNDLE_RELEASE_NOTES);
        } else {
            Bundle args = getArguments();
            if (args != null) {
                mNewReleaseUrl = args.getString(BUNDLE_NEW_RELEASE_URL);
                mReleaseNotes = args.getString(BUNDLE_RELEASE_NOTES);
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        

        
        View newVersionView = getActivity().getLayoutInflater().inflate(R.layout.new_version_dialog, null);
        ((TextView) newVersionView.findViewById(R.id.new_version_dialog_download_url)).setText(mNewReleaseUrl);
        ((TextView) newVersionView.findViewById(R.id.new_version_dialog_release_notes)).setText(mReleaseNotes);
        builder.setView(newVersionView);

        return builder.create();
    }
}
