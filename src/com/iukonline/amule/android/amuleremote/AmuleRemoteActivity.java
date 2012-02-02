package com.iukonline.amule.android.amuleremote;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.android.amuleremote.AmuleClientStatus;
import com.iukonline.android.amuleremote.AmuleControllerPreferences;




public class AmuleRemoteActivity extends FragmentActivity implements ClientStatusWatcher {
    
    
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
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mApp = (AmuleControllerApplication) getApplication();
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
        
        if (! mHandleURI.equals(NO_URI_TO_HANDLE)) {
            Bundle b = new Bundle();
            b.putString(BUNDLE_PARAM_URI_TO_HANDLE, mHandleURI);
            mHandleURI = NO_URI_TO_HANDLE;
            showDialog(ID_DIALOG_ADD_ED2K, b);
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