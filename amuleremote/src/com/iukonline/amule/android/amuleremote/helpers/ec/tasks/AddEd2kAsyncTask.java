package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

public class AddEd2kAsyncTask extends AmuleAsyncTask {
    
    String mEd2kUrl;
    
    public void setEd2kUrl(String url) {
        mEd2kUrl = url;
    }

    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, ECClientException, ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        mECClient.addED2KLink(mEd2kUrl);
    }

    @Override
    protected void notifyResult() {
        Toast.makeText(mECHelper.getApplication(), R.string.dialog_ed2k_notify, Toast.LENGTH_LONG).show();
    }
}
