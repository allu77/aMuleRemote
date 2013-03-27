package com.iukonline.amule.android.amuleremote.search;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ToggleButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.R;

public class SearchInputFragment extends SherlockFragment {
    
    private final static String BUNDLE_FILE_NAME = "file_name";
    private final static String BUNDLE_SEARCH_TYPE = "search_type";
    private final static String BUNDLE_SHOW_ADVANCED = "show_advanced";
    private final static String BUNDLE_FILE_TYPE = "file_type";
    private final static String BUNDLE_EXTENSION = "extension";
    private final static String BUNDLE_MIN_SIZE = "min_size";
    private final static String BUNDLE_MIN_SIZE_DIM = "min_size_dim";
    private final static String BUNDLE_MAX_SIZE = "max_size";
    private final static String BUNDLE_MAX_SIZE_DIM = "max_size_dim";
    private final static String BUNDLE_AVAILABILITY = "availability";
    
    public interface SearchInputFragmentContainter {
        public void startSearch(SearchContainer s) ;
    }
    
    AmuleControllerApplication mApp;
    
    EditText mFileNameEdit;
    Button mGoButton;
    ToggleButton mAdvancedParamsButton;
    View mAdvancedParamsLayout;
    Spinner mTypeSpinner;
    Spinner mFileTypeSpinner;
    EditText mExtensionEdit;
    EditText mMinSizeEdit;
    Spinner mMinSizeDimSpinner;
    EditText mMaxSizeEdit;
    Spinner mMaxSizeDimSpinner;
    EditText mAvailabilityEdit;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getActivity().getApplication();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        
        if (mFileNameEdit != null) outState.putString(BUNDLE_FILE_NAME, mFileNameEdit.getEditableText().toString());
        if (mTypeSpinner != null) outState.putInt(BUNDLE_SEARCH_TYPE, mTypeSpinner.getSelectedItemPosition());
        if (mAdvancedParamsButton != null) outState.putBoolean(BUNDLE_SHOW_ADVANCED, mAdvancedParamsButton.isChecked());
        if (mFileTypeSpinner != null) outState.putInt(BUNDLE_FILE_TYPE, mFileTypeSpinner.getSelectedItemPosition());
        if (mExtensionEdit != null) outState.putString(BUNDLE_EXTENSION, mExtensionEdit.getEditableText().toString());
        if (mMinSizeEdit != null) outState.putString(BUNDLE_MIN_SIZE, mMinSizeEdit.getEditableText().toString());
        if (mMinSizeDimSpinner != null) outState.putInt(BUNDLE_MIN_SIZE_DIM, mMinSizeDimSpinner.getSelectedItemPosition());
        if (mMaxSizeEdit != null) outState.putString(BUNDLE_MAX_SIZE, mMaxSizeEdit.getEditableText().toString());
        if (mMaxSizeDimSpinner != null) outState.putInt(BUNDLE_MAX_SIZE_DIM, mMaxSizeDimSpinner.getSelectedItemPosition());
        if (mAvailabilityEdit != null) outState.putString(BUNDLE_AVAILABILITY, mAvailabilityEdit.getEditableText().toString());
        
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    
    @Override
    public void onPause() {
        
        if (mTypeSpinner != null) {
            SharedPreferences.Editor e = mApp.mSettings.edit();
            e.putLong(AmuleControllerApplication.AC_SETTING_SEARCH_TYPE, mTypeSpinner.getSelectedItemPosition());
            e.commit();
        }

        super.onPause();
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
        
        mFileTypeSpinner = (Spinner) v.findViewById(R.id.search_file_type);
        mExtensionEdit = (EditText) v.findViewById(R.id.search_extension);
        mMinSizeEdit = (EditText) v.findViewById(R.id.search_min_size);
        mMinSizeDimSpinner = (Spinner) v.findViewById(R.id.search_min_size_dim);
        mMaxSizeEdit = (EditText) v.findViewById(R.id.search_max_size);
        mMaxSizeDimSpinner = (Spinner) v.findViewById(R.id.search_max_size_dim);
        mAvailabilityEdit = (EditText) v.findViewById(R.id.search_availability);
        
        mGoButton = (Button) v.findViewById(R.id.search_button_go);
        mAdvancedParamsButton = (ToggleButton) v.findViewById(R.id.search_button_advanced_params);
        mAdvancedParamsLayout = v.findViewById(R.id.search_layout_advanced);

        if (savedInstanceState != null) {
            mFileNameEdit.setText(savedInstanceState.getString(BUNDLE_FILE_NAME));
            mTypeSpinner.setSelection(savedInstanceState.getInt(BUNDLE_SEARCH_TYPE));
            mFileTypeSpinner.setSelection(savedInstanceState.getInt(BUNDLE_FILE_TYPE));
            mMinSizeEdit.setText(savedInstanceState.getString(BUNDLE_MIN_SIZE));
            mMinSizeDimSpinner.setSelection(savedInstanceState.getInt(BUNDLE_MIN_SIZE_DIM));
            mMaxSizeEdit.setText(savedInstanceState.getString(BUNDLE_MAX_SIZE));
            mMaxSizeDimSpinner.setSelection(savedInstanceState.getInt(BUNDLE_MAX_SIZE_DIM));
            mAvailabilityEdit.setText(savedInstanceState.getString(BUNDLE_AVAILABILITY));
            mAdvancedParamsButton.setChecked(savedInstanceState.getBoolean(BUNDLE_SHOW_ADVANCED));
        } else {
            mAdvancedParamsButton.setChecked(false);
            mTypeSpinner.setSelection((int) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SEARCH_TYPE, 0));
        }
        
