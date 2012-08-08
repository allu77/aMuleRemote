package com.iukonline.amule.android.amuleremote.echelper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.acra.ErrorReporter;

import android.os.AsyncTask;
import android.os.DropBoxManager;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.CategoriesWatcher;
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
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECUtils;
import com.iukonline.amule.ec.fake.ECClientFake;
import com.iukonline.amule.ec.v203.ECClientV203;
import com.iukonline.amule.ec.v204.ECClientV204;



public class ECHelper {
    
    public AmuleControllerApplication mApplication;

    private boolean isClientStale = false;
    
    private int mClientConnectTimeout;
    private int mClientReadTimeout;
    
    private String mServerHost;
    private int mServerPort;
    private String mServerVersion;
    private String mServerPassword;

    private Socket mAmuleSocket;
    protected ECClient mECClient;
    
    public DropBoxManager mDropBox;
    
    private AmuleClientStatus mECClientStatus = ClientStatusWatcher.AmuleClientStatus.NOT_CONNECTED;

    // When adding new cached data, remeber to add the to resetStaleClientData() for stateful clients
    
    
    HashMap<String, ECPartFile> mDlQueue ;
    long mDlQueueAge = -1;
    
    ECStats mStats;
    ECCategory[] mCategories;

    // Watchers
    HashMap<String, DlQueueWatcher> mDlQueueWatchers = new HashMap<String, DlQueueWatcher>();
    HashMap<String, ECStatsWatcher> mECStatsWatchers = new HashMap<String, ECStatsWatcher>();
    HashMap<String, CategoriesWatcher> mCategoriesWatchers = new HashMap<String, CategoriesWatcher>();
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
    
    public HashMap<String, ECPartFile> registerForDlQueueUpdates (DlQueueWatcher watcher) { 
        registerWatcher(watcher, mDlQueueWatchers);
        return getDlQueue();
    }
    
    public ECStats registerForECStatsUpdates (ECStatsWatcher watcher) {
        registerWatcher(watcher, mECStatsWatchers);
        return this.getStats();
    }
    
    public ECCategory[] registerForCategoriesUpdates (CategoriesWatcher watcher) {
        registerWatcher(watcher, mCategoriesWatchers);
        return this.getCategories();
    }
    
