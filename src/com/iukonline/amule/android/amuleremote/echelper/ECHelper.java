package com.iukonline.amule.android.amuleremote.echelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.AsyncTask;
import android.util.Log;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher.AmuleClientStatus;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.DlQueueWatcher;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ECPartFileActionWatcher;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ECStatsWatcher;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AmuleAsyncTask;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AmuleAsyncTask.TaskScheduleQueueStatus;
import com.iukonline.amule.android.amuleremote.echelper.tasks.ECPartFileActionAsyncTask.ECPartFileAction;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECUtils;



public class ECHelper {
    
    AmuleControllerApplication mApplication;

    // TODO: BONIFICARE
    public static final byte AC_GET_CLIENT_MODE_PREEMPTIVE = 0;
    public static final byte AC_GET_CLIENT_MODE_BESTEFFORT = 1;
    
    private int mClientConnectTimeout;
    private int mClientReadTimeout;
    
    private String mServerHost;
    private int mServerPort;
    private String mServerPassword;

    private Socket mAmuleSocket;
    protected ECClient mECClient;
    private AmuleClientStatus mECClientStatus = ClientStatusWatcher.AmuleClientStatus.NOT_CONNECTED;

    ArrayList<ECPartFile> mDlQueue ;
    long mDlQueueAge = -1;
    
    ECStats mStats;

    // Watchers
    HashMap<String, DlQueueWatcher> mDlQueueWatchers = new HashMap<String, DlQueueWatcher>();
    HashMap<String, ECStatsWatcher> mECStatsWatchers = new HashMap<String, ECStatsWatcher>();
    HashMap<String, ClientStatusWatcher> mAmuleStatusWatchers = new HashMap<String, ClientStatusWatcher>();
    HashMap<String, HashMap<String, ECPartFileWatcher>> mECPartFileWatchers = new HashMap<String, HashMap<String, ECPartFileWatcher>>();
    HashMap<String, HashMap<String, ECPartFileActionWatcher>> mECPartFileActionWatchers = new HashMap<String, HashMap<String, ECPartFileActionWatcher>>();
    
    ConcurrentLinkedQueue <AmuleAsyncTask> mTaskQueue = new ConcurrentLinkedQueue<AmuleAsyncTask>();
    
    public AmuleAsyncTask getNewTask(Class<? extends AmuleAsyncTask> taskType) {
        AmuleAsyncTask newTask;
        try {
            newTask = taskType.newInstance();
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        }
        
        newTask.initialize(this);
        return newTask;
    }

    public boolean executeTask(AmuleAsyncTask task, TaskScheduleMode mode) {
        
        //Toast.makeText(getApplication(), "Executing task " + task.toString() + " mode " + mode.toString(), Toast.LENGTH_LONG).show();

        AmuleAsyncTask nextTask = getNextTask();
        
        if (nextTask != null) {
            switch (mode) {
            case BEST_EFFORT:
                //Toast.makeText(getApplication(), "Cannot run " + task.toString() + "  busy queue", Toast.LENGTH_LONG).show();
                return false;
            case PREEMPTIVE:
                //Toast.makeText(getApplication(), "Flushing queue to run " + task.toString(), Toast.LENGTH_LONG).show();
                if (! emptyTaskQueue()) return false;
                break;
            }           
        }
        //Toast.makeText(getApplication(), "Adding " + task.toString() + "  to queue", Toast.LENGTH_LONG).show();
        mTaskQueue.add(task);
        task.setQueueStatus(TaskScheduleQueueStatus.QUEUED);
        processTaskQueue();
        return true;
    }
    
    private void processTaskQueue() {
        AmuleAsyncTask nextTask = getNextTask();
        if (nextTask == null) {
            //Toast.makeText(getApplication(), "ProcesTaskQueue called on empty queue...", Toast.LENGTH_LONG).show();
        } else {
            //Toast.makeText(getApplication(), "Processing task " + nextTask.toString(), Toast.LENGTH_LONG).show();
            if (nextTask.getStatus() == AsyncTask.Status.PENDING && nextTask.getQueueStatus() != TaskScheduleQueueStatus.LAUNCHED) {
                nextTask.execute();
            }
        }
    }
    
