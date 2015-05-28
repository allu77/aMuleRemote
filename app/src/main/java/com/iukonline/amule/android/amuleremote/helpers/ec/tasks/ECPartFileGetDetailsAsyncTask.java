/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;


public class ECPartFileGetDetailsAsyncTask extends AmuleAsyncTask {

    ECPartFile mECPartFile;
    
    public ECPartFileGetDetailsAsyncTask setECPartFile(ECPartFile file) {
        mECPartFile = file;
        return this;
    }
    
    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, ECClientException, ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        mECClient.refreshPartFile(mECPartFile, ECCodes.EC_DETAIL_FULL);
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyECPartFileWatchers(mECPartFile);
    }
}
