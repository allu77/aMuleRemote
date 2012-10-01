package com.iukonline.amule.android.amuleremote.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.iukonline.amule.android.amuleremote.R;

public class ManualBugReportDialogFragment extends AlertDialogFragment {
    
    public final static String BUNDLE_COMMENTS = "comments";
    
    protected EditText mInput;

    public ManualBugReportDialogFragment() {}
    
    public ManualBugReportDialogFragment(Message okMessage) {
        mOkMessage = okMessage;
        mShowCancel = true;
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mInput != null) outState.putString(BUNDLE_COMMENTS, mInput.getText().toString());
        super.onSaveInstanceState(outState);
    }
    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        
        View manualView = getActivity().getLayoutInflater().inflate(R.layout.dialog_manual_bug_report, null);
        mInput = (EditText) manualView.findViewById(R.id.manual_bug_report_comment);
        
        if (savedInstanceState != null) {
            mInput.setText(savedInstanceState.getString(BUNDLE_COMMENTS));
        }
        
        builder.setView(manualView);
        builder.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if (mOkMessage != null) {
                                    Bundle b = new Bundle();
                                    b.putString(BUNDLE_COMMENTS, mInput.getText().toString()); 
                                    mOkMessage.setData(b);
                                    mOkMessage.sendToTarget();
                                }
                            }
                        }
                    );
        return builder.create();
    }

}
