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

package com.iukonline.amule.android.amuleremote.helpers;

import android.content.SharedPreferences;
import android.util.Log;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsHelper {

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    public final static String SETTING_SERVER_LIST = "amule_server_list";
    public final static String SETTING_SERVER_CURRENT = "amule_server_current";

    public static final String SETTING_LEGACY_SERVER_HOSTNAME   = "amule_server_host";
    public static final String SETTING_LEGACY_SERVER_PORT       = "amule_server_port";
    public static final String SETTING_LECAGY_SERVER_PASSWORD   = "amule_server_password";
    public static final String SETTING_LEGACY_SERVER_VERSION    = "amule_server_version";

    private JSONArray jsonServers;
    private ArrayList<ServerSettings> mServerList;
    private int mCurrentServer = 0;
    private SharedPreferences mSettings;

    public boolean mNeedNavigationRefresh = true;

    public SettingsHelper(SharedPreferences settings) {
        mSettings = settings;
        refreshServerList();
        mCurrentServer = mSettings.getInt(SETTING_SERVER_CURRENT, 0);
        if (DEBUG) Log.d(TAG, "SettingsHelper.SettingsHelper: Server list size = " + mServerList.size() + ", Current server = " + mCurrentServer);
    }

    public int getServerCount() {
        return mServerList.size();
    }

    public int getCurrentServer() { return mCurrentServer < mServerList.size() ? mCurrentServer : 0 ; }
    public void setCurrentServer(int server) {
        mCurrentServer = server;
        mSettings.edit()
                .putInt(SETTING_SERVER_CURRENT, server)
                .commit();
    }

    public ServerSettings getServerSettings(int i) {
        if (DEBUG) Log.d(TAG, "SettingsHelper.getServerSettings: getting server " + i + "/" + mServerList.size());
        return (i < mServerList.size()) ? mServerList.get(i) :  null;
    }

    public ServerSettings getCurrentServerSettings() {
        if (mServerList.size() == 0) return null;
        return getServerSettings(mCurrentServer < mServerList.size() ? mCurrentServer : 0);
    }

    public boolean addServerSettings(ServerSettings serverSettings) {
        if (serverSettings != null) {
            if (DEBUG) Log.d(TAG, "SettingsHelper.addServerSettings: Previous list size was " + mServerList.size());
            mServerList.add(serverSettings);
            if (DEBUG) Log.d(TAG, "SettingsHelper.addServerSettings: Now list size is " + mServerList.size());
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

        if (DEBUG) Log.d(TAG, "SettingsHelper.commitServerList: Committing list of size " + mServerList.size());

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
