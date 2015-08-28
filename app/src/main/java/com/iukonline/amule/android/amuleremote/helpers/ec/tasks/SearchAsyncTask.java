/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import android.util.Log;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.search.SearchContainer;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

import java.io.IOException;

public class SearchAsyncTask extends AmuleAsyncTask {

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
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
    protected void backgroundTask() throws IOException, AmuleAsyncTaskException, ECClientException, ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        
        
        switch (mTargetStatus) {
        case STOPPED:
            if (mSearch.mSearchStatus == ECSearchStatus.RUNNING) {
                mECClient.searchStop();
                
                mResult = mECHelper.mApp.getResources().getString(R.string.search_task_stopped);
                mSearch.mSearchStatus = ECSearchStatus.STOPPED;
            } else {
                throw new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.search_task_cannot_stop));
            }
            break;
        case RUNNING:
            switch (mSearch.mSearchStatus) {
            case STARTING:
                try {
                    if (DEBUG) Log.d(TAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: Starting search");
                    mResult = mECClient.searchStart(mSearch.mFileName, mSearch.mType, mSearch.mExtension, mSearch.mMinSize, mSearch.mMaxSize, mSearch.mAvailability, mSearch.mSearchType);
                    mSearch.mSearchStatus = ECSearchStatus.RUNNING;
                } catch (ECServerException e) {
                    mResult = e.getMessage();
                    mSearch.mSearchStatus = ECSearchStatus.FAILED;
                }
                break;
            case RUNNING:
                if (DEBUG) Log.d(TAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: Refreshing search progress");
                mSearch.mSearchProgress = mECClient.searchProgress();
                if (DEBUG) Log.d(TAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: progress = " + mSearch.mSearchProgress);
                if (mSearch.mSearchProgress == 100) mSearch.mSearchStatus = ECSearchStatus.FINISHED;
                if (DEBUG) Log.d(TAG, "SearchAsyncTask.backgroundTask: Launching backgroundTask: new status = " + mSearch.mSearchStatus.toString());
                if (isCancelled()) return;
                mSearch.mResults = mECClient.searchGetReults(mSearch.mResults);
                break;
            default:
                throw new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.search_task_cannot_start));
            }
            
            break;
        default:
            throw new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.search_task_cannot_set_status, mTargetStatus));
        }

    }

    @Override
    protected void notifyResult() {
        if (mResult != null) Toast.makeText(mECHelper.getApplication(), mResult, Toast.LENGTH_LONG).show();
        mECHelper.notifyECSearchListWatcher();
    }

}
