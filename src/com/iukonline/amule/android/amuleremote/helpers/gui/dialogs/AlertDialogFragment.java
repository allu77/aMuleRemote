package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.iukonline.amule.android.amuleremote.R;

public class AlertDialogFragment extends SherlockDialogFragment {
    
    protected final static String BUNDLE_TITLE = "title";
    protected final static String BUNDLE_OK_MSG = "ok_msg";
    protected final static String BUNDLE_CANCEL_MSG = "cancel_msg";
    protected final static String BUNDLE_SHOW_CANCEL = "show_cancel";
    
    protected int mTitle = -1;
    protected int mMessage = -1;
    protected Message mOkMessage;
    protected Message mCancelMessage;
    protected boolean mShowCancel;
    
    public AlertDialogFragment() {
    }
    
    public AlertDialogFragment(int title, int message, Message okMessage, Message cancelMessage, boolean showCancel) {
        mTitle = title;
        mMessage = message;
        mOkMessage = okMessage;
        mCancelMessage = cancelMessage;
        mShowCancel = showCancel;
    }
    
    public AlertDialogFragment(int title, Message okMessage, Message cancelMessage, boolean showCancel) {
        mTitle = title;
        mOkMessage = okMessage;
        mCancelMessage = cancelMessage;
        mShowCancel = showCancel;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        
        outState.putInt(BUNDLE_TITLE, mTitle);
        outState.putParcelable(BUNDLE_OK_MSG, mOkMessage);
        outState.putParcelable(BUNDLE_CANCEL_MSG, mCancelMessage);
        outState.putBoolean(BUNDLE_SHOW_CANCEL, mShowCancel);
        
        super.onSaveInstanceState(outState);
    }
    
    protected AlertDialog.Builder getDefaultAlertDialogBuilder(Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mTitle = savedInstanceState.getInt(BUNDLE_TITLE);
            mOkMessage = savedInstanceState.getParcelable(BUNDLE_OK_MSG);
            mCancelMessage = savedInstanceState.getParcelable(BUNDLE_CANCEL_MSG);
            mShowCancel = savedInstanceState.getBoolean(BUNDLE_SHOW_CANCEL);
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        
        if (mTitle >= 0) builder.setTitle(mTitle);
        if (mMessage >= 0) builder.setMessage(mMessage);
        
        builder.setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (mOkMessage != null) mOkMessage.sendToTarget();
                                }
                            }
                        );
        
        if (mShowCancel) {
            builder.setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    if (mCancelMessage != null) mCancelMessage.sendToTarget();
                                }
                            }
                        );
        }
        return builder;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return getDefaultAlertDialogBuilder(savedInstanceState).create();
    }


}
