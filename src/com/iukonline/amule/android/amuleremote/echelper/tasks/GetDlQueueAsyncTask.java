package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import com.iukonline.amule.ec.ECException;
import com.iukonline.amule.ec.ECPartFile;


public class GetDlQueueAsyncTask extends AmuleAsyncTask {
    
    private ArrayList <ECPartFile> mDlQueue; 

    @Override
    protected String backgroundTask() throws ECException, UnknownHostException, SocketTimeoutException, IOException {
        
        if (isCancelled()) return null;
        ECPartFile[] tmpList = mECClient.getDownloadQueue();
        if (isCancelled()) return null;
        if (tmpList != null) {
            mDlQueue = new ArrayList<ECPartFile>(Arrays.asList(tmpList));
        } else {
            mDlQueue = new ArrayList<ECPartFile>();
        }
        
        return null;
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyDlQueueWatchers(mDlQueue);
        
        // TODO .. find a better way...
        mECHelper.getApplication().mainNeedsRefresh = false;
    }

}
