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

import android.annotation.SuppressLint;
import android.content.Context;

import com.iukonline.amule.android.amuleremote.R;

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

    public static int chooseFontColor (int backgroundColor) {
        // http://stackoverflow.com/questions/3942878/how-to-decide-font-color-in-white-or-black-depending-on-background-color

        long r = backgroundColor / 65536L;
        long g = (backgroundColor % 65536L) / 256L;
        long b = backgroundColor % 256L;

        double lR = ((double) r) / 255.0;
        double lG = ((double) g) / 255.0;
        double lB = ((double) b) / 255.0;

        lR = lR < 0.03928 ? lR / 12.92 : Math.pow((lR + 0.055) / 1.055, 2.4);
        lG = lG < 0.03928 ? lG / 12.92 : Math.pow((lG + 0.055) / 1.055, 2.4);
        lB = lB < 0.03928 ? lB / 12.92 : Math.pow((lB + 0.055) / 1.055, 2.4);

        double L = 0.2126 * lR + 0.7152 * lG + 0.0722 * lB;

        return L > 0.179 ? 0 : 0xffffff;
    }

}
