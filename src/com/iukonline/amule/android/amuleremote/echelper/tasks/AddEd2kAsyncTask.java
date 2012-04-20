package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.android.amuleremote.R;
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
        mECClient.addED2KLink(mEd2kUrl);
        if (isCancelled()) return null;
        return null;
    }

    @Override
    protected void notifyResult() {
        Toast.makeText(mECHelper.getApplication(), R.string.dialog_ed2k_notify, Toast.LENGTH_LONG).show();
    }
}
