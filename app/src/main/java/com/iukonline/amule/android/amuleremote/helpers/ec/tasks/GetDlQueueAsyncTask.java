package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;


public class GetDlQueueAsyncTask extends AmuleAsyncTask {
    
    private HashMap<String, ECPartFile> mDlQueue;
    
    public void setDlQueue(HashMap<String, ECPartFile> q) {
        mDlQueue = q;
    }

    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, ECClientException, ECPacketParsingException, ECServerException {
        
        if (isCancelled()) return;
        if (mDlQueue == null) {
            mDlQueue = mECClient.getDownloadQueue();
        } else {
            mECClient.refreshDlQueue(mDlQueue);
        }
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyDlQueueWatchers(mDlQueue);
        
        // TODO .. find a better way...
        mECHelper.getApplication().mainNeedsRefresh = false;
    }

}
