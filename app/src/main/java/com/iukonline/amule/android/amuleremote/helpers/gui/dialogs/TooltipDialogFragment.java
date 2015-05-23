package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.iukonline.amule.android.amuleremote.R;

public class TooltipDialogFragment extends AlertDialogFragment {
    
    public final static String BUNDLE_TOOLTIP_ID = "tooltip_id";
    public final static String BUNDLE_IS_CHECKED = "is_checked";

    protected long mTooltipId;
    protected CheckBox mCheckBox;
    
    public TooltipDialogFragment() {}
    
    public TooltipDialogFragment(long tooltipId, int title, int msg) {
        super(title, msg, false);
        mTooltipId = tooltipId;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(BUNDLE_TOOLTIP_ID, mTooltipId);
        super.onSaveInstanceState(outState);
    }

    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        
        if (savedInstanceState != null) {
            mTooltipId = savedInstanceState.getInt(BUNDLE_TOOLTIP_ID);
        }

        View checkBoxView = View.inflate(getActivity(), R.layout.dialog_tooltip, null);
        mCheckBox = (CheckBox) checkBoxView.findViewById(R.id.dialog_tooltip_checkbox);
        
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);
        builder.setView(checkBoxView);
        
        builder.setPositiveButton(R.string.alert_dialog_ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Bundle b = new Bundle();
                                b.putBoolean(BUNDLE_IS_CHECKED, mCheckBox.isChecked());
                                b.putLong(BUNDLE_TOOLTIP_ID, mTooltipId);
                                sendEventToListener(ALERTDIALOG_EVENT_OK, b);
                            }
                        }
                    );
     
        return builder.create();
    }
    
}
