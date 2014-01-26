package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.iukonline.amule.android.amuleremote.R;

public class AlertDialogFragment extends SherlockDialogFragment {
    
    public final static int ALERTDIALOG_EVENT_OK = 1;
    public final static int ALERTDIALOG_EVENT_CANCEL = 2;
    
    public interface AlertDialogListener {
        public void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values);
    }
    
    protected final static String BUNDLE_TITLE = "title";
    protected final static String BUNDLE_MESSAGE = "message";
    protected final static String BUNDLE_TITLE_STR = "title_str";
    protected final static String BUNDLE_MESSAGE_STR = "message_str";

    
    protected final static String BUNDLE_OK_MSG = "ok_msg";
    protected final static String BUNDLE_CANCEL_MSG = "cancel_msg";
    protected final static String BUNDLE_SHOW_CANCEL = "show_cancel";
    
    protected int mTitle = -1;
    protected int mMessage = -1;
    protected String mTitleStr;
    protected String mMessageStr;
    protected Message mOkMessage;
    protected Message mCancelMessage;
    protected boolean mShowCancel;
    
    public AlertDialogFragment() {
    }
    
    public AlertDialogFragment(int title, int message, boolean showCancel) {
        mTitle = title;
        mMessage = message;
        mShowCancel = showCancel;
    }

    public AlertDialogFragment(String title, String message, boolean showCancel) {
        mTitleStr = title;
        mMessageStr = message;
        mShowCancel = showCancel;
    }
    
    public AlertDialogFragment(int message, boolean showCancel) {
        mMessage = message;
        mShowCancel = showCancel;
    }

    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        
        outState.putInt(BUNDLE_TITLE, mTitle);
        outState.putInt(BUNDLE_MESSAGE, mMessage);
        outState.putString(BUNDLE_TITLE_STR, mTitleStr);
        outState.putString(BUNDLE_MESSAGE_STR, mMessageStr);

        outState.putParcelable(BUNDLE_OK_MSG, mOkMessage);
        outState.putParcelable(BUNDLE_CANCEL_MSG, mCancelMessage);
        outState.putBoolean(BUNDLE_SHOW_CANCEL, mShowCancel);
        
        super.onSaveInstanceState(outState);
    }
    
    protected AlertDialog.Builder getDefaultAlertDialogBuilder(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getInt(BUNDLE_TITLE);
            mMessage = savedInstanceState.getInt(BUNDLE_MESSAGE);
            mTitleStr = savedInstanceState.getString(BUNDLE_TITLE_STR);
            mMessageStr = savedInstanceState.getString(BUNDLE_MESSAGE_STR);


            mOkMessage = savedInstanceState.getParcelable(BUNDLE_OK_MSG);
            mCancelMessage = savedInstanceState.getParcelable(BUNDLE_CANCEL_MSG);
            mShowCancel = savedInstanceState.getBoolean(BUNDLE_SHOW_CANCEL);
        }
        
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
                                    if (mOkMessage != null) mOkMessage.sendToTarget();
                                }
                            }
                        );
        
        if (mShowCancel) {
            builder.setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    sendEventToListener(ALERTDIALOG_EVENT_CANCEL, null);
                                    if (mCancelMessage != null) mCancelMessage.sendToTarget();
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
