package com.iukonline.amule.android.amuleremote;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.echelper.AmuleWatcher.DlQueueWatcher;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComparator;

public class DlQueueFragment extends ListFragment implements DlQueueWatcher {
    
    public interface DlQueueFragmentContainer {
        public void partFileSelected(byte[] hash);
    }
    
    private final static String BUNDLE_SORT_BY = "sort";
    private final static String BUNDLE_SELECTED_ITEM = "selected";


    AmuleControllerApplication mApp;

    
    ArrayList <ECPartFile> mDlQueue;
    DownloadListAdapter mDlAdapter;
    byte mSortBy;
    int mRestoreSelected = -1;
    ECPartFileComparator mDlQueueComparator;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mApp = (AmuleControllerApplication) getActivity().getApplication();
        
        if (savedInstanceState == null) {
            mSortBy = (byte) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_FILENAME);
            //mSortBy = AmuleControllerApplication.AC_SETTING_SORT_FILENAME;
        } else {
            mSortBy = savedInstanceState.getByte(BUNDLE_SORT_BY, AmuleControllerApplication.AC_SETTING_SORT_FILENAME);
            mRestoreSelected = savedInstanceState.getInt(BUNDLE_SELECTED_ITEM, -1);
        }
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

        updateDlQueue(mApp.mECHelper.registerForDlQueueUpdates(this));
    }
    
    @Override
    public void onPause() {
        
        mApp.mECHelper.unRegisterFromDlQueueUpdates(this);
        
        SharedPreferences.Editor e = mApp.mSettings.edit();
        e.putLong(AmuleControllerApplication.AC_SETTING_SORT, AmuleControllerApplication.AC_SETTING_SORT_PROGRESS);
        e.commit();
        
        super.onPause();
    }
    
    
    @Override
    public void onSaveInstanceState (Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByte(BUNDLE_SORT_BY, mSortBy);
        //outState.putInt(BUNDLE_SELECTED_ITEM,this.getSelectedItemPosition());
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
        default:
            return super.onOptionsItemSelected(item);
        }
        
        mDlQueueComparator = new ECPartFileComparator(AmuleControllerApplication.getDlComparatorTypeFromSortSetting(mSortBy));
        refreshView();
        return true;
    }
    
    
    
    

    public void refreshView() {
        
        if (mDlAdapter != null) {
            //Toast.makeText(mApp, "Notify data set changed", Toast.LENGTH_LONG).show();
            mDlAdapter.notifyDataSetChanged();
        }

        
        if (mDlQueue != null) {
            //ECPartFileComparator c = new ECPartFileComparator((byte) mApp.mSettings.getLong(AmuleControllerApplication.AC_SETTING_SORT, ECPartFileComparator.AC_SETTING_SORT_FILENAME));
            Collections.sort(mDlQueue, mDlQueueComparator);
            DownloadListAdapter dlListAdapter = new DownloadListAdapter(getActivity(), R.layout.dlqueue_fragment, mDlQueue );
            
            setListAdapter(dlListAdapter);
        } else {
            ((TextView) getView().findViewById(android.R.id.empty)).setText(R.string.dlqueue_server_not_queried);
        }
    }   
    
    
    @Override
    public void updateDlQueue(ArrayList<ECPartFile> newDlQueue) {

        boolean createAdapter = false;
        
        if (newDlQueue != null) {
            ((TextView) getListView().getEmptyView()).setText(R.string.dlqueue_empty);
/*            if (mDlQueue == null) {
                createAdapter = true;
            }*/
        }
        
        mDlQueue = newDlQueue;
        
        if (mDlQueue != null) {
            Collections.sort(mDlQueue, mDlQueueComparator);
            
            // TODO: Capire perchè non funzionava così quando si rimuoveva un elemento..
            
/*            if (mDlAdapter != null) {
                mDlAdapter.setList(mDlQueue);
            } else {
                mDlAdapter = new DownloadListAdapter(getActivity(), R.layout.dlqueue_fragment, mDlQueue );
                setListAdapter(mDlAdapter);
                
            }
            if (mRestoreSelected > 0) {
                //setSelection(mRestoreSelected);
                mRestoreSelected = -1;
            }
            
  */          
            
/*            if (createAdapter) {
                mDlAdapter = new DownloadListAdapter(getActivity(), R.layout.dlqueue_fragment, mDlQueue );
                setListAdapter(mDlAdapter);
                if (mRestoreSelected > 0) {
                    //setSelection(mRestoreSelected);
                    mRestoreSelected = -1;
                }
            }*/
            
        }
        refreshView();
    }
    
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
       
        ((DlQueueFragmentContainer) getActivity()).partFileSelected(mDlQueue.get(position).getHash());
        
    }
    
    @Override
    public String getWatcherId() {
        return this.getClass().getName();
    }

    

    private class DownloadListAdapter extends ArrayAdapter<ECPartFile> {

        private ArrayList<ECPartFile> items;

        public DownloadListAdapter(Context context, int textViewResourceId, ArrayList<ECPartFile> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }
        
        public void setList (ArrayList<ECPartFile> items) {
            this.items = items;
            this.notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                //Toast.makeText(mApp, "getView pos: " + position, Toast.LENGTH_SHORT).show();
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.amuledl_row, null);
                }
                if (position < items.size()) {
                    ECPartFile o = items.get(position);
                    if (o != null) {
                            //Toast.makeText(mApp, "o not null", Toast.LENGTH_SHORT).show();
                            float perc = ((float) o.getSizeDone()) * 100f / ((float) o.getSizeFull());
                        
                            ((TextView) v.findViewById(R.id.amuledl_row_filename)).setText(o.getFileName());
                            ((TextView) v.findViewById(R.id.amuledl_row_transferred)).setText(String.format("%s/%s (%.1f%%)", 
                                            GUIUtils.longToBytesFormatted(o.getSizeDone()), 
                                            GUIUtils.longToBytesFormatted(o.getSizeFull()),
                                            perc
                                            ));
                            
                            
                            
                            
                            TextView commView = (TextView) v.findViewById(R.id.amuledl_row_hascomments);
                            if (o.getCommentCount() == 0) {
                                commView.setVisibility(View.GONE);
                            } else {
                                commView.setVisibility(View.VISIBLE);
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
                                commView.setText(commText);
                                commView.setTextColor(getResources().getColor(commColor));
                            }
                            
                            v.findViewById(R.id.amuledl_row_hascomments).setVisibility(o.getCommentCount() > 0 ? View.VISIBLE : View.GONE);
                            
                            int sourceCount = o.getSourceCount();
                            int sourceA4AF = o.getSourceA4AF();
                            int sourceXfer = o.getSourceXfer();
                            int sourceNotCur = o.getSourceNotCurrent();
                            
                            StringBuffer source = new StringBuffer(Integer.toString(sourceCount - sourceNotCur));
                            if (sourceNotCur > 0) source.append("/" + Integer.toString(sourceCount));
                            if (sourceA4AF > 0) source.append("+"+Integer.toString(sourceA4AF));
                            if (sourceXfer > 0) source.append(" (" + Integer.toString(sourceXfer) + ")");
                            
                            ((TextView) v.findViewById(R.id.amuledl_row_sources)).setText(source.toString());
                            
                            ((TextView) v.findViewById(R.id.amuledl_row_eta)).setText(GUIUtils.getETA(o.getSizeFull() - o.getSizeDone(), o.getSpeed()));
                            
                            ProgressBar bar = (ProgressBar) v.findViewById(R.id.amuledl_row_progress);
                            
                            TextView tvStatus = (TextView) v.findViewById(R.id.amuledl_row_status);
                            TextView tvPrio = (TextView) v.findViewById(R.id.amuledl_row_prio);
                            
                            switch (o.getPrio()) {
                            case ECPartFile.PR_LOW:
                                tvPrio.setText(R.string.partfile_prio_low);
                                break;
                            case ECPartFile.PR_NORMAL:
                                tvPrio.setText(R.string.partfile_prio_normal);
                                break;
                            case ECPartFile.PR_HIGH:
                                tvPrio.setText(R.string.partfile_prio_high);
                                break;
                            case ECPartFile.PR_AUTO_LOW:
                                tvPrio.setText(R.string.partfile_prio_auto_low);
                                break;
                            case ECPartFile.PR_AUTO_NORMAL:
                                tvPrio.setText(R.string.partfile_prio_auto_normal);
                                break;
                            case ECPartFile.PR_AUTO_HIGH:
                                tvPrio.setText(R.string.partfile_prio_auto_high);
                                break;
                            default:
                                tvPrio.setText(R.string.partfile_prio_unknown);
                                break;
                            }
                            
                            int barResource = 0;
                                            
     
                            switch (o.getStatus()) {
                            
                            case ECPartFile.PS_ALLOCATING:
                                // TODO What's this?
                                tvStatus.setText(R.string.partfile_status_allocating);
                                barResource = R.drawable.file_progress_waiting;
                                break;
                            case ECPartFile.PS_COMPLETE:
                                tvStatus.setText(R.string.partfile_status_complete);
                                barResource = R.drawable.file_progress_running;
                                break;
                            case ECPartFile.PS_COMPLETING:
                                tvStatus.setText(R.string.partfile_status_completing);
                                barResource = R.drawable.file_progress_running;
                                break;
                            case ECPartFile.PS_EMPTY:
                                // TODO: Check why this happens...
                                tvStatus.setText(R.string.partfile_status_empty);
                                barResource = R.drawable.file_progress_blocked;
                                break;
                            case ECPartFile.PS_ERROR:
                                tvStatus.setText(R.string.partfile_status_error);
                                barResource = R.drawable.file_progress_blocked;
                                break;
                            case ECPartFile.PS_HASHING:
                                tvStatus.setText(R.string.partfile_status_hashing);
                                barResource = R.drawable.file_progress_waiting;
                                break;
                            case ECPartFile.PS_INSUFFICIENT:
                                // TODO What's this?
                                tvStatus.setText(R.string.partfile_status_insuffcient);
                                barResource = R.drawable.file_progress_blocked;
                                break;
                            case ECPartFile.PS_PAUSED:
                                tvStatus.setText(R.string.partfile_status_paused);
                                barResource = R.drawable.file_progress_stopped;
                                break;
                            case ECPartFile.PS_READY:
                                if (sourceXfer > 0) {
                                    tvStatus.setText(R.string.partfile_status_downloading);
                                    tvStatus.append( " " + GUIUtils.longToBytesFormatted(o.getSpeed()) + "/s");
                                    barResource = R.drawable.file_progress_running;
                                } else {
                                    tvStatus.setText(R.string.partfile_status_waiting);
                                    barResource = R.drawable.file_progress_waiting;
                                } 
                                break;
                            case ECPartFile.PS_UNKNOWN:
                                tvStatus.setText(R.string.partfile_status_unknown);
                                barResource = R.drawable.file_progress_blocked;
                                break;
                            case ECPartFile.PS_WAITINGFORHASH:
                                // TODO What's this?
                                tvStatus.setText(R.string.partfile_status_waitingforhash);
                                barResource = R.drawable.file_progress_waiting;
                                break;
                                
                            default:
                                tvStatus.setText("UNKNOWN-" + o.getStatus());
                                barResource = R.drawable.file_progress_blocked;
                                break;
                            }
                            
                            
                            // Note: get/set Bounds is needed. Otherwise the bar would disappear on scroll. WA found on
                            // http://stackoverflow.com/questions/2805866/android-progressbar-setprogressdrawable-only-works-once
                            
                            Rect bounds = bar.getProgressDrawable().getBounds();
                            bar.setProgressDrawable(getResources().getDrawable(barResource));
                            bar.getProgressDrawable().setBounds(bounds);
      
                            
                            // Note: have to set progress to 0 in order to re-draw the progress
                            // http://stackoverflow.com/questions/4348032/android-progressbar-does-not-update-progress-view-drawable
                            
                            bar.setProgress(0);
                            bar.setProgress((int) (bar.getMax() * perc / 100));
                            
                            
                    }
                }
                return v;
        }
    }


    

    
}
