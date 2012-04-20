package com.iukonline.amule.android.amuleremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;

import com.iukonline.amule.android.amuleremote.echelper.ECHelper;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;

// TODO : Pubblicare license delle varie librerie importate?
// - ActionBarSherlock
// - acra (ha anche una notice)




@ReportsCrashes(formKey = "dFEwUy12NFVEcDJQV09palh1YXB2d0E6MQ",
                mode = ReportingInteractionMode.NOTIFICATION,
                resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
                resNotifTickerText = R.string.crash_notif_ticker_text,
                resNotifTitle = R.string.crash_notif_title,
                resNotifText = R.string.crash_notif_text,
                //resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
                resDialogText = R.string.crash_dialog_text,
                //resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
                resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
                resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)


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
        if (activity == null) mAutoRefresh = false;
        refreshRefreshSettings();
    }
    
    private void startRefresh() {
        if (mAutoRefreshTimer == null) {
            
            if (mAutoRefresh) {
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
    
    UpdateChecker mUpdateChecker;
    

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
        
        if (autoRefresh != mAutoRefresh || autoRefreshInterval != mAutoRefreshInterval) {
            mAutoRefresh = autoRefresh;
            mAutoRefreshInterval = autoRefreshInterval;
            startRefresh();
        }
    }

    @Override
    public void onCreate() {
        
        ACRA.init(this);
        
        super.onCreate();
        
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            mUpdateChecker = new UpdateChecker(pInfo.versionCode);

            //mUpdateChecker = new UpdateChecker(1);
        } catch (NameNotFoundException e) {
        }
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        refreshServerSettings();
        refreshRefreshSettings();
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
    
    public String getReleaseNotes() {
        InputStream is = getResources().openRawResource(R.raw.releasenotes);
        
        char[] buf = new char[2048];
        Reader r;
        StringBuilder s = new StringBuilder();

        try {
            r = new InputStreamReader(is, "UTF-8");
            while (true) {
                int n = r.read(buf);
                if (n < 0)
                  break;
                s.append(buf, 0, n);
            }
        } catch (UnsupportedEncodingException e) {
        } catch (IOException e) {
        }
        return s.toString();
    }
    

}
