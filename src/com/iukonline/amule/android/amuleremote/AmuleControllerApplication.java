package com.iukonline.amule.android.amuleremote;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.iukonline.amule.android.amuleremote.echelper.ECHelper;


public class AmuleControllerApplication extends Application {
    
    public static final String AC_SHARED_PREFERENCES_NAME = "AmuleController";
    
    public static final String AC_SETTING_HOSTNAME   = "amule_server_host";
    public static final String AC_SETTING_PORT       = "amule_server_port";
    public static final String AC_SETTING_PASSWORD   = "amule_server_password"; 
    
    public static final String AC_SETTING_AUTOREFRESH = "amule_client_autorefresh";
    
    public static final String AC_SETTING_SORT          = "amule_client_sort";
    
    public static final String AC_SETTING_CONNECT_TIMEOUT = "amule_client_connect_timeout";
    public static final String AC_SETTING_READ_TIMEOUT = "amule_client_read_timeout";
    
    SharedPreferences mSettings;
    ECHelper mECHelper = new ECHelper(this);
    
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

    @Override
    public void onCreate() {
        super.onCreate();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
        refreshServerSettings();
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }
    
    
    
    final static int AMULE_ACTIVITY_ID_MAIN = 1;
    final static int AMULE_ACTIVITY_ID_DETAILS = 2;
    final static int AMULE_FRAGMENT_DL_QUEUE = 3;

}
