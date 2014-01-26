package com.iukonline.amule.android.amuleremote.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;

public class NewVersionDialogFragment extends AlertDialogFragment {
    
    private final static String BUNDLE_NEW_RELEASE_URL = "new_release_url";
    private final static String BUNDLE_RELEASE_NOTES = "release_notes";
    
    private String mNewReleaseUrl;
    private String mReleaseNotes;
    
    public NewVersionDialogFragment() {}
    
    public NewVersionDialogFragment(String newReleaseURL, String releaseNotes) {
        mNewReleaseUrl = newReleaseURL;
        mReleaseNotes = releaseNotes;
        
        mShowCancel = false;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_NEW_RELEASE_URL, mNewReleaseUrl);
        outState.putString(BUNDLE_RELEASE_NOTES, mReleaseNotes);
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        
        if (savedInstanceState != null) {
            mNewReleaseUrl = savedInstanceState.getString(BUNDLE_NEW_RELEASE_URL);
            mReleaseNotes = savedInstanceState.getString(BUNDLE_RELEASE_NOTES);
        }
        
        View newVersionView = getActivity().getLayoutInflater().inflate(R.layout.new_version_dialog, null);
        ((TextView) newVersionView.findViewById(R.id.new_version_dialog_download_url)).setText(mNewReleaseUrl);
        ((TextView) newVersionView.findViewById(R.id.new_version_dialog_release_notes)).setText(mReleaseNotes);
        builder.setView(newVersionView);

        return builder.create();
    }
}
