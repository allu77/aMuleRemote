/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.iukonline.amule.android.amuleremote.R;

public class EditTextDialogFragment extends AlertDialogFragment {
    

    public final static String BUNDLE_EDIT_STRING = "edit_string";
    
    protected EditText mInput;
    protected String mDefaultText;
    
    public EditTextDialogFragment() {}
    
    // NEW
    public EditTextDialogFragment(int title, String defaultText) {
        super(title, true);
        mDefaultText = defaultText;
    }
    
    //OLD
    /*
    public EditTextDialogFragment(int title, String defaultText, Message okMessage, Message cancelMessage) {
        super(title, okMessage, cancelMessage, true);
        mDefaultText = defaultText;
    }
    */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mInput != null) outState.putString(BUNDLE_EDIT_STRING, mInput.getText().toString());
        super.onSaveInstanceState(outState);
    }
    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        
        mInput = new EditText(getActivity());
        if (mDefaultText != null) mInput.setText(mDefaultText);
        
        if (savedInstanceState != null) mInput.setText(savedInstanceState.getString(BUNDLE_EDIT_STRING));

        builder.setView(mInput);        
        builder.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Bundle b = new Bundle();
                                b.putString(BUNDLE_EDIT_STRING, mInput.getText().toString()); 
                                sendEventToListener(ALERTDIALOG_EVENT_OK, b);
                                if (mOkMessage != null) {
                                    mOkMessage.setData(b);
                                    mOkMessage.sendToTarget();
                                }
                            }
                        }
                    );
        return builder.create();
    }


}
