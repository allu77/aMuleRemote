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

import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECCodes;
import com.iukonline.amule.ec.exceptions.ECClientException;
import com.iukonline.amule.ec.exceptions.ECPacketParsingException;
import com.iukonline.amule.ec.exceptions.ECServerException;

import java.io.IOException;

public class GetCategoriesAsyncTask extends AmuleAsyncTask {
    
    private ECCategory[] mCategoryList;

    @Override
    protected void backgroundTask() throws IOException, ECClientException, ECPacketParsingException, ECServerException {
        if (isCancelled()) return;
        mCategoryList = mECClient.getCategories(ECCodes.EC_DETAIL_FULL);
    }

    @Override
    protected void notifyResult() {
        mECHelper.notifyCategoriesWatchers(mCategoryList);
    }

}