        showAdvancedParams(mAdvancedParamsButton.isChecked());
        
        mGoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchInputFragment.goButton.onClick: Fetching search parameters");
                
                SearchContainer s = new SearchContainer();
                s.mFileName = mFileNameEdit.getText().toString();
                s.mSearchType = (byte) mTypeSpinner.getSelectedItemPosition();

                /* From include/tags/FileTags.h   
                // Media values for FT_FILETYPE
                   #define ED2KFTSTR_AUDIO                 wxT("Audio")    
                   #define ED2KFTSTR_VIDEO                 wxT("Video")    
                   #define ED2KFTSTR_IMAGE                 wxT("Image")    
                   #define ED2KFTSTR_DOCUMENT              wxT("Doc")      
                   #define ED2KFTSTR_PROGRAM               wxT("Pro")      
                   #define ED2KFTSTR_ARCHIVE               wxT("Arc")      // *Mule internal use only
                   #define ED2KFTSTR_CDIMAGE               wxT("Iso")      // *Mule internal use only
                   
                   */

                switch (mFileTypeSpinner.getSelectedItemPosition()) {
                case 1:
                    s.mType = "Arc";
                    break;
                case 2:
                    s.mType = "Audio";
                    break;
                case 3:
                    s.mType = "Iso";
                    break;
                case 4:
                    s.mType = "Image";
                    break;
                case 5:
                    s.mType = "Pro";
                    break;
                case 6:
                    s.mType = "Doc";
                    break;
                case 7:
                    s.mType = "Video";
                    break;
                }
                
                String extension = mExtensionEdit.getEditableText().toString();
                if (extension.length() > 0) s.mExtension = extension;

                long minSize;
                long maxSize;
                long availability;
                try {
                    minSize = Long.parseLong(mMinSizeEdit.getEditableText().toString());
                    for (int i = 0; i < mMinSizeDimSpinner.getSelectedItemPosition(); i++) minSize *= 1024L;
                    if (minSize > 0L) s.mMinSize = minSize;
                } catch (NumberFormatException e) {
                    // Do Nothing
                }
                try {
                    maxSize = Long.parseLong(mMaxSizeEdit.getEditableText().toString());
                    for (int i = 0; i < mMaxSizeDimSpinner.getSelectedItemPosition(); i++) maxSize *= 1024L;
                    if (maxSize > 0) s.mMaxSize = maxSize;
                } catch (NumberFormatException e) {
                    // Do Nothing
                }
                try {
                    availability = Long.parseLong(mAvailabilityEdit.getEditableText().toString());
                    if (availability > 0) s.mAvailability = availability;
                } catch (NumberFormatException e) {
                    // Do Nothing
                }
                
                if (mApp.enableLog) Log.d(AmuleControllerApplication.AC_LOGTAG, "SearchInputFragment.goButton.onClick: Starting search");
                ((SearchInputFragmentContainter) getActivity()).startSearch(s);
            }
        });
        
        mAdvancedParamsButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showAdvancedParams(mAdvancedParamsButton.isChecked());
            }
        });
        
        
        
        return v;
        
    }
    
    private void showAdvancedParams(boolean show) {
        //mAdvancedParamsButton.setChecked(show);
        mAdvancedParamsLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

}
