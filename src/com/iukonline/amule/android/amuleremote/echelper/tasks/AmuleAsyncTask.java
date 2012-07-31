package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.echelper.ECHelper;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECPacket;
import com.iukonline.amule.ec.ECRawPacket;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

public abstract class AmuleAsyncTask extends AsyncTask<Void, Void, Exception> {

    public enum TaskScheduleMode { BEST_EFFORT, PREEMPTIVE, QUEUE } 
    public enum TaskScheduleQueueStatus { QUEUED, LAUNCHED }

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
    abstract protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, AmuleAsyncTaskException, ECClientException, ECPacketParsingException, ECServerException;
    
    protected Exception doInBackground(Void... params) {
        
        if (mPreExecuteError != null) return mPreExecuteError;
        try {
            if (mECHelper.mApplication.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Requesting ECClient");
            mECClient = mECHelper.getECClient();
        } catch (UnknownHostException e1) {
            return e1;
        } catch (IOException e1) {
            return e1;
        }
        
        try {
            if (mECHelper.mApplication.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Launching backgroundTask: " + getClass().getName());
            backgroundTask();
            if (mECHelper.mApplication.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "backgroundTask finished");
        } catch (IOException e) {
            
            if (mECClient.isStateful()) {
                mECHelper.setClientStale();
                return e; // If client is stateful, we need to refresh all data
            }
            if (isCancelled()) return null;
            
            if (mECHelper.mApplication.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Resetting ECClient");
            mECHelper.resetClient();
            
            try {
                if (mECHelper.mApplication.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Requesting ECClient");
                mECClient = mECHelper.getECClient();
                if (mECHelper.mApplication.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Launching backgroundTask: " + getClass().getName());
                backgroundTask();
                if (mECHelper.mApplication.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "backgroundTask finished");
                
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
    
    @Override
    protected void onPostExecute(Exception result) {
        if (result == null) {
            mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.IDLE);
            notifyResult();
        } else {

            // TODO: Use string resources
            
            String notifyText = "Unhandled error";
            
            if (result instanceof ECServerException) {
                notifyText = "Server retuned an error - " + result.getMessage();
            } else if (result instanceof ECClientException) {
                notifyText = "Error building request - " + result.getMessage();
                if (mECHelper.mApplication.enableLog) {
                    Log.e(AmuleControllerApplication.AC_LOGTAG, notifyText);
                    ECPacket req = ((ECClientException)result).getRequestPacket();
                    ECPacket resp = ((ECClientException)result).getResponsePacket();
                    if (req != null) {
                        ECRawPacket rr = req.getRawPacket();
                        if (rr != null) {
                            mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, "Request:\n" + rr.dump());
                        }
                    }
                    if (resp != null) {
                        ECRawPacket rr = resp.getRawPacket();
                        if (rr != null) {
                            mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, "Response:\n" + rr.dump());
                        }
                    }
                }
                mECHelper.sendParsingExceptionIfEnabled(result);
            } else if (result instanceof ECPacketParsingException) {
                notifyText = "Error parsing packet - " + result.getMessage();
                if (mECHelper.mApplication.enableLog) {
                    Log.e(AmuleControllerApplication.AC_LOGTAG, notifyText);
                    ECRawPacket p = ((ECPacketParsingException)result).getCausePacket();
                    if (p != null) {
                        mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, "Packet");
                        
                        String[] lines = p.dump().split("\\n");
                        for (int i = 0; i < lines.length; i++) mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, lines[i]);
                        
                        
                        //Log.e(AmuleControllerApplication.AC_LOGTAG, "Request:");
                        //Log.e(AmuleControllerApplication.AC_LOGTAG, p.dump());
                    }
                }
                mECHelper.sendParsingExceptionIfEnabled(result);
            } else if (result instanceof UnknownHostException) {
                notifyText = "Cannot resolve server hostname/IP";
            } else if (result instanceof SocketTimeoutException) {
                notifyText = "Connection to server timed out";
            } else if (result instanceof IOException) {
                notifyText = "Unexpected end of connection to server - " + result.getMessage();
            } else if (result instanceof AmuleAsyncTaskException) {
                notifyText = result.getMessage();
            } 

            Toast.makeText(mECHelper.getApplication(), notifyText, Toast.LENGTH_LONG).show();
            mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.ERROR);
            
            mECHelper.resetStaleClientData();

        }
    }
    

    @Override
    public String toString() {
        String className = getClass().getName();
        
        return String.format("Class: %s, Status: %s", className.substring(className.lastIndexOf('.') + 1), getStatus());
    }
    
    
    
}