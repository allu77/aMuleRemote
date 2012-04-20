package com.iukonline.amule.android.amuleremote.dialogs;

import com.iukonline.amule.android.amuleremote.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Message;
import android.widget.EditText;

public class EditTextDialogFragment extends AlertDialogFragment {
    
    public final static String BUNDLE_EDIT_STRING = "edit_string";
    
    protected EditText mInput;
    protected String mDefaultText;
    
    public EditTextDialogFragment() {}
    
    public EditTextDialogFragment(int title, String defaultText, Message okMessage, Message cancelMessage) {
        super(title, okMessage, cancelMessage, true);
        mDefaultText = defaultText;
    }
    
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
                                if (mOkMessage != null) {
                                    Bundle b = new Bundle();
                                    b.putString(BUNDLE_EDIT_STRING, mInput.getText().toString()); 
                                    mOkMessage.setData(b);
                                    mOkMessage.sendToTarget();
                                }
                            }
                        }
                    );
        return builder.create();
    }


}
