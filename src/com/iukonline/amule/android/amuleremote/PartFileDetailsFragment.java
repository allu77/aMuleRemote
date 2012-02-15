package com.iukonline.amule.android.amuleremote;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.ec.ECPartFile;

public class PartFileDetailsFragment extends Fragment implements ECPartFileWatcher {
    
    byte[] mHash;
    ECPartFile mPartFile;
    AmuleControllerApplication mApp;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHash = getArguments().getByteArray(PartFileActivity.BUNDLE_PARAM_HASH);
        mApp = (AmuleControllerApplication) getActivity().getApplication();

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
    
            
            ((TextView) v.findViewById(R.id.partfile_detail_link)).setText(mPartFile.getEd2kLink());
            
            
            ((TextView) v.findViewById(R.id.partfile_detail_done)).setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeDone()));
            ((TextView) v.findViewById(R.id.partfile_detail_size)).setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeFull()));
            
            ((TextView) v.findViewById(R.id.partfile_detail_remaining)).setText("TBD");
            
            ((TextView) v.findViewById(R.id.partfile_detail_sources_available)).setText(Integer.toString(mPartFile.getSourceCount()));
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
        
            //LinearLayout titleBar = (LinearLayout) v.findViewById(R.id.partfile_detail_//titleBar);
        
    
            
            switch (mPartFile.getStatus()) {
            
            case ECPartFile.PS_ALLOCATING:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_allocating);
                //titleBar.setBackgroundResource(R.color.progressWaitingMid);
                break;
            case ECPartFile.PS_COMPLETE:
                tvStatus.setText(R.string.partfile_status_complete);
                ////titleBar.setBackgroundResource(R.color.progressRunningMid);
                break;
            case ECPartFile.PS_COMPLETING:
                tvStatus.setText(R.string.partfile_status_completing);
                ////titleBar.setBackgroundResource(R.color.progressRunningMid);
                break;
            case ECPartFile.PS_EMPTY:
                tvStatus.setText(R.string.partfile_status_empty); 
                //titleBar.setBackgroundResource(R.color.progressBlockedMid);
                break;
            case ECPartFile.PS_ERROR:
                
                tvStatus.setText(R.string.partfile_status_error);
                //titleBar.setBackgroundResource(R.color.progressBlockedMid);
                break;
            case ECPartFile.PS_HASHING:
                tvStatus.setText(R.string.partfile_status_hashing);
                //titleBar.setBackgroundResource(R.color.progressWaitingMid);
                
    
                break;
            case ECPartFile.PS_INSUFFICIENT:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_insuffcient);
                //titleBar.setBackgroundResource(R.color.progressBlockedMid);
                break;
            case ECPartFile.PS_PAUSED:
                tvStatus.setText(R.string.partfile_status_paused);
                //titleBar.setBackgroundResource(R.color.progressStoppedMid);
                break;
            case ECPartFile.PS_READY:
                if (mPartFile.getSourceXfer() > 0) {
                    tvStatus.setText(R.string.partfile_status_downloading);
                    tvStatus.append( " " + GUIUtils.longToBytesFormatted(mPartFile.getSpeed()) + "/s");
                    //titleBar.setBackgroundResource(R.color.progressRunningMid);
                } else {
                    tvStatus.setText(R.string.partfile_status_waiting);
                    //titleBar.setBackgroundResource(R.color.progressWaitingMid);
                } 
                break;
            case ECPartFile.PS_UNKNOWN:
                tvStatus.setText(R.string.partfile_status_unknown);
                //titleBar.setBackgroundResource(R.color.progressBlockedMid);
                break;
            case ECPartFile.PS_WAITINGFORHASH:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_waitingforhash);
                //titleBar.setBackgroundResource(R.color.progressWaitingMid);
    
                break;
                
            default:
                tvStatus.setText("UNKNOWN-" + mPartFile.getStatus());
                //titleBar.setBackgroundResource(R.color.progressBlockedMid);
    
                break;
            }
            
        
        }
    }

    
    
    
    
    
    
    
    
    
    // Interface ECPartFileWatcher

    @Override
    public String getWatcherId() {
        // TODO Auto-generated method stub
        return this.getClass().getName();
    }

    
    @Override
    public void updateECPartFile(ECPartFile newECPartFile) {
        // TODO: Check if hash is the same...
        if (mPartFile == null) mPartFile = newECPartFile;
        // We shouldn't need to re-assign mPartFile, since this should be the same modified...
        refreshView();
    }

}
