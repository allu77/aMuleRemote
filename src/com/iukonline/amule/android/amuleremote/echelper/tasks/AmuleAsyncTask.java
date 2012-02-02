package com.iukonline.amule.android.amuleremote.echelper.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.echelper.ECHelper;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECException;

public abstract class AmuleAsyncTask extends AsyncTask<Void, Void, String> {

    public enum TaskScheduleMode { BEST_EFFORT, PREEMPTIVE, QUEUE } 
    public enum TaskScheduleQueueStatus { QUEUED, LAUNCHED }

    protected ECHelper mECHelper;
    protected TaskScheduleMode mMode;
    protected ECClient mECClient;
    protected TaskScheduleQueueStatus mQueueStatus;
    
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
    
    abstract protected String backgroundTask() throws ECException, UnknownHostException, SocketTimeoutException, IOException;
    
    protected String doInBackground(Void... params) {
        try {
            mECClient = mECHelper.getECClient();
            return backgroundTask();
        
        } catch (ECException e) {
            return isCancelled() ? null : new String("EC error - " + e.getLocalizedMessage());
        } catch (UnknownHostException e) {
            return isCancelled() ? null : new String("HOST error - " + e.getLocalizedMessage());
        } catch (SocketTimeoutException e) {
            return isCancelled() ? null : new String("Socket timeout");
        } catch (IOException e) {

            
            if (isCancelled()) return null;
            
            //mECHelper.releaseECClient();
            mECHelper.resetClient();
            
            try {
                mECClient = mECHelper.getECClient();
                return backgroundTask();
                
            } catch (ECException e2) {
                return isCancelled() ? null : new String("EC error - " + e2.getLocalizedMessage());
            } catch (UnknownHostException e2) {
                return isCancelled() ? null : new String("HOST error - " + e2.getLocalizedMessage());
            } catch (SocketTimeoutException e2) {
                return isCancelled() ? null : new String("Socket timeout");
            } catch (IOException e2) {
                return isCancelled() ? null : new String("IO error - " + e2.getLocalizedMessage());
            }
        }
    }
    
    abstract protected void notifyResult();
    
    @Override
    protected void onCancelled() {
        //mECHelper.releaseECClient(this, true);
        mECHelper.resetClient();
        mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.IDLE);
    }
    
    @Override
    protected void onPostExecute(String result) {
        if (result == null) {
            //mECHelper.releaseECClient(this, false);
            mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.IDLE);
            notifyResult();
        } else {
            Toast.makeText(mECHelper.getApplication(), result, Toast.LENGTH_LONG).show();
            //mECHelper.releaseECClient(this, true);
            //mECHelper.resetClient();
            mECHelper.notifyAmuleClientStatusWatchers(ClientStatusWatcher.AmuleClientStatus.ERROR);
        }
    }


    @Override
    public String toString() {
        String className = getClass().getName();
        
        return String.format("Class: %s, Status: %s", className.substring(className.lastIndexOf('.') + 1), getStatus());
    }
    
    
    
}