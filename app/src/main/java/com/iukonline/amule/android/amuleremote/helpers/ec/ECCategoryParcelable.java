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

import android.os.Parcel;
import android.os.Parcelable;

import com.iukonline.amule.ec.ECCategory;

public class ECCategoryParcelable implements Parcelable {

    private ECCategory mECCategory;

    public static final Parcelable.Creator<ECCategoryParcelable> CREATOR = new Parcelable.Creator<ECCategoryParcelable>() {
        public ECCategoryParcelable createFromParcel(Parcel in) {
            return new ECCategoryParcelable(in);
        }

        public ECCategoryParcelable[] newArray(int size) {
            return new ECCategoryParcelable[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mECCategory.getId());
        parcel.writeString(mECCategory.getTitle());
        parcel.writeString(mECCategory.getPath());
        parcel.writeString(mECCategory.getComment());
        parcel.writeLong(mECCategory.getColor());
        parcel.writeByte(mECCategory.getPrio());
    }

    public ECCategoryParcelable(ECCategory category) {
        mECCategory = category;
    }

    public ECCategoryParcelable(Parcel parcel) {
        mECCategory = new ECCategory(
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readLong(),
                parcel.readByte()
        );
    }

    public static ECCategoryParcelable[] convertArray(ECCategory[] cat) {
        ECCategoryParcelable[] parcelables = new ECCategoryParcelable[cat.length];
        for (int i = 0; i < cat.length; i++) {
            parcelables[i] = new ECCategoryParcelable(cat[i]);
        }
        return parcelables;
    }

    public ECCategory getECCategory() { return mECCategory; }

    public String toString() { return mECCategory != null ? mECCategory.getTitle() : "" ; }
}
