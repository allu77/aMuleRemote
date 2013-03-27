package com.iukonline.amule.android.amuleremote.search;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.SearchStartResultAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.android.amuleremote.search.SearchResultDetailsFragment.SearchResultDetailsFragmentContainter;
import com.iukonline.amule.ec.ECSearchFile;

public class SearchDetailsActivity extends SherlockFragmentActivity implements SearchResultDetailsFragmentContainter, RefreshingActivity, ClientStatusWatcher, ECSearchListWatcher {
    
    public final static String BUNDLE_PARAM_POSITION = "position";
    
    public final static int MSG_ADD_SEARCHFILE = 1;
    
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
        
        if (savedInstanceState == null) {
            SearchResultDetailsFragment f = new SearchResultDetailsFragment();
            f.mPosition = mPosition;
            
            getSupportFragmentManager().beginTransaction().add(R.id.search_details_frag_result_details, f).commit();
        }
        
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
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.onCreateOptionsMenu: Inflating menu");

        MenuInflater inflater = getSupportMenuInflater();
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
                    refreshItem.setActionView(R.layout.refresh_progress);
                } else {
                    refreshItem.setActionView(null);
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
            // TODO: Launch a no-op or default task
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
    public void updateECSearchList(ArrayList<SearchContainer> searches) {
        if (searches == null) finish();
        
        supportInvalidateOptionsMenu();
    }

    @Override
    public void refreshContent() {
        refreshSearchDetails(TaskScheduleMode.BEST_EFFORT);
    }

    @Override
    public void startSearchResult(ECSearchFile sf) {
        
        Handler h = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.startSearchResult: delete confirmed");
                SearchStartResultAsyncTask t = (SearchStartResultAsyncTask)mApp.mECHelper.getNewTask(SearchStartResultAsyncTask.class);
                t.setECSearchFile((ECSearchFile) msg.obj);
                mApp.mECHelper.executeTask(t, TaskScheduleMode.QUEUE);
            }
        };
        
        Message mOk = h.obtainMessage(MSG_ADD_SEARCHFILE, sf);
        
        String dialogMsg = getResources().getString(R.string.dialog_add_search_file, sf.getFileName());
        AlertDialogFragment d = new AlertDialogFragment(null, dialogMsg, mOk, null, true);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchDetailsActivity.startSearchResult: showing dialog");
        d.show(getSupportFragmentManager(), "add_search_dialog");
        
        
        

    }

}
