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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.AmuleRemoteApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComment;

import java.util.ArrayList;



public class PartFileCommentsFragment extends ListFragment implements ECPartFileWatcher {

    private final static String TAG = AmuleRemoteApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;

    byte[] mHash;
    ECPartFile mPartFile;
    AmuleRemoteApplication mApp;
    
    CommentsAdapter mCommentsAdpater;

    
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
        
        View v = inflater.inflate(R.layout.frag_comments, container, false);
        return v;
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
                mCommentsAdpater = new CommentsAdapter(getActivity(), R.layout.frag_sourcenames, mPartFile.getComments() );
                setListAdapter(mCommentsAdpater);
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
        
        if (mCommentsAdpater != null) {
            mCommentsAdpater.notifyDataSetChanged();
        }
    }
    
    
    private class CommentsAdapter extends ArrayAdapter<ECPartFileComment> {
        private ArrayList<ECPartFileComment> items;
        
        public CommentsAdapter(Context context, int textViewResourceId, ArrayList<ECPartFileComment> items) {
            super(context, textViewResourceId, items);
            this.items = items;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.part_comments_row, null);
                }
                int ratingColor = android.R.color.primary_text_dark;
                ECPartFileComment o = items.get(position);
                if (o != null) {
                    int ratingRes = R.string.partfile_deatil_comments_rating_notrated;
                    switch (o.getRating()) {
                    case ECPartFile.RATING_INVALID:
                        ratingRes = R.string.partfile_deatil_comments_rating_invalid;
                        ratingColor = R.color.ratingInvalid;
                        break;
                    case ECPartFile.RATING_POOR:
                        ratingRes = R.string.partfile_deatil_comments_rating_poor;
                        ratingColor = R.color.ratingPoor;
                        break;
                    case ECPartFile.RATING_FAIR:
                        ratingRes = R.string.partfile_deatil_comments_rating_fair;
                        ratingColor = R.color.ratingFair;
                        break;
                    case ECPartFile.RATING_GOOD:
                        ratingRes = R.string.partfile_deatil_comments_rating_good;
                        ratingColor = R.color.ratingGood;
                        break;
                    case ECPartFile.RATING_EXCELLENT:
                        ratingRes = R.string.partfile_deatil_comments_rating_excellent;
                        ratingColor = R.color.ratingExcellent;
                        break;

                    }
                    ((TextView) v.findViewById(R.id.amuledl_comments_row_rating)).setText(ratingRes);
                    ((TextView) v.findViewById(R.id.amuledl_comments_row_rating)).setTextColor(getResources().getColor(ratingColor));
                    ((TextView) v.findViewById(R.id.amuledl_comments_row_author)).setText(o.getAuthor());
                    ((TextView) v.findViewById(R.id.amuledl_comments_row_filename)).setText(o.getSourceName());
                    TextView commentTv = ((TextView) v.findViewById(R.id.amuledl_comments_row_comment));
                    if (o.getComment() != null && o.getComment().length() > 0) {
                        commentTv.setText(o.getComment());
                        commentTv.setVisibility(View.VISIBLE);
                    } else {
                        commentTv.setVisibility(View.GONE);
                    }
                }
                return v;
        }
        
    }
    
}
