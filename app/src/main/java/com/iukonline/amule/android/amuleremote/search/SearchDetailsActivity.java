/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.search;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECSearchListWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.GetCategoriesAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.SearchAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.SearchStartResultAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment.AlertDialogListener;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.android.amuleremote.search.SearchResultDetailsFragment.SearchResultDetailsFragmentContainter;
import com.iukonline.amule.ec.ECSearchFile;

import java.util.ArrayList;

public class SearchDetailsActivity extends ActionBarActivity implements AlertDialogListener, SearchResultDetailsFragmentContainter, RefreshingActivity, ClientStatusWatcher, ECSearchListWatcher {
    
    public final static String BUNDLE_PARAM_POSITION = "position";
    
    public final static String TAG_DIALOG_ADD_SEARCH = "add_search_dialog";
    
    AmuleControllerApplication mApp;
    MenuItem refreshItem;
    boolean mIsProgressShown = false;
    int mPosition;
    SearchContainer mSearch;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getApplication();
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreate: Calling super");
        super.onCreate(savedInstanceState);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreate: Back from super");
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreate: Calling setContentView");
        setContentView(R.layout.act_search_details);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreate: Back from setContentView");
        
        mPosition = getIntent().getExtras().getInt(BUNDLE_PARAM_POSITION);
        mSearch = mApp.mECHelper.getSearchItem(mPosition);
        if (mSearch != null) getSupportActionBar().setTitle(mSearch.mFileName);
        
        if (savedInstanceState == null) {
            SearchResultDetailsFragment f = new SearchResultDetailsFragment();
            f.mPosition = mPosition;
            
            getSupportFragmentManager().beginTransaction().add(R.id.search_details_frag_result_details, f).commit();
        }

        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        adView.loadAd(adRequest);


    }

    @Override
    protected void onPause() {
        super.onPause();
        mApp.registerRefreshActivity(null);
        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        mApp.mECHelper.unRegisterFromECSearchList(this);
        mApp.mOnTopActivity = null;
    }

    @Override
    protected void onResume() {
        mApp.mOnTopActivity = this;
        super.onResume();
        mApp.registerRefreshActivity(this);
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECSearchList(mApp.mECHelper.registerForECSsearchList(this));
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreateOptionsMenu: Inflating menu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_options, menu);

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreateOptionsMenu: Saving MenuItems");
        
        refreshItem = menu.findItem(R.id.menu_search_opt_refresh);
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreateOptionsMenu: Calling super");
        boolean superRet = super.onCreateOptionsMenu(menu);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreateOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onPrepareOptionsMenu: Setting items visibility");
        
        if (refreshItem != null)  {
            
            if (mSearch != null && (mSearch.mSearchStatus== ECSearchStatus.STARTING || mSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
                if (mIsProgressShown) {
                    MenuItemCompat.setActionView(refreshItem, R.layout.refresh_progress);
                    //refreshItem.setActionView(R.layout.refresh_progress);
                } else {
                    MenuItemCompat.setActionView(refreshItem, null);
                    //refreshItem.setActionView(null);
                }
                refreshItem.setVisible(true);
            } else {
                refreshItem.setVisible(false);
            }
        }
        
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onPrepareOptionsMenu: calling super");
        boolean superRet = super.onPrepareOptionsMenu(menu);
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onPrepareOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_search_opt_refresh:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onOptionsItemSelected: menu_opt_refresh selected");
            refreshSearchDetails(TaskScheduleMode.QUEUE);
            return true;
        default:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onOptionsItemSelected: Unknown item selected. Calling super");
            return super.onOptionsItemSelected(item);
        }
    }


    public void refreshSearchDetails(TaskScheduleMode mode)  {
        
        if (mSearch != null && (mSearch.mSearchStatus== ECSearchStatus.STARTING || mSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
            SearchAsyncTask t = (SearchAsyncTask) mApp.mECHelper.getNewTask(SearchAsyncTask.class);
            t.setSearchContainer(mSearch);
            t.setTargetStatus(ECSearchStatus.RUNNING);
            
            mApp.mECHelper.executeTask(t, mode);
        } else {
            // We are doing something else to keep connection alive... 
            GetCategoriesAsyncTask t = (GetCategoriesAsyncTask) mApp.mECHelper.getNewTask(GetCategoriesAsyncTask.class);
            mApp.mECHelper.executeTask(t, mode);
        }
    }
    
    
    // ClientStatusWatcher interface
    
    @Override
    public String getWatcherId() {
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
        supportInvalidateOptionsMenu();
        
    }

    @Override
    public UpdateResult updateECSearchList(ArrayList<SearchContainer> searches) {
        if (searches == null) {
            finish();
        } else {
            supportInvalidateOptionsMenu();
        }
        return UpdateResult.DO_NOTHING;
    }

    @Override
    public void refreshContent() {
        refreshSearchDetails(TaskScheduleMode.BEST_EFFORT);
    }

    @Override
    public void startSearchResult(ECSearchFile sf) {
        
        mApp.mStartDownload = sf;
        
        String dialogMsg = getResources().getString(R.string.dialog_add_search_file, sf.getFileName());
        AlertDialogFragment d = new AlertDialogFragment(null, dialogMsg, true);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.startSearchResult: showing dialog");
        d.show(getSupportFragmentManager(), "add_search_dialog");

    }

    @Override
    public void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values) {
        String tag = dialog.getTag();
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.alertDialogEvent: dialog tag " + tag + ", event " + event);
        if (tag != null) {
            if (tag.equals(TAG_DIALOG_ADD_SEARCH)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK) {
                    if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.alertDialogEvent: add to download");
                    if (mApp.mStartDownload != null) {
                        SearchStartResultAsyncTask t = (SearchStartResultAsyncTask)mApp.mECHelper.getNewTask(SearchStartResultAsyncTask.class);
                        t.setECSearchFile(mApp.mStartDownload);
                        mApp.mECHelper.executeTask(t, TaskScheduleMode.QUEUE);
                        mApp.mStartDownload = null;
                    }
                } else if (event == AlertDialogFragment.ALERTDIALOG_EVENT_CANCEL) {
                    mApp.mStartDownload = null;
                }
            }
        }
    }

}
