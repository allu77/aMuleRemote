package com.iukonline.amule.android.amuleremote.search;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.gui.GUIUtils;
import com.iukonline.amule.ec.ECSearchFile;

public class SearchResultDetailsListAdapter extends ArrayAdapter<ECSearchFile> {
    
    static class SearchResultsHolder {
        TextView mFileName;
        TextView mSize;
        TextView mSources;
        TextView mSourcesXfer;
        
    }
    
    public SearchResultDetailsListAdapter(Context context, int textViewResourceId, ArrayList<ECSearchFile> items) {
        super(context, textViewResourceId, items);
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            
        SearchResultsHolder holder;
        
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.frag_search_result_details_row, null);
            holder = new SearchResultsHolder();
            
            holder.mFileName = (TextView) v.findViewById(R.id.search_result_detail_filename);
            holder.mSize = (TextView) v.findViewById(R.id.search_result_detail_size);
            holder.mSources = (TextView) v.findViewById(R.id.search_result_detail_sources);
            holder.mSourcesXfer = (TextView) v.findViewById(R.id.search_result_detail_sources_xfer);
            
            v.setTag(holder);
        } else {
            holder = (SearchResultsHolder) v.getTag();
        }
        
        if (position < getCount()) {
            ECSearchFile o = getItem(position);
            holder.mFileName.setText(o.getFileName());
            holder.mSize.setText(GUIUtils.longToBytesFormatted(o.getSizeFull()));
            holder.mSources.setText(Integer.toString(o.getSourceCount()));
            holder.mSourcesXfer.setText(Integer.toString(o.getSourceXfer()));
        }
        return v;
    }

    
}
