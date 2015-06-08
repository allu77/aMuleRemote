/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers;

import android.content.SharedPreferences;
import android.util.Log;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsHelper {

    private final static String TAG = AmuleControllerApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    public final static String SETTING_SERVER_LIST = "amule_server_list";
    public final static String SETTING_SERVER_CURRENT = "amule_server_current";

    public static final String SETTING_LEGACY_SERVER_HOSTNAME   = "amule_server_host";
    public static final String SETTING_LEGACY_SERVER_PORT       = "amule_server_port";
    public static final String SETTING_LECAGY_SERVER_PASSWORD   = "amule_server_password";
    public static final String SETTING_LEGACY_SERVER_VERSION    = "amule_server_version";


    /*public static String TEST_SERVER = "{\n" +
            "\tservers: [\n" +
            "\t\t{\n" +
            "\t\t\tname: \"Esterno\",\n" +
            "\t\t\thost: \"***REMOVED***\",\n" +
            "\t\t\tport: 4712,\n" +
            "\t\t\tversion: \"V204\",\n" +
            "\t\t\tpassword: \"***REMOVED***\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}";*/

    private JSONArray jsonServers;
    private ArrayList<ServerSettings> mServerList;
    private int mCurrentServer = 0;
    private SharedPreferences mSettings;

    public SettingsHelper(SharedPreferences settings) {
        mSettings = settings;
        refreshServerList();
    }

    public int getServerCount() {
        return mServerList.size();
    }

    public ServerSettings getServerSettings(int i) {
        return (i < mServerList.size()) ? mServerList.get(i) :  null;
    }

    public ServerSettings getCurrentServerSettings() {
        if (mServerList.size() == 0) return null;
        return getServerSettings(mCurrentServer < mServerList.size() ? mCurrentServer : 0);
    }

    public boolean addServerSettings(ServerSettings serverSettings) {
        if (serverSettings != null) {
            mServerList.add(serverSettings);
            return true;
        } else {
            return false;
        }
    }

    public boolean removeServerSettings(int i) {
        if (i >= 0 && i < mServerList.size()) {
            mServerList.remove(i);
            return true;
        } else {
            return false;
        }
    }

    public ServerSettings editServerSettings(int i, String name, String host, int port, String password, String version) {
        if (i >= 0 && i < mServerList.size()) {
            ServerSettings serverSettings = mServerList.get(i);
            serverSettings.name = name;
            serverSettings.host = host;
            serverSettings.port = port;
            serverSettings.password = password;
            serverSettings.version = version;
            return serverSettings;
        } else {
            return null;
        }
    }

    public boolean refreshServerList() {
        try {
            JSONObject jsonObj = new JSONObject(mSettings.getString(SETTING_SERVER_LIST, "{servers:[]}"));
            jsonServers = jsonObj.getJSONArray("servers");
        } catch (JSONException e) {
            jsonServers = new JSONArray();
        }
        int jsonLength = jsonServers.length();
        mServerList = new ArrayList<ServerSettings>(jsonLength);
        for (int i = 0; i < jsonLength; i++) {
            JSONObject jsonServer = null;
            try {
                jsonServer = jsonServers.getJSONObject(i);
                mServerList.add(new ServerSettings(
                        jsonServer.getString("name"),
                        jsonServer.getString("host"),
                        jsonServer.getInt("port"),
                        jsonServer.getString("password"),
                        jsonServer.getString("version")
                ));
            } catch (JSONException e) {
                return false;
            }
        }
        return true;
    }

    public boolean commitServerList() {
        StringBuilder sb = new StringBuilder("{servers:[");

        for (ServerSettings s : mServerList) {
            sb.append("{name:\""+s.name+"\",host:\""+s.host+"\",port:"+s.port+",password:\""+s.password+"\",version:\""+s.version+"\"},");
        }

        if(mServerList.size() > 0) sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");

        mSettings.edit()
                .putString(SETTING_SERVER_LIST, sb.toString())
                .commit();

        //TEST_SERVER = sb.toString();

        return true;
    }

    public void convertLegacyServerSettings() {
        if (!mSettings.contains(SETTING_LEGACY_SERVER_HOSTNAME)) return;

        if (DEBUG) Log.d(TAG, "SettingsHelper.convertLegacyServerSettings(): Converting legacy server settings to new");
        try {
            addServerSettings(new ServerSettings(
                    "Default",
                    mSettings.getString(SETTING_LEGACY_SERVER_HOSTNAME, "FAKEHOST"),
                    Integer.parseInt(mSettings.getString(SETTING_LEGACY_SERVER_PORT, "4712")),
                    mSettings.getString(SETTING_LECAGY_SERVER_PASSWORD, "FAKEPASSWORD"),
                    mSettings.getString(SETTING_LEGACY_SERVER_VERSION, "V204")
            ));
        } catch (NumberFormatException e) {
            addServerSettings(new ServerSettings(
                    "Default",
                    mSettings.getString(SETTING_LEGACY_SERVER_HOSTNAME, "FAKEHOST"),
                    4712,
                    mSettings.getString(SETTING_LECAGY_SERVER_PASSWORD, "FAKEPASSWORD"),
                    mSettings.getString(SETTING_LEGACY_SERVER_VERSION, "V204")
            ));
        }
        commitServerList();
        if (DEBUG) Log.d(TAG, "SettingsHelper.convertLegacyServerSettings(): Removing legacy settings");
        mSettings.edit()
                .remove(SETTING_LEGACY_SERVER_HOSTNAME)
                .remove(SETTING_LEGACY_SERVER_PORT)
                .remove(SETTING_LECAGY_SERVER_PASSWORD)
                .remove(SETTING_LEGACY_SERVER_VERSION)
                .commit();
    }

    public static class ServerSettings {
        public String name;
        public String host;
        public int port;
        public String password;
        public String version;

        public ServerSettings(String name, String host, int port, String password, String version) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.password = password;
            this.version = version;
        }
    }
}
