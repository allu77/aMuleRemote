package com.iukonline.android.amuleremote;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Editable;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.AmuleControllerPreferences;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;
import com.iukonline.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.android.amuleremote.echelper.tasks.AddEd2kAsyncTask;
import com.iukonline.android.amuleremote.echelper.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.android.amuleremote.echelper.tasks.GetDlQueueAsyncTask;



public class AmuleControllerActivity extends FragmentActivity  implements ClientStatusWatcher {
    
    private final static int ID_DIALOG_ADD_ED2K            = 3;

    public final static String BUNDLE_PARAM_ERRSTR          = "errstr";
    public final static String BUNDLE_PARAM_URI_TO_HANDLE   = "uri_to_handle";
    
    public final static String NO_URI_TO_HANDLE       = "NO_URI"; 
    
    private AmuleControllerApplication mApp;
    private String mHandleURI;
    
    ImageView mRefresh;
    ProgressBar mProgress; 
    
    String mED2KUrl;
    
    ArrayList <ECPartFile> mDlQueue;

   
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mApp = (AmuleControllerApplication) getApplication();
        setContentView(R.layout.main);    
        
        mRefresh = (ImageView) findViewById(R.id.main_button_refresh_new);
        mRefresh.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { /*updateDlList();*/ } });
        
        mProgress = (ProgressBar) findViewById(R.id.main_refresh);
        
        Intent i = getIntent();
        String a = i.getAction();
        
        mHandleURI = a.equals(Intent.ACTION_VIEW) ? i.getData().toString() : NO_URI_TO_HANDLE; 

    }

    @Override
    protected void onResume() {

        super.onResume();
        
        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        if (mDlQueue == null) mApp.mainNeedsRefresh = true;
        
        if (! mApp.refreshServerSettings()) {
            // No server configured
            Intent settingsActivity = new Intent(this, AmuleControllerPreferences.class);
            startActivity(settingsActivity);
        }
        
        //autoRefreshView();
        
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_opt_refresh:
            // updateDlList();
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
            e1.putLong(AmuleControllerApplication.AC_SETTING_SORT, ECPartFileComparator.AC_SETTING_SORT_FILENAME);
            e1.commit();
            //refreshView();
            return true;
        case R.id.menu_opt_sort_status:
            SharedPreferences.Editor e2 = mApp.mSettings.edit();
            e2.putLong(AmuleControllerApplication.AC_SETTING_SORT, ECPartFileComparator.AC_SETTING_SORT_STATUS);
            e2.commit();
            //refreshView();
            return true;
        case R.id.menu_opt_sort_transfered:
            SharedPreferences.Editor e3 = mApp.mSettings.edit();
            e3.putLong(AmuleControllerApplication.AC_SETTING_SORT, ECPartFileComparator.AC_SETTING_SORT_TRANSFERED);
            e3.commit();
            //refreshView();
            return true;
        case R.id.menu_opt_sort_progress:
            SharedPreferences.Editor e4 = mApp.mSettings.edit();
            e4.putLong(AmuleControllerApplication.AC_SETTING_SORT, ECPartFileComparator.AC_SETTING_SORT_PROGRESS);
            e4.commit();
            //refreshView();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case ID_DIALOG_ADD_ED2K:
            //AlertDialog.Builder ed2kBuilder = new AlertDialog.Builder(this);
            //ed2kBuilder.
            
            //Context mContext = getApplicationContext();
            Dialog addED2KDialog = new Dialog(this);

            addED2KDialog.setContentView(R.layout.amuledl_added2k);
            
            // TODO add title
            addED2KDialog.setTitle("Custom Dialog");
            
            Button bOK = (Button) addED2KDialog.findViewById(R.id.added2k_ok);
            Button bCancel = (Button) addED2KDialog.findViewById(R.id.added2k_cancel);
            
            
            bCancel.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { dismissDialog(ID_DIALOG_ADD_ED2K); } });
            bOK.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { handleAddDialog((View)v.getParent().getParent()  ); } });
            
            
            return addED2KDialog;
        default:
            return super.onCreateDialog(id);
        }
    }
    
    private void handleAddDialog (View dialogView) {   
        EditText et = (EditText) dialogView.findViewById(R.id.added2k_ed2k);
        Editable ed = et.getText();
        String url = ed.toString();
        
        //String url = ((EditText) dialogView.findViewById(R.id.added2k_ed2k)).getText().toString();        
        addED2KURL(url);
    }
    
    private void addED2KURL (String url) {


        AddEd2kAsyncTask addEd2kTask = (AddEd2kAsyncTask) mApp.mECHelper.getNewTask(AddEd2kAsyncTask.class);
        addEd2kTask.setEd2kUrl(url);
        GetDlQueueAsyncTask dlQueueTask = (GetDlQueueAsyncTask) mApp.mECHelper.getNewTask(GetDlQueueAsyncTask.class);

        mApp.mECHelper.executeTask(addEd2kTask, TaskScheduleMode.QUEUE);
        mApp.mECHelper.executeTask(dlQueueTask, TaskScheduleMode.QUEUE);
        
        
    }
    
    @Override
    protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
        switch (id) {
        case ID_DIALOG_ADD_ED2K:
            if (args != null) {
                String uri = args.getString(BUNDLE_PARAM_URI_TO_HANDLE);
                if (uri != null) {
                    ((EditText) dialog.findViewById(R.id.added2k_ed2k)).setText(uri);
                }
            }
            break;
        default:
            super.onPrepareDialog(id, dialog, args);
        }
    }


    @Override
    public int getWatcherId() {
        // TODO Auto-generated method stub
        return AmuleControllerApplication.AMULE_ACTIVITY_ID_MAIN;
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
        if (show) {
            mProgress.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.GONE);
        } else {
            mProgress.setVisibility(View.GONE);
            mRefresh.setVisibility(View.VISIBLE);
        }
    }
    


    
    
}