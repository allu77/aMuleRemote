package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.ECHelper;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
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
            if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Requesting ECClient");
            mECClient = mECHelper.getECClient();
            if (mECClient == null) return new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.error_null_client));
        } catch (UnknownHostException e1) {
            return e1;
        } catch (IOException e1) {
            return e1;
        }
        
        try {
            if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Launching backgroundTask: " + getClass().getName());
            backgroundTask();
            if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "backgroundTask finished");
        } catch (IOException e) {
            
            if (mECClient.isStateful()) {
                return e; // If client is stateful, we need to refresh all data
            }
            if (isCancelled()) return null;
            
            if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Resetting ECClient");
            mECHelper.resetClient();
            
            try {
                if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Requesting ECClient");
                mECClient = mECHelper.getECClient();
                if (mECClient == null) return new AmuleAsyncTaskException(mECHelper.mApp.getResources().getString(R.string.error_null_client));
                if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "Launching backgroundTask: " + getClass().getName());
                backgroundTask();
                if (mECHelper.mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "backgroundTask finished");
                
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
                if (mECHelper.mApp.enableLog) {
                    Log.e(AmuleControllerApplication.AC_LOGTAG, notifyText);
                    ECPacket req = ((ECClientException)result).getRequestPacket();
                    ECPacket resp = ((ECClientException)result).getResponsePacket();
                    if (req != null && mECHelper.mDropBox != null) {
                        ECRawPacket rr = req.getRawPacket();
                        if (rr != null) {
                            mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, "Request:\n" + rr.dump());
                        }
                    }
                    if (resp != null && mECHelper.mDropBox != null) {
                        ECRawPacket rr = resp.getRawPacket();
                        if (rr != null) {
                            mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, "Response:\n" + rr.dump());
                        }
                    }
                }
                mECHelper.sendParsingExceptionIfEnabled(result);
            } else if (result instanceof ECPacketParsingException) {
                notifyText = r.getText(R.string.error_packet_parsing) + " - " + result.getMessage();
                if (mECHelper.mApp.enableLog) {
                    Log.e(AmuleControllerApplication.AC_LOGTAG, notifyText);
                    ECRawPacket p = ((ECPacketParsingException)result).getCausePacket();
                    if (p != null && mECHelper.mDropBox != null) {
                        mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, "Packet");
                        
                        String[] lines = p.dump().split("\\n");
                        for (int i = 0; i < lines.length; i++) mECHelper.mDropBox.addText(AmuleControllerApplication.AC_LOGTAG, lines[i]);
                        
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
                if (mECHelper.mApp.enableLog) Log.e(AmuleControllerApplication.AC_LOGTAG, "Got an unexpected exception " + result.getClass().getName() + " for background task " + getClass().getName());
            }

            Toast.makeText(mECHelper.getApplication(), notifyText, Toast.LENGTH_LONG).show();
            mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.ERROR);


        }
    }
    

    @Override
    public String toString() {
        String className = getClass().getName();
        
        return String.format("Class: %s, Status: %s", className.substring(className.lastIndexOf('.') + 1), getStatus());
    }
    
    
    
}