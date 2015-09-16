/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iukonline.amule.android.amuleremote.R;
import com.iukonline.amule.android.amuleremote.helpers.ec.ECCategoryParcelable;
import com.iukonline.amule.ec.ECCategory;

public class CategoryListDialogFragment extends ListDialogFragment {

    public static CategoryListDialogFragment newInstance(int title, ECCategoryParcelable[] parcelableList) {
        CategoryListDialogFragment fragment = new CategoryListDialogFragment();
        Bundle args = fragment.setAlertDialogFragmentArguments(title, true);
        args.putParcelableArray(BUNDLE_PARCELABLE_LIST, parcelableList);
        return fragment;
    }

    @NonNull
    @Override
    protected <T extends Parcelable> ArrayAdapter<T> getArrayAdapterParcelable(T[] list) {
        ArrayAdapter<T> arrayAdapter;
        if (list != null) {
            arrayAdapter = new ArrayAdapter<T>(
                    getActivity().getApplicationContext(),
                    R.layout.part_dialog_category_list_row,
                    R.id.dialog_list_text,
                    list
            ) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View v = vi.inflate(R.layout.part_dialog_category_list_row, null);

                    ECCategory cat = ((ECCategoryParcelable) mParcelableList[position]).getECCategory();
                    ((TextView) v.findViewById(R.id.dialog_category_list_text)).setText(cat.getId() == 0 ? getResources().getString(R.string.category_uncategorized) : cat.getTitle());

                    GradientDrawable d = (GradientDrawable) (((ImageView) v.findViewById(R.id.dialog_category_list_cat_box)).getDrawable());
                    d.setColor((int) (cat.getId() == 0 ? 0 : 255 << 24 | cat.getColor()));

                    return v;
                }
            };
        } else {
            arrayAdapter = new ArrayAdapter<T>(
                    getActivity().getApplicationContext(),
                    R.layout.part_dialog_category_list_row,
                    R.id.dialog_list_text
            );
        }
        return arrayAdapter;
    }
}
