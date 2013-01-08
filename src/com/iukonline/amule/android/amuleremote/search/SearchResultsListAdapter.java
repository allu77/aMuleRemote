package com.iukonline.amule.android.amuleremote.search;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;

public class SearchResultsListAdapter extends ArrayAdapter<SearchContainer> {
    
    static class SearchResultsHolder {
        TextView mFileName;
        TextView mResults;
        TextView mStatus;
        TextView mProgress;
        
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
            holder.mStatus = (TextView) v.findViewById(R.id.search_result_status);
            holder.mProgress = (TextView) v.findViewById(R.id.search_result_progress);
            
            v.setTag(holder);
        } else {
            holder = (SearchResultsHolder) v.getTag();
        }
        
        
        if (position < getCount()) {
            SearchContainer o = getItem(position);
            holder.mFileName.setText(o.mFileName);
            holder.mStatus.setText(" - " + o.mSearchStatus.toString());
            holder.mProgress.setText(" - " + o.mSearchProgress + "%");
            if (o.mResults != null && o.mResults.resultMap != null) {
                holder.mResults.setText(" - " + Integer.toString(o.mResults.resultMap.size()));
            } else {
                holder.mResults.setText(" - 0");
            }
        }
        return v;
    }

    
}
