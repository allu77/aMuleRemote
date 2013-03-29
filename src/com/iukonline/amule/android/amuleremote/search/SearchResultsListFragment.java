package com.iukonline.amule.android.amuleremote.search;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECSearchListWatcher;

public class SearchResultsListFragment extends SherlockListFragment implements ECSearchListWatcher {
    
    
    public interface SearchResultsListFragmentContainter {
        public void viewResultDetails(int selected) ;
    }
    
    
    AmuleControllerApplication mApp;
    SearchResultsListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getActivity().getApplication();
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
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.onCreateView: Inflating view");
        View v = inflater.inflate(R.layout.frag_search_results_list, container, false);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.onCreateView: Inflated view");
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
    public void onListItemClick(ListView l, View v, int position, long id) {
        ((SearchResultsListFragmentContainter) getActivity()).viewResultDetails(position);
    }

    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    @Override
    public void updateECSearchList(ArrayList<SearchContainer> searches) {
        
        if (searches == null) {
            if (mAdapter != null) mAdapter.clear();
            return;
        }
        if (mAdapter == null) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: Creating new adapter");
            mAdapter = new SearchResultsListAdapter(getActivity(), R.layout.frag_search_results_list, new ArrayList<SearchContainer>());
            setListAdapter(mAdapter);
        }
        
        int searchesSize = searches.size();
        int adapterSize = mAdapter.getCount();
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: Search List update received. Size is " + searchesSize + " while current adapter size is " + adapterSize);
        
        if (searchesSize == 0 ) {
            if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: Empty search list");
            mAdapter.clear();
        } else {
            if (adapterSize == 0) {
                // Adapter is empty
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: Adapter is empty");
                
                // mAdapter.addAll(searches); Doesn't seem to work
                for (SearchContainer s : searches) {
                    mAdapter.add(s);
                }                
                
            } else if (searches.get(searchesSize - 1) != mAdapter.getItem(adapterSize - 1)) {
                // Eldest search item is different... => Two different search lists, let's clear and re-populate
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: Search List changed");
                mAdapter.clear();
                for (SearchContainer s : searches) {
                    mAdapter.add(s);
                }
            } else if (adapterSize < searchesSize) {
                // Eldest search item is the same, but size differs. We need to add new elements
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: Search List is bigger");
                for (int i = 0 ; i < searchesSize - adapterSize; i++) {
                    mAdapter.insert(searches.get(i), i);
                }
            } else if (adapterSize == searchesSize) {
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: Search List is the same");
                // Do nothing. The list is the same.
                mAdapter.notifyDataSetChanged();
            } else {
                // TODO Raise exception
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchResultsListFragment.updateECSearchList: This should nevere happen...");
                mAdapter.clear();
            }
        }
    }
}
