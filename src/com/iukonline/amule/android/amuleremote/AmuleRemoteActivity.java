package com.iukonline.amule.android.amuleremote;

import java.util.ArrayList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.Window;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.iukonline.amule.android.amuleremote.DlQueueFragment.DlQueueFragmentContainer;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.echelper.tasks.GetDlQueueAsyncTask;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;




public class AmuleRemoteActivity extends FragmentActivity implements ClientStatusWatcher, DlQueueFragmentContainer {
    
    
    private final static int ID_DIALOG_ADD_ED2K            = 3;

    public final static String BUNDLE_PARAM_ERRSTR          = "errstr";
    public final static String BUNDLE_PARAM_URI_TO_HANDLE   = "uri_to_handle";
    
    public final static String NO_URI_TO_HANDLE       = "NO_URI"; 
    
    private AmuleControllerApplication mApp;
    private String mHandleURI;
    
    String mED2KUrl;
    
    ArrayList <ECPartFile> mDlQueue;
    
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
        if (mDlQueue == null) refreshDlQueue();
        

        
        if (! mHandleURI.equals(NO_URI_TO_HANDLE)) {
            Bundle b = new Bundle();
            b.putString(BUNDLE_PARAM_URI_TO_HANDLE, mHandleURI);
            mHandleURI = NO_URI_TO_HANDLE;
            showDialog(ID_DIALOG_ADD_ED2K, b);
        }
    }
    
    @Override
    protected void onPause() {
        
        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        
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
                refreshItem.setActionView(getRefreshProgressBar());
            } else {
                refreshItem.setActionView(null);
            }

        }

        return super.onPrepareOptionsMenu(menu);
    }
    
    private ProgressBar getRefreshProgressBar() {
        ProgressBar refreshProgressBar = new ProgressBar(this);
        refreshProgressBar.setIndeterminate(true);
        return refreshProgressBar;
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
            showDialog(ID_DIALOG_ADD_ED2K);
            return true;
        case R.id.menu_opt_sort_filename:
            SharedPreferences.Editor e1 = mApp.mSettings.edit();
            e1.putLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_FILENAME);
            e1.commit();
            //refreshView();
            return true;
        case R.id.menu_opt_sort_status:
            SharedPreferences.Editor e2 = mApp.mSettings.edit();
            e2.putLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_STATUS);
            e2.commit();
            //refreshView();
            return true;
        case R.id.menu_opt_sort_transfered:
            SharedPreferences.Editor e3 = mApp.mSettings.edit();
            e3.putLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_TRANSFERED);
            e3.commit();
            //refreshView();
            return true;
        case R.id.menu_opt_sort_progress:
            SharedPreferences.Editor e4 = mApp.mSettings.edit();
            e4.putLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_PROGRESS);
            e4.commit();
            //refreshView();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    
    
    
    
    public void refreshDlQueue()  {
        mApp.mECHelper.executeTask(
                (GetDlQueueAsyncTask) mApp.mECHelper.getNewTask(GetDlQueueAsyncTask.class), 
                TaskScheduleMode.BEST_EFFORT
        );
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
        // TODO Auto-generated method stub
        return this.getClass().getName();
    }

    @Override
    public void notifyStatusChange(AmuleClientStatus status) {
        
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
        
        mIsProgressShown = show;
        invalidateOptionsMenu();
        
    }
}