package com.iukonline.amule.android.amuleremote.helpers.gui;

import com.iukonline.amule.android.amuleremote.R;

import android.annotation.SuppressLint;
import android.content.Context;

public class GUIUtils {
    @SuppressLint("DefaultLocale")
    public static String longToBytesFormatted(long in) {
        if (in > 1073741824) {
            return String.format("%.1fGB", (float) in / 1073741824f);
        } else if (in > 1048576) {
            return String.format("%.1fMB", (float) in / 1048576f);
        } else if (in > 1024) {
            return String.format("%.1fkB", (float) in / 1024f);
        } else {
            return new String(in + "B");
        }
    }
    
    public static String getETA(Context c, long remaining, long speed) {
        
        String sEta;
        if (speed > 0) {
            long eta = remaining / speed;
            if (eta < 60) {
                sEta = c.getResources().getString(R.string.guiutils_eta_secs, eta);
            } else if (eta < 3600) {
                sEta = c.getResources().getString(R.string.guiutils_eta_mins, eta / 60, eta % 60); 
            } else if (eta < 86400) {
                sEta = c.getResources().getString(R.string.guiutils_eta_hours,  eta / 3600, (eta % 3600) / 60);
            } else {
                sEta = c.getResources().getString(R.string.guiutils_eta_days,  eta / 86400);
            }
        } else {
            sEta = "";
        }
        return sEta;
    }

}
