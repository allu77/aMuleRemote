package com.iukonline.amule.android.amuleremote;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.DlQueueFragment.DlQueueFragmentContainer;
import com.iukonline.amule.android.amuleremote.UpdateChecker.UpdatesWatcher;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ECStatsWatcher;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AddEd2kAsyncTask;
import com.iukonline.amule.android.amuleremote.echelper.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.echelper.tasks.GetDlQueueAsyncTask;
import com.iukonline.amule.android.amuleremote.echelper.tasks.GetECStatsAsyncTask;
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
        MyAlertDialogFragment dialogFrag = MyAlertDialogFragment.newInstance();
        AlertDialog.Builder builder = dialogFrag.getAlertDialogBuilder(this);
        
        // TODO Create string resource
        builder.setTitle("Provide ED2K link");
        final EditText input = new EditText(this);
        input.setText(url);
        builder.setView(input);
        builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                
                AddEd2kAsyncTask ed2kTask = (AddEd2kAsyncTask) mApp.mECHelper.getNewTask(AddEd2kAsyncTask.class);
                ed2kTask.setEd2kUrl(input.getText().toString());
                
                if (mApp.mECHelper.executeTask(ed2kTask, TaskScheduleMode.QUEUE)) {
                    refreshDlQueue(TaskScheduleMode.QUEUE);
                }
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
              }
            });
        
        dialogFrag.show(getSupportFragmentManager(), "rename_dialog");
    }
    
    public void showAboutDialog() {
        //DialogFragment d = mApp.getAboutDialog();
        
        String versionName = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (NameNotFoundException e) {

        }
        
        MyAlertDialogFragment d = MyAlertDialogFragment.newInstance();
        AlertDialog.Builder db = d.getAlertDialogBuilder(this);
        
        View aboutView =  getLayoutInflater().inflate(R.layout.about_dialog, null);
        ((TextView) aboutView.findViewById(R.id.about_dialog_appname)).setText(getString(R.string.app_name) + " " + versionName);
        ((TextView) aboutView.findViewById(R.id.about_dialog_release_notes)).setText(mApp.getReleaseNotes());

        db.setView(aboutView);
        db.setPositiveButton(R.string.alert_dialog_ok, null);
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
        // TODO Auto-generated method stub
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
            ((TextView) findViewById(R.id.main_dl_rate)).setText(GUIUtils.longToBytesFormatted(newStats.getSpeedDl()) + "/s");
            ((TextView) findViewById(R.id.main_ul_rate)).setText(GUIUtils.longToBytesFormatted(newStats.getSpeedUl()) + "/s");
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
        
        
        MyAlertDialogFragment d = MyAlertDialogFragment.newInstance();
        AlertDialog.Builder db = d.getAlertDialogBuilder(this);
        
        
        View newVersionView =  getLayoutInflater().inflate(R.layout.new_version_dialog, null);
        ((TextView) newVersionView.findViewById(R.id.new_version_dialog_download_url)).setText(newReleaseURL);
        ((TextView) newVersionView.findViewById(R.id.new_version_dialog_release_notes)).setText(releaseNotes);
        
        db.setView(newVersionView);
        db.setPositiveButton(R.string.alert_dialog_ok, null);
        
        d.show(getSupportFragmentManager(), "new_release_dialog");
        
    }
}