    private AmuleAsyncTask getNextTask() {
        
        AmuleAsyncTask nextTask = mTaskQueue.peek();
        
        while (nextTask != null) {
            if (nextTask.getStatus() != AsyncTask.Status.FINISHED) {
                return nextTask;
            }
            nextTask = mTaskQueue.poll();
        }
        return null;
    }
    
    private boolean emptyTaskQueue() {
        AmuleAsyncTask nextTask = getNextTask();
        while (nextTask != null) {
            //Toast.makeText(getApplication(), "Cancelling task " + nextTask.toString() , Toast.LENGTH_LONG).show();
            if (! nextTask.cancel(true)) return false;
            
            mTaskQueue.poll();
            nextTask = getNextTask();
        }
        //Toast.makeText(getApplication(), "No more task to cancel", Toast.LENGTH_LONG).show();
        return true;
    }
    
    
    
    
    
    public ECHelper(AmuleControllerApplication application) {
        mApplication = application;
    }
    
  
    public AmuleControllerApplication getApplication() {
        return mApplication;
    }
    
    
    public ClientStatusWatcher.AmuleClientStatus registerForAmuleClientStatusUpdates (ClientStatusWatcher watcher) {
        //Toast.makeText(getApplication(), "Registering watcher " + watcher.getWatcherId() + " to status changes", Toast.LENGTH_LONG).show();
        registerWatcher(watcher, mAmuleStatusWatchers);
        return mECClientStatus;
    }
    
    public ArrayList <ECPartFile> registerForDlQueueUpdates (DlQueueWatcher watcher) { 
        registerWatcher(watcher, mDlQueueWatchers);
        return getDlQueue();
    }
    
    public ECStats registerForECStatsUpdates (ECStatsWatcher watcher) {
        registerWatcher(watcher, mECStatsWatchers);
        return this.getStats();
    }
    
    public ECPartFile registerForECPartFileUpdates (ECPartFileWatcher watcher, byte[] hash) {
        String hashString = ECUtils.byteArrayToHexString(hash);
        if (! mECPartFileWatchers.containsKey(hashString)) mECPartFileWatchers.put(hashString, new HashMap <String, ECPartFileWatcher>());
        registerWatcher(watcher, mECPartFileWatchers.get(hashString));
        return getPartFileFromHash(hash);
    }
    
    public void registerForECPartFileActions (ECPartFileActionWatcher watcher, byte[] hash) {
        String hashString = ECUtils.byteArrayToHexString(hash);
        if (! mECPartFileActionWatchers.containsKey(hashString)) mECPartFileActionWatchers.put(hashString, new HashMap <String, ECPartFileActionWatcher>());
        registerWatcher(watcher, mECPartFileActionWatchers.get(hashString));
    }
    
    @SuppressWarnings("unchecked")
    private void registerWatcher(AmuleWatcher watcher, @SuppressWarnings("rawtypes") HashMap to) {
        if (to.containsKey(watcher.getWatcherId())) {
            if (to.get(watcher.getWatcherId()) != watcher) {
                unRegisterWatcher((AmuleWatcher) to.get(watcher.getWatcherId()), to);
                to.put(watcher.getWatcherId(), watcher);
            } 
        } else {
            to.put(watcher.getWatcherId(), watcher);
        }
    }
    
    public void unRegisterFromAmuleClientStatusUpdates (ClientStatusWatcher watcher) { unRegisterWatcher(watcher, mAmuleStatusWatchers); }
    
    public void unRegisterFromDlQueueUpdates (DlQueueWatcher watcher) { unRegisterWatcher(watcher, mDlQueueWatchers); }
    
