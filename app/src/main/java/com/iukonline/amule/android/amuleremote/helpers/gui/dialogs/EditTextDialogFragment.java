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
import android.content.DialogInterface;
import android.os.Build;
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

        mInput = new EditText(getActivity());

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            /* Bug on Froyo prevents margin to be set in FrameLayout
               http://stackoverflow.com/questions/24842244/margins-inside-framelayout-is-not-working-in-android-2-2
               Let's skip older release for now
             */

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.leftMargin = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
            params.rightMargin = params.leftMargin;

            mInput.setLayoutParams(params);
        }

        FrameLayout container = new FrameLayout(getActivity());
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
