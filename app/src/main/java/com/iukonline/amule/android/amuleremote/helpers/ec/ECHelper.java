/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.ec;

import android.os.AsyncTask;
import android.os.DropBoxManager;
import android.util.Log;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.CategoriesWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher.AmuleClientStatus;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.DlQueueWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileActionWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECSearchListWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECStatsWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask.TaskScheduleQueueStatus;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileActionAsyncTask.ECPartFileAction;
import com.iukonline.amule.android.amuleremote.search.SearchContainer;
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECClient;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECUtils;
import com.iukonline.amule.ec.fake.ECClientFake;
import com.iukonline.amule.ec.v203.ECClientV203;
import com.iukonline.amule.ec.v204.ECClientV204;

import org.acra.ACRA;

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



public class ECHelper {
    
    private final static long DATA_MAX_AGE_MILLIS = 120000L;
    private final static long IDLE_CLIENT_MAX_AGE_MILLIS = 60000L;
    
    public AmuleControllerApplication mApp;

    // TBV: Possiamo farne a meno? 
    // private boolean isClientStale = false;
    private boolean mNeedsGUICleanUp = false;
    
    private int mClientConnectTimeout;
    private int mClientReadTimeout;
    
    private String mServerHost;
    private int mServerPort;
    private String mServerVersion;
    private String mServerPassword;

    private Socket mAmuleSocket;
    protected ECClient mECClient;
    private long mECClientLastIdle = -1;
    
    public DropBoxManager mDropBox;
    
    private AmuleClientStatus mECClientStatus = ClientStatusWatcher.AmuleClientStatus.NOT_CONNECTED;

    // When adding new cached data, remeber to add the to resetStaleClientData() for stateful clients
    
    HashMap<String, ECPartFile> mDlQueue ;
    long mDlQueueLasMod = -1;
    
    ECStats mStats;
    ECCategory[] mCategories;
    
    ArrayList <SearchContainer> mSearches = new ArrayList <SearchContainer>(); 

    // Watchers
    HashMap<String, DlQueueWatcher> mDlQueueWatchers = new HashMap<String, DlQueueWatcher>();
    HashMap<String, ECStatsWatcher> mECStatsWatchers = new HashMap<String, ECStatsWatcher>();
    HashMap<String, CategoriesWatcher> mCategoriesWatchers = new HashMap<String, CategoriesWatcher>();
    HashMap<String, ClientStatusWatcher> mAmuleStatusWatchers = new HashMap<String, ClientStatusWatcher>();
    HashMap<String, HashMap<String, ECPartFileWatcher>> mECPartFileWatchers = new HashMap<String, HashMap<String, ECPartFileWatcher>>();
    HashMap<String, HashMap<String, ECPartFileActionWatcher>> mECPartFileActionWatchers = new HashMap<String, HashMap<String, ECPartFileActionWatcher>>();
    HashMap<String, ECSearchListWatcher> mECSearchListWatchers = new HashMap<String, ECSearchListWatcher>();
    
    ConcurrentLinkedQueue <AmuleAsyncTask> mTaskQueue = new ConcurrentLinkedQueue<AmuleAsyncTask>();
    