    public void unRegisterFromECStatsUpdates (ECStatsWatcher watcher) { unRegisterWatcher(watcher, mECStatsWatchers); }
    
    public void unRegisterFromECPartFileUpdates (ECPartFileWatcher watcher, byte[] hash) {
        String hashString = ECUtils.byteArrayToHexString(hash);
        if (mECPartFileWatchers.containsKey(hashString)) {
            unRegisterWatcher(watcher, mECPartFileWatchers.get(hashString));
        }
    }
    
    public void unRegisterFromECPartFileActions (ECPartFileActionWatcher watcher, byte[] hash) {
        String hashString = ECUtils.byteArrayToHexString(hash);
        if (mECPartFileActionWatchers.containsKey(hashString)) {
            unRegisterWatcher(watcher, mECPartFileActionWatchers.get(hashString));
        }
    }
    
    private void unRegisterWatcher(AmuleWatcher watcher, @SuppressWarnings("rawtypes") HashMap from) {
        if (from.containsKey(watcher.getWatcherId()) && from.get(watcher.getWatcherId()) == watcher) {
            from.remove(watcher.getWatcherId());
        }
    }

    public void notifyAmuleClientStatusWatchers (AmuleClientStatus status) {
        //Toast.makeText(getApplication(), "Client status changed to " + status.toString(), Toast.LENGTH_LONG).show();
        
        mECClientStatus = status;
        if (status == AmuleClientStatus.ERROR) {
            resetClient();
        }
        
        if (status != AmuleClientStatus.WORKING) {
            mTaskQueue.poll();
            AmuleAsyncTask nextTask = this.getNextTask();
            
            if (nextTask != null) {
                switch (status) {
                case ERROR:
                case NOT_CONNECTED:
                    emptyTaskQueue();
                case IDLE:
                    processTaskQueue();
                    return; // Do not update status, since more task are running...
                }
            }
        }
        
        //Toast.makeText(getApplication(), "Notifying status wachers", Toast.LENGTH_LONG).show();
        Iterator <ClientStatusWatcher> i = mAmuleStatusWatchers.values().iterator();
        while (i.hasNext()) {
            ClientStatusWatcher n = i.next();
            //Toast.makeText(getApplication(), "Notifying " + n.getWatcherId(), Toast.LENGTH_LONG).show();
            n.notifyStatusChange(status);
        }
    }

    public void notifyDlQueueWatchers (ArrayList<ECPartFile> dlQueue) {
        setDlQueue(dlQueue);
        Iterator <DlQueueWatcher> i = mDlQueueWatchers.values().iterator();
        while (i.hasNext()) i.next().updateDlQueue(dlQueue);
    }
    
    public void notifyECStatsWatchers (ECStats stats) {
        
        setStats(stats);
        Iterator <ECStatsWatcher> i = mECStatsWatchers.values().iterator();
        while (i.hasNext()) {
            i.next().updateECStats(stats);
        }
    }
    
    public void notifyECPartFileWatchers (ECPartFile file) {
        String hashString = ECUtils.byteArrayToHexString(file.getHash());
        if (mECPartFileWatchers.containsKey(hashString)) {
            Iterator <ECPartFileWatcher> i = mECPartFileWatchers.get(hashString).values().iterator();
            while (i.hasNext()) i.next().updateECPartFile(file);
        }
    }
    
    public void notifyECPartFileActionWatchers (ECPartFile file, ECPartFileAction action) {
        String hashString = ECUtils.byteArrayToHexString(file.getHash());
        if (mECPartFileActionWatchers.containsKey(hashString)) {
            Iterator <ECPartFileActionWatcher> i = mECPartFileActionWatchers.get(hashString).values().iterator();
            while (i.hasNext()) i.next().notifyECPartFileActionDone(action);
        }
    }

    public ArrayList<ECPartFile> getDlQueue() {
        return mDlQueue;
    }

