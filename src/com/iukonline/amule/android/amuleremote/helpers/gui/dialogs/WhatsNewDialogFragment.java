package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;

public class WhatsNewDialogFragment extends AlertDialogFragment {
    
    private final static String BUNDLE_WELCOME = "welcome";
    private String mWelcome;
    
    public WhatsNewDialogFragment() {}
    
    public WhatsNewDialogFragment(String welcome) {
        mShowCancel = false;
        mWelcome = welcome;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(BUNDLE_WELCOME, mWelcome);
        super.onSaveInstanceState(outState);
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        
        if (savedInstanceState != null) {
            mWelcome = savedInstanceState.getString(BUNDLE_WELCOME);
        }
        
        View whatsNewView = getActivity().getLayoutInflater().inflate(R.layout.dialog_whats_new, null);
        ((TextView) whatsNewView.findViewById(R.id.dialog_whats_new_title)).setText(mWelcome);
        builder.setView(whatsNewView);

        return builder.create();
    }
}
