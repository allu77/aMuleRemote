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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileSourceName;

import java.util.ArrayList;




public class PartFileSourceNamesFragment extends ListFragment implements ECPartFileWatcher {

    
    interface RenameDialogContainer {
        void showRenameDialog(String fileName);
    }

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
    byte[] mHash;
    ECPartFile mPartFile;
    AmuleRemoteApplication mApp;
    
    SourceNamesAdapter mSourceNamesAdpater;
    
    String mLastSelected = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mHash = getArguments().getByteArray(PartFileActivity.BUNDLE_PARAM_HASH);
        mApp = (AmuleRemoteApplication) getActivity().getApplication();
        
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
        
        View v = inflater.inflate(R.layout.frag_sourcenames, container, false);
        return v;
    }
    
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        registerForContextMenu(getListView());
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
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        // For context menu, the standard menu inflater must be used
        // https://groups.google.com/forum/?fromgroups=#!topic/actionbarsherlock/wQlIvR-jUYQ
        
        android.view.MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.sourcenames_context, menu);
        
        AdapterView.AdapterContextMenuInfo aMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        mLastSelected = mPartFile.getSourceNames().get(aMenuInfo.position).getName();

    } 
    
    
    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
        case R.id.sourcenames_context_rename:
            ((RenameDialogContainer) getActivity()).showRenameDialog(mLastSelected);
            break;
        }
        
        return super.onContextItemSelected(item);
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
                mSourceNamesAdpater = new SourceNamesAdapter(getActivity(), R.layout.frag_sourcenames, mPartFile.getSourceNames() );
                setListAdapter(mSourceNamesAdpater);
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
    
    
    public void refreshView() {
        
        if (mSourceNamesAdpater != null) {
            mSourceNamesAdpater.notifyDataSetChanged();
        }
    }
    
    
    private class SourceNamesAdapter extends ArrayAdapter<ECPartFileSourceName> {
        
        // FIXME Wrong usage of ArrayAdapter (see DlQueueFragment)
        // The fix can be complex as here we are using the same ECPartFile and ArrayList is update there... What happens here??
        
        private ArrayList<ECPartFileSourceName> items;
        
        public SourceNamesAdapter(Context context, int textViewResourceId, ArrayList<ECPartFileSourceName> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.part_sourcenames_row, null);
                }
                ECPartFileSourceName o = items.get(position);
                if (o != null) {
                    ((TextView) v.findViewById(R.id.amuledl_sourcenames_count)).setText(Integer.toString(o.getCount()));
                    ((TextView) v.findViewById(R.id.amuledl_sourcenames_name)).setText(o.getName());
                }
                return v;
        }
        
    }
    
}
