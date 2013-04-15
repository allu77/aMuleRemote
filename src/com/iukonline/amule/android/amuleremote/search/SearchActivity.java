package com.iukonline.amule.android.amuleremote.search;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECSearchListWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.GetCategoriesAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.SearchAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.android.amuleremote.search.SearchInputFragment.SearchInputFragmentContainter;
import com.iukonline.amule.android.amuleremote.search.SearchResultsListFragment.SearchResultsListFragmentContainter;

public class SearchActivity extends SherlockFragmentActivity implements RefreshingActivity, SearchInputFragmentContainter, SearchResultsListFragmentContainter, ClientStatusWatcher, ECSearchListWatcher {
    
    public final static int MSG_START_SEARCH = 1;
    
    private final static String TAG_DIALOG_SERVER_VERSION = "dialog_server_version";
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
        getSupportActionBar().setTitle(R.string.search_title);
        mFragManager = getSupportFragmentManager();
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Calling super");
        super.onCreate(savedInstanceState);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Back from super");
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Calling setContentView");
        setContentView(R.layout.act_search);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreate: Back from setContentView");
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        mApp.registerRefreshActivity(null);
        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        mApp.mECHelper.unRegisterFromECSearchList(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mApp.registerRefreshActivity(this);
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECSearchList(mApp.mECHelper.registerForECSsearchList(this));
        
        String serverVersion = mApp.mECHelper.getServerVersion();
        
        
        
        if (serverVersion == null || !(serverVersion.equals("V204") || serverVersion.equals("V203"))) {
            
            if (mFragManager.findFragmentByTag(TAG_DIALOG_SERVER_VERSION) == null) {
                Handler h = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        finish();
                    }
                };
                
                AlertDialogFragment d = new AlertDialogFragment(R.string.dialog_search_not_available_title, R.string.dialog_search_not_available_message, h.obtainMessage(), null, false);
                
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onResume: search not available - showing dialog");
                d.show(mFragManager, TAG_DIALOG_SERVER_VERSION);
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onResume: search not available - end");

            }
        } else {
            FragmentTransaction ft = mFragManager.beginTransaction();
            if (mFragManager.findFragmentByTag(TAG_FRAGMENT_SEARCH_INPUT) == null) {
                ft.replace(R.id.frag_search_input, new SearchInputFragment(), TAG_FRAGMENT_SEARCH_INPUT);
            }
            if (mFragManager.findFragmentByTag(TAG_FRAGMENT_SEARCH_RESULTS) == null) {
                ft.replace(R.id.frag_search_results, new SearchResultsListFragment(), TAG_FRAGMENT_SEARCH_RESULTS);
            }
            ft.commit();

        }
        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.onCreateOptionsMenu: Inflating menu");

        MenuInflater inflater = getSupportMenuInflater();
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
            
            if (lastSearch != null && (lastSearch.mSearchStatus== ECSearchStatus.STARTING || lastSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
                if (mIsProgressShown) {
                    refreshItem.setActionView(R.layout.refresh_progress);
                } else {
                    refreshItem.setActionView(null);
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
        
        if (lastSearch != null && (lastSearch.mSearchStatus== ECSearchStatus.STARTING || lastSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
            
            Handler h = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.startSearch: delete confirmed");
                    lastSearch.mSearchStatus = ECSearchStatus.STOPPED;
                    startSearchTask((SearchContainer) msg.obj);
                }
            };
            
            Message mOk = h.obtainMessage(MSG_START_SEARCH, s);
            
            AlertDialogFragment d = new AlertDialogFragment(R.string.dialog_start_search, mOk, null, true);
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.startSearch: showing dialog");
            d.show(getSupportFragmentManager(), "start_search_dialog");
            
        } else {
            startSearchTask(s);
        }
        
    }
    
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
        
        if (lastSearch != null && (lastSearch.mSearchStatus== ECSearchStatus.STARTING || lastSearch.mSearchStatus == ECSearchStatus.RUNNING)) {
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
    public void updateECSearchList(ArrayList<SearchContainer> searches) {
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
    }

    @Override
    public void viewResultDetails(int selected) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchActivity.viewResultDetails: Result " +  selected + " selected , starting SearchDetailsActivity");

        Intent i = new Intent(this, SearchDetailsActivity.class);
        i.putExtra(SearchDetailsActivity.BUNDLE_PARAM_POSITION, selected);
        startActivity(i);
    }
    
}
