package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;


public class ECPartFileActionAsyncTask extends AmuleAsyncTask {
    
    public enum ECPartFileAction {
        PAUSE, RESUME, DELETE, 
        A4AF_NOW, A4AF_AUTO, A4AF_AWAY, 
        PRIO_LOW, PRIO_NORMAL, PRIO_HIGH, PRIO_AUTO,
        RENAME
    }

    ECPartFile mECPartFile;
    ECPartFileAction mAction;
    
    String mStringParam = null;
    
    public ECPartFileActionAsyncTask setECPartFile(ECPartFile file) {
        mECPartFile = file;
        return this;
    }
    
    public ECPartFileActionAsyncTask setAction(ECPartFileAction action) {
        mAction = action;
        return this;
    }

    public ECPartFileActionAsyncTask setStringParam(String param) {
        mStringParam = param;
        return this;
    }

    
    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, AmuleAsyncTaskException, ECClientException, ECPacketParsingException, ECServerException {
        
        if (isCancelled()) return;
        switch (mAction) {
        case DELETE:
            mECClient.changeDownloadStatus(mECPartFile.getHash(), ECCodes.EC_OP_PARTFILE_DELETE);
            break;
        case PAUSE:
            mECClient.changeDownloadStatus(mECPartFile.getHash(), ECCodes.EC_OP_PARTFILE_PAUSE);
            break;
        case RESUME:
            mECClient.changeDownloadStatus(mECPartFile.getHash(), ECCodes.EC_OP_PARTFILE_RESUME);
            break;
        case A4AF_NOW:
            mECClient.changeDownloadStatus(mECPartFile.getHash(), ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS);
            break;
        case A4AF_AUTO:
            mECClient.changeDownloadStatus(mECPartFile.getHash(), ECCodes.EC_OP_PARTFILE_SWAP_A4AF_THIS_AUTO);
            break;
        case A4AF_AWAY:
            mECClient.changeDownloadStatus(mECPartFile.getHash(), ECCodes.EC_OP_PARTFILE_SWAP_A4AF_OTHERS);
            break;
        case PRIO_LOW:
            mECClient.setPartFilePriority(mECPartFile.getHash(), ECPartFile.PR_LOW);
            break;
        case PRIO_NORMAL:
            mECClient.setPartFilePriority(mECPartFile.getHash(), ECPartFile.PR_NORMAL);
            break;
        case PRIO_HIGH:
            mECClient.setPartFilePriority(mECPartFile.getHash(), ECPartFile.PR_HIGH);
            break;
        case PRIO_AUTO:
            mECClient.setPartFilePriority(mECPartFile.getHash(), ECPartFile.PR_AUTO);
            break;
        case RENAME:
            if (mStringParam == null) throw new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.partfile_task_rename_noname));
            mECClient.renamePartFile(mECPartFile.getHash(), mStringParam);
            break;
        }
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyECPartFileActionWatchers(mECPartFile, mAction);
    }

}
