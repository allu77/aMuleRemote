package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.util.Log;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.search.SearchContainer;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

public class SearchAsyncTask extends AmuleAsyncTask {
    
    private SearchContainer mSearch;
    private ECSearchStatus mTargetStatus;
    private String mResult;
    
    public void setSearchContainer(SearchContainer s) {
        mSearch = s;
    }
    
    public void setTargetStatus(ECSearchStatus s) {
        mTargetStatus = s;
    }

    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, AmuleAsyncTaskException, ECClientException, ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        
        
        switch (mTargetStatus) {
        case STOPPED:
            if (mSearch.mSearchStatus == ECSearchStatus.RUNNING) {
                mECClient.searchStop();
                
                // TODO: Provide string resource
                mResult = "Search stopped";
                mSearch.mSearchStatus = ECSearchStatus.STOPPED;
            } else {
                // TODO: Provide string resource
                throw new AmuleAsyncTaskException("Only running searches can be stopped");
            }
            break;
        case RUNNING:
            switch (mSearch.mSearchStatus) {
            case STARTING:
                try {
                    if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: Starting search");
                    mResult = mECClient.searchStart(mSearch.mFileName, mSearch.mType, mSearch.mExtension, mSearch.mMinSize, mSearch.mMaxSize, mSearch.mAvailability, mSearch.mSearchType);
                    mSearch.mSearchStatus = ECSearchStatus.RUNNING;
                } catch (ECServerException e) {
                    mResult = e.getMessage();
                    mSearch.mSearchStatus = ECSearchStatus.FAILED;
                }
                break;
            case RUNNING:
                if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: Refreshing search progress");
                mSearch.mSearchProgress = mECClient.searchProgress();
                if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: progress = " + mSearch.mSearchProgress);
                if (mSearch.mSearchProgress == 100) mSearch.mSearchStatus = ECSearchStatus.FINISHED;
                if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: new status = " + mSearch.mSearchStatus.toString());
                if (isCancelled()) return;
                mSearch.mResults = mECClient.searchGetReults(mSearch.mResults);
                break;
            default:
                // TODO: Provide string resource
                throw new AmuleAsyncTaskException("This search can't be started or refreshed");
            }
            
            break;
        default:
            throw new AmuleAsyncTaskException("Searches can't be set to status " + mTargetStatus);
        }

    }

    @Override
    protected void notifyResult() {
        if (mResult != null) Toast.makeText(mECHelper.getApplication(), mResult, Toast.LENGTH_LONG).show();
        mECHelper.notifyECSearchListWatcher();
    }

}
