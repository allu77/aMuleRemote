/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.dlqueue;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.BuildConfig;
import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.AmuleWatcher.DlQueueWatcher;
import com.iukonline.amule.android.amuleremote.helpers.gui.GUIUtils;
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class DlQueueFragment extends ListFragment implements DlQueueWatcher {
    
    public interface DlQueueFragmentContainer {
        void partFileSelected(byte[] hash);
    }

    private final static String TAG = AmuleControllerApplication.AC_LOGTAG;
    private final static boolean DEBUG = BuildConfig.DEBUG;
    
    private final static String BUNDLE_SORT_BY = "sort";
    private final static String BUNDLE_SELECTED_ITEM = "selected";
    private final static String BUNDLE_CATEGORY_FILTER = "category";


    AmuleControllerApplication mApp;
    DlQueueFragmentContainer mActivity;

    
    HashMap<String, ECPartFile> mDlQueue;
    DownloadListAdapter mDlAdapter;
    byte mSortBy;
    long mCatId;
    int mRestoreSelected = -1;
    ECPartFileComparator mDlQueueComparator;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mApp = (AmuleControllerApplication) activity.getApplication();
        mActivity = (DlQueueFragmentContainer) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (savedInstanceState == null) {
            mSortBy = (byte) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_FILENAME);
        } else {
            mSortBy = savedInstanceState.getByte(BUNDLE_SORT_BY, AmuleControllerApplication.AC_SETTING_SORT_FILENAME);
            mRestoreSelected = savedInstanceState.getInt(BUNDLE_SELECTED_ITEM, -1);
            mCatId = savedInstanceState.getLong(BUNDLE_CATEGORY_FILTER, ECCategory.NEW_CATEGORY_ID);
        }

        if (DEBUG) Log.d(TAG, "Sort settings onCreate is " + mSortBy);
        mDlQueueComparator = new ECPartFileComparator(AmuleControllerApplication.getDlComparatorTypeFromSortSetting(mSortBy));
        setHasOptionsMenu(true);
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
        
        View v = inflater.inflate(R.layout.dlqueue_fragment, container, false);
        return v;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        registerAllListeners();
    }

    public void registerAllListeners() {
        updateDlQueue(mApp.mECHelper.registerForDlQueueUpdates(this));
    }
    
    @Override
    public void onPause() {
        
        mApp.mECHelper.unRegisterFromDlQueueUpdates(this);
        
        SharedPreferences.Editor e = mApp.mSettings.edit();
        e.putLong(AmuleControllerApplication.AC_SETTING_SORT, mSortBy);
        e.commit();
        if (DEBUG) Log.d(TAG, "Sort settings saved onPause is " + mSortBy);

        
        super.onPause();
    }
    
    
    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByte(BUNDLE_SORT_BY, mSortBy);
        outState.putInt(BUNDLE_SELECTED_ITEM, getSelectedItemPosition());
        outState.putLong(BUNDLE_CATEGORY_FILTER, mCatId);
    }
    
    

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.dlqueue_options, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
        case R.id.menu_dlqueue_opt_sort_filename:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_FILENAME;
            break;
        case R.id.menu_dlqueue_opt_sort_progress:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_PROGRESS;
            break;
        case R.id.menu_dlqueue_opt_sort_status:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_STATUS;
            break;
        case R.id.menu_dlqueue_opt_sort_transfered:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_TRANSFERED;
            break;
        case R.id.menu_dlqueue_opt_sort_size:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_SIZE;
            break;
        case R.id.menu_dlqueue_opt_sort_speed:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_SPEED;
            break;
        case R.id.menu_dlqueue_opt_sort_priority:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_PRIORITY;
            break;
        case R.id.menu_dlqueue_opt_sort_remaining:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_REMAINING;
            break;
        case R.id.menu_dlqueue_opt_sort_last_seen_complete:
            mSortBy = AmuleControllerApplication.AC_SETTING_SORT_LAST_SEE_COMPLETE;
            break;
        default:
            return super.onOptionsItemSelected(item);
        }
        
        mDlQueueComparator.setCompType(AmuleControllerApplication.getDlComparatorTypeFromSortSetting(mSortBy));
        
        refreshView();
        return true;
    }
    
    public void filterDlQueueByCategory(long catId) {
        mCatId = catId;
        updateDlQueue(mApp.mECHelper.getDlQueue());
    }
    

    public void refreshView() {

        if (mDlQueue == null) ((TextView) getView().findViewById(android.R.id.empty)).setText(R.string.dlqueue_server_not_queried);
        
        if (mDlAdapter != null) {
            if (DEBUG) Log.d(TAG, "refreshView sorting with type " + mDlQueueComparator.getCompType());
            mDlAdapter.sort(mDlQueueComparator);
            mDlAdapter.notifyDataSetChanged();
        } else {
            if (mDlQueue != null) {
                mDlAdapter = new DownloadListAdapter(getActivity(), R.layout.dlqueue_fragment, new ArrayList<ECPartFile> (mDlQueue.values()));
        
                if (DEBUG) Log.d(TAG, "refreshView sorting with type " + mDlQueueComparator.getCompType());
                mDlAdapter.sort(mDlQueueComparator);
                setListAdapter(mDlAdapter);
            } 
            
            if (mRestoreSelected > 0) {
                setSelection(mRestoreSelected);
                mRestoreSelected = -1;
            }
        }
        
    }   
    
    
    @Override
    public void updateDlQueue(HashMap<String, ECPartFile> newDlQueue) {

        
        if (newDlQueue == null) {
            //mDlAdapter = null; // RESET LIST...
            if (DEBUG) Log.d(TAG, "Resetting Dl Queue");
            if (mDlAdapter != null) mDlAdapter.clear();
        } else {
            if (DEBUG) Log.d(TAG, "Updating Dl Queue");
            
            // DlQueue fetched (if empty it will be non null anyway). Set the corect empty message.
            ((TextView) getListView().getEmptyView()).setText(R.string.dlqueue_empty);
            
            if (mDlAdapter != null) {
                
                int i = 0;
                
                ArrayList<ECPartFile> foundHash = new ArrayList<ECPartFile>();
                
                while (i < mDlAdapter.getCount()) {
                    ECPartFile pOld = mDlAdapter.getItem(i);

                    if ((mCatId == ECCategory.NEW_CATEGORY_ID || pOld.getCat() == mCatId) && newDlQueue.containsValue(pOld)) {
                        foundHash.add(pOld);
                        //dlQueueMap.remove(ECUtils.byteArrayToHexString(pOld.getHash()));
                        i++;
                    } else {
                        mDlAdapter.remove(pOld);
                    }
                }
                Iterator <ECPartFile> iter = newDlQueue.values().iterator();
                while (iter.hasNext()) {
                    ECPartFile p = iter.next();
                    if ((mCatId == ECCategory.NEW_CATEGORY_ID || p.getCat() == mCatId) && ! foundHash.contains(p)) mDlAdapter.add(p);
                }
                
                //mDlAdapter.sort(mDlQueueComparator);
            }
            
            if (DEBUG) Log.d(TAG, "Dl Queue Updated");
        }
        
        mDlQueue = newDlQueue;
        refreshView();
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (DEBUG) Log.d(TAG, "User selected item in position " + position + ", id " + id);
        mActivity.partFileSelected(mDlAdapter.getItem(position).getHash());
    }
    
    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    static class DlQueueViewHolder {
        TextView mFileName;
        TextView mTransfered;
        TextView mComment;
        TextView mSources;
        TextView mETA;
        ProgressBar mProgress;
        TextView mStatus;
        TextView mPrio;
    } 

    private class DownloadListAdapter extends ArrayAdapter<ECPartFile> {

        //private ArrayList<ECPartFile> items;
        
        public DownloadListAdapter(Context context, int textViewResourceId, ArrayList<ECPartFile> items) {
                super(context, textViewResourceId, items);
                //this.items = items;
        }
        
        /*
        public void setList (ArrayList<ECPartFile> items) {
            this.items = items;
        }*/

        
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                
            DlQueueViewHolder holder;
            
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.amuledl_row, null);
                holder = new DlQueueViewHolder();
                holder.mFileName = (TextView) v.findViewById(R.id.amuledl_row_filename);
                holder.mTransfered = (TextView) v.findViewById(R.id.amuledl_row_transferred);
                holder.mComment = (TextView) v.findViewById(R.id.amuledl_row_hascomments);
                
                holder.mSources = (TextView) v.findViewById(R.id.amuledl_row_sources);
                holder.mETA = (TextView) v.findViewById(R.id.amuledl_row_eta);
                holder.mProgress = (ProgressBar) v.findViewById(R.id.amuledl_row_progress);
                holder.mStatus = (TextView) v.findViewById(R.id.amuledl_row_status);
                holder.mPrio = (TextView) v.findViewById(R.id.amuledl_row_prio);
                
                v.setTag(holder);
            } else {
                holder = (DlQueueViewHolder) v.getTag();
            }
            
            
            if (position < getCount()) {
                ECPartFile o = getItem(position);
                if (o != null) {
                        float perc = ((float) o.getSizeDone()) * 100f / ((float) o.getSizeFull());
                        
                        holder.mFileName.setText(o.getFileName());
                        holder.mTransfered.setText(String.format("%s/%s (%.1f%%)", 
                                        GUIUtils.longToBytesFormatted(o.getSizeDone()), 
                                        GUIUtils.longToBytesFormatted(o.getSizeFull()),
                                        perc
                                        ));
                        
                        if (o.getCommentCount() == 0) {
                            holder.mComment.setVisibility(View.GONE);
                        } else {
                            holder.mComment.setVisibility(View.VISIBLE);
                            String commText = "!!";
                            int commColor = android.R.color.primary_text_dark;
                            switch (o.getWorstRating()) {
                            case ECPartFile.RATING_INVALID:
                                commColor = R.color.ratingInvalid;
                                break;
                            case ECPartFile.RATING_POOR:
                                commColor = R.color.ratingPoor;
                                commText = "!";
                                break;
                            case ECPartFile.RATING_FAIR:
                                commColor = R.color.ratingFair;
                                commText = "!";
                                break;
                            case ECPartFile.RATING_GOOD:
                                commText = "!";
                                commColor = R.color.ratingGood;
                                break;
                            case ECPartFile.RATING_EXCELLENT:
                                commColor = R.color.ratingExcellent;
                                break;
                            }
                            holder.mComment.setText(commText);
                            holder.mComment.setTextColor(getResources().getColor(commColor));
                        }
                        
                        int sourceCount = o.getSourceCount();
                        int sourceA4AF = o.getSourceA4AF();
                        int sourceXfer = o.getSourceXfer();
                        int sourceNotCur = o.getSourceNotCurrent();
                        
                        StringBuffer source = new StringBuffer(Integer.toString(sourceCount - sourceNotCur));
                        if (sourceNotCur > 0) source.append("/" + Integer.toString(sourceCount));
                        if (sourceA4AF > 0) source.append("+"+Integer.toString(sourceA4AF));
                        if (sourceXfer > 0) source.append(" (" + Integer.toString(sourceXfer) + ")");
                        
                        holder.mSources.setText(source.toString());
                        
                        holder.mETA.setText(GUIUtils.getETA(getContext(), o.getSizeFull() - o.getSizeDone(), o.getSpeed()));
                        
                        switch (o.getPrio()) {
                        case ECPartFile.PR_LOW:
                            holder.mPrio.setText(R.string.partfile_prio_low);
                            break;
                        case ECPartFile.PR_NORMAL:
                            holder.mPrio.setText(R.string.partfile_prio_normal);
                            break;
                        case ECPartFile.PR_HIGH:
                            holder.mPrio.setText(R.string.partfile_prio_high);
                            break;
                        case ECPartFile.PR_AUTO_LOW:
                            holder.mPrio.setText(R.string.partfile_prio_auto_low);
                            break;
                        case ECPartFile.PR_AUTO_NORMAL:
                            holder.mPrio.setText(R.string.partfile_prio_auto_normal);
                            break;
                        case ECPartFile.PR_AUTO_HIGH:
                            holder.mPrio.setText(R.string.partfile_prio_auto_high);
                            break;
                        default:
                            holder.mPrio.setText(R.string.partfile_prio_unknown);
                            break;
                        }
                        
                        int barResource = 0;
                                        
 
                        switch (o.getStatus()) {
                        
                        case ECPartFile.PS_ALLOCATING:
                            holder.mStatus.setText(R.string.partfile_status_allocating);
                            barResource = R.drawable.file_progress_waiting;
                            break;
                        case ECPartFile.PS_COMPLETE:
                            holder.mStatus.setText(R.string.partfile_status_complete);
                            barResource = R.drawable.file_progress_running;
                            break;
                        case ECPartFile.PS_COMPLETING:
                            holder.mStatus.setText(R.string.partfile_status_completing);
                            barResource = R.drawable.file_progress_running;
                            break;
                        case ECPartFile.PS_EMPTY:
                            holder.mStatus.setText(R.string.partfile_status_empty);
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        case ECPartFile.PS_ERROR:
                            holder.mStatus.setText(R.string.partfile_status_error);
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        case ECPartFile.PS_HASHING:
                        case ECPartFile.PS_WAITINGFORHASH:
                            holder.mStatus.setText(R.string.partfile_status_hashing);
                            barResource = R.drawable.file_progress_waiting;
                            break;
                        case ECPartFile.PS_INSUFFICIENT:
                            holder.mStatus.setText(R.string.partfile_status_insuffcient);
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        case ECPartFile.PS_PAUSED:
                            holder.mStatus.setText(R.string.partfile_status_paused);
                            barResource = R.drawable.file_progress_stopped;
                            break;
                        case ECPartFile.PS_READY:
                            if (sourceXfer > 0) {
                                holder.mStatus.setText(R.string.partfile_status_downloading);
                                holder.mStatus.append( " " + GUIUtils.longToBytesFormatted(o.getSpeed()) + "/s");
                                barResource = R.drawable.file_progress_running;
                            } else {
                                holder.mStatus.setText(R.string.partfile_status_waiting);
                                barResource = R.drawable.file_progress_waiting;
                            } 
                            break;
                        case ECPartFile.PS_UNKNOWN:
                            holder.mStatus.setText(R.string.partfile_status_unknown);
                            barResource = R.drawable.file_progress_blocked;
                            break;

                            
                        default:
                            holder.mStatus.setText("UNKNOWN-" + o.getStatus());
                            barResource = R.drawable.file_progress_blocked;
                            break;
                        }
                        
                        
                        // Note: get/set Bounds is needed. Otherwise the bar would disappear on scroll. WA found on
                        // http://stackoverflow.com/questions/2805866/android-progressbar-setprogressdrawable-only-works-once
                        
                        Rect bounds = holder.mProgress.getProgressDrawable().getBounds();
                        holder.mProgress.setProgressDrawable(getResources().getDrawable(barResource));
                        holder.mProgress.getProgressDrawable().setBounds(bounds);
  
                        
                        // Note: have to set progress to 0 in order to re-draw the progress
                        // http://stackoverflow.com/questions/4348032/android-progressbar-does-not-update-progress-view-drawable
                        
                        holder.mProgress.setProgress(0);
                        holder.mProgress.setProgress((int) (holder.mProgress.getMax() * perc / 100));
                }
            }
            return v;
        }
    
    
    }


    

    
}
