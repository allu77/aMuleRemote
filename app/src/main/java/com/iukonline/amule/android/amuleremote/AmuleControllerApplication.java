package com.iukonline.amule.android.amuleremote;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.DropBoxManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.helpers.UpdateChecker;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ClientStatusWatcher.AmuleClientStatus;
import com.iukonline.amule.android.amuleremote.helpers.ec.ECHelper;
import com.iukonline.amule.android.amuleremote.helpers.gui.dialogs.WhatsNewDialogFragment;
import com.iukonline.amule.android.amuleremote.search.SearchContainer;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;
import com.iukonline.amule.ec.ECSearchFile;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Timer;
import java.util.TimerTask;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;


@ReportsCrashes(formKey = "",
                httpMethod = Method.PUT,
                reportType = Type.JSON,
                formUri = "http://amuleremote.iriscouch.com/acra-amuleremote/_design/acra-storage/_update/report",
                formUriBasicAuthLogin = "amuleremote-reporter",
                formUriBasicAuthPassword = "ZRYEgTWS9wdHnK4DktUrcHh3",
                //formKey = "***REMOVED***",
                mode = ReportingInteractionMode.DIALOG,
                customReportContent = { 
                    ReportField.APP_VERSION_CODE,
                    ReportField.APP_VERSION_NAME,
                    ReportField.PACKAGE_NAME,
                    ReportField.BUILD,
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
                    ReportField.CRASH_CONFIGURATION,
                    ReportField.INSTALLATION_ID
                },
                logcatArguments = { "-t", "500", "-v", "time", "aMuleRemote:D", "*:S" },
                additionalDropBoxTags = { "aMuleRemote" },
                resToastText = R.string.crash_toast_text, 
                resDialogText = R.string.crash_dialog_text,
                resDialogTitle = R.string.crash_dialog_title,
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, 
                resDialogOkToast = R.string.crash_dialog_ok_toast 
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
    
    
    // Hidden settings 
    public static final String AC_SETTING_SORT          = "amule_client_sort";
    public static final String AC_SETTING_SEARCH_TYPE   = "amule_client_search_type";
    public static final String AC_SETTING_LAST_APP_VER  = "amule_client_last_app_ver";
    public static final String AC_SETTING_TIPS_SHOWN    = "amule_client_tips_shown";
    
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
    
    
    private final static String TAG_DIALOG_WHATS_NEW = "dialog_whats_new";
    
    public boolean sendExceptions = false;
    public boolean enableLog = false;
    public boolean enableDebugOptions = false; 
    public int mVersionCode = -1;
    public String mVersionName;
    
    public ECSearchFile mStartDownload;
    public SearchContainer mStartSearch;

    public Activity mOnTopActivity = null;
    
    RefreshingActivity mRefreshingActivity;
    private Timer mAutoRefreshTimer;
    private boolean mAutoRefresh;
    private int mAutoRefreshInterval;
    
    
    public SharedPreferences mSettings;
    public ECHelper mECHelper = new ECHelper(this);
    //ECHelper mECHelper = new ECHelperFakeClient(this);
    
    public boolean mainNeedsRefresh = true;
    
    public UpdateChecker mUpdateChecker;
    
    public interface RefreshingActivity {
        void refreshContent();
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
                                    if (mRefreshingActivity != null) {
                                        mRefreshingActivity.refreshContent();
                                    } else {
                                        if (mECHelper != null && mECHelper.getECClientStatus() == AmuleClientStatus.IDLE) {
                                            // TODO: need to send some keepalive command
                                        }
                                    }
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

    
    public void notifyErrorOnGUI(CharSequence errorText) {
        if (mOnTopActivity != null) {
            Crouton.makeText(mECHelper.getApplication().mOnTopActivity, errorText, Style.ALERT).show();
        } else {
            Toast.makeText(this, "aMuleRemote: " + errorText, Toast.LENGTH_LONG).show();
        }
    }

    

    public AmuleControllerApplication() {
        super();

    }

    public boolean refreshServerSettings()  {

        String host = mSettings.getString(AC_SETTING_HOSTNAME, "");
        int port = 4712;
        
        try {
            port = Integer.parseInt(mSettings.getString(AC_SETTING_PORT, "4712"));
            if (port < 1 || port > 65535) {
                Toast.makeText(this,  R.string.error_invalid_port, Toast.LENGTH_LONG).show();
                port = 4712;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this,  R.string.error_invalid_port, Toast.LENGTH_LONG).show();
        }
        String password = mSettings.getString(AC_SETTING_PASSWORD, "");
        String version = mSettings.getString(AC_SETTING_VERSION, "V200");
        
        int connTimeout = 10;
        int readTimeout = 30;
        try {
            connTimeout = Integer.parseInt(mSettings.getString(AC_SETTING_CONNECT_TIMEOUT, "10")) * 1000;
            readTimeout = Integer.parseInt(mSettings.getString(AC_SETTING_READ_TIMEOUT, "30")) * 1000;
        } catch (NumberFormatException e) {
            // TODO: Provide custom error
        }
        
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
        
        if (mECHelper != null && android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
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
            mVersionCode = pInfo.versionCode;
            mVersionName = pInfo.versionName;
            mUpdateChecker = new UpdateChecker(mVersionCode);
        } catch (NameNotFoundException e) {
        }
        
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        normalizeSettings();
        refreshServerSettings();
        refreshRefreshSettings();
    }
    
    private void normalizeSettings() {

        SharedPreferences.Editor e = mSettings.edit();
        if (mSettings.getInt(AC_SETTING_LAST_APP_VER, -1) < 17) {
            e.remove(AmuleControllerApplication.AC_SETTING_ENABLE_EXCEPTIONS);
        }
        e.commit();
        
    }

    
    public static ECPartFileComparator.ComparatorType getDlComparatorTypeFromSortSetting (byte sortSetting) {
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
    
    public boolean showWhatsNew(FragmentManager fm) {
        
        int currVersion = mSettings.getInt(AC_SETTING_LAST_APP_VER, -1);
        if (mVersionCode > currVersion) {
            if (fm.findFragmentByTag(TAG_DIALOG_WHATS_NEW) == null) {
                StringBuilder sb = new StringBuilder();
                if (currVersion < 14) sb.append(getResources().getString(R.string.dialog_whats_new_features_14)); 
                if (currVersion < 18) sb.append("\n" + getResources().getString(R.string.dialog_whats_new_features_18));
                WhatsNewDialogFragment d = new WhatsNewDialogFragment(getResources().getString(R.string.dialog_whats_new_welcome, mVersionName), sb.toString());
                d.show(fm, TAG_DIALOG_WHATS_NEW);
                SharedPreferences.Editor e = mSettings.edit();
                e.putInt(AmuleControllerApplication.AC_SETTING_LAST_APP_VER, mVersionCode);
                e.commit();
            }
            return true;
        } else {
            return false;
        }
        
    }
    
    public void resetAppVersionInfo() {
        SharedPreferences.Editor e = mSettings.edit();
        e.remove(AmuleControllerApplication.AC_SETTING_LAST_APP_VER);
        e.commit();
    }

}
