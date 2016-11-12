/*
 * Copyright (c) 2015. Gianluca Vegetti
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.iukonline.amule.android.amuleremote.partfile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileActionWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.ECCategoryParcelable;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileActionAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileActionAsyncTask.ECPartFileAction;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileGetDetailsAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment.AlertDialogListener;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.CategoryListDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.EditTextDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.ListDialogFragment;
import com.iukonline.amule.android.amuleremote.partfile.PartFileSourceNamesFragment.RenameDialogContainer;
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECPartFile;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class PartFileActivity extends AppCompatActivity implements AlertDialogListener, ClientStatusWatcher, ECPartFileWatcher, ECPartFileActionWatcher, RenameDialogContainer, RefreshingActivity {
    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
    public final static String BUNDLE_PARAM_HASH = "hash";
    public static String BUNDLE_SELECTED_TAB = "tab";
    public final static String BUNDLE_NEEDS_REFRESH = "needs_refresh";
    
    private final static String TAG_DIALOG_RENAME = "rename_dialog";
    private final static String TAG_DIALOG_DELETE = "delete_dialog";
    private final static String TAG_DIALOG_CATEGORY = "category_dialog";

    
    private AmuleRemoteApplication mApp;
    
    byte[] mHash = null;
    ECPartFile mPartFile;
    
    ActionBar mBar;
    ActionBar.Tab mTabDetails;
    ActionBar.Tab mTabSourceNames;
    ActionBar.Tab mTabComments;

    @InjectView(R.id.partfile_fab) FloatingActionButton mFab;

    private boolean mIsProgressShown = false;
    private boolean mNeedsRefresh = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApp = (AmuleRemoteApplication) getApplication();
        this.setContentView(R.layout.act_partfile);
        
        Intent i = getIntent();
        if (i != null) {
            Bundle b = i.getExtras();
            if (b != null) {
                mHash = b.getByteArray(BUNDLE_PARAM_HASH);
            }
        }
        
        mBar = getSupportActionBar();
        mBar.setDisplayHomeAsUpEnabled(true);
        mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        
        mTabDetails = mBar.newTab();
        mTabDetails.setText(R.string.partfile_tab_details);
        mTabDetails.setTabListener(new TabListener<PartFileDetailsFragment>(this, "details", PartFileDetailsFragment.class, mHash));
        
        mBar.addTab(mTabDetails);
        
        mTabSourceNames = mBar.newTab();
        mTabSourceNames.setText(R.string.partfile_tab_sources);
        mTabSourceNames.setTabListener(new TabListener<PartFileSourceNamesFragment>(this, "names", PartFileSourceNamesFragment.class, mHash));
        mBar.addTab(mTabSourceNames);
        
        mTabComments = mBar.newTab();
        mTabComments.setText(R.string.partfile_tab_comments); 
        mTabComments.setTabListener(new TabListener<PartFileCommentsFragment>(this, "comments", PartFileCommentsFragment.class, mHash));
        mBar.addTab(mTabComments);

        
        if (savedInstanceState != null) {
            String selectedTab = savedInstanceState.getString(BUNDLE_SELECTED_TAB);
            if (selectedTab == null) {
                mBar.selectTab(mTabDetails);
            } else if (selectedTab.equals("names")) {
                mBar.selectTab(mTabSourceNames);
            } else if (selectedTab.equals("comments")) {
                if (mBar.getTabCount() == 3) {
                    mBar.selectTab(mTabComments);
                }
            }
            mNeedsRefresh = savedInstanceState.getBoolean(BUNDLE_NEEDS_REFRESH, true);
        }


        ButterKnife.inject(this);
        mFab.setTag("PLAY");

        mFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mFab.getTag().equals("PAUSE")) {
                    if (DEBUG) Log.d(TAG, "PartFileActivity.mFab.onClick: Pausing parftile");
                    doPartFileAction(ECPartFileAction.PAUSE);
                } else {
                    if (DEBUG) Log.d(TAG, "PartFileActivity.mFab.onClick: Resuming parftile");
                    doPartFileAction(ECPartFileAction.RESUME);
                }
            }
        });
        
        
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("TEST_DEVICE_ID")
                .build();
        adView.loadAd(adRequest);


    }

    @Override
    protected void onResume() {
        mApp.mOnTopActivity = this;
        super.onResume();
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECPartFile(mApp.mECHelper.registerForECPartFileUpdates(this, mHash));
        mApp.registerRefreshActivity(this);
        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        mApp.mECHelper.unRegisterFromECPartFileUpdates(this, mHash);
        mApp.mECHelper.unRegisterFromECPartFileActions(this, mHash);
        mApp.registerRefreshActivity(null);
        mApp.mOnTopActivity = null;
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mBar != null) {
            ActionBar.Tab selectedTab = mBar.getSelectedTab();
            if (selectedTab != null) {
                if (selectedTab == mTabSourceNames) {
                    outState.putString(BUNDLE_SELECTED_TAB, "names");
                } else if (selectedTab == mTabComments) {
                    outState.putString(BUNDLE_SELECTED_TAB, "comments");
                }
            }
        }
        
        outState.putBoolean(BUNDLE_NEEDS_REFRESH, mNeedsRefresh);
    }

    @OnClick(R.id.partfile_fab)
    public void onFabPressed() {
        if (mFab.getTag().equals("PAUSE")) {
            if (DEBUG) Log.d(TAG, "PartFileActivity.mFab.onClick: Pausing parftile");
            doPartFileAction(ECPartFileAction.PAUSE);
        } else {
            if (DEBUG) Log.d(TAG, "PartFileActivity.mFab.onClick: Resuming parftile");
            doPartFileAction(ECPartFileAction.RESUME);
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_options, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        MenuItem refreshItem = menu.findItem(R.id.menu_detail_opt_refresh);
        
        if (refreshItem != null) {
            if (mIsProgressShown) {
                MenuItemCompat.setActionView(refreshItem, R.layout.part_refresh_progress);
                //refreshItem.setActionView(R.layout.part_refresh_progress);
            } else {
                MenuItemCompat.setActionView(refreshItem, null);
                //refreshItem.setActionView(null);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_detail_opt_refresh:
            refreshPartFileDetails();
            return true;
        case R.id.menu_detail_opt_a4af_auto:
            doPartFileAction(ECPartFileAction.A4AF_AUTO);
            return true;
        case R.id.menu_detail_opt_a4af_now:
            doPartFileAction(ECPartFileAction.A4AF_NOW);
            return true;
        case R.id.menu_detail_opt_a4af_away:
            doPartFileAction(ECPartFileAction.A4AF_AWAY);
            return true;
        case R.id.menu_detail_opt_prio_auto:
            doPartFileAction(ECPartFileAction.PRIO_AUTO);
            return true;
        case R.id.menu_detail_opt_prio_low:
            doPartFileAction(ECPartFileAction.PRIO_LOW);
            return true;
        case R.id.menu_detail_opt_prio_normal:
            doPartFileAction(ECPartFileAction.PRIO_NORMAL);
            return true;
        case R.id.menu_detail_opt_prio_high:
            doPartFileAction(ECPartFileAction.PRIO_HIGH);
            return true;
        case R.id.menu_detail_opt_rename:
            showRenameDialog(mPartFile.getFileName());
            return true;
        case R.id.menu_detail_opt_delete:
            showDeleteConfirmDialog();
            return true;
        case R.id.menu_detail_opt_category:
            showChangeCategoryDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    public void showChangeCategoryDialog() {
        CategoryListDialogFragment d = CategoryListDialogFragment.newInstance(R.string.dialog_category_title, ECCategoryParcelable.convertArray(mApp.mECHelper.getCategories()));
        if (DEBUG) Log.d(TAG, "PartFileActivity.showChangeCategoryDialog: showing dialog");

        if (DEBUG) {
            ECCategory[] tmpCat = mApp.mECHelper.getCategories();
            Log.d(TAG, "PartFileActivity.showChangeCategoryDialog: Categories:");
            for (int i = 0; i < tmpCat.length; i++) {
                Log.d(TAG, "PartFileActivity.showChangeCategoryDialog: " + tmpCat[i].getTitle());
            }
            ECCategoryParcelable[] tmpParc = ECCategoryParcelable.convertArray(tmpCat);
            Log.d(TAG, "PartFileActivity.showChangeCategoryDialog: Parcelable:");
            for (int i = 0; i < tmpParc.length; i++) {
                Log.d(TAG, "PartFileActivity.showChangeCategoryDialog: " + tmpParc[i].toString());
            }

        }

        d.show(getSupportFragmentManager(), TAG_DIALOG_CATEGORY);
    }
    
    
    public void showRenameDialog(String fileName) {
        EditTextDialogFragment d = EditTextDialogFragment.newInstance(R.string.dialog_rename_partfile, fileName);
        if (DEBUG) Log.d(TAG, "PartFileActivity.showRenameDialog: showing dialog");
        d.show(getSupportFragmentManager(), TAG_DIALOG_RENAME);
    }
    
    
    private void showDeleteConfirmDialog() {
        
        AlertDialogFragment d = AlertDialogFragment.newInstance(R.string.partfile_dialog_delete_confirm, true);
        if (DEBUG) Log.d(TAG, "PartFileActivity.showRenameDialog: showing dialog");
        d.show(getSupportFragmentManager(), TAG_DIALOG_DELETE);
    }
    
    
    
    private void refreshPartFileDetails() {
        ECPartFileGetDetailsAsyncTask refreshTask = (ECPartFileGetDetailsAsyncTask) mApp.mECHelper.getNewTask(ECPartFileGetDetailsAsyncTask.class);
        refreshTask.setECPartFile(mPartFile);
        if (mApp.mECHelper.executeTask(refreshTask, TaskScheduleMode.BEST_EFFORT)) {
            // Refresh is queued...
            mNeedsRefresh = false;
        }
            
    }
    
    
    private void doPartFileAction(ECPartFileAction actionType) {
        doPartFileAction(actionType, true);
    }
    
    private void doPartFileAction(ECPartFileAction actionType, boolean refreshAfter) {
        doPartFileAction(actionType, refreshAfter, null);
        
    }
    
    private void doPartFileAction(ECPartFileAction actionType, boolean refreshAfter, String stringParam ) {
        if (DEBUG) Log.d(TAG, "PartFileActivity.doParcFileAction: Executing action " + actionType + " with parameter " + stringParam);
        ECPartFileActionAsyncTask actionTask = (ECPartFileActionAsyncTask) mApp.mECHelper.getNewTask(ECPartFileActionAsyncTask.class);
        actionTask.setECPartFile(mPartFile).setAction(actionType);
        if (stringParam != null) actionTask.setStringParam(stringParam);
        
        mApp.mECHelper.registerForECPartFileActions(this, mHash);
        if (mApp.mECHelper.executeTask(actionTask, TaskScheduleMode.QUEUE) && refreshAfter) {
            ECPartFileGetDetailsAsyncTask detailsTask = (ECPartFileGetDetailsAsyncTask) mApp.mECHelper.getNewTask(ECPartFileGetDetailsAsyncTask.class);
            detailsTask.setECPartFile(mPartFile);
            mApp.mECHelper.executeTask(detailsTask, TaskScheduleMode.QUEUE);
        }
    }

    
    
    private void refreshView() {
        
        if (mPartFile != null) {

            mBar.setTitle(mPartFile.getFileName());
            
            if (mPartFile.getCommentCount() == 0) {
                if (mBar.getTabCount() == 3) {
                    ActionBar.Tab prevSelected = mBar.getSelectedTab();
                    mBar.removeTab(mTabComments);
                    if (prevSelected == mTabComments) prevSelected = mTabDetails;
                    mBar.selectTab(prevSelected);
                }
            } else {
                if (mBar.getTabCount() == 2) {
                    mBar.addTab(mTabComments);
                }
            }


            switch (mPartFile.getStatus()) {
            case ECPartFile.PS_EMPTY:
            case ECPartFile.PS_READY:
                mFab.setTag("PAUSE");
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause_white_24dp));
                mFab.setVisibility(View.VISIBLE);
                break;
            case ECPartFile.PS_PAUSED:
                mFab.setTag("PLAY");
                mFab.setImageDrawable(getResources().getDrawable(R.drawable.ic_play_arrow_white_24dp));
                mFab.setVisibility(View.VISIBLE);
                break;
            default:
                mFab.setVisibility(View.GONE);
                break;
            }

        } else {
            mFab.setVisibility(View.GONE);
        }
    }
    
    public void showProgress(boolean show) {
        if (show == mIsProgressShown) return;
        
        mIsProgressShown = show;
        invalidateOptionsMenu();
        
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
    

    // ECPartFileWatcher Interface
    

    @Override
    public void updateECPartFile(ECPartFile newECPartFile) {
        if (newECPartFile != null) {
            if (mPartFile == null) {
                mPartFile = newECPartFile;
                if (mNeedsRefresh) refreshPartFileDetails();
            } else {
                if (! mPartFile.getHashAsString().equals(newECPartFile.getHashAsString())) {
                    Toast.makeText(mApp, R.string.error_unexpected, Toast.LENGTH_LONG).show();
                    if (DEBUG) Log.e(TAG, "Got a different hash in updateECPartFile!");
                    mApp.mECHelper.resetClient();
                }
            }
            refreshView();
        } else {
            finish();
        }
    }

    
    // ECPartFileActionWatcher Interface
    
    @Override
    public void notifyECPartFileActionDone(ECPartFileAction action) {
        mApp.mECHelper.unRegisterFromECPartFileActions(this, mHash);
        if (action == ECPartFileAction.DELETE) {
            mApp.mECHelper.invalidateDlQueue();
            finish();
        }
    }

    
    
    

    



    
    // RefreshingActivity Interface
    
    @Override
    public void refreshContent() {
        refreshPartFileDetails();
    }


    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment mFragment;
        private final Activity mActivity;
        private final String mTag;
        private final Class<T> mClass;
        private final byte[] mHash;


        private FragmentTransaction getFT(FragmentTransaction ft) {
            
            if (ft == null) {
                FragmentManager fragMgr = ((FragmentActivity) mActivity).getSupportFragmentManager();
                return fragMgr.beginTransaction();
            } else {
                return ft;
            }
            
        }

        /** Constructor used each time a new tab is created.
         * @param activity  The host Activity, used to instantiate the fragment
         * @param tag  The identifier tag for the fragment
         * @param clz  The fragment's Class, used to instantiate the fragment
         */
        
        public TabListener(Activity activity, String tag, Class<T> clz, byte[] hash) {
            mActivity = activity;
            mTag = tag;
            mClass = clz;
            mHash = hash;
        }

        /* The following are each of the ActionBar.TabListener callbacks */

        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            
            FragmentTransaction realFT = getFT(ft);
            
            
            // Check if the fragment is already initialized
            if (mFragment == null) {
                // If not, instantiate and add it to the activity
                mFragment = Fragment.instantiate(mActivity, mClass.getName());
                Bundle b = new Bundle();
                b.putByteArray(BUNDLE_PARAM_HASH, mHash);
                mFragment.setArguments(b);
                //realFT.add(R.id.partfile_tabcontent, mFragment, mTag);
                
                // http://stackoverflow.com/questions/9083747/android-ics-actionbar-tabs-orientation-change
                realFT.replace(R.id.partfile_tabcontent, mFragment, mTag);
            } else {
                // If it exists, simply attach it in order to show it
                realFT.attach(mFragment);
            }
            
            if (ft == null) realFT.commit();
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                FragmentTransaction realFT = getFT(ft);
                realFT.detach(mFragment);
                if (ft == null) realFT.commit();
            }
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // Do nothing
        }
    }


 
    @Override
    public void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values) {
        String tag = dialog.getTag();
        if (DEBUG) Log.d(TAG, "PartFileActivity.alertDialogEvent: dialog tag " + tag + ", event " + event);
        if (tag != null) {
            if (tag.equals(TAG_DIALOG_RENAME)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK && values != null) {
                    String newName = values.getString(EditTextDialogFragment.BUNDLE_EDIT_STRING);
                    if (newName != null) {
                        if (DEBUG) Log.d(TAG, "PartFileActivity.alertDialogEvent: Launching rename action with name " + EditTextDialogFragment.BUNDLE_EDIT_STRING);
                        doPartFileAction(ECPartFileAction.RENAME, true, newName);
                    }
                }
            } else if (tag.equals(TAG_DIALOG_DELETE)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK) {
                    if (DEBUG) Log.d(TAG, "PartFileActivity.alertDialogEvent: delete confirmed");
                    doPartFileAction(ECPartFileAction.DELETE, false);
                }
            } else if (tag.equals(TAG_DIALOG_CATEGORY)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK) {
                    if (values != null && values.containsKey(ListDialogFragment.BUNDLE_LIST_SELECTED_PARCELABLE)) {
                        ECCategory selected = ((ECCategoryParcelable) values.getParcelable(ListDialogFragment.BUNDLE_LIST_SELECTED_PARCELABLE)).getECCategory();
                        if (DEBUG)
                            Log.d(TAG, "PartFileActivity.alertDialogEvent: Change to category " + selected);
                        doPartFileAction(ECPartFileAction.SET_CATEGORY, true, "" + selected.getId());
                    }
                }
            }
        }
    }
}
