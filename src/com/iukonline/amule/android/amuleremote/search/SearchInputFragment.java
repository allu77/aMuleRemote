package com.iukonline.amule.android.amuleremote.search;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.actionbarsherlock.app.SherlockFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.R;

public class SearchInputFragment extends SherlockFragment {
    
    public interface SearchInputFragmentContainter {
        public void startSearch(SearchContainer s) ;
    }
    
    AmuleControllerApplication mApp;
    
    EditText mFileNameEdit;
    Button mGoButton;
    Spinner mTypeSpinner;
    
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
        
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchInputFragment.onCreateView: Inflating view");
        View v = inflater.inflate(R.layout.frag_search_input, container, false);
        if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchInputFragment.onCreateView: Inflated view");
        
        mFileNameEdit = (EditText) v.findViewById(R.id.search_file_name);
        mTypeSpinner = (Spinner) v.findViewById(R.id.search_search_type);
        mGoButton = (Button) v.findViewById(R.id.search_button_go);
        
        mGoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchInputFragment.goButton.onClick: Fetching search parameters");
                
                SearchContainer s = new SearchContainer();
                s.mFileName = mFileNameEdit.getText().toString();
                s.mSearchType = (byte) mTypeSpinner.getSelectedItemPosition(); 

                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchInputFragment.goButton.onClick: Starting search");
                ((SearchInputFragmentContainter) getActivity()).startSearch(s);
            }
        });
        return v;
    }

}