    public AmuleClientStatus getECClientStatus() {
        return mECClientStatus;
    }
    
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
            default:
                // DO NOTHING
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
        mApp = application;
    }
    
  
    public AmuleControllerApplication getApplication() {
        return mApp;
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
    
    public ArrayList <SearchContainer> registerForECSsearchList (ECSearchListWatcher watcher) {
        registerWatcher(watcher, mECSearchListWatchers);
        return mSearches;
    }
    
    public ECCategory[] registerForCategoriesUpdates (CategoriesWatcher watcher) {
        registerWatcher(watcher, mCategoriesWatchers);
        return this.getCategories();
    }
    
    public ECPartFile registerForECPartFileUpdates (ECPartFileWatcher watcher, byte[] hash) {
        if (hash == null) return null;
        String hashString = ECUtils.byteArrayToHexString(hash);
        if (! mECPartFileWatchers.containsKey(hashString)) mECPartFileWatchers.put(hashString, new HashMap <String, ECPartFileWatcher>());
        registerWatcher(watcher, mECPartFileWatchers.get(hashString));
        return mDlQueue == null ? null : mDlQueue.get(hashString);
    }
    
    public void registerForECPartFileActions (ECPartFileActionWatcher watcher, byte[] hash) {
        if (hash != null) {
            String hashString = ECUtils.byteArrayToHexString(hash);
            if (! mECPartFileActionWatchers.containsKey(hashString)) mECPartFileActionWatchers.put(hashString, new HashMap <String, ECPartFileActionWatcher>());
            registerWatcher(watcher, mECPartFileActionWatchers.get(hashString));
        }
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
    
    public void unRegisterFromECSearchList (ECSearchListWatcher watcher) { unRegisterWatcher(watcher, mECSearchListWatchers); }
    
    public void unRegisterFromECPartFileUpdates (ECPartFileWatcher watcher, byte[] hash) {
        if (hash != null) {
            String hashString = ECUtils.byteArrayToHexString(hash);
            if (mECPartFileWatchers.containsKey(hashString)) {
                unRegisterWatcher(watcher, mECPartFileWatchers.get(hashString));
            }
        }
    }
    
    public void unRegisterFromECPartFileActions (ECPartFileActionWatcher watcher, byte[] hash) {
        if (hash != null) {
            String hashString = ECUtils.byteArrayToHexString(hash);
            if (mECPartFileActionWatchers.containsKey(hashString)) {
                unRegisterWatcher(watcher, mECPartFileActionWatchers.get(hashString));
            }
        }
    }
    

    
    private void unRegisterWatcher(AmuleWatcher watcher, @SuppressWarnings("rawtypes") HashMap from) {
        if (from.containsKey(watcher.getWatcherId()) && from.get(watcher.getWatcherId()) == watcher) {
            from.remove(watcher.getWatcherId());
        }
    }

    
    
    public void notifyAmuleClientStatusWatchers (AmuleClientStatus status) {
        //Toast.makeText(getApplication(), "Client status changed to " + status.toString(), Toast.LENGTH_LONG).show();
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.notifyAmuleClientStatusWatchers: client status is " + status);
        mECClientStatus = status;
        if (status == AmuleClientStatus.ERROR) {
            // Moved in resetClient
            // if (mECClient != null && mECClient.isStateful()) {
            //    setClientStale();
            // }
            resetClient();
        }
        
        if (status != AmuleClientStatus.WORKING) {
            mECClientLastIdle = System.currentTimeMillis();
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.notifyAmuleClientStatusWatchers: mECClientLastIdle set to " + Long.toString(mECClientLastIdle));
            mTaskQueue.poll();
            AmuleAsyncTask nextTask = this.getNextTask();
            
            if (nextTask != null) {
                switch (status) {
                case ERROR:
                case NOT_CONNECTED:
                    emptyTaskQueue();
                    return;
                case IDLE:
                    processTaskQueue();
                    return; // Do not update status, since more task are running...
                default:
                    // DO NOTHING
                    break;
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
    



    public void notifyECSearchListWatcher (ArrayList<SearchContainer> s) {
        // Avoid concurrent modification
        ArrayList <ECSearchListWatcher> toBeDeleted = new ArrayList<ECSearchListWatcher>();
        for (ECSearchListWatcher w : mECSearchListWatchers.values()) {
            if (w.updateECSearchList(s) == ECSearchListWatcher.UpdateResult.UNREGISTER) toBeDeleted.add(w);
        }
        for (ECSearchListWatcher w : toBeDeleted) {
            unRegisterFromECSearchList(w);
        }

    }
    
    public void notifyECSearchListWatcher() {
        notifyECSearchListWatcher(mSearches);
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
        mDlQueueLasMod = System.currentTimeMillis();
    }
    
    public void invalidateDlQueue() {
        mDlQueueLasMod = -1;
    }
    
    public boolean isDlQueueValid() {
        return mDlQueueLasMod < 0 || (System.currentTimeMillis() - mDlQueueLasMod > DATA_MAX_AGE_MILLIS) ? false : true;
    }

    public Socket getAmuleSocket() throws UnknownHostException, IOException {
        if (mAmuleSocket == null && mServerHost != null) {
            //Log.d("ECHELPER", "Creating new socket");
            mAmuleSocket = new Socket();
        }
        return mAmuleSocket;
    }
    
    public ECClient getECClient() throws UnknownHostException, IOException, IllegalArgumentException {
        
        // This should prevent the null Exception on mServerVersion. However it's not clear why that happened.
        // Need to check if client is null when calling getECClient
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.getECClient: Validating server info");
        if (! this.validateServerInfo()) return null;

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.getECClient: Client last idle at " + Long.toString(mECClientLastIdle));
        
        if (mECClient != null && System.currentTimeMillis() - mECClientLastIdle > IDLE_CLIENT_MAX_AGE_MILLIS) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.getECClient: Current client is too old. Resetting.");
            resetClient();
        }
        
        Socket s = null;
        if (! mServerVersion.equals("Fake")) {
            s = getAmuleSocket();
            if (s.isClosed() || s.isInputShutdown() || s.isOutputShutdown()) {
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.getECClient: Current socket is not valid. Resetting.");
                resetSocket();
                s = getAmuleSocket();
            }
            if (!s.isConnected()) {
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.getECClient: Current socket is not connected. Connecting.");
                s.connect(new InetSocketAddress(InetAddress.getByName(mServerHost), mServerPort), mClientConnectTimeout);
                s.setSoTimeout(mClientReadTimeout);
            }
        }
        
        if (mECClient == null) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "ECHelper.getECClient: Creating a " + mServerVersion + " client");
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
            c.setClientName("Amule Remote for Android");
            c.setClientVersion(mApp.mVersionName);
            try {
                c.setPassword(mServerPassword);
            } catch (NoSuchAlgorithmException e) {
            }
            c.setSocket(s);
            mECClient = c;
            mECClientLastIdle = System.currentTimeMillis();
        }
        
        if (mECClient != null) {
            ACRA.getErrorReporter().putCustomData("ServerVersion", mECClient.getServerVersion());
            // TBV: Can be removed? setClientStale(false);
        }
        return mECClient;
    }

    public String getServerHost() {
        return mServerHost;
    }
    
    public String getServerVersion() {
        return mServerVersion;
    }

    public void resetClient() {
        if (mECClient != null && mECClient.isStateful()) {
            setClientStale();
        }
        mECClient = null;
        resetSocket();
    }
    
    public void setClientStale() {
        setClientStale(true);
    }
    
    public void setClientStale(boolean s) {
        // TBV: Can be removed isClientStale = s;
        
        if (s) {
            mDlQueue = null;
            mDlQueueLasMod = -1;
            mStats = null;
            mCategories = null;
            mSearches = new ArrayList<SearchContainer>();
            //mSearches = null;
            
            mNeedsGUICleanUp = true;
        }
    }
    
    public void checkStaleDataOnGUI () {
        
        // WARNING: Call this only on main thread
        
        if (mNeedsGUICleanUp) {
            notifyDlQueueWatchers(null);
            notifyECStatsWatchers(null);
            notifyCategoriesWatchers(null);
            notifyECPartFileWatchers(null);
            notifyECSearchListWatcher(null);
            
            mNeedsGUICleanUp = false;
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

    public boolean validateServerInfo()  {
        return (mServerHost != null && mServerHost.length() > 0 && mServerPort > 0 && mServerVersion != null && mServerPassword != null && mClientConnectTimeout > 0 && mClientReadTimeout > 0) ? true : false;
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
    
    public void addSearchToList(SearchContainer s) {
        mSearches.add(0, s);
    }
    
    public SearchContainer getSearchItem(int position) {
        if (mSearches != null && mSearches.size() > position) {
            return mSearches.get(position);
        }
        return null;
    }
    
    public void sendParsingExceptionIfEnabled(Exception e) {
        if (mApp.sendExceptions) ACRA.getErrorReporter().handleException(e);
    }
}
