package com.iukonline.amule.android.amuleremote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;

public class AboutDialogFragment extends AlertDialogFragment {
    
    private final static String BUNDLE_VERSION_NAME = "version_name";
    private final static String BUNDLE_RELEASE_NOTES = "release_notes";
    

    private String mVersionName;
    private String mReleaseNotes;
    
    public AboutDialogFragment() {}
    
    public AboutDialogFragment(String versionName, String releaseNotes) {
        mVersionName = versionName;
        mReleaseNotes = releaseNotes;
        
        mShowCancel = false;
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_VERSION_NAME, mVersionName);
        outState.putString(BUNDLE_RELEASE_NOTES, mReleaseNotes);
        super.onSaveInstanceState(outState);
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        
        if (savedInstanceState != null) {
            mVersionName = savedInstanceState.getString(BUNDLE_VERSION_NAME);
            mReleaseNotes = savedInstanceState.getString(BUNDLE_RELEASE_NOTES);
        }
        
        View aboutView = getActivity().getLayoutInflater().inflate(R.layout.about_dialog, null);
        ((TextView) aboutView.findViewById(R.id.about_dialog_appname)).setText(getString(R.string.app_name) + " " + mVersionName);
        ((TextView) aboutView.findViewById(R.id.about_dialog_release_notes)).setText(mReleaseNotes);
        builder.setView(aboutView);

        return builder.create();
        
        
    }

}
