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

}
