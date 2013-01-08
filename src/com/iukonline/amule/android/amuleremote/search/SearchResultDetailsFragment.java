package com.iukonline.amule.android.amuleremote.search;

import java.util.ArrayList;
import java.util.Collection;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECSearchListWatcher;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.ec.ECSearchFile;

public class SearchResultDetailsFragment extends SherlockListFragment implements ECSearchListWatcher {
    AmuleControllerApplication mApp;
    SearchResultDetailsListAdapter mAdapter;
    
    int mPosition;
    SearchContainer mSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getActivity().getApplication();
        // mSearch = mApp.mECHelper.getSearchItem(mPosition);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.onCreate: Will show details for position " + mPosition);
        super.onCreate(savedInstanceState);
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
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.onCreateView: Inflating view");
        View v = inflater.inflate(R.layout.frag_search_result_details_list, container, false);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.onCreateView: Inflated view");
        return v;
    }

    @Override
    public void onResume() {
        updateECSearchList(mApp.mECHelper.registerForECSsearchList(this));
        super.onResume();
    }
    
    @Override
    public void onPause() {
        mApp.mECHelper.unRegisterFromECSearchList(this);
        super.onPause();
    }

    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    @Override
    public void updateECSearchList(ArrayList<SearchContainer> searches) {

        if (mAdapter == null) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.updateECSearchList: Creating new adapter");
            mAdapter = new SearchResultDetailsListAdapter(getActivity(), R.layout.frag_search_results_list, new ArrayList<ECSearchFile>());
            setListAdapter(mAdapter);
        }
        
        mSearch = searches.get(mPosition);
        if (mSearch.mSearchStatus != ECSearchStatus.STARTING && mSearch.mSearchStatus != ECSearchStatus.RUNNING) mApp.mECHelper.unRegisterFromECSearchList(this);

        Collection<ECSearchFile> newList = null;
        if (mSearch.mResults != null) newList = mSearch.mResults.resultMap.values();
        
        if (newList == null) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.updateECSearchList: No results, clearing adapter");
            mAdapter.clear();
        } else {
            int i = 0;
            
            ArrayList<Object> foundItems = new ArrayList<Object>();
            
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.updateECSearchList: Looking for results to be removed");
            while (i < mAdapter.getCount()) {
                ECSearchFile pOld = mAdapter.getItem(i);

                if (newList.contains(pOld)) {
                    foundItems.add(pOld);
                    i++;
                } else {
                    mAdapter.remove(pOld);
                }
            }
            
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.updateECSearchList: Adding new results");
            
            for (ECSearchFile p : newList) {
                if (! foundItems.contains(p)) mAdapter.add(p);
            }
            
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultDetailsFragment.updateECSearchList: Refreshing data");
            
            mAdapter.notifyDataSetChanged();
        }
    }
}
