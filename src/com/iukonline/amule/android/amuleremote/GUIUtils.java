package com.iukonline.amule.android.amuleremote;

public class GUIUtils {
    static String longToBytesFormatted(long in) {
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
    
    static String getETA(long remaining, long speed) {
        
     // TODO Convert to string resources...
        
        String sEta;
        if (speed > 0) {
            long eta = remaining / speed;
            if (eta < 60) {
                sEta = Long.toString(eta) + " secs";
            } else if (eta < 3600) {
                sEta = String.format("%d:%02d mins", eta / 60, eta % 60);
            } else if (eta < 86400) {
                sEta = String.format("%d:%02d hours", eta / 3600, (eta % 3600) / 60);
            } else {
                sEta = Long.toString(eta / 86400) + " days";
            }
        } else {
            sEta = "";
        }
        return sEta;
    }

}
