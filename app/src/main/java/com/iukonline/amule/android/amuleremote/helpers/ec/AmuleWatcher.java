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

package com.iukonline.amule.android.amuleremote.helpers.ec;

import com.iukonline.amule.android.amuleremote.helpers.ec.tasks.ECPartFileActionAsyncTask.ECPartFileAction;
import com.iukonline.amule.android.amuleremote.search.SearchContainer;
import com.iukonline.amule.ec.ECCategory;
import com.iukonline.amule.ec.ECPartFile;
import com.iukonline.amule.ec.ECStats;

import java.util.ArrayList;
import java.util.HashMap;


public abstract interface AmuleWatcher {
    public interface ClientStatusWatcher extends AmuleWatcher {
        
        public enum AmuleClientStatus {
            IDLE, ERROR, WORKING, NOT_CONNECTED
        }
    
        void notifyStatusChange(AmuleClientStatus status); 
    }

    public interface DlQueueWatcher extends AmuleWatcher {
        void updateDlQueue(HashMap<String, ECPartFile> newDlQueue);
    }

    public interface ECStatsWatcher extends AmuleWatcher {
        void updateECStats(ECStats newStats);
    }
    
    public interface CategoriesWatcher extends AmuleWatcher {
        void updateCategories(ECCategory[] newCategoryList);
    }
    
    public interface ECPartFileWatcher extends AmuleWatcher {
        void updateECPartFile(ECPartFile newECPartFile);
    }

    public interface ECPartFileActionWatcher extends AmuleWatcher {
        void notifyECPartFileActionDone(ECPartFileAction action);
    }
    
    public interface ECSearchListWatcher extends AmuleWatcher {
        public enum UpdateResult { UNREGISTER, DO_NOTHING }
        UpdateResult updateECSearchList(ArrayList <SearchContainer> searches);
    }
    
    String getWatcherId();
}

