package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;


public class GetECStatsAsyncTask extends AmuleAsyncTask {
    
    ECStats mECStats;

    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, ECClientException, ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        mECStats = mECClient.getStats(ECCodes.EC_DETAIL_CMD);
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyECStatsWatchers(mECStats);
    }

}
