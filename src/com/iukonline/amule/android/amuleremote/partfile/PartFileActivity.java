package com.iukonline.amule.android.amuleremote.partfile;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication.RefreshingActivity;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileActionWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileActionAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileActionAsyncTask.ECPartFileAction;
import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileGetDetailsAsyncTask;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.AlertDialogFragment.AlertDialogListener;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.EditTextDialogFragment;
import com.iukonline.amule.android.amuleremote.partfile.PartFileSourceNamesFragment.RenameDialogContainer;
import com.iukonline.amule.ec.ECPartFile;


public class PartFileActivity extends SherlockFragmentActivity implements AlertDialogListener, ClientStatusWatcher, ECPartFileWatcher, ECPartFileActionWatcher, RenameDialogContainer, RefreshingActivity {
    public final static String BUNDLE_PARAM_HASH = "hash";
    public static String BUNDLE_SELECTED_TAB = "tab";
    public final static String BUNDLE_NEEDS_REFRESH = "needs_refresh";
    
    private final static String TAG_DIALOG_RENAME = "rename_dialog";
    private final static String TAG_DIALOG_DELETE = "delete_dialog";

    
    private AmuleControllerApplication mApp;
    
    byte[] mHash;
    ECPartFile mPartFile;
    
    ActionBar mBar;
    ActionBar.Tab mTabDetails;
    ActionBar.Tab mTabSourceNames;
    ActionBar.Tab mTabComments;
    
    Button bPause;
    Button bResume;
    Button bDelete;
    
    private boolean mIsProgressShown = false;
    private boolean mNeedsRefresh = true;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mApp = (AmuleControllerApplication) getApplication();
        this.setContentView(R.layout.partfile_activity);
        mHash = getIntent().getExtras().getByteArray(BUNDLE_PARAM_HASH);
        
        mBar = getSupportActionBar();
        mBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        
        mTabDetails = mBar.newTab();
        mTabDetails.setText("Details"); //TODO Provide string resource
        mTabDetails.setTabListener(new TabListener<PartFileDetailsFragment>(this, "details", PartFileDetailsFragment.class, mHash));
        
        mBar.addTab(mTabDetails);
        
        mTabSourceNames = mBar.newTab();
        mTabSourceNames.setText("Sources"); //TODO Provide string resource
        mTabSourceNames.setTabListener(new TabListener<PartFileSourceNamesFragment>(this, "names", PartFileSourceNamesFragment.class, mHash));
        mBar.addTab(mTabSourceNames);
        
        mTabComments = mBar.newTab();
        mTabComments.setText("Comments"); //TODO Provide string resource
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
        
        bPause = (Button) findViewById(R.id.partfile_button_pause);
        bResume = (Button) findViewById(R.id.partfile_button_resume);
        bDelete = (Button) findViewById(R.id.partfile_button_delete);
        
        
        bPause.setOnClickListener(new OnClickListener() { public void onClick(View v) { doPartFileAction(ECPartFileAction.PAUSE); } });
        bResume.setOnClickListener(new OnClickListener() { public void onClick(View v) { doPartFileAction(ECPartFileAction.RESUME); } });
        
        
        bDelete.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { showDeleteConfirmDialog(); } });
        

    }

    @Override
    protected void onResume() {
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
    }
    
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if (mBar != null) {
            Tab selectedTab = mBar.getSelectedTab();
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

    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.detail_options, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        
        MenuItem refreshItem = menu.findItem(R.id.menu_detail_opt_refresh);
        
        if (refreshItem != null) {
            if (mIsProgressShown) {
                refreshItem.setActionView(R.layout.refresh_progress);
            } else {
                refreshItem.setActionView(null);
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
            
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    
    public void showRenameDialog(String fileName) {
        EditTextDialogFragment d = new EditTextDialogFragment(R.string.dialog_rename_partfile, fileName);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "PartFileActivity.showRenameDialog: showing dialog");
        d.show(getSupportFragmentManager(), TAG_DIALOG_RENAME);
    }
    
    
    private void showDeleteConfirmDialog() {
        
        AlertDialogFragment d = new AlertDialogFragment(R.string.partfile_dialog_delete_confirm, true);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "PartFileActivity.showRenameDialog: showing dialog");
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
        
        boolean pauseEnabled = false;
        boolean resumeEnabled = false;
        boolean deleteEnabled = false;

        if (mPartFile != null) {
            
            mBar.setTitle(mPartFile.getFileName());
            
            if (mPartFile.getCommentCount() == 0) {
                if (mBar.getTabCount() == 3) {
                    Tab prevSelected = mBar.getSelectedTab();
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
                pauseEnabled = true;
                deleteEnabled = true;
                break;
            case ECPartFile.PS_ERROR:
                deleteEnabled = true;
                break;
            case ECPartFile.PS_HASHING:
                deleteEnabled = true;
                break;
            case ECPartFile.PS_PAUSED:
                deleteEnabled = true;
                resumeEnabled = true;
                break;
            case ECPartFile.PS_READY:
                deleteEnabled = true;
                pauseEnabled = true;
                break;
            case ECPartFile.PS_WAITINGFORHASH:
                deleteEnabled = true;
                break;
            }
        }

        bPause.setEnabled(pauseEnabled);
        bResume.setEnabled(resumeEnabled);
        bDelete.setEnabled(deleteEnabled);

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
                    if (mApp.enableLog) Log.e(AmuleControllerApplication.AC_LOGTAG, "Got a different hash in updateECPartFile!");
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

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            
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

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
            if (mFragment != null) {
                // Detach the fragment, because another one is being attached
                FragmentTransaction realFT = getFT(ft);
                realFT.detach(mFragment);
                if (ft == null) realFT.commit();
            }
        }

        @Override
        public void onTabReselected(Tab tab, FragmentTransaction ft) {
            // Do nothing
        }
    }


 
    @Override
    public void alertDialogEvent(AlertDialogFragment dialog, int event, Bundle values) {
        String tag = dialog.getTag();
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "PartFileActivity.alertDialogEvent: dialog tag " + tag + ", event " + event);
        if (tag != null) {
            if (tag.equals(TAG_DIALOG_RENAME)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK && values != null) {
                    String newName = values.getString(EditTextDialogFragment.BUNDLE_EDIT_STRING);
                    if (newName != null) {
                        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "PartFileActivity.alertDialogEvent: Launching rename action with name " + EditTextDialogFragment.BUNDLE_EDIT_STRING);
                        doPartFileAction(ECPartFileAction.RENAME, true, newName);
                    }
                }
            } else if (tag.equals(TAG_DIALOG_DELETE)) {
                if (event == AlertDialogFragment.ALERTDIALOG_EVENT_OK) {
                    if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "PartFileActivity.alertDialogEvent: delete confirmed");
                    doPartFileAction(ECPartFileAction.DELETE, false);
                }
            }
        }
    }
}
