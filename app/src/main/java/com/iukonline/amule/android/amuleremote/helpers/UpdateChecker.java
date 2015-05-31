/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.Flavor;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class UpdateChecker {

    private final static String TAG = AmuleControllerApplication.AC_LOGTAG;

    private static final String AC_UPDATE_URL = Flavor.UPDATE_CHECKER_URL_PREFIX;
    private static final long AC_UPDATE_INTERVAL = Flavor.UPDATE_CHECKER_INTERVAL;
    
    public interface UpdatesWatcher {
        void notifyUpdate(String newReleaseURL, String releaseNotes);
    }
    
    UpdatesWatcher mUpdatesWatcher;
    long mLatestUpdatesCheck;
    boolean mLatestUpdatePublished = true;
    
    private String mReleaseNotes;
    private String mNewReleaseURL;
    
    private int mCurrentVersionCode;
    private long mBuildId = -1L;
    
    public UpdateChecker(int currentVersionCode) {
        mCurrentVersionCode = currentVersionCode;
    }

    public UpdateChecker(int currentVersionCode, long buildId) {
        mCurrentVersionCode = currentVersionCode;
        mBuildId = buildId;
    }
    
    public void registerUpdatesWatcher(UpdatesWatcher watcher) {
        mUpdatesWatcher = watcher;
        if (watcher != null) {
            if (! mLatestUpdatePublished) {
                mUpdatesWatcher.notifyUpdate(mNewReleaseURL, mReleaseNotes);
                mLatestUpdatePublished = true;
            } else {
                checkForUpdates();
            }
        }
    }
    
    private void checkForUpdates() {
        long now = System.currentTimeMillis();
        if (now - mLatestUpdatesCheck > AC_UPDATE_INTERVAL) {
            // Schedule update check Task, which will result in an update refresh and publish
            
            AsyncTask<String, Void, String> fetchReleaseJSON = new AsyncTask<String, Void, String>() {
                
                @Override
                protected String doInBackground(String... params) {
                    return getTextFileFromURL(params[0]);
                }
                
                @Override
                protected void onPostExecute(String result) {
                    if (result != null) parseUpdatesJSON(result);
                }
            };
            fetchReleaseJSON.execute(AC_UPDATE_URL + mCurrentVersionCode + ".json");
            mLatestUpdatesCheck = now;
        }
    }

    private void parseUpdatesJSON(String jsonString) {
        int latestVersionCode;
        long latestBuildId = -1L;
        String latestVersionReleaseNotesURL;
        String latestVersionDownloadURL;
        JSONObject json;


        try {
            json = new JSONObject(jsonString);
            latestVersionCode = json.getInt("versionCode");
            latestVersionReleaseNotesURL = json.getString("releaseNotesURL");
            latestVersionDownloadURL = json.getString("downloadURL");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid JSON object returned");
            return;
        }



        if (mBuildId >= 0) {
            try {
                latestBuildId = json.getLong("buildId");
            } catch (JSONException e) {
                Log.e(TAG, "Missing expected build Id");
                // DO NOT RETURN, just rely on version code
            }
        }


        if (latestVersionCode > mCurrentVersionCode || (latestVersionCode == mCurrentVersionCode && mBuildId > 0 && mBuildId < latestBuildId)) {

            mNewReleaseURL = latestVersionDownloadURL;
            AsyncTask<String, Void, String> fetchReleaseNotes = new AsyncTask<String, Void, String>() {

                @Override
                protected String doInBackground(String... params) {
                    return getTextFileFromURL(params[0]);
                }

                @Override
                protected void onPostExecute(String result) {
                    mReleaseNotes = result == null ? "" : result;

                    if (mUpdatesWatcher != null) {
                        mUpdatesWatcher.notifyUpdate(mNewReleaseURL, mReleaseNotes);
                    } else {
                        mLatestUpdatePublished = false;
                    }
                }
            };
            fetchReleaseNotes.execute(latestVersionReleaseNotesURL);
        }
    }

    
    private String getTextFileFromURL(String url) {
        DefaultHttpClient client = new DefaultHttpClient();
        String responseBody = null;
        HttpResponse httpResponse;
        ByteArrayOutputStream out = null;

        int statusCode;

        try {
            HttpGet httpget = new HttpGet(url);
            httpResponse = client.execute(httpget);
            statusCode = httpResponse.getStatusLine().getStatusCode();


            if (statusCode == HttpStatus.SC_OK) {
                out = new ByteArrayOutputStream();
                httpResponse.getEntity().writeTo(out);
                responseBody = out.toString();
            }

        } catch (Exception e) {
            Log.e(TAG, e.toString());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
            }
        }
        return responseBody;
    }
}
