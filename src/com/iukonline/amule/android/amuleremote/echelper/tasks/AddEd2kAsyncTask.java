package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.ec.ECException;

import android.widget.Toast;

public class AddEd2kAsyncTask extends AmuleAsyncTask {
    
    String mEd2kUrl;
    
    public void setEd2kUrl(String url) {
        mEd2kUrl = url;
    }

    @Override
    protected String backgroundTask() throws ECException, UnknownHostException, SocketTimeoutException, IOException {
        if (isCancelled()) return null;
        mECClient.addED2KURL(mEd2kUrl);
        if (isCancelled()) return null;
        return null;
    }

    @Override
    protected void notifyResult() {
        // TODO: Localize
        Toast.makeText(mECHelper.getApplication(), "Ed2k URL added", Toast.LENGTH_LONG).show();
    }
}