    public void setDlQueue(ArrayList<ECPartFile> mdlList) {
        mDlQueue = mdlList;
        mDlQueueAge = System.currentTimeMillis();
    }
    
    public void invalidateDlQueue() {
        mDlQueueAge = -1;
    }
    
    public boolean isDlQueueValid() {
        return mDlQueueAge > 0 ? true : false;
    }

    /*
    public void cancelLatestTask() {
        if (mLatestTask != null) {
                mLatestTask.cancel(true);
                resetClient();
        }
    }
    */

    
    
    
    
    
    public Socket getAmuleSocket() throws UnknownHostException, IOException {
        if (mAmuleSocket == null && mServerHost != null) {
            Log.d("ECHELPER", "Creating new socket");
            mAmuleSocket = new Socket();
        }
        return mAmuleSocket;
    }

    public ECClient getECClient() throws UnknownHostException, IOException {
        Socket s = getAmuleSocket();
        if (s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
            Log.d("ECHELPER", "Invalid socket! Resetting");
            resetSocket();
            s = getAmuleSocket();
        }
        if (!s.isConnected()) {
            Log.d("ECHELPER", "Connecting socket");
            s.connect(new InetSocketAddress(InetAddress.getByName(mServerHost), mServerPort), mClientConnectTimeout);
            s.setSoTimeout(mClientReadTimeout);
        }
        if (mECClient == null) {
            Log.d("ECHELPER", "Creating new client");
            ECClient c = new ECClient();
            c.setClientName("Amule Remote Controller");
            c.setClientVersion("0.1aplha");
            try {
                c.setPassword(mServerPassword);
            } catch (NoSuchAlgorithmException e) {
            }
            c.setSocket(s);
            mECClient = c;
        }
        return mECClient;
    }

    public String getServerHost() {
        return mServerHost;
    }


    public void resetClient() {
        Log.d("ECHELPER", "Setting client to null");
        mECClient = null;
        //Toast.makeText(getApplication(), "Resetting client", Toast.LENGTH_LONG).show();
        Log.d("ECHELPER", "Resetting socket");
        resetSocket();
        //notifyAmuleClientStatusWatchers(ClientStatusWatcher.AMULE_CLIENT_STATUS_NOT_CONNECTED);
    }

    public void resetSocket() {
        if (mAmuleSocket != null) {
            try {
                Log.d("ECHELPER", "Closing socket");
                mAmuleSocket.shutdownInput();
                mAmuleSocket.shutdownOutput();
                mAmuleSocket.close();
            } catch (IOException e) {
                // Do Nothing. We're closing. Right?
            }
            Log.d("ECHELPER", "Setting socket to null");
            mAmuleSocket = null;
        }
    }

    public void setServerInfo(String host, int port, String password, int connTimeout, int readTimeout)  {
        
        if (!host.equalsIgnoreCase(mServerHost) || port != mServerPort || connTimeout != mClientConnectTimeout || readTimeout != mClientReadTimeout) {
            // Server or host changed (don't care about password)
            mServerHost = host;
            mServerPort = port;
            mClientConnectTimeout = connTimeout;
            mClientReadTimeout = readTimeout;
            resetClient();
            
        } 
        if (!password.equals(mServerPassword)) {
            mServerPassword = new String(password);
            resetClient();
        }
    }

    public ECStats getStats() {
        return mStats;
    }

    public void setStats(ECStats mStats) {
        this.mStats = mStats;
    }

    public ECPartFile getPartFileFromHash(byte[] hash) {
        Iterator<ECPartFile> itr = mDlQueue.iterator();
        while (itr.hasNext()) {
            ECPartFile p = itr.next();
            byte[] h = p.getHash();
            boolean matches = true;
            for (int i = 0; i < hash.length && matches; i++) {
                if (hash[i] != h[i]) matches = false; 
            }
            if (matches) return p;
        }
        return null;
    }

}
