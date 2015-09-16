/*
 * Copyright (c) 2015. Gianluca Vegetti, All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 */

package com.iukonline.amule.android.amuleremote.helpers.gui.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.iukonline.amule.android.amuleremote.R;

public class ListDialogFragment extends AlertDialogFragment {

    protected final static String BUNDLE_STRING_LIST = "string_list";
    protected final static String BUNDLE_PARCELABLE_LIST = "parcelable_list";

    public final static String BUNDLE_LIST_SELECTED_INDEX = "list_selected_index";
    public final static String BUNDLE_LIST_SELECTED_STRING = "list_selected_string";
    public final static String BUNDLE_LIST_SELECTED_PARCELABLE = "list_selected_parcelable";

    private String[] mStringList;
    private Parcelable[] mParcelableList;

    public static ListDialogFragment newInstance(int title, String[] stringList) {
        ListDialogFragment fragment = new ListDialogFragment();
        Bundle args = fragment.setAlertDialogFragmentArguments(title, true);
        args.putStringArray(BUNDLE_STRING_LIST, stringList);
        return fragment;
    }

    public static ListDialogFragment newInstance(int title, Parcelable[] parcelableList) {
        ListDialogFragment fragment = new ListDialogFragment();
        Bundle args = fragment.setAlertDialogFragmentArguments(title, true);
        args.putParcelableArray(BUNDLE_PARCELABLE_LIST, parcelableList);
        return fragment;
    }

    public ListDialogFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // STRING_LIST is supposed to be immutable, so no point of saving instance state.
        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(BUNDLE_STRING_LIST)) mStringList = args.getStringArray(BUNDLE_STRING_LIST);
            if (args.containsKey(BUNDLE_PARCELABLE_LIST)) mParcelableList = args.getParcelableArray(BUNDLE_PARCELABLE_LIST);
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = getDefaultAlertDialogBuilder(savedInstanceState);

        if (mStringList != null) {
            ArrayAdapter<String> arrayAdapter = getArrayAdapterString(mStringList);

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Bundle b = new Bundle();
                    b.putInt(BUNDLE_LIST_SELECTED_INDEX, i);
                    b.putString(BUNDLE_LIST_SELECTED_STRING, mStringList[i]);
                    sendEventToListener(ALERTDIALOG_EVENT_OK, b);
                }
            });
        } else {
            ArrayAdapter<Parcelable> arrayAdapter = getArrayAdapterParcelable(mParcelableList);

            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Bundle b = new Bundle();
                    b.putInt(BUNDLE_LIST_SELECTED_INDEX, i);
                    b.putParcelable(BUNDLE_LIST_SELECTED_PARCELABLE, mParcelableList[i]);
                    sendEventToListener(ALERTDIALOG_EVENT_OK, b);
                }
            });

        }

        return builder.create();
    }

    @NonNull
    protected <T extends String> ArrayAdapter<T> getArrayAdapterString(T[] list) {
        ArrayAdapter<T> arrayAdapter;
        if (list != null) {
            arrayAdapter = new ArrayAdapter<T>(
                    getActivity().getApplicationContext(),
                    R.layout.part_dialog_list_row,
                    R.id.dialog_list_text,
                    list
            );
        } else {
            arrayAdapter = new ArrayAdapter<T>(
                    getActivity().getApplicationContext(),
                    R.layout.part_dialog_list_row,
                    R.id.dialog_list_text
            );
        }
        return arrayAdapter;
    }

    @NonNull
    protected <T extends Parcelable> ArrayAdapter<T> getArrayAdapterParcelable(T[] list) {
        ArrayAdapter<T> arrayAdapter;
        if (list != null) {
            arrayAdapter = new ArrayAdapter<T>(
                    getActivity().getApplicationContext(),
                    R.layout.part_dialog_list_row,
                    R.id.dialog_list_text,
                    list
            );
        } else {
            arrayAdapter = new ArrayAdapter<T>(
                    getActivity().getApplicationContext(),
                    R.layout.part_dialog_list_row,
                    R.id.dialog_list_text
            );
        }
        return arrayAdapter;
    }


}
