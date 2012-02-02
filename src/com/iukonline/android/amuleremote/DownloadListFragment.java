package com.iukonline.android.amuleremote;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DownloadListFragment extends ListFragment {
    
    ImageView mRefresh;
    ProgressBar mProgress; 


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        
        View v = inflater.inflate(R.layout.dlqueue_fragment, container, true);
        
        mRefresh = (ImageView) v.findViewById(R.id.main_button_refresh_new);
        mRefresh.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { updateDlList(); } });
        
        mProgress = (ProgressBar) v.findViewById(R.id.main_refresh);
        
        
        
        return v;    

    }
    
    public void updateDlList()  {
        
    }
}
