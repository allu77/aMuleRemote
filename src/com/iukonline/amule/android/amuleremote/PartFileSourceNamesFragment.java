package com.iukonline.amule.android.amuleremote;

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItem;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileSourceName;



public class PartFileSourceNamesFragment extends ListFragment implements ECPartFileWatcher {

    
    interface RenameDialogContainer {
        public void showRenameDialog(String fileName);
    }

    
    byte[] mHash;
    ECPartFile mPartFile;
    AmuleControllerApplication mApp;
    
    SourceNamesAdapter mSourceNamesAdpater;
    
    String mLastSelected = null;

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
        
        View v = inflater.inflate(R.layout.partfile_sourcenames_fragment, container, false);
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
        
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.sourcenames_context, menu);
        
        AdapterView.AdapterContextMenuInfo aMenuInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;
        mLastSelected = mPartFile.getSourceNames().get(aMenuInfo.position).getName();

    } 
    
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        
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
        // TODO: Check if hash is the same...
        if (mPartFile == null && newECPartFile != null) {
            mPartFile = newECPartFile;
            mSourceNamesAdpater = new SourceNamesAdapter(getActivity(), R.layout.partfile_sourcenames_fragment, mPartFile.getSourceNames() );
            setListAdapter(mSourceNamesAdpater);
        }
        // We shouldn't need to re-assign mPartFile, since this should be the same modified...
        

        refreshView();
    }
    
    
    public void refreshView() {
        
        if (mSourceNamesAdpater != null) {
            mSourceNamesAdpater.notifyDataSetChanged();
        }
    }
    
    
    private class SourceNamesAdapter extends ArrayAdapter<ECPartFileSourceName> {
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
                    v = vi.inflate(R.layout.amuledl_sourcenames_row, null);
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
