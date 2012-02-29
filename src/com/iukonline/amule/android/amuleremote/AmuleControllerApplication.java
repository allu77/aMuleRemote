package com.iukonline.amule.android.amuleremote;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.echelper.ECHelper;
import com.iukonline.amule.android.amuleremote.echelper.ECHelperFakeClient;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;


public class AmuleControllerApplication extends Application {
    
    public static final String AC_SHARED_PREFERENCES_NAME = "AmuleController";
    
    public static final String AC_SETTING_HOSTNAME   = "amule_server_host";
    public static final String AC_SETTING_PORT       = "amule_server_port";
    public static final String AC_SETTING_PASSWORD   = "amule_server_password"; 
    
    public static final String AC_SETTING_AUTOREFRESH = "amule_client_autorefresh";
    public static final String AC_SETTING_AUTOREFRESH_INTERVAL = "amule_client_autorefresh_interval";
    
    public static final String AC_SETTING_SORT          = "amule_client_sort";
    public static final byte AC_SETTING_SORT_FILENAME = 0x0;
    public static final byte AC_SETTING_SORT_STATUS = 0x1;
    public static final byte AC_SETTING_SORT_TRANSFERED = 0x2;
    public static final byte AC_SETTING_SORT_PROGRESS = 0x3;
    
    public static final String AC_SETTING_CONNECT_TIMEOUT = "amule_client_connect_timeout";
    public static final String AC_SETTING_READ_TIMEOUT = "amule_client_read_timeout";
    
    interface RefreshingActivity {
        public void refreshContent();
    }
    
    RefreshingActivity mRefreshingActivity;
    private Timer mAutoRefreshTimer;
    private boolean mAutoRefresh;
    private int mAutoRefreshInterval;
    
    public void registerRefreshActivity(RefreshingActivity activity) {
        mRefreshingActivity = activity;
        if (activity != null) {
            refreshRefreshSettings();
        }
    }
    
    private void startRefresh() {
        Toast.makeText(this, "START REFRESH CALLED", Toast.LENGTH_SHORT).show();
        if (mAutoRefreshTimer == null) {
            
            if (mAutoRefresh) {
                
                Toast.makeText(this, "NO TIMER - CREATING NEW", Toast.LENGTH_SHORT).show();
                
                mAutoRefreshTimer = new Timer();
                mAutoRefreshTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (mRefreshingActivity != null) {
                            ((Activity) mRefreshingActivity).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mRefreshingActivity.refreshContent();
                                }
                            });
                        }
                    }
                }, 0, mAutoRefreshInterval * 1000);
            }
        } else {
            Toast.makeText(this, "CANCELLING PREVIOUS TIMER", Toast.LENGTH_SHORT).show();
            
            mAutoRefreshTimer.cancel();
            mAutoRefreshTimer = null;
            if (mAutoRefresh) {
                startRefresh();
            }
        }
    }

    
    
    
    SharedPreferences mSettings;
    ECHelper mECHelper = new ECHelper(this);
    //ECHelper mECHelper = new ECHelperFakeClient(this);
    
    public boolean mainNeedsRefresh = true;
    
    public AmuleControllerApplication() {
       super();
    }

    public boolean refreshServerSettings()  {
        
        String host = mSettings.getString(AC_SETTING_HOSTNAME, "");
        int port = Integer.parseInt(mSettings.getString(AC_SETTING_PORT, "4712"));
        String password = mSettings.getString(AC_SETTING_PASSWORD, "");
        
        int connTimeout = Integer.parseInt(mSettings.getString(AC_SETTING_CONNECT_TIMEOUT, "10")) * 1000;
        int readTimeout = Integer.parseInt(mSettings.getString(AC_SETTING_READ_TIMEOUT, "30")) * 1000;
        
        if (host.length() == 0 || password.length() == 0) {
            return false;
        }
        
        mECHelper.setServerInfo(
                        mSettings.getString(AC_SETTING_HOSTNAME, "example.com"),
                        port,
                        mSettings.getString(AC_SETTING_PASSWORD, "FAKEPASSWORD"),
                        connTimeout,
                        readTimeout
        );
        
        return true;
    }
    
    public void refreshRefreshSettings() {
        boolean autoRefresh = mSettings.getBoolean(AC_SETTING_AUTOREFRESH, false);
        int autoRefreshInterval = Integer.parseInt(mSettings.getString(AC_SETTING_AUTOREFRESH_INTERVAL, "10"));
        
        Toast.makeText(this, "REFRESH CALLED", Toast.LENGTH_SHORT).show();
        if (autoRefresh != mAutoRefresh || autoRefreshInterval != mAutoRefreshInterval) {
            Toast.makeText(this, "Settings changed - " + autoRefresh + " - " + autoRefreshInterval, Toast.LENGTH_SHORT).show();
            mAutoRefresh = autoRefresh;
            mAutoRefreshInterval = autoRefreshInterval;
            startRefresh();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        refreshServerSettings();
        refreshRefreshSettings();
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }
    
    static ECPartFileComparator.ComparatorType getDlComparatorTypeFromSortSetting (byte sortSetting) {
        switch (sortSetting) {
        case AC_SETTING_SORT_FILENAME:
            return ECPartFileComparator.ComparatorType.FILENAME;
        case AC_SETTING_SORT_STATUS:
            return ECPartFileComparator.ComparatorType.STATUS;
        case AC_SETTING_SORT_TRANSFERED:
            return ECPartFileComparator.ComparatorType.TRANSFERED;
        case AC_SETTING_SORT_PROGRESS:
            return ECPartFileComparator.ComparatorType.PROGRESS;
        }
        return null;
    }
    

}
