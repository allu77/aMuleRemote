package com.iukonline.amule.android.amuleremote.search;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment.AlertDialogListener;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.android.amuleremote.search.SearchInputFragment.SearchInputFragmentContainter;
import com.iukonline.amule.android.amuleremote.search.SearchResultsListFragment.SearchResultsListFragmentContainter;

import java.util.ArrayList;

public class SearchActivity extends ActionBarActivity implements AlertDialogListener, RefreshingActivity, SearchInputFragmentContainter, SearchResultsListFragmentContainter, ClientStatusWatcher, ECSearchListWatcher {
    
    
    private final static String TAG_DIALOG_SERVER_VERSION = "dialog_server_version";
    private final static String TAG_DIALOG_START_SEARCH = "dialog_start_search";
    
    private final static String TAG_FRAGMENT_SEARCH_INPUT = "frag_search_input";
    private final static String TAG_FRAGMENT_SEARCH_RESULTS = "frag_search_results";

    
    
    AmuleControllerApplication mApp;
    MenuItem refreshItem;
    boolean mIsProgressShown = false;
    SearchContainer lastSearch = null;

    FragmentManager mFragManager;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getApplication();

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Calling super");
        super.onCreate(savedInstanceState);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Back from super");
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Calling setContentView");
        setContentView(R.layout.act_search);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Back from setContentView");

        getSupportActionBar().setTitle(R.string.search_title);
        mFragManager = getSupportFragmentManager();

        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        adView.loadAd(adRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.registerRefreshActivity(this);
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECSearchList(mApp.mECHelper.registerForECSsearchList(this));
        
        
        
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        
        String serverVersion = mApp.mECHelper.getServerVersion();
        
        if (serverVersion != null && (serverVersion.equals("V204") || serverVersion.equals("V203"))) {
            FragmentTransaction ft = mFragManager.beginTransaction();
            if (mFragManager.findFragmentByTag(TAG_FRAGMENT_SEARCH_INPUT) == null) {
                ft.replace(R.id.frag_search_input, new SearchInputFragment(), TAG_FRAGMENT_SEARCH_INPUT);
            }
            if (mFragManager.findFragmentByTag(TAG_FRAGMENT_SEARCH_RESULTS) == null) {
                ft.replace(R.id.frag_search_results, new SearchResultsListFragment(), TAG_FRAGMENT_SEARCH_RESULTS);
            }
            ft.commit();
        }
        
        if (serverVersion == null || !(serverVersion.equals("V204") || serverVersion.equals("V203"))) {
            
            if (mFragManager.findFragmentByTag(TAG_DIALOG_SERVER_VERSION) == null) {
                
                AlertDialogFragment d = new AlertDialogFragment(R.string.dialog_search_not_available_title, R.string.dialog_search_not_available_message, false);
                
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onResume: search not available - showing dialog");
                d.show(mFragManager, TAG_DIALOG_SERVER_VERSION);
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onResume: search not available - end");
                
            }
        }

        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreateOptionsMenu: Inflating menu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_options, menu);

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreateOptionsMenu: Saving MenuItems");
        
        refreshItem = menu.findItem(R.id.menu_search_opt_refresh);
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreateOptionsMenu: Calling super");
        boolean superRet = super.onCreateOptionsMenu(menu);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreateOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onPrepareOptionsMenu: Setting items visibility");
        
        if (refreshItem != null)  {
            
            if (lastSearch != null && (lastSearch.mSearchStatus == ECSearchStatus.STARTING || lastSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
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
        
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onPrepareOptionsMenu: calling super");
        boolean superRet = super.onPrepareOptionsMenu(menu);
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onPrepareOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_search_opt_refresh:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onOptionsItemSelected: menu_opt_refresh selected");
            refreshSearchList(TaskScheduleMode.QUEUE);
            return true;
        default:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onOptionsItemSelected: Unknown item selected. Calling super");
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    public void startSearch(SearchContainer s) {
        
        if (lastSearch != null && (lastSearch.mSearchStatus == ECSearchStatus.STARTING || lastSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
            
            mApp.mStartSearch = s;
            AlertDialogFragment d = new AlertDialogFragment(R.string.dialog_start_search, true);
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.startSearch: showing dialog");
            d.show(getSupportFragmentManager(), TAG_DIALOG_START_SEARCH);
            
        } else {
            startSearchTask(s);
        }
        
    }
    
    @SuppressWarnings("ConstantConditions") // IntelliJ was failing code inspection on mApp nullable
    private void startSearchTask(SearchContainer s) {
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.startSearchTask: Adding search");

        mApp.mECHelper.addSearchToList(s);
        mApp.mECHelper.notifyECSearchListWatcher();

        SearchAsyncTask t = (SearchAsyncTask) mApp.mECHelper.getNewTask(SearchAsyncTask.class);
        t.setSearchContainer(s);
        t.setTargetStatus(ECSearchStatus.RUNNING);
        
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.startSearchTask: Scheduling start task");
        mApp.mECHelper.executeTask(t, TaskScheduleMode.QUEUE);
        
        
        viewResultDetails(0);
    }
    
    public void refreshSearchList(TaskScheduleMode mode)  {
        
        if (lastSearch != null && (lastSearch.mSearchStatus == ECSearchStatus.STARTING || lastSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
            SearchAsyncTask t = (SearchAsyncTask) mApp.mECHelper.getNewTask(SearchAsyncTask.class);
            t.setSearchContainer(lastSearch);
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
    public void refreshContent() {
        refreshSearchList(TaskScheduleMode.BEST_EFFORT);
    }



    @Override
    public UpdateResult updateECSearchList(ArrayList<SearchContainer> searches) {
        if (searches == null) {
            finish();
        } else {
            if (searches.size() == 0) {
                lastSearch = null;
            } else {
                lastSearch = searches.get(0);
                supportInvalidateOptionsMenu();
            }
        }
        return UpdateResult.DO_NOTHING;
    }

    @Override
    public void viewResultDetails(int selected) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.viewResultDetails: Result " +  selected + " selected , starting SearchDetailsActivity");

        Intent i = new Intent(this, SearchDetailsActivity.class);
        i.putExtra(SearchDetailsActivity.BUNDLE_PARAM_POSITION, selected);
        startActivity(i);
    }

    @Override
    public void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values) {
        String tag = dialog.getTag();
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.alertDialogEvent: dialog tag " + tag + ", event " + event);
        if (tag != null) {
            if (tag.equals(TAG_DIALOG_SERVER_VERSION)) {
                finish();
            } else if (tag.equals(TAG_DIALOG_START_SEARCH)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK) {
                    if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.alertDialogEvent: running search termination confirmed");
                    if (mApp.mStartSearch != null) {
                        if (lastSearch != null) lastSearch.mSearchStatus = ECSearchStatus.STOPPED;
                        startSearchTask(mApp.mStartSearch);
                        mApp.mStartSearch = null;
                    }
                } else if (event == AlertDialogFragment.ALERTDIALOG_EVENT_CANCEL) {
                    mApp.mStartSearch = null;
                }
            }
        }
    }
    
}
