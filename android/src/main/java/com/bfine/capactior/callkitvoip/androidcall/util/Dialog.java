package com.bfine.capactior.callkitvoip.androidcall.util;


import android.content.Context;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.bfine.capactior.callkitvoip.R;

public class Dialog {

    public static AlertDialog createConnectDialog(
            EditText participantEditText,
            DialogInterface.OnClickListener callParticipantsClickListener,
            DialogInterface.OnClickListener cancelClickListener,
            Context context) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        alertDialogBuilder.setIcon(R.drawable.ic_videocam_green_24px);
        alertDialogBuilder.setTitle("Connect to a room");
        alertDialogBuilder.setPositiveButton("Connect", callParticipantsClickListener);
        alertDialogBuilder.setNegativeButton("Cancel", cancelClickListener);
        alertDialogBuilder.setCancelable(false);

        setRoomNameFieldInDialog(participantEditText, alertDialogBuilder);

        return alertDialogBuilder.create();
    }

    private static void setRoomNameFieldInDialog(
            EditText roomNameEditText, AlertDialog.Builder alertDialogBuilder) {
        roomNameEditText.setHint("room name");
        alertDialogBuilder.setView(roomNameEditText);
    }
}