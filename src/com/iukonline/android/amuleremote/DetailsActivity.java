package com.iukonline.android.amuleremote;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.AmuleControllerApplication;
import com.iukonline.amule.android.amuleremote.GUIUtils;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECPartFile.ECPartFileComment;
import com.iukonline.android.amuleremote.echelper.AmuleWatcher.ClientStatusWatcher;
import com.iukonline.android.amuleremote.echelper.AmuleWatcher.ECPartFileWatcher;
import com.iukonline.android.amuleremote.echelper.tasks.AmuleAsyncTask.TaskScheduleMode;
import com.iukonline.android.amuleremote.echelper.tasks.ECPartFileActionAsyncTask;
import com.iukonline.android.amuleremote.echelper.tasks.ECPartFileActionAsyncTask.ECPartFileAction;
import com.iukonline.android.amuleremote.echelper.tasks.ECPartFileGetDetailsAsyncTask;

public class DetailsActivity extends Activity implements ClientStatusWatcher, ECPartFileWatcher {
    
    public final static String BUNDLE_PARAM_HASH    = "hash";
    public final static String BUNDLE_PARAM_ERRSTR    = "errstr";
    

    private final static int ID_DIALOG_DELETE                = 3;

    byte[] mHash;
    ECPartFile mPartFile;
    AmuleControllerApplication mApp;
    boolean mNeedsRefresh = false;
    
