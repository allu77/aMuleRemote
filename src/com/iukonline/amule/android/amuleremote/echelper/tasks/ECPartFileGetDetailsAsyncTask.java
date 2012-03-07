package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.ec.ECException;
import com.iukonline.amule.ec.ECPartFile;


public class ECPartFileGetDetailsAsyncTask extends AmuleAsyncTask {

    ECPartFile mECPartFile;
    
    public ECPartFileGetDetailsAsyncTask setECPartFile(ECPartFile file) {
        mECPartFile = file;
        return this;
    }
    
    @Override
    protected String backgroundTask() throws ECException, UnknownHostException, SocketTimeoutException, IOException {
        if (isCancelled()) return null;
        mECPartFile.setClient(mECClient);
        mECPartFile.refresh(true);
        return null;
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyECPartFileWatchers(mECPartFile);
    }
}
