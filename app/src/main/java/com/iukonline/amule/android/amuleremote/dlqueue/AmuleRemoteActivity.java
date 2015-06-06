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
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iukonline.amule.android.amuleremote.AboutDialogFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.AmuleControllerPreferences;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.dlqueue.DlQueueFragment.DlQueueFragmentContainer;
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

import java.util.ArrayList;
import java.util.Arrays;


public class AmuleRemoteActivity extends ActionBarActivity implements AlertDialogListener, ClientStatusWatcher, DlQueueFragmentContainer, ECStatsWatcher, CategoriesWatcher, RefreshingActivity, UpdatesWatcher  {
    
    
    public final static String BUNDLE_PARAM_ERRSTR          = "errstr";
    public final static String BUNDLE_PARAM_URI_TO_HANDLE   = "uri_to_handle";
    
    private final static String BUNDLE_CATEGORY_FILTER = "category";
    
    public final static String NO_URI_TO_HANDLE       = "NO_URI";
    
    private final static String TAG_DIALOG_NO_SERVER = "dialog_no_server";
    private final static String TAG_DIALOG_ADD_ED2K = "dialog_add_ed2k";
    private final static String TAG_DIALOG_TOOLTIP = "dialog_tooltip";
    
    private AmuleControllerApplication mApp;
    private String mHandleURI;
    private CategoriesAdapter mCategoriesAdapter;
    private long mCatId = ECCategory.NEW_CATEGORY_ID;
    
    String mED2KUrl;
    
    private boolean mIsProgressShown = false;
    private boolean mServerConfigured = false;
    
    MenuItem refreshItem;
    MenuItem addEd2kItem;
    
    MenuItem debugOptionsItem;
    
    TextView mTextDlRate;
    TextView mTextUlRate;
    TextView mTextEDonkeyStatus;
    TextView mTextKADStatus;
    View mViewConnBar;
    
    ActionBar mActionBar;
    
    FragmentManager mFragManager;
    TooltipHelper mTooltipHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getApplication();
        mApp.refreshDebugSettings();
        mTooltipHelper = new TooltipHelper(mApp.mSettings);
        
        mFragManager = getSupportFragmentManager();
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreate: Calling super");
        super.onCreate(savedInstanceState);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreate: Back from super");
        
        if (savedInstanceState != null) {
            mCatId = savedInstanceState.getLong(BUNDLE_CATEGORY_FILTER, ECCategory.NEW_CATEGORY_ID);
        }
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreate: Calling setContentView");
        setContentView(R.layout.main);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreate: back from setContentView");

        
        mTextDlRate = (TextView) findViewById(R.id.main_dl_rate);
        mTextUlRate = (TextView) findViewById(R.id.main_ul_rate);
        mTextEDonkeyStatus = (TextView) findViewById(R.id.main_edonkey_status);
        mTextKADStatus = (TextView) findViewById(R.id.main_kad_status);
        mViewConnBar = findViewById(R.id.main_conn_bar);
        
        mActionBar = getSupportActionBar();

        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        adView.loadAd(adRequest);
        
        Intent i = getIntent();
        String a = i.getAction();
        
