package com.iukonline.amule.android.amuleremote;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class MyAlertDialogFragment extends DialogFragment {
    
    int mTitle;
    DialogFragmentCaller mCaller;
    AlertDialog.Builder mBuilder;
    
    interface DialogFragmentCaller {
        public void myAlertDialogPositiveClick();
        public void myAlertDialogNegativeClick();
    }
    
    public static MyAlertDialogFragment newInstance(int title, DialogFragmentCaller caller) {
        MyAlertDialogFragment frag = new MyAlertDialogFragment();
        frag.mTitle = title;
        frag.mCaller = caller;

        return frag;
    }
    
    public static MyAlertDialogFragment newInstance() {
        MyAlertDialogFragment frag = new MyAlertDialogFragment();
        return frag;
    }
    
    public AlertDialog.Builder getAlertDialogBuilder(Context context) {
        if (mBuilder == null) mBuilder = new AlertDialog.Builder(context);
        return mBuilder;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        if (mBuilder == null) {
            getAlertDialogBuilder(getActivity()).setTitle(mTitle)
            .setPositiveButton(R.string.alert_dialog_ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    mCaller.myAlertDialogPositiveClick();
                                }
                            }
                        )
                        .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    mCaller.myAlertDialogNegativeClick();
                                }
                            }
                        );

        }

        return mBuilder.create();
    }

}
