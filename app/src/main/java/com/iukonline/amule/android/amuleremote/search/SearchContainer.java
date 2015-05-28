/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.search;

import com.iukonline.amule.ec.ECSearchResults;

public class SearchContainer {
    
    public enum ECSearchStatus {
        STARTING,
        RUNNING,
        FINISHED,
        STOPPED,
        FAILED
    }
    
    public String mFileName;
    public String mType;
    public String mExtension;
    public long mMinSize = -1L;
    public long mMaxSize = -1L;
    public long mAvailability = -1L;
    public byte mSearchType;
    
    public byte mSearchProgress = 0;
    public ECSearchStatus mSearchStatus = ECSearchStatus.STARTING;
    
    public ECSearchResults mResults;

}
