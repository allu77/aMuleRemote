package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.ec.ECException;
import com.iukonline.amule.ec.ECStats;


public class GetECStatsAsyncTask extends AmuleAsyncTask {
    
    ECStats mECStats;

    @Override
    protected String backgroundTask() throws ECException, UnknownHostException, SocketTimeoutException, IOException {
        if (isCancelled()) return null;
        mECStats = mECClient.getStats();
        return null;
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyECStatsWatchers(mECStats);
    }

}
