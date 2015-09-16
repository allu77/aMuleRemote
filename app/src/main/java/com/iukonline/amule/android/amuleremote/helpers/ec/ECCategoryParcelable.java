/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
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
