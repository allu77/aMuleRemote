package com.iukonline.amule.android.amuleremote;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.DropBoxManager;
import android.preference.PreferenceManager;

import com.iukonline.amule.android.amuleremote.echelper.ECHelper;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;


@ReportsCrashes(formKey = "dEcyUXpSdlVIZGd6TWZYRlhaOUhvT3c6MA",
                mode = ReportingInteractionMode.NOTIFICATION,
                customReportContent = { 
                    ReportField.REPORT_ID, 
                    ReportField.PHONE_MODEL, 
                    ReportField.BRAND, 
                    ReportField.PRODUCT, 
                    ReportField.ANDROID_VERSION, 
                    ReportField.DISPLAY, 
                    ReportField.USER_COMMENT, 
                    ReportField.USER_APP_START_DATE, 
                    ReportField.USER_CRASH_DATE, 
                    ReportField.STACK_TRACE, 
                    ReportField.CUSTOM_DATA, 
                    ReportField.DROPBOX, 
                    ReportField.LOGCAT, 
                    ReportField.INITIAL_CONFIGURATION, 
                    ReportField.CRASH_CONFIGURATION 
                },
                logcatArguments = { "-t", "500", "-v", "time", "aMuleRemote:D", "*:S" },
                additionalDropBoxTags = { "aMuleRemote" },
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
    
    public static final String AC_LOGTAG = "aMuleRemote";
    
    public static final String AC_SHARED_PREFERENCES_NAME = "AmuleController";
    
    public static final String AC_SETTING_HOSTNAME   = "amule_server_host";
    public static final String AC_SETTING_PORT       = "amule_server_port";
    public static final String AC_SETTING_PASSWORD   = "amule_server_password";
    public static final String AC_SETTING_VERSION    = "amule_server_version"; 
    
    public static final String AC_SETTING_AUTOREFRESH = "amule_client_autorefresh";
    public static final String AC_SETTING_AUTOREFRESH_INTERVAL = "amule_client_autorefresh_interval";
    
    public static final String AC_SETTING_SORT          = "amule_client_sort";
    
    public static final String AC_SETTING_ENABLE_LOG    = "debug_enable_log";
    public static final String AC_SETTING_ENABLE_EXCEPTIONS = "debug_enable_exceptions";
    public static final String AC_SETTING_ENABLE_DEBUG_OPTIONS = "debug_enable_options";
    
    public static final byte AC_SETTING_SORT_FILENAME = 0x0;
    public static final byte AC_SETTING_SORT_STATUS = 0x1;
    public static final byte AC_SETTING_SORT_TRANSFERED = 0x2;
    public static final byte AC_SETTING_SORT_PROGRESS = 0x3;
    
    public static final byte AC_SETTING_SORT_SIZE = 0x4;
    public static final byte AC_SETTING_SORT_SPEED = 0x5;
    public static final byte AC_SETTING_SORT_PRIORITY = 0x6;
    public static final byte AC_SETTING_SORT_REMAINING = 0x7;
    public static final byte AC_SETTING_SORT_LAST_SEE_COMPLETE = 0x8;
    
    
    public static final String AC_SETTING_CONNECT_TIMEOUT = "amule_client_connect_timeout";
    public static final String AC_SETTING_READ_TIMEOUT = "amule_client_read_timeout";
    
    public boolean sendExceptions = false;
    public boolean enableLog = false;
    public boolean enableDebugOptions = false; 
    
    RefreshingActivity mRefreshingActivity;
    private Timer mAutoRefreshTimer;
    private boolean mAutoRefresh;
    private int mAutoRefreshInterval;
    
    
    public SharedPreferences mSettings;
    ECHelper mECHelper = new ECHelper(this);
    //ECHelper mECHelper = new ECHelperFakeClient(this);
    
    public boolean mainNeedsRefresh = true;
    
    UpdateChecker mUpdateChecker;
    
    interface RefreshingActivity {
        public void refreshContent();
    }

    
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
                                    if (mRefreshingActivity != null) mRefreshingActivity.refreshContent();
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

    
    

    

    public AmuleControllerApplication() {
        super();

    }

    public boolean refreshServerSettings()  {
        
        String host = mSettings.getString(AC_SETTING_HOSTNAME, "");
        int port = Integer.parseInt(mSettings.getString(AC_SETTING_PORT, "4712"));
        String password = mSettings.getString(AC_SETTING_PASSWORD, "");
        String version = mSettings.getString(AC_SETTING_VERSION, "V200");
        
        int connTimeout = Integer.parseInt(mSettings.getString(AC_SETTING_CONNECT_TIMEOUT, "10")) * 1000;
        int readTimeout = Integer.parseInt(mSettings.getString(AC_SETTING_READ_TIMEOUT, "30")) * 1000;
        
        if (host.length() == 0 || password.length() == 0) {
            return false;
        }
        
        mECHelper.setServerInfo(
                        mSettings.getString(AC_SETTING_HOSTNAME, "example.com"),
                        port,
                        version,
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
    
    public void refreshDebugSettings() {
        sendExceptions = mSettings.getBoolean(AC_SETTING_ENABLE_EXCEPTIONS, false);
        enableLog = mSettings.getBoolean(AC_SETTING_ENABLE_LOG, false);
        enableDebugOptions = mSettings.getBoolean(AC_SETTING_ENABLE_DEBUG_OPTIONS, false);
        
        if (mECHelper != null) {
            Object o = getSystemService(DROPBOX_SERVICE);
            if (o != null) mECHelper.mDropBox = (DropBoxManager) o;
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
        case AC_SETTING_SORT_SIZE:
            return ECPartFileComparator.ComparatorType.SIZE;
        case AC_SETTING_SORT_SPEED:
            return ECPartFileComparator.ComparatorType.SPEED;
        case AC_SETTING_SORT_PRIORITY:
            return ECPartFileComparator.ComparatorType.PRIORITY;
        case AC_SETTING_SORT_REMAINING:
            return ECPartFileComparator.ComparatorType.REMAINING;
        case AC_SETTING_SORT_LAST_SEE_COMPLETE:
            return ECPartFileComparator.ComparatorType.LAST_SEEN_COMPLETE;
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
