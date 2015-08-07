/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;

public class SearchInputFragment extends Fragment {

    private final static String TAG = AmuleControllerApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
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
        void startSearch(SearchContainer s) ;
    }
    
    AmuleControllerApplication mApp;
    
    EditText mFileNameEdit;
    Button mGoButton;
    View mAdvancedParamsLayout;
    Spinner mTypeSpinner;
    Spinner mFileTypeSpinner;
    EditText mExtensionEdit;
    EditText mMinSizeEdit;
    Spinner mMinSizeDimSpinner;
    EditText mMaxSizeEdit;
    Spinner mMaxSizeDimSpinner;
    EditText mAvailabilityEdit;
    
    MenuItem mShowAdvancedItem;
    MenuItem mHideAdvancedItem;
    
    private boolean mShowAdvanced = false;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        mApp = (AmuleControllerApplication) getActivity().getApplication();
        super.onCreate(savedInstanceState);
        
        setHasOptionsMenu(true);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        
        if (mFileNameEdit != null) outState.putString(BUNDLE_FILE_NAME, mFileNameEdit.getEditableText().toString());
        if (mTypeSpinner != null) outState.putInt(BUNDLE_SEARCH_TYPE, mTypeSpinner.getSelectedItemPosition());
        if (mFileTypeSpinner != null) outState.putInt(BUNDLE_FILE_TYPE, mFileTypeSpinner.getSelectedItemPosition());
        if (mExtensionEdit != null) outState.putString(BUNDLE_EXTENSION, mExtensionEdit.getEditableText().toString());
        if (mMinSizeEdit != null) outState.putString(BUNDLE_MIN_SIZE, mMinSizeEdit.getEditableText().toString());
        if (mMinSizeDimSpinner != null) outState.putInt(BUNDLE_MIN_SIZE_DIM, mMinSizeDimSpinner.getSelectedItemPosition());
        if (mMaxSizeEdit != null) outState.putString(BUNDLE_MAX_SIZE, mMaxSizeEdit.getEditableText().toString());
        if (mMaxSizeDimSpinner != null) outState.putInt(BUNDLE_MAX_SIZE_DIM, mMaxSizeDimSpinner.getSelectedItemPosition());
        if (mAvailabilityEdit != null) outState.putString(BUNDLE_AVAILABILITY, mAvailabilityEdit.getEditableText().toString());
        if (mAdvancedParamsLayout != null) outState.putBoolean(BUNDLE_SHOW_ADVANCED, mShowAdvanced);
        
        super.onSaveInstanceState(outState);
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
        
        if (DEBUG) Log.d(TAG, "SearchInputFragment.onCreateView: Inflating view");
        View v = inflater.inflate(R.layout.frag_search_input, container, false);
        if (DEBUG) Log.d(TAG, "SearchInputFragment.onCreateView: Inflated view");
        
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
        mAdvancedParamsLayout = v.findViewById(R.id.search_layout_advanced);

        if (savedInstanceState != null) {
            
            this.setInputFields(
                            savedInstanceState.getString(BUNDLE_FILE_NAME), 
                            savedInstanceState.getInt(BUNDLE_SEARCH_TYPE),
                            savedInstanceState.getInt(BUNDLE_FILE_TYPE),
                            savedInstanceState.getString(BUNDLE_EXTENSION),
                            savedInstanceState.getString(BUNDLE_MIN_SIZE),
                            savedInstanceState.getInt(BUNDLE_MIN_SIZE_DIM),
                            savedInstanceState.getString(BUNDLE_MAX_SIZE),
                            savedInstanceState.getInt(BUNDLE_MAX_SIZE_DIM),
                            savedInstanceState.getString(BUNDLE_AVAILABILITY)
                            );
            showAdvancedParams(savedInstanceState.getBoolean(BUNDLE_SHOW_ADVANCED));
        } else {
            showAdvancedParams(false);
            mTypeSpinner.setSelection((int) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SEARCH_TYPE, 0));
        }
        
        
        mGoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                
                if (DEBUG) Log.d(TAG, "SearchInputFragment.goButton.onClick: Fetching search parameters");
                
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
    
                // Hide softkeyboard on click
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mFileNameEdit.getWindowToken(), 0);
                
                if (DEBUG) Log.d(TAG, "SearchInputFragment.goButton.onClick: Starting search");
                ((SearchInputFragmentContainter) getActivity()).startSearch(s);
            }
        });
        
        
        return v;
        
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.search_input_options, menu);
        
        mShowAdvancedItem = menu.findItem(R.id.menu_search_input_opt_show_advanced_params);
        mHideAdvancedItem = menu.findItem(R.id.menu_search_input_opt_hide_advanced_params);
        
        super.onCreateOptionsMenu(menu, inflater);
    }
    

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (mShowAdvancedItem != null) mShowAdvancedItem.setVisible(! mShowAdvanced);
        if (mHideAdvancedItem != null) mHideAdvancedItem.setVisible(mShowAdvanced);
        
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_search_input_opt_clear_input_form:
            setInputFields(null);
            return true;
        case R.id.menu_search_input_opt_show_advanced_params:
            showAdvancedParams(true);
            return true;
        case R.id.menu_search_input_opt_hide_advanced_params:
            showAdvancedParams(false);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
        
    }

    
    private void showAdvancedParams(boolean show) {
        //mAdvancedParamsButton.setChecked(show);
        mShowAdvanced = show;
        mAdvancedParamsLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        getActivity().supportInvalidateOptionsMenu();
    }
    
    public void setInputFields(SearchContainer s) {
        if (s != null) {
            int minSizeDim = 0;
            while (s.mMinSize / Math.pow(1024, minSizeDim ) > 0) {
                minSizeDim++;
            }
            int maxSizeDim = 0;
            while (s.mMaxSize / Math.pow(1024, maxSizeDim ) > 0) {
                maxSizeDim++;
            }
            
            
            int fileType = 0;
            if (s.mType == null || s.mType.length() == 0) {
                fileType = 0;
            } else if (s.mType.equals("Arc")) {
                fileType = 1;
            } else if (s.mType.equals("Audio")) {
                fileType = 2;
            } else if (s.mType.equals("Iso")) {
                fileType = 3;
            } else if (s.mType.equals("Image")) {
                fileType = 4;
            } else if (s.mType.equals("Pro")) {
                fileType = 5;
            } else if (s.mType.equals("Doc")) {
                fileType = 6;
            } else if (s.mType.equals("Video")) {
                fileType = 7;
            } 
            
            String minSize = s.mMinSize < 0 ? "" : Integer.toString((int) (s.mMinSize / Math.pow(1024, minSizeDim)));
            String maxSize = s.mMaxSize < 0 ? "" : Integer.toString((int) (s.mMaxSize / Math.pow(1024, maxSizeDim)));
            String availability = s.mAvailability < 0 ? "" : Long.toString(s.mAvailability);
            
            setInputFields(s.mFileName, (int) s.mSearchType, fileType, s.mExtension, minSize, minSizeDim, maxSize, maxSizeDim, availability);

        } else {
            setInputFields("", (int) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SEARCH_TYPE, 0), 0, "", "", 0, "", 0, "");
        }
    }
    
    public void setInputFields(String fileName, int searchType, int fileType, String extension, String minSize, int minSizeDim, String maxSize, int maxSizeDim, String availability) {
        if (mFileNameEdit != null) mFileNameEdit.setText(fileName);
        if (mTypeSpinner != null) mTypeSpinner.setSelection(searchType);
        if (mFileTypeSpinner != null) mFileTypeSpinner.setSelection(fileType);
        if (mExtensionEdit != null) mExtensionEdit.setText(extension);
        if (mMinSizeEdit != null) mMinSizeEdit.setText(minSize);
        if (mMinSizeDimSpinner != null) mMinSizeDimSpinner.setSelection(minSizeDim);
        if (mMaxSizeEdit != null) mMaxSizeEdit.setText(maxSize);
        if (mMaxSizeDimSpinner != null) mMaxSizeDimSpinner.setSelection(maxSizeDim);
        if (mAvailabilityEdit != null) mAvailabilityEdit.setText(availability);
    }
}
