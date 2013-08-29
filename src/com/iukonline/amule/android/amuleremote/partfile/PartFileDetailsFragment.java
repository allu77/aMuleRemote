package com.iukonline.amule.android.amuleremote.partfile;

import java.text.DateFormat;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.android.amuleremote.helpers.gui.GUIUtils;
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECPartFile;

public class PartFileDetailsFragment extends SherlockFragment implements ECPartFileWatcher {
    
    byte[] mHash;
    ECPartFile mPartFile;
    AmuleControllerApplication mApp;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHash = getArguments().getByteArray(PartFileActivity.BUNDLE_PARAM_HASH);
        mApp = (AmuleControllerApplication) getActivity().getApplication();
        
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
        
        View v = inflater.inflate(R.layout.partfile_details_fragment, container, false);
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
            
            TextView tvStatus = (TextView) v.findViewById(R.id.partfile_detail_status);
            TextView tvPrio = (TextView) v.findViewById(R.id.partfile_detail_priority);
    
            
            ((TextView) v.findViewById(R.id.partfile_detail_filename)).setText(mPartFile.getFileName());
            
            String textCat = getResources().getString(R.string.partfile_details_cat_unknown);
            long cat = mPartFile.getCat();
            if (cat == 0) {
                textCat = getResources().getString(R.string.partfile_details_cat_nocat);;
            } else {
                ECCategory[] catList = mApp.mECHelper.getCategories();
                if (catList != null) {
                    for (int i = 0; i < catList.length; i++) {
                        if (catList[i].getId() == cat) {
                            textCat = catList[i].getTitle();
                            break;
                        }
                    }
                }
            }
            ((TextView) v.findViewById(R.id.partfile_detail_category)).setText(textCat);

            
            
            ((TextView) v.findViewById(R.id.partfile_detail_link)).setText(mPartFile.getEd2kLink());
            
            
            ((TextView) v.findViewById(R.id.partfile_detail_done)).setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeDone()));
            ((TextView) v.findViewById(R.id.partfile_detail_size)).setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeFull()));
            
            ((TextView) v.findViewById(R.id.partfile_detail_remaining)).setText(GUIUtils.getETA(getActivity(), mPartFile.getSizeFull() - mPartFile.getSizeDone(), mPartFile.getSpeed()));
            
            ((TextView) v.findViewById(R.id.partfile_detail_lastseencomplete)).setText(
                            mPartFile.getLastSeenComp() == null || mPartFile.getLastSeenComp().getTime() == 0 ? 
                            getResources().getString(R.string.partfile_last_seen_never) 
                            : 
                            DateFormat.getDateTimeInstance().format(mPartFile.getLastSeenComp()));
            
            ((TextView) v.findViewById(R.id.partfile_detail_sources_available)).setText(Integer.toString(mPartFile.getSourceCount() - mPartFile.getSourceNotCurrent()));
            ((TextView) v.findViewById(R.id.partfile_detail_sources_active)).setText(Integer.toString(mPartFile.getSourceXfer()));
            ((TextView) v.findViewById(R.id.partfile_detail_sources_a4af)).setText(Integer.toString(mPartFile.getSourceA4AF()));
            ((TextView) v.findViewById(R.id.partfile_detail_sources_notcurrent)).setText(Integer.toString(mPartFile.getSourceNotCurrent()));
            
            switch (mPartFile.getPrio()) {
            case ECPartFile.PR_LOW:
                tvPrio.setText(R.string.partfile_prio_low);
                break;
            case ECPartFile.PR_NORMAL:
                tvPrio.setText(R.string.partfile_prio_normal);
                break;
            case ECPartFile.PR_HIGH:
                tvPrio.setText(R.string.partfile_prio_high);
                break;
            case ECPartFile.PR_AUTO_LOW:
                tvPrio.setText(R.string.partfile_prio_auto_low);
                break;
            case ECPartFile.PR_AUTO_NORMAL:
                tvPrio.setText(R.string.partfile_prio_auto_normal);
                break;
            case ECPartFile.PR_AUTO_HIGH:
                tvPrio.setText(R.string.partfile_prio_auto_high);
                break;
            default:
                tvPrio.setText(R.string.partfile_prio_unknown);
                break;
            }
        
            
            int statusColor = R.color.progressWaitingMid;
            
            switch (mPartFile.getStatus()) {
            
            case ECPartFile.PS_ALLOCATING:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_allocating);
                break;
            case ECPartFile.PS_COMPLETE:
                tvStatus.setText(R.string.partfile_status_complete);
                statusColor = R.color.progressRunningMid;
                break;
            case ECPartFile.PS_COMPLETING:
                tvStatus.setText(R.string.partfile_status_completing);
                statusColor = R.color.progressRunningMid;
                break;
            case ECPartFile.PS_EMPTY:
                tvStatus.setText(R.string.partfile_status_empty); 
                statusColor = R.color.progressBlockedMid;
                break;
            case ECPartFile.PS_ERROR:
                tvStatus.setText(R.string.partfile_status_error);
                statusColor = R.color.progressBlockedMid;
                break;
            case ECPartFile.PS_HASHING:
                tvStatus.setText(R.string.partfile_status_hashing);
                break;
            case ECPartFile.PS_INSUFFICIENT:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_insuffcient);
                statusColor = R.color.progressBlockedMid;
                break;
            case ECPartFile.PS_PAUSED:
                tvStatus.setText(R.string.partfile_status_paused);
                break;
            case ECPartFile.PS_READY:
                if (mPartFile.getSourceXfer() > 0) {
                    tvStatus.setText(R.string.partfile_status_downloading);
                    tvStatus.append( " " + GUIUtils.longToBytesFormatted(mPartFile.getSpeed()) + "/s");
                    statusColor = R.color.progressRunningMid;
                } else {
                    tvStatus.setText(R.string.partfile_status_waiting);
                    statusColor = R.color.progressWaitingMid;
                } 
                break;
            case ECPartFile.PS_UNKNOWN:
                tvStatus.setText(R.string.partfile_status_unknown);
                statusColor = R.color.progressStoppedMid;
                break;
            case ECPartFile.PS_WAITINGFORHASH:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_waitingforhash);
                break;
            default:
                tvStatus.setText("UNKNOWN-" + mPartFile.getStatus());
                break;
            }
            tvStatus.setTextColor(getResources().getColor(statusColor));
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
                    if (mApp.enableLog) Log.e(AmuleControllerApplication.AC_LOGTAG, "Got a different hash in updateECPartFile!");
                    mApp.mECHelper.resetClient();
                }
            }
        }

        refreshView();
    }

}
