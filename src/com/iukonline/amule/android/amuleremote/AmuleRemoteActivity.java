package com.iukonline.amule.android.amuleremote;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.DlQueueFragment.DlQueueFragmentContainer;
import com.iukonline.amule.android.amuleremote.UpdateChecker.UpdatesWatcher;
import com.iukonline.amule.android.amuleremote.dialogs.AboutDialogFragment;
import com.iukonline.amule.android.amuleremote.dialogs.EditTextDialogFragment;
import com.iukonline.amule.android.amuleremote.dialogs.NewVersionDialogFragment;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ECStatsWatcher;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AddEd2kAsyncTask;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.echelper.tasks.GetDlQueueAsyncTask;
import com.iukonline.amule.android.amuleremote.echelper.tasks.GetECStatsAsyncTask;
import com.iukonline.amule.ec.ECConnState;
import com.iukonline.amule.ec.ECStats;


public class AmuleRemoteActivity extends FragmentActivity implements ClientStatusWatcher, DlQueueFragmentContainer, ECStatsWatcher, RefreshingActivity, UpdatesWatcher  {
    
    
    
    public final static String BUNDLE_PARAM_ERRSTR          = "errstr";
    public final static String BUNDLE_PARAM_URI_TO_HANDLE   = "uri_to_handle";
    
    public final static String NO_URI_TO_HANDLE       = "NO_URI"; 
    
    private AmuleControllerApplication mApp;
    private String mHandleURI;
    
    String mED2KUrl;
    
    private boolean mIsProgressShown = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        mApp = (AmuleControllerApplication) getApplication();
        
        Intent i = getIntent();
        String a = i.getAction();
        
