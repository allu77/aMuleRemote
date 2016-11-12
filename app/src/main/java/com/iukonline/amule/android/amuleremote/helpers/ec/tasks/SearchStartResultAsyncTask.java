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

package com.iukonline.amule.android.amuleremote.helpers.ec.tasks;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.widget.Toast;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.ec.ECSearchFile;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

public class SearchStartResultAsyncTask extends AmuleAsyncTask {
    
    private ECSearchFile mECSearchFile;
    
    public void setECSearchFile(ECSearchFile sf) {
        mECSearchFile = sf;
    }

    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, AmuleAsyncTaskException, ECClientException,
                    ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        mECClient.searchStartResult(mECSearchFile);
        
    }

    @Override
    protected void notifyResult() {

        Toast.makeText(
            mECHelper.mApp, 
            mECHelper.mApp.getResources().getString(R.string.search_details_file_added, mECSearchFile.getFileName()), 
            Toast.LENGTH_LONG
        ).show();
    }

}
