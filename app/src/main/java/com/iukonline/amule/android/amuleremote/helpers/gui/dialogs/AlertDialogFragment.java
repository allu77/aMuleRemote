/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.iukonline.amule.android.amuleremote.R;

public class AlertDialogFragment extends DialogFragment {
    
    public final static int ALERTDIALOG_EVENT_OK = 1;
    public final static int ALERTDIALOG_EVENT_CANCEL = 2;
    
    public interface AlertDialogListener {
        void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values);
    }
    
    protected final static String BUNDLE_TITLE = "title";
    protected final static String BUNDLE_MESSAGE = "message";
    protected final static String BUNDLE_TITLE_STR = "title_str";
    protected final static String BUNDLE_MESSAGE_STR = "message_str";

    protected final static String BUNDLE_SHOW_CANCEL = "show_cancel";
    
    protected int mTitle = -1;
    protected int mMessage = -1;
    protected String mTitleStr;
    protected String mMessageStr;
    protected boolean mShowCancel;
    
    public AlertDialogFragment() {
        super();
    }

    public static AlertDialogFragment newInstance(int title, int message, boolean showCancel) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setAlertDialogFragmentArguments(title, message, showCancel);
        return fragment;
    }

    public static AlertDialogFragment newInstance(String title, String message, boolean showCancel) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setAlertDialogFragmentArguments(title, message, showCancel);
        return fragment;
    }
    public static AlertDialogFragment newInstance(int title, boolean showCancel) {
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setAlertDialogFragmentArguments(title, showCancel);
        return fragment;
    }

    protected void setAlertDialogFragmentArguments(int title, int message, boolean showCancel) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_TITLE, title);
        args.putInt(BUNDLE_MESSAGE, message);
        args.putBoolean(BUNDLE_SHOW_CANCEL, showCancel);
        setArguments(args);
    }

    protected void setAlertDialogFragmentArguments(String title, String message, boolean showCancel) {
        Bundle args = new Bundle();
        args.putString(BUNDLE_TITLE_STR, title);
        args.putString(BUNDLE_MESSAGE_STR, message);
        args.putBoolean(BUNDLE_SHOW_CANCEL, showCancel);
        setArguments(args);
    }

    protected Bundle setAlertDialogFragmentArguments(int title, boolean showCancel) {
        Bundle args = new Bundle();
        args.putInt(BUNDLE_TITLE, title);
        args.putBoolean(BUNDLE_SHOW_CANCEL, showCancel);
        setArguments(args);
        return args;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getInt(BUNDLE_TITLE);
            mMessage = savedInstanceState.getInt(BUNDLE_MESSAGE);
            mTitleStr = savedInstanceState.getString(BUNDLE_TITLE_STR);
            mMessageStr = savedInstanceState.getString(BUNDLE_MESSAGE_STR);
            mShowCancel = savedInstanceState.getBoolean(BUNDLE_SHOW_CANCEL);
        } else {
            Bundle args = getArguments();
            if (args != null) {
                if (args.containsKey(BUNDLE_TITLE_STR)) mTitleStr = args.getString(BUNDLE_TITLE_STR);
                mTitle = args.getInt(BUNDLE_TITLE, mTitle);
                if (args.containsKey(BUNDLE_MESSAGE_STR)) mMessageStr = args.getString(BUNDLE_MESSAGE_STR);
                mMessage = args.getInt(BUNDLE_MESSAGE, mMessage);
                mShowCancel = args.getBoolean(BUNDLE_SHOW_CANCEL, mShowCancel);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        
        outState.putInt(BUNDLE_TITLE, mTitle);
        outState.putInt(BUNDLE_MESSAGE, mMessage);
        outState.putString(BUNDLE_TITLE_STR, mTitleStr);
        outState.putString(BUNDLE_MESSAGE_STR, mMessageStr);

        //outState.putParcelable(BUNDLE_OK_MSG, mOkMessage);
        //outState.putParcelable(BUNDLE_CANCEL_MSG, mCancelMessage);
        outState.putBoolean(BUNDLE_SHOW_CANCEL, mShowCancel);
        
        super.onSaveInstanceState(outState);
    }
    
    protected AlertDialog.Builder getDefaultAlertDialogBuilder(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        if (mTitle >= 0) {
            builder.setTitle(mTitle);
        } else if (mTitleStr != null) {
            builder.setTitle(mTitleStr);
        }
        if (mMessage >= 0) {
            builder.setMessage(mMessage);
        } else if (mMessageStr != null) {
            builder.setMessage(mMessageStr);
        }
        
        builder.setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    sendEventToListener(ALERTDIALOG_EVENT_OK, null);
                                    //if (mOkMessage != null) mOkMessage.sendToTarget();
                                }
                            }
                        );
        if (mShowCancel) {
            builder.setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    sendEventToListener(ALERTDIALOG_EVENT_CANCEL, null);
                                    //if (mCancelMessage != null) mCancelMessage.sendToTarget();
                                }
                            }
                        );
        } else {
            setCancelable(false);
        }

        return builder;
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return getDefaultAlertDialogBuilder(savedInstanceState).create();
    }
    
    protected void sendEventToListener(int event, Bundle values) {
        try {
            Activity a = getActivity();
            if (a != null) ((AlertDialogListener) a).alertDialogEvent(this, event, values);
        } catch (ClassCastException e) {
            // Do Nothing
        }
    }


}
