/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.iukonline.amule.android.amuleremote.R;

public class EditTextDialogFragment extends AlertDialogFragment {
    

    public final static String BUNDLE_EDIT_STRING = "edit_string";
    
    protected EditText mInput;
    protected String mDefaultText;
    
    public EditTextDialogFragment() {}
    
    public static EditTextDialogFragment newInstance(int title, String defaultText) {
        EditTextDialogFragment fragment = new EditTextDialogFragment();
        fragment.setAlertDialogFragmentArguments(title, true);

        Bundle args = fragment.getArguments();
        args.putString(BUNDLE_EDIT_STRING, defaultText);
        fragment.setArguments(args);

        return fragment;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mInput != null) outState.putString(BUNDLE_EDIT_STRING, mInput.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle b = savedInstanceState == null ? getArguments() : savedInstanceState;

        if (b != null) {
            mDefaultText = b.getString(BUNDLE_EDIT_STRING);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);


        FrameLayout container = new FrameLayout(getActivity());
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        params.rightMargin = params.leftMargin;

        mInput = new EditText(getActivity());
        mInput.setLayoutParams(params);

        container.addView(mInput);
        if (mDefaultText != null) mInput.setText(mDefaultText);
        
        if (savedInstanceState != null) mInput.setText(savedInstanceState.getString(BUNDLE_EDIT_STRING));

        builder.setView(container);
        builder.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Bundle b = new Bundle();
                                b.putString(BUNDLE_EDIT_STRING, mInput.getText().toString()); 
                                sendEventToListener(ALERTDIALOG_EVENT_OK, b);
                                /*
                                if (mOkMessage != null) {
                                    mOkMessage.setData(b);
                                    mOkMessage.sendToTarget();
                                }
                                 */

                            }
                        }
                    );
        return builder.create();
    }


}
