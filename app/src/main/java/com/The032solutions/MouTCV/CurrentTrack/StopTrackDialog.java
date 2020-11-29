package com.The032solutions.MouTCV.CurrentTrack;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.The032solutions.MouTCV.R;

class StopTrackDialog {

    Dialog create(final Context context,
                  final CurrentTrackView.CurrentTrackState trackStateBeforeDialog) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogStyle);
        builder.setMessage(R.string.stopTrackDialogMessage)
                .setPositiveButton(R.string.stopTrackDialogPositiveButton,
                        new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        if (trackStateBeforeDialog == CurrentTrackView.CurrentTrackState.START) {
                            CurrentTrackView.startTrack(null, null);
                        }
                    }
                })
                .setNegativeButton(R.string.stopTrackDialogNegativeButton, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        CurrentTrackView.endTrackAndSave();
                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialogInterface) {
                if (trackStateBeforeDialog == CurrentTrackView.CurrentTrackState.START) {
                    CurrentTrackView.startTrack(null, null);
                }
            }
        });

        return builder.create();
    }
}
