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
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import com.bluefire.bclreader.publicclasses.BCLReaderSettings;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * This dialog displays the viewer settings to the user.
 *
 */
@SuppressLint("ValidFragment")
public class ViewerSettingsDialog extends DialogFragment {

    /**
     * Interface to notify the listener when a viewer settings have been changed.
     */
    public interface OnViewerSettingsChange {
        public void onViewerSettingsChange(BCLReaderSettings settings);
    }

    protected static final String TAG = "ViewerSettingsDialog";

    private OnViewerSettingsChange mListener;

    private BCLReaderSettings mOriginalSettings;

    public ViewerSettingsDialog(OnViewerSettingsChange listener, BCLReaderSettings originalSettings) {
        mListener = listener;
        mOriginalSettings = originalSettings;
    }

    @Override
    public Dialog onCreateDialog(
            Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate correct settings into settings fragment wrapper (either reflowable or FXL)
        View v;
        final boolean isFixedLayout = ((ReaderControllerActivity) getActivity()).isFixedLayout();
        if (isFixedLayout) {
            v = inflater.inflate(R.layout.viewer_settings_fixed_layout, null);
        } else {
            v = inflater.inflate(R.layout.viewer_settings, null);
        }

        final View dialogView = v;

        NumberPicker textSizeNumberPicker = new NumberPicker(getContext());
        NumberPicker marginWidthNumberPicker = new NumberPicker(getContext());
        RadioGroup themeGroup = new RadioGroup(getContext());
        CheckBox justifiedText = new CheckBox(getContext());

        if (!isFixedLayout) {

            // Text Size
            textSizeNumberPicker = (NumberPicker) dialogView.findViewById(R.id.fontSize);
            textSizeNumberPicker.setValue((int) mOriginalSettings.getTextScale());

            // Margin Width
            marginWidthNumberPicker = (NumberPicker) dialogView.findViewById(R.id.marginWidth);
            marginWidthNumberPicker.setValue((int) (mOriginalSettings.getMarginAmount() * 10));

            // Theme
            themeGroup = (RadioGroup) dialogView.findViewById(R.id.themeSettings);
            switch (mOriginalSettings.getTheme()) {
                case BCL_READER_THEME_DEFAULT:
                    themeGroup.check(R.id.themeDefault);
                    break;
                case BCL_READER_THEME_NIGHT:
                    themeGroup.check(R.id.themeNight);
                    break;
                case BCL_READER_THEME_SEPIA:
                    themeGroup.check(R.id.themeSepia);
                    break;
            }

            // Justified Text
            justifiedText = (CheckBox) dialogView.findViewById(R.id.checkbox_justified_text);
            justifiedText.setChecked(mOriginalSettings.getIsTextAlignmentJustified());

        }

        // Columns
        final RadioGroup spreadGroup = (RadioGroup) dialogView.findViewById(R.id.spreadSettings);
        switch (mOriginalSettings.getColumnMode()) {
            case BCL_READER_COLUMN_MODE_DEFAULT:
                spreadGroup.check(R.id.spreadAuto);
                break;
            case BCL_READER_COLUMN_MODE_SINGLE:
                spreadGroup.check(R.id.spreadSingle);
                break;
            case BCL_READER_COLUMN_MODE_DOUBLE:
                spreadGroup.check(R.id.spreadDouble);
                break;
        }


        // Media Overlay: Tap to Play
        final CheckBox tapToPlay = (CheckBox) dialogView.findViewById(R.id.checkbox_tap_to_play);
        tapToPlay.setChecked(mOriginalSettings.getIsMediaOverlayTapToPlayEnabled());

        final NumberPicker dialogTextSizeNumberPicker = textSizeNumberPicker;
        final NumberPicker dialogMarginWidthNumberPicker = marginWidthNumberPicker;
        final RadioGroup dialogThemeGroup = themeGroup;
        final CheckBox dialogJustifiedText = justifiedText;



        builder.setView(dialogView)
                .setTitle(R.string.settings)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            double textScale = 100;
                            double marginAmount = 0;
                            BCLReaderSettings.BCLReaderTheme theme = BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_DEFAULT;
                            boolean isTextAlignmentJustified = false;

                            if (!isFixedLayout) {
                                // Text Scale
                                textScale = dialogTextSizeNumberPicker.getValue();

                                // Margin Width
                                marginAmount = dialogMarginWidthNumberPicker.getValue();

                                // Theme
                                theme = null;
                                switch (dialogThemeGroup.getCheckedRadioButtonId()) {
                                    case R.id.themeDefault:
                                        theme = BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_DEFAULT;
                                        break;
                                    case R.id.themeNight:
                                        theme = BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_NIGHT;
                                        break;
                                    case R.id.themeSepia:
                                        theme = BCLReaderSettings.BCLReaderTheme.BCL_READER_THEME_SEPIA;
                                        break;
                                }


                                isTextAlignmentJustified = dialogJustifiedText.isChecked();
                            }

                            // Column Mode
                            BCLReaderSettings.BCLReaderColumnMode columnMode = null;
                            switch (spreadGroup.getCheckedRadioButtonId()) {
                                case R.id.spreadAuto:
                                    columnMode = BCLReaderSettings.BCLReaderColumnMode.BCL_READER_COLUMN_MODE_DEFAULT;
                                    break;
                                case R.id.spreadSingle:
                                    columnMode = BCLReaderSettings.BCLReaderColumnMode.BCL_READER_COLUMN_MODE_SINGLE;
                                    break;
                                case R.id.spreadDouble:
                                    columnMode = BCLReaderSettings.BCLReaderColumnMode.BCL_READER_COLUMN_MODE_DOUBLE;
                                    break;
                            }


                            boolean isMediaOverlayTapToPlayEnabled = tapToPlay.isChecked();

                            BCLReaderSettings settings = new BCLReaderSettings(columnMode, isMediaOverlayTapToPlayEnabled, isTextAlignmentJustified, marginAmount * 0.1, textScale, theme);
                            mListener.onViewerSettingsChange(settings);
                        }
                        dismiss();
                    }

                    private int parseString(String s, int defaultValue) {
                        try {
                            return Integer.parseInt(s);
                        } catch (Exception e) {
                            Log.e(TAG, "" + e.getMessage(), e);
                        }
                        return defaultValue;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }

}
