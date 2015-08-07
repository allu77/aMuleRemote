/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.dlqueue;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iukonline.amule.android.amuleremote.AboutDialogFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.dlqueue.DlQueueFragment.DlQueueFragmentContainer;
import com.iukonline.amule.android.amuleremote.helpers.SettingsHelper;
import com.iukonline.amule.android.amuleremote.helpers.UpdateChecker.UpdatesWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.CategoriesWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECStatsWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AddEd2kAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.GetCategoriesAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.GetDlQueueAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.GetECStatsAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.gui.GUIUtils;
import com.iukonline.amule.android.amuleremote.helpers.gui.TooltipHelper;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment.AlertDialogListener;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.EditTextDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.NewVersionDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.TooltipDialogFragment;
import com.iukonline.amule.android.amuleremote.partfile.PartFileActivity;
import com.iukonline.amule.android.amuleremote.search.SearchActivity;
import com.iukonline.amule.android.amuleremote.settings.SettingsActivity;
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECConnState;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.ECUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AmuleRemoteActivity extends AppCompatActivity implements AlertDialogListener, ClientStatusWatcher, DlQueueFragmentContainer, ECStatsWatcher, CategoriesWatcher, RefreshingActivity, UpdatesWatcher  {
    private final static String TAG = AmuleControllerApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
    
    public final static String BUNDLE_PARAM_ERRSTR          = "errstr";
    public final static String BUNDLE_PARAM_URI_TO_HANDLE   = "uri_to_handle";
    
    private final static String BUNDLE_CATEGORY_FILTER = "category";
    
    public final static String NO_URI_TO_HANDLE       = "NO_URI";
    
    private final static String TAG_DIALOG_NO_SERVER = "dialog_no_server";
    private final static String TAG_DIALOG_ADD_ED2K = "dialog_add_ed2k";
    private final static String TAG_DIALOG_TOOLTIP = "dialog_tooltip";
    
    private AmuleControllerApplication mApp;
    private String mHandleURI;
    private NavigationAdapter mNavigationAdapter;

    private long mCatId = ECCategory.NEW_CATEGORY_ID;
    
    String mED2KUrl;
    
    private boolean mIsProgressShown = false;
    private boolean mServerConfigured = false;
    
    MenuItem refreshItem;
    MenuItem addEd2kItem;
    
    MenuItem debugOptionsItem;
    
    @InjectView(R.id.main_dl_rate)TextView mTextDlRate;
    @InjectView(R.id.main_ul_rate) TextView mTextUlRate;
    @InjectView(R.id.main_edonkey_status) TextView mTextEDonkeyStatus;
    @InjectView(R.id.main_kad_status) TextView mTextKADStatus;
    @InjectView(R.id.main_conn_bar) View mViewConnBar;

    ActionBar mActionBar;
    ActionBar.OnNavigationListener mNavigationListener;
    
    FragmentManager mFragManager;
    TooltipHelper mTooltipHelper;

    private DlQueueFragment mDlQueueFragment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getApplication();
        mApp.refreshDebugSettings();
        mTooltipHelper = new TooltipHelper(mApp.mSettings);
        
        mFragManager = getSupportFragmentManager();
        
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreate: Calling super");
        super.onCreate(savedInstanceState);
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreate: Back from super");
        
        if (savedInstanceState != null) {
            mCatId = savedInstanceState.getLong(BUNDLE_CATEGORY_FILTER, ECCategory.NEW_CATEGORY_ID);
        }
        
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreate: Calling setContentView");
        setContentView(R.layout.main);
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreate: back from setContentView");
        ButterKnife.inject(this);

        mActionBar = getSupportActionBar();
        createNavigation();
        mApp.mSettingsHelper.mNeedNavigationRefresh = true;

        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        adView.loadAd(adRequest);
        
        Intent i = getIntent();
        String a = i.getAction();
        
        mHandleURI = (a != null && a.equals(Intent.ACTION_VIEW)) ? i.getData().toString() : NO_URI_TO_HANDLE; 

        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreate: end");

    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment.getId() == R.id.fragment_dlqueue) mDlQueueFragment = (DlQueueFragment) fragment;
    }

    private void createNavigation() {

        mNavigationAdapter = new NavigationAdapter(mApp, R.layout.part_nav_text);
        mNavigationAdapter.setNotifyOnChange(false);

        mNavigationListener = new ActionBar.OnNavigationListener() {

            private boolean isFirstSelection = true;

            @Override
            public boolean onNavigationItemSelected(int position, long itemId) {
                if (isFirstSelection) {
                    // Avoid doing anything on navigation initialization
                    isFirstSelection = false;
                    return true;
                }

                NavigationItem n = mNavigationAdapter.getItem(position);
                if (n.isCategory()) {
                    if (DEBUG) Log.d(TAG, "AmuleRemoteActivity->onNavigationItemSelected: category " + n.getCategory().getId() + " clicked");
                    mCatId = n.getCategory().getId();
                    if (mDlQueueFragment != null)
                        mDlQueueFragment.filterDlQueueByCategory(mCatId);
                } else {
                    int serverIndex = mNavigationAdapter.getServerIndex(position);
                    if (DEBUG) Log.d(TAG, "AmuleRemoteActivity->onNavigationItemSelected: setting server " + serverIndex);
                    mNavigationAdapter.cleanCategories();
                    mNavigationAdapter.setSelectedServer(serverIndex);
                    mApp.mSettingsHelper.setCurrentServer(serverIndex);
                    mApp.mECHelper.resetClient();
                    mApp.refreshServerSettings();
                    registerAllListeners();
                    filterDlQueueByCategory(ECCategory.NEW_CATEGORY_ID);
                    refreshDlQueue();
                }
                return true;
            }
        };

    }

    private void showNavigation() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        mActionBar.setTitle("");
    }

    private void hideNavigation() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        mActionBar.setTitle(R.string.app_name);
    }

    @Override
    protected void onResume() {

        mApp.mOnTopActivity = this;

        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: Reading settings");
        mApp.refreshDebugSettings();
        mServerConfigured = mApp.refreshServerSettings();

        if (mServerConfigured && mApp.mSettingsHelper.mNeedNavigationRefresh) {
            int serverCount = mApp.mSettingsHelper.getServerCount();
            int selectedServer = mApp.mSettingsHelper.getCurrentServer();
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: Navigation needs a refresh. Setting server " + selectedServer + ", server count " + serverCount);
            mNavigationAdapter.clear();
            mNavigationAdapter.setServerCount(serverCount);
            if (serverCount > 1) {
                for (int i = 0; i < serverCount; i++) {
                    mNavigationAdapter.add(new NavigationItem(mApp.mSettingsHelper.getServerSettings(i)));
                }
                showNavigation();
                mNavigationAdapter.setSelectedServer(selectedServer);
                mActionBar.setSelectedNavigationItem(selectedServer);
                mActionBar.setListNavigationCallbacks(mNavigationAdapter, mNavigationListener);
            } else {
                hideNavigation();
            }
            mApp.mSettingsHelper.mNeedNavigationRefresh = false;
        }


        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: Calling super");
        super.onResume();
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: Back from super");
        

        // TBV: Everything that creates a dialog should be in onPostResume due to a bug in ICS
        // https://code.google.com/p/android/issues/detail?id=23096
        
        // TBV: This should clear the disappearing refresh bug. Not elegant as onCreateMenu gets called twice...
        supportInvalidateOptionsMenu();
        registerAllListeners();
        mApp.registerRefreshActivity(this);

        if (! mApp.mECHelper.isDlQueueValid()) {
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: launching refreshDlQueue");
            refreshDlQueue();
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: back from refreshDlQueue");
        }

        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: end");
        
    }

    private void registerAllListeners() {
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onResume: registering for async activities");
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECStats(mApp.mECHelper.registerForECStatsUpdates(this));
        updateCategories(mApp.mECHelper.registerForCategoriesUpdates(this));
    }

    private void createNavigationAdapter(int serverCount, int selectedServer) {
        mNavigationAdapter = new NavigationAdapter(mApp, R.layout.part_nav_text);
        mNavigationAdapter.setNotifyOnChange(false);
    }

    private void filterDlQueueByCategory(long catId) {
        mCatId = catId;
        if (mNavigationAdapter != null) {
            int catPos = mNavigationAdapter.findCategoryById(catId);
            mActionBar.setSelectedNavigationItem(catPos);
        }
        mDlQueueFragment.filterDlQueueByCategory(catId);
    }
    
    
    
    @Override
    protected void onPostResume() {
        
        // Everyting that creates a dialog should be in onPostResume
        // https://code.google.com/p/android/issues/detail?id=23096
        super.onPostResume();
        showPostResumeDialog();
        
    }
    
    protected void showPostResumeDialog() {
        if (mApp.showWhatsNew(mFragManager)) return;
        
        // SHOW TOOLTIPS
        
        if (! mServerConfigured) {
            if (mFragManager.findFragmentByTag(TAG_DIALOG_NO_SERVER) == null) {
                if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.showPostResumeDialog: no server configured - showing dialog");
                AlertDialogFragment d = new AlertDialogFragment(R.string.dlqueue_dialog_title_no_server_configured, R.string.dlqueue_dialog_message_no_server_configured, true);
                d.show(mFragManager, TAG_DIALOG_NO_SERVER);
            }
            return;
        }

        if (! mHandleURI.equals(NO_URI_TO_HANDLE)) {
            String parURI = new String(mHandleURI);
            mHandleURI = new String(NO_URI_TO_HANDLE);
            if (parURI.toLowerCase().startsWith("ed2k://%7c")) {
                parURI = parURI.replaceAll("%7C", "|").replaceAll("%7c", "|");
            }
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onPostResume: handling ed2k URI");
            showAddED2KDialog(parURI);
            return;
        }
        
        if (mFragManager.findFragmentByTag(TAG_DIALOG_TOOLTIP) == null) {
            TooltipDialogFragment tooltip = mTooltipHelper.getNextTooltipDialog();
            if (tooltip != null) {
                tooltip.show(mFragManager, TAG_DIALOG_TOOLTIP);
            }
        } else {
            return;
        }

        mApp.mUpdateChecker.registerUpdatesWatcher(this);
        
    }


    @Override
    protected void onPause() {

        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onPause: un-registering from async activities");

        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        mApp.mECHelper.unRegisterFromECStatsUpdates(this);
        mApp.mECHelper.unRegisterFromCategoriesUpdates(this);

        mApp.registerRefreshActivity(null);
        mApp.mUpdateChecker.registerUpdatesWatcher(null);

        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onPause: calling super");
        super.onPause();
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onPause: end");

        mApp.mOnTopActivity = null;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onSaveInstanceState: adding data to bundle");
        outState.putLong(BUNDLE_CATEGORY_FILTER, mCatId);
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onSaveInstanceState: calling super");
        super.onSaveInstanceState(outState);
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onSaveInstanceState: end");

    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreateOptionsMenu: Inflating menu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);

        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreateOptionsMenu: Saving MenuItems");
        
        refreshItem = menu.findItem(R.id.menu_opt_refresh);
        addEd2kItem = menu.findItem(R.id.menu_opt_added2k);

        debugOptionsItem = menu.findItem(R.id.menu_opt_debug);
        
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreateOptionsMenu: Calling super");
        boolean superRet = super.onCreateOptionsMenu(menu);
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onCreateOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        if (mApp != null && DEBUG) Log.d(TAG, "AmuleRemoteActivity.onPrepareOptionsMenu: Setting items visibility");
        
        if (refreshItem != null) {
            refreshItem.setVisible(mServerConfigured);
            if (mIsProgressShown) {
                MenuItemCompat.setActionView(refreshItem, R.layout.refresh_progress);
            } else {
                MenuItemCompat.setActionView(refreshItem, null);
            }
        }

        addEd2kItem.setVisible(mServerConfigured);
        
        if (mApp != null) {
            if (debugOptionsItem != null) debugOptionsItem.setVisible(DEBUG);
        }

        if (mApp != null && DEBUG) Log.d(TAG, "AmuleRemoteActivity.onPrepareOptionsMenu: calling super");
        boolean superRet = super.onPrepareOptionsMenu(menu);
        if (mApp != null && DEBUG) Log.d(TAG, "AmuleRemoteActivity.onPrepareOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_opt_refresh:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_refresh selected");
            refreshDlQueue(TaskScheduleMode.QUEUE);
            return true;
        case R.id.menu_opt_refresh_cat:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_refresh_cat selected");
            refreshCategories(TaskScheduleMode.QUEUE);
            return true;
        case R.id.menu_opt_settings:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_settings selected");
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true; 
        case R.id.menu_opt_reset:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_reset selected");
            mApp.mECHelper.resetClient();
            return true;
        case R.id.menu_opt_added2k:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_added2k selected");
            showAddED2KDialog(null);
            return true;
        case R.id.menu_opt_about:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_about selected");
            showAboutDialog();
            return true;
        case R.id.menu_opt_send_report:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_send_report selected");
            //ErrorReporter.getInstance().handleException(new Exception("MANUAL BUG REPORT"));
            showManualBugReportDialog();
            return true;
        case R.id.menu_opt_help:
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.url_help)));
            startActivity(myIntent);
            return true;
        case R.id.menu_opt_search:
            Intent searchActivity = new Intent(this, SearchActivity.class);
            startActivity(searchActivity);
            return true;
        case R.id.menu_opt_reset_app_version_info:
            mApp.resetAppVersionInfo();
            return true; 
        case R.id.menu_opt_reset_tips:
            mTooltipHelper.resetShown();
            return true; 
        default:
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.onOptionsItemSelected: Unknown item selected. Calling super");
            return super.onOptionsItemSelected(item);
        }
    }

    
    public void refreshDlQueue()  {
        refreshDlQueue(TaskScheduleMode.BEST_EFFORT);
    }

    public void refreshDlQueue(TaskScheduleMode mode)  {

        if (mApp.mECHelper.getCategories() == null) {
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.refreshDlQueue: Category list null, scheduling refresh");
            if (!refreshCategories(mode)) return;
            mode = TaskScheduleMode.QUEUE;
        }
        
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.refreshDlQueue: Scheduling Get Stats Task");
        if (mApp.mECHelper.executeTask(mApp.mECHelper.getNewTask(GetECStatsAsyncTask.class), mode)) {
            GetDlQueueAsyncTask t = (GetDlQueueAsyncTask) mApp.mECHelper.getNewTask(GetDlQueueAsyncTask.class);
            t.setDlQueue(mApp.mECHelper.getDlQueue());
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.refreshDlQueue: Scheduling GetDlQueue Task");
            mApp.mECHelper.executeTask(t, TaskScheduleMode.QUEUE);
        }
    }
    
    public boolean refreshCategories() {
        return refreshCategories(TaskScheduleMode.BEST_EFFORT);
    }
    
    public boolean refreshCategories(TaskScheduleMode mode) {
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.refreshCategories: Scheduling GetGategories Task");
        return mApp.mECHelper.executeTask(mApp.mECHelper.getNewTask(GetCategoriesAsyncTask.class), mode);
    }
    
    public void showAddED2KDialog(String url) {
        
        if (mFragManager.findFragmentByTag(TAG_DIALOG_ADD_ED2K) == null) {
            EditTextDialogFragment d = new EditTextDialogFragment(R.string.dialog_added2k_title, url);
    
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.showAddED2KDialog: showing dialog");
            d.show(mFragManager, TAG_DIALOG_ADD_ED2K);
        }
    }
    
    public void showAboutDialog() {
        
        String versionName = null;
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = pInfo.versionName;
        } catch (NameNotFoundException e) {
        }
        
        AboutDialogFragment d = new AboutDialogFragment(versionName, mApp.getReleaseNotes());
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.showAboutDialog: showing dialog");
        d.show(getSupportFragmentManager(), "about_dialog");
    }
    
    public void showManualBugReportDialog() {
        
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");

        String subject = "[Bug report]";
        String[] to = new String[1];
        to[0] = "amuleremote@iukonline.com";
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.setType("text/html");

        String data = "<p>" + getResources().getString(R.string.bug_report_mail_text)  + "</p>";

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, Html.fromHtml(data));
        startActivity(Intent.createChooser(emailIntent, "Email:"));

    }
    
    
    
    // DlQueueFragmentContainer
    
    public void partFileSelected(byte[] hash) {
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.partFileSelected: Partfile " + ECUtils.byteArrayToHexString(hash) + " selected, starting PartFileActivity");

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
    public void updateECStats(ECStats newStats) {
        if (newStats != null) {
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.updateECStats: Updating Stats");
            
            mTextDlRate.setText(GUIUtils.longToBytesFormatted(newStats.getDlSpeed()) + "/s \u2193");
            mTextUlRate.setText(GUIUtils.longToBytesFormatted(newStats.getUlSpeed()) + "/s \u2191");
            
            ECConnState c = newStats.getConnState();
            if (c == null) {
                mTextEDonkeyStatus.setText(R.string.stats_status_not_connected);
                mTextKADStatus.setText(R.string.stats_status_not_connected);
            } else {
                if (c.isKadFirewalled()) {
                    mTextKADStatus.setText(R.string.stats_status_firewalled);
                } else if (c.isKadRunning()) {
                    mTextKADStatus.setText(R.string.stats_status_connected);
                } else {
                    mTextKADStatus.setText(R.string.stats_status_not_connected);
                }
                
                if (c.isConnectedEd2k()) {
                    mTextEDonkeyStatus.setText(R.string.stats_status_connected);
                } else if (c.isConnectingEd2k()) {
                    mTextEDonkeyStatus.setText(R.string.stats_status_connecting);
                } else {
                    mTextEDonkeyStatus.setText(R.string.stats_status_not_connected);
                }
            }
            
            mViewConnBar.setVisibility(View.VISIBLE);
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.updateECStats: Stats updated");
        } else {
            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.updateECStats: Hiding connection bar");
            mViewConnBar.setVisibility(View.GONE);
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
        
        // FIXME: Prima di rilasciare verificare che app number sul manifest sia incrementato...
        
        NewVersionDialogFragment d = new NewVersionDialogFragment(newReleaseURL, releaseNotes);
        d.show(getSupportFragmentManager(), "new_release_dialog");
        
    }

    @Override
    public void updateCategories(ECCategory[] newCategoryList) {
        if (newCategoryList == null) {
            if (mNavigationAdapter.getServerCount() > 1) {
                mNavigationAdapter.cleanCategories();
                filterDlQueueByCategory(ECCategory.NEW_CATEGORY_ID);
            } else {
                hideNavigation();
            }
            return;
        }

        showNavigation();
        mNavigationAdapter.cleanCategories();
        mNavigationAdapter.addCategories(newCategoryList);
        mNavigationAdapter.addCategory(new ECCategory(getResources().getString(R.string.cat_all_files), null, null, (byte) 0, (byte) 0));
        mActionBar.setListNavigationCallbacks(mNavigationAdapter, mNavigationListener);
        filterDlQueueByCategory(mCatId);
    }

    private class NavigationItem {
        SettingsHelper.ServerSettings mServer;
        ECCategory mCategory;

        NavigationItem(SettingsHelper.ServerSettings server) { mServer = server; }
        NavigationItem(ECCategory cat ) { mCategory = cat; }

        public SettingsHelper.ServerSettings getServer() { return mServer; }
        public ECCategory getCategory() { return mCategory; }

        public boolean isCategory() { return mCategory != null; }
        public String getNavigationText() {
            if (mCategory != null) {
                return mCategory.getId() == 0L ? getString(R.string.category_uncategorized) : mCategory.getTitle();
            }

            if (mServer != null) return mServer.name;

            return "";
        }
    }

    private class NavigationAdapter extends ArrayAdapter<NavigationItem> {

        private int mServerCount = 0;
        private int mSelectedServer = -1;

        public NavigationAdapter (Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (DEBUG) Log.d(TAG, "NavigationAdapter.getView: creating view for position " + position + ", mSelectedServer " + mSelectedServer + ", mServerCount " + mServerCount);
            NavViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(mApp, R.layout.part_nav_text, null);
                holder = new NavViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (NavViewHolder) convertView.getTag();
            }
            NavigationItem item = getItem(position);
            if (item.isCategory()) {
                holder.mCategoryText.setText(item.getNavigationText());
                if (mServerCount > 1) {
                    holder.mServerText.setText(getItem(mSelectedServer).getNavigationText());
                    holder.mServerText.setVisibility(View.VISIBLE);
                } else {
                    holder.mServerText.setVisibility(View.GONE);
                }
            } else {
                holder.mServerText.setText(item.getNavigationText());
                holder.mCategoryText.setText(getText(R.string.cat_all_files));
            }
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            NavViewDropDownHolder holder;
            NavigationItem item = getItem(position);
            if (convertView == null) {
                convertView = View.inflate(mApp, R.layout.part_nav_dropdown, null);
                holder = new NavViewDropDownHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (NavViewDropDownHolder) convertView.getTag();
            }
            holder.mNavText.setText(item.getNavigationText());
            if (item.isCategory()) {
                holder.mNavText.setTextSize(getResources().getDimension(R.dimen.abc_text_size_small_material));
                holder.mNavCatBox.setVisibility(View.VISIBLE);

                if (holder.mNavCatBox.getDrawable() != null) {
                    ECCategory cat = item.getCategory();
                    if (cat.getId() == ECCategory.NEW_CATEGORY_ID || cat.getId() == 0L) {
                        ((GradientDrawable) holder.mNavCatBox.getDrawable()).setColor(Color.argb(0, 0, 0, 0));
                    } else {
                        long color = cat.getColor();
                        long r = color / 65536L;
                        long g = (color % 65536L) / 256L;
                        long b = color % 256L;
                        ((GradientDrawable) holder.mNavCatBox.getDrawable()).setColor(Color.argb(255, (int) r, (int) g, (int) b));
                    }

                }

                /*GradientDrawable backgroundGradient = (GradientDrawable) holder.mNavCatBox.getBackground();
                if (backgroundGradient != null) backgroundGradient.setColor(Color.rgb((int) r, (int) g, (int) b));*/
            } else {
                holder.mNavCatBox.setVisibility(View.GONE);
                holder.mNavText.setTextSize(getResources().getDimension(R.dimen.abc_text_size_subhead_material));
            }
            return convertView;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return mServerCount == 0;
        }

        @Override
        public boolean isEnabled(int position) {
            return mServerCount == 0 || position != mSelectedServer;
        }

        public void setServerCount (int server) { mServerCount = server; }
        public int getServerCount () { return mServerCount; }
        public void setSelectedServer(int server) { mSelectedServer = server; }
        public int getSelectedServer() { return mSelectedServer; }


        public void addCategory(ECCategory cat) {
            addCategories(new ECCategory[] {cat});
        }

        public void addCategories(ECCategory[] catList) {
            if (catList == null) return;

            int startIndex = mServerCount > 1 ? mSelectedServer + 1 : 0;
            for (int i = catList.length - 1; i >= 0; i--) {
                insert(new NavigationItem(catList[i]), startIndex);
            }
        }

        public void cleanCategories() {
            if (mServerCount > 1) {
                int toBeRemoved = getCount() - mServerCount;
                for (int i = 0; i < toBeRemoved; i++) {
                    remove(getItem(mSelectedServer + 1));
                }
            } else {
                clear();
            }
        }

        public int getServerIndex(int position) {
            return mServerCount == 0 ? 0 : (position <= mSelectedServer ? position : position - getCount() + mServerCount);
        }

        public int findCategoryById(long catId) {
            for (int i = mSelectedServer + 1; i < getCount() && getItem(i).isCategory(); i++) {
                if (getItem(i).getCategory().getId() == catId) return i;
            }
            return -1;
        }

    }

    static class NavViewHolder {
        @InjectView(R.id.part_nav_server_text) TextView mServerText;
        @InjectView(R.id.part_nav_category_text) TextView mCategoryText;
        public NavViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    static class NavViewDropDownHolder {
        @InjectView(R.id.part_nav_dropdown_text) TextView mNavText;
        @InjectView(R.id.part_nav_dropdown_cat_box) ImageView mNavCatBox;
        public NavViewDropDownHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }


    @Override
    public void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values) {
        String tag = dialog.getTag();
        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.alertDialogEvent: dialog tag " + tag + ", event " + event);
        if (tag != null) {
            if (tag.equals(TAG_DIALOG_NO_SERVER)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK) {
                    Intent settingsActivity = new Intent(AmuleRemoteActivity.this, SettingsActivity.class);
                    startActivity(settingsActivity);
                } else {
                    showPostResumeDialog();
                }
            } else if (tag.equals(TAG_DIALOG_TOOLTIP)) {
                mTooltipHelper.handleDialogClosed(dialog, event, values);
                showPostResumeDialog();
            } else if (tag.equals(TAG_DIALOG_ADD_ED2K)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK && values != null) {
                    String u = values.getString(EditTextDialogFragment.BUNDLE_EDIT_STRING);
                    if (u != null) {
                        if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.alertDialogEvent: ed2k URI provided, scheduling add task");
                        
                        AddEd2kAsyncTask ed2kTask = (AddEd2kAsyncTask) mApp.mECHelper.getNewTask(AddEd2kAsyncTask.class);
                        ed2kTask.setEd2kUrl(u);
                        
                        if (mApp.mECHelper.executeTask(ed2kTask, TaskScheduleMode.QUEUE)) {
                            if (DEBUG) Log.d(TAG, "AmuleRemoteActivity.alertDialogEvent: ed2k URI provided, scheduling refreshDlQueue task");
                            refreshDlQueue(TaskScheduleMode.QUEUE);
                        }
                    }
                }                
            } else {
                showPostResumeDialog();
            }
        }
    }



}