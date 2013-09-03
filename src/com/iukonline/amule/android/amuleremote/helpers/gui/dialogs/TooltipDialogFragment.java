package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.AlertDialog;
import android.os.Bundle;

public class TooltipDialogFragment extends AlertDialogFragment {
    
    public final static int TOOLTIP_NOT_LOCALIZED = 1;
    public final static int TOOLTIP_AUTO_LOCALIZED = 2;
    
    public final static String BUNDLE_TOOLTIP_ID = "tooltip_id";

    protected int mTooltipId;
    
    public TooltipDialogFragment() {}
    
    public TooltipDialogFragment(int tooltipId) {
        super("TEST_TITLE", "TEST_MSG", false);
        mTooltipId = tooltipId;
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(BUNDLE_TOOLTIP_ID, mTooltipId);
        super.onSaveInstanceState(outState);
    }

    protected AlertDialog.Builder getDefaultAlertDialogBuilder(Bundle savedInstanceState) {
        AlertDialog.Builder builder = super.getDefaultAlertDialogBuilder(savedInstanceState);

        if (savedInstanceState != null) {
            mTooltipId = savedInstanceState.getInt(BUNDLE_TOOLTIP_ID);
        }
        
        // ADD CHECKBOX, BIND EVENT ON OK
        
        return builder;
    }
    
}