        mHandleURI = (a != null && a.equals(Intent.ACTION_VIEW)) ? i.getData().toString() : NO_URI_TO_HANDLE; 

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreate: end");

    }
    

    @Override
    protected void onResume() {

        mApp.mOnTopActivity = this;

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: Reading settings");
        mApp.refreshDebugSettings();
        mServerConfigured = mApp.refreshServerSettings();

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: Calling super");
        super.onResume();
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: Back from super");
        

        // TBV: Everyting that creats a dialog should be in onPostResume due to a bug in ICS
        // https://code.google.com/p/android/issues/detail?id=23096
        
        // TBV: This should clear the disappearing refresh bug. Not elegant as onCreateMenu gets called twice...
        supportInvalidateOptionsMenu();

        
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: registering for async activities");
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECStats(mApp.mECHelper.registerForECStatsUpdates(this));
        updateCategories(mApp.mECHelper.registerForCategoriesUpdates(this));
        
        mApp.registerRefreshActivity(this);

        
        
        if (! mApp.mECHelper.isDlQueueValid()) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: launching refreshDlQueue");
            refreshDlQueue();
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: back from refreshDlQueue");
        }

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: end");
        
    }
    
    
    
    @Override
    protected void onPostResume() {
        
        // Everyting that creates a dialog should be in onPostResume
        // https://code.google.com/p/android/issues/detail?id=23096
        super.onPostResume();
        showPostResumeDialog();
        
        /*
        if (! mServerConfigured) {
            if (mFragManager.findFragmentByTag(TAG_DIALOG_NO_SERVER) == null) {
                AlertDialogFragment d = new AlertDialogFragment(R.string.dlqueue_dialog_title_no_server_configured, R.string.dlqueue_dialog_message_no_server_configured, true);
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: no server configured - showing dialog");
                d.show(mFragManager, TAG_DIALOG_NO_SERVER);
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: no server configured - end");
            } else {
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onResume: no server configured - dialog already shown");
            }
            return;
        }
        
        mApp.showWhatsNew(mFragManager);
        mApp.mUpdateChecker.registerUpdatesWatcher(this);        

        if (! mHandleURI.equals(NO_URI_TO_HANDLE)) {
            String parURI = new String(mHandleURI);
            mHandleURI = new String(NO_URI_TO_HANDLE);
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPostResume: handling ed2k URI");
            showAddED2KDialog(parURI);
        }
        */
    }
    
    protected void showPostResumeDialog() {
        if (mApp.showWhatsNew(mFragManager)) return;
        
        // SHOW TOOLTIPS
        
        if (! mServerConfigured) {
            if (mFragManager.findFragmentByTag(TAG_DIALOG_NO_SERVER) == null) {
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.showPostResumeDialog: no server configured - showing dialog");
                AlertDialogFragment d = new AlertDialogFragment(R.string.dlqueue_dialog_title_no_server_configured, R.string.dlqueue_dialog_message_no_server_configured, true);
                d.show(mFragManager, TAG_DIALOG_NO_SERVER);
            }
            return;
        }

        if (! mHandleURI.equals(NO_URI_TO_HANDLE)) {
            String parURI = new String(mHandleURI);
            mHandleURI = new String(NO_URI_TO_HANDLE);
            if (mApp.mSettings.getBoolean(AmuleControllerApplication.AC_SETTING_CONVERT_PIPE, true)) {
                if (parURI.toLowerCase().startsWith("ed2k://%7c")) {
                    Log.d(AmuleControllerApplication.AC_LOGTAG, "DA CONVERTIRE");
                    parURI = parURI.replaceAll("%7C", "|").replaceAll("%7c", "|");
                }
            }
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPostResume: handling ed2k URI");
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

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPause: un-registering from async activities");

        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        mApp.mECHelper.unRegisterFromECStatsUpdates(this);
        mApp.mECHelper.unRegisterFromCategoriesUpdates(this);

        mApp.registerRefreshActivity(null);
        mApp.mUpdateChecker.registerUpdatesWatcher(null);

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPause: calling super");
        super.onPause();
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPause: end");

        mApp.mOnTopActivity = null;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onSaveInstanceState: adding data to bundle");
        outState.putLong(BUNDLE_CATEGORY_FILTER, mCatId);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onSaveInstanceState: calling super");
        super.onSaveInstanceState(outState);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onSaveInstanceState: end");

    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreateOptionsMenu: Inflating menu");

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_options, menu);

        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreateOptionsMenu: Saving MenuItems");
        
        refreshItem = menu.findItem(R.id.menu_opt_refresh);
        //if (refreshItem == null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreateOptionsMenu: refreshItem is null");
        addEd2kItem = menu.findItem(R.id.menu_opt_added2k);
        //if (addEd2kItem == null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreateOptionsMenu: addEd2kItem is null");
        
        debugOptionsItem = menu.findItem(R.id.menu_opt_debug);
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreateOptionsMenu: Calling super");
        boolean superRet = super.onCreateOptionsMenu(menu);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onCreateOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPrepareOptionsMenu: Setting items visibility");
        
        if (refreshItem != null) {
            refreshItem.setVisible(mServerConfigured);
            if (mIsProgressShown) {
                MenuItemCompat.setActionView(refreshItem, R.layout.refresh_progress);
                //refreshItem.setActionView(R.layout.refresh_progress);
            } else {
                MenuItemCompat.setActionView(refreshItem, null);
                //refreshItem.setActionView(null);
            }
        }

        addEd2kItem.setVisible(mServerConfigured);
        
        if (mApp != null) {
            if (debugOptionsItem != null) debugOptionsItem.setVisible(mApp.enableDebugOptions);
        }

        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPrepareOptionsMenu: calling super");
        boolean superRet = super.onPrepareOptionsMenu(menu);
        if (mApp != null && mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onPrepareOptionsMenu: super returned " + superRet + " - end");
        return superRet;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_opt_refresh:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_refresh selected");
            refreshDlQueue(TaskScheduleMode.QUEUE);
            return true;
        case R.id.menu_opt_refresh_cat:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_refresh_cat selected");
            refreshCategories(TaskScheduleMode.QUEUE);
            return true;
        case R.id.menu_opt_settings:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_settings selected");
            //Intent settingsActivity = new Intent(this, AmuleControllerPreferences.class);
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true; 
        case R.id.menu_opt_reset:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_reset selected");
            mApp.mECHelper.resetClient();
            return true;
        case R.id.menu_opt_added2k:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_added2k selected");
            showAddED2KDialog(null);
            return true;
        case R.id.menu_opt_about:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_about selected");
            showAboutDialog();
            return true;
        case R.id.menu_opt_send_report:
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: menu_opt_send_report selected");
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
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.onOptionsItemSelected: Unknown item selected. Calling super");
            return super.onOptionsItemSelected(item);
        }
    }

    
    public void refreshDlQueue()  {
        refreshDlQueue(TaskScheduleMode.BEST_EFFORT);
    }

    public void refreshDlQueue(TaskScheduleMode mode)  {

        if (mApp.mECHelper.getCategories() == null) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.refreshDlQueue: Category list null, scheduling refresh");
            if (!refreshCategories(mode)) return;
            mode = TaskScheduleMode.QUEUE;
        }
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.refreshDlQueue: Scheduling Get Stats Task");
        if (mApp.mECHelper.executeTask(mApp.mECHelper.getNewTask(GetECStatsAsyncTask.class), mode)) {
            GetDlQueueAsyncTask t = (GetDlQueueAsyncTask) mApp.mECHelper.getNewTask(GetDlQueueAsyncTask.class);
            t.setDlQueue(mApp.mECHelper.getDlQueue());
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.refreshDlQueue: Scheduling GetDlQueue Task");
            mApp.mECHelper.executeTask(t, TaskScheduleMode.QUEUE);
        }
    }
    
    public boolean refreshCategories() {
        return refreshCategories(TaskScheduleMode.BEST_EFFORT);
    }
    
    public boolean refreshCategories(TaskScheduleMode mode) {
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.refreshCategories: Scheduling GetGategories Task");
        return mApp.mECHelper.executeTask(mApp.mECHelper.getNewTask(GetCategoriesAsyncTask.class), mode);
    }
    
    public void showAddED2KDialog(String url) {
        
        if (mFragManager.findFragmentByTag(TAG_DIALOG_ADD_ED2K) == null) {
            EditTextDialogFragment d = new EditTextDialogFragment(R.string.dialog_added2k_title, url);
    
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.showAddED2KDialog: showing dialog");
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
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.showAboutDialog: showing dialog");
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
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.partFileSelected: Partfile " + ECUtils.byteArrayToHexString(hash) + " selected, starting PartFileActivity");

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
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.updateECStats: Updating Stats");
            
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
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.updateECStats: Stats updated");
        } else {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.updateECStats: Hiding connection bar");
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
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.updateCategories: Hiding category list");
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            return;
        }
        
        // TODO: Optimize adapter (update List and do not re-create each time)
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.updateCategories: Updating Categories");
        if (mCategoriesAdapter == null) {
            ArrayList <ECCategory> catList = new ArrayList<ECCategory>(newCategoryList.length + 1);
            catList.add(new ECCategory(getResources().getString(R.string.cat_all_files), null, null, (byte) 0, (byte) 0));
            catList.addAll(Arrays.asList(newCategoryList));
            
            mCategoriesAdapter = new CategoriesAdapter(this, R.layout.dlqueue_fragment, catList);
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            mActionBar.setListNavigationCallbacks(
                            mCategoriesAdapter,
                            new ActionBar.OnNavigationListener() {
                                @Override
                                public boolean onNavigationItemSelected(int position, long itemId) {
                                    DlQueueFragment f = (DlQueueFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_dlqueue);
                                    mCatId = itemId;
                                    if (f != null) {
                                        f.filterDlQueueByCategory(itemId);
                                    }
                                    return true;
                                }
                            }
            );
        } else {
            mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            int selectedIndex = getSupportActionBar().getSelectedNavigationIndex();
            mCatId = ECCategory.NEW_CATEGORY_ID;
            if (selectedIndex >= 0 && selectedIndex < mCategoriesAdapter.getCount()) {
                mCatId = mCategoriesAdapter.getItemId(selectedIndex);
            }
            while (mCategoriesAdapter.getCount() > 1) {
                mCategoriesAdapter.remove(mCategoriesAdapter.getItem(mCategoriesAdapter.getCount() - 1));
            }
            for (int i = 0; i < newCategoryList.length; i++) mCategoriesAdapter.add(newCategoryList[i]);
        }
        
        for (int i = 0; i < mCategoriesAdapter.getCount(); i++) {
            if (mCategoriesAdapter.getItemId(i) == mCatId) {
                mActionBar.setSelectedNavigationItem(i);
                break;
            }
        }
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.updateCategories: Categories udpated");
        
    }
    
    
    private class CategoriesAdapter extends ArrayAdapter<ECCategory> {
        
        public CategoriesAdapter(Context context, int textViewResourceId, ArrayList<ECCategory> objects) {
            super(context, textViewResourceId, objects);
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            
            //TODO: handle convertView
            
            TextView v = new TextView(getContext());
            ECCategory c = getItem(position);
            if (c.getId() == 0L) {
                v.setText(R.string.category_uncategorized);
            } else {
                v.setText(c.getTitle());
            }
            return v;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            //TODO: handle convertView
            
            //TODO: this is quick and dirty. 
            TextView v = (TextView) getView(position, convertView, parent);
            
            float d = getContext().getResources().getDisplayMetrics().density;
            v.setMinHeight((int) (30 * d));
            v.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout l = new LinearLayout(getContext());
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            
            p.setMargins((int) (20 * d), (int) (5 * d), (int) (10 * d), (int) (5 * d));
            l.addView(v,  p);
            
            return l;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }


    @Override
    public void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values) {
        String tag = dialog.getTag();
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.alertDialogEvent: dialog tag " + tag + ", event " + event);
        if (tag != null) {
            if (tag.equals(TAG_DIALOG_NO_SERVER)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK) {
                    Intent settingsActivity = new Intent(AmuleRemoteActivity.this, AmuleControllerPreferences.class);
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
                        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.alertDialogEvent: ed2k URI provided, scheduling add task");
                        
                        AddEd2kAsyncTask ed2kTask = (AddEd2kAsyncTask) mApp.mECHelper.getNewTask(AddEd2kAsyncTask.class);
                        ed2kTask.setEd2kUrl(u);
                        
                        if (mApp.mECHelper.executeTask(ed2kTask, TaskScheduleMode.QUEUE)) {
                            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "AmuleRemoteActivity.alertDialogEvent: ed2k URI provided, scheduling refreshDlQueue task");
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