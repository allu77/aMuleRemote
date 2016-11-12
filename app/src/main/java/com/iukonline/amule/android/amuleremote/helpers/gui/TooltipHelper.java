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

package com.iukonline.amule.android.amuleremote.helpers.gui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.TooltipDialogFragment;

import java.util.Locale;

public class TooltipHelper {
    
    public final static String TOOLTIPHELPER_SHOWN_SETTING = "tooltip_helper_shown";
    
    public final static long TOOLTIPHELPER_TOOLTIP_LOCALIZATION = 0x1L;
    
    private SharedPreferences mSettings;
    private long mShown = 0L;
    
    public TooltipHelper(SharedPreferences settings) {
        mSettings = settings;
        mShown = settings.getLong(TOOLTIPHELPER_SHOWN_SETTING, 0x0L);
    }
    
    public TooltipDialogFragment getNextTooltipDialog() {
        if ((mShown & TOOLTIPHELPER_TOOLTIP_LOCALIZATION) == 0L) {
            Locale l = Locale.getDefault();
            if (l.getLanguage().equals("en") || l.getLanguage().equals("it")) {
                // No tips to show
                setTooltipShown(TOOLTIPHELPER_TOOLTIP_LOCALIZATION);
            //} else if (l.getLanguage().equals("pt") && l.getCountry().equals("BR")) {
            //    return new TooltipDialogFragment(TOOLTIPHELPER_TOOLTIP_LOCALIZATION, R.string.dialog_tooltip_title, R.string.dialog_tooltip_auto_localization);
            } else {
                return TooltipDialogFragment.newInstance(TOOLTIPHELPER_TOOLTIP_LOCALIZATION, R.string.dialog_tooltip_title, R.string.dialog_tooltip_no_localization);
            }
        } 
        return null;
    }
    
    public void setTooltipShown(long tooltipId) {
        mShown |= tooltipId;
        SharedPreferences.Editor e = mSettings.edit();
        e.putLong(TOOLTIPHELPER_SHOWN_SETTING, mShown);
        e.commit();
    }
    
    public void resetShown() {
        mShown = 0x0L;
        SharedPreferences.Editor e = mSettings.edit();
        e.putLong(TOOLTIPHELPER_SHOWN_SETTING, mShown);
        e.commit();
    }
    
    
    public void handleDialogClosed(AlertDialogFragment dialog, int event, Bundle values) {
        boolean isChecked = values.getBoolean(TooltipDialogFragment.BUNDLE_IS_CHECKED);
        if (isChecked) {
            setTooltipShown(values.getLong(TooltipDialogFragment.BUNDLE_TOOLTIP_ID));
        }
    }
}
