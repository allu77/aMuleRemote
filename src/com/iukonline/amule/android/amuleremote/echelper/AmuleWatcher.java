package com.iukonline.amule.android.amuleremote.echelper;

import java.util.ArrayList;

import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;


public abstract interface AmuleWatcher {
    public interface ClientStatusWatcher extends AmuleWatcher {
        
        public enum AmuleClientStatus {
            IDLE, ERROR, WORKING, NOT_CONNECTED
        }
    
        void notifyStatusChange(AmuleClientStatus status); 
    }

    public interface DlQueueWatcher extends AmuleWatcher {
        void updateDlQueue(ArrayList<ECPartFile> newDlQueue);
    }

    public interface ECStatsWatcher extends AmuleWatcher {
        void updateECStats(ECStats newStats);
    }
    
    public interface ECPartFileWatcher extends AmuleWatcher {
        void updateECPartFile(ECPartFile newECPartFile);
    }

    String getWatcherId();
}

