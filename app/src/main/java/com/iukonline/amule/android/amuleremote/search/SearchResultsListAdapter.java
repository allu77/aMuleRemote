package com.iukonline.amule.android.amuleremote.search;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.gui.GUIUtils;

public class SearchResultsListAdapter extends ArrayAdapter<SearchContainer> {
    
    static class SearchResultsHolder {
        TextView mFileName;
        TextView mResults;
        TextView mParams;
        ProgressBar mProgressBar;
        
    }
    
    public SearchResultsListAdapter(Context context, int textViewResourceId, ArrayList<SearchContainer> items) {
        super(context, textViewResourceId, items);
    }
    
    
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
            
        SearchResultsHolder holder;
        
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.frag_search_results_row, null);
            holder = new SearchResultsHolder();
            
            
            
            holder.mFileName = (TextView) v.findViewById(R.id.search_result_filename);
            holder.mResults = (TextView) v.findViewById(R.id.search_result_results);
            holder.mParams = (TextView) v.findViewById(R.id.search_result_params);
            holder.mProgressBar = (ProgressBar) v.findViewById(R.id.search_result_progress_bar);
            
            
            v.setTag(holder);
        } else {
            holder = (SearchResultsHolder) v.getTag();
        }
        
        
        if (position < getCount()) {
            SearchContainer o = getItem(position);
            holder.mFileName.setText(o.mFileName);
            
            switch (o.mSearchStatus) {
            case STARTING:
            case RUNNING:
                holder.mProgressBar.setVisibility(View.VISIBLE);
                break;
            default:
                holder.mProgressBar.setVisibility(View.GONE);
                break;
            }
            
            // TODO: Provide string resources
            StringBuilder params = new StringBuilder();
            params.append(getContext().getResources().getStringArray(R.array.search_search_type_description)[o.mSearchType]);
            if (o.mType != null && o.mType.length() > 0) params.append(", " + o.mType);
            if (o.mExtension != null && o.mExtension.length() > 0) params.append(", *." + o.mExtension);
            if (o.mMinSize > 0) params.append(", min " + GUIUtils.longToBytesFormatted(o.mMinSize));
            if (o.mMaxSize > 0) params.append(", max " + GUIUtils.longToBytesFormatted(o.mMaxSize));
            if (o.mAvailability > 0) params.append(", avail " + o.mAvailability); 
            holder.mParams.setText(params.toString());
            
            if (o.mResults != null && o.mResults.resultMap != null) {
                holder.mResults.setText(getContext().getResources().getString(R.string.search_list_result_count, o.mResults.resultMap.size()));
            } else {
                holder.mResults.setText(R.string.search_list_no_result);
            }
        }
        return v;
    }

    
}
