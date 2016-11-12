/*
  ~ Copyright (c) 2015. Gianluca Vegetti
  ~ 
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU General Public License as published by
  ~ the Free Software Foundation, either version 3 of the License, or
  ~ (at your option) any later version.
  ~ 
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~ GNU General Public License for more details.
  ~ 
  ~ You should have received a copy of the GNU General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.iukonline.amule.android.amuleremote;

public class Flavor {
    public final static String UPDATE_CHECKER_URL_PREFIX = "https://dl.dropboxusercontent.com/u/15068406/AmuleRemote/SNAPSHOT/aMuleRemote-update-";
    public final static long UPDATE_CHECKER_INTERVAL = 60L*60L*1000L;
    public final static boolean UPDATE_CHECKER_CHECK_BUILD = true;

    public final static boolean ACRA_ENABLED = false;
}