    public ECPartFile registerForECPartFileUpdates (ECPartFileWatcher watcher, byte[] hash) {
        String hashString = ECUtils.byteArrayToHexString(hash);
        if (! mECPartFileWatchers.containsKey(hashString)) mECPartFileWatchers.put(hashString, new HashMap <String, ECPartFileWatcher>());
        registerWatcher(watcher, mECPartFileWatchers.get(hashString));
        return mDlQueue == null ? null : mDlQueue.get(hashString);
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
    
    public void unRegisterFromCategoriesUpdates (CategoriesWatcher watcher) { unRegisterWatcher(watcher, mCategoriesWatchers); }
    
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
            // Moved in resetClient
            // if (mECClient != null && mECClient.isStateful()) {
            //    setClientStale();
            // }
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

    public void notifyDlQueueWatchers (HashMap<String, ECPartFile> dlQueue) {
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
    
    public void notifyCategoriesWatchers(ECCategory[] categories) {
        mCategories = categories;
        Iterator <CategoriesWatcher> i = mCategoriesWatchers.values().iterator();
        while (i.hasNext()) {
            i.next().updateCategories(categories);
        }
    }
    
    public void notifyECPartFileWatchers (ECPartFile file) {
        if (file == null) {
            for (HashMap <String, ECPartFileWatcher> m : mECPartFileWatchers.values()) {
                for (ECPartFileWatcher w : m.values()) {
                    w.updateECPartFile(null);
                }
            }
        } else {
            String hashString = ECUtils.byteArrayToHexString(file.getHash());
            if (mECPartFileWatchers.containsKey(hashString)) {
                for (ECPartFileWatcher w : mECPartFileWatchers.get(hashString).values()) {
                    w.updateECPartFile(file);
                }
            }
        }
    }
    
    public void notifyECPartFileActionWatchers (ECPartFile file, ECPartFileAction action) {
        String hashString = ECUtils.byteArrayToHexString(file.getHash());
        if (mECPartFileActionWatchers.containsKey(hashString)) {
            Iterator <ECPartFileActionWatcher> i = mECPartFileActionWatchers.get(hashString).values().iterator();
            while (i.hasNext()) i.next().notifyECPartFileActionDone(action);
        }
    }

    public HashMap<String, ECPartFile> getDlQueue() {
        return mDlQueue;
    }

    public void setDlQueue(HashMap<String, ECPartFile> dlQueue) {
        mDlQueue = dlQueue;
        mDlQueueAge = System.currentTimeMillis();
    }
    
    public void invalidateDlQueue() {
        mDlQueueAge = -1;
    }
    
    public boolean isDlQueueValid() {
        return mDlQueueAge > 0 ? true : false;
    }

    
    
    public Socket getAmuleSocket() throws UnknownHostException, IOException {
        if (mAmuleSocket == null && mServerHost != null) {
            //Log.d("ECHELPER", "Creating new socket");
            mAmuleSocket = new Socket();
        }
        return mAmuleSocket;
    }

    public ECClient getECClient() throws UnknownHostException, IOException {
        Socket s = null;
        if (! mServerVersion.equals("Fake")) {
            s = getAmuleSocket();
            if (s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
                //Log.d("ECHELPER", "Invalid socket! Resetting");
                resetSocket();
                s = getAmuleSocket();
            }
            if (!s.isConnected()) {
                //Log.d("ECHELPER", "Connecting socket");
                s.connect(new InetSocketAddress(InetAddress.getByName(mServerHost), mServerPort), mClientConnectTimeout);
                s.setSoTimeout(mClientReadTimeout);
            }
        }
        if (mECClient == null) {
            //Log.d("ECHELPER", "Creating new client");
            ECClient c;
            if (mServerVersion.equals("V204")) {
                c = new ECClientV204();
            } else  if (mServerVersion.equals("V203")) {
                c = new ECClientV203();
            } else if (mServerVersion.equals("Fake")) {
                c = new ECClientFake();
            } else {
                c = new ECClient();
            }
            c.setClientName("Amule Remote Controller");
            c.setClientVersion("0.4");
            try {
                c.setPassword(mServerPassword);
            } catch (NoSuchAlgorithmException e) {
            }
            c.setSocket(s);
            mECClient = c;
        }
        
        if (mECClient != null) {
            ErrorReporter.getInstance().putCustomData("ServerVersion", mECClient.getServerVersion());
            isClientStale = false;
        }
        return mECClient;
    }

    public String getServerHost() {
        return mServerHost;
    }


    public void resetClient() {
        if (mECClient != null && mECClient.isStateful()) {
            setClientStale();
        }
        mECClient = null;
        resetSocket();
    }
    
    public void setClientStale() {
        isClientStale = true;
    }
    
    public void resetStaleClientData() {
        if (isClientStale) {
            mDlQueue = null;
            mDlQueueAge = -1;
            mStats = null;
            mCategories = null;
            
            // This should reset stateful data
            notifyDlQueueWatchers(null);
            notifyECStatsWatchers(null);
            notifyCategoriesWatchers(null);
            notifyECPartFileWatchers(null);
            
            isClientStale = false;
        }
    }

    public void resetSocket() {
        if (mAmuleSocket != null) {
            try {
                //mAmuleSocket.shutdownInput();
                //mAmuleSocket.shutdownOutput();
                mAmuleSocket.close();
            } catch (IOException e) {
                // Do Nothing. We're closing. Right?
            }
            mAmuleSocket = null;
        }
    }

    public void setServerInfo(String host, int port, String version, String password, int connTimeout, int readTimeout)  {
        
        if (!host.equalsIgnoreCase(mServerHost) || port != mServerPort || !version.equalsIgnoreCase(mServerVersion) || connTimeout != mClientConnectTimeout || readTimeout != mClientReadTimeout) {
            // Server or host changed (don't care about password)
            mServerHost = host;
            mServerPort = port;
            mServerVersion = version;
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
    
    public ECCategory[] getCategories() {
        return mCategories;
    }
    
    public void sendParsingExceptionIfEnabled(Exception e) {
        if (mApplication.sendExceptions) ErrorReporter.getInstance().handleException(e);
    }

}