    ImageView mRefresh;
    ProgressBar mProgress; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.amuledl_details);
        setTitle(R.string.partfile_detail_title);
        
        mApp = (AmuleControllerApplication) getApplication();
        
        
        // mPartFile = mApp.mECHelper.getPartFileFromHash(getIntent().getExtras().getByteArray(BUNDLE_PARAM_HASH));
        mHash = getIntent().getExtras().getByteArray(BUNDLE_PARAM_HASH);
        
        
        mRefresh = (ImageView) findViewById(R.id.partfile_detail_button_refresh_new);
        mRefresh.setOnClickListener(
                        new View.OnClickListener() { 
                            public void onClick(View v) {
                                if (mPartFile != null) {
                                    ECPartFileGetDetailsAsyncTask refreshTask = (ECPartFileGetDetailsAsyncTask) mApp.mECHelper.getNewTask(ECPartFileGetDetailsAsyncTask.class);
                                    refreshTask.setECPartFile(mPartFile);
                                    mApp.mECHelper.executeTask(refreshTask, TaskScheduleMode.BEST_EFFORT); 
                                }
                            } 
                        });
        mProgress = (ProgressBar) findViewById(R.id.partfile_detail_refresh);
        
        // TODO CHECK IF REFRESH IS NEEDED
        //ECPartFileGetDetailsAsyncTask refreshTask = (ECPartFileGetDetailsAsyncTask) mApp.mECHelper.getNewTask(ECPartFileGetDetailsAsyncTask.class);
        //refreshTask.setECPartFile(mPartFile);
        //mApp.mECHelper.executeTask(refreshTask, TaskScheduleMode.BEST_EFFORT);
        mNeedsRefresh = true;

    }
    
    @Override
    protected void onResume() {
        super.onResume();

        notifyStatusChange(mApp.mECHelper.registerForAmuleClientStatusUpdates(this));
        updateECPartFile(mApp.mECHelper.registerForECPartFileUpdates(this, mHash));
 
        
        if (mNeedsRefresh) {
            ECPartFileGetDetailsAsyncTask refreshTask = (ECPartFileGetDetailsAsyncTask) mApp.mECHelper.getNewTask(ECPartFileGetDetailsAsyncTask.class);
            refreshTask.setECPartFile(mPartFile);
            mApp.mECHelper.executeTask(refreshTask, TaskScheduleMode.BEST_EFFORT);
            mNeedsRefresh = false;

        }
        

    }

    
    
    
    @Override
    protected void onPause() {
        
        super.onPause();
        mApp.mECHelper.unRegisterFromAmuleClientStatusUpdates(this);
        if (mPartFile != null) mApp.mECHelper.unRegisterFromECPartFileUpdates(this, mPartFile.getHash());
        
    }

    public void showProgress(boolean show) {
        if (show) {
            mProgress.setVisibility(View.VISIBLE);
            mRefresh.setVisibility(View.GONE);
        } else {
            mProgress.setVisibility(View.GONE);
            mRefresh.setVisibility(View.VISIBLE);
        }
    }
    
    private void refreshView() {

        //TODO test if mPartFile is null...
        
        Button bPause = (Button) findViewById(R.id.partfile_detail_button_pause);
        Button bResume = (Button) findViewById(R.id.partfile_detail_button_resume);
        Button bDelete = (Button) findViewById(R.id.partfile_detail_button_delete);

        
        if (mPartFile == null) {
            bPause.setEnabled(false);
            bResume.setEnabled(false);
            bDelete.setEnabled(false);
        } else {
        
            ((TextView) findViewById(R.id.partfile_detail_filename)).setText(mPartFile.getFileName());
            
            TextView tvStatus = (TextView) findViewById(R.id.partfile_detail_status);
            TextView tvPrio = (TextView) findViewById(R.id.partfile_detail_priority);
    
            
            ((TextView) findViewById(R.id.partfile_detail_link)).setText(mPartFile.getEd2kLink());
            
            
            ((TextView) findViewById(R.id.partfile_detail_done)).setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeDone()));
            ((TextView) findViewById(R.id.partfile_detail_size)).setText(GUIUtils.longToBytesFormatted(mPartFile.getSizeFull()));
            
            ((TextView) findViewById(R.id.partfile_detail_remaining)).setText("TBD");
            
            ((TextView) findViewById(R.id.partfile_detail_sources_available)).setText(Integer.toString(mPartFile.getSourceCount()));
            ((TextView) findViewById(R.id.partfile_detail_sources_active)).setText(Integer.toString(mPartFile.getSourceXfer()));
            ((TextView) findViewById(R.id.partfile_detail_sources_a4af)).setText(Integer.toString(mPartFile.getSourceA4AF()));
            ((TextView) findViewById(R.id.partfile_detail_sources_notcurrent)).setText(Integer.toString(mPartFile.getSourceNotCurrent()));
            
            bPause.setEnabled(false);
            bResume.setEnabled(false);
            bDelete.setEnabled(false);
    
            switch (mPartFile.getPrio()) {
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
        
            LinearLayout titleBar = (LinearLayout) findViewById(R.id.partfile_detail_titlebar);
        
    
            
            switch (mPartFile.getStatus()) {
            
            case ECPartFile.PS_ALLOCATING:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_allocating);
                titleBar.setBackgroundResource(R.color.progressWaitingMid);
                break;
            case ECPartFile.PS_COMPLETE:
                tvStatus.setText(R.string.partfile_status_complete);
                titleBar.setBackgroundResource(R.color.progressRunningMid);
                break;
            case ECPartFile.PS_COMPLETING:
                tvStatus.setText(R.string.partfile_status_completing);
                titleBar.setBackgroundResource(R.color.progressRunningMid);
                break;
            case ECPartFile.PS_EMPTY:
                // TODO: Check why this happens...
                if (mPartFile.getSourceXfer() > 0) {
                    tvStatus.setText(R.string.partfile_status_downloading);
                    titleBar.setBackgroundResource(R.color.progressRunningMid);
                } else if (mPartFile.getSourceCount() > 0) {
                    tvStatus.setText(R.string.partfile_status_waiting);
                    titleBar.setBackgroundResource(R.color.progressWaitingMid);
                } else {
                    tvStatus.setText(R.string.partfile_status_empty); 
                    titleBar.setBackgroundResource(R.color.progressBlockedMid);
                }
                bPause.setEnabled(true);
                bDelete.setEnabled(true);
                break;
            case ECPartFile.PS_ERROR:
                bDelete.setEnabled(true);
                
                tvStatus.setText(R.string.partfile_status_error);
                titleBar.setBackgroundResource(R.color.progressBlockedMid);
                break;
            case ECPartFile.PS_HASHING:
                tvStatus.setText(R.string.partfile_status_hashing);
                titleBar.setBackgroundResource(R.color.progressWaitingMid);
                bDelete.setEnabled(true);
                
    
                break;
            case ECPartFile.PS_INSUFFICIENT:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_insuffcient);
                titleBar.setBackgroundResource(R.color.progressBlockedMid);
                break;
            case ECPartFile.PS_PAUSED:
                tvStatus.setText(R.string.partfile_status_paused);
                titleBar.setBackgroundResource(R.color.progressStoppedMid);
                bResume.setEnabled(true);
                bDelete.setEnabled(true);
                break;
            case ECPartFile.PS_READY:
                // TODO Check why this happens
                if (mPartFile.getSourceXfer() > 0) {
                    tvStatus.setText(R.string.partfile_status_downloading);
                    tvStatus.append( " " + GUIUtils.longToBytesFormatted(mPartFile.getSpeed()) + "/s");
                    titleBar.setBackgroundResource(R.color.progressRunningMid);
                } else {
                    tvStatus.setText(R.string.partfile_status_waiting);
                    titleBar.setBackgroundResource(R.color.progressWaitingMid);
                } 
                bPause.setEnabled(true);
                bDelete.setEnabled(true);
                break;
            case ECPartFile.PS_UNKNOWN:
                tvStatus.setText(R.string.partfile_status_unknown);
                titleBar.setBackgroundResource(R.color.progressBlockedMid);
                break;
            case ECPartFile.PS_WAITINGFORHASH:
                // TODO What's this?
                tvStatus.setText(R.string.partfile_status_waitingforhash);
                titleBar.setBackgroundResource(R.color.progressWaitingMid);
                bDelete.setEnabled(true);
    
                break;
                
            default:
                tvStatus.setText("UNKNOWN-" + mPartFile.getStatus());
                titleBar.setBackgroundResource(R.color.progressBlockedMid);
    
                break;
            }
            
            ArrayList <ECPartFile.ECPartFileComment> commentList = mPartFile.getComments();
            ArrayList <ECPartFile.ECPartFileSourceName> sourceNameList = mPartFile.getSourceNames();
            
            if (commentList.isEmpty()) {
                findViewById(R.id.partfile_detail_comments).setVisibility(View.GONE);
            } else {
                findViewById(R.id.partfile_detail_comments).setVisibility(View.VISIBLE);
                CommentListAdapter commentsAdapter = new CommentListAdapter(this, R.id.partfile_detail_comments_list, commentList);
    
            }
            
            if (sourceNameList.isEmpty()) {
                findViewById(R.id.partfile_detail_sourcenames).setVisibility(View.GONE);
            } else {
                findViewById(R.id.partfile_detail_sourcenames).setVisibility(View.VISIBLE);
                SourceNameListAdapter sourceNamesAdapter = new SourceNameListAdapter(this, R.id.partfile_detail_sourcenames_list, sourceNameList);
                ListView lv1 = (ListView) findViewById(R.id.partfile_detail_sourcenames_list);
                lv1.setAdapter(sourceNamesAdapter);
    
            }
            
            if (sourceNameList.isEmpty() && commentList.isEmpty()) {
                findViewById(R.id.partfile_detail_dummylayout).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.partfile_detail_dummylayout).setVisibility(View.GONE);
            }
        
            bPause.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { doAction(ECPartFileAction.PAUSE); } });
            bResume.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { doAction(ECPartFileAction.RESUME); } });
            bDelete.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { showDialog(ID_DIALOG_DELETE); } });
        }
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
        case ID_DIALOG_DELETE:
            AlertDialog.Builder builderDel = new AlertDialog.Builder(this);
            builderDel.setMessage(new String("Are you sure?"))
                   .setCancelable(false)
                   .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dialog.dismiss();
                           doAction(ECPartFileAction.DELETE); 
                       }
                   })
                   .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dialog.dismiss();                           
                       }
                   }
                   );
            return builderDel.create();

        default:
            return super.onCreateDialog(id);
        }
    }
    
    private void doAction(ECPartFileAction actionType ) {
        ECPartFileActionAsyncTask actionTask = (ECPartFileActionAsyncTask) mApp.mECHelper.getNewTask(ECPartFileActionAsyncTask.class);
        actionTask.setECPartFile(mPartFile).setAction(actionType);
        ECPartFileGetDetailsAsyncTask detailsTask = (ECPartFileGetDetailsAsyncTask) mApp.mECHelper.getNewTask(ECPartFileGetDetailsAsyncTask.class);
        detailsTask.setECPartFile(mPartFile);
        
        mApp.mECHelper.executeTask(actionTask, TaskScheduleMode.QUEUE);
        mApp.mECHelper.executeTask(detailsTask, TaskScheduleMode.QUEUE);
        
        
        /*
        new AsyncTask<Integer, Void, String> () {

            @Override
            protected void onPreExecute() {
                setRequestedOrientation(getResources().getConfiguration().orientation);
                mApp.mainNeedsRefresh = true;
                //showDialog(ID_DIALOG_SENDING_COMMAND);
                showProgress(true);
            }

            @Override
            protected String doInBackground(Integer... params) {
                
                try {
                    mApp.getECClient(this, AmuleControllerApplication.AC_GET_CLIENT_MODE_PREEMPTIVE); // Lock client for this task
                    
                    int actionType = params[0];
                    switch (actionType) {
                    case ACTION_DELETE:
                        mPartFile.remove();
                        if (isCancelled()) return null;
                        mApp.releaseECClient(this, false);
                        finish();
                        return null;
                    case ACTION_PAUSE:
                        mPartFile.pause();
                        break;
                    case ACTION_RESUME:
                        mPartFile.resume();
                        break;
                    case ACTION_A4AF_NOW:
                        mPartFile.swapA4AFThis();
                        break;
                    case ACTION_A4AF_AUTO:
                        mPartFile.swapA4AFThisAuto();
                        break;
                    case ACTION_A4AF_AWAY:
                        mPartFile.swapA4AFOthers();
                        break;
                    case ACTION_PRIO_LOW:
                        mPartFile.changePriority(ECPartFile.PR_LOW);
                        break;
                    case ACTION_PRIO_NORMAL:
                        mPartFile.changePriority(ECPartFile.PR_NORMAL);
                        break;
                    case ACTION_PRIO_HIGH:
                        mPartFile.changePriority(ECPartFile.PR_HIGH);
                        break;
                    case ACTION_PRIO_AUTO:
                        mPartFile.changePriority(ECPartFile.PR_AUTO);
                        break;
                    case ACTION_REFRESH:
                        //mPartFile.refresh(true);
                        break;
                    }

                    if (isCancelled()) return null;
                    mPartFile.refresh(true);
                    
                } catch (ECException e) {
                    if (isCancelled()) return null;
                    return new String("EC - " + e.getLocalizedMessage());
                } catch (UnknownHostException e) {
                    if (isCancelled()) return null;
                    return new String("HOST - " + e.getLocalizedMessage());
                } catch (SocketTimeoutException e) {
                    if (isCancelled()) return null;
                    return new String("Socket timeout");
                } catch (IOException e) {
                    if (isCancelled()) return null;
                    return new String("IO - " + e.getLocalizedMessage());
                }
                
                return null;
            }
            
            @Override
            protected void onCancelled() {
                //removeDialog(ID_DIALOG_SENDING_COMMAND);
                showProgress(false);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                mApp.releaseECClient(this, false);
                // TODO: RESET CLIENT?
            }

            @Override
            protected void onPostExecute(String result) {
                //removeDialog(ID_DIALOG_SENDING_COMMAND);
                showProgress(false);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                
                if (result == null) {
                    mApp.releaseECClient(this, false);
                    refreshView();
                } else {
                    mApp.releaseECClient(this, true);
                    Bundle b = new Bundle();
                    b.putString(BUNDLE_PARAM_ERRSTR, result);
                    showDialog(ID_DIALOG_COMMAND_ERROR, b);
                }
            }
        }.execute(actionType);
        */
        
    }
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_options, menu);
        
        return super.onCreateOptionsMenu(menu);
    }
       
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        if (! super.onPrepareOptionsMenu(menu)) {
            return false;
        }
        
        MenuItem mA4AF = menu.findItem(R.id.menu_detail_opt_a4af);
        
        switch (mPartFile.getStatus()) {
        case ECPartFile.PS_ALLOCATING:
        case ECPartFile.PS_PAUSED:
        case ECPartFile.PS_READY:
            mA4AF.setEnabled(true);
            break;
        default:
            mA4AF.setEnabled(false);
            break;
        }

        
        return true;
        
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_detail_opt_prio_auto:
            doAction(ECPartFileAction.PRIO_AUTO);
            return true;
        case R.id.menu_detail_opt_prio_high:
            doAction(ECPartFileAction.PRIO_HIGH);
            return true;
        case R.id.menu_detail_opt_prio_normal:
            doAction(ECPartFileAction.PRIO_NORMAL);
            return true;
        case R.id.menu_detail_opt_prio_low:
            doAction(ECPartFileAction.PRIO_LOW);
            return true;
        
        case R.id.menu_detail_opt_a4af_now:
            doAction(ECPartFileAction.A4AF_NOW);
            return true;
        case R.id.menu_detail_opt_a4af_auto:
            doAction(ECPartFileAction.A4AF_AUTO);
            return true;
        case R.id.menu_detail_opt_a4af_away:
            doAction(ECPartFileAction.A4AF_AWAY);
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    
    
    
    
    
    
    private class CommentListAdapter extends ArrayAdapter<ECPartFile.ECPartFileComment> {

        private ArrayList<ECPartFileComment> items;

        public CommentListAdapter(Context context, int textViewResourceId, ArrayList<ECPartFile.ECPartFileComment> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.amuledl_comments_row, null);
                }
                ECPartFile.ECPartFileComment o = items.get(position);
                ((TextView) v.findViewById(R.id.amuledl_comments_row_author)).setText(o.author);
                ((TextView) v.findViewById(R.id.amuledl_comments_row_rating)).setText(o.rating);
                ((TextView) v.findViewById(R.id.amuledl_comments_row_filename)).setText(o.sourceName);
                ((TextView) v.findViewById(R.id.amuledl_comments_row_comment)).setText(o.comment);
                
                return v;
        }
    }
        
    private class SourceNameListAdapter extends ArrayAdapter<ECPartFile.ECPartFileSourceName> {

        private ArrayList<ECPartFile.ECPartFileSourceName> items;

        public SourceNameListAdapter(Context context, int textViewResourceId, ArrayList<ECPartFile.ECPartFileSourceName> items) {
                super(context, textViewResourceId, items);
                this.items = items;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
                View v = convertView;
                if (v == null) {
                    LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    v = vi.inflate(R.layout.amuledl_sourcenames_row, null);
                }
                ECPartFile.ECPartFileSourceName o = items.get(position);
                ((TextView) v.findViewById(R.id.amuledl_sourcenames_count)).setText(Integer.toString(o.count));
                ((TextView) v.findViewById(R.id.amuledl_sourcenames_name)).setText(o.name);
                return v;
        }

    }

    @Override
    public int getWatcherId() {
        // TODO Implement differentiation by hash...
        return AmuleControllerApplication.AMULE_ACTIVITY_ID_DETAILS;
    }

    @Override
    public void notifyStatusChange(AmuleClientStatus status) {
        switch (status) {
        case WORKING:
            showProgress(true);
            break;
        default:
            showProgress(false);
        }
    }

    @Override
    public void updateECPartFile(ECPartFile newECPartFile) {
        // TODO: Check if hash is the same...
        if (mPartFile == null) mPartFile = newECPartFile;
        // We shouldn't need to re-assign mPartFile, since this should be the same modified...
        refreshView();
    }
    
}
