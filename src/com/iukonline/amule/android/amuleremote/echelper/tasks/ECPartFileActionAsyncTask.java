package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.iukonline.amule.ec.ECException;
import com.iukonline.amule.ec.ECPartFile;


public class ECPartFileActionAsyncTask extends AmuleAsyncTask {
    
    public enum ECPartFileAction {
        PAUSE, RESUME, DELETE, 
        A4AF_NOW, A4AF_AUTO, A4AF_AWAY, 
        PRIO_LOW, PRIO_NORMAL, PRIO_HIGH, PRIO_AUTO
    }

    ECPartFile mECPartFile;
    ECPartFileAction mAction;
    
    public ECPartFileActionAsyncTask setECPartFile(ECPartFile file) {
        mECPartFile = file;
        return this;
    }
    
    public ECPartFileActionAsyncTask setAction(ECPartFileAction action) {
        mAction = action;
        return this;
    }

    @Override
    protected String backgroundTask() throws ECException, UnknownHostException, SocketTimeoutException, IOException {
        
        switch (mAction) {
        case DELETE:
            mECPartFile.remove();
            if (isCancelled()) return null;
            return null;
        case PAUSE:
            mECPartFile.pause();
            break;
        case RESUME:
            mECPartFile.resume();
            break;
        case A4AF_NOW:
            mECPartFile.swapA4AFThis();
            break;
        case A4AF_AUTO:
            mECPartFile.swapA4AFThisAuto();
            break;
        case A4AF_AWAY:
            mECPartFile.swapA4AFOthers();
            break;
        case PRIO_LOW:
            mECPartFile.changePriority(ECPartFile.PR_LOW);
            break;
        case PRIO_NORMAL:
            mECPartFile.changePriority(ECPartFile.PR_NORMAL);
            break;
        case PRIO_HIGH:
            mECPartFile.changePriority(ECPartFile.PR_HIGH);
            break;
        case PRIO_AUTO:
            mECPartFile.changePriority(ECPartFile.PR_AUTO);
            break;
        }
        return null;
    }

    @Override
    protected void notifyResult() {
        if (mAction == ECPartFileAction.DELETE) {
            // TODO need to finish detail activity or do something to detail fragment...
        }
    }

}
