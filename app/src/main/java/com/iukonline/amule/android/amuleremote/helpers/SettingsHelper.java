/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SettingsHelper {

    public static String TEST_SERVER = "{\n" +
            "\tservers: [\n" +
            "\t\t{\n" +
            "\t\t\tname: \"Esterno\",\n" +
            "\t\t\thost: \"***REMOVED***\",\n" +
            "\t\t\tport: 4712,\n" +
            "\t\t\tversion: \"V204\",\n" +
            "\t\t\tpassword: \"***REMOVED***\"\n" +
            "\t\t}\n" +
            "\t]\n" +
            "}";

    private JSONArray jsonServers;
    private ArrayList<ServerSettings> mServerList;

    public SettingsHelper() {
        refresh();
    }
    public int getServerCount() {
        return mServerList.size();
    }

    public ServerSettings getServerSettings(int i) {
        return (i < mServerList.size()) ? mServerList.get(i) :  null;
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

    public boolean refresh() {
        try {
            JSONObject jsonObj = new JSONObject(TEST_SERVER);
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

    public boolean commit() {
        StringBuilder sb = new StringBuilder("{servers:[");

        for (ServerSettings s : mServerList) {
            sb.append("{name:\""+s.name+"\",host:\""+s.host+"\",port:"+s.port+",password:\""+s.password+"\",version:\""+s.version+"\"},");
        }

        if(mServerList.size() > 0) sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");

        TEST_SERVER = sb.toString();

        return true;
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
