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

package com.iukonline.amule.android.amuleremote.search;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECSearchListWatcher;
import com.iukonline.amule.android.amuleremote.search.SearchContainer.ECSearchStatus;
import com.iukonline.amule.ec.ECSearchFile;
import com.iukonline.amule.ec.ECSearchFile.ECSearchFileComparator;

import java.util.ArrayList;
import java.util.Collection;

public class SearchResultDetailsFragment extends ListFragment implements ECSearchListWatcher {

    public interface SearchResultDetailsFragmentContainter {
        void startSearchResult(ECSearchFile sf) ;
    }

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    private final static int SETTINGS_SORT_FILENAME = 1;
    private final static int SETTINGS_SORT_SIZE = 2;
    private final static int SETTINGS_SORT_SOURCES = 3;
    private final static int SETTINGS_SORT_SOURCES_COMPLETE = 4;
    
    private final static String BUNDLE_SORT_BY = "sort_by";
    
    private int mSortBy = SETTINGS_SORT_SOURCES;
    
    AmuleRemoteApplication mApp;
    SearchResultDetailsListAdapter mAdapter;
    ECSearchFileComparator mComparator;
    
    int mPosition;
    SearchContainer mSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleRemoteApplication) getActivity().getApplication();
        // mSearch = mApp.mECHelper.getSearchItem(mPosition);
        if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.onCreate: Will show details for position " + mPosition);
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            mSortBy = savedInstanceState.getInt(BUNDLE_SORT_BY, SETTINGS_SORT_SOURCES);
        }
        
        setHasOptionsMenu(true);
        
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        
        outState.putInt(BUNDLE_SORT_BY, mSortBy);
        
        super.onSaveInstanceState(outState);
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
        
        if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.onCreateView: Inflating view");
        View v = inflater.inflate(R.layout.frag_search_result_details_list, container, false);
        if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.onCreateView: Inflated view");
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
        ((SearchResultDetailsFragmentContainter) getActivity()).startSearchResult(mAdapter.getItem(position));
    }
    
    
    
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.searches_result_details_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_search_details_opt_sort_filename:
            mSortBy = SETTINGS_SORT_FILENAME;
            if (mComparator != null) mComparator.setCompType(ECSearchFileComparator.ComparatorType.FILENAME);
            break;
        case R.id.menu_search_details_opt_sort_size:
            mSortBy = SETTINGS_SORT_SIZE;
            if (mComparator != null) mComparator.setCompType(ECSearchFileComparator.ComparatorType.SIZE);
            break;
        case R.id.menu_search_details_opt_sort_sources:
            mSortBy = SETTINGS_SORT_SOURCES;
            if (mComparator != null) mComparator.setCompType(ECSearchFileComparator.ComparatorType.SOURCES_COUNT);
            break;
        case R.id.menu_search_details_opt_sort_sources_complete:
            mSortBy = SETTINGS_SORT_SOURCES_COMPLETE;
            if (mComparator != null) mComparator.setCompType(ECSearchFileComparator.ComparatorType.SOURCES_XFER);
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        
        if (mAdapter != null && mComparator != null) mAdapter.sort(mComparator);
        return true;
    }

    
    
    

    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    @Override
    public UpdateResult updateECSearchList(ArrayList<SearchContainer> searches) {

        UpdateResult retVal = UpdateResult.DO_NOTHING;

        if (searches == null || mPosition >= searches.size()) {
            getActivity().finish();
            return retVal;
        }
        
        if (mAdapter == null) {
            if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.updateECSearchList: Creating new adapter");
            mAdapter = new SearchResultDetailsListAdapter(getActivity(), R.layout.frag_search_results_list, new ArrayList<ECSearchFile>());
            setListAdapter(mAdapter);
            switch (mSortBy) {
            case SETTINGS_SORT_FILENAME:
                mComparator = new ECSearchFileComparator(ECSearchFileComparator.ComparatorType.FILENAME);
                break;
            case SETTINGS_SORT_SIZE:
                mComparator = new ECSearchFileComparator(ECSearchFileComparator.ComparatorType.SIZE);
                break;
            case SETTINGS_SORT_SOURCES:
                mComparator = new ECSearchFileComparator(ECSearchFileComparator.ComparatorType.SOURCES_COUNT);
                break;
            case SETTINGS_SORT_SOURCES_COMPLETE:
                mComparator = new ECSearchFileComparator(ECSearchFileComparator.ComparatorType.SOURCES_XFER);
                break;
            default:
                mComparator = new ECSearchFileComparator(ECSearchFileComparator.ComparatorType.SOURCES_COUNT);
                break;
            }
        }

        
        mSearch = searches.get(mPosition);
        if (mSearch.mSearchStatus != ECSearchStatus.STARTING && mSearch.mSearchStatus != ECSearchStatus.RUNNING) {
            ((TextView) getListView().getEmptyView()).setText(R.string.search_no_results);
            //mApp.mECHelper.unRegisterFromECSearchList(this);
            retVal = UpdateResult.UNREGISTER;
        }

        Collection<ECSearchFile> newList = null;
        if (mSearch.mResults != null) newList = mSearch.mResults.resultMap.values();
        
        if (newList == null) {
            if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.updateECSearchList: No results, clearing adapter");
            mAdapter.clear();
        } else {
            int i = 0;
            
            ArrayList<Object> foundItems = new ArrayList<Object>();
            
            if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.updateECSearchList: Looking for results to be removed");
            while (i < mAdapter.getCount()) {
                ECSearchFile pOld = mAdapter.getItem(i);

                if (newList.contains(pOld)) {
                    foundItems.add(pOld);
                    i++;
                } else {
                    mAdapter.remove(pOld);
                }
            }
            
            if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.updateECSearchList: Adding new results");
            
            for (ECSearchFile p : newList) {
                if (! foundItems.contains(p)) mAdapter.add(p);
            }
            
            if (DEBUG) Log.d(TAG, "SearchResultDetailsFragment.updateECSearchList: Refreshing data");
            
            mAdapter.sort(mComparator);
            mAdapter.notifyDataSetChanged();
        }
        return retVal;
    }

}
