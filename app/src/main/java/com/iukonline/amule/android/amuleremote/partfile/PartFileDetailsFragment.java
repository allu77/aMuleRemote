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

package com.iukonline.amule.android.amuleremote.partfile;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.android.amuleremote.helpers.gui.GUIUtils;
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECPartFile;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class PartFileDetailsFragment extends Fragment implements ECPartFileWatcher {

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    byte[] mHash;
    ECPartFile mPartFile;
    AmuleRemoteApplication mApp;

    @InjectView(R.id.partfile_detail_status) TextView mStatusText;
    @InjectView(R.id.partfile_detail_priority) TextView mPriorityText;
    @InjectView(R.id.partfile_detail_filename) TextView mFileNameText;
    @InjectView(R.id.partfile_detail_category) TextView mCategoryText;
    @InjectView(R.id.partfile_detail_link) TextView mLinkText;
    @InjectView(R.id.partfile_detail_done) TextView mDoneText;
    @InjectView(R.id.partfile_detail_size) TextView mSizeText;
    @InjectView(R.id.partfile_detail_remaining) TextView mRemainingText;
    @InjectView(R.id.partfile_detail_lastseencomplete) TextView mLastSeenText;
    @InjectView(R.id.partfile_detail_sources_available) TextView mSourcesAvailableText;
    @InjectView(R.id.partfile_detail_sources_active) TextView mSourcesActiveText;
    @InjectView(R.id.partfile_detail_sources_a4af) TextView mSourcesA4AFText;
    @InjectView(R.id.partfile_detail_sources_notcurrent) TextView mSourcesNotCurrentText;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHash = getArguments().getByteArray(PartFileActivity.BUNDLE_PARAM_HASH);
        mApp = (AmuleRemoteApplication) getActivity().getApplication();
        
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            //return null;
        }

        View v = inflater.inflate(R.layout.frag_partfile_details, container, false);
        ButterKnife.inject(this, v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateECPartFile(mApp.mECHelper.registerForECPartFileUpdates(this, mHash));
    }


    @Override
    public void onPause() {
        super.onPause();
        mApp.mECHelper.unRegisterFromECPartFileUpdates(this, mHash);
    }








    private void refreshView() {


        View v = getView();

        if (mPartFile != null) {

            mFileNameText.setText(mPartFile.getFileName());

            String textCat = getResources().getString(R.string.partfile_details_cat_unknown);
            int backgroundColorCat = 0;
            int textColorCat = getResources().getColor(R.color.secondary_text);
            long cat = mPartFile.getCat();
            if (cat == 0) {
                textCat = getResources().getString(R.string.partfile_details_cat_nocat);

            } else {
                ECCategory[] catList = mApp.mECHelper.getCategories();
                if (catList != null) {
                    for (int i = 0; i < catList.length; i++) {
                        if (catList[i].getId() == cat) {
                            textCat = catList[i].getTitle();

                            backgroundColorCat = 0xff000000 | (int) catList[i].getColor();
                            textColorCat = 0xff000000 | GUIUtils.chooseFontColor((int) catList[i].getColor());


                            break;
                        }
                    }
                }
            }
            mCategoryText.setText(textCat);
            ((GradientDrawable) mCategoryText.getBackground()).setColor(backgroundColorCat);
            mCategoryText.setTextColor(textColorCat);

            mLinkText.setText(mPartFile.getEd2kLink());
            mDoneText.setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeDone()));
            mSizeText.setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeFull()));
            mRemainingText.setText(GUIUtils.getETA(getActivity(), mPartFile.getSizeFull() - mPartFile.getSizeDone(), mPartFile.getSpeed()));

            Date lastSeenComplateDate = mPartFile.getLastSeenComp();
            DateTime lastSeenComplateDateTime = new DateTime(lastSeenComplateDate);
            DateTime now = new DateTime();
            if (lastSeenComplateDate == null || lastSeenComplateDate.getTime() == 0L) {
                mLastSeenText.setText(getResources().getString(R.string.partfile_last_seen_never));
            } else {
                long lastSeenSeconds = (System.currentTimeMillis() -  mPartFile.getLastSeenComp().getTime()) / 1000L;

                if (lastSeenSeconds <= 60L) {
                    mLastSeenText.setText(getResources().getString(R.string.partfile_last_seen_now));
                } else if (lastSeenSeconds <= 3600L) {
                    mLastSeenText.setText(getResources().getQuantityString(R.plurals.partfile_last_seen_mins, (int) (lastSeenSeconds / 60L), lastSeenSeconds / 60L));
                } else if (lastSeenSeconds <= 86400L) {
                    int lastSeenHours = (int) (lastSeenSeconds / 3600);
                    if (lastSeenHours < 12 || lastSeenComplateDateTime.getDayOfMonth() == now.getDayOfMonth()) {
                        mLastSeenText.setText(getResources().getQuantityString(R.plurals.partfile_last_seen_hours, lastSeenHours, lastSeenHours));
                    } else {
                        mLastSeenText.setText(getResources().getString(R.string.partfile_last_seen_yesterday));
                    }
                } else {
                    int diffDays = Days.daysBetween(lastSeenComplateDateTime, now).getDays();
                    if (diffDays <= 31) {
                        mLastSeenText.setText(getResources().getQuantityString(R.plurals.partfile_last_seen_days, diffDays, diffDays));
                    } else if (diffDays <= 180) {
                        int diffMonths = Months.monthsBetween(lastSeenComplateDateTime, now).getMonths();
                        mLastSeenText.setText(getResources().getQuantityString(R.plurals.partfile_last_seen_months, diffMonths, diffMonths));
                    } else {
                        mLastSeenText.setText(
                                getResources().getString(R.string.partfile_last_seen_date,
                                        lastSeenComplateDateTime.toString(
                                                DateTimeFormat
                                                        .forStyle("L-")
                                                        .withLocale(getResources().getConfiguration().locale)
                                        )
                                )
                        );
                    }
                }
            }

            mSourcesAvailableText.setText(Integer.toString(mPartFile.getSourceCount() - mPartFile.getSourceNotCurrent()));
            mSourcesActiveText.setText(Integer.toString(mPartFile.getSourceXfer()));
            mSourcesA4AFText.setText(Integer.toString(mPartFile.getSourceA4AF()));
            mSourcesNotCurrentText.setText(Integer.toString(mPartFile.getSourceNotCurrent()));

            switch (mPartFile.getPrio()) {
            case ECPartFile.PR_LOW:
                mPriorityText.setText(R.string.partfile_prio_low);
                break;
            case ECPartFile.PR_NORMAL:
                mPriorityText.setText(R.string.partfile_prio_normal);
                break;
            case ECPartFile.PR_HIGH:
                mPriorityText.setText(R.string.partfile_prio_high);
                break;
            case ECPartFile.PR_AUTO_LOW:
                mPriorityText.setText(R.string.partfile_prio_auto_low);
                break;
            case ECPartFile.PR_AUTO_NORMAL:
                mPriorityText.setText(R.string.partfile_prio_auto_normal);
                break;
            case ECPartFile.PR_AUTO_HIGH:
                mPriorityText.setText(R.string.partfile_prio_auto_high);
                break;
            default:
                mPriorityText.setText(R.string.partfile_prio_unknown);
                break;
            }


            int statusColor = R.color.progressWaitingMid;

            switch (mPartFile.getStatus()) {

            case ECPartFile.PS_ALLOCATING:
                mStatusText.setText(R.string.partfile_status_allocating);
                break;
            case ECPartFile.PS_COMPLETE:
                mStatusText.setText(R.string.partfile_status_complete);
                statusColor = R.color.progressRunningMid;
                break;
            case ECPartFile.PS_COMPLETING:
                mStatusText.setText(R.string.partfile_status_completing);
                statusColor = R.color.progressRunningMid;
                break;
            case ECPartFile.PS_EMPTY:
                mStatusText.setText(R.string.partfile_status_empty);
                statusColor = R.color.progressBlockedMid;
                break;
            case ECPartFile.PS_ERROR:
                mStatusText.setText(R.string.partfile_status_error);
                statusColor = R.color.progressBlockedMid;
                break;
            case ECPartFile.PS_WAITINGFORHASH:
            case ECPartFile.PS_HASHING:
                mStatusText.setText(R.string.partfile_status_hashing);
                break;
            case ECPartFile.PS_INSUFFICIENT:
                mStatusText.setText(R.string.partfile_status_insuffcient);
                statusColor = R.color.progressBlockedMid;
                break;
            case ECPartFile.PS_PAUSED:
                mStatusText.setText(R.string.partfile_status_paused);
                break;
            case ECPartFile.PS_READY:
                if (mPartFile.getSourceXfer() > 0) {
                    mStatusText.setText(R.string.partfile_status_downloading);
                    mStatusText.append(" " + GUIUtils.longToBytesFormatted(mPartFile.getSpeed()) + "/s");
                    statusColor = R.color.progressRunningMid;
                } else {
                    mStatusText.setText(R.string.partfile_status_waiting);
                    statusColor = R.color.progressWaitingMid;
                }
                break;
            case ECPartFile.PS_UNKNOWN:
                mStatusText.setText(R.string.partfile_status_unknown);
                statusColor = R.color.progressStoppedMid;
                break;
            default:
                mStatusText.setText("UNKNOWN-" + mPartFile.getStatus());
                break;
            }
            mStatusText.setTextColor(getResources().getColor(statusColor));
        }
    }










    // Interface ECPartFileWatcher

    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }


    @Override
    public void updateECPartFile(ECPartFile newECPartFile) {
        if (newECPartFile != null) {
            if (mPartFile == null) {
                mPartFile = newECPartFile;
            } else {
                if (! mPartFile.getHashAsString().equals(newECPartFile.getHashAsString())) {
                    Toast.makeText(mApp, R.string.error_unexpected, Toast.LENGTH_LONG).show();
                    if (DEBUG) Log.e(TAG, "Got a different hash in updateECPartFile!");
                    mApp.mECHelper.resetClient();
                }
            }
        }

        refreshView();
    }

}
