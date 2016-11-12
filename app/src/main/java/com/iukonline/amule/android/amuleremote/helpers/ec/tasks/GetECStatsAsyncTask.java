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

import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.ECStats;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;


public class GetECStatsAsyncTask extends AmuleAsyncTask {
    
    ECStats mECStats;

    @Override
    protected void backgroundTask() throws UnknownHostException, SocketTimeoutException, IOException, ECClientException, ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        mECStats = mECClient.getStats(ECCodes.EC_DETAIL_CMD);
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyECStatsWatchers(mECStats);
    }

}
