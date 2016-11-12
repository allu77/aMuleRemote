/*
 * Copyright (c) 2015. Gianluca Vegetti
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.ECHelper;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECRawPacket;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public abstract class AmuleAsyncTask extends AsyncTask<Void, Void, Exception> {

    public enum TaskScheduleMode { BEST_EFFORT, PREEMPTIVE, QUEUE } 
    public enum TaskScheduleQueueStatus { QUEUED, LAUNCHED }

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    protected ECHelper mECHelper;
    protected TaskScheduleMode mMode;
    protected ECClient mECClient;
    protected TaskScheduleQueueStatus mQueueStatus;
    
    protected Exception mPreExecuteError;
    
    public void initialize(ECHelper helper) {
        mECHelper = helper;
    }


    public TaskScheduleQueueStatus getQueueStatus() {
        return mQueueStatus;
    }


    public void setQueueStatus(TaskScheduleQueueStatus mQueueStatus) {
        this.mQueueStatus = mQueueStatus;
    }


    @Override
    protected void onPreExecute() {
        mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.WORKING);
    }
    
    
    // NOTE: Every thrown exception must also be handled in onPostExecute
    abstract protected void backgroundTask() throws IOException, AmuleAsyncTaskException, ECClientException, ECPacketParsingException, ECServerException;
    
    protected Exception doInBackground(Void... params) {
        
        if (mPreExecuteError != null) return mPreExecuteError;
        try {
            if (DEBUG) Log.d(TAG, "Requesting ECClient");
            mECClient = mECHelper.getECClient();
            if (mECClient == null) return new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.error_null_client));
        } catch (UnknownHostException e1) {
            return e1;
        } catch (IOException e1) {
            return e1;
        } catch (IllegalArgumentException e1) {
            return e1;            
        }
        
        try {
            if (DEBUG) Log.d(TAG, "Launching backgroundTask: " + getClass().getName());
            backgroundTask();
            if (DEBUG) Log.d(TAG, "backgroundTask finished");
        } catch (IOException e) {
            
            if (mECClient.isStateful()) {
                return e; // If client is stateful, we need to refresh all data
            }
            if (isCancelled()) return null;
            
            if (DEBUG) Log.d(TAG, "Resetting ECClient");
            mECHelper.resetClient();
            
            try {
                if (DEBUG) Log.d(TAG, "Requesting ECClient");
                mECClient = mECHelper.getECClient();
                if (mECClient == null) return new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.error_null_client));
                if (DEBUG) Log.d(TAG, "Launching backgroundTask: " + getClass().getName());
                backgroundTask();
                if (DEBUG) Log.d(TAG, "backgroundTask finished");
                
            } catch (Exception e2) {
                return isCancelled() ? null : e2;
            }
        } catch (Exception e) {
            return isCancelled() ? null : e;
        }
        
        return null;
    }
    
    abstract protected void notifyResult();
    
    @Override
    protected void onCancelled() {
        mECHelper.resetClient();
        mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.IDLE);
    }
    
    @TargetApi(Build.VERSION_CODES.FROYO)
    protected void logToDropBox(String msg) {
        // mDropBox is not null only for >= FROYO
        if (mECHelper.mDropBox != null) {
            mECHelper.mDropBox.addText(TAG, msg);
        }
        
    }
    
    @Override
    protected void onPostExecute(Exception result) {
        
        // TBV: This should force GUI clean up for stateful clients if needed
        mECHelper.checkStaleDataOnGUI();
        
        if (result == null) {
            mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.IDLE);
            notifyResult();
        } else {
            
            Resources r = mECHelper.mApp.getResources();
            String notifyText;
            
            if (result instanceof ECServerException) {
                notifyText = r.getText(R.string.error_server) + " - " + result.getMessage();
            } else if (result instanceof ECClientException) {
                notifyText = r.getText(R.string.error_client) + " - " + result.getMessage();
                if (DEBUG) {
                    Log.e(TAG, notifyText);
                    ECPacket req = ((ECClientException)result).getRequestPacket();
                    ECPacket resp = ((ECClientException)result).getResponsePacket();
                    if (req != null) {
                        ECRawPacket rr = req.getRawPacket();
                        if (rr != null) {
                            logToDropBox("Request:\n" + rr.dump());
                        }
                    }
                    if (resp != null) {
                        ECRawPacket rr = resp.getRawPacket();
                        if (rr != null) {
                            logToDropBox("Response:\n" + rr.dump());
                        }
                    }
                }
                mECHelper.sendParsingExceptionIfEnabled(result);
            } else if (result instanceof ECPacketParsingException) {
                notifyText = r.getText(R.string.error_packet_parsing) + " - " + result.getMessage();
                if (DEBUG) {
                    Log.e(TAG, notifyText);
                    ECRawPacket p = ((ECPacketParsingException)result).getCausePacket();
                    if (p != null) {
                        logToDropBox("Packet");
                        
                        String[] lines = p.dump().split("\\n");
                        for (int i = 0; i < lines.length; i++) logToDropBox(lines[i]);
                        
                    }
                }
                mECHelper.sendParsingExceptionIfEnabled(result);
            } else if (result instanceof UnknownHostException) {
                notifyText = r.getText(R.string.error_host).toString();
            } else if (result instanceof SocketTimeoutException) {
                notifyText = r.getText(R.string.error_timeout).toString();
            } else if (result instanceof IOException) {
                notifyText = r.getText(R.string.error_io) + " - " + result.getMessage();
            } else if (result instanceof AmuleAsyncTaskException) {
                notifyText = result.getMessage();
            } else {
                notifyText = r.getText(R.string.error_unexpected).toString();
                if (DEBUG) Log.e(TAG, "Got an unexpected exception " + result.getClass().getName() + " for background task " + getClass().getName());
            }

            //Toast.makeText(mECHelper.getApplication(), notifyText, Toast.LENGTH_LONG).show();
            //if (mECHelper.getApplication().mOnTopActivity != null) Crouton.makeText(mECHelper.getApplication().mOnTopActivity, notifyText, Style.ALERT).show();
            mECHelper.getApplication().notifyErrorOnGUI(notifyText);
            mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.ERROR);
        }
    }
    

    @Override
    public String toString() {
        String className = getClass().getName();
        
        return String.format("Class: %s, Status: %s", className.substring(className.lastIndexOf('.') + 1), getStatus());
    }
    
    
    
}