        mHandleURI = a.equals(Intent.ACTION_VIEW) ? i.getData().toString() : NO_URI_TO_HANDLE; 
        
    }
    
    @Override
    protected void onResume() {

        super.onResume();
        
        if (! mApp.refreshServerSettings()) {
            // No server configured
            Intent settingsActivity = new Intent(this, AmuleControllerPreferences.class);
            startActivity(settingsActivity);
            return;
        }
        
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECStats(mApp.mECHelper.registerForECStatsUpdates(this));
        
        mApp.registerRefreshActivity(this);
        mApp.mUpdateChecker.registerUpdatesWatcher(this);
        
        if (! mHandleURI.equals(NO_URI_TO_HANDLE)) {
            showAddED2KDialog(mHandleURI);
        }
        
        if (! mApp.mECHelper.isDlQueueValid()) this.refreshDlQueue();
        
    }
    
    @Override
    protected void onPause() {
        
        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        mApp.registerRefreshActivity(null);
        mApp.mUpdateChecker.registerUpdatesWatcher(null);
        
        super.onPause();
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        MenuItem refreshItem = menu.findItem(R.id.menu_opt_refresh);
        
        if (refreshItem != null) {
            if (mIsProgressShown) {
                //Toast.makeText(this, "Showing progressbar", Toast.LENGTH_LONG).show();
                //refreshItem.setActionView((View) getRefreshProgressBar());
                
                refreshItem.setActionView(R.layout.refresh_progress);
            } else {
                //Toast.makeText(this, "Showing button", Toast.LENGTH_LONG).show();
                refreshItem.setActionView(null);
                
            }

        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_opt_refresh:
            refreshDlQueue();
            return true;
        case R.id.menu_opt_settings:
            Intent settingsActivity = new Intent(this, AmuleControllerPreferences.class);
            startActivity(settingsActivity);
            return true; 
        case R.id.menu_opt_reset:
            mApp.mECHelper.resetClient();
            return true;
        case R.id.menu_opt_added2k:
            showAddED2KDialog(null);
            return true;
        case R.id.menu_opt_about:
            showAboutDialog();
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    
    public void refreshDlQueue()  {
        refreshDlQueue(TaskScheduleMode.BEST_EFFORT);
    }

    public void refreshDlQueue(TaskScheduleMode mode)  {
        if (mApp.mECHelper.executeTask(mApp.mECHelper.getNewTask(GetECStatsAsyncTask.class), mode)) {
            mApp.mECHelper.executeTask(mApp.mECHelper.getNewTask(GetDlQueueAsyncTask.class), TaskScheduleMode.QUEUE);
        }
    }
    
    
    public void showAddED2KDialog(String url) {
        
        Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle b = msg.getData();
                if (b != null) {
                    String u = b.getString(EditTextDialogFragment.BUNDLE_EDIT_STRING);
                    if (u != null) {
                        AddEd2kAsyncTask ed2kTask = (AddEd2kAsyncTask) mApp.mECHelper.getNewTask(AddEd2kAsyncTask.class);
                        ed2kTask.setEd2kUrl(u);
                        
                        if (mApp.mECHelper.executeTask(ed2kTask, TaskScheduleMode.QUEUE)) {
                            refreshDlQueue(TaskScheduleMode.QUEUE);
                        }
                    }
                }
            }
        };
        
        EditTextDialogFragment d = new EditTextDialogFragment(R.string.dialog_added2k_title, url, h.obtainMessage(), null);
        d.show(getSupportFragmentManager(), "rename_dialog");
        
    }
    
    public void showAboutDialog() {
        
        String versionName = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (NameNotFoundException e) {
        }
        
        AboutDialogFragment d = new AboutDialogFragment(versionName, mApp.getReleaseNotes());
        d.show(getSupportFragmentManager(), "about_dialog");
    }
    
    
    
    // DlQueueFragmentContainer
    
    public void partFileSelected(byte[] hash) {
        Intent i = new Intent(this, PartFileActivity.class);
        i.putExtra(PartFileActivity.BUNDLE_PARAM_HASH, hash);
        startActivity(i);
    }

    // ClientStatusWatcher interface
    
    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    @Override
    public void notifyStatusChange(AmuleClientStatus status) {
        //Toast.makeText(mApp, "Notification received - " + status, Toast.LENGTH_LONG).show();
        
        switch (status) {
        case WORKING:
            showProgress(true);
            break;
        default:
            showProgress(false);
        }
    }
    
    public void showProgress(boolean show) {
        if (show == mIsProgressShown) return;
        
        //Toast.makeText(mApp, "Changing status - " + show, Toast.LENGTH_LONG).show();
        mIsProgressShown = show;
        invalidateOptionsMenu();
        
    }

    @Override
    public void updateECStats(ECStats newStats) {
        if (newStats != null) {
            ((TextView) findViewById(R.id.main_dl_rate)).setText(GUIUtils.longToBytesFormatted(newStats.getDlSpeed()) + "/s \u2193");
            ((TextView) findViewById(R.id.main_ul_rate)).setText(GUIUtils.longToBytesFormatted(newStats.getUlSpeed()) + "/s \u2191");
            
            // TODO STRING RESOURCES
            ECConnState c = newStats.getConnState();
            if (c == null) {
                ((TextView) findViewById(R.id.main_edonkey_status)).setText("Not Connected");
                ((TextView) findViewById(R.id.main_kad_status)).setText("Not Connected");
            } else {
                if (c.isKadFirewalled()) {
                    ((TextView) findViewById(R.id.main_kad_status)).setText("Firewalled");
                } else if (c.isKadRunning()) {
                    ((TextView) findViewById(R.id.main_kad_status)).setText("Connected");
                } else {
                    ((TextView) findViewById(R.id.main_kad_status)).setText("Not Connected");
                }
                
                if (c.isConnectedEd2k()) {
                    ((TextView) findViewById(R.id.main_edonkey_status)).setText("Connected");
                } else if (c.isConnectingEd2k()) {
                    ((TextView) findViewById(R.id.main_edonkey_status)).setText("Connecting");
                } else {
                    ((TextView) findViewById(R.id.main_edonkey_status)).setText("Not Connected");
                }
            }
            
            findViewById(R.id.main_conn_bar).setVisibility(View.VISIBLE);
        }
    }
    
    // RefreshingActivity Interface
    
    @Override
    public void refreshContent() {
        refreshDlQueue();
    }

    // UpdatesWatcher Interface
    
    @Override
    public void notifyUpdate(String newReleaseURL, String releaseNotes) {
        
        // TODO: TESTARE
        
        NewVersionDialogFragment d = new NewVersionDialogFragment(newReleaseURL, releaseNotes);
        d.show(getSupportFragmentManager(), "new_release_dialog");
        
    }
}