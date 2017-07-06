/*
 * ViewerSettingsDialog.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-30.
 */
//  Copyright (c) 2014 Readium Foundation and/or its licensees. All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:
//  1. Redistributions of source code must retain the above copyright notice, this 
//  list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice, 
//  this list of conditions and the following disclaimer in the documentation and/or 
//  other materials provided with the distribution.
//  3. Neither the name of the organization nor the names of its contributors may be 
//  used to endorse or promote products derived from this software without specific 
//  prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
//  OF THE POSSIBILITY OF SUCH DAMAGE

package com.bluefire.sampleapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.bluefire.bclreader.publicclasses.BCLNavigationElement;
import com.bluefire.bclreader.publicclasses.BCLPackage;

import java.util.ArrayList;
import java.util.List;


/**
 * This dialog displays the viewer settings to the user.
 *
 */
public class TableOfContentsDialog extends DialogFragment {

    /**
     * Interface to notify the listener when a viewer settings have been changed.
     */
    public interface OnTableOfContentsChange {
        public void onTableOfContentsChange(String href);
    }

    protected static final String TAG = "ViewerSettingsDialog";

    private OnTableOfContentsChange mListener;
    private BCLPackage mPackage;
    private String mHref;

    public TableOfContentsDialog() {

    }

    @SuppressLint("ValidFragment")
    public TableOfContentsDialog(OnTableOfContentsChange listener, BCLPackage pckg) {
        mListener = listener;
        mPackage = pckg;
    }

    @Override
    public Dialog onCreateDialog(
            Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.table_of_contents_dialog, null);

        final ListView items = (ListView) dialogView.findViewById(R.id.tableOfContents);


        final BCLNavigationElement navigationTable = mPackage.getTableOfContents();

        List<String> list = flatNavigationTable(navigationTable, new ArrayList<String>(), "");
        final List<BCLNavigationElement> navigationElements = flatNavigationTable(navigationTable, new ArrayList<BCLNavigationElement>());
        StringListAdapter stringListAdapter = new StringListAdapter(getActivity().getApplicationContext(), list);
        items.setAdapter(stringListAdapter);
        items.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                BCLNavigationElement navigation = navigationElements.get(arg2);
                if (navigation instanceof BCLNavigationElement) {
                    BCLNavigationElement point = (BCLNavigationElement) navigation;
                    Log.i(TAG, "Table of Contents item selected: " + point.getContent());
                    Log.i(TAG, "Table of Contents title: " + point.getTitle());
                    Log.i(TAG, "Table of Contents estimated title: " + point.getSpineItemChapterTitle());

                    mHref = point.getContent();
                    if (mListener != null) {
                        mListener.onTableOfContentsChange(mHref);
                    }
                    dismiss();
                }

            }
        });



        builder.setView(dialogView)
                .setTitle(R.string.table_of_contents)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }

    private List<String> flatNavigationTable(BCLNavigationElement parent,
                                             List<String> list, String shift) {
        String newShift = shift + "   ";
        for (BCLNavigationElement ne : parent.getChildren()) {
            list.add(shift + ne.getTitle());
            flatNavigationTable(ne, list, newShift);
        }
        return list;
    }

    private List<BCLNavigationElement> flatNavigationTable(BCLNavigationElement parent,
                                                           List<BCLNavigationElement> list) {
        for (BCLNavigationElement ne : parent.getChildren()) {
            list.add(ne);
            flatNavigationTable(ne, list);
        }
        return list;
    }


}